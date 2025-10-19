package org.freedger.services.ditto.models;

import com.google.gson.annotations.SerializedName;

public enum InstrumentCategory {
  @SerializedName("stocks")
  STOCKS,
  @SerializedName("funds")
  FUNDS,
  @SerializedName("futures")
  FUTURES,
  @SerializedName("forex")
  FOREX,
  @SerializedName("crypto")
  CRYPTO,
  @SerializedName("indices")
  INDICES,
  @SerializedName("bonds")
  BONDS,
  @SerializedName("economy")
  ECONOMY,
  @SerializedName("options")
  OPTIONS,
  @SerializedName("other")
  OTHER,
}
