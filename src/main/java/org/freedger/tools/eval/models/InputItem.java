package org.freedger.tools.eval.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "InputItem", description = "The input item for the eval.")
public class InputItem {
  public static final String MESSAGES_KEY = "messages";
  public static final String VALIDATIONS_KEY = "validations";

  @Schema(name = MESSAGES_KEY, description = "The conversation messages.")
  @ArraySchema(schema = @Schema(implementation = MessageItem.class))
  @JsonProperty(MESSAGES_KEY)
  public List<MessageItem> messages;

  @Schema(name = VALIDATIONS_KEY, description = "The validation rules.")
  @ArraySchema(
      schema =
          @Schema(
              anyOf = {TypeRule.class, JournalsRule.class, CategoriesRule.class, TagsRule.class}))
  @JsonProperty(VALIDATIONS_KEY)
  public List<Object> validations;
}
