
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
      case "type":
        if not validate_type(output, validation):
          return 0.0
      case "journals":
        if not validate_journals(output, validation):
          return 0.0
      case "categories":
        if not validate_categories(output, validation):
          return 0.0
      case "tags":
        if not validate_tags(output, validation):
          return 0.0
      case _:
        raise ValueError(f"Unknown validation type: {validation_type}")
  return 1.0

def validate_type(output: dict, validation: dict) -> bool:
  type: str = validation.get("transactionType")
  return output.get("type") == type

def validate_journals(output: dict, validation: dict) -> bool:
  journal_type: str = validation.get("journalType")
  journals: list[dict] = output.get("credits") if journal_type == "credit" else output.get("debits")
  matchers: list[dict] = validation.get("journals")
  remain_matchers: list[dict] = matchers.copy()
  for journal in journals:
    matched = False
    for matcher in remain_matchers:
      if match_journal(journal, matcher):
        matched = True
        remain_matchers.remove(matcher)
        break
    if not matched:
      return False
  if len(remain_matchers) > 0:
    return False
  return True

def match_journal(journal: dict, matcher: dict) -> bool:
  account_id: str = journal.get("accountId")
  account_channel_id: str | None = journal.get("accountChannelId")
  platform_id: str | None = journal.get("platformId")
  amount: str = journal.get("amount")
  currency_id: str = journal.get("currencyId")
  time: str = journal.get("time")
  in_balance: bool = journal.get("inBalance")

  expected_account_id: str = matcher.get("accountId")
  expected_account_channel_id: str | None = matcher.get("accountChannelId")
  expected_platform_id: str | None = matcher.get("platformId")
  expected_amount: str = matcher.get("amount")
  expected_currency_id: str = matcher.get("currencyId")
  expected_time: str = matcher.get("time")
  expected_in_balance: bool = matcher.get("inBalance")

  if account_id != expected_account_id:
    return False
  if account_channel_id != expected_account_channel_id:
    return False
  if platform_id != expected_platform_id:
    return False
  if float(amount) != float(expected_amount):
    return False
  if currency_id != expected_currency_id:
    return False
  if time != expected_time:
    return False
  if in_balance != expected_in_balance:
    return False
  return True

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

if __name__ == "__main__":
  output = {
    "categoryIds": [
      "01986eb9d22e77299b0b67460bc10d13"
    ],
    "credits": [
      {
        "accountChannelId": "01986eb9d6c2784aa3a2f9ad2b46100f",
        "accountId": "01986eb9d6c2784aa3a2f9ad2b46100d",
        "amount": "15000",
        "currencyId": "67fb6c93007e33a200b9963d@g",
        "inBalance": True,
        "platformId": None,
        "time": "2025-01-01T09:00:00+08:00"
      }
    ],
    "debits": [
      {
        "accountChannelId": None,
        "accountId": "01986eb9d6c2784aa3a2f9ad2b461015",
        "amount": "15000",
        "currencyId": "67fb6c93007e33a200b9963d@g",
        "inBalance": False,
        "platformId": None,
        "time": "2025-01-01T09:00:00+08:00"
      }
    ],
    "note": "預訂年底東京行程（雄獅旅行社），刷 PI 錢包付款",
    "tags": [
      "東京",
      "旅遊"
    ],
    "type": "payment"
  }
  sample = {
    "output_text": json.dumps(output)
  }
  item = {
      "userMessage": "我在雄獅旅行社預訂了年底要去日本東京的行程，總共花了1萬五千元，刷PI錢包付款",
      "validations": [
        {
          "type": "type",
          "transactionType": "payment"
        },
        {
          "type": "journals",
          "journalType": "credit",
          "journals": [
            {
              "accountId": "01986eb9d6c2784aa3a2f9ad2b46100d",
              "accountChannelId": "01986eb9d6c2784aa3a2f9ad2b46100f",
              "platformId": None,
              "amount": "15000",
              "currencyId": "67fb6c93007e33a200b9963d@g",
              "inBalance": True,
              "time": "2025-01-01T09:00:00+08:00"
            }
          ]
        },
        {
          "type": "journals",
          "journalType": "debit",
          "journals": [
            {
              "accountId": "01986eb9d6c2784aa3a2f9ad2b461015",
              "accountChannelId": None,
              "platformId": None,
              "amount": "15000",
              "currencyId": "67fb6c93007e33a200b9963d@g",
              "inBalance": False,
              "time": "2025-01-01T09:00:00+08:00"
            }
          ]
        },
        {
          "type": "categories",
          "categoryIds": [
            "01986eb9d22e77299b0b67460bc10d13"
          ]
        },
        {
          "type": "tags",
          "tagsRegex": "(日本)?東京|(東京)?旅(遊|行)",
          "minTags": 1
        }
      ]
    }
  print(grade(sample, item))