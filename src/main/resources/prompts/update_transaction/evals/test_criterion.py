
def grade(sample: dict, item: dict) -> float:
  """
  Test criterion for the update transaction eval.

  Args:
    sample: The sample data. It contains the model response and a "output_text" key for the text output.
    item: The item data. It's the JSON object under the "item" key of the input JSONL file.

  Returns:
    The grade between 0.0 and 1.0.
  """
  output = sample.get("output_text")
  ground_truth = item.get("ground_truth")
  return 1.0 if output == ground_truth else 0.0
