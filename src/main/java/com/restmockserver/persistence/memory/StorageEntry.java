package com.restmockserver.persistence.memory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aa069w on 2/21/2017.
 */
public class StorageEntry {
    private List<String> entries = new ArrayList<String>();

    public StorageEntry() {}

    public List<String> getEntries() {
        return entries;
    }

    public void setEntries(List<String> entries) {
        this.entries = entries;
    }

    @Override
    public String toString() {
        return "StorageEntry{" +
                "entries=" + entries +
                '}';
    }
}
