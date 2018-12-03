package es.elixir.bsc.openebench.bioconda;

import es.elixir.bsc.openebench.tools.OpenEBenchEndpoint;
import es.elixir.bsc.openebench.bioconda.BiocondaPackage.Metadata;
import es.elixir.bsc.openebench.model.tools.Distributions;
import es.elixir.bsc.openebench.model.tools.Publication;
import es.elixir.bsc.openebench.model.tools.Tool;
import es.elixir.bsc.openebench.model.tools.Web;
import es.elixir.bsc.openebench.tools.OpenEBenchRepository;
import es.elixir.bsc.openebench.tools.ToolsComparator;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Dmitry Repchevsky
 */

public class BiocondaRepositoryImporter {
    
    public final static String ID_TEMPLATE = OpenEBenchEndpoint.URI_BASE + "bioconda:%s:%s/%s/%s";
    
    private final Map<String, String> dois = OpenEBenchRepository.getDOIPublications();
    
    private OpenEBenchRepository repository;

    public BiocondaRepositoryImporter() {}

    public BiocondaRepositoryImporter(String username, String password) {
        repository = new OpenEBenchRepository(username, password);
    }
    
    public void load() {

        final ExecutorService executor = Executors.newFixedThreadPool(32);
                
        try {
            final Collection<BiocondaPackage> packages = BiocondaRepository.getPackages();
            final CountDownLatch latch = new CountDownLatch(packages.size());

            for (BiocondaPackage pack : packages) {
                executor.execute(() -> {
                    try {
                        Tool tool = find(pack);
                        if (tool == null) {
                            tool = create(pack, null);
                            if (tool != null) {
                                String id = tool.id.toString();
                                if (id.startsWith(OpenEBenchEndpoint.URI_BASE)) {
                                    final String[] nodes = id.substring(OpenEBenchEndpoint.URI_BASE.length()).split("/")[0].split(":");
                                    id = nodes.length == 1 ? nodes[0] : nodes[1];
                                    
                                    if (repository != null) {
                                        // insert 'common' tool
                                        final Tool common = new Tool(URI.create(OpenEBenchEndpoint.URI_BASE + id), null);
                                        common.setName(tool.getName());
                                        common.setDescription(tool.getDescription());
                                        common.setWeb(tool.getWeb());
                                        repository.patch(common);
                                    }
                                }
                            }
                        }
                        if (tool != null) {
                            checkDOIs(tool);

                            System.out.println("> PUT: " + tool.id.toString());
                            if (repository != null) {
                                repository.patch(tool);
                            } else {
                                System.out.println("    name: " + tool.getName());
                                Web web = tool.getWeb();
                                if (web != null) {
                                    System.out.println("    homepage: " + web.getHomepage());
                                }
                                System.out.println("    description: " + tool.getDescription());
//                                final Jsonb jsonb = JsonbBuilder.create(
//                                        new JsonbConfig().withPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE)
//                                        .withFormatting(true));
//                                System.out.println(jsonb.toJson(tool));
                            }
                            
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(BiocondaRepositoryImporter.class.getName()).log(Level.SEVERE, pack.toString(), ex);
                    }
                    latch.countDown();
                });
            }

            latch.await();
        } catch (InterruptedException ex) {
            Logger.getLogger(BiocondaRepositoryImporter.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            executor.shutdown();
        }
    }
    
    private void checkDOIs(final Tool tool) {
        final List<Publication> publications = tool.getPublications();
        if (!publications.isEmpty()) {
            String id = tool.id.toString();
            if (id.startsWith(OpenEBenchEndpoint.URI_BASE)) {
                final String[] nodes = id.substring(OpenEBenchEndpoint.URI_BASE.length()).split("/")[0].split(":");
                id = nodes.length == 1 ? nodes[0] : nodes[1];
                for (Publication publication : publications) {
                    final String doi = publication.getDOI();
                    if (doi != null) {
                        final String _id = dois.get(doi);
                        if (!id.equals(_id)) {
                            System.out.println(String.format(">openebench warning: %s vs %s, %s", id, _id, doi));
                        }
                    }
                }
            }
        }
    }
    
    public static Tool find(final BiocondaPackage pack) throws IOException {
        
        String id = search(pack);
        if (id == null) {
            final Tool tmp = create(pack, null);
            if (tmp != null) {
                double score = 0;
                for (Tool t : OpenEBenchRepository.getTools().values()) {
                    if (t.id.toString().substring(OpenEBenchEndpoint.URI_BASE.length()).startsWith("biotools:")) {
                        final double s = ToolsComparator.compare(tmp, t);
                        if (s > score) {
                            score = s;
                            id = t.id.toString();
                        }
                    }
                }
                if (score < 0.5) {
                    return null;
                }
            }
            if (id == null) {
                return null;
            }
        }
        
        if (id.startsWith(OpenEBenchEndpoint.URI_BASE)) {
            final String[] nodes = id.substring(OpenEBenchEndpoint.URI_BASE.length()).split("/");
            if (nodes.length > 0) {
                final String[] _id = nodes[0].split(":");
                final Tool tool = create(pack, _id.length > 1 ? _id[1] : _id[0]);
                if (tool != null) {
                    tool.setExternalId(pack.name + ":" + pack.version);
                    return tool;
                }
            }
        }
        return null;
    }
    
    /*
     *  Searches the bio.tools Tool for the bioconda package
     */
    private static String search(final BiocondaPackage pack) {
        
        if (pack.name != null && pack.name.startsWith("bioconductor-")) {
            final String name = pack.name.substring("bioconductor-".length());
            
            for (Tool t : OpenEBenchRepository.getTools().values()) {
                final String uri = t.id.getPath();

                if (uri.endsWith("bioconductor.org")) {
                    final int idx = uri.indexOf("biotools:" + name + ":");
                    if (idx > 0) {
                        return t.id.toString();
                    }
                }
            }
        }
        
        if (pack.name != null && pack.name.startsWith("r-")) {
            final String name = pack.name.substring("r-".length());
            
            for (Tool t : OpenEBenchRepository.getTools().values()) {
                final String uri = t.id.getPath();
                
                if (uri.endsWith("cran.r-project.org")) {
                    final int idx = uri.indexOf("biotools:" + name + ":");
                    if (idx > 0) {
                        return t.id.toString();
                    }
                }
            }
        }
        
        return null;
    }

    /*
     * Creates the bio.tools Tool from the bioconda package and the provided id.
     */    
    private static Tool create(BiocondaPackage pack, String id) throws IOException {

        final Metadata metadata = pack.getMetadata();
        if (metadata == null) {
            return null;
        }
        
        URI homepage = null;

        if (metadata.home != null && !metadata.home.isEmpty()) {
            try {
                homepage = URI.create(metadata.home);
            } catch(IllegalArgumentException ex) {}
        }
        if (homepage == null) {
            try {
                homepage = URI.create(BiocondaRepository.SERVER);
            } catch(IllegalArgumentException ex) {}
        }

        final String authority = homepage != null ? homepage.getHost() : "";

        String _id = id != null ? id : pack.name.toLowerCase();
        String doi = null;
        if (metadata.identifiers != null && metadata.identifiers.length > 0) {
            for (String identifier : metadata.identifiers) {
                if (identifier != null) {
                    if (id == null && identifier.startsWith("biotools:")) {
                        _id = identifier.substring("biotools:".length()).toLowerCase();
                    } else if (identifier.startsWith("doi:")) {
                        doi = identifier.substring("doi:".length());
                    }
                }
            }
        }
        
        Tool tool = new Tool(URI.create(String.format(ID_TEMPLATE, _id, pack.version, "cmd", authority)), "cmd");

        if (!_id.equals(pack.name)) {
            tool.setExternalId(pack.version == null || pack.version.isEmpty() ? pack.name : pack.name + ":" + pack.version);
        }
        
        if (doi != null) {
            boolean hasPublication = false;
            for (Publication publication : tool.getPublications()) {
                if (doi.equals(publication.getDOI())) {
                    hasPublication = true;
                    break;
                }
            }
            if (!hasPublication) {
                Publication publication = new Publication();
                publication.setDOI(doi);
                tool.getPublications().add(publication);
            }
        }
        
        tool.setName(pack.name);
        if (homepage != null) {
            Web web = new Web();
            web.setHomepage(homepage);
            tool.setWeb(web);
        }

        tool.setLicense(metadata.license);
        tool.setDescription(metadata.summary);

        // set repository
        if (metadata.git != null && !metadata.git.isEmpty()) {
            try {
                final URI repository = URI.create(metadata.git);
                tool.getRepositories().add(repository);
            } catch(IllegalArgumentException ex) {}
        }

        if (metadata.src_urls != null && metadata.src_urls.length > 0) {
            for (String src_url : metadata.src_urls) {
                try {
                    final URI source = URI.create(src_url);
                    Distributions distributions = tool.getDistributions();
                    if (distributions == null) {
                        tool.setDistributions(distributions = new Distributions());
                    }
                    distributions.getSourcecodeDistributions().add(source);

                } catch(IllegalArgumentException ex) {}

            }
        }
        if (pack.file != null && !pack.file.isEmpty()) {
            try {
                final URI conda = URI.create(pack.toString());
                Distributions distributions = tool.getDistributions();
                if (distributions == null) {
                    tool.setDistributions(distributions = new Distributions());
                }
                distributions.getBinaryPackagesDistributions().add(conda);
            } catch(IllegalArgumentException ex) {}
        }        

        return tool;
    }

}
