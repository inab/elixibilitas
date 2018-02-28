package es.elixir.bsc.openebench.galaxy;

import es.elixir.bsc.biotools.parser.model.ToolType;
import es.elixir.bsc.openebench.bioconda.BiocondaPackage;
import static es.elixir.bsc.openebench.galaxy.Test.findByCitation;
import es.elixir.bsc.openebench.model.tools.Tool;
import es.elixir.bsc.openebench.tools.OpenEBenchRepository;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Repchevsky
 */

public class GalaxyRepositoryImporter {
    
    private final static String ID_TEMPLATE = OpenEBenchRepository.URI_BASE + "galaxy:%s:%s/%s/%s";
            
    public static void main(String[] args) throws IOException {
        List<GalaxyTool> galaxy_tools = new ArrayList<>();
        
        //try (GalaxyToolsIterator iter = new GalaxyToolsIterator(URI.create("https://usegalaxy.org/"))) {
        try (GalaxyToolsIterator iter = new GalaxyToolsIterator(URI.create("https://galaxy.bi.uni-freiburg.de/"))) {
            while(iter.hasNext()) {
                final GalaxyTool galaxy_tool = iter.next();
                if (galaxy_tool != null) {
                    galaxy_tools.add(galaxy_tool);
                }
            }
        }
        
        for (GalaxyTool galaxy_tool : galaxy_tools) {
            System.out.println("=> " + galaxy_tool.id);
            
            final List<BiocondaPackage> requirements = galaxy_tool.getRequirements();

            
            final String name = galaxy_tool.getName().replace(' ', '_');
            final String version = galaxy_tool.getVersion();
            final String type = "workflow";
            
            final String[] tokens = galaxy_tool.id.split("/");
            
            final String authority = tokens.length > 0 ? tokens[0] : galaxy_tool.getRepository();

            Tool tool;
            if (requirements.size() != 1) {
                tool = new Tool(URI.create(String.format(ID_TEMPLATE, name, version, type, authority)), type);
            } else {
                final BiocondaPackage pack = requirements.get(0);
                String id = find(pack);
                if (id == null) {
                    id = name;
                }
                tool = new Tool(URI.create(String.format(ID_TEMPLATE, id, version, type, authority)), type);                
                
                //BiocondaPackage.Metadata metadata = pack.getMetadata();
                //metadata.
                
                
            }
            tool.setDescription(galaxy_tool.description);

            System.out.println("-> " + tool.id.toString());
            for (BiocondaPackage pack : requirements) {
                System.out.println("    dependency: " + pack);
            }
            System.out.println("   " + tool.getDescription());
//            for (String cite : galaxy_tool.getCitations()) {
//                for (Tool t : findByCitation(cite)) {
//                    System.out.println("    -> by citation: " + t.id);
//                }
//            }
        }
    }
    
    private static String find(BiocondaPackage pack) {
        if (pack.name != null && pack.name.startsWith("bioconductor-")) {
            final String name = pack.name.substring("bioconductor-".length());
            
            for (Tool t : OpenEBenchRepository.getTools().values()) {
                final String uri = t.id.getPath();
                
                if (uri.endsWith("bioconductor.org")) {
                    final int idx = uri.indexOf("bio.tools:" + name + ":");
                    if (idx > 0) {
//                        System.out.print("    -> by bioconductor: " + pack.name + ":" + pack.version);
//                        System.out.println("    " + t.id);
                        return t.getName();
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
                        return t.getName();
                    }
                }
            }
        }
        
        return null;
    }
}
