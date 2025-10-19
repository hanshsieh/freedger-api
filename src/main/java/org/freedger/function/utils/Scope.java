package org.freedger.function.utils;

public enum Scope {
  // Can read the Ditto authorization config of the user.
  // It's used for the Ditto webhook.
  READ_DITTO_AUTH("read:ditto_auth"),
  // Can read the ledgers of the user.
  READ_LEDGERS("read:ledgers"),
  // Can create and update the ledgers of the user.
  WRITE_LEDGERS("write:ledgers");

  private final String value;

  Scope(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
