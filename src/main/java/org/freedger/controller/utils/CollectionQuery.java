package org.freedger.controller.utils;

import java.util.List;

public abstract class CollectionQuery {
  public final String name;

  public CollectionQuery(String name) {
    this.name = name;
  }

  public abstract List<String> forReader(List<String> ledgerIds);

  public abstract List<String> forWriter(List<String> ledgerIds);
}
