import json
from test_criterion import grade

if __name__ == "__main__":
  try:
    output_text_line = input("Enter output_text (one-line JSON): ").strip()
    item_line = input("Enter item (one-line JSON): ").strip()
    sample = {"output_text": output_text_line}
    item = json.loads(item_line)
    print(grade(sample, item))
  except Exception as e:
    print(f"Error: {e}")