package org.freedger.tools.eval;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.help.HelpFormatter;
import org.freedger.tools.eval.models.EvalContext;
import org.freedger.tools.eval.models.InputItem;
import org.freedger.tools.eval.models.TransactionDraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.Reasoning;
import com.openai.models.ReasoningEffort;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.ResponseTextConfig;

public class UpdateTransactionLatency implements Closeable {
  private static final String ARG_INPUT = "input";
  private static final String ARG_INPUT_MSG_COUNT = "input-msg-count";
  private static final String ARG_COUNT = "count";
  private static final String ARG_HELP_SHORT = "h";
  private static final String ARG_HELP_LONG = "help";
  private static final String ARG_VERBOSE_SHORT = "v";
  private static final String ARG_VERBOSE_LONG = "verbose";
  private final Pattern templateRegex = Pattern.compile("\\{\\{([a-zA-Z0-9_.]+)\\}\\}");
  private static final Logger logger = LoggerFactory.getLogger(UpdateTransactionLatency.class);
  
  private final UpdateTransactionUtils utils = new UpdateTransactionUtils();
  private final ObjectMapper objectMapper = new ObjectMapper();
  private OpenAIClient client = OpenAIOkHttpClient.fromEnv();
  private String inputPath;
  private int inputMsgCount;
  private int testCount;
  private boolean verbose = false;
  private int exitCode = 0;

  public static void main(String[] args) {
    try (var latency = new UpdateTransactionLatency()) {
      latency.run(args);
      System.exit(latency.exitCode);
    } catch (Exception e) {
      logger.error("Error running latency test", e);
      System.exit(1);
    }
  }

  public void run(String[] args) throws Exception {
    if (!parseCommandLine(args)) {
      return;
    }

    // Load context and input data
    final var context = utils.loadContext();
    final var inputItems = loadInputItems(inputPath);
    if (inputItems.isEmpty()) {
      System.err.println("Error: No input items found in " + inputPath);
      exitCode = 1;
      return;
    }

    logger.info("Starting latency test with {} requests using {} input items", testCount, inputItems.size());

    // Run latency tests
    final var latencies = runLatencyTests(context, inputItems, testCount);
    
    // Calculate and print statistics
    printStatistics(latencies);
  }

  private boolean parseCommandLine(String[] args) throws Exception {
    final var options = createCommandLineOptions();
    final var parser = new DefaultParser();
    final var cmd = parser.parse(options, args);

    exitCode = 0;
    if (cmd.hasOption(ARG_HELP_SHORT)) {
      printHelp(options);
      return false;
    }

    inputPath = cmd.getOptionValue(ARG_INPUT);
    final var countStr = cmd.getOptionValue(ARG_COUNT);
    final var inputMsgCountStr = cmd.getOptionValue(ARG_INPUT_MSG_COUNT);

    inputMsgCount = Integer.parseInt(inputMsgCountStr);
    if (inputMsgCount <= 0) {
      System.err.printf("Error: --%s must be positive", ARG_INPUT_MSG_COUNT);
      exitCode = 1;
      return false;
    }
    
    testCount = Integer.parseInt(countStr);
    if (testCount <= 0) {
      System.err.printf("Error: --%s must be positive", ARG_COUNT);
      exitCode = 1;
      return false;
    }

    verbose = cmd.hasOption(ARG_VERBOSE_SHORT);
    return true;
  }

  private Options createCommandLineOptions() {
    final var options = new Options();
    
    options.addOption(Option.builder()
      .longOpt(ARG_INPUT)
      .hasArg()
      .desc("Input resource path (e.g., @input_1.jsonl)")
      .required()
      .get());

    options.addOption(Option.builder()
      .longOpt(ARG_INPUT_MSG_COUNT)
      .hasArg()
      .desc("Number of messages in each input item")
      .required()
      .get());
    
    options.addOption(Option.builder()
      .longOpt(ARG_COUNT)
      .hasArg()
      .desc("Number of requests to send")
      .required()
      .get());
    
    options.addOption(Option.builder(ARG_HELP_SHORT)
      .longOpt(ARG_HELP_LONG)
      .desc("Print this help message")
      .get());
    
    options.addOption(Option.builder(ARG_VERBOSE_SHORT)
      .longOpt(ARG_VERBOSE_LONG)
      .desc("Print verbose output")
      .get());
    
    return options;
  }

  private void printHelp(Options options) throws IOException {
    final var formatter = HelpFormatter.builder()
      .get();
    formatter.printHelp(getClass().getSimpleName(), 
      "Evaluate the latency of the update transaction OpenAI API calls",
      options.getOptions(), 
      "", 
      true);
  }

