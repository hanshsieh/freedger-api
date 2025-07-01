package org.freedger.dto.ditto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the permission rules for read/write access.
 */
public class PermissionRules {
    @SerializedName("everything")
    private boolean everything;
    
    @SerializedName("queriesByCollection")
    private Map<String, List<String>> queriesByCollection;
    
    public PermissionRules() {
        this(false);
    }
    
    public PermissionRules(boolean everything) {
        this.everything = everything;
        this.queriesByCollection = new HashMap<>();
    }
    
    public void addQuery(String collection, String query) {
        this.queriesByCollection
            .computeIfAbsent(collection, _ -> new ArrayList<>())
            .add(query);
    }
    
    // Getters and Setters
    public boolean isEverything() {
        return everything;
    }

    public void setEverything(boolean everything) {
        this.everything = everything;
    }

    public Map<String, List<String>> getQueriesByCollection() {
        return queriesByCollection;
    }

    public void setQueriesByCollection(Map<String, List<String>> queriesByCollection) {
        this.queriesByCollection = queriesByCollection != null ? 
            new HashMap<>(queriesByCollection) : new HashMap<>();
    }
}