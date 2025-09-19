package org.freedger.services.openai;

import java.util.ArrayList;
import java.util.List;

import org.freedger.services.openai.tools.GetDraftReference;
import org.freedger.services.openai.tools.UpdateTransactionDraft;
import org.freedger.services.openai.models.DraftState;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.Reasoning;
import com.openai.models.ReasoningEffort;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseFunctionToolCall;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.ResponseTextConfig;
import com.openai.models.responses.ToolChoiceOptions;

public class App {
  private final StorageService storageService;
  private final List<ResponseInputItem> inputs = new ArrayList<>();
  private ResponseCreateParams.Builder paramsBuilder;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private OpenAIClient client;
  public static void main( String[] args ) throws Exception {
    new App().run();
  }

  public App() throws Exception {
    storageService = StorageService.createFromFile("prompts/2_rounds_rag/storage.json");
  }

  public void run() throws Exception {
    client = OpenAIOkHttpClient.fromEnv();
    try {
      paramsBuilder = createParamsBuilder();
      inputs.clear();
      collectDraftReference();
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

  private void collectDraftReference() throws IOException {    
    addInitialInputs();
    addUserInput("prompts/2_rounds_rag/request1/message3_1.md");
    setInstructions("prompts/2_rounds_rag/request1/instructions.md");
    ResponseCreateParams params = paramsBuilder
      .input(ResponseCreateParams.Input.ofResponse(inputs))
      .addTool(GetDraftReference.class)
      .toolChoice(ResponseCreateParams.ToolChoice.ofOptions(ToolChoiceOptions.REQUIRED))
      .build();
    OpenAIUtils.printRequest(params);
    Response response = client.responses().create(params);
    OpenAIUtils.printResponse(response);
    inputs.clear();
    paramsBuilder = createParamsBuilder()
      .previousResponseId(response.id());
    for (var output : response.output()) {
      if (output.isFunctionCall()) {
        final var functionCall = output.asFunctionCall();
        handlFunctionCall(functionCall);
      }
    }
  }

  private void updateTransactionDraft() throws IOException {
    setInstructions("prompts/2_rounds_rag/request2/instructions.md");
    ResponseCreateParams params = paramsBuilder
      .input(ResponseCreateParams.Input.ofResponse(inputs))
      .addTool(UpdateTransactionDraft.class)
      .toolChoice(ResponseCreateParams.ToolChoice.ofOptions(ToolChoiceOptions.REQUIRED))
      .build();

    OpenAIUtils.printRequest(params);

    Response response = client.responses().create(params);
    OpenAIUtils.printResponse(response);
    inputs.clear();
    paramsBuilder = createParamsBuilder()
      .previousResponseId(response.id());
    for (var output : response.output()) {
      if (output.isFunctionCall()) {
        final var functionCall = output.asFunctionCall();
        handlFunctionCall(functionCall);
      }
    }
  }

  private void addInitialInputs() throws IOException {
    inputs.add(ResponseInputItem.ofMessage(
      ResponseInputItem.Message.builder()
        .role(ResponseInputItem.Message.Role.DEVELOPER)
        .addInputTextContent(loadResourceAsString("prompts/2_rounds_rag/request1/message1.md"))
        .build()));
    var contextStr = loadResourceAsString("prompts/2_rounds_rag/request1/message2.md");
    final var draftState = objectMapper.readValue(
      loadResourceAsString("prompts/2_rounds_rag/request1/draft_state.json"), DraftState.class);
    contextStr = contextStr.replace("{{transaction}}", objectMapper.writeValueAsString(draftState.transaction));
    contextStr = contextStr.replace("{{currencies}}", objectMapper.writeValueAsString(draftState.currencies));
    contextStr = contextStr.replace("{{accounts}}", objectMapper.writeValueAsString(draftState.accounts));
    contextStr = contextStr.replace("{{categories}}", objectMapper.writeValueAsString(draftState.categories));
    contextStr = contextStr.replace("{{platforms}}", objectMapper.writeValueAsString(draftState.platforms));
    inputs.add(ResponseInputItem.ofMessage(
      ResponseInputItem.Message.builder()
        .role(ResponseInputItem.Message.Role.DEVELOPER)
        .addInputTextContent(contextStr)
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

  private Object callFunction(ResponseFunctionToolCall functionCall) {
    switch (functionCall.name()) {
      case "GetDraftReference":
        return functionCall.arguments(GetDraftReference.class).execute(storageService);
      case "UpdateTransactionDraft":
        return functionCall.arguments(UpdateTransactionDraft.class).execute();
      default:
        throw new IllegalArgumentException("Unknown function: " + functionCall.name());
    }
  }

  private void handlFunctionCall(ResponseFunctionToolCall functionCall) throws IOException {
    final var result = callFunction(functionCall);
    final var jsonResult = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
    inputs.add(ResponseInputItem.ofFunctionCallOutput(
      ResponseInputItem.FunctionCallOutput.builder()
        .callId(functionCall.callId())
        .output(jsonResult)
        .build()));
  }

  private ResponseCreateParams.Builder createParamsBuilder() {
    return ResponseCreateParams.builder()
      .model(ChatModel.GPT_5_NANO_2025_08_07)
      .text(ResponseTextConfig.builder()
        .verbosity(ResponseTextConfig.Verbosity.MEDIUM)
        .build())
      .reasoning(Reasoning.builder()
        .effort(ReasoningEffort.MINIMAL)
        .build());
  }

  private void setInstructions(String filePath) {
    final var content = loadResourceAsString(filePath);
    paramsBuilder.instructions(content);
  }
}
