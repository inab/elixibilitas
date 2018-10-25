/**
 * *****************************************************************************
 * Copyright (C) 2017 ELIXIR ES, Spanish National Bioinformatics Institute (INB)
 * and Barcelona Supercomputing Center (BSC)
 *
 * Modifications to the initial code base are copyright of their respective
 * authors, or their employers as appropriate.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 *****************************************************************************
 */

package es.elixir.bsc.openebench.rest;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import es.elixir.bsc.elixibilitas.dao.AlambiqueDAO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.security.Principal;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonPatch;
import javax.json.JsonPointer;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonWriter;
import javax.json.stream.JsonParser;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 * @author Dmitry Repchevsky
 */

@Path("/alambique/")
@ApplicationScoped
public class AlambiqueServices {
    
    @Inject
    private MongoClient mc;

    @Inject 
    private ServletContext ctx;

    @Context
    private UriInfo uriInfo;

    @Resource
    private ManagedExecutorService executor;
    
    private AlambiqueDAO alambiqueDAO;

    @PostConstruct
    public void init() {
        final String baseURI = UriBuilder.fromUri(uriInfo.getBaseUri()).path(AlambiqueServices.class).build().toString();

        final MongoClientURI mongodbURI = new MongoClientURI(ctx.getInitParameter("mongodb.url"));
        alambiqueDAO = new AlambiqueDAO(mc.getDatabase(mongodbURI.getDatabase()), baseURI);
    }
    
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Returns all documents.",
        parameters = {
            @Parameter(in = ParameterIn.PATH, name = "projection", description = "fields to return", required = false)
        },
        responses = {
            @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON),
                         description = "JSON array"
            ),
            @ApiResponse(responseCode = "404", description = "document not found")
        }
    )
    public void getAlambique(@QueryParam("projection")
                             @Parameter(description = "properties to be returned",
                                        example = "project.license.open_source")
                             final List<String> projections,
                             @Suspended final AsyncResponse asyncResponse) {
        executor.submit(() -> {
            asyncResponse.resume(getAlambiqueAsync(projections).build());
        });
    }

    private Response.ResponseBuilder getAlambiqueAsync(List<String> projections) {
        StreamingOutput stream = (OutputStream out) -> {
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"))) {
                alambiqueDAO.write(writer, projections);
            }
        };
                
        return Response.ok(stream);
    }
    
    @GET
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Returns documents by the unprefixed tool's id.",
        responses = {
            @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON),
                         description = "JSON document"
            ),
            @ApiResponse(responseCode = "404", description = "document not found")
        }
    )
    public void getAlambique(@PathParam("id")
                             @Parameter(description = "unprefixed tool id",
                                        example = "pmut")
                             final String id,
                             @Suspended final AsyncResponse asyncResponse) {
        executor.submit(() -> {
            asyncResponse.resume(getAlambiqueAsync(id, null).build());
        });
    }

    @GET
    @Path("/{id}/{type}/{host}{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Returns a document by the tool's id.",
        responses = {
            @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON),
                         description = "JSON document or 'null' if not found"
            ),
            @ApiResponse(responseCode = "404", description = "document not found")
        }
    )
    public void getAlambique(@PathParam("id")
                             @Parameter(description = "prefixed tool id",
                                        example = "biotools:pmut:2017")
                             final String id,
                             @PathParam("type")
                             @Parameter(description = "tool type",
                                        example = "web") 
                             final String type,
                             @PathParam("host")
                             @Parameter(description = "tool authority",
                                        example = "mmb.irbbarcelona.org")
                             final String host,
                             @PathParam("path")
                             @Parameter(description = "json pointer",
                                        example = "project")
                             final String path,
                             @Suspended final AsyncResponse asyncResponse) {
        executor.submit(() -> {
            asyncResponse.resume(getAlambiqueAsync(id + '/' + type + '/' + host, path).build());
        });
    }

    private Response.ResponseBuilder getAlambiqueAsync(String id, String path) {
        final String json = alambiqueDAO.getJSON(id);
        if (json == null || json.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND);
        }
        if (path != null && path.length() > 0) {
            JsonPointer pointer = Json.createPointer(path);
            JsonStructure structure = Json.createReader(new StringReader(json)).read();
            try {
                if (!pointer.containsValue(structure)) {
                    return Response.ok("null");
                }
            } catch(JsonException ex) {
                return Response.ok("null");
            }
            final JsonValue value = pointer.getValue(structure);
            StreamingOutput stream = (OutputStream out) -> {
                try (JsonWriter writer = Json.createWriter(out)) {
                    writer.write(value);
                }
            };
            return Response.ok(stream);
        }
        
        return Response.ok(json);
    }
    
    @PUT
    @Path("/{id : .*}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Inserts the document into the database."
    )
    @RolesAllowed("alambique_submitter")
    public void putAlambique(@PathParam("id")
                             @Parameter(description = "full tool id",
                                 example = "biotools:pmut:2017/web/mmb.irbbarcelona.org") 
                             final String id,
                             @RequestBody(description = "json document", required = true) 
                             final String json,
                             @Context SecurityContext security,
                             @Suspended final AsyncResponse asyncResponse) {

        final Principal principal = security.getUserPrincipal();
        final String user = principal != null ? principal.getName() : null;
        
        executor.submit(() -> {
            asyncResponse.resume(
                    putAlambiqueAsync(user, id, json).build());
        });
    }

    private Response.ResponseBuilder putAlambiqueAsync(String source, String id, String json) {
        alambiqueDAO.put(source, id, json);
        return Response.ok();
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Updates documents in the database.",
        description = "Accepts an array of JSON documents with defined @id " +
                      "(i.e. 'https://openebench.bsc.es/monitor/alambique/biotools:pmut:2017/web/mmb.irbbarcelona.org'). " +
                      "If the document is already exists - properties are merged."
    )
    @RolesAllowed("alambique_submitter")
    public void updateAlambique(@RequestBody(
                                    description = "batch update of documents",
                                    content = @Content(
                                        mediaType = MediaType.APPLICATION_JSON),
                                    required = true) final Reader reader,
                                @Context SecurityContext security,
                                @Suspended final AsyncResponse asyncResponse) {

        final Principal principal = security.getUserPrincipal();
        final String user = principal != null ? principal.getName() : null;
        
        executor.submit(() -> {
            asyncResponse.resume(
                updateAlambiqueAsync(user, reader).build());
        });
    }

    private Response.ResponseBuilder updateAlambiqueAsync(final String user, final Reader reader) {
        final JsonParser parser = Json.createParser(reader);
                
        if (parser.hasNext() &&
            parser.next() == JsonParser.Event.START_ARRAY) {
            try {
                Stream<JsonValue> stream = parser.getArrayStream();
                stream.forEach(item->{
                    if (JsonValue.ValueType.OBJECT == item.getValueType()) {
                        final JsonObject object = item.asJsonObject();
                        alambiqueDAO.upsert(user, object);
                    }
                });
            } catch (Exception ex) {
                Response.status(Response.Status.BAD_REQUEST);
                Logger.getLogger(AlambiqueServices.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            Response.status(Response.Status.BAD_REQUEST);
        }
        return Response.ok();
    }

    @PATCH
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Updates documents in the database.",
        description = "Accepts an array of JSON documents with defined @id " +
                      "(i.e. 'https://openebench.bsc.es/monitor/alambique/biotools:pmut:2017/web/mmb.irbbarcelona.org'). " +
                      "Method uses mongodb 'upsert' operation."
    )
    @RolesAllowed("alambique_submitter")
    public void patchAlambique(@RequestBody(
                                   description = "batch update of documents",
                                   content = @Content(
                                       mediaType = MediaType.APPLICATION_JSON),
                                   required = true) final Reader reader,
                               @Context SecurityContext security,
                               @Suspended final AsyncResponse asyncResponse) {

        final Principal principal = security.getUserPrincipal();
        final String user = principal != null ? principal.getName() : null;
        
        executor.submit(() -> {
            asyncResponse.resume(
                patchAlambiqueAsync(user, reader).build());
        });
    }
    
    private Response.ResponseBuilder patchAlambiqueAsync(final String user, final Reader reader) {
        final JsonParser parser = Json.createParser(reader);
                
        if (parser.hasNext() &&
            parser.next() == JsonParser.Event.START_ARRAY) {
            try {
                Stream<JsonValue> stream = parser.getArrayStream();
                stream.forEach(item->{
                    if (JsonValue.ValueType.OBJECT == item.getValueType()) {
                        final JsonObject object = item.asJsonObject();
                        alambiqueDAO.merge(user, object);
                    }
                });
            } catch (Exception ex) {
                Response.status(Response.Status.BAD_REQUEST);
                Logger.getLogger(AlambiqueServices.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            Response.status(Response.Status.BAD_REQUEST);
        }
        return Response.ok();
    }

    @PATCH
    @Path("/{id}/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Updates documents in the database.",
        description = "Accepts JSON document as an input."
    )
    @RolesAllowed("alambique_submitter")
    public void patchAlambique(@PathParam("id")
                               @Parameter(description = "prefixed tool id",
                                          example = "biotools:pmut:2017") 
                               final String id,
                               @RequestBody(description = "partial document",
                                      content = @Content(mediaType = MediaType.APPLICATION_JSON),
                                  required = true) final String json,
                               @Context SecurityContext security,
                               @Suspended final AsyncResponse asyncResponse) {

        final Principal principal = security.getUserPrincipal();
        final String user = principal != null ? principal.getName() : null;
        
        executor.submit(() -> {
            asyncResponse.resume(patchAlambiqueAsync(user, id, null, json).build());
        });
    }

    @PATCH
    @Path("/{id}/{type}/{host}{path:.*}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Updates documents in the database.",
        description = "Accepts JSON document as an input. " +
                      "Merges the documents if the 'path' is empty or uses JSON Patch otherwise."
    )
    @RolesAllowed("alambique_submitter")
    public void patchAlambique(@PathParam("id")
                               @Parameter(description = "prefixed tool id",
                                          example = "biotools:pmut:2017") 
                               final String id,
                               @PathParam("type")
                               @Parameter(description = "tool type",
                                         example = "web") 
                               final String type,
                               @PathParam("host")
                               @Parameter(description = "tool authority",
                                          example = "mmb.irbbarcelona.org")
                               final String host,
                               @PathParam("path")
                               @Parameter(description = "json pointer",
                                          example = "project")
                               final String path,
                               @RequestBody(description = "the property value",
                                     content = @Content(mediaType = MediaType.APPLICATION_JSON),
                                     required = true)
                               final String json,
                               @Context SecurityContext security,
                               @Suspended final AsyncResponse asyncResponse) {

        final Principal principal = security.getUserPrincipal();
        final String user = principal != null ? principal.getName() : null;
        
        executor.submit(() -> {
            asyncResponse.resume(patchAlambiqueAsync(user, id + '/' + type + '/' + host, path, json).build());
        });
    }
    
    private Response.ResponseBuilder patchAlambiqueAsync(String user, String id, String path, String json) {
        
        final String result;
        
        if (path == null || path.isEmpty()) {
            result = alambiqueDAO.merge(user, id, json);
        } else {
            final JsonValue value;
            try {
                value = Json.createReader(new StringReader(json)).readValue();
            } catch(Exception ex) {
                return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), ex.getMessage());
            }

            final JsonPatch patch = Json.createPatchBuilder().replace(path, value).build();
            result = alambiqueDAO.patch(user, id, patch);
        }
        
        return Response.status(result == null ? Response.Status.NOT_MODIFIED : Response.Status.OK);
    }
    
    @GET
    @Path("/log/{id}/{type}/{host}{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Retrieves documents changes log"
    )
    public void getAlambiqueLog(@PathParam("id") String id,
                                @PathParam("type") String type,
                                @PathParam("host") String host,
                                @PathParam("path") String path,
                                @QueryParam("from") final String from,
                                @QueryParam("to") final String to,
                                @QueryParam("limit") final Integer limit,
                                @Suspended final AsyncResponse asyncResponse) {
        if (path == null || path.isEmpty()) {
            asyncResponse.resume(Response.status(Response.Status.BAD_REQUEST).build());
        }
        executor.submit(() -> {
            asyncResponse.resume(getAlambiqueLogAsync(id + "/" + type + "/" + host, path, from, to, limit).build());
        });
    }

    private Response.ResponseBuilder getAlambiqueLogAsync(String id, String field, String from, String to, Integer limit) {
        final JsonArray array = alambiqueDAO.findLog(id, field, from, to, limit);
        if (array == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR);
        }
        if (array.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND);
        }
        
        return Response.ok(array);
    }
}
