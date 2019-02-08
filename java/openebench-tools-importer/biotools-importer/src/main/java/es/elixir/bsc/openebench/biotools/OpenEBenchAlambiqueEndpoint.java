package es.elixir.bsc.openebench.biotools;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.stream.JsonGenerator;
import javax.xml.bind.DatatypeConverter;

/**
 * @author Dmitry Repchevsky
 */

public class OpenEBenchAlambiqueEndpoint {
    
    public static final String ALAMBIQUE_URI_BASE = "https://dev-openebench.bsc.es/monitor/alambique/";
    
    private final String credentials;
    
    public OpenEBenchAlambiqueEndpoint(String name, String password) {
        String _credentials;
        try {
            final StringBuilder sb = new StringBuilder().append(name).append(':').append(password);
            _credentials = DatatypeConverter.printBase64Binary(sb.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            _credentials = "";
        }
        this.credentials = _credentials;
    }
    
    public int put(final JsonObject jtool) throws MalformedURLException, IOException {
        
        if (credentials.isEmpty()) {
            return 401;
        }
        
        final String id = jtool.getString("biotoolsCURIE", null);
        if (id == null || id.isEmpty()) {
            return 400;
        }

        final URL url = new URL(ALAMBIQUE_URI_BASE + id);
        final HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setDoOutput(true);
        
        con.setRequestMethod("PUT");
        con.setRequestProperty("Authorization", "Basic " + credentials);
        con.setRequestProperty("Content-type", "application/json");
        try (OutputStream out = con.getOutputStream();
             JsonGenerator writer = Json.createGenerator(out)) {
            writer.write(jtool);
        }

        return con.getResponseCode();
    }
}
