package org.freedger.domain.models;

import java.time.Instant;
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
}
