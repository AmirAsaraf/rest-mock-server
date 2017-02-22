package com.restmockserver.utils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by aa069w on 2/12/2017.
 */
public class JsonSearchUtils {

    public static final String SERVICE = "service";
    public static final String REQUESTS = "requests";
    public static final String RESPONSES = "responses";
    public static final String PERSISTENCE = "persistence";
    public static final String INTERCEPTORS = "interceptors";
    public static final String INTERCEPTOR = "interceptor";
    public static final String URL = "url";
    public static final String METHOD = "method";
    public static final String ON_REQUEST = "onRequest";
    public static final String ID = "id";
    public static final String STATUS = "status";
    public static final String BODY = "body";
    public static final String INJECTION_START = "{";
    public static final String INJECTION_END = "}";
    public static final String INJECTION_RESOLVED_URL = "injection.resolvedUrl";
    public static final String HEADERS = "headers";
    public static final String INJECTION_KEY = "injection.key";
    public static final String INJECTION_VALUE = "injection.value";
    public static final String INJECTION_PREFIX = "injection.prefix";
    public static final String INJECTION_POSTFIX = "injection.postfix";
    public static final String REQUEST_FIELD = "request.field";
    public static final String CLASS = "class";

    /*
                subResources = aaa/dsds/
                uriInfo.request.headers
                uriInfo.request.headers
                uriInfo.requestUri.query
                uriInfo.requestUri.string (full url)
                */
    public static JSONObject getRequest(JSONObject jsonObject, String method, String url) {
        JSONArray requests = getRequests(jsonObject);

        createInjections(requests, method, url);

        for (int i = 0 ; i < requests.size() ; i++) {

            JSONObject request = (JSONObject)requests.get(i);

            if ( request.get(METHOD).equals(method) &&
                    ( request.get(URL).equals(url) || (request.containsKey(INJECTION_RESOLVED_URL) && request.get(INJECTION_RESOLVED_URL).equals(url)) )) {
                return request;
            }
        }

        return null;
    }



    public static JSONObject getRequest(JSONObject jsonObject, String id) {
        JSONArray requests = getRequests(jsonObject);
        for (int i = 0 ; i < requests.size() ; i++) {
            if ( ((JSONObject)requests.get(i)).get(ID).equals(id)){
                return ((JSONObject)requests.get(i));
            }
        }

        return null;
    }

    public static JSONObject getResponse(JSONObject jsonObject, String id) {
        JSONArray responses = getResponses(jsonObject);
        for (int i = 0 ; i < responses.size() ; i++) {
            if ( ((JSONObject)responses.get(i)).get(ID).equals(id)){
                return ((JSONObject)responses.get(i));
            }
        }

        return null;
    }

    public static JSONObject getPersistency(JSONObject jsonObject, String id) {
        JSONArray persistencies = getPersistencies(jsonObject);
        for (int i = 0 ; i < persistencies.size() ; i++) {
            if ( ((JSONObject)persistencies.get(i)).get(ID).equals(id)){
                return ((JSONObject)persistencies.get(i));
            }
        }

        return null;
    }

    public static JSONObject getInterceptor(JSONObject jsonObject, String id) {
        JSONArray interceptors = getInterceptors(jsonObject);
        for (int i = 0 ; i < interceptors.size() ; i++) {
            if ( ((JSONObject)interceptors.get(i)).get(ID).equals(id)){
                return ((JSONObject)interceptors.get(i));
            }
        }

        return null;
    }



    //Support single injection per url
    private static void createInjections(JSONArray requests, String method, String url) {
        for (int i = 0 ; i < requests.size() ; i++) {
            JSONObject requestJsonObject = ((JSONObject)requests.get(i));
            if (requestJsonObject.get(METHOD).equals(method)) {
                String requestUrl = (String) requestJsonObject.get(URL);
                //Check if contains injection brackets
                if (!requestUrl.contains(INJECTION_START) && !requestUrl.contains(INJECTION_END)) {
                    continue;
                }

                extractInjection(requestJsonObject, requestUrl, url);
            }
        }
    }

    //Input url with {X} => X
    private static void extractInjection(JSONObject requestJsonObject, String requestUrl, String url) {
        int indexOfPrefix = requestUrl.indexOf(INJECTION_START);
        int indexOfPostfix = requestUrl.indexOf(INJECTION_END);

        String injectionPrefix = requestUrl.substring(0, indexOfPrefix);
        String injectionPostfix = requestUrl.substring(indexOfPostfix + 1, requestUrl.length());

        if (!url.startsWith(injectionPrefix) || !url.endsWith(injectionPostfix)) {
            return;
        }

        requestJsonObject.put(INJECTION_KEY,requestUrl.substring(indexOfPrefix + 1, indexOfPostfix));
        requestJsonObject.put(INJECTION_PREFIX,injectionPrefix);
        requestJsonObject.put(INJECTION_POSTFIX,injectionPostfix);

        int valueStart = (injectionPrefix.length());
        int valueEnd = url.lastIndexOf(injectionPostfix);


        String value = url.substring(valueStart,valueEnd);

        requestJsonObject.put(INJECTION_VALUE, value);
        requestJsonObject.put(INJECTION_RESOLVED_URL, injectionPrefix + value + injectionPostfix);
    }


    private static JSONArray getRequests(JSONObject jsonObject) {
        return (JSONArray) ((JSONObject)jsonObject.get(SERVICE)).get(REQUESTS);
    }

    private static JSONArray getResponses(JSONObject jsonObject) {
        return (JSONArray) ((JSONObject)jsonObject.get(SERVICE)).get(RESPONSES);
    }

    private static JSONArray getPersistencies(JSONObject jsonObject) {
        return (JSONArray) ((JSONObject)jsonObject.get(SERVICE)).get(PERSISTENCE);
    }

    private static JSONArray getInterceptors(JSONObject jsonObject) {
        return (JSONArray) ((JSONObject)jsonObject.get(SERVICE)).get(INTERCEPTORS);
    }

    public static String applyInjections(JSONObject request, String target) {
        String injectionKey = INJECTION_START + request.get(INJECTION_KEY) + INJECTION_END;
        if (target.contains(injectionKey) && request.containsKey(INJECTION_VALUE)) {
            return target.replace(injectionKey, (String) request.get(INJECTION_VALUE));
        }
        return target;
    }

}
