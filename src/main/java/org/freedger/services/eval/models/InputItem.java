package org.freedger.services.eval.models;

import java.util.List;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "InputItem", description = "The input item for the eval.")
public class InputItem {
  public static final String USER_MESSAGE_KEY = "user_message";

  @Schema(name = USER_MESSAGE_KEY, description = "The user message.")
  public String userMessage;

  @Schema(name = "validations", description = "The validation rules.")
  @ArraySchema(schema = @Schema(anyOf = {CategoriesRule.class, TagsRule.class}))
  public List<Object> validations;
}
