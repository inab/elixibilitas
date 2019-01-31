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
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import java.io.BufferedWriter;
import java.io.CharArrayWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 * REST Service to get some statistics.
 * 
 * @author Dmitry Repchevsky
 */

@OpenAPIDefinition(info = @Info(title = "OpenEBench Statistics API services", 
                                version = "0.1", 
                                description = "OpenEBench Statistics API services",
                                license = @License(name = "LGPL 2.1", 
                                            url = "https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html"),
                                contact = @Contact(url = "https://openebench.bsc.es")
                                ),
                    //security = @SecurityRequirement(name = "openid-connect"), 
                    servers = {@Server(url = "https://openebench.bsc.es/")})
@Path("/rest/")
@ApplicationScoped
public class StatisticsServices {
    
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
    
    private volatile long tools_uptime;
    private volatile long metrics_uptime;

    private CharArrayWriter tools_stat;
    private CharArrayWriter metrics_stat;
    
    @PostConstruct
    public void init() {
        
        final String toolsBaseURI = UriBuilder.fromUri(uriInfo.getBaseUri()).path(ToolsServices.class).build().toString();
        final String metricsBaseURI = UriBuilder.fromUri(uriInfo.getBaseUri()).path(MetricsServices.class).build().toString();
        
        final MongoClientURI mongodbURI = new MongoClientURI(ctx.getInitParameter("mongodb.url"));
        
        toolsDAO = new ToolsDAO(mc.getDatabase(mongodbURI.getDatabase()), toolsBaseURI);
        metricsDAO = new MetricsDAO(mc.getDatabase(mongodbURI.getDatabase()), metricsBaseURI);
    }
    
    @GET
    @Path("/metrics/statistics/")
    @Produces(MediaType.APPLICATION_JSON)
    public void getMetricsStatistics(
            @HeaderParam("Cache-Control") final String cache,
            @Suspended final AsyncResponse asyncResponse) {
        executor.submit(() -> {
            asyncResponse.resume(getMetricsStatisticsAsync(cache).build());
        });
    }
    
    private Response.ResponseBuilder getMetricsStatisticsAsync(final String cache) {
        StreamingOutput stream = (OutputStream out) -> {
            if (metrics_stat == null || "no-cache".equalsIgnoreCase(cache) || System.currentTimeMillis() - metrics_uptime > 86400000) {
                final CharArrayWriter stat = metricsDAO.statistics(new CharArrayWriter());
                if (stat != null) {
                    metrics_stat = stat;
                    metrics_uptime = System.currentTimeMillis();
                } else if (metrics_stat == null) {
                    return;
                }
            }
            
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"))) {
                metrics_stat.writeTo(writer);
            }
        };
        return Response.ok(stream);
    }
    
    @GET
    @Path("/tools/statistics/")
    @Produces(MediaType.APPLICATION_JSON)
    public void getToolsStatistics(
            @HeaderParam("Cache-Control") final String cache,
            @Suspended final AsyncResponse asyncResponse) {
        executor.submit(() -> {
            asyncResponse.resume(getToolsStatisticsAsync(cache).build());
        });
    }
    
    private Response.ResponseBuilder getToolsStatisticsAsync(final String cache) {
        StreamingOutput stream = (OutputStream out) -> {
            if (tools_stat == null || "no-cache".equalsIgnoreCase(cache) || System.currentTimeMillis() - tools_uptime > 86400000) {
                final CharArrayWriter stat = toolsDAO.statistics(new CharArrayWriter());
                if (stat != null) {
                    tools_stat = stat;
                    tools_uptime = System.currentTimeMillis();
                } else if (tools_stat == null) {
                    return;
                }
            }
            
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"))) {
                tools_stat.writeTo(writer);
            }
        };
        return Response.ok(stream);
    }
}
