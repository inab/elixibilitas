package es.elixir.bsc.openebench.tools;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.stream.JsonGenerator;

/**
 * @author Dmitry Repchevsky
 */

public class OpenEBenchAlambiqueEndpoint {
    
    private final String credentials;
    
    public OpenEBenchAlambiqueEndpoint(String name, String password) {
        String _credentials;
        try {
            final StringBuilder sb = new StringBuilder().append(name).append(':').append(password);
            _credentials = Base64.getEncoder().encodeToString(sb.toString().getBytes("UTF-8"));
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

        final URL url = new URL(OpenEBenchEndpoint.ALAMBIQUE_URI_BASE + id);
        final HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setInstanceFollowRedirects(false);
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
