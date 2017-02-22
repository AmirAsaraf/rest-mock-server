package com.restmockserver.services;

import com.restmockserver.config.Configuration;
import com.restmockserver.core.HTTPRequestPart;
import com.restmockserver.core.WildCardsResolver;
import com.restmockserver.interceptors.IInterceptor;
import com.restmockserver.persistence.memory.MemoryStorage;
import com.restmockserver.persistence.memory.StorageEntry;
import com.restmockserver.utils.JsonSearchUtils;
import com.sun.jersey.server.impl.application.WebApplicationContext;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by aa069w on 2/9/2017.
 */
public class BaseService {

    private static  BaseService         baseService         = null;

    private         Configuration       configuration       = new Configuration();

    private         JSONObject          serviceDefinition   = null;

    private         WildCardsResolver   wildCardsResolver   = null;

    private         MemoryStorage       memoryStorage       = null;


    private BaseService() {}

    public static BaseService getInstance () {
        if (baseService == null) {
            baseService = new BaseService();
        }

        return baseService;
    }

    public void init() {
        try {
            serviceDefinition = readServiceFile();
            wildCardsResolver = WildCardsResolver.getInstance();
            memoryStorage = MemoryStorage.getInstance();
        }
        catch (Exception e) {
            System.err.println("Failed to start mock server!");
        }
    }

    private JSONObject readServiceFile() throws IOException, ParseException {
        JSONParser parser = new JSONParser();

        try {
            Object obj = parser.parse(new FileReader(configuration.read("service.file.location")));
            System.out.println("Services file loaded successfully...");
            return (JSONObject) obj;

        } catch (ParseException | IOException e) {
            System.err.println("Error : " + e.getMessage());
            throw e;
        }
    }

    public Response handleRequest(String subResources, UriInfo uriInfo, String body) {

        System.out.println("===================");

        //1: Get Request
        JSONObject request = getRequestObject(subResources, (WebApplicationContext) uriInfo);
        if (request == null)
            return null;

        //2: Get onRequest IDs
        JSONArray onRequestActions = handleOnRequestActions(subResources, request);
        if (onRequestActions == null)
            return null;

        //3: Check persistence - supports only memory
        handlePersistence(uriInfo, body, request);

        //4: Response always last
        JSONObject response = getResponseObject(subResources, onRequestActions);

        if (response == null)
            return null;

        Method runMethod = null;
        Object interceptorInstance = null;
        String interceptorClassName = null;
        String InterceptorPersistencyId = null;

        //5: Intercept response
        return handleResponse(request, response, runMethod, interceptorInstance, interceptorClassName, InterceptorPersistencyId);
    }

