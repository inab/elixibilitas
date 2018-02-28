package es.elixir.bsc.openebench.galaxy;

import es.elixir.bsc.openebench.bioconda.BiocondaPackage;
import es.elixir.bsc.openebench.bioconda.BiocondaPackage.Metadata;
import es.elixir.bsc.openebench.model.tools.Distributions;
import es.elixir.bsc.openebench.tools.OpenEBenchRepository;
import es.elixir.bsc.openebench.model.tools.Publication;
import es.elixir.bsc.openebench.model.tools.Tool;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author redmitry
 */
public class Test {

    public static void main(String[] args) throws IOException {
        
        List<GalaxyTool> tools = new ArrayList<>();
        
        //try (GalaxyToolsIterator iter = new GalaxyToolsIterator(URI.create("https://usegalaxy.org/"))) {
        try (GalaxyToolsIterator iter = new GalaxyToolsIterator(URI.create("https://galaxy.bi.uni-freiburg.de/"))) {
            while(iter.hasNext()) {
                final GalaxyTool tool = iter.next();
                if (tool != null) {
                    tools.add(tool);
                }
            }
        }
        
        for (GalaxyTool tool : tools) {
            System.out.println("=> " + tool.id);
            final List<BiocondaPackage> requirements = tool.getRequirements();
            for (BiocondaPackage pack : requirements) {
                System.out.println("    dependency: " + pack);
            }
            for (BiocondaPackage pack : requirements) {
                find(pack);
            }
            for (String cite : tool.getCitations()) {
                for (Tool t : findByCitation(cite)) {
                    System.out.println("    -> by citation: " + t.id);
                }
            }
        }
    }
    
    public static List<Tool> findByCitation(String citation) {
        
        final List<Tool> tools = new ArrayList<>();
        for (Tool tool : OpenEBenchRepository.getTools().values()) {
            for (Publication publication : tool.getPublications()) {
                final String doi = publication.getDOI();
                if (doi != null && doi.equals(citation)) {
                    tools.add(tool);
                }
            }
        }
        
        return tools;
    }
    
    private static Tool find(final BiocondaPackage pack) throws IOException {
        
        if (pack.name != null && pack.name.startsWith("bioconductor-")) {
            final String name = pack.name.substring("bioconductor-".length());
            
            for (Tool t : OpenEBenchRepository.getTools().values()) {
                final String uri = t.id.getPath();
                
                if (uri.endsWith("bioconductor.org")) {
                    final int idx = uri.indexOf("bio.tools:" + name + ":");
                    if (idx > 0) {
                        System.out.print("    -> by bioconductor: " + pack.name + ":" + pack.version);
                        System.out.println("    " + t.id);
                        return t;
                    }
                }
            }
        }
        
        if (pack.name != null && pack.name.startsWith("r-")) {
            final String name = pack.name.substring("r-".length());
            
            for (Tool t : OpenEBenchRepository.getTools().values()) {
                final String uri = t.id.getPath();
                
                if (uri.endsWith("cran.r-project.org")) {
                    final int idx = uri.indexOf("bio.tools:" + name + ":");
                    if (idx > 0) {
                        System.out.print("    -> by r-project: " + pack.name + ":" + pack.version);
                        System.out.println("    " + t.id);
                        return t;
                    }
                }
            }
        }

        final Metadata meta = pack.getMetadata();
        
        if (meta != null && meta.home != null && meta.home.length() > 0) {
            final Tool tool = OpenEBenchRepository.getByHomepage(meta.home);
            if (tool != null) {
                System.out.print("    -> by home: " + pack.name + ":" + pack.version);
                System.out.println("    " + tool.id);
                return tool;
            }
        }
        if (meta != null && meta.summary != null && meta.summary.length() > 0) {
            for (Tool t : OpenEBenchRepository.getTools().values()) {
                if (meta.summary.equals(t.getDescription())) {
                    System.out.print("    -> by description: " + pack.name + ":" + pack.version);
                    System.out.println("    " + t.id);
                    return t; 
                }
            }
            Tool tool = null;
            float score = 0;
            for (Tool t : OpenEBenchRepository.getTools().values()) {
                final float sc = pack.cmpSummary(t.getDescription());
                if (sc > score) {
                    score = sc;
                    tool = t;
                }
            }
            if (tool != null && score > 0.5 ) {
                System.out.print("    -> by description score: " + score + " " + pack.name + ":" + pack.version);
                System.out.println("    " + tool.id);
                return tool;
            }
        }
        if (meta != null && meta.git != null && meta.git.length() > 0) {
            final Tool tool = OpenEBenchRepository.getByHomepage(meta.git);
            if (tool != null) {
                System.out.print("    -> " + pack.name + ":" + pack.version);
                System.out.println("    " + tool.id);
                return tool;
            }
            for (Tool t : OpenEBenchRepository.getTools().values()) {
                for (URI uri : t.getRepositories()) {
                    if (meta.git.equals(uri.toString())) {
                        System.out.print("    -> by git: " + pack.name + ":" + pack.version);
                        System.out.println("    " + tool.id);
                        return t;
                    }
                }
            }
        }

        if (meta != null && meta.src_urls != null && meta.src_urls.length > 0) {
            for (Tool t : OpenEBenchRepository.getTools().values()) {
                // check binary distributions
                final Distributions distributions = t.getDistributions();
                if (distributions != null) {
                    for (URI distribution : distributions.getSourcecodeDistributions()) {
                        for (int i = 0; i < meta.src_urls.length; i++) {
                            final String uri = meta.src_urls[i];
                            if (distribution.toString().equals(uri)) {
                                System.out.print("    -> by sources:" + pack.name + ":" + pack.version);
                                System.out.println("    " + t.id);
                                return t;
                            }
                        }
                    }

                    // check binary packages distributions
                    for (URI distribution : distributions.getSourcePackagesDistributions()) {
                        for (int i = 0; i < meta.src_urls.length; i++) {
                            final String uri = meta.src_urls[i];
                            if (distribution.toString().equals(uri)) {
                                System.out.print("    -> by packages: " + pack.name + ":" + pack.version);
                                System.out.println("    " + t.id);
                                return t;
                            }
                        }
                    }
                }
            }
        }

        for (Tool t : OpenEBenchRepository.getTools().values()) {
            if (pack.name.equalsIgnoreCase(t.getName())) {
                System.out.print("    -> by id: " + pack.name + ":" + pack.version);
                System.out.println("    " + t.id);
                return t;
            }
        }
        
        return null;
    }

}
