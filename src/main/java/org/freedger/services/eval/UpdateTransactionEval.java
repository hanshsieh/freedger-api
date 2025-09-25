package org.freedger.services.eval;

import org.freedger.services.eval.models.EvalContext;
import org.freedger.services.eval.models.InputItem;
import org.freedger.services.eval.models.TransactionDraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.JsonValue;
import com.openai.core.MultipartField;
import com.openai.models.evals.EvalCreateParams;
import com.openai.models.evals.EvalCreateParams.DataSourceConfig;
import com.openai.models.evals.EvalCreateParams.TestingCriterion;
import com.openai.models.evals.runs.RunCreateParams;
import com.openai.models.evals.runs.RunCreateParams.DataSource.CreateEvalResponsesRunDataSource;
import com.openai.models.evals.runs.RunCreateParams.DataSource.CreateEvalResponsesRunDataSource.InputMessages.Template.InnerTemplate.ChatMessage;
import com.openai.models.files.FileCreateParams;
import com.openai.models.files.FileObject;
import com.openai.models.files.FilePurpose;
import com.openai.models.responses.ResponseFormatTextJsonSchemaConfig;

public class UpdateTransactionEval implements Closeable {
  private static final Logger logger = LoggerFactory.getLogger(UpdateTransactionEval.class);
  private static final String CONTEXT_FILE = "prompts/update_transaction/evals/context.json";
  private static final String INPUT_FILE = "prompts/update_transaction/evals/input.jsonl";
  private static final String PYTHON_GRADER_FILE = "prompts/update_transaction/evals/test_criterion.py";
  private static final String INTRO_PROMPT_FILE = "prompts/update_transaction/inputs/intro.md";
  private static final String CONTEXT_PROMPT_FILE = "prompts/update_transaction/inputs/context.md";
  private static final String STATUS_PROMPT_FILE = "prompts/update_transaction/inputs/status.md";
  private static final Duration FILE_EXPIRE_TIME = Duration.ofHours(1);
  private final ObjectMapper objectMapper = new ObjectMapper();
  private OpenAIClient client = OpenAIOkHttpClient.fromEnv();

  public static void main(String[] args) throws Exception {
    try (var eval = new UpdateTransactionEval()) {
      eval.run();
    }
  }

  public void run() throws Exception {
    final var context = objectMapper.readValue(
      EvalUtils.loadResourceAsString(CONTEXT_FILE), EvalContext.class);
    
    String evalId = context.evalId;
    if (evalId == null) {
      logger.info("Eval ID is not set, creating a new eval");
      evalId = createEval();
    }
    
    FileObject fileObj = uploadFile(INPUT_FILE);

    submitRun(context, evalId, fileObj);
  }

  String createEval() {
    final var pythonSource = EvalUtils.loadResourceAsString(PYTHON_GRADER_FILE);
    final var params = EvalCreateParams.builder()
    .name("Update Transaction Draft")
    .dataSourceConfig(DataSourceConfig.ofCustom(DataSourceConfig.Custom.builder()
      .itemSchema(EvalUtils.extractJsonSchema(InputItem.class))
      .includeSampleSchema(true)
      .build()))
    .addTestingCriterion(EvalCreateParams.TestingCriterion.ofPython(TestingCriterion.Python.builder()
      .name("Match output with ground truth")
      .source(pythonSource)
      .passThreshold(0.8)
      .build()))
    .build();
    final var eval = client.evals().create(params);
    logger.info("Eval ID: {}", eval.id());
    return eval.id();
  }

  FileObject uploadFile(String resourcePath) throws IOException {
    logger.info("Uploading file: {}", resourcePath);
    try (var fileStream = EvalUtils.loadResourceAsStream(resourcePath)) {
      var field = MultipartField.<InputStream>builder()
        .value(fileStream)
        .filename("input.jsonl")
        .build();
      final var file = client.files().create(FileCreateParams.builder()
        .purpose(FilePurpose.EVALS)
        .file(field)
        .expiresAfter(FileCreateParams.ExpiresAfter.builder()
          .anchor(JsonValue.from("created_at"))
          .seconds(FILE_EXPIRE_TIME.getSeconds())
          .build())
        .build());
      logger.info("File ID: {}", file.id());
      return file;
    }
  }

