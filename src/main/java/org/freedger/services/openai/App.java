package org.freedger.services.openai;

import java.util.ArrayList;
import java.util.List;

import org.freedger.services.eval.models.EvalContext;
import org.freedger.services.openai.models.UpdateTransactionDraft;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.Reasoning;
import com.openai.models.ReasoningEffort;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.ResponseTextConfig;

public class App {
  private final List<ResponseInputItem> inputs = new ArrayList<>();
  private ResponseCreateParams.Builder paramsBuilder;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private OpenAIClient client;
  public static void main( String[] args ) throws Exception {
    new App().run();
  }

  public void run() throws Exception {
    client = OpenAIOkHttpClient.fromEnv();
    try {
      paramsBuilder = createParamsBuilder();
      inputs.clear();
      updateTransactionDraft();
      updateTransactionDraft();
    } finally {
      client.close();
    }
  }
  
  private static String loadResourceAsString(String resourcePath) {
    try (var inputStream = App.class.getClassLoader().getResourceAsStream(resourcePath)) {
      if (inputStream == null) {
        throw new IllegalStateException("Resource not found: " + resourcePath);
      }
      return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new RuntimeException("Failed to read resource: " + resourcePath, e);
    }
  }

  private void updateTransactionDraft() throws IOException {    
    addInitialInputs();
    addUserInput("prompts/update_transaction/evals/user4.md");
    final var params = paramsBuilder
      .input(ResponseCreateParams.Input.ofResponse(inputs))
      .text(UpdateTransactionDraft.class)
      .build();
    OpenAIUtils.printRequest(params);

    final var startTime = System.currentTimeMillis();
    final var response = client.responses().create(params);
    final var endTime = System.currentTimeMillis();

    System.out.println("API Latency: " + (endTime - startTime) / 1000.0 + " s");
    OpenAIUtils.printResponse(response);
    inputs.clear();
    paramsBuilder = createParamsBuilder()
      .previousResponseId(response.id());
  }

  private void addInitialInputs() throws IOException {
    inputs.add(ResponseInputItem.ofMessage(
      ResponseInputItem.Message.builder()
        .role(ResponseInputItem.Message.Role.DEVELOPER)
        .addInputTextContent(loadResourceAsString("prompts/update_transaction/inputs/intro.md"))
        .build()));
    var contextStr = loadResourceAsString("prompts/update_transaction/inputs/context.md");
    final var context = objectMapper.readValue(
      loadResourceAsString("prompts/update_transaction/evals/context.json"), EvalContext.class);
    contextStr = contextStr.replace("{{currentTime}}", context.currentTime);
    contextStr = contextStr.replace("{{timeZone}}", context.timeZone);
    contextStr = contextStr.replace("{{locale}}", context.locale);
    contextStr = contextStr.replace("{{defaultCurrencyId}}", context.defaultCurrencyId);
    contextStr = contextStr.replace("{{currencies}}", objectMapper.writeValueAsString(context.currencies));
    contextStr = contextStr.replace("{{accounts}}", objectMapper.writeValueAsString(context.accounts));
    contextStr = contextStr.replace("{{categories}}", objectMapper.writeValueAsString(context.categories));
    contextStr = contextStr.replace("{{platforms}}", objectMapper.writeValueAsString(context.platforms));
    contextStr = contextStr.replace("{{tags}}", objectMapper.writeValueAsString(context.tags));
    inputs.add(ResponseInputItem.ofMessage(
      ResponseInputItem.Message.builder()
        .role(ResponseInputItem.Message.Role.DEVELOPER)
        .addInputTextContent(contextStr)
        .build()));

    var currentDraftStr = loadResourceAsString("prompts/update_transaction/inputs/status.md");
    currentDraftStr = currentDraftStr.replace("{{transaction}}", objectMapper.writeValueAsString(context.transaction));
    inputs.add(ResponseInputItem.ofMessage(
      ResponseInputItem.Message.builder()
        .role(ResponseInputItem.Message.Role.DEVELOPER)
        .addInputTextContent(currentDraftStr)
        .build()));
  }

  private void addUserInput(String filePath) {
    final var content = loadResourceAsString(filePath);
    inputs.add(ResponseInputItem.ofMessage(
        ResponseInputItem.Message.builder()
          .role(ResponseInputItem.Message.Role.USER)
          .addInputTextContent(content)
          .build()));
  }

  private ResponseCreateParams.Builder createParamsBuilder() {
    return ResponseCreateParams.builder()
      .model(ChatModel.GPT_5_MINI_2025_08_07)
      .text(ResponseTextConfig.builder()
        .verbosity(ResponseTextConfig.Verbosity.MEDIUM)
        .build())
      .reasoning(Reasoning.builder()
        .effort(ReasoningEffort.MINIMAL)
        .build());
  }
}
