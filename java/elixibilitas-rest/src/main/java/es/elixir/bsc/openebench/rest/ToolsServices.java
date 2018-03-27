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
import es.elixir.bsc.elixibilitas.dao.ToolsDAO;
import es.elixir.bsc.openebench.rest.ext.ContentRange;
import es.elixir.bsc.openebench.rest.ext.Range;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URI;
import java.security.Principal;
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
import javax.json.JsonPatch;
import javax.json.JsonPointer;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonWriter;
import javax.json.stream.JsonParser;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.OPTIONS;
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
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 * @author Dmitry Repchevsky
 */

@Path("/")
@ApplicationScoped
public class ToolsServices {

    @Inject
    private MongoClient mc;

    @Inject 
    private ServletContext ctx;
        
    @Context
    private UriInfo uriInfo;

    @Resource
    private ManagedExecutorService executor;

    private String ctx_jsonld;
    
    private ToolsDAO toolsDAO;
    
    @PostConstruct
    public void init() {
        try (InputStream in = ctx.getResourceAsStream("/META-INF/resources/jsonld.owl")) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String ln;
            StringBuilder sb = new StringBuilder();
            while ((ln = reader.readLine()) != null) {
                sb.append(ln).append('\n');
            }
            ctx_jsonld = sb.toString();

        } catch (IOException ex) {
            Logger.getLogger(ToolsServices.class.getName()).log(Level.SEVERE, null, ex);
        }

        final MongoClientURI uri = new MongoClientURI(ctx.getInitParameter("mongodb.url"));
        final String baseURI = UriBuilder.fromUri(uriInfo.getBaseUri()).path(ToolsServices.class).build().toString();

