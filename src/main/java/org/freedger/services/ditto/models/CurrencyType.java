package org.freedger.services.ditto.models;

import com.google.gson.annotations.SerializedName;

public enum CurrencyType {
  @SerializedName("fiat")
  FIAT,
  @SerializedName("crypto")
  CRYPTO,
  @SerializedName("other")
  OTHER,
}
