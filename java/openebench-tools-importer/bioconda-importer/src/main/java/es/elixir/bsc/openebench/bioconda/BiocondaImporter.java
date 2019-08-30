package es.elixir.bsc.openebench.bioconda;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The tool to import bioconda packages into the openebench repository.
 * 
 * @author Dmitry Repchevsky
 */

public class BiocondaImporter {
    
    private final static String HELP = 
            "bioconda-importer [-u && -p]\n\n" +
            "parameters:\n\n" +
            "-h (--help)                - this help message\n" +
            "-u (--user) 'username'     - OpenEBench username\n" +
            "-p (--password) 'password' - OpenEBench pasword\n\n" +
            "comment: in the absense of credentials the tool only simulates the activity.\n" +
            "example: >java -jar bioconda-importer.jar\n";
    
    public static void main(String[] args) {
        Map<String, List<String>> params = parameters(args);
        
        if (params.get("-h") != null ||
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

        String u = user == null || user.isEmpty() ? null : user.get(0);
        String p = password == null || password.isEmpty() ? null : password.get(0);

        if (u == null || password == null) {
            try (InputStream in = BiocondaImporter.class.getClassLoader().getResourceAsStream("META-INF/config.properties")) {
                if (in != null) {
                    final Properties properties = new Properties();
                    properties.load(in);
                    u = properties.getProperty("user");
                    p = properties.getProperty("password");
                }
            } catch (IOException ex) {
                Logger.getLogger(BiocondaImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (u == null || u.isEmpty() || p == null || p.isEmpty()) {
            new BiocondaRepositoryImporter().load();
        } else {
            new BiocondaRepositoryImporter(u, p).load();
        }
    }
    
    private static Map<String, List<String>> parameters(String[] args) {
        TreeMap<String, List<String>> parameters = new TreeMap();        
        List<String> values = null;
        for (String arg : args) {
            switch(arg) {
                case "-u":
                case "--user":
                case "-p":
                case "--password":
                case "-h":
                case "--help": values = parameters.get(arg);
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
