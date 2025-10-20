package org.freedger.domain.models;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class DittoAuthRequest {
  @NonNull
  String userId;
  String transactionId;
}
