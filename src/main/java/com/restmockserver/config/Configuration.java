package com.restmockserver.config;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configuration {
    //PRODUCTION
    public static final String CONFIG_PROPERTIES_LOCATION = "config.properties";
    //DEV
    //public static final String CONFIG_PROPERTIES_LOCATION = "src/main/java/com/warwarwar/config.properties";
    Properties properties = null;
    InputStream input = null;

    public String read(String key) {
        if (properties == null) {
            initProperties();
        }
        return properties.getProperty(key);
    }

    private void initProperties() {
        try{
            if (input == null) {
                input = new FileInputStream (CONFIG_PROPERTIES_LOCATION);
            }
            if (properties == null) {
                properties = new Properties();
                properties.load(input);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