  private List<String> loadInputItems(String inputPath) throws IOException {
    final var resourcePath = inputPath.startsWith("@") ? inputPath.substring(1) : inputPath;
    final var content = UpdateTransactionUtils.loadResourceAsString(resourcePath);
    
    return Arrays.stream(content.split("\n"))
      .map(String::trim)
      .filter(line -> !line.isEmpty())
      .collect(Collectors.toList());
  }

  private List<Duration> runLatencyTests(EvalContext context, List<String> inputItems, int count) 
    throws IOException {
    final var latencies = new ArrayList<Duration>();
    
    for (int i = 0; i < count; i++) {
      final var inputItem = inputItems.get(i % inputItems.size());
      final var latency = measureApiLatency(context, inputItem);
      latencies.add(latency);
      
      logger.info("Request {}/{} completed in {:.3f}s",
        i + 1, count, latency.toMillis() / 1_000.0);
    }
    
    return latencies;
  }

  private Duration measureApiLatency(EvalContext context, String inputItem) throws IOException {
    final List<ResponseInputItem> inputs = new ArrayList<>();

    final var inputTemplate = utils.createInputTemplate(context, inputMsgCount);
    final var innerTemplates = inputTemplate.template();
    Object inputItemJson = Configuration.defaultConfiguration().jsonProvider().parse(inputItem);
    for (var innerTemplate : innerTemplates) {
      if (!innerTemplate.isChatMessage()) {
        continue;
      }
      final var chatMessage = innerTemplate.asChatMessage();
      final var role = chatMessage.role();
      final var contentTemplate = chatMessage.content();
      final var content = templateRegex.matcher(contentTemplate).replaceAll((result) -> {
        final var jsonPath = "$." + result.group(1);
        final var value = JsonPath.read(inputItemJson, jsonPath);
        if (value instanceof String) {
          return (String) value;
        }
        try {
          return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
          throw new RuntimeException("Failed to write value as string: " + value, e);
        }
      });

      inputs.add(ResponseInputItem.ofMessage(
        ResponseInputItem.Message.builder()
          .role(ResponseInputItem.Message.Role.of(role))
          .addInputTextContent(content)
          .build())
      );
    }
    final var params = ResponseCreateParams.builder()
      .model(ChatModel.of(context.model))
      .text(ResponseTextConfig.builder()
        .verbosity(ResponseTextConfig.Verbosity.LOW)
        .build())
      .reasoning(Reasoning.builder()
        .effort(ReasoningEffort.of(context.reasoningEffort))
        .build())
      .input(ResponseCreateParams.Input.ofResponse(inputs))
      .text(TransactionDraft.class)
      .build();

    if (verbose) {
      UpdateTransactionUtils.printRequest(params);
    }
    final var startTime = Instant.now();
    final var response = client.responses().create(params);
    final var endTime = Instant.now();
    final var latency = Duration.between(startTime, endTime);
    if (verbose) {
      UpdateTransactionUtils.printResponse(response);
    }
    return latency;
  }

  private void printStatistics(List<Duration> latencies) {
    if (latencies.isEmpty()) {
      System.out.println("No latency data available");
      return;
    }
    
    // Sort latencies for percentile calculations
    final var sortedLatencies = new ArrayList<>(latencies);
    Collections.sort(sortedLatencies);
    
    final var count = sortedLatencies.size();
    final var removeCount = Math.max((count - 1) / 2, (int) Math.round(count * 0.1)); // Remove top and bottom 10%
    
    // Calculate trimmed mean (remove top and bottom 10%)
    final var trimmedLatencies = sortedLatencies.subList(removeCount, count - removeCount);
    final var averageLatency = trimmedLatencies.stream()
      .mapToDouble((t) -> t.toMillis() / 1_000.0)
      .average()
      .orElse(0.0);
    
    final var minLatency = Collections.min(latencies);
    final var maxLatency = Collections.max(latencies);
    
    System.out.println("=============");
    System.out.println("Latency Statistics");
    System.out.println("=============");
    System.out.printf("Total requests: %d%n", count);
    System.out.printf("Average latency (trimmed 10%%): %.3f seconds%n", averageLatency);
    System.out.printf("Minimum latency: %.3f seconds%n", minLatency);
    System.out.printf("Maximum latency: %.3f seconds%n", maxLatency);
    System.out.println("=============");
  }

  @Override
  public void close() throws IOException {
    client.close();
  }
}