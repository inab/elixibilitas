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
    @Path("/metrics/log/{id : .*}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getMetricsLog(@PathParam("id") final String id,
                              @QueryParam("field") final String field,
                              @Suspended final AsyncResponse asyncResponse) {
        if (id == null || id.isEmpty() ||
            field == null || field.isEmpty()) {
            asyncResponse.resume(Response.status(Response.Status.BAD_REQUEST).build());
        }
        executor.submit(() -> {
            asyncResponse.resume(getMetricsLogAsync(id, field).build());
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
        builder.add("total", getStatistics("total"));
        builder.add("operational", getStatistics("operational"));
        builder.add("cmd", getStatistics("cmd"));
        builder.add("web", getStatistics("web"));
        builder.add("db", getStatistics("db"));
        builder.add("app", getStatistics("app"));
        builder.add("lib", getStatistics("lib"));
        builder.add("ontology", getStatistics("ontology"));
        builder.add("workflow", getStatistics("workflow"));
        builder.add("plugin", getStatistics("plugin"));
        builder.add("sparql", getStatistics("sparql"));
        builder.add("soap", getStatistics("soap"));
        builder.add("script", getStatistics("script"));
        builder.add("rest", getStatistics("rest"));
        builder.add("workbench", getStatistics("workbench"));
        builder.add("suite", getStatistics("suite"));
        
        builder.add("cmd.operational", getMetricsStatistics("cmd"));
        builder.add("web.operational", getMetricsStatistics("web"));
        builder.add("db.operational", getMetricsStatistics("db"));
        builder.add("app.operational", getMetricsStatistics("app"));
        builder.add("lib.operational", getMetricsStatistics("lib"));
        builder.add("ontology.operational", getMetricsStatistics("ontology"));
        builder.add("workflow.operational", getMetricsStatistics("workflow"));
        builder.add("plugin.operational", getMetricsStatistics("plugin"));
        builder.add("sparql.operational", getMetricsStatistics("sparql"));
        builder.add("soap.operational", getMetricsStatistics("soap"));
        builder.add("script.operational", getMetricsStatistics("script"));
        builder.add("rest.operational", getMetricsStatistics("rest"));
        builder.add("workbench.operational", getMetricsStatistics("workbench"));
        builder.add("suite.operational", getMetricsStatistics("suite"));

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
    
    private long getStatistics(final String field) {
        switch(field) {
            case "total": return ToolDAO.count(mc);
            case "operational": return MetricsDAO.count(mc, "{'project.website.operational' : true}");
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
        return MetricsDAO.count(mc, "{'project.website.operational' : true, '_id': { $regex: '/" + type + "/'}}");
    }
}
