package com.restmockserver.persistence.memory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by aa069w on 2/21/2017.
 */
public class MemoryStorage {

    private static      MemoryStorage             memoryStorage     = null;

    private             Map<String, StorageEntry> storageEntries    = null;

    private MemoryStorage() {}

    public static MemoryStorage getInstance() {
        if (memoryStorage == null) {
            memoryStorage =  new MemoryStorage();
        }

        return memoryStorage;
    }

    /**
     * Add value to persistence ID
     * @param persistenceId
     * @param value
     */
    public void add(String persistenceId, String value) {
        laztInit();

        StorageEntry storageEntry = getOrCreate(persistenceId);

        storageEntry.getEntries().add(value);
    }

    public StorageEntry get(String persistenceId, int index) {
        if (storageEntries == null) {
            System.err.println("WARNNING: Can't find any storage entries");
            return null;
        }

        StorageEntry storageEntry = storageEntries.get(persistenceId);

        if (storageEntry == null) {
            System.err.println("WARNNING: Can't find storage for : " + persistenceId);
        }

        return storageEntry;
    }

    private void laztInit() {
        if (storageEntries == null) {
            storageEntries = new HashMap<>();
        }
    }

    private StorageEntry getOrCreate(String persistenceId) {
        if (!storageEntries.containsKey(persistenceId)) {
            storageEntries.put(persistenceId, new StorageEntry());
        }

        return storageEntries.get(persistenceId);
    }
}
