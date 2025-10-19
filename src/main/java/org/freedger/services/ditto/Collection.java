package org.freedger.services.ditto;

public enum Collection {
  LEDGERS("Ledgers"),
  ACCOUNTS("Accounts"),
  CURRENCIES("Currencies");

  private final String name;

  Collection(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
