package org.freedger.function.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A Ditto collection query that allows read and write access to the documents under the specific
 * ledgers, and read access to the global documents.
 */
public class LedgerChildOrGlobalQuery extends LedgerChildQuery {
  public LedgerChildOrGlobalQuery(String name) {
    super(name);
  }

  public List<String> forReader(List<String> ledgerIds) {
    // legacy QL doesn't support querying docs with a missing field
    // https://support.ditto.live/hc/en-us/requests/2215
    final var clauses = new ArrayList<String>();
    clauses.add("_id['ledgerId'] == null");
    clauses.addAll(
        ledgerIds.stream()
            .map(id -> String.format("_id['ledgerId'] == '%s'", id))
            .collect(Collectors.toList()));
    return clauses;
  }
}
