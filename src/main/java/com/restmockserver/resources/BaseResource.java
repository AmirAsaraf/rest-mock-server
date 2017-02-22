package com.restmockserver.resources;

import com.restmockserver.services.BaseService;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * Created by aa069w on 1/12/2017.
 */
@Path("/")
public class BaseResource {

    private BaseService baseService = null;

    @GET
    @Path("{subResources: [a-zA-Z0-9_/]+}")
    public Response getHandler(
            @Context final UriInfo uriInfo,
            @PathParam("subResources") String subResources)
    {
        return BaseService.getInstance().handleRequest(subResources, uriInfo, null);
    }

    @POST
    @Path("{subResources: [a-zA-Z0-9_/]+}")
    public Response postHandler(
            @Context final UriInfo uriInfo,
            @PathParam("subResources") String subResources,
            String body)
    {
        return BaseService.getInstance().handleRequest(subResources, uriInfo, body);
    }

    @PUT
    @Path("{subResources: [a-zA-Z0-9_/]+}")
    public Response putHandler(
            @Context final UriInfo uriInfo,
            @PathParam("subResources") String subResources,
            String body)
    {
        return BaseService.getInstance().handleRequest(subResources, uriInfo, body);
    }

    @DELETE
    @Path("{subResources: [a-zA-Z0-9_/]+}")
    public Response deleteHandler(
            @Context final UriInfo uriInfo,
            @PathParam("subResources") String subResources,
            String body)
    {
        return BaseService.getInstance().handleRequest(subResources, uriInfo, body);
    }
}
