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
import es.elixir.bsc.elixibilitas.dao.ToolsDAO;
import es.elixir.bsc.openebench.checker.MetricsChecker;
import es.elixir.bsc.openebench.rest.ext.ContentRange;
import es.elixir.bsc.openebench.rest.ext.Range;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.servers.Server;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonPointer;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonWriter;
import javax.json.stream.JsonGenerator;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 * REST Service to get some statistics.
 * 
 * @author Dmitry Repchevsky
 */

@OpenAPIDefinition(info = @Info(title = "OpenEBench REST API services", 
                                version = "0.1", 
                                description = "OpenEBench REST API services",
                                license = @License(name = "LGPL 2.1", 
                                            url = "https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html"),
                                contact = @Contact(url = "https://openebench.bsc.es")
                                ),
                    //security = @SecurityRequirement(name = "openid-connect"), 
                    servers = {@Server(url = "https://openebench.bsc.es/")})
@Path("/rest/")
@ApplicationScoped
public class MonitorRestServices {
    
    @Inject
    private MongoClient mc;

    @Inject 
    private ServletContext ctx;

    @Context
    private UriInfo uriInfo;

    @Resource
    private ManagedExecutorService executor;
    
    private ToolsDAO toolsDAO;
    private MetricsDAO metricsDAO;
    
    private Map<String, MetricsChecker> checkers;
    
    @PostConstruct
    public void init() {
        
        final String toolsBaseURI = UriBuilder.fromUri(uriInfo.getBaseUri()).path(ToolsServices.class).build().toString();
        final String metricsBaseURI = UriBuilder.fromUri(uriInfo.getBaseUri()).path(MetricsServices.class).build().toString();
        
        final MongoClientURI mongodbURI = new MongoClientURI(ctx.getInitParameter("mongodb.url"));
        
        toolsDAO = new ToolsDAO(mc.getDatabase(mongodbURI.getDatabase()), toolsBaseURI);
        metricsDAO = new MetricsDAO(mc.getDatabase(mongodbURI.getDatabase()), metricsBaseURI);
        
        checkers = MetricsChecker.checkers();
    }

    @OPTIONS
    @Path("/search")
    public Response search() {
         return Response.ok()
                 .header("Access-Control-Allow-Headers", "Range")
                 .header("Access-Control-Expose-Headers", "Accept-Ranges")
                 .header("Access-Control-Expose-Headers", "Content-Range")
                 .build();
    }

    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Returns all tools descriptions.",
               description = "Queries the tools with a possibility to limit the response. " +
                             "The response is grouped by ids and sorted by names.",
//        parameters = {
//            @Parameter(in = QUERY, name = "skip", description = "skip 'n' tools"),
//            @Parameter(in = QUERY, name = "limit", description = "return 'n' tools"),
//            @Parameter(in = QUERY, name = "projection", description = "tools properties to return"),
//            @Parameter(in = QUERY, name = "text", description = "text to search"),
//            @Parameter(in = QUERY, name = "name", description = "text to search in the 'name' property"),
//            @Parameter(in = QUERY, name = "description", description = "text to search in the 'description' property")
//        },

        responses = {
            @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                            schema = @Schema(ref="https://openebench.bsc.es/monitor/tool/tool.json")))
        }

