package es.elixir.bsc.openebench;

import es.elixir.bsc.openebench.bioconda.BiocondaRepositoryImporter;
import es.elixir.bsc.openebench.biotools.BiotoolsRepositoryImporter;
import es.elixir.bsc.openebench.galaxy.GalaxyRepositoryImporter;
import es.elixir.bsc.openebench.openminted.OMTDRepositoryImporter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A common tool to import tools from different repositories
 * 
 * @author Dmitry Repchevsky
 */

public class OpenEBenchToolsImporter {
    
    private final static String HELP = 
            "import-tools (-biotools || -bioconda || -galaxy) [-user && -password]\n\n" +
            "parameters:\n\n" +
            "-h (--help)                - this help message\n" +
            "-biotools                  - import tools from biotools repository\n" +
            "-bioconda                  - import tools from bioconda repository\n" +
            "-galaxy 'server'           - import tools from galaxy server repository\n" +
            "-u (--user) 'username'     - OpenEBench username\n" +
            "-p (--password) 'password' - OpenEBench pasword\n\n" +
            "comment: in the absense of credentials the tool only simulates the activity.\n" +
            "example: >java -jar import-tools.jar -biotools\n";
    
    public static void main(String[] args) {
        Map<String, List<String>> params = parameters(args);
        
        if (params.isEmpty() || 
            params.get("-h") != null ||
            params.get("--help") != null) {
            System.out.println(HELP);
            System.exit(0);
        }
        
        List<String> user = params.get("-u");
        if (user == null) {
            user = params.get("--user");
        }

        List<String> password = params.get("-p");
        if (password == null) {
            password = params.get("--password");
        }

        final String u = user == null || user.isEmpty() ? null : user.get(0);
        final String p = password == null || password.isEmpty() ? null : password.get(0);

        if (params.get("-biotools") != null) {
            
            if (u == null || u.isEmpty() || p == null || p.isEmpty()) {
                new BiotoolsRepositoryImporter().load();
            } else {
                new BiotoolsRepositoryImporter(u, p).load();
            }
        } else if (params.get("-bioconda") != null) {
            if (u == null || u.isEmpty() || p == null || p.isEmpty()) {
                new BiocondaRepositoryImporter().load();
            } else {
                new BiocondaRepositoryImporter(u, p).load();
            }
        } else if (params.get("-omtd") != null) {
            final URI server = URI.create("https://test.openminted.eu/api/request/application/all");
            if (u == null || u.isEmpty() || p == null || p.isEmpty()) {
                new OMTDRepositoryImporter(server).load();
            } else {
                new OMTDRepositoryImporter(server, u, p).load();
            }            
        } else {
            final List<String> galaxy = params.get("-galaxy");
            if (galaxy != null) {
                final String server = galaxy.isEmpty() ? "https://galaxy.bi.uni-freiburg.de/" 
                        : galaxy.get(0);
                final URI uri = URI.create(server);
                
                if (u == null || u.isEmpty() || p == null || p.isEmpty()) {
                    new GalaxyRepositoryImporter(uri).load();
                } else {
                    new GalaxyRepositoryImporter(uri, u, p).load();
                }                
            } else {
                System.out.println(HELP);
            }
        }
    }
    
    private static Map<String, List<String>> parameters(String[] args) {
        TreeMap<String, List<String>> parameters = new TreeMap();        
        List<String> values = null;
        for (String arg : args) {
            switch(arg) {
                case "-u":
                case "-p":
                case "-biotools":
                case "-bioconda":
                case "-galaxy":
                case "-omtd":
                case "-h":
                case "--helph": values = parameters.get(arg);
                                if (values == null) {
                                    values = new ArrayList(); 
                                    parameters.put(arg, values);
                                }
                                break;
                default: if (values != null) {
                    values.add(arg);
                }
            }
        }
        return parameters;
    }

}
