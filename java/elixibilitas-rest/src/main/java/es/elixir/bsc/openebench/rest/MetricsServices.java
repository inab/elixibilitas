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
import es.elixir.bsc.elixibilitas.dao.MetricsDAO;
import es.elixir.bsc.openebench.rest.validator.JsonSchema;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
import javax.ws.rs.HeaderParam;
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
 * REST Service to operate over Metrics objects.
 * 
 * @author Dmitry Repchevsky
 */

@Path("/metrics/")
@ApplicationScoped
public class MetricsServices {
    
    @Inject
    private MongoClient mc;

    @Inject 
    private ServletContext ctx;

    @Context
    private UriInfo uriInfo;

    @Resource
    private ManagedExecutorService executor;
    
    private MetricsDAO metricsDAO;
    
    @PostConstruct
    public void init() {
        final String baseURI = UriBuilder.fromUri(uriInfo.getBaseUri()).path(MetricsServices.class).build().toString();

        final MongoClientURI mongodbURI = new MongoClientURI(ctx.getInitParameter("mongodb.url"));
        metricsDAO = new MetricsDAO(mc.getDatabase(mongodbURI.getDatabase()), baseURI);
    }

    /**
     * Proxy method to return Metrics JSON Schema.
     * 
     * @param ctx injected servlet context.
     * 
     * @return JSON Schema for the Metrics
     */
    @GET
    @Path("/metrics.json")
    @Produces(MediaType.APPLICATION_JSON)
    @Hidden
    public Response getMetricsJsonSchema(@Context ServletContext ctx) {
        return Response.ok(ctx.getResourceAsStream("/META-INF/resources/metrics.json")).build();
    }