    private Response handleResponse(JSONObject request, JSONObject response, Method runMethod, Object interceptorInstance, String interceptorClassName, String interceptorPersistencyId) {
        if (response.containsKey(JsonSearchUtils.INTERCEPTOR)) {

            JSONObject interceptorJsonObject = JsonSearchUtils.getInterceptor(this.serviceDefinition, (String) response.get(JsonSearchUtils.INTERCEPTOR));
            interceptorClassName = (String) interceptorJsonObject.get(JsonSearchUtils.CLASS);
            interceptorPersistencyId = (String) interceptorJsonObject.get(JsonSearchUtils.PERSISTENCE);

            try {
                Class<IInterceptor> interceptorClass = (Class<IInterceptor>) Class.forName(interceptorClassName);
                interceptorInstance = interceptorClass.newInstance();
                String methodName = IInterceptor.RUN;
                runMethod = interceptorInstance.getClass().getMethod(methodName, String.class, String.class, List.class);

            } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return convertJsonToHttpResponse(request, response, runMethod, interceptorInstance, interceptorClassName, interceptorPersistencyId);
    }

    private JSONObject getResponseObject(String subResources, JSONArray onRequestActions) {
        JSONObject response = JsonSearchUtils.getResponse(this.serviceDefinition, (String) onRequestActions.get(onRequestActions.size() - 1) );

        if (response == null) {
            System.err.println(" Can't handle " + subResources + " response, since it's not defined!");
            return null;
        }
        return response;
    }

    private void handlePersistence(UriInfo uriInfo, String body, JSONObject request) {
        String persistenceId = (String) request.get(JsonSearchUtils.PERSISTENCE);
        if (persistenceId != null) {

            JSONObject persistence = JsonSearchUtils.getPersistency(this.serviceDefinition, persistenceId);

            String requestField = (String) persistence.get(JsonSearchUtils.REQUEST_FIELD);

            if  (requestField != null) {
                String valueToPersist = getDataFromRequest(uriInfo, HTTPRequestPart.valueOf(requestField.toUpperCase()), body);

                if (valueToPersist != null) {
                    memoryStorage.add(persistenceId, valueToPersist);
                    System.out.println("PERSISTING: key: " + persistenceId + ", value: " + valueToPersist);
                }
            }
        }
    }

    private JSONArray handleOnRequestActions(String subResources, JSONObject request) {
        JSONArray onRequestActions = (JSONArray) request.get(JsonSearchUtils.ON_REQUEST);

        if (onRequestActions == null || onRequestActions.size() == 0) {
            System.err.println("Missing or empty onResponse element for " + subResources);
            return null;
        }

        //3: Execute requests before response
        if (onRequestActions.size() > 1) {
            for (int i = 0 ; i < onRequestActions.size() - 1 ; i++) {
                sendRequest((String) onRequestActions.get(i));
            }
        }
        return onRequestActions;
    }

    private JSONObject getRequestObject(String subResources, WebApplicationContext uriInfo) {
        String method  = uriInfo.getRequest().getMethod();
        JSONObject request = JsonSearchUtils.getRequest(this.serviceDefinition, method, subResources);

        if (request == null) {
            System.err.println("REQUEST:" + method + " " + subResources + " not defined");
            return null;
        }

        System.out.println("REQUEST:" + method + " " + subResources);
        return request;
    }

    private String getDataFromRequest(UriInfo uriInfo, HTTPRequestPart httpRequestPart, String body) {
        if (httpRequestPart == HTTPRequestPart.BODY) {
            return body;
        }

        return null;
    }

    public void sendRequest(String requestId) {

        JSONObject request = JsonSearchUtils.getRequest(this.serviceDefinition, requestId);

        if (request == null) {
            System.err.println("Can't find request with id : " + requestId);
            return;
        }

        HttpClient client = new HttpClient();

        String method = (String) request.get(JsonSearchUtils.METHOD);
        String url = (String) request.get(JsonSearchUtils.URL);
        HttpMethodBase methodToExecute = null;

        switch (method) {
            case "GET" : {
                methodToExecute = new GetMethod(url);
                break;
            }
            case "POST" : {
                methodToExecute = new PostMethod(url);
                break;
            }
            case "PUT" : {
                methodToExecute = new PutMethod(url);
                break;
            }
            case "DELETE" : {
                methodToExecute = new DeleteMethod(url);
                break;
            }
        }
        try {
            System.out.println("- REQUEST:" + method + " " + url);
            int statusCode = client.executeMethod(methodToExecute);
            System.out.println("- RESPONSE:" + statusCode);

        } catch (Exception e) {
            System.err.println("- ERROR:Executing " + method + " " + url + "(" + e.getMessage() + ")");
        } finally {
            methodToExecute.releaseConnection();
        }
    }

    private Response convertJsonToHttpResponse(JSONObject request, JSONObject response, Method runMethod, Object interceptorInstance, String interceptorClassName, String InterceptorPersistencyId) {
        long status = (long) response.get(JsonSearchUtils.STATUS);
        String processedBody = processBody(request, (String) response.get(JsonSearchUtils.BODY));
        response.put(JsonSearchUtils.BODY, processedBody);

        if (runMethod != null && interceptorInstance != null) {
            System.out.println("INTERCEPTING: class: " + interceptorClassName);
            try {
                StorageEntry storageEntry =  memoryStorage.get(InterceptorPersistencyId, 0);

                if (storageEntry != null) {
                    processedBody = (String) runMethod.invoke(interceptorInstance,request.toJSONString(), response.toJSONString(), storageEntry.getEntries());
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                System.out.println("ERROR: intercepting response failed!" + e.getMessage());
            }
        }

        System.out.print("RESPONSE:" + status + "(" + processedBody + ")");

        Response.ResponseBuilder responseBuilder = Response.status((int) status).entity(processedBody);

        addHeaders(responseBuilder, (JSONArray)response.get(JsonSearchUtils.HEADERS));

        System.out.println();

        return responseBuilder.build();
    }

    private String processBody(JSONObject request, String body) {
        body = wildCardsResolver.resolveText(body);
        body = JsonSearchUtils.applyInjections(request, body);
        return body;
    }

    private void addHeaders(Response.ResponseBuilder responseBuilder, JSONArray jsonArray) {
        if (jsonArray != null && jsonArray.size() > 0) {
            for (int i=0 ; i < jsonArray.size() ; i++) {
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);

                String name = wildCardsResolver.resolveText((String) jsonObject.get("name"));
                String value = wildCardsResolver.resolveText((String) jsonObject.get("value"));

                responseBuilder.header(name, value);

                System.out.print("(" + name + ":" + value+ ")");
            }
        }
    }
}
