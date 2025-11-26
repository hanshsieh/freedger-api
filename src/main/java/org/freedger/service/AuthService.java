package org.freedger.service;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.freedger.domain.config.AppConfig;
import org.freedger.domain.models.CollectionQuery;
import org.freedger.domain.models.DittoAuthRequest;
import org.freedger.domain.models.LedgerChildOrGlobalQuery;
import org.freedger.domain.models.LedgerChildQuery;
import org.freedger.openapi.models.AuthorizeResponse;
import org.freedger.openapi.models.Permission;
import org.freedger.openapi.models.PermissionRules;
import org.freedger.repository.ditto.DittoClient;
import org.freedger.repository.ditto.models.Ledger;

public class AuthService {
  private static final List<CollectionQuery> collections =
    List.of(
        new LedgerChildQuery("AccountChannels"),
        new LedgerChildQuery("AccountGroups"),
        new LedgerChildQuery("Accounts"),
        new LedgerChildQuery("Categories"),
        new LedgerChildQuery("CategoryGroups"),
        new LedgerChildOrGlobalQuery("Currencies"),
        new LedgerChildOrGlobalQuery("Instruments"),
        new LedgerChildQuery("Journals"),
        new CollectionQuery("Ledgers") {
          @Override
          public List<String> forReader(List<String> ledgerIds) {
            return ledgerIds.stream()
                .map(id -> String.format("_id == '%s'", id))
                .collect(Collectors.toList());
          }

          @Override
          public List<String> forWriter(List<String> ledgerIds) {
            return Collections.emptyList();
          }
        },
        new LedgerChildQuery("Platforms"),
        new LedgerChildQuery("ProjectGroups"),
        new LedgerChildQuery("Projects"),
        new LedgerChildOrGlobalQuery("Quotes"),
        new LedgerChildQuery("Tags"),
        new LedgerChildQuery("Transactions"),
        new LedgerChildQuery("Goals"),
        new LedgerChildQuery("GoalHistories"));

  private final AppConfig config;
  private final DittoClient dittoClient;
  
  @Inject
  public AuthService(AppConfig config, DittoClient dittoClient) {
    this.config = config;
    this.dittoClient = dittoClient;
  }

  public AuthorizeResponse dittoAuthorize(DittoAuthRequest request) throws IOException {
    List<Ledger> accessibleLedgers =
      dittoClient.queryLedgers(request.getUserId(), request.getTransactionId()).getData();

    AuthorizeResponse response = buildAuthResponse(request.getUserId(), accessibleLedgers);
    return response;
  }

  /**
   * Builds the permissions response for accessible ledgers.
   *
   * @param userId The user ID
   * @param ledgers List of ledgers the user has access to
   * @return DittoWebhookResponse with the appropriate permissions
   */
  private AuthorizeResponse buildAuthResponse(String userId, List<Ledger> ledgers) {
    final var readQueries = new HashMap<String, List<String>>();
    final var writeQueries = new HashMap<String, List<String>>();
    final var ledgerIds = ledgers.stream().map(Ledger::getId).collect(Collectors.toList());

    for (var collection : collections) {
      readQueries.put(collection.name, collection.forReader(ledgerIds));
      writeQueries.put(collection.name, collection.forWriter(ledgerIds));
    }

    // Create permission rules for read and write
    final var readRules =
        new PermissionRules()
            .everything(false)
            .queriesByCollection(
                readQueries.entrySet().stream()
                    .filter(e -> !e.getValue().isEmpty())
                    .collect(
                        Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue().stream().sorted().collect(Collectors.toList()))));
    final var writeRules =
        new PermissionRules()
            .everything(false)
            .queriesByCollection(
                writeQueries.entrySet().stream()
                    .filter(e -> !e.getValue().isEmpty())
                    .collect(
                        Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue().stream().sorted().collect(Collectors.toList()))));

    final var permissions = new Permission().read(readRules).write(writeRules);
    return new AuthorizeResponse()
        .authenticate(true)
        .userID(userId)
        .expirationSeconds((int) config.getDitto().getTokenExpiresIn().toSeconds())
        .permissions(permissions);
  }
}
