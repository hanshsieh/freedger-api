package org.freedger.services.ditto.models;

import com.google.gson.annotations.SerializedName;

public enum AccountType {
    @SerializedName("cash")
    CASH,
    @SerializedName("loadable")
    LOADABLE,
    @SerializedName("bank")
    BANK,
    @SerializedName("volatile")
    VOLATILE,
    @SerializedName("credit")
    CREDIT,
    @SerializedName("counterparty")
    COUNTERPARTY,
    @SerializedName("loan")
    LOAN,
    @SerializedName("invoice")
    INVOICE,
}
