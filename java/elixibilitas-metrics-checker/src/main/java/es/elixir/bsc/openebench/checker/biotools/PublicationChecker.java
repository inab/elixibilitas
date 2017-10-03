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

package es.elixir.bsc.openebench.checker.biotools;

import es.elixir.bsc.elixibilitas.model.metrics.Metrics;
import es.elixir.bsc.elixibilitas.model.metrics.Project;
import es.elixir.bsc.openebench.model.tools.Publication;
import es.elixir.bsc.openebench.model.tools.Tool;
import es.elixir.bsc.openebench.checker.MetricsChecker;
import java.net.URI;
import java.util.List;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;

/**
 * @author Dmitry Repchevsky
 */

public class PublicationChecker implements MetricsChecker {
    
    private final static URI doiResolverURI = URI.create("https://doi.org/");
    private final static URI pmidResolverURI = URI.create("https://www.ncbi.nlm.nih.gov/pubmed/");
    private final static URI pmcidResolverURI = URI.create("https://www.ncbi.nlm.nih.gov/pmc/articles/");
        
    @Override
    public Boolean check(Tool tool, Metrics metrics) {
        
        List<Publication> publications = tool.getPublications();
        if (publications.isEmpty()) {
            return null;
        }
        
        Boolean bool = check(publications);
        
        Project project = metrics.getProject();
        if (project != null) {
            project.setPublication(bool);
        } else if (bool != null) {
            project = new Project();
            project.setPublication(bool);
            metrics.setProject(project);
        }

        return bool;
    }
    
    private static Boolean check(List<Publication> publications) {

        if (publications.isEmpty()) {
            return null;
        }
        
        try {
            for (Publication publication : publications) {
                final String doi = publication.getDOI();
                if (doi != null && checkDOI(doi)) {
                    return true;
                }

                final String pmid = publication.getPMID();
                if (pmid != null && checkPMID(pmid)) {
                    return true;
                }
                
                final String pmcid = publication.getPMCID();
                if (pmcid != null && checkPMCID(pmcid)) {
                    return true;
                }
            }
        } catch (Exception ex) {
            return null;
        }
        return false;
    }
    
    private static boolean checkDOI(String doi) {
        URI uri = doiResolverURI.resolve(doi);
        final int code = ClientBuilder.newClient().target(uri).request(MediaType.WILDCARD).get().getStatus();
        return code != 404;
    }
    
    private static boolean checkPMID(String pmid) {
        URI uri = pmidResolverURI.resolve(pmid);
        final int code = ClientBuilder.newClient().target(uri).request(MediaType.WILDCARD).get().getStatus();
        return code != 404;
    }
    
    private static boolean checkPMCID(String pmid) {
        URI uri = pmcidResolverURI.resolve(pmid);
        final int code = ClientBuilder.newClient().target(uri).request(MediaType.WILDCARD).get().getStatus();
        return code != 404;
    }
}
