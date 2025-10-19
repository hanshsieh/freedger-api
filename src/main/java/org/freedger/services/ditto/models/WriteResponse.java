package org.freedger.services.ditto.models;

import java.util.List;

public class WriteResponse {
  private List<WriteCommandResult> results;

  public List<WriteCommandResult> getResults() {
    return results;
  }

  public void setResults(List<WriteCommandResult> results) {
    this.results = results;
  }
}
