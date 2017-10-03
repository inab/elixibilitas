package es.elixir.bsc.openebench.tools.rest;

import com.mongodb.MongoClient;
import es.elixir.bsc.elixibilitas.tools.dao.ToolDAO;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
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

    @GET
    @Path("/tools/biotools.owl")
    @Produces("application/rdf+xml")
    public Response get(@Context ServletContext ctx) {
        return Response.ok(ctx.getResourceAsStream("/META-INF/resources/biotools.owl"), MediaType.TEXT_HTML).build();
    }
    
    /**
     * Get back all tools as a JSON array.
     * 
     * @param asyncResponse 
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public void getTools(@Suspended final AsyncResponse asyncResponse) {
        executor.submit(() -> {
            asyncResponse.resume(getToolsAsync().build());
        });
    }

    private ResponseBuilder getToolsAsync() {
        StreamingOutput stream = (OutputStream out) -> {
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"))) {
                ToolDAO.write(mc, writer);
            }
        };
                
        return Response.ok(stream);
    }

    @GET
    @Path("/{id}{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getTool(@PathParam("path") final String path,
                        @Context final UriInfo uriInfo, 
                        @Suspended final AsyncResponse asyncResponse) {
        final URI uri = uriInfo.getRequestUri();
        executor.submit(() -> {
            if (path == null || path.isEmpty()) {
                asyncResponse.resume(getToolsAsync(uri.toString()).build());
            } else {
                asyncResponse.resume(getToolAsync(uri.toString()).build());
            }
        });
    }

    private ResponseBuilder getToolAsync(String id) {
        final String json = ToolDAO.getJSON(mc, id);
        return json != null ? Response.ok(json, MediaType.APPLICATION_JSON_TYPE) :
                              Response.status(Status.NOT_FOUND);
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
    public void putTool(@PathParam("id") String id, String json,
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
