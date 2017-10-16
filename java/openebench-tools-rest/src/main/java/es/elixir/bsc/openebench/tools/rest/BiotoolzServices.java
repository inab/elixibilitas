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

package es.elixir.bsc.openebench.tools.rest;

import com.mongodb.MongoClient;
import es.elixir.bsc.elixibilitas.tools.dao.ToolDAO;
import io.swagger.oas.annotations.Operation;
import io.swagger.oas.annotations.Parameter;
import io.swagger.oas.annotations.media.Content;
import io.swagger.oas.annotations.media.Schema;
import io.swagger.oas.annotations.parameters.RequestBody;
import io.swagger.oas.annotations.responses.ApiResponse;
import io.swagger.oas.annotations.servers.Server;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonPointer;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonWriter;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

/**
 * @author Dmitry Repchevsky
 */

@Path("/")
public class BiotoolzServices {

    @Inject 
    private ServletContext ctx;
        
    @Inject
    private MongoClient mc;

    @Resource
    private ManagedExecutorService executor;

    private String ctx_jsonld;
    
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
            Logger.getLogger(BiotoolzServices.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    /**
     * Proxy method to return Tool JSON Schema.
     * 
     * @return JSON Schema for the Tool
     */
    @GET
    @Path("/tool.json")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getToolJsonSchema() {
        return Response.ok(ctx.getResourceAsStream("/META-INF/resources/tool.json")).build();
    }

    /**
     * Get back all tools as a JSON array.
     * 
     * @param skip
     * @param limit
     * @param projections
     * @param asyncResponse 
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Return all tools descriptions",
        parameters = {
            @Parameter(in = "query", name = "skip", description = "skip n tools", required = false),
            @Parameter(in = "query", name = "limit", description = "return n tools", required = false),
            @Parameter(in = "query", name = "projection", description = "fields to return", required = false)
        },

        responses = {
            @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                            schema = @Schema(ref="https://elixir.bsc.es/tool/tool.json")))
        }
    )
    public void getTools(@QueryParam("skip") final Integer skip,
                         @QueryParam("limit") final Integer limit,
                         @QueryParam("projection") final List<String> projections,
                         @Suspended final AsyncResponse asyncResponse) {
        executor.submit(() -> {
            asyncResponse.resume(getToolsAsync(skip, limit, projections).build());
        });
    }

    private ResponseBuilder getToolsAsync(Integer skip, Integer to, List<String> projections) {
        StreamingOutput stream = (OutputStream out) -> {
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"))) {
                ToolDAO.write(mc, writer, skip, to, projections);
            }
        };
                
        return Response.ok(stream);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getTools(@QueryParam("projection") final List<String> projections,
                         @Context final UriInfo uriInfo, 
                         @Suspended final AsyncResponse asyncResponse) {
        final URI uri = uriInfo.getRequestUri();
        executor.submit(() -> {
            asyncResponse.resume(getToolsAsync(uri.toString()).build());
        });
    }

    @GET
    @Path("/{id}/{type}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getToolz(@Context final UriInfo uriInfo, 
                         @Suspended final AsyncResponse asyncResponse) {
        final URI uri = uriInfo.getRequestUri();
        executor.submit(() -> {
            asyncResponse.resume(getToolsAsync(uri.toString()).build());
        });
    }
    
    @GET
    @Path("/{id}/{type}/{host}{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        servers = {@Server(url = "https://elixir.bsc.es/tool")},
        description = "Return one or many tools by the id",
        parameters = {
            @Parameter(in = "path", name = "id", description = "prefixed tool id", required = true),
            @Parameter(in = "path", name = "type", description = "tool type", required = false),
            @Parameter(in = "path", name = "host", description = "tool authority", required = false),
            @Parameter(in = "path", name = "path", description = "json pointer", required = false)
        },
        responses = {
            @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                            schema = @Schema(ref="https://elixir.bsc.es/tool/tool.json")
            )),
            @ApiResponse(responseCode = "404", description = "tool(s) not found")
        }
    )
    public void getTool(@PathParam("id") final String id,
                        @PathParam("type") final String type,
                        @PathParam("host") final String host,
                        @PathParam("path") final String path,
                        @Context final UriInfo uriInfo, 
                        @Suspended final AsyncResponse asyncResponse) {
        final URI uri = uriInfo.getRequestUri();
        executor.submit(() -> {
            if (type == null || type.isEmpty() ||
                host == null || host.isEmpty()) {
                asyncResponse.resume(getToolsAsync(uri.toString()).build());
            } else {
                asyncResponse.resume(getToolAsync(uri.toString(), path).build());
            }
        });
    }

    private ResponseBuilder getToolAsync(String id, String path) {
        final String json = ToolDAO.getJSON(mc, id);
        if (json == null) {
            return Response.status(Response.Status.NOT_FOUND);
        }
        if (path != null && path.length() > 0) {
            JsonPointer pointer = Json.createPointer(path);
            JsonStructure structure = Json.createReader(new StringReader(json)).read();
            final JsonValue value = pointer.getValue(structure);
            if (value == null) {
                return Response.status(Response.Status.NOT_FOUND);
            }
            StreamingOutput stream = (OutputStream out) -> {
                try (JsonWriter writer = Json.createWriter(out)) {
                    writer.write(value);
                }
            };
            return Response.ok(stream);
        }

        return Response.ok(json);
    }
    
    private ResponseBuilder getToolsAsync(String uri) {
        final String json = ToolDAO.getJSONArray(mc, uri);
        return json != null ? Response.ok(json, MediaType.APPLICATION_JSON_TYPE) :
                              Response.status(Status.NOT_FOUND);
    }
    
    @GET
    @Path("/{id}/{type}/{host}")
    @Produces("application/ld+json")
    public void getOntology(@Context final UriInfo uriInfo,
                            @Suspended final AsyncResponse asyncResponse) {
        final URI uri = uriInfo.getRequestUri();
        executor.submit(() -> {
            asyncResponse.resume(getOntologyAsync(uri.toString()).build());
        });
    }

    private ResponseBuilder getOntologyAsync(String id) {
        final String json = ToolDAO.getJSON(mc, id);
        if (json == null) {
            return Response.status(Status.NOT_FOUND);
        }
        
        StreamingOutput stream = (OutputStream out) -> {
            out.write(ctx_jsonld.getBytes());
            out.write('[');
            out.write(json.getBytes());
            out.write(']');
            out.write('\n');
            out.write('}');
            out.write('\n');
            out.write(']');
        };

        return Response.ok(stream);
    }

    @PUT
    @Path("/{id : .*}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
        description = "insert the tool into the database",
        parameters = {
            @Parameter(in = "path", name = "id", description = "prefixed tool id", required = true)
        }
    )
    @RolesAllowed("admin")
    public void putTool(@PathParam("id") final String id, 
                        @RequestBody(description = "json tool object",
                            content = @Content(schema = @Schema(ref="https://elixir.bsc.es/tool/tool.json")),
                            required = true) final String json,
                        @Suspended final AsyncResponse asyncResponse) {
        executor.submit(() -> {
            asyncResponse.resume(putToolAsync(id, json).build());
        });
    }
    
    private ResponseBuilder putToolAsync(String id, String json) {
        ToolDAO.put(mc, id, json);
        return Response.ok();
    }
}
