package org.freedger.services.ditto.models;

import com.google.gson.annotations.SerializedName;

public class UpsertCommand<Id, Value> extends WriteCommand {
  @SerializedName("id")
  private Id id;

  @SerializedName("value")
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
