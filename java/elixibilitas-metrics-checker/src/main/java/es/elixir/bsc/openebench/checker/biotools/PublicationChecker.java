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

import es.bsc.inb.elixir.openebench.model.metrics.Metrics;
import es.bsc.inb.elixir.openebench.model.tools.Publication;
import es.bsc.inb.elixir.openebench.model.tools.Tool;
import es.elixir.bsc.openebench.checker.MetricsChecker;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;


/**
 * @author Dmitry Repchevsky
 */

public class PublicationChecker implements MetricsChecker {
    
    private final static URI DOI_RESOLVER_URI = URI.create("https://doi.org/");
    private final static URI PMID_RESOLVER_URI = URI.create("https://www.ncbi.nlm.nih.gov/pubmed/");
    private final static URI PMCID_RESOLVER_URI = URI.create("https://www.ncbi.nlm.nih.gov/pmc/articles/");
      
    @Override
    public String getToolPath() {
        return "/publications";
    }
    
    @Override
    public String getMetricsPath() {
        return "/project/publications";
    }

    @Override
    public Boolean check(Tool tool, Metrics metrics) {
        
        List<Publication> publications = tool.getPublications();
        if (publications.isEmpty()) {
            return null;
        }
        
//        Integer n = check(publications);
//        
//        Project project = metrics.getProject();
//        if (project != null) {
//            project.setPublications(n);
//        } else if (n != null) {
//            metrics.setProject(project = new Project());
//            project.setPublications(n);
//        }
//
//        return n == null ? null : n > 0;
        return true;
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

                final String pmid = publication.getPmid();
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
        return check(DOI_RESOLVER_URI.resolve(doi));
    }
    
    private static boolean checkPMID(String pmid) {
        return check(PMID_RESOLVER_URI.resolve(pmid));
    }
    
    private static boolean checkPMCID(String pmid) {
        return check(PMCID_RESOLVER_URI.resolve(pmid));
    }
    
    private static boolean check(final URI uri) {
        try {
            HttpURLConnection con = (HttpURLConnection)uri.toURL().openConnection();
            
            con.setReadTimeout(120000);
            con.setConnectTimeout(300000);
            con.setInstanceFollowRedirects(true);
            
            con.addRequestProperty("User-Agent", "Mozilla/5.0 Gecko/20100101 Firefox/54.0");
            return con.getResponseCode() < 300;                        
        } catch (IOException ex) {}
        
        return false;
    }
}
