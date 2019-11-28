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
import es.elixir.bsc.elixibilitas.meta.ToolsMetaIterator;
import es.elixir.bsc.openebench.rest.ext.ContentRange;
import es.elixir.bsc.openebench.rest.ext.Range;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
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
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonPatch;
import javax.json.JsonPointer;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonWriter;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.OPTIONS;
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
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 * @author Dmitry Repchevsky
 */

@Path("/tool/")
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
    @Path("/tool.json")
    @Produces(MediaType.APPLICATION_JSON)
    @Hidden
    public Response getToolJsonSchema() {
        return Response.ok(ctx.getResourceAsStream("/META-INF/resources/tool.json")).build();
    }
    

    @GET
    @Path("/")
    @Produces("application/ld+json")
    @Hidden
    @Operation(operationId = "getOntology",
        summary = "Returns all tools as an OWL ontology.",
        responses = {
            @ApiResponse(content = @Content(mediaType = "application/ld+json"))
        }
    )    
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
    @Path("/")
    @Hidden
    public Response getTools() {
         return Response.ok()
                 .header("Access-Control-Allow-Headers", "Range")
                 .header("Access-Control-Expose-Headers", "Accept-Ranges")
                 .header("Access-Control-Expose-Headers", "Content-Range")
                 .build();
    }

    @GET
    @Path("/")
    @Produces("text/uri-list")
    public void listToolsIDs(@Suspended final AsyncResponse asyncResponse) {
        executor.submit(() -> {
            asyncResponse.resume(
                    listToolsIDsAsync()
                    .type("text/uri-list")
                    .build());
        });
    }
    
    public ResponseBuilder listToolsIDsAsync() {
        StreamingOutput stream = (OutputStream out) -> {
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"))) {
                toolsDAO.group(writer, null);
            } catch(Exception ex) {
                Logger.getLogger(ToolsServices.class.getName()).log(Level.SEVERE, null, ex);
            }
        };
        return Response.ok(stream);
    }

    /**
     * Get back all tools as a JSON array.
     * 
     * @param range Range header (RFC7233)
     * @param from query parameter substitution for the range.firstPos
     * @param to query parameter substitution for the range.lastPos
     * @param asyncResponse json array that contains all tools objects
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getAllTools",
        summary = "Returns all tools descriptions.",
        description = "returns an array of tools which can be restricted by the standard HTTP 'Range' header.",
        responses = {
            @ApiResponse(content = @Content(
               mediaType = MediaType.APPLICATION_JSON,
               array = @ArraySchema(schema = @Schema(
                   ref="https://openebench.bsc.es/monitor/tool/tool.json"
               ))),
               headers = @Header(name = "Content-Range",
                                 description = "standart HTTP header ('Content-Range: tools 10-30/20000')"))
        }
    )
    public void getTools(@HeaderParam("Range") 
                         @Parameter(description = "HTTP Range Header",
                                    example = "Range: tools=10-30",
                                    schema = @Schema(type = "string")) 
                         final Range range,
                         @QueryParam("from") 
                         @Parameter(description = "alternative to the RFC2733 Range.firstPos",
                                    example = "10",
                                    schema = @Schema(type = "integer"))
                         final Long from,
                         @Parameter(description = "alternative to the RFC2733 Range.lastPos",
                                    example = "30",
                                    schema = @Schema(type = "integer"))
                         @QueryParam("to")
                         final Long to,
                         @Suspended final AsyncResponse asyncResponse) {
        executor.submit(() -> {
            asyncResponse.resume(
                    getToolsAsync(range == null ? from : range.getFirstPos(), 
                                  range == null ? to : range.getLastPos())
                    .header("Access-Control-Allow-Headers", "Range")
                    .header("Access-Control-Expose-Headers", "Accept-Ranges")
                    .header("Access-Control-Expose-Headers", "Content-Range")
                    .type(MediaType.APPLICATION_JSON)
                    .build());
        });
    }

    private ResponseBuilder getToolsAsync(final Long from, final Long to) {
        StreamingOutput stream = (OutputStream out) -> {
            final Long limit;
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
        final long count = (int) toolsDAO.count();
        
        final ContentRange range = new ContentRange("tools", from, to, count);
        
        ResponseBuilder response = from == null && to == null 
                ? Response.ok() : Response.status(Response.Status.PARTIAL_CONTENT);
        
        return response.header("Accept-Ranges", "tools").header("Content-Range", range.toString()).entity(stream);
    }

    @GET
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Returns a tool defined by the id",
        description = "if the id is unprefixed (and unversioned) returns a 'common' tool, " +
                      "otherwise the first matched tool is returned.",
        responses = {
            @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                            schema = @Schema(ref="https://openebench.bsc.es/monitor/tool/tool.json")
            )),
            @ApiResponse(responseCode = "404", description = "tool not found")
        }
    )
    public void getTool(@PathParam("id")
                        @Parameter(description = "the tool id",
                                   example = "'pmut', 'biotools:pmut:2017'")
                        final String id,
                        @Suspended final AsyncResponse asyncResponse) {
        executor.submit(() -> {
            asyncResponse.resume(getToolAsync(id, null).build());
        });
    }

    @GET
    @Path("/{id}/{type}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getTools(@PathParam("id")
                         @Parameter(description = "prefixed tool id",
                                    example = "biotools:pmut:2017")
                         final String id,
                         @PathParam("type")
                         @Parameter(description = "tool type",
                                    example = "web")
                         final String type,
                         @Suspended final AsyncResponse asyncResponse) {
        executor.submit(() -> {
            if (id.indexOf(':') < 0) {
                asyncResponse.resume(getToolAsync(id, "/" + type).build()); // type == path
            } else {
                asyncResponse.resume(getToolsAsync(id + "/" + type).build());
            }
        });
    }
    
    @GET
    @Path("/{id}/{type}/{host}{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Returns either entire tool's json document or its subdocument",
        description = "returns matched tool's document or its property defined by the json path. " +
                      "if more than one documents found (i.e. more than one version of tool matches the id), " +
                      "the first matched document is returned.",
        responses = {
            @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                            schema = @Schema(ref="https://openebench.bsc.es/monitor/tool/tool.json")
            )),
            @ApiResponse(responseCode = "404", description = "tool not found")
        }
    )

    public void getTool(@PathParam("id") 
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
                                   example = "contacts")
                        final String path,
                        @Suspended final AsyncResponse asyncResponse) {
        executor.submit(() -> {
            if (id.indexOf(':') < 0) {
                asyncResponse.resume(getToolAsync(id, "/" + type + "/" + host + "/" + path).build());
            } else {
                asyncResponse.resume(
                    getToolAsync(id + "/" + type + "/" + host, path).build());
            }
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
    @Path("/{id}/{type}/{host}")
    @Produces("application/ld+json")
    @Operation(
        summary = "Returns semantic descriprtion of the tool",
        description = "returns JSON-LD OWL 2 tools description.",
        responses = {
            @ApiResponse(content = @Content(mediaType = "application/ld+json")
            ),
            @ApiResponse(responseCode = "404", description = "tool not found")
        }
    )
    public void getToolOntology(@PathParam("id")
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
    @Path("/{id : .*}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Inserts the tool into the database."
    )
    @RolesAllowed("tools_submitter")
    public void putTool(@PathParam("id")
                        @Parameter(description = "full tool id",
                            example = "biotools:pmut:2017/web/mmb.irbbarcelona.org")
                        final String id,
                        @RequestBody(description = "json tool object",
                            content = @Content(schema = @Schema(ref="https://openebench.bsc.es/monitor/tool/tool.json")),
                            required = true) final String json,
                        @Context javax.ws.rs.core.SecurityContext security,
                        @Suspended final AsyncResponse asyncResponse) {
        

        final Principal principal = security.getUserPrincipal();
        final String user = principal != null ? principal.getName() : null;

        executor.submit(() -> {
            asyncResponse.resume(
                    putToolAsync(user, id, json).build());
        });
    }
    
    private ResponseBuilder putToolAsync(String user, String id, String json) {
        final String result = toolsDAO.put(user, id, json);
        return Response.status(result == null ? Status.NOT_MODIFIED : Status.OK);
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Updates tools in the database."
    )
    @RolesAllowed("tools_submitter")
    public void updateTools(@RequestBody(description = "batch update of tools properties",
                                required = true) final Reader reader,
                            @Context SecurityContext security,
                            @Suspended final AsyncResponse asyncResponse) {

        
        final Principal principal = security.getUserPrincipal();
        final String user = principal != null ? principal.getName() : null;
        
        executor.submit(() -> {
            asyncResponse.resume(
                    updateToolsAsync(user, reader).build());
        });
    }

    private ResponseBuilder updateToolsAsync(final String user, final Reader reader) {
        try(JsonParser parser = Json.createParser(reader)) {
            if (parser.hasNext() &&
                parser.next() == JsonParser.Event.START_ARRAY) {
                Stream<JsonValue> stream = parser.getArrayStream();
                stream.forEach(item->{
                    if (JsonValue.ValueType.OBJECT == item.getValueType()) {
                        final JsonObject object = item.asJsonObject();
                        toolsDAO.upsert(user, object);
                    }
                });
            } else {
                return Response.status(Response.Status.BAD_REQUEST);
            }
        } catch (Exception ex) {
            Logger.getLogger(ToolsServices.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(Response.Status.BAD_REQUEST);
        }

        return Response.ok();
    }

    @PATCH
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Updates tools in the database."
    )
    @RolesAllowed("tools_submitter")
    public void patchTools(@RequestBody(description = "batch update of tools properties",
                                required = true) final Reader reader,
                           @Context SecurityContext security,
                           @Suspended final AsyncResponse asyncResponse) {

        
        final Principal principal = security.getUserPrincipal();
        final String user = principal != null ? principal.getName() : null;
        
        executor.submit(() -> {
            asyncResponse.resume(
                    patchToolsAsync(user, reader).build());
        });
    }

    private ResponseBuilder patchToolsAsync(String user, Reader reader) {
        final JsonParser parser = Json.createParser(reader);
                
        if (parser.hasNext() &&
            parser.next() == JsonParser.Event.START_ARRAY) {
            Stream<JsonValue> stream = parser.getArrayStream();
            stream.forEach(item->{
                if (JsonValue.ValueType.OBJECT == item.getValueType()) {
                    toolsDAO.merge(user, item.asJsonObject());
                }
            });
        } else {
            Response.status(Response.Status.BAD_REQUEST);
        }
        return Response.ok();
    }

    @PATCH
    @Path("/{id}/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Updates the tool in the database.",
        description = "merges the data into the stored tool.")
    @RolesAllowed("tools_submitter")
    public void patchTool(@PathParam("id")
                          @Parameter(description = "unprefixed tool id",
                                     example = "pmut") final String id,
                          @RequestBody(description = "tool´s property value",
                                required = true) final String json,
                          @Context SecurityContext security,
                          @Suspended final AsyncResponse asyncResponse) {
        
        final Principal principal = security.getUserPrincipal();
        final String user = principal != null ? principal.getName() : null;

        executor.submit(() -> {
            asyncResponse.resume(
                patchToolAsync(user, id, null, json).build());
        });
    }

    @PATCH
    @Path("/{id}/{type}/{host}{path:.*}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Updates the tool in the database.",
        description = "If no $path defined, merges the data into the stored tool,\n " +
                      "otherwise, generates and applies JSON PATCH (RFC 6902):\n" +
                      "[{ 'op': 'replace', 'path': $path, 'value': $json }]\n" +
                      "curl -v -X PATCH -u user:pass -H 'Content-Type: application/json' " +
                      "https://openebench.bsc.es/monitor/tool/{id}/description -d '\"new tool description\"'"
    )
    @RolesAllowed("tools_submitter")
    public void patchTool(@PathParam("id") final String id,
                          @PathParam("type") final String type,
                          @PathParam("host") final String host,
                          @PathParam("path") final String path, 
                          @RequestBody(description = "tool´s property value",
                                required = true) final String json,
                          @Context SecurityContext security,
                          @Suspended final AsyncResponse asyncResponse) {
        
        final Principal principal = security.getUserPrincipal();
        final String user = principal != null ? principal.getName() : null;

        executor.submit(() -> {
            asyncResponse.resume(
                patchToolAsync(user, id + '/' + type + '/' + host, path, json).build());
        });
    }

    private ResponseBuilder patchToolAsync(String user, String id, String path, String json) {
        
        final String result;
        
        if (path == null || path.isEmpty()) {
            result = toolsDAO.merge(user, id, json);
        } else {
            final JsonValue value;
            try {
                value = Json.createReader(new StringReader(json)).readValue();
            } catch(Exception ex) {
                return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), ex.getMessage());
            }

            final JsonPatch patch = Json.createPatchBuilder().replace(path, value).build();
            result = toolsDAO.patch(user, id, patch);
        }
        
        return Response.status(result == null ? Status.NOT_MODIFIED : Status.OK).entity(result);
    }
    
    @GET
    @Path("/log/{id}/{type}/{host}{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getToolsLog(@PathParam("id") String id,
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

    @GET
    @Path("/meta")
    @Produces(MediaType.APPLICATION_JSON)
    public void getToolsMeta(@Suspended final AsyncResponse asyncResponse) {
        executor.submit(() -> {
            asyncResponse.resume(getToolsMetaAsync(null).build());
        });
    }
    
    @GET
    @Path("/meta/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getToolsMeta(@PathParam("id") String id,
                             @Suspended final AsyncResponse asyncResponse) {
        executor.submit(() -> {
            asyncResponse.resume(getToolsMetaAsync(id).build());
        });
    }

    private Response.ResponseBuilder getToolsMetaAsync(String id) {
        StreamingOutput stream = (OutputStream out) -> {
            try (JsonGenerator gen = Json.createGenerator(out);
                 PipedReader reader = new PipedReader();
                 JsonParser parser = Json.createParser(reader)) {
                
                final PipedWriter writer = new PipedWriter(reader);
                executor.submit(() -> {
                    try {
                        toolsDAO.search(writer, id, null, null, null, null, null, null);
                    }
                    finally {
                        try {
                            writer.close();
                        } catch(IOException ex) {}
                    }
                });
                
                if (parser.hasNext() &&
                    parser.next() == JsonParser.Event.START_ARRAY) {

                    gen.writeStartArray();
                    
                    ToolsMetaIterator iterator = new ToolsMetaIterator(parser.getArrayStream());
                    while (iterator.hasNext()) {
                        final JsonValue meta = iterator.next();
                        gen.write(meta);
                    }
                    
                    gen.writeEnd();
                }

            } catch(Exception ex) {
                Logger.getLogger(ToolsServices.class.getName()).log(Level.SEVERE, null, ex);
            }
        };
        return Response.ok(stream);
    }

}
