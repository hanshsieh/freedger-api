package org.freedger.services.eval;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.freedger.services.openai.models.EvalContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.JsonValue;
import com.openai.models.evals.EvalCreateParams;
import com.openai.models.evals.EvalCreateParams.DataSourceConfig;
import com.openai.models.evals.EvalCreateParams.DataSourceConfig.Custom.ItemSchema;
import com.openai.models.graders.gradermodels.StringCheckGrader;

public class UpdateTransactionEval implements Closeable {
  private static final Logger logger = LoggerFactory.getLogger(UpdateTransactionEval.class);
  private static final String CONTEXT_FILE = "prompts/update_transaction/evals/context.json";
  private static final String DATA_KEY_USER_MESSAGE = "userMessage";
  private static final String DATA_KEY_GROUND_TRUTH = "groundTruth";
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
    logger.info("Eval ID: {}", evalId);
  }

  String createEval() {
    final var params = EvalCreateParams.builder()
    .name("Update Transaction Draft")
    .dataSourceConfig(DataSourceConfig.ofCustom(DataSourceConfig.Custom.builder()
      .itemSchema(ItemSchema.builder()
        .putAdditionalProperty("type", JsonValue.from("object"))
        .putAdditionalProperty("properties", JsonValue.from(Map.of(
          DATA_KEY_USER_MESSAGE, JsonValue.from(Map.of("type", "string")),
          DATA_KEY_GROUND_TRUTH, JsonValue.from(Map.of("type", "string"))
        )))
        .putAdditionalProperty("required", JsonValue.from(List.of(DATA_KEY_USER_MESSAGE, DATA_KEY_GROUND_TRUTH)))
        .putAdditionalProperty("additionalProperties", JsonValue.from(false))
        .build())
      .includeSampleSchema(true)
      .build()))
    .addTestingCriterion(EvalCreateParams.TestingCriterion.ofStringCheck(StringCheckGrader.builder()
      .name("Match output with ground truth")
      .input("{{sample.output_text}}")
      .operation(StringCheckGrader.Operation.EQ)
      .reference("{{item.%s}}".formatted(DATA_KEY_GROUND_TRUTH))
      .build()))
    .build();
    final var eval = client.evals().create(params);
    return eval.id();
  }

  @Override
  public void close() throws IOException {
    client.close();
  }
}
