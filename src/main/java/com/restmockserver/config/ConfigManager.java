package com.restmockserver.config;

public class ConfigManager {

    private static Configuration configuration = null;

    public static Configuration getConfiguration() {
         if (configuration == null) {
             configuration = new Configuration();
         }

        return configuration;
    }

}
