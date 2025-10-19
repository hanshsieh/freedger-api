package org.freedger.tools.eval.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "MessageItem",
    description = "A single message turn with input and expected assistant output.")
public class MessageItem {
  public static final String USER_MESSAGE_KEY = "userMessage";
  public static final String INPUT_TRANSACTION_KEY = "inputTransaction";
  public static final String OUTPUT_TRANSACTION_KEY = "outputTransaction";

  @Schema(name = USER_MESSAGE_KEY, description = "The user's message for this turn.")
  @JsonProperty(USER_MESSAGE_KEY)
  public String userMessage;

  @Schema(
      name = INPUT_TRANSACTION_KEY,
      description = "The current transaction draft to be shown in status before this user message.")
  @JsonProperty(INPUT_TRANSACTION_KEY)
  public TransactionDraft inputTransaction;

  @Schema(
      name = OUTPUT_TRANSACTION_KEY,
      description =
          "The assistant's transaction draft reply for this turn. Can be null for the last turn.",
      nullable = true)
  @JsonProperty(OUTPUT_TRANSACTION_KEY)
  public TransactionDraft outputTransaction;
}
