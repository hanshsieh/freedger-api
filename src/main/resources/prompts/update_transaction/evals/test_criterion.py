
import json


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
    type: str = validation.get("type")
    if type == "categories":
      if not validate_categories(output, validation):
        return 0.0
  return 1.0

def validate_categories(output: dict, validation: dict) -> bool:
  categoryIds: list[str] = validation.get("categoryIds")
  categoryIds.sort()
  outputCategoryIds: list[str] = output.get("categoryIds")
  outputCategoryIds.sort()
  return outputCategoryIds == categoryIds