        toolsDAO = new ToolsDAO(mc.getDatabase(uri.getDatabase()), baseURI);
    }

    /**
     * Proxy method to return Tool JSON Schema.
     * 
     * @return JSON Schema for the Tool
     */
    @GET
    @Path("/tool/tool.json")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getToolJsonSchema() {
        return Response.ok(ctx.getResourceAsStream("/META-INF/resources/tool.json")).build();
    }

    /**
     * Proxy method to return Tool JSON Schema.
     * 
     * @return JSON Schema for the Tool
     */
    @GET
    @Path("/tools.owl")
    public Response getToolsOntology() {
        return Response.ok(ctx.getResourceAsStream("/META-INF/resources/tools.owl"), "application/owl+xml").build();
    }
    

    @GET
    @Path("/tool/")
    @Produces("application/ld+json")
    public void getOntology(@Suspended final AsyncResponse asyncResponse) {
        executor.submit(() -> {
            asyncResponse.resume(getOntologyAsync().build());
        });
    }

    private ResponseBuilder getOntologyAsync() {
        StreamingOutput stream = (OutputStream out) -> {
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"))) {
                writer.write(ctx_jsonld);
                toolsDAO.write(writer);
                writer.write("\n}\n]");
            } catch(Exception ex) {
                Logger.getLogger(ToolsServices.class.getName()).log(Level.SEVERE, null, ex);
            }
        };

        return Response.ok(stream);
    }

    @OPTIONS
    @Path("/tool/")
    public Response search() {
         return Response.ok()
                 .header("Access-Control-Allow-Headers", "Range")
                 .header("Access-Control-Expose-Headers", "Accept-Ranges")
                 .header("Access-Control-Expose-Headers", "Content-Range")
                 .build();
    }

    /**
     * Get back all tools as a JSON array.
     * 
     * @param range
     * @param asyncResponse 
     */
    @GET
    @Path("/tool/")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Returns all tools descriptions.",
        responses = {
            @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                            schema = @Schema(ref="https://openebench.bsc.es/monitor/tool/tool.json")))
        }
    )
    public void getTools(@HeaderParam("Range") final Range range,
                         @Suspended final AsyncResponse asyncResponse) {
        executor.submit(() -> {
            asyncResponse.resume(
                    getToolsAsync(range == null ? null : range.getFirstPos(), 
                                  range == null ? null : range.getLastPos())
                    .header("Access-Control-Allow-Headers", "Range")
                    .header("Access-Control-Expose-Headers", "Accept-Ranges")
                    .header("Access-Control-Expose-Headers", "Content-Range")
                    .type(MediaType.APPLICATION_JSON)
                    .build());
        });
    }

    private ResponseBuilder getToolsAsync(final Integer from, final Integer to) {
        StreamingOutput stream = (OutputStream out) -> {
            final Integer limit;
            if (from == null || to == null) {
                limit = to;
            } else {
                limit = to - from;
            }
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"))) {
                toolsDAO.search(writer, null, from, limit, null, null, null, null);
            } catch(Exception ex) {
                Logger.getLogger(ToolsServices.class.getName()).log(Level.SEVERE, null, ex);
            }
        };
        final int count = (int) toolsDAO.count();
        
        final ContentRange range = new ContentRange("tools", from, to, count);
        
        ResponseBuilder response = from == null && to == null 
                ? Response.ok() : Response.status(Response.Status.PARTIAL_CONTENT);
        
        return response.header("Accept-Ranges", "tools").header("Content-Range", range.toString()).entity(stream);
    }

    @GET
    @Path("/tool/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getTools(@PathParam("id") final String id,
                         @Suspended final AsyncResponse asyncResponse) {
        executor.submit(() -> {
            asyncResponse.resume(getToolsAsync(id).build());
        });
    }

    @GET
    @Path("/tool/{id}/{type}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getTools(@PathParam("id") final String id,
                         @PathParam("type") final String type,
                         @Suspended final AsyncResponse asyncResponse) {
        executor.submit(() -> {
            asyncResponse.resume(getToolsAsync(id + "/" + type).build());
        });
    }
    
    @GET
    @Path("/tool/{id}/{type}/{host}{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Returns one or many tools by the id.",
//        parameters = {
//            @Parameter(in = "path", name = "id", description = "prefixed tool id", required = true),
//            @Parameter(in = "path", name = "type", description = "tool type", required = false),
//            @Parameter(in = "path", name = "host", description = "tool authority", required = false),
//            @Parameter(in = "path", name = "path", description = "json pointer", required = false)
//        },
        responses = {
            @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                            schema = @Schema(ref="https://openebench.bsc.es/monitor/tool/tool.json")
            )),
            @ApiResponse(responseCode = "404", description = "tool(s) not found")
        }
    )

    public void getTool(@PathParam("id") final String id,
                        @PathParam("type") final String type,
                        @PathParam("host") final String host,
                        @PathParam("path") final String path,
                        @Suspended final AsyncResponse asyncResponse) {
        executor.submit(() -> {
            asyncResponse.resume(
                    getToolAsync(id + "/" + type + "/" + host, path).build());
        });
    }

    private ResponseBuilder getToolAsync(String id, String path) {
        final String json = toolsDAO.getJSON(id);
        if (json == null) {
            return Response.status(Response.Status.NOT_FOUND);
        }
        if (path != null && path.length() > 0) {
            JsonPointer pointer = Json.createPointer(path);
            JsonStructure structure = Json.createReader(new StringReader(json)).read();
            if (!pointer.containsValue(structure)) {
                return Response.status(Response.Status.NOT_FOUND);
            }
            final JsonValue value = pointer.getValue(structure);
            StreamingOutput stream = (OutputStream out) -> {
                try (JsonWriter writer = Json.createWriter(out)) {
                    writer.write(value);
                } catch(Exception ex) {
                    Logger.getLogger(ToolsServices.class.getName()).log(Level.SEVERE, null, ex);
                }
            };
            return Response.ok(stream);
        }

        return Response.ok(json);
    }
    
    private ResponseBuilder getToolsAsync(String id) {
        final String json = toolsDAO.getTools(id);
        return json != null ? Response.ok(json, MediaType.APPLICATION_JSON_TYPE) :
                              Response.status(Status.NOT_FOUND);
    }
    
    @GET
    @Path("/tool/{id}/{type}/{host}{path:.*}")
    @Produces("application/ld+json")
    public void getToolOntology(@PathParam("id") final String id,
                                @PathParam("type") final String type,
                                @PathParam("host") final String host,
                                @Suspended final AsyncResponse asyncResponse) {
        executor.submit(() -> {
            asyncResponse.resume(getToolOntologyAsync(id + "/" + type + "/" + host).build());
        });
    }

    private ResponseBuilder getToolOntologyAsync(String id) {
        final String json = toolsDAO.getJSON(id);
        if (json == null) {
            return Response.status(Status.NOT_FOUND);
        }
        
        StreamingOutput stream = (OutputStream out) -> {
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"))) {
                writer.write(ctx_jsonld);
                writer.write('[');
                writer.write(json);
                writer.write("\n]\n}\n]");
            } catch(Exception ex) {
                Logger.getLogger(ToolsServices.class.getName()).log(Level.SEVERE, null, ex);
            }
        };

        return Response.ok(stream);
    }

    @PUT
    @Path("/tool/{id : .*}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Inserts the tool into the database."
//        parameters = {
//            @Parameter(in = "path", name = "id", description = "prefixed tool id", required = true)
//        }
    )
    @RolesAllowed("admin")
    public void putTool(@HeaderParam("datasource") final String datasource,
                        @PathParam("id") final String id, 
                        @RequestBody(description = "json tool object",
                            content = @Content(schema = @Schema(ref="https://openebench.bsc.es/monitor/tool/tool.json")),
                            required = true) final String json,
                        @Context javax.ws.rs.core.SecurityContext security,
                        @Suspended final AsyncResponse asyncResponse) {
        

        final Principal principal = security.getUserPrincipal();
        final String user = principal != null ? principal.getName() : null;

        executor.submit(() -> {
            asyncResponse.resume(
                    putToolAsync(datasource == null || datasource.isEmpty() ? user : datasource, id, json).build());
        });
    }
    
    private ResponseBuilder putToolAsync(String user, String id, String json) {
        final String result = toolsDAO.put(user, id, json);
        return Response.status(result == null ? Status.NOT_MODIFIED : Status.OK);
    }

    @PATCH
    @Path("/tool/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Updates tools in the database."
    )
    @RolesAllowed("admin")
    public void patchTools(@HeaderParam("datasource") final String datasource,
                           @RequestBody(description = "batch update of tools properties",
                                required = true) final Reader reader,
                           @Context SecurityContext security,
                           @Suspended final AsyncResponse asyncResponse) {

        
        final Principal principal = security.getUserPrincipal();
        final String user = principal != null ? principal.getName() : null;
        
        executor.submit(() -> {
            asyncResponse.resume(
                    patchToolsAsync(datasource == null || datasource.isEmpty() ? user : datasource, reader).build());
        });
    }

    private ResponseBuilder patchToolsAsync(String user, Reader reader) {
        final JsonParser parser = Json.createParser(reader);
                
        if (parser.hasNext() &&
            parser.next() == JsonParser.Event.START_ARRAY) {
            Stream<JsonValue> stream = parser.getArrayStream();
            stream.forEach(item->{
                if (JsonValue.ValueType.OBJECT == item.getValueType()) {
                    toolsDAO.update(user, item.asJsonObject());
                }
            });
        } else {
            Response.status(Response.Status.BAD_REQUEST);
        }
        return Response.ok();
    }

    @PATCH
    @Path("/tool/{id}/{type}/{host}{path:.*}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Updates the tool in the database.",
        description = "generates and applies JSON PATCH (RFC 6902):\n" +
                      "[{ 'op': 'replace', 'path': $path, 'value': $json }]\n" +
                      "curl -v -X PATCH -u user:pass -H 'Content-Type: application/json' " +
                      "https://openebench.bsc.es/monitor/tool/{id}/description -d '\"new tool description\"'"
        
//        parameters = {
//            @Parameter(in = "path", name = "id", description = "prefixed tool id", required = true),
//            @Parameter(in = "path", name = "type", description = "tool type", required = false),
//            @Parameter(in = "path", name = "host", description = "tool authority", required = false),
//            @Parameter(in = "path", name = "path", description = "json pointer", required = false)
//        }
    )
    @RolesAllowed("admin")
    public void patchTool(@HeaderParam("datasource") final String datasource,
                          @PathParam("id") final String id,
                          @PathParam("type") final String type,
                          @PathParam("host") final String host,
                          @PathParam("path") final String path,
                          @Context final UriInfo uriInfo, 
                          @RequestBody(description = "tool´s property value",
                                required = true) final String json,
                          @Context SecurityContext security,
                          @Suspended final AsyncResponse asyncResponse) {

        final JsonValue value;
        try {
            value = Json.createReader(new StringReader(json)).readValue();
        } catch(Exception ex) {
            asyncResponse.resume(Response.status(Response.Status.BAD_REQUEST.getStatusCode(), ex.getMessage()));
            return;
        }
        
        final Principal principal = security.getUserPrincipal();
        final String user = principal != null ? principal.getName() : null;
        
        final URI uri = uriInfo.getRequestUri();
        executor.submit(() -> {
            asyncResponse.resume(
                    patchToolAsync(datasource == null || datasource.isEmpty() ? user : datasource, uri.toString(), path, value).build());
        });
    }

    private ResponseBuilder patchToolAsync(String user, String id, String path, JsonValue value) {
        final JsonPatch patch = Json.createPatchBuilder().replace(path, value).build();
        final String result = toolsDAO.patch(user, id, patch);
        return Response.status(result == null ? Status.NOT_MODIFIED : Status.OK).entity(result);
    }
    
    @GET
    @Path("/tool/log/{id}/{type}/{host}{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getToolsLog(@PathParam("id") String id,
                           @PathParam("type") String type,
                           @PathParam("host") String host,
                           @PathParam("path") String path,
                           @QueryParam("from") final String  from,
                           @QueryParam("to") final String  to,
                           @QueryParam("limit") final Integer limit,
                           @Suspended final AsyncResponse asyncResponse) {
        if (path == null || path.isEmpty()) {
            asyncResponse.resume(Response.status(Response.Status.BAD_REQUEST).build());
        }
        executor.submit(() -> {
            asyncResponse.resume(getToolsLogAsync(id + "/" + type + "/" + host, path, from, to, limit).build());
        });
    }

    private Response.ResponseBuilder getToolsLogAsync(String id, String field, String  from, String  to, Integer limit) {
        final JsonArray array = toolsDAO.findLog(id, field, from, to, limit);
        if (array == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR);
        }
        if (array.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND);
        }
        
        return Response.ok(array);
    }
}
