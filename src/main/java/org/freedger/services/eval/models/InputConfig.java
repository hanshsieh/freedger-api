package org.freedger.services.eval.models;

public class InputConfig {
  public final String filePath;
  public final int messageCount;

  public InputConfig(String filePath, int messageCount) {
    this.filePath = filePath;
    this.messageCount = messageCount;
  }
}


