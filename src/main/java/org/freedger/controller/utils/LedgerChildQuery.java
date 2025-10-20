package org.freedger.controller.utils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A Ditto collection query that allows read and write access to the documents under the specific
 * ledgers.
 */
public class LedgerChildQuery extends CollectionQuery {
  public LedgerChildQuery(String name) {
    super(name);
  }

  public List<String> forReader(List<String> ledgerIds) {
    return buildQueryForLedgers(ledgerIds);
  }

  public List<String> forWriter(List<String> ledgerIds) {
    return buildQueryForLedgers(ledgerIds);
  }

  private List<String> buildQueryForLedgers(List<String> ledgerIds) {
    return ledgerIds.stream()
        .map(id -> String.format("_id['ledgerId'] == '%s'", id))
        .collect(Collectors.toList());
  }
}
