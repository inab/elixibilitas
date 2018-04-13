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

import io.swagger.v3.oas.annotations.Hidden;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 *
 * @author Dmitry Repchevsky
 */

@Path("/")
@ApplicationScoped
public class WebProxyService {
    
    @Inject 
    private ServletContext ctx;

    /**
     * Proxy method to return Tool JSON Schema.
     * 
     * @return JSON Schema for the Tool
     */
    @GET
    @Path("/tools.owl")
    @Hidden
    public Response getToolsOntology() {
        return Response.ok(ctx.getResourceAsStream("/META-INF/resources/tools.owl"), "application/rdf+xml").build();
    }
    
//    @GET
//    @Path("/index.html")
//    @Hidden
//    public Response getHomePage() {
//        return Response.ok(ctx.getResourceAsStream("/META-INF/resources/index.html"), "text/html").build();
//    }
}
