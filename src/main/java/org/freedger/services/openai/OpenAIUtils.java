package org.freedger.services.openai;

import com.openai.models.Reasoning;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseInputContent;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.models.responses.ResponseTextConfig;
import com.openai.models.responses.Tool;

public class OpenAIUtils {
  public static void printRequest(ResponseCreateParams params) {
    printHeader("Request");
    System.out.println("Model: " + params.model());
    System.out.println("Tool Choice: " + params.toolChoice());
    if (params.text().isPresent()) {
      printTextConfig(params.text().get());
    }
    if (params.reasoning().isPresent()) {
      printReasoningConfig(params.reasoning().get());
    }
    if (params.instructions().isPresent()) {
      System.out.println("Instructions:\n" + params.instructions().get());
    }
    if (params.input().isPresent()) {
      printInput(params.input().get());
    }
    if (params.tools().isPresent()) {
      final var tools = params.tools().get();
      for (var i = 0; i < tools.size(); i++) {
        System.out.printf("Tool %d:\n", i + 1);
        printTool(tools.get(i));
      }
    }
  }

  public static void printResponse(Response response) {
    printHeader("Response");
    System.out.println("ID: " + response.id());
    System.out.println("Model: " + response.model());
    final var output = response.output();
    for (var i = 0; i < output.size(); i++) {
      System.out.printf("Output %d:\n", i + 1);
      printResponseOutputItem(output.get(i));
    }
  }

  private static void printHeader(String title) {
    System.out.println("=============\n" + title + "\n=============");
  }

  private static void printTextConfig(ResponseTextConfig textConfig) {
    System.out.println("Text Config:");
    System.out.println("  Verbosity: " + textConfig.verbosity());
  }

  private static void printReasoningConfig(Reasoning reasoning) {
    System.out.println("Reasoning Config:");
    System.out.println("  Effort: " + reasoning.effort());
  }

  private static void printInput(ResponseCreateParams.Input input) {
    final var textOpt = input.text();
    if (textOpt.isPresent()) {
      System.out.println("Text input: " + textOpt.get());
    }
    final var responseOpt = input.response();
    if (responseOpt.isPresent()) {
      final var response = responseOpt.get();
      for (var i = 0; i < response.size(); i++) {
        System.out.printf("Response input item %d:\n", i + 1);
        printResponseInputItem(response.get(i));
      }
    }
  }

  private static void printResponseInputItem(ResponseInputItem item) {
    final var messageOpt = item.message();
    if (messageOpt.isPresent()) {
      final var message = messageOpt.get();
      System.out.println("  Type: Message");
      System.out.println("  Role: " + message.role());
      final var contents = message.content();
      for (var i = 0; i < contents.size(); i++) {
        System.out.printf("  Content %d:\n", i + 1);
        printResponseInputContent(contents.get(i));
      }
    }
    final var functionCallOutputOpt = item.functionCallOutput();
    if (functionCallOutputOpt.isPresent()) {
      final var functionCallOutput = functionCallOutputOpt.get();
      System.out.println("  Type: Function Call Output");
      System.out.println("  Call ID: " + functionCallOutput.callId());
      System.out.println("  Output: " + functionCallOutput.output());
    }
  }

  private static void printResponseInputContent(ResponseInputContent content) {
    final var inputTextOpt = content.inputText();
    if (inputTextOpt.isPresent()) {
      final var inputText = inputTextOpt.get();
      System.out.println("    Input Text: " + inputText.text());
    }
  }

  private static void printTool(Tool tool) {
    final var functionOpt = tool.function();
    if (functionOpt.isPresent()) {
      final var function = functionOpt.get();
      System.out.println("  Type: Function");
      System.out.println("  Function: " + function.toString());
    }
  }

  private static void printResponseOutputItem(ResponseOutputItem item) {
    final var messageOpt = item.message();
    if (messageOpt.isPresent()) {
      final var message = messageOpt.get();
      System.out.println("  Type: Message");
      System.out.println("  Role: " + message._role());
      final var contents = message.content();
      for (var i = 0; i < contents.size(); i++) {
        System.out.printf("  Content %d:\n", i + 1);
        printResponseOutputContent(contents.get(i));
      }
    }
    final var functionCallOpt = item.functionCall();
    if (functionCallOpt.isPresent()) {
      final var functionCall = functionCallOpt.get();
      System.out.println("  Type: Function Call");
      System.out.println("  Name: " + functionCall.name());
      System.out.println("  Call ID: " + functionCall.callId());
      System.out.println("  Arguments: " + functionCall.arguments());
    }
    final var reasoningOpt = item.reasoning();
    if (reasoningOpt.isPresent()) {
      final var reasoning = reasoningOpt.get();
      System.out.println("  Type: Reasoning");
      System.out.println("  Summaries:");
      for (var i = 0; i < reasoning.summary().size(); i++) {
        System.out.printf("  Summary %d: %s\n", i + 1, reasoning.summary().get(i).text());
      }
    }
  }

  private static void printResponseOutputContent(ResponseOutputMessage.Content content) {
    final var outputTextOpt = content.outputText();
    if (outputTextOpt.isPresent()) {
      final var outputText = outputTextOpt.get();
      System.out.println("    Output Text: " + outputText.text());
    }
  }
}
