package com.restmockserver.interceptors;

import java.util.List;

/**
 * Created by aa069w on 2/21/2017.
 */
public interface IInterceptor {

    public static final String RUN = "run";

    /**
     *
     * @param request
     * @param response
     * @param persistenceValues
     * @return
     */
    public String run(String request, String response , List<String> persistenceValues);
}
