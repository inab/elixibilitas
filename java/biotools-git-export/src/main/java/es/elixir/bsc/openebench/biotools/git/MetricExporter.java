package es.elixir.bsc.openebench.biotools.git;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.JsonWriter;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.SystemReader;

/**
 * @author Dmitry Repchevsky
 */

public class MetricExporter {
    
    private final static String HELP = 
            "biotools-importer [-u && -p]\n\n" +
            "parameters:\n\n" +
            "-h (--help)                - this help message\n" +
            "-u (--user) 'username'     - OpenEBench username\n" +
            "-p (--password) 'password' - OpenEBench pasword\n\n" +
            "comment: in the absense of credentials the tool only simulates the activity.\n" +
            "example: >java -jar biotools-importer.jar\n";

    public final static String TOOLS_ENDPOINT = "https://openebench.bsc.es/monitor/rest/search?id=biotools::&projection=xid";

    public final static String HOMEPAGE_METRICS_ENDPOINT = "https://openebench.bsc.es/monitor/rest/metrics/availability/%s";
    public final static String HOMEPAGE_BIOSCHEMAS_ENDPOINT = "https://openebench.bsc.es/monitor/metrics/%s/project/website/bioschemas";
    public final static String PUBLICATIONS_ENDPOINT = "https://openebench.bsc.es/monitor/metrics/%s/project/publications";

    public final static String GIT = "https://github.com/redmitry/content.git";
    public final static String BRANCH = "origin/master";

    public static void main(String[] args) throws IOException {
        
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

        final String u = user == null || user.isEmpty() ? null : user.get(0);
        final String p = password == null || password.isEmpty() ? null : password.get(0);

        if (u == null || u.isEmpty() || p == null || p.isEmpty()) {
            System.out.println(HELP);
        } else {
            process(u, p);
        }
    }
    
    private static void process(final String user, final String password) {
        // ignore file names validation as we do not use 'real' file system
        SystemReader old = SystemReader.getInstance();
        SystemReader.setInstance(new JgitSytemReaderHack(old));

        Configuration config = Configuration.unix().toBuilder()
                                    .setWorkingDirectory("/")
                                    .build();

        FileSystem fs = Jimfs.newFileSystem(config);
        final Path root = fs.getPath("/");
        
        try (Git git = Git.cloneRepository()
                         .setURI(GIT)
                         .setDirectory(root)
                         .setBranch("master")
                         .setRemote(BRANCH)
                         .call()) {
            
            setMetrics(git);
            
            git.add().addFilepattern(".").call();

            git.commit().setMessage("openebench metrics " + Instant.now()).call();

            git.push().setRemote(BRANCH).setCredentialsProvider(
                    new UsernamePasswordCredentialsProvider(user, password)).call();
        } 
        catch (GitAPIException ex) {
            Logger.getLogger(MetricExporter.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
    }

    private static void setMetrics(final Git git) {
        
        HashMap<String, Path> files = new HashMap();

        // index .json files by biotoolsID
        try (Stream<Path> stream = Files.walk(git.getRepository().getWorkTreePath())){
            final Iterator<Path> iter = stream.iterator();
            while (iter.hasNext()) {
                final Path path = iter.next();
                if (Files.isRegularFile(path) && path.toString().endsWith(".json")) {
                    final String biotoolsID = getBiotoolsID(path);
                    if (biotoolsID != null) {
                        files.put(biotoolsID, path);
                    }
                }
            }
        } catch(IOException ex) {
            Logger.getLogger(MetricExporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // iterate over all 'biotools:' tools
        try (BufferedInputStream in = new BufferedInputStream(new URL(TOOLS_ENDPOINT).openStream());
             JsonParser parser = Json.createParser(in)) {
            
            if (parser.hasNext() &&
                parser.next() == JsonParser.Event.START_ARRAY) {

                String prev_xid = null;
                try (Stream<JsonValue> stream = parser.getArrayStream()) {
                    final Iterator<JsonValue> iter = stream.iterator();
                    while(iter.hasNext()) {
                        final JsonValue value = iter.next();
                        if (value.getValueType() == ValueType.OBJECT) {
                            final JsonObject tool = value.asJsonObject();

                            // strip out url base keeping a short id (aka 'biotools:pmut:2017/cmd/...'
                            String id = URI.create(tool.getString("@id", null)).getPath();
                            id = id.substring(id.indexOf("/tool/") + 6);
                            
                            final String label = tool.getString("@label", null);
                            
                            String xid = tool.getString("xid", null);
                            if (xid == null) {
                                xid = label;
                            } else {
                                final int colon = xid.indexOf(':');
                                if (colon >= 0) {
                                    xid = xid.substring(0, colon);
                                }
                            }
                            
                            // check whether we already processed the xid
                            if (xid == null || xid.equals(prev_xid)) {
                                continue;
                            }
                            prev_xid = xid;

                            final Path file = files.get(xid);
                            
                            if (file != null) {
                                System.out.println("=> found " + file);
                                
                                JsonObjectBuilder builder = Json.createObjectBuilder();
                                
                                final JsonObject homepage_object = HomepageMetrics.getHomepageAvailability(label);
                                final JsonValue bioschemas_value = HomepageMetrics.getBioSchemas(label);

                                if ((homepage_object != null && !homepage_object.isEmpty()) || bioschemas_value != null) {
                                    final JsonObjectBuilder homepage_builder = homepage_object == null ? Json.createObjectBuilder() : 
                                                                                Json.createObjectBuilder(homepage_object);
                                    if (bioschemas_value != null) {
                                        homepage_builder.add("bioschemas", bioschemas_value);
                                    }
                                    builder.add("homepage_metrics", homepage_builder);
                                }
                                
                                final JsonArray publications = PublicationsMetrics.getPublications(id);
                                if (publications != null) {
                                    builder.add("publications", publications);
                                }
                                
                                final String file_name = file.getFileName().toString();
                                final Path metrics_file = file.getParent().resolve(file_name.substring(0, file_name.length() - ".json".length()) + ".oeb.json");
                                try (JsonWriter writer = Json.createWriterFactory(Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true))
                                        .createWriter(Files.newBufferedWriter(metrics_file))) {
                                    writer.writeObject(builder.build());
                                }
                            }
                        }
                    }
                }
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(MetricExporter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MetricExporter.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
    private static String getBiotoolsID(final Path path) {
        try (JsonReader parser = Json.createReader(Files.newBufferedReader(path))) {
            final JsonStructure structure = parser.read();
            if (structure.getValueType() == ValueType.OBJECT) {
                final JsonObject object = structure.asJsonObject();
                final JsonValue biotoolsID = object.get("biotoolsID");
                if (biotoolsID != null && 
                    biotoolsID.getValueType() == ValueType.STRING) {

                    return ((JsonString)biotoolsID).getString();
                }
            }
        } catch(IOException ex) {
            Logger.getLogger(MetricExporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
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
