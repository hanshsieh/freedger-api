package org.freedger.services.ditto.models;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public class WriteRequest {
    @NotNull
    private List<WriteCommand> commands;

    public List<WriteCommand> getCommands() {
        return commands;
    }

    public void setCommands(List<WriteCommand> commands) {
        this.commands = commands;
    }
}
