package org.freedger.repository.ditto.models;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class WriteRequest {
  @NotNull private List<WriteCommand> commands;

  public List<WriteCommand> getCommands() {
    return commands;
  }

  public void setCommands(List<WriteCommand> commands) {
    this.commands = commands;
  }
}
