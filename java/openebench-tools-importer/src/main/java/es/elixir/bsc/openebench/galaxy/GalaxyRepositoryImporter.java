package es.elixir.bsc.openebench.galaxy;

import es.elixir.bsc.openebench.bioconda.BiocondaPackage;
import es.elixir.bsc.openebench.model.tools.Publication;
import es.elixir.bsc.openebench.model.tools.Tool;
import es.elixir.bsc.openebench.tools.OpenEBenchEndpoint;
import es.elixir.bsc.openebench.tools.OpenEBenchRepository;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The class to import Galaxy tools into the OpenEBench repository.
 * 
 * @author Dmitry Repchevsky
 */

public class GalaxyRepositoryImporter {
    
    private final static String ID_TEMPLATE = OpenEBenchEndpoint.URI_BASE + "galaxy:%s:%s/%s/%s";
            
    private final URI server;
    private OpenEBenchRepository repository;
    
    public GalaxyRepositoryImporter(URI server) {
        this.server = server;
    }
    
    public GalaxyRepositoryImporter(URI server, String username, String password) {
        this.server = server;
        this.repository = new OpenEBenchRepository(username, password);
    }
    
    public void load() {

        List<GalaxyTool> galaxy_tools = new ArrayList<>();
        
        try (GalaxyToolsIterator iter = new GalaxyToolsIterator(server)) {
            while(iter.hasNext()) {
                final GalaxyTool galaxy_tool = iter.next();
                if (galaxy_tool != null) {
                    galaxy_tools.add(galaxy_tool);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(GalaxyRepositoryImporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        for (GalaxyTool galaxy_tool : galaxy_tools) {
            final List<BiocondaPackage> requirements = galaxy_tool.getRequirements();
            
            final String name = galaxy_tool.getName().replace(' ', '_');
            final String version = galaxy_tool.getVersion();
            final String type = "workflow";
            
            final String[] tokens = galaxy_tool.id.split("/");
            
            final String authority = tokens.length > 0 ? tokens[0] : galaxy_tool.getRepository();

            Tool tool = null;
            if (requirements.size() != 1) {
//                System.out.println("=> " + galaxy_tool.id);
                tool = new Tool(URI.create(String.format(ID_TEMPLATE, name, version, type, authority)), type);
            } else {
                final BiocondaPackage pack = requirements.get(0);
                
                final String id = pack.name + ":" + pack.version;
                
                for (Tool t : OpenEBenchRepository.getTools().values()) {
                    if (id.equals(t.getExternalId())) {
                        final String[] nodes = t.id.toString().substring(OpenEBenchEndpoint.URI_BASE.length()).split("/");
                            if (nodes.length > 0) {
                                //System.out.println("-> " + galaxy_tool.id);
                                final String[] _id = nodes[0].split(":");
                                tool = new Tool(URI.create(String.format(ID_TEMPLATE, _id.length > 1 ? _id[1] : _id[0], version, type, authority)), type);
                                tool.setExternalId(galaxy_tool.id);
                            }
                        break;
                    }
                }
                if(tool == null) {
                    //System.out.println("=> " + galaxy_tool.id);
                    //tool = new Tool(URI.create(String.format(ID_TEMPLATE, pack.name, pack.version, type, authority)), type);
                    tool = new Tool(URI.create(String.format(ID_TEMPLATE, name, version, type, authority)), type);
                }
                

                try {
                    BiocondaPackage.Metadata metadata = pack.getMetadata();
                    if (metadata != null && metadata.license != null && !metadata.license.isEmpty()) {
                        tool.setLicense(metadata.license);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(GalaxyRepositoryImporter.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            System.out.println("> PUT: " + tool.id);
            
            tool.setName(name);
            tool.setDescription(galaxy_tool.description);
            
            final String link = galaxy_tool.getLink();
            if (link != null) {
                tool.setHomepage(server.resolve(link));
            }
            
            for (String citation : galaxy_tool.getCitations()) {
                if (citation.indexOf('\n') < 0) {
                    final Publication publication = new Publication();
//                    if (repository == null) {
//                        System.out.println("     citation: " + citation);
//                    }
                    publication.setDOI(citation);
                    tool.getPublications().add(publication);
                }
            }
            
            
            if (repository == null) {
//                for (BiocondaPackage pack : requirements) {
//                    System.out.println("     dependency: " + pack);
//                }
//                System.out.println("     description: '" + tool.getDescription() + "'");
            }

            if (repository != null) {
                try {
                    repository.put(tool);
                } catch (IOException ex) {
                    Logger.getLogger(GalaxyRepositoryImporter.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
