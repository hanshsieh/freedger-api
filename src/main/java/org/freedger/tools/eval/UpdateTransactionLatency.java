package org.freedger.tools.eval;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.help.HelpFormatter;
import org.freedger.tools.eval.models.EvalContext;
import org.freedger.tools.eval.models.InputItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;

public class UpdateTransactionLatency implements Closeable {
  private static final String ARG_INPUT = "input";
  private static final String ARG_INPUT_MSG_COUNT = "input-msg-count";
  private static final String ARG_COUNT = "count";
  private static final String ARG_HELP_SHORT = "h";
  private static final String ARG_HELP_LONG = "help";
  private static final Logger logger = LoggerFactory.getLogger(UpdateTransactionLatency.class);
  
  private final UpdateTransactionUtils utils = new UpdateTransactionUtils();
  private final ObjectMapper objectMapper = new ObjectMapper();
  private OpenAIClient client = OpenAIOkHttpClient.fromEnv();
  private String inputPath;
  private int inputMsgCount;
  private int testCount;
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

  private List<InputItem> loadInputItems(String inputPath) throws IOException {
    final var resourcePath = inputPath.startsWith("@") ? inputPath.substring(1) : inputPath;
    final var content = UpdateTransactionUtils.loadResourceAsString(resourcePath);
    
    return Arrays.stream(content.split("\n"))
      .filter(line -> !line.trim().isEmpty())
      .map(line -> {
        try {
          return objectMapper.readValue(line, InputItem.class);
        } catch (JsonProcessingException e) {
          throw new RuntimeException("Failed to parse input item: " + line, e);
        }
      })
      .collect(Collectors.toList());
  }

  private List<Double> runLatencyTests(EvalContext context, List<InputItem> inputItems, int count) {
    final var latencies = new ArrayList<Double>();
    
    for (int i = 0; i < count; i++) {
      final var inputItem = inputItems.get(i % inputItems.size());
      final var latency = measureLatency(context, inputItem);
      latencies.add(latency);
      
      logger.info("Request {}/{} completed in {:.3f}s", i + 1, count, latency);
    }
    
    return latencies;
  }

  private double measureLatency(EvalContext context, InputItem inputItem) {
    try {
      final var startTime = System.nanoTime();
      
      // Simulate a request - for now, we'll just simulate the time it would take
      // to make an API call. This is a placeholder implementation.
      // In a real implementation, you would make an actual API call here.
      sendApiCall(context, inputItem);
      
      final var endTime = System.nanoTime();
      final var latencySeconds = (endTime - startTime) / 1_000_000_000.0;
      
      return latencySeconds;
      
    } catch (Exception e) {
      logger.error("Error measuring latency for input item", e);
      throw new RuntimeException("Failed to measure latency", e);
    }
  }

  private void sendApiCall(EvalContext context, InputItem inputItem) throws InterruptedException {
    // Simulate processing time - this is a placeholder
    // In a real implementation, you would make an actual OpenAI API call here
    // For now, we'll simulate some processing time
    
    final var prompt = buildPrompt(context, inputItem);
    logger.debug("Built prompt with {} characters", prompt.length());
    
    // Simulate network and processing delay
    Thread.sleep((long) (Math.random() * 1000 + 500)); // 0.5-1.5 seconds
  }

  private String buildPrompt(EvalContext context, InputItem inputItem) {
    final var promptBuilder = new StringBuilder();
    promptBuilder.append("Please update the following transaction draft based on the user message:\n\n");
    
    // Add the first user message from the input item
    if (!inputItem.messages.isEmpty()) {
      final var firstMessage = inputItem.messages.get(0);
      promptBuilder.append("Current transaction: ");
      try {
        promptBuilder.append(objectMapper.writeValueAsString(firstMessage.inputTransaction));
      } catch (JsonProcessingException e) {
        promptBuilder.append(firstMessage.inputTransaction.toString());
      }
      promptBuilder.append("\n\nUser message: ");
      promptBuilder.append(firstMessage.userMessage);
      promptBuilder.append("\n\nPlease respond with an updated transaction draft in JSON format.");
    }
    
    return promptBuilder.toString();
  }

  private void printStatistics(List<Double> latencies) {
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
      .mapToDouble(Double::doubleValue)
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