    @GET
    @Path("/{id}/{type}/{host}{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Returns tools metrics by the tool's id.",
        responses = {
            @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                            schema = @Schema(ref="https://openebench.bsc.es/monitor/metrics/metrics.json")),
                         description = "Metrics JSON description"
            ),
            @ApiResponse(responseCode = "404", description = "metrics not found")
        }
    )
    public void getMetrics(@PathParam("id")
                           @Parameter(description = "prefixed tool id",
                                      example = "bio.tools:pmut:2017")
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

        if (id == null || id.isEmpty() ||
            type == null || type.isEmpty() ||
            host == null || host.isEmpty()) {
            asyncResponse.resume(Response.status(Response.Status.NOT_FOUND).build());
        }
                    
        executor.submit(() -> {
            asyncResponse.resume(getMetricsAsync(id + '/' + type + '/' + host, path).build());
        });
    }

    private ResponseBuilder getMetricsAsync(String id, String path) {
        final String json = metricsDAO.getJSON(id);
        if (json == null || json.isEmpty()) {
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
                }
            };
            return Response.ok(stream);
        }
        
        return Response.ok(json);
    }
    
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Returns all tools metrics.",
        parameters = {
            @Parameter(in = ParameterIn.PATH, name = "projection", description = "fields to return", required = false)
        },
        responses = {
            @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                            schema = @Schema(ref="https://openebench.bsc.es/monitor/metrics/metrics.json")),
                         description = "JSON array of metrics"
            ),
            @ApiResponse(responseCode = "404", description = "metrics not found")
        }
    )
    public void getMetrics(@QueryParam("projection")
                           @Parameter(description = "properties to be returned",
                                      example = "project.license.open_source")
                           final List<String> projections,
                           @Suspended final AsyncResponse asyncResponse) {
        executor.submit(() -> {
            asyncResponse.resume(getToolsAsync(projections).build());
        });
    }

    private ResponseBuilder getToolsAsync(List<String> projections) {
        StreamingOutput stream = (OutputStream out) -> {
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"))) {
                metricsDAO.write(writer, projections);
            }
        };
                
        return Response.ok(stream);
    }
    
    @PUT
    @Path("/{id : .*}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Inserts the metrics into the database."
    )
    @RolesAllowed("metrics_submitter")
    public void putMetrics(@PathParam("id")
                           @Parameter(description = "full tool id",
                               example = "bio.tools:pmut:2017/web/mmb.irbbarcelona.org") 
                           final String id,
                           @RequestBody(description = "json metrics object",
                              content = @Content(schema = @Schema(ref="https://openebench.bsc.es/monitor/metrics/metrics.json")),
                              required = true) 
                           @JsonSchema(location="metrics.json") final String json,
                           @Context SecurityContext security,
                           @Suspended final AsyncResponse asyncResponse) {

        final Principal principal = security.getUserPrincipal();
        final String user = principal != null ? principal.getName() : null;
        
        executor.submit(() -> {
            asyncResponse.resume(
                    putMetricsAsync(user, id, json).build());
        });
    }

    private Response.ResponseBuilder putMetricsAsync(String source, String id, String json) {
        metricsDAO.put(source, id, json);
        return Response.ok();
    }

    @PATCH
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Updates metrics in the database.",
        description = "Accepts an array of JSON documents with defined @id " +
                      "(i.e. 'https://openebench.bsc.es/monitor/metrics/bio.tools:pmut:2017/web/mmb.irbbarcelona.org'). " +
                      "Method uses mongodb 'upsert' operation."
    )
    @RolesAllowed("metrics_submitter")
    public void patchMetrics(@RequestBody(
                                 description = "batch update of metrics properties",
                                 content = @Content(
                                     mediaType = MediaType.APPLICATION_JSON),
                                 required = true) final Reader reader,
                             @Context final UriInfo uriInfo,
                             @Context SecurityContext security,
                             @Suspended final AsyncResponse asyncResponse) {

        final String prefix = uriInfo.getRequestUri().toString();
        final Principal principal = security.getUserPrincipal();
        final String user = principal != null ? principal.getName() : null;
        
        executor.submit(() -> {
            asyncResponse.resume(
                    patchMetricsAsync(prefix, user, reader).build());
        });
    }
    
    private ResponseBuilder patchMetricsAsync(final String prefix, final String user, final Reader reader) {
        final JsonParser parser = Json.createParser(reader);
                
        if (parser.hasNext() &&
            parser.next() == JsonParser.Event.START_ARRAY) {
            try {
                Stream<JsonValue> stream = parser.getArrayStream();
                stream.forEach(item->{
                    if (JsonValue.ValueType.OBJECT == item.getValueType()) {
                        final JsonObject object = item.asJsonObject();
                        String id = object.getString("@id");
                        if (id != null && id.startsWith(prefix)) {
                            try {
                                id = id.substring(prefix.length());
                                metricsDAO.update(user, id, object.toString());
                            } catch (Exception ex) {
                                Logger.getLogger(MetricsServices.class.getName()).log(Level.SEVERE, id, ex);
                            }
                        }
                    }
                });
            } catch (Exception ex) {
                Response.status(Response.Status.BAD_REQUEST);
                Logger.getLogger(MetricsServices.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            Response.status(Response.Status.BAD_REQUEST);
        }
        return Response.ok();
    }

    @PATCH
    @Path("/{id}/{type}/{host}{path:.*}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Updates metrics in the database.",
        description = "Accepts JSON document as an input. " +
                      "Uses mongodb 'upsert' if the 'path' is empty or JSON Patch otherwise."
    )
    @RolesAllowed("metrics_submitter")
    public void patchMetrics(@HeaderParam("datasource") final String datasource,
                             @PathParam("id")
                             @Parameter(description = "prefixed tool id",
                                        example = "bio.tools:pmut:2017") 
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
                             @RequestBody(description = "metrics property value",
                                    content = @Content(mediaType = MediaType.APPLICATION_JSON),
                                required = true) final String json,
                             @Context SecurityContext security,
                             @Suspended final AsyncResponse asyncResponse) {

        final Principal principal = security.getUserPrincipal();
        final String user = principal != null ? principal.getName() : null;
        
        executor.submit(() -> {
            asyncResponse.resume(patchMetricsAsync(datasource == null || datasource.isEmpty() ? user : datasource, id + '/' + type + '/' + host, path, json).build());
        });
    }
    
    private ResponseBuilder patchMetricsAsync(String user, String id, String path, String json) {
        
        final String result;
        
        if (path == null || path.isEmpty()) {
            result = metricsDAO.update(user, id, json);
        } else {
            final JsonValue value;
            try {
                value = Json.createReader(new StringReader(json)).readValue();
            } catch(Exception ex) {
                return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), ex.getMessage());
            }

            final JsonPatch patch = Json.createPatchBuilder().replace(path, value).build();
            result = metricsDAO.patch(user, id, patch);
        }
        
        return Response.status(result == null ? Status.NOT_MODIFIED : Status.OK);
    }
    
    @GET
    @Path("/log/{id}/{type}/{host}{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Retrieves metrics changes log"
    )
    public void getMetricsLog(@PathParam("id") String id,
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
            asyncResponse.resume(getMetricsLogAsync(id + "/" + type + "/" + host, path, from, to, limit).build());
        });
    }

    private Response.ResponseBuilder getMetricsLogAsync(String id, String field, String from, String to, Integer limit) {
        final JsonArray array = metricsDAO.findLog(id, field, from, to, limit);
        if (array == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR);
        }
        if (array.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND);
        }
        
        return Response.ok(array);
    }
}
