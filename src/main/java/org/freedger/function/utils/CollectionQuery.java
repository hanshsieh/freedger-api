package org.freedger.function.utils;

import java.util.List;
import java.util.stream.Collectors;

public class CollectionQuery {
    public final String name;

    public CollectionQuery(String name) {
        this.name = name;
    }

    public List<String> forReader(List<String> ledgerIds) {
        return buildQueryForLedgers(ledgerIds);
    }
    public List<String> forWriter(List<String> ledgerIds) {
        return buildQueryForLedgers(ledgerIds);
    }

    private List<String> buildQueryForLedgers(List<String> ledgerIds) {
        if (ledgerIds.isEmpty()) {
            return List.of();
        }
        final var clauses = ledgerIds.stream()
            .map(id -> String.format("_id['ledgerId'] == '%s'", id))
            .collect(Collectors.toList());
        return List.of(String.join(" || ", clauses));
    }
}