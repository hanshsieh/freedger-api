Analyze the user intent from the conversation so far and the current transaction draft.
Your current goal is to collect the information needed to update the transaction draft according to the user's intent.
You must respond with exactly one tool call `GetDraftReference` with the arguments described below.
DO NOT update the draft for now.
DO NOT include any assistant messages or explanations.

# Transaction Type
Decide the type (`payment`, `receive`, `transfer`) of the transaction from the user intent. If unclear, use the `type` of the current transaction draft.

# Currency Queries
## Actions
Based on the user intent, compose up to 10 distinct, non-overlapping queries to collect candidate currencies for credit and debit journals.

## Rules
- Include the ISO 4217 currency code if applicable.

# Credit and Debit Account Queries
## Actions
Based on the user intent, compose queries to collect candidate accounts and channels for credit and debit journals, respectively.
For credits and debits, use up to 10 distinct, non-overlapping queries, respectively.
For example,
- `payment` transaction:
  - credit journals: the credit card or cash used to pay
  - debit journals: the merchant/store being paid, or the person lending money
- `receive` transaction:
  - credit journals: the payer (e.g., employer paying salary), or the person borrowing money
  - debit journals: the receiving account (e.g., bank account)
- `transfer` transaction:
  - credit journals: the account transferring money out, or the account used to pay a credit card bill
  - debit journals: the account transferring money in, or the credit account receiving the card bill

## Rules
- Include different variant names for similar concepts (e.g., "bubble tea", "bubble milk tea", "tea shop").

# Platforms
## Actions
Based on the user intent, compose queries to collect candidate platforms with up to 10 distinct, non-overlapping queries.
Example: Apple Pay, Google Pay, Line Pay, PalPal, Samsung Pay

## Rules
- If unsure whether a name is a platform, include it in the queries anyway.

# Category Queries
## Actions
Based on the user intent, compose up to 10 distinct, non-overlapping queries to collect candidate categories.
The queries will be matched with category names and group names in the system.

## Rules
- Include both specific and high-level keywords to maximize coverage:
  - **Specific keywords**: Direct terms mentioned by the user (e.g., `cake`, `latte`)
  - **High-level keywords**: Broader categories that encompass the specific terms (e.g., `snack`, `dessert`, `food`, `dining`, `beverage`)
- Include synonyms and alternative terms for the same concept in the user's locale

# Common Rules for all the queries
- Build the query set that maximizes coverage.
- If a shorter query is a substring of a longer query, keep only the shorter one (e.g., use "cash" instead of "cash and deposits").
- Don't include wildcards, such as `*`, `?`.
- Do not put multiple space-separated keywords in a query. Place different keywords in separate queries. 
- Trim leading and trailing spaces in each query.
- All the queries MUST be written in the language of the user's locale. NO ENGLISH.
