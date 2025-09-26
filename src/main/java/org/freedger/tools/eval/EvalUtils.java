package org.freedger.tools.eval;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.github.victools.jsonschema.generator.Option;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import com.github.victools.jsonschema.module.swagger2.Swagger2Module;
import com.openai.core.JsonField;
import com.openai.core.JsonValue;
import com.openai.models.Reasoning;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseInputContent;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.models.responses.ResponseTextConfig;
import com.openai.models.responses.StructuredResponse;
import com.openai.models.responses.StructuredResponseCreateParams;
import com.openai.models.responses.Tool;

public class EvalUtils {

  /**
   * Extract the JSON schema from a Java class.
   * It's useful for the OpenAI SDK classes that not yet support defining the JSON schema with classes.
   * 
   * Reference: https://github.com/openai/openai-java/blob/a0a9a1ebff9e442003786caa5b6be45fe34fed9f/openai-java-core/src/main/kotlin/com/openai/core/StructuredOutputs.kt#L199C1-L223C2
   * 
   * @param clazz The Java class to extract the JSON schema from.
   * @return The JSON schema.
   */
  @SuppressWarnings("unchecked")
  public static <T> JsonField<T> extractJsonSchema(Class<?> clazz) {
    final var configBuilder = new SchemaGeneratorConfigBuilder(
            SchemaVersion.DRAFT_2020_12,
            OptionPreset.PLAIN_JSON
        )
        // Add `"additionalProperties" : false` to all object schemas (see OpenAI).
        .with(Option.FORBIDDEN_ADDITIONAL_PROPERTIES_BY_DEFAULT)
        // Use `JacksonModule` to support the use of Jackson annotations to set property and
        // class names and descriptions and to mark fields with `@JsonIgnore`.
        .with(new JacksonModule())
        // Use `Swagger2Module` to support OpenAPI Swagger 2 `@Schema` annotations to set
        // property constraints (e.g., a `"pattern"` constraint for a string property).
        .with(new Swagger2Module());

    configBuilder
      .forFields()
      // For OpenAI schemas, _all_ properties _must_ be required. Override the interpretation of
      // the Jackson `required` parameter to the `@JsonProperty` annotation: it will always be
      // assumed to be `true`, even if explicitly `false` and even if there is no `@JsonProperty`
      // annotation present.
      .withRequiredCheck((fieldScope) -> true);

    final var shema = new SchemaGenerator(configBuilder.build()).generateSchema(clazz);
    return JsonValue.fromJsonNode(shema);
  }

  public static String loadResourceAsString(String resourcePath) {
    try (var inputStream = loadResourceAsStream(resourcePath)) {
      return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new RuntimeException("Failed to read resource: " + resourcePath, e);
    }
  }

  public static InputStream loadResourceAsStream(String resourcePath) {
    final var inputStream = EvalUtils.class.getClassLoader().getResourceAsStream(resourcePath);
    if (inputStream == null) {
      throw new IllegalStateException("Resource not found: " + resourcePath);
    }
    return inputStream;
  }

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

  public static <T> void printRequest(StructuredResponseCreateParams<T> params) {
    final var rawParams = params.rawParams();
    printRequest(rawParams);
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

  public static <T> void printResponse(StructuredResponse<T> response) {
    final var rawResponse = response.rawResponse();
    printResponse(rawResponse);
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
