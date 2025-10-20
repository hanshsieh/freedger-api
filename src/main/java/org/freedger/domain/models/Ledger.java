package org.freedger.domain.models;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class Ledger {
  @NonNull
  private String id;

  @NonNull
  private Instant createdAt;

  @NonNull
  private Instant updatedAt;

  @NonNull
  private String name;

  @NonNull
  @Builder.Default
  private List<String> readerIds = Collections.emptyList();

  @NonNull
  @Builder.Default
  private List<String> writerIds = Collections.emptyList();

  @NonNull
  @Builder.Default
  private String note = "";

  @NonNull
  private String externalAccountId;

  @NonNull
  private String currencyId;

  public org.freedger.openapi.models.Ledger toOpenApiModel() {
    final var ledger = new org.freedger.openapi.models.Ledger();
    ledger.setId(id);
    ledger.setCreatedAt(createdAt.atOffset(ZoneOffset.UTC));
    ledger.setUpdatedAt(updatedAt.atOffset(ZoneOffset.UTC));
    ledger.setName(name);
    ledger.setReaderIds(readerIds);
    ledger.setWriterIds(writerIds);
    ledger.setNote(note);
    ledger.setExternalAccountId(externalAccountId);
    ledger.setCurrencyId(currencyId);
    return ledger;
  }
}
