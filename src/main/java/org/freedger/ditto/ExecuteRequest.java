package org.freedger.ditto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Represents a query request to Ditto's query API.
 */
public class ExecuteRequest {
    @JsonProperty("statement")
    private String statement;

    @JsonProperty("args")
    private Map<String, Object> args;

    public ExecuteRequest() {
    }

    public ExecuteRequest(String statement, Map<String, Object> args) {
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
