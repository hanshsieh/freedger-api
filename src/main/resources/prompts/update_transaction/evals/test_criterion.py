
import json
import re


def grade(sample: dict, item: dict) -> float:
  """
  Test criterion for the update transaction eval.

  Args:
    sample: The sample data. It contains the model response and a "output_text" key for the text output.
    item: The item data. It's the JSON object under the "item" key of the input JSONL file.

  Returns:
    The grade between 0.0 and 1.0.
  """
  output: dict = json.loads(sample.get("output_text"))
  validations: list[dict] = item.get("validations")
  for validation in validations:
    validation_type: str = validation.get("type")
    match validation_type:
      case "categories":
        if not validate_categories(output, validation):
          return 0.0
      case "tags":
        if not validate_tags(output, validation):
          return 0.0
  return 1.0

def validate_categories(output: dict, validation: dict) -> bool:
  category_ids: list[str] = validation.get("categoryIds")
  category_ids.sort()
  output_category_ids: list[str] = output.get("categoryIds")
  output_category_ids.sort()
  return output_category_ids == category_ids

def validate_tags(output: dict, validation: dict) -> bool:
  tags_regex: str | None = validation.get("tagsRegex")
  min_tags: int | None = validation.get("minTags")
  tags: list[str] = output.get("tags")
  if type(tags_regex) == str:
    if not all(re.match(tags_regex, tag) for tag in tags):
      return False
  if type(min_tags) == int:
    if len(tags) < min_tags:
      return False
  return True