  void submitRun(EvalContext context, String evalId, FileObject fileObj) throws JsonProcessingException {
    final var inputTemplate = createInputTemplate(context);
    final var run = client.evals().runs().create(RunCreateParams.builder()
      .name(context.evalRunName)
      .evalId(evalId)
      .dataSource(CreateEvalResponsesRunDataSource.builder()
        .model(context.model)
        .samplingParams(CreateEvalResponsesRunDataSource.SamplingParams.builder()
          // SDK doesn't yet support reasoning_effort
          .putAdditionalProperty("reasoning_effort", JsonValue.from(context.reasoningEffort))
          .text(CreateEvalResponsesRunDataSource.SamplingParams.Text.builder()
            // The API doesn't yet support verbosity. 
            // See https://community.openai.com/t/cannot-set-verbosity-for-gpt-5-evals/1354524?
            .format(createOutputSchema())
            .build())
          .build())
        .type(CreateEvalResponsesRunDataSource.Type.RESPONSES)
        .fileIdSource(fileObj.id())
        .inputMessages(inputTemplate)
        .build())
      .build());
    logger.info("Run ID: {}", run.id());
  }

  private ResponseFormatTextJsonSchemaConfig createOutputSchema() {
    return ResponseFormatTextJsonSchemaConfig.builder()
      .name("UpdateTransactionDraft")
      .schema(EvalUtils.extractJsonSchema(TransactionDraft.class))
      .strict(true)
      .build();
  }

  private CreateEvalResponsesRunDataSource.InputMessages.Template createInputTemplate(
    EvalContext context) throws JsonProcessingException {
    final var introPrompt = EvalUtils.loadResourceAsString(INTRO_PROMPT_FILE);
    final var contextPromptTemplate = EvalUtils.loadResourceAsString(CONTEXT_PROMPT_FILE);
    final var statusPromptTemplate = EvalUtils.loadResourceAsString(STATUS_PROMPT_FILE);

    var contextPrompt = contextPromptTemplate;
    contextPrompt = contextPrompt.replace("{{currentTime}}", context.currentTime);
    contextPrompt = contextPrompt.replace("{{timeZone}}", context.timeZone);
    contextPrompt = contextPrompt.replace("{{locale}}", context.locale);
    contextPrompt = contextPrompt.replace("{{defaultExternalAccountId}}", context.defaultExternalAccountId);
    contextPrompt = contextPrompt.replace("{{defaultCurrencyId}}", context.defaultCurrencyId);
    contextPrompt = contextPrompt.replace("{{currencies}}", objectMapper.writeValueAsString(context.currencies));
    contextPrompt = contextPrompt.replace("{{accounts}}", objectMapper.writeValueAsString(context.accounts));
    contextPrompt = contextPrompt.replace("{{categories}}", objectMapper.writeValueAsString(context.categories));
    contextPrompt = contextPrompt.replace("{{platforms}}", objectMapper.writeValueAsString(context.platforms));
    contextPrompt = contextPrompt.replace("{{tags}}", objectMapper.writeValueAsString(context.tags));

    var statusPrompt = statusPromptTemplate;
    statusPrompt = statusPrompt.replace("{{transaction}}", "{{item.%s}}".formatted(InputItem.INITIAL_TRANSACTION_KEY));

    final var inputTemplate = CreateEvalResponsesRunDataSource.InputMessages.Template.builder()
      .addTemplate(ChatMessage.builder()
        .role("developer")
        .content(introPrompt)
        .build())
      .addTemplate(ChatMessage.builder()
        .role("developer")
        .content(contextPrompt)
        .build())
      .addTemplate(ChatMessage.builder()
        .role("developer")
        .content(statusPrompt)
        .build())
      .addTemplate(ChatMessage.builder()
        .role("user")
        .content("{{item.%s}}".formatted(InputItem.USER_MESSAGE_KEY))
        .build())
      .build();
    return inputTemplate;
  }

  @Override
  public void close() throws IOException {
    client.close();
  }
}
