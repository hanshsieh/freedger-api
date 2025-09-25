package org.freedger.services.eval.models;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "MessageItem", description = "A single message turn with input and expected assistant output.")
public class MessageItem {
  public static final String USER_MESSAGE_KEY = "userMessage";
  public static final String INPUT_TRANSACTION_KEY = "inputTransaction";
  public static final String OUTPUT_TRANSACTION_KEY = "outputTransaction";

  @Schema(name = USER_MESSAGE_KEY,
    description = "The user's message for this turn.")
  public String userMessage;

  @Schema(name = INPUT_TRANSACTION_KEY,
    description = "The current transaction draft to be shown in status before this user message.")
  public TransactionDraft inputTransaction;

  @Schema(name = OUTPUT_TRANSACTION_KEY,
    description = "The assistant's transaction draft reply for this turn. Can be null for the last turn.",
    nullable = true)
  public TransactionDraft outputTransaction;
}


