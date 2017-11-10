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

package es.elixir.bsc.elixibilitas.statistics.rest;

import com.mongodb.MongoClient;
import es.elixir.bsc.elixibilitas.tools.dao.ToolDAO;
import es.elixir.bsc.openebench.metrics.dao.MetricsDAO;
import io.swagger.oas.annotations.Operation;
import io.swagger.oas.annotations.Parameter;
import io.swagger.oas.annotations.media.Content;
import io.swagger.oas.annotations.media.Schema;
import io.swagger.oas.annotations.responses.ApiResponse;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObjectBuilder;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

/**
 * REST Service to get some statistics.
 * 
 * @author Dmitry Repchevsky
 */

@Path("/")
public class StatisticsService {
    
    @Inject
    private MongoClient mc;

    @Resource
    private ManagedExecutorService executor;
    
    /**
     * Proxy method to return Biotoolz ontology.
     * 
     * @param ctx injected servlet context.
     * 
     * @return biotoolz ontology
     */
    @GET
    @Path("/biotools.owl")
    @Produces("application/rdf+xml")
    public Response getMetricsJsonSchema(@Context ServletContext ctx) {
        return Response.ok(ctx.getResourceAsStream("biotools.owl")).build();
    }

    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Returns all tools descriptions.",
        parameters = {
            @Parameter(in = "query", name = "skip", description = "skip n tools", required = false),
            @Parameter(in = "query", name = "limit", description = "return n tools", required = false),
            @Parameter(in = "query", name = "projection", description = "fields to return", required = false),
            @Parameter(in = "query", name = "text", description = "text to search", required = false)
        },

        responses = {
            @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                            schema = @Schema(ref="https://elixir.bsc.es/tool/tool.json")))
        }
    )
    public void search(@QueryParam("skip") final Integer skip,
                       @QueryParam("limit") final Integer limit,
                       @QueryParam("projection") final List<String> projections,
                       @QueryParam("text") final String text,
                              @Suspended final AsyncResponse asyncResponse) {
        executor.submit(() -> {
            asyncResponse.resume(searchAsync(skip, limit, projections, text).build());
        });
    }
    
    private Response.ResponseBuilder searchAsync(final Integer skip, 
                              final Integer limit, 
                              final List<String> projections, 
                              final String text) {

        StreamingOutput stream = (OutputStream out) -> {
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"))) {
                ToolDAO.write(mc, writer, skip, limit, text, projections);
            }
        };
                
        return Response.ok(stream);
    }

    @GET
    @Path("/metrics/log/{id}/{type}/{host}{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getMetricsLog(@PathParam("id") String id,
                           @PathParam("type") String type,
                           @PathParam("host") String host,
                           @PathParam("path") String path,
                              @Suspended final AsyncResponse asyncResponse) {
        if (path == null || path.isEmpty()) {
            asyncResponse.resume(Response.status(Response.Status.BAD_REQUEST).build());
        }
        executor.submit(() -> {
            asyncResponse.resume(getMetricsLogAsync(id + "/" + type + "/" + host, path).build());
        });
    }

    private Response.ResponseBuilder getMetricsLogAsync(String id, String field) {
        final JsonArray array = MetricsDAO.findLog(mc, id, field);
        if (array == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR);
        }
        if (array.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND);
        }
        
        return Response.ok(array);
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
            case "total": return ToolDAO.count(mc);
            case "operational": return MetricsDAO.count(mc, "{'project.website.operational' : {$in: [200, 202]}}");
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
            case "suite": return ToolDAO.count(mc, String.format("{'_id.type' : '%s'}", field));
        }

        return Long.MIN_VALUE;
    }

    private long getMetricsStatistics(final String type) {
        return MetricsDAO.count(mc, "{'project.website.operational' : {$in: [200, 202]}, '_id': { $regex: '/" + type + "/'}}");
    }
}
