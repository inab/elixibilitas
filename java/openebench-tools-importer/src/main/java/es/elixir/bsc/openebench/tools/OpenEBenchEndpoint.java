package es.elixir.bsc.openebench.tools;

import es.elixir.bsc.openebench.model.tools.CommandLineTool;
import es.elixir.bsc.openebench.model.tools.DatabasePortal;
import es.elixir.bsc.openebench.model.tools.DesktopApplication;
import es.elixir.bsc.openebench.model.tools.Library;
import es.elixir.bsc.openebench.model.tools.Ontology;
import es.elixir.bsc.openebench.model.tools.Plugin;
import es.elixir.bsc.openebench.model.tools.SOAPServices;
import es.elixir.bsc.openebench.model.tools.SPARQLEndpoint;
import es.elixir.bsc.openebench.model.tools.Script;
import es.elixir.bsc.openebench.model.tools.Suite;
import es.elixir.bsc.openebench.model.tools.Tool;
import es.elixir.bsc.openebench.model.tools.WebAPI;
import es.elixir.bsc.openebench.model.tools.WebApplication;
import es.elixir.bsc.openebench.model.tools.Workbench;
import es.elixir.bsc.openebench.model.tools.Workflow;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.config.PropertyNamingStrategy;
import javax.json.stream.JsonParser;
import javax.xml.bind.DatatypeConverter;

/**
 * @author Dmitry Repchevsky
 */

public class OpenEBenchEndpoint {
    
    public static final String URI_BASE = "https://openebench.bsc.es/monitor/tool/";
    
    private final String credentials;
    
    public OpenEBenchEndpoint(String name, String password) {
        String _credentials;
        try {
            final StringBuilder sb = new StringBuilder().append(name).append(':').append(password);
            _credentials = DatatypeConverter.printBase64Binary(sb.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            _credentials = "";
        }
        this.credentials = _credentials;
    }
    
    public int put(final Tool tool) throws MalformedURLException, IOException {
        final Jsonb jsonb = JsonbBuilder.create(
                new JsonbConfig().withPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE));
        final String json = jsonb.toJson(tool);
        
        final URL url = tool.id.toURL();
        final HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setDoOutput(true);
        
        con.setRequestMethod("PUT");
        con.setRequestProperty("Authorization", "Basic " + credentials);
        con.setRequestProperty("Content-type", "application/json");
        try (OutputStream out = con.getOutputStream()) {
            out.write(json.getBytes("UTF-8"));
        }

        return con.getResponseCode();
    }

    public int patch(final Tool tool) throws MalformedURLException, IOException {
        final Jsonb jsonb = JsonbBuilder.create(
                new JsonbConfig().withPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE));
        final String json = jsonb.toJson(tool);
        
        final URL url = tool.id.toURL();
        final HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setDoOutput(true);

        con.setRequestMethod("POST");
        con.setRequestProperty("X-HTTP-Method-Override", "PATCH");
        con.setRequestProperty("Authorization", "Basic " + credentials);
        con.setRequestProperty("Content-type", "application/json");
        try (OutputStream out = con.getOutputStream()) {
            out.write(json.getBytes("UTF-8"));
        }
        
        return con.getResponseCode();
    }
    
    public static Map<String, Tool> get() {
        Map<String, Tool> toolz = new ConcurrentHashMap<>();
        
        final Jsonb jsonb = JsonbBuilder.create(
                new JsonbConfig().withPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE));

        try {
            HttpURLConnection con = (HttpURLConnection)URI.create(URI_BASE).toURL().openConnection();
            con.setRequestProperty("Accept", "application/json");
            con.setUseCaches(false);
            con.setDoOutput(true);
            try (InputStream in = con.getInputStream();
                 JsonParser parser = Json.createParser(new BufferedInputStream(in))) {

                if (parser.hasNext() &&
                    parser.next() == JsonParser.Event.START_ARRAY) {

                    final Iterator<JsonValue> iter = parser.getArrayStream().iterator();
                    while(iter.hasNext()) {
                        final JsonValue value = iter.next();
                        if (value.getValueType() == JsonValue.ValueType.OBJECT) {
                            final JsonObject object = value.asJsonObject();
                            final Tool tool = deserialize(jsonb, object);
                            toolz.put(tool.id.toString(), tool);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ToolsComparator.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return toolz;
    }

    private static Tool deserialize(Jsonb jsonb, JsonObject object) {
        
        final String json = object.toString();

        final String type = object.getString("@type", null);
        if (type != null) {
            switch(type) {
                case CommandLineTool.TYPE: return jsonb.fromJson(json, CommandLineTool.class);
                case WebApplication.TYPE: return jsonb.fromJson(json, WebApplication.class);
                case DatabasePortal.TYPE: return jsonb.fromJson(json, DatabasePortal.class);
                case DesktopApplication.TYPE: return jsonb.fromJson(json, DesktopApplication.class);
                case Library.TYPE: return jsonb.fromJson(json, Library.class);
                case Ontology.TYPE: return jsonb.fromJson(json, Ontology.class);
                case Workflow.TYPE: return jsonb.fromJson(json, Workflow.class);
                case Plugin.TYPE: return jsonb.fromJson(json, Plugin.class);
                case SPARQLEndpoint.TYPE: return jsonb.fromJson(json, SPARQLEndpoint.class);
                case SOAPServices.TYPE: return jsonb.fromJson(json, SOAPServices.class);
                case Script.TYPE: return jsonb.fromJson(json, Script.class);
                case WebAPI.TYPE: return jsonb.fromJson(json, WebAPI.class);
                case Workbench.TYPE: return jsonb.fromJson(json, Workbench.class);
                case Suite.TYPE: return jsonb.fromJson(json, Suite.class);
            }
        }
        return jsonb.fromJson(json, Tool.class);
    }
}
