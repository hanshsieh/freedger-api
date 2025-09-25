package org.freedger.services.openai;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.freedger.services.eval.models.Account;
import org.freedger.services.eval.models.Category;
import org.freedger.services.eval.models.Currency;
import org.freedger.services.eval.models.Platform;
import org.freedger.services.openai.models.AccountCategory;
import org.freedger.services.openai.models.Storage;
import org.freedger.services.openai.models.TransactionType;

import com.fasterxml.jackson.databind.ObjectMapper;

public class StorageService {
  private final Storage storage;

  private StorageService(Storage storage) {
    this.storage = storage;
  }

  public static StorageService createFromFile(String filePath) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    Storage storage = objectMapper.readValue(loadResourceAsString(filePath), Storage.class);
    return new StorageService(storage);
  }

  public List<Account> getAccountsByIds(Set<String> ids) {
    return storage.accounts.stream()
      .filter(account -> ids.contains(account.id))
      .collect(Collectors.toList());
  }

  public List<Account> queryAccounts(List<String> queries, AccountCategory category) {
    return storage.accounts.stream()
      .filter(account -> account.category.equals(category))
      .filter(account -> queries.stream().map(String::toLowerCase).anyMatch(query -> 
        account.name.toLowerCase().contains(query) ||
        account.groupName != null && account.groupName.toLowerCase().contains(query) ||
        account.channels.stream().anyMatch(channel -> channel.name.toLowerCase().contains(query)))
      )
      .collect(Collectors.toList());
  }

  public List<Category> queryCategories(List<String> queries, TransactionType transactionType) {
    return storage.categories.stream()
      .filter(category -> category.transactionType.equals(transactionType))
      .filter(category -> queries.stream().map(String::toLowerCase).anyMatch(query -> 
        category.name.toLowerCase().contains(query) ||
        category.groupName != null && category.groupName.toLowerCase().contains(query)))
      .collect(Collectors.toList());
  }

  public List<Platform> queryPlatforms(List<String> queries) {
    return storage.platforms.stream()
      .filter(platform -> queries.stream().map(String::toLowerCase).anyMatch(query -> 
        platform.name.toLowerCase().contains(query)))
      .collect(Collectors.toList());
  }

  public List<Currency> queryCurrencies(List<String> queries) {
    return storage.currencies.stream()
      .filter(currency -> queries.stream().map(String::toLowerCase).anyMatch(query -> 
        currency.name.toLowerCase().contains(query) ||
        currency.code.toLowerCase().contains(query)))
      .collect(Collectors.toList());
  }

  public List<Currency> getCurrenciesByIds(Set<String> ids) {
    return storage.currencies.stream()
      .filter(currency -> ids.contains(currency.id))
      .collect(Collectors.toList());
  }

  private static String loadResourceAsString(String resourcePath) {
    try (var inputStream = StorageService.class.getClassLoader().getResourceAsStream(resourcePath)) {
      if (inputStream == null) {
        throw new IllegalStateException("Resource not found: " + resourcePath);
      }
      return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new RuntimeException("Failed to read resource: " + resourcePath, e);
    }
  }
}
