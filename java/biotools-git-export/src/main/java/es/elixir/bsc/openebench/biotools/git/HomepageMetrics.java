package es.elixir.bsc.openebench.biotools.git;

import static es.elixir.bsc.openebench.biotools.git.MetricExporter.HOMEPAGE_BIOSCHEMAS_ENDPOINT;
import static es.elixir.bsc.openebench.biotools.git.MetricExporter.HOMEPAGE_METRICS_ENDPOINT;
import java.io.BufferedInputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

/**
 * @author Dmitry Repchevsky
 */

public class HomepageMetrics {

    public static JsonObject getHomepageAvailability(final String id) {
        // read web metrics from global object
        try (JsonReader p = Json.createReader(
                new BufferedInputStream(
                        new URL(String.format(HOMEPAGE_METRICS_ENDPOINT, id)).openStream()))) {

            return p.readObject();
        } catch (Exception ex) {
            Logger.getLogger(MetricExporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static JsonValue getBioSchemas(final String id) {
        // read web metrics from global object
        try (JsonReader p = Json.createReader(
                new BufferedInputStream(
                        new URL(String.format(HOMEPAGE_BIOSCHEMAS_ENDPOINT, id)).openStream()))) {

            return p.readValue();
        } catch (Exception ex) {
            Logger.getLogger(MetricExporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
