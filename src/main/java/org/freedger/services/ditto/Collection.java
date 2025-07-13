package org.freedger.services.ditto;

public enum Collection {
    LEDGERS("Ledgers"),
    ACCOUNTS("Accounts");

    private final String name;

    Collection(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