//        responses = {
//            @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON,
//                                            array = @ArraySchema(
//                                            schema = @Schema(ref="https://openebench.bsc.es/monitor/tool/tool.json"))))
//        }
    )
    public void search(@HeaderParam("Range") final Range range,
                       @QueryParam("id") final String id,
                       @QueryParam("projection") final List<String> projections,
                       @QueryParam("text") final String text,
                       @QueryParam("name") final String name,
                       @QueryParam("description") final String description,
                       @Suspended final AsyncResponse asyncResponse) {
        executor.submit(() -> {
            asyncResponse.resume(searchAsync(id, range != null ? range.getFirstPos() :  null, 
                    range != null ? range.getLastPos() : null,
                    projections, text, name, description)
                        .header("Access-Control-Allow-Headers", "Range")
                        .header("Access-Control-Expose-Headers", "Accept-Ranges")
                        .header("Access-Control-Expose-Headers", "Content-Range")
                        .build());
        });
    }
    
    private Response.ResponseBuilder searchAsync(
                              final String id,
                              final Integer from, 
                              final Integer to, 
                              final List<String> projections, 
                              final String text,
                              final String name,
                              final String description) {

        StreamingOutput stream = (OutputStream out) -> {

            final Integer limit;
            if (from == null || to == null) {
                limit = to;
            } else {
                limit = to - from;
            }

            try (Writer writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"))) {
                toolsDAO.search(writer, id, from, limit, text, name, description, projections);
            }
        };

        final int count = toolsDAO.search_count(id, text, name, description);
        
        final ContentRange range = new ContentRange("tools", from, to, count);
        
        ResponseBuilder response = from == null && to == null 
                ? Response.ok() : Response.status(Response.Status.PARTIAL_CONTENT);
        
        return response.header("Accept-Ranges", "tools").header("Content-Range", range.toString()).entity(stream);
    }

    @OPTIONS
    @Path("/aggregate")
    public Response aggregate() {
         return Response.ok()
                 .header("Access-Control-Allow-Headers", "Range")
                 .header("Access-Control-Expose-Headers", "Accept-Ranges")
                 .header("Access-Control-Expose-Headers", "Content-Range")
                 .build();
    }
    
    @GET
    @Path("/aggregate")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Returns all tools descriptions.",
               description = "The same as '/search' with a difference in the output format",
