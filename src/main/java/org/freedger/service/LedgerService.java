package org.freedger.service;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.Collections;

import javax.inject.Inject;

import org.freedger.domain.models.CreateLedgerRequest;
import org.freedger.domain.models.Ledger;
import org.freedger.domain.models.Result;
import org.freedger.repository.ditto.DittoClient;

public class LedgerService {
  private final DittoClient dittoClient;
  
  @Inject
  public LedgerService(DittoClient dittoClient) {
    this.dittoClient = dittoClient;
  }

  public Result<Ledger> createLedger(CreateLedgerRequest request) throws IOException {
    final var dittoLedgerCreate = new org.freedger.repository.ditto.models.CreateLedgerRequest();
    dittoLedgerCreate.setName(request.getName());
    dittoLedgerCreate.setNote(request.getNote());
    dittoLedgerCreate.setCurrencyId(request.getCurrencyId());
    dittoLedgerCreate.setExternalAccountName(request.getExternalAccountName());
    dittoLedgerCreate.setReaderIds(Collections.emptyList());
    dittoLedgerCreate.setWriterIds(Collections.singletonList(request.getUserId()));

    final var dittoResp = dittoClient.createLedger(dittoLedgerCreate);
    final var dittoLedger = dittoResp.getData();
    final var respLedger = Ledger.builder()
      .id(dittoLedger.getId())
      .createdAt(dittoLedger.getCreatedAt())
      .updatedAt(dittoLedger.getUpdatedAt())
      .name(dittoLedger.getName())
      .readerIds(dittoLedger.getReaderIds())
      .writerIds(dittoLedger.getWriterIds())
      .note(dittoLedger.getNote())
      .currencyId(dittoLedger.getCurrencyId())
      .externalAccountId(dittoLedger.getExternalAccountId())
      .build();
    return Result.<Ledger>builder()
      .data(respLedger)
      .transactionId(dittoResp.getTransactionId())
      .build();
  }
}
