package org.freedger.tools.eval.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

public class TagsRule {
  @Schema(
      name = "type",
      description = "Type of the validation rule.",
      allowableValues = {"tags"})
  @JsonProperty("type")
  public String type;

  @Schema(
      name = "tagsRegex",
      description = "All the tags must match the regex.",
      types = {"string", "null"})
  @JsonProperty("tagsRegex")
  public String tagsRegex;

  @Schema(
      name = "minTags",
      description = "The minimum number of tags.",
      types = {"integer", "null"})
  @JsonProperty("minTags")
  public Integer minTags;
}
