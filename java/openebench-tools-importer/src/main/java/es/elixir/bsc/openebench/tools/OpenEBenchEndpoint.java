package es.elixir.bsc.openebench.tools;

import es.elixir.bsc.openebench.model.tools.Tool;
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
        
        con.setRequestMethod("PATCH");
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
        
        try (InputStream in = URI.create(URI_BASE).toURL().openStream();
             JsonParser parser = Json.createParser(new BufferedInputStream(in))) {
            if (parser.hasNext() &&
                parser.next() == JsonParser.Event.START_ARRAY) {
                
                final Iterator<JsonValue> iter = parser.getArrayStream().iterator();
                while(iter.hasNext()) {
                    final JsonValue value = iter.next();
                    if (value.getValueType() == JsonValue.ValueType.OBJECT) {
                        final JsonObject object = value.asJsonObject();
                        final Tool tool = jsonb.fromJson(object.toString(), Tool.class);
                        toolz.put(tool.id.toString(), tool);
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ToolsComparator.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return toolz;
    }

}
