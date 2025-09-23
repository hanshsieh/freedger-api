package org.freedger.services.eval.models;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "InputItem", description = "The input item for the eval.")
public class InputItem {
  public static final String USER_MESSAGE_KEY = "user_message";
  public static final String GROUND_TRUTH_KEY = "ground_truth";

  @Schema(name = USER_MESSAGE_KEY, description = "The user message.")
  public String userMessage;

  @Schema(name = GROUND_TRUTH_KEY, description = "The ground truth.")
  public String groundTruth;

}
