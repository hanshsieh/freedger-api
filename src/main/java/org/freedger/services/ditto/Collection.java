package org.freedger.services.ditto;

public enum Collection {
  LEDGERS("Ledgers"),
  ACCOUNTS("Accounts"),
  CURRENCIES("Currencies"),
  INSTRUMENTS("Instruments"),
  QUOTES("Quotes");

  private final String name;

  Collection(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
