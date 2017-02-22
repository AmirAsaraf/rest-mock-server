package com.restmockserver.core;

/**
 * Created by aa069w on 2/21/2017.
 */
public enum HTTPRequestPart {
    BODY("body");

    private String name;

    HTTPRequestPart(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
