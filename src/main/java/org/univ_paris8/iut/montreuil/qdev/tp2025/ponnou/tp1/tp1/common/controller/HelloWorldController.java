package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Demo", description = "Endpoints de demonstration (Exercice 1)")
public class HelloWorldController {

    @GET
    @Path("/helloWorld")
    @Operation(summary = "Hello World", description = "Endpoint simple de test retournant un message Hello World.")
    public Response helloWorld() {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("message", "Hello World");
        return Response.ok(body).build();
    }

    @GET
    @Path("/params")
    @Operation(summary = "Demo QueryParams", description = "Endpoint de demonstration des QueryParams.")
    public Response queryParams(@QueryParam("name") @DefaultValue("World") String name,
                                @QueryParam("count") @DefaultValue("1") int count) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", name);
        body.put("count", count);
        body.put("message", "Hello " + name + " (x" + count + ")");
        return Response.ok(body).build();
    }

    @GET
    @Path("/params/{id}")
    @Operation(summary = "Demo PathParams", description = "Endpoint de demonstration des PathParams combine avec QueryParams.")
    public Response pathParams(@PathParam("id") Long id,
                               @QueryParam("detail") @DefaultValue("false") boolean detail) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", id);
        body.put("detail", detail);
        body.put("message", "Ressource #" + id + (detail ? " (avec detail)" : ""));
        return Response.ok(body).build();
    }
}