//        parameters = {
//            @Parameter(in = QUERY, name = "skip", description = "skip 'n' tools"),
//            @Parameter(in = QUERY, name = "limit", description = "return 'n' tools"),
//            @Parameter(in = QUERY, name = "projection", description = "tools properties to return"),
//            @Parameter(in = QUERY, name = "text", description = "text to search"),
//            @Parameter(in = QUERY, name = "name", description = "text to search in the 'name' property"),
//            @Parameter(in = QUERY, name = "description", description = "text to search in the 'description' property")
//        },

        responses = {
            @ApiResponse(content = 
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                             schema = @Schema(ref="https://openebench.bsc.es/monitor/tool/tool.json")))
        }
    )

    public void aggregate(@HeaderParam("Range") final Range range,
                          @QueryParam("id") final String id,
                          @QueryParam("skip") final Integer skip,
                          @QueryParam("limit") final Integer limit,
                          @QueryParam("projection") final List<String> projections,
                          @QueryParam("text") final String text,
                          @QueryParam("name") final String name,
                          @QueryParam("description") final String description,
                          @Suspended final AsyncResponse asyncResponse) {
        executor.submit(() -> {
            if (range != null) {
                asyncResponse.resume(aggregateAsync(id, range.getFirstPos(), range.getLastPos(), projections, text, name, description)
                        .header("Access-Control-Allow-Headers", "Range")
                        .header("Access-Control-Expose-Headers", "Accept-Ranges")
                        .header("Access-Control-Expose-Headers", "Content-Range")
                        .build());
            } else {
                final Integer from = skip;
                Integer to = limit;
                if (from != null && to != null) {
                    to += limit;
                }
                
                asyncResponse.resume(aggregateAsync(id, from, to, projections, text, name, description)
                        .header("Access-Control-Allow-Headers", "Range")
                        .header("Access-Control-Expose-Headers", "Accept-Ranges")
                        .header("Access-Control-Expose-Headers", "Content-Range")
                        .build());
            }
        });
    }
    
    private Response.ResponseBuilder aggregateAsync(final String id, 
                              final Integer from, 
                              final Integer to, 
                              final List<String> projections, 
                              final String text,
                              final String name,
                              final String description) {

        StreamingOutput stream = (OutputStream out) -> {
            final Integer limit;
            if (from == null || to == null) {
                limit = to;
            } else {
                limit = to - from;
            }
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"))) {
                toolsDAO.aggregate(writer, id, from, limit, text, name, description, projections);
            }
        };
        final int count = toolsDAO.aggregate_count(id, text, name, description);
        
        final ContentRange range = new ContentRange("items", from, to, count);
        
        ResponseBuilder response = from == null && to == null 
                ? Response.ok() : Response.status(Response.Status.PARTIAL_CONTENT);
        
        return response.header("Accept-Ranges", "items")
                       .header("Content-Range", range.toString()).entity(stream);
    }

    @GET
    @Path("/metrics/{id}/{type}/{host}{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public void metrics(@PathParam("id") String id,
                        @PathParam("type") String type,
                        @PathParam("host") String host,
                        @PathParam("path") String path,
                        @Suspended final AsyncResponse asyncResponse) {
        
        if (path == null || path.isEmpty()) {
            asyncResponse.resume(Response.status(Response.Status.BAD_REQUEST).build());
        }
        
        executor.submit(() -> {
            asyncResponse.resume(getMetricsAsync(id + '/' + type + '/' + host, path).build());
        });
    }
    
    private Response.ResponseBuilder getMetricsAsync(String id, String path) {        
        final MetricsChecker checker = checkers.get(path);
        if (checker == null) {
            return Response.status(Response.Status.NOT_FOUND);
        }
        
        final String json = toolsDAO.getJSON(id);
        if (json == null || json.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND);
        }

        final JsonPointer pointer = Json.createPointer(checker.getToolPath());
        final JsonStructure structure = Json.createReader(new StringReader(json)).read();
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

    @GET
    @Path("/widget/tool/{id:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getToolWidget(@PathParam("id") String id,
                          @Suspended final AsyncResponse asyncResponse) {
        executor.submit(() -> {
            asyncResponse.resume(getToolWidgetAsync(id).build());
        });
    }

    private Response.ResponseBuilder getToolWidgetAsync(String id) {
        final JsonArray array = toolsDAO.getJSONArray(id);
        
        if (array.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND);
        }
        
        JsonObject object = array.getJsonObject(0);
        for (int i = 1; i < array.size(); i++) {
            final JsonObject obj = array.getJsonObject(i);
            if (obj.getString("@timestamp", "").compareTo(object.getString("@timestamp", "")) > 0) {
                object = obj;
            }
        }
        return Response.ok(object);
    }

    @GET
    @Path("/widget/metrics/{id:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getMetricsWidget(@PathParam("id") String id,
                          @Suspended final AsyncResponse asyncResponse) {
        executor.submit(() -> {
            asyncResponse.resume(getMetricsWidgetAsync(id).build());
        });
    }

    private Response.ResponseBuilder getMetricsWidgetAsync(String id) {
        final JsonArray array = toolsDAO.getJSONArray(id);
        
        if (array.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND);
        }
        
        JsonObject object = array.getJsonObject(0);
        for (int i = 1; i < array.size(); i++) {
            final JsonObject obj = array.getJsonObject(i);
            if (obj.getString("@timestamp", "").compareTo(object.getString("@timestamp", "")) > 0) {
                object = obj;
            }
        }
        
        final String _id = object.getString("@id", null);
        
        final String json = metricsDAO.getJSON(_id.substring(toolsDAO.baseURI.length()));
        if (json == null) {
            return Response.status(Response.Status.NOT_FOUND);
        }
        
        return Response.ok(json);
    }
    
    @GET
    @Path("/homepage/{id}/{type}/{host}{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getHomePageMonitoring(@PathParam("id") String id,
                           @PathParam("type") String type,
                           @PathParam("host") String host,
                           @PathParam("path") String path,
                           @Suspended final AsyncResponse asyncResponse) {
        executor.submit(() -> {
            asyncResponse.resume(getHomePageMonitoringAsync(id + "/" + type + "/" + host).build());
        });
    }
            
    private Response.ResponseBuilder getHomePageMonitoringAsync(String id) {
        final JsonArray operational = metricsDAO.findLog(id, "/project/website/operational", null, null, null);
        if (operational == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR);
        } else if (operational.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND);
        }
        
        final JsonArray access_time = metricsDAO.findLog(id, "/project/website/access_time", null, null, null);
        if (access_time == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR);
        } else if (access_time.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND);
        }
        
        StreamingOutput stream = (OutputStream out) -> {
            try (JsonGenerator writer = Json.createGenerator(out)) {
                writer.writeStartArray();

                String code = "0";
                String date = "1970-01-01T00:00:00.000Z";
                JsonObject obj = operational.getJsonObject(0);

                for (int i = 0, j = 1, m = access_time.size(), n = operational.size(); i < m; i++) {
                    writer.writeStartObject();

                    final JsonObject o = access_time.getJsonObject(i);
                    final String adate = o.getString("date", null);
                    final String time = o.getString("value", "0");

                    if (adate.compareTo(date) >= 0) {
                        code = obj.getString("value", "0");
                        if (j < n) {
                            obj = operational.getJsonObject(j++);
                            date = obj.getString("date", null);
                        }
                    }

                    writer.write("date", adate);
                    writer.write("code", Integer.parseInt(code));
                    writer.write("access_time", Integer.parseInt(time));

                    writer.writeEnd();
                }
                
                writer.writeEnd();
            }
        };
        
        return Response.ok(stream);
    }
    
    @GET
    @Path("/statistics/")
    @Produces(MediaType.APPLICATION_JSON)
    public void getStatistics(@Suspended final AsyncResponse asyncResponse) {
            
        executor.submit(() -> {
            asyncResponse.resume(getStatisticsAsync().build());
        });
    }

    private Response.ResponseBuilder getStatisticsAsync() {
        
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("all", Json.createObjectBuilder()
                     .add("total", getStatistics("total"))
                     .add("operational", getStatistics("operational")));
        addMetricsStatistics(builder, "cmd");
        addMetricsStatistics(builder, "web");
        addMetricsStatistics(builder, "db");
        addMetricsStatistics(builder, "app");
        addMetricsStatistics(builder, "lib");
        addMetricsStatistics(builder, "ontology");
        addMetricsStatistics(builder, "workflow");
        addMetricsStatistics(builder, "plugin");
        addMetricsStatistics(builder, "sparql");
        addMetricsStatistics(builder, "soap");
        addMetricsStatistics(builder, "script");
        addMetricsStatistics(builder, "rest");
        addMetricsStatistics(builder, "workbench");
        addMetricsStatistics(builder, "suite");

        return Response.ok(builder.build());
    }

    @GET
    @Path("/statistics/count/{field}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getStatisticsRepo(@PathParam("field") String field,
                                  @QueryParam("text") String text,
                                  @Suspended final AsyncResponse asyncResponse) {
            
        executor.submit(() -> {
            asyncResponse.resume(getStatisticsRepo(field, text).build());
        });
    }
    
    private Response.ResponseBuilder getStatisticsRepo(final String field, final String text) {
        return Response.ok(toolsDAO.count(field, text));
    }

    @GET
    @Path("/statistics/{field}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getMetrics(@PathParam("field") String field,
                           @Suspended final AsyncResponse asyncResponse) {
            
        executor.submit(() -> {
            asyncResponse.resume(getStatisticsAsync(field).build());
        });
    }
    
    private Response.ResponseBuilder getStatisticsAsync(final String field) {
        
        long result = getStatistics(field);
        return result == Long.MIN_VALUE ? Response.status(Response.Status.BAD_REQUEST) :
                         Response.ok(result);
    }
    
    public void addMetricsStatistics(JsonObjectBuilder builder, String field) {
        builder.add(field, Json.createObjectBuilder()
                     .add("total", getStatistics(field))
                     .add("operational", getMetricsStatistics(field)));
    }
    
    private long getStatistics(final String field) {
        switch(field) {
            case "total": return toolsDAO.count();
            case "operational": return metricsDAO.count("{'project.website.operational' : {$in: [200, 202]}}");
            case "cmd":
            case "web":
            case "db":
            case "app":
            case "lib":
            case "ontology":
            case "workflow":
            case "plugin":
            case "sparql":
            case "soap":
            case "script":
            case "rest":
            case "workbench":
            case "suite": return toolsDAO.count(String.format("{'_id.type' : '%s'}", field));
        }

        return Long.MIN_VALUE;
    }

    private long getMetricsStatistics(final String type) {
        return metricsDAO.count("{'project.website.operational' : {$in: [200, 202]}, '_id': { $regex: '/" + type + "/'}}");
    }
}
