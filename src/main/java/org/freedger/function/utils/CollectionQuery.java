package org.freedger.function.utils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class CollectionQuery {
    public final String name;

    public CollectionQuery(String name) {
        this.name = name;
    }

    public abstract List<String> forReader(List<String> ledgerIds);
    public abstract List<String> forWriter(List<String> ledgerIds);
}