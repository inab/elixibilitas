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
    
    private final static URI DOI_RESOLVER_URI = URI.create("https://doi.org/");
    private final static URI PMID_RESOLVER_URI = URI.create("https://www.ncbi.nlm.nih.gov/pubmed/");
    private final static URI PMCID_RESOLVER_URI = URI.create("https://www.ncbi.nlm.nih.gov/pmc/articles/");
        
    @Override
    public Boolean check(Tool tool, Metrics metrics) {
        
        List<Publication> publications = tool.getPublications();
        if (publications.isEmpty()) {
            return null;
        }
        
        Integer n = check(publications);
        
        Project project = metrics.getProject();
        if (project != null) {
            project.setPublications(n);
        } else if (n != null) {
            project = new Project();
            project.setPublications(n);
            metrics.setProject(project);
        }

        return n == null ? null : n > 0;
    }
    
    private static Integer check(List<Publication> publications) {

        if (publications.isEmpty()) {
            return null;
        }
        
        int n = 0;

        for (Publication publication : publications) {
            try {
                final String doi = publication.getDOI();
                if (doi != null && checkDOI(doi)) {
                    n++;
                    continue;
                }

                final String pmid = publication.getPMID();
                if (pmid != null && checkPMID(pmid)) {
                    n++;
                    continue;
                }

                final String pmcid = publication.getPMCID();
                if (pmcid != null && checkPMCID(pmcid)) {
                    n++;
                }
            } catch (Exception ex) {}
        }

        return n;
    }
    
    private static boolean checkDOI(String doi) {
        URI uri = DOI_RESOLVER_URI.resolve(doi);
        final int code = ClientBuilder.newClient().target(uri).request(MediaType.WILDCARD).get().getStatus();
        return code != 404;
    }
    
    private static boolean checkPMID(String pmid) {
        URI uri = PMID_RESOLVER_URI.resolve(pmid);
        final int code = ClientBuilder.newClient().target(uri).request(MediaType.WILDCARD).get().getStatus();
        return code != 404;
    }
    
    private static boolean checkPMCID(String pmid) {
        URI uri = PMCID_RESOLVER_URI.resolve(pmid);
        final int code = ClientBuilder.newClient().target(uri).request(MediaType.WILDCARD).get().getStatus();
        return code != 404;
    }
}
