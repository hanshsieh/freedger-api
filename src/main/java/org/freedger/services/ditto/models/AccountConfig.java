package org.freedger.services.ditto.models;

import java.util.Map;

import jakarta.validation.constraints.NotNull;

public class AccountConfig {
    

    @NotNull
    private String name;

    @NotNull
    private String type;

    private boolean archived;

    private String groupId;

    @NotNull
    private String currencyId;

    private boolean autoClear;

    @NotNull
    private Map<String, AccountChannel> channels;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean isArchived) {
        this.archived = isArchived;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(String currencyId) {
        this.currencyId = currencyId;
    }

    public boolean isAutoClear() {
        return autoClear;
    }

    public void setAutoClear(boolean autoClear) {
        this.autoClear = autoClear;
    }

    public Map<String, AccountChannel> getChannels() {
        return channels;
    }

    public void setChannels(Map<String, AccountChannel> channels) {
        this.channels = channels;
    }
}
