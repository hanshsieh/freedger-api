package org.freedger.repository.ditto.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpsertCommand<Id, Value> extends WriteCommand {
  @JsonProperty("id")
  private Id id;

  @JsonProperty("value")
  private Value value;

  public UpsertCommand() {
    super("upsert");
  }

  public Id getId() {
    return id;
  }

  public UpsertCommand<Id, Value> setId(Id id) {
    this.id = id;
    return this;
  }

  public Value getValue() {
    return value;
  }

  public UpsertCommand<Id, Value> setValue(Value value) {
    this.value = value;
    return this;
  }
}
