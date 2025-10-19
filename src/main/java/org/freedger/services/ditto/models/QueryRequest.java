package org.freedger.services.ditto.models;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

/** Represents a query request to Ditto's query API. */
public class QueryRequest {
  @SerializedName("statement")
  private String statement;

  @SerializedName("args")
  private Map<String, Object> args;

  public QueryRequest() {}

  public QueryRequest(String statement, Map<String, Object> args) {
    this.statement = statement;
    this.args = args;
  }

  public String getStatement() {
    return statement;
  }

  public void setStatement(String query) {
    this.statement = query;
  }

  public Map<String, Object> getArgs() {
    return args;
  }

  public void setArgs(Map<String, Object> args) {
    this.args = args;
  }
}
