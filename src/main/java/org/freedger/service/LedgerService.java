package org.freedger.service;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.Collections;

import javax.inject.Inject;

import org.freedger.domain.exception.NotFoundException;
import org.freedger.domain.models.CreateLedgerRequest;
import org.freedger.domain.models.Ledger;
import org.freedger.domain.models.Result;
import org.freedger.domain.models.UpdateLedgerRequest;
import org.freedger.repository.ditto.DittoClient;
import org.freedger.repository.ditto.exceptions.DittoNotFoundException;
import org.freedger.repository.ditto.models.GetLedgerRequest;

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

  public Result<Ledger> updateLedger(UpdateLedgerRequest request) throws IOException, NotFoundException {
    try {
      final var dittoLedgerUpdate = new org.freedger.repository.ditto.models.UpdateLedgerRequest() {{
        setId(request.getId());
        setUserId(request.getUserId());
        setName(request.getName());
        setNote(request.getNote());
        setCurrencyId(request.getCurrencyId());
        setExternalAccountId(request.getExternalAccountId());
        setReaderIds(request.getReaderIds());
        setWriterIds(request.getWriterIds());
      }};

      final var updateResp = dittoClient.updateLedger(dittoLedgerUpdate);

      // We aren't able to obtain the created time. To obtain it, we need to get the ledger again.
      final var updatedLedger = dittoClient.getLedger(new GetLedgerRequest() {{
        setId(request.getId());
        setUserId(request.getUserId());
        setTransactionId(updateResp.getTransactionId());
      }}).getData();
      final var respLedger = Ledger.builder()
        .id(updatedLedger.getId())
        .createdAt(updatedLedger.getCreatedAt())
        .updatedAt(updatedLedger.getUpdatedAt())
        .name(updatedLedger.getName())
        .readerIds(updatedLedger.getReaderIds())
        .writerIds(updatedLedger.getWriterIds())
        .note(updatedLedger.getNote())
        .currencyId(updatedLedger.getCurrencyId())
        .externalAccountId(updatedLedger.getExternalAccountId())
        .build();
      return Result.<Ledger>builder()
        .data(respLedger)
        .transactionId(updateResp.getTransactionId())
        .build();
    } catch (DittoNotFoundException e) {
      throw new NotFoundException("Ledger not found");
    }
  }
}
