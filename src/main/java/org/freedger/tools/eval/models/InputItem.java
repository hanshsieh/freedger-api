package org.freedger.tools.eval.models;

import java.util.List;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "InputItem", description = "The input item for the eval.")
public class InputItem {
  public static final String MESSAGES_KEY = "messages";

  @Schema(name = MESSAGES_KEY, description = "The conversation messages.")
  @ArraySchema(schema = @Schema(implementation = MessageItem.class))
  public List<MessageItem> messages;

  @Schema(name = "validations", description = "The validation rules.")
  @ArraySchema(schema = @Schema(anyOf = {TypeRule.class, JournalsRule.class, CategoriesRule.class, TagsRule.class}))
  public List<Object> validations;
}
