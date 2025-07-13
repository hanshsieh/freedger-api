package org.freedger.services.ditto.models;

import java.util.List;

public class WriteRequest {
    private List<WriteCommand> commands;

    public List<WriteCommand> getCommands() {
        return commands;
    }

    public void setCommands(List<WriteCommand> commands) {
        this.commands = commands;
    }
}
