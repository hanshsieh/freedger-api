package org.freedger.repository.ditto.models;

import com.google.gson.annotations.SerializedName;

public class RpcError {
  @SerializedName("message")
  private String message;

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
