package org.freedger.service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.freedger.domain.models.Ledger;
import org.freedger.repository.ditto.DittoClient;
import org.freedger.service.models.QueryLedgerRequest;

public class LedgerService {
  private final DittoClient dittoClient;
  
  @Inject
  public LedgerService(DittoClient dittoClient) {
    this.dittoClient = dittoClient;
  }

  public List<Ledger> queryLedgers(QueryLedgerRequest request) throws IOException {
    return dittoClient.queryLedgers(request.getUserId(), request.getTransactionId()).getData().stream()
        .map(org.freedger.repository.ditto.models.Ledger::toDomain)
        .collect(Collectors.toList());
  }
}
