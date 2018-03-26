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

import java.io.IOException;
import java.util.List;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

/**
 * The filter that add "application/json" or "application/ld+json" content to be accepted.
 * For instance some browsers expect "text/html" or "application/xml" what
 * makes impossible to get JSON back from the REST services.
 * 
 * @author Dmitry Repchevsky
 */

@Provider
@PreMatching
public class JSONContentTypeFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext rc) throws IOException {
        final MultivaluedMap<String, String> headers = rc.getHeaders();
        final List<String> list = headers.get("Accept");
        if (list != null) {
            for (String header : list) {
                final String[] ranges = header.split(",");
                for (String range : ranges) {
                    final String[] nodes = range.split(";");
                    if (MediaType.APPLICATION_XML.equals(nodes[0].trim())) {
                        if (nodes.length <= 1) {
                            headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
                        }
                        final String qs = nodes[1].replace(" ", "");
                        if (qs.startsWith("q=")) {
                            try {
                                final Float q = Float.parseFloat(qs.substring(2));
                                headers.add(HttpHeaders.ACCEPT, q > 0.5 ?
                                        MediaType.APPLICATION_JSON :
                                        "application/ld+json");
                                
                            } catch(NumberFormatException ex) {   
                            }
                        }
                        return;
                    } 
                }
            }
        }
    }
}
