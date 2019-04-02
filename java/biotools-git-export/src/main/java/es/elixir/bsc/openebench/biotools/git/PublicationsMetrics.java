package es.elixir.bsc.openebench.biotools.git;

import static es.elixir.bsc.openebench.biotools.git.MetricExporter.PUBLICATIONS_ENDPOINT;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.json.JsonValue;

/**
 * @author Dmitry Repchevsky
 */

public class PublicationsMetrics {
    
    public static JsonArray getPublications(final String id) {
        
        // read publications from the instance
        try (JsonReader p = Json.createReader(
                new BufferedInputStream(
                        new URL(String.format(PUBLICATIONS_ENDPOINT, id)).openStream()))) {

                final JsonStructure structure = p.read();
                if (structure != null && 
                    structure.getValueType() == JsonValue.ValueType.ARRAY && 
                    !structure.asJsonArray().isEmpty()) {
                    
                    final JsonArray publications = structure.asJsonArray();
                    
                    JsonArrayBuilder publications_builder = Json.createArrayBuilder();
                    
                    for (int i = 0; i < publications.size(); i++) {
                        final JsonObject publication = publications.getJsonObject(i);
                        final JsonArray entries = publication.getJsonArray("entries");
                        
                        final JsonArrayBuilder entries_builder = Json.createArrayBuilder();
                        for (int j = 0; j < entries.size(); j++) {
                            final JsonObject entry = entries.getJsonObject(j);
                            
                            final JsonObjectBuilder entry_builder = Json.createObjectBuilder(entry);
                            entry_builder.remove("refs");
                            entry_builder.remove("ref_count");
                            entries_builder.add(entry_builder);
                        }
                        publications_builder.add(entries_builder);
                    }
                    return publications_builder.build();
                }
        } catch (MalformedURLException ex) {
             Logger.getLogger(MetricExporter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
             Logger.getLogger(MetricExporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }

}
