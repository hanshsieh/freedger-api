package org.freedger.services.openai.tools;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.freedger.services.openai.models.Account;
import org.freedger.services.openai.models.AccountCategory;
import org.freedger.services.openai.models.DraftReference;
import org.freedger.services.openai.models.Platform;
import org.freedger.services.openai.models.TransactionType;
import org.freedger.services.openai.StorageService;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "GetDraftReference", description = "Gets the reference data for updating the transaction draft.")
public class GetDraftReference {
  @Schema(description = "The type of the transaction.", allowableValues = {"payment", "receive", "transfer"})
  public String transactionType;

  @Schema(description = "List of query strings for the currencies. " +
    "Currencies whose name contains any of the query strings (case-insensitive) as a substring will be returned.")
  public List<String> currencyQueries;

  @Schema(description = "List of query strings for the credit accounts. " +
    "Accounts whose name, group name, or channel names contain any of the query strings (case-insensitive) as a substring will be returned.")
  public List<String> creditAccountQueries;

  @Schema(description = "List of query strings for the debit accounts. " +
    "Accounts whose name, group name, or channel names contain any of the query strings (case-insensitive) as a substring will be returned.")
  public List<String> debitAccountQueries;

  @Schema(description = "List of query strings for the categories. " +
    "Categories with matching `transactionType` and name or group name matching any of the query strings (case-insensitive) as a substring will be returned.")
  public List<String> categoryQueries;

  @Schema(description = "List of query strings for the platforms. " +
    "Platforms whose name contains any of the query strings (case-insensitive) as a substring will be returned.")
  public List<String> platformQueries;

  public DraftReference execute(StorageService storageService) {
    final AccountCategory creditCategory, debitCategory;
    final var type = TransactionType.fromString(transactionType);
    switch (type) {
      case PAYMENT:
        creditCategory = AccountCategory.PERSONAL;
        debitCategory = AccountCategory.EXTERNAL;
        break;
      case RECEIVE:
        creditCategory = AccountCategory.EXTERNAL;
        debitCategory = AccountCategory.PERSONAL;
        break;
      case TRANSFER:
        creditCategory = AccountCategory.PERSONAL;
        debitCategory = AccountCategory.PERSONAL;
        break;
      default:
        throw new IllegalArgumentException("Invalid transaction type: " + type);
    }
    final List<Account> creditAccounts = storageService.queryAccounts(creditAccountQueries, creditCategory);
    final List<Account> debitAccounts = storageService.queryAccounts(debitAccountQueries, debitCategory);
    final List<Account> currencyNamedAccounts;
    if (creditCategory == AccountCategory.PERSONAL || debitCategory == AccountCategory.PERSONAL) {
      // It's common to named accounts with currency name, such as USD, Line point.
      currencyNamedAccounts = storageService.queryAccounts(currencyQueries, AccountCategory.PERSONAL);
    } else {
      currencyNamedAccounts = Collections.emptyList();
    }

    final List<Platform> platforms = storageService.queryPlatforms(platformQueries);
    final List<Account> platformAccounts = storageService.getAccountsByIds(platforms.stream()
      .flatMap(platform -> platform.items.stream())
      .map(item -> item.accountId)
      .collect(Collectors.toSet()));
    final var accountIds = new HashSet<String>();
    final var accounts = Stream.concat(
        Stream.concat(creditAccounts.stream(), debitAccounts.stream()),
        Stream.concat(platformAccounts.stream(), currencyNamedAccounts.stream())
      ).filter(account -> accountIds.add(account.id))
      .collect(Collectors.toList());
    final var accountCurrencies = storageService.getCurrenciesByIds(accounts.stream()
      .map(account -> account.currencyId)
      .collect(Collectors.toSet()));
    final var currencyIds = new HashSet<String>();
    final var currencies = Stream.concat(storageService.queryCurrencies(currencyQueries).stream(), accountCurrencies.stream())
      .filter(currency -> currencyIds.add(currency.id))
      .collect(Collectors.toList());

    final var categories = storageService.queryCategories(categoryQueries, type);

    final DraftReference draftReference = new DraftReference();
    draftReference.accounts = accounts;
    draftReference.currencies = currencies;
    draftReference.categories = categories;
    draftReference.platforms = platforms;
    return draftReference;
  }
  
}
