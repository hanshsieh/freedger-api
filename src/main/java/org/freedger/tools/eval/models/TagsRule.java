package org.freedger.tools.eval.models;

import io.swagger.v3.oas.annotations.media.Schema;

public class TagsRule {
  @Schema(description = "Type of the validation rule.", allowableValues = {"tags"})
  public String type;

  @Schema(name = "tagsRegex", description = "All the tags must match the regex.", types = {"string", "null"})
  public String tagsRegex;

  @Schema(name = "minTags", description = "The minimum number of tags.", types = {"integer", "null"})
  public Integer minTags;
}
