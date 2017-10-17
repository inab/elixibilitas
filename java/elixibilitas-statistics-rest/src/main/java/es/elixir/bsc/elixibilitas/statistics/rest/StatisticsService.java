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
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
    @Path("/statistics/{field}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getMetrics(@PathParam("field") String field,
                           @Suspended final AsyncResponse asyncResponse) {
            
        executor.submit(() -> {
            asyncResponse.resume(getStatisticsAsync(field).build());
        });
    }
    
    private Response.ResponseBuilder getStatisticsAsync(final String field) {
        
        switch(field) {
            case "total": return Response.ok(ToolDAO.count(mc));
            case "operational": return Response.ok(MetricsDAO.count(mc, "{'project.website.operational' : true}"));
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
            case "suite": return Response.ok(ToolDAO.count(mc, String.format("{'_id.type' : '%s'}", field)));
        }

        return Response.status(Response.Status.BAD_REQUEST);
    }
}
