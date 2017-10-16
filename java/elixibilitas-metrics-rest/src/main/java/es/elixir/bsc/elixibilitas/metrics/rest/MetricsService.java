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

package es.elixir.bsc.elixibilitas.metrics.rest;

import com.mongodb.MongoClient;
import es.elixir.bsc.openebench.metrics.dao.MetricsDAO;
import io.swagger.oas.annotations.Operation;
import io.swagger.oas.annotations.Parameter;
import io.swagger.oas.annotations.info.Contact;
import io.swagger.oas.annotations.info.Info;
import io.swagger.oas.annotations.info.License;
import io.swagger.oas.annotations.media.Content;
import io.swagger.oas.annotations.media.Schema;
import io.swagger.oas.annotations.parameters.RequestBody;
import io.swagger.oas.annotations.responses.ApiResponse;
import io.swagger.oas.annotations.servers.Server;
import java.io.OutputStream;
import java.io.StringReader;
import javax.annotation.Resource;
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
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

/**
 * REST Service to operate over Metrics objects.
 * 
 * @author Dmitry Repchevsky
 */

@Info(
    title = "OpenEBench Metrics services",
    version = "0.1",
    description = "OpenEBench Metrics services",
    license = @License(name = "LGPL 2.1", url = "https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html"),
    contact = @Contact(url = "https://elixir.bsc.es")
)
@Path("/")
public class MetricsService {
    
    @Inject
    private MongoClient mc;

    @Resource
    private ManagedExecutorService executor;
    
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
    public Response getMetricsJsonSchema(@Context ServletContext ctx) {
        return Response.ok(ctx.getResourceAsStream("/META-INF/resources/metrics.json")).build();
    }

    @GET
    @Path("/{id}/{type}/{host}{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        servers = {@Server(url = "https://elixir.bsc.es/metrics")},
        description = "Return tools metrics by the tool's id",
        parameters = {
            @Parameter(in = "path", name = "id", description = "prefixed tool id", required = true),
            @Parameter(in = "path", name = "type", description = "tool type", required = true),
            @Parameter(in = "path", name = "host", description = "tool authority", required = true),
            @Parameter(in = "path", name = "path", description = "json pointer", required = false)
        },
        responses = {
            @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                            schema = @Schema(ref="https://elixir.bsc.es/metrics/metrics.json")),
                         description = "Metrics JSON description"
            ),
            @ApiResponse(responseCode = "404", description = "metrics not found")
        }
    )
    public void getMetrics(@PathParam("id") String id,
                           @PathParam("type") String type,
                           @PathParam("host") String host,
                           @PathParam("path") String path,
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
    
    private Response.ResponseBuilder getMetricsAsync(String id, String path) {
        final String json = MetricsDAO.getJSON(mc, id);
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
    
    @PUT
    @Path("/{id : .*}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
        description = "insert the metrics into the database",
        parameters = {
            @Parameter(in = "path", name = "id", description = "prefixed tool id", required = true)
        }
    )
    public void putMetrics(@PathParam("id") final String id, 
                        @RequestBody(description = "json metrics object",
                            content = @Content(schema = @Schema(ref="https://elixir.bsc.es/tool/tool.json")),
                            required = true) final String json,
                        @Suspended final AsyncResponse asyncResponse) {
        executor.submit(() -> {
            asyncResponse.resume(putToolAsync(id, json).build());
        });
    }
    
    private Response.ResponseBuilder putToolAsync(String id, String json) {
        MetricsDAO.put(mc, id, json);
        return Response.ok();
    }

}
