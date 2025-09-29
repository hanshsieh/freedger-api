
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
  affects_balance: bool = journal.get("affectsBalance")

  expected_account_id: str = matcher.get("accountId")
  expected_account_channel_id: str | None = matcher.get("accountChannelId")
  expected_platform_id: str | None = matcher.get("platformId")
  expected_amount: str = matcher.get("amount")
  expected_currency_id: str = matcher.get("currencyId")
  expected_time: str = matcher.get("time")
  expected_affects_balance: bool = matcher.get("affectsBalance")

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
  if affects_balance != expected_affects_balance:
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
