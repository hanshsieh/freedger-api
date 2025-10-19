package org.freedger.tools.eval;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.JsonValue;
import com.openai.core.MultipartField;
import com.openai.models.evals.EvalCreateParams;
import com.openai.models.evals.EvalCreateParams.DataSourceConfig;
import com.openai.models.evals.EvalCreateParams.TestingCriterion;
import com.openai.models.evals.runs.RunCreateParams;
import com.openai.models.evals.runs.RunCreateParams.DataSource.CreateEvalResponsesRunDataSource;
import com.openai.models.files.FileCreateParams;
import com.openai.models.files.FileObject;
import com.openai.models.files.FilePurpose;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.Duration;
import org.freedger.tools.eval.models.EvalContext;
import org.freedger.tools.eval.models.InputConfig;
import org.freedger.tools.eval.models.InputItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateTransactionEval implements Closeable {
  private static final Logger logger = LoggerFactory.getLogger(UpdateTransactionEval.class);
  private static final InputConfig[] INPUT_CONFIGS =
      new InputConfig[] {new InputConfig("prompts/update_transaction/evals/input_1.jsonl", 1)};
  private static final String PYTHON_GRADER_FILE =
      "prompts/update_transaction/evals/test_criterion.py";
  private static final Duration FILE_EXPIRE_TIME = Duration.ofHours(1);
  private final UpdateTransactionUtils utils = new UpdateTransactionUtils();
  private OpenAIClient client = OpenAIOkHttpClient.fromEnv();

  public static void main(String[] args) throws Exception {
    try (var eval = new UpdateTransactionEval()) {
      eval.run();
    }
  }

  public void run() throws Exception {
    final var context = utils.loadContext();

    String evalId = context.evalId;
    if (evalId == null) {
      logger.info("Eval ID is not set, creating a new eval");
      evalId = createEval();
    }

    for (final var inputConfig : INPUT_CONFIGS) {
      FileObject fileObj = uploadFile(inputConfig.filePath);
      submitRun(context, evalId, fileObj, inputConfig);
    }
  }

  String createEval() {
    final var pythonSource = UpdateTransactionUtils.loadResourceAsString(PYTHON_GRADER_FILE);
    final var params =
        EvalCreateParams.builder()
            .name("Update Transaction Draft")
            .dataSourceConfig(
                DataSourceConfig.ofCustom(
                    DataSourceConfig.Custom.builder()
                        .itemSchema(UpdateTransactionUtils.extractJsonSchema(InputItem.class))
                        .includeSampleSchema(true)
                        .build()))
            .addTestingCriterion(
                EvalCreateParams.TestingCriterion.ofPython(
                    TestingCriterion.Python.builder()
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
    try (var fileStream = UpdateTransactionUtils.loadResourceAsStream(resourcePath)) {
      var field =
          MultipartField.<InputStream>builder().value(fileStream).filename("input.jsonl").build();
      final var file =
          client
              .files()
              .create(
                  FileCreateParams.builder()
                      .purpose(FilePurpose.EVALS)
                      .file(field)
                      .expiresAfter(
                          FileCreateParams.ExpiresAfter.builder()
                              .anchor(JsonValue.from("created_at"))
                              .seconds(FILE_EXPIRE_TIME.getSeconds())
                              .build())
                      .build());
      logger.info("File ID: {}", file.id());
      return file;
    }
  }

  void submitRun(EvalContext context, String evalId, FileObject fileObj, InputConfig inputConfig)
      throws JsonProcessingException {
    final var inputTemplate = utils.createInputTemplate(context, inputConfig.messageCount);
    final var fileNameOnly = Paths.get(inputConfig.filePath).getFileName().toString();
    final var run =
        client
            .evals()
            .runs()
            .create(
                RunCreateParams.builder()
                    .name(
                        String.format(
                            "%s (model: %s, reasoning: %s, file: %s)",
                            context.evalRunName,
                            context.model,
                            context.reasoningEffort,
                            fileNameOnly))
                    .evalId(evalId)
                    .dataSource(
                        CreateEvalResponsesRunDataSource.builder()
                            .model(context.model)
                            .samplingParams(
                                CreateEvalResponsesRunDataSource.SamplingParams.builder()
                                    // SDK doesn't yet support reasoning_effort
                                    .putAdditionalProperty(
                                        "reasoning_effort", JsonValue.from(context.reasoningEffort))
                                    .text(
                                        CreateEvalResponsesRunDataSource.SamplingParams.Text
                                            .builder()
                                            // The API doesn't yet support verbosity.
                                            // See
                                            // https://community.openai.com/t/cannot-set-verbosity-for-gpt-5-evals/1354524?
                                            .format(
                                                UpdateTransactionUtils
                                                    .createTransactionOutputSchema())
                                            .build())
                                    .build())
                            .type(CreateEvalResponsesRunDataSource.Type.RESPONSES)
                            .fileIdSource(fileObj.id())
                            .inputMessages(inputTemplate)
                            .build())
                    .build());
    logger.info("Run ID: {}", run.id());
  }

  @Override
  public void close() throws IOException {
    client.close();
  }
}
