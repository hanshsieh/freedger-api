package org.freedger;

public enum Scope {
    READ_DITTO_AUTH("read:ditto_auth");

    private final String value;

    Scope(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
