package es.elixir.bsc.openebench.openminted;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import javax.json.Json;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;

/**
 * @author Dmitry Repchevsky
 */

public class OMTDRegistryIterator implements Iterator<OMTDComponent> , Closeable, AutoCloseable {

    private final BufferedInputStream in;
    private final JsonParser parser;
    private final Iterator<JsonValue> iterator;
    
    public OMTDRegistryIterator(final URI endpoint) throws IOException {
        in = new BufferedInputStream(endpoint.toURL().openStream());
        parser = Json.createParser(in);
        if (parser.hasNext() &&
            parser.next() == JsonParser.Event.START_OBJECT) {
            while (parser.hasNext() && 
                   parser.next() != JsonParser.Event.KEY_NAME ||
                   !"results".equals(parser.getString())) {}
            
            if (parser.hasNext() && parser.next() == JsonParser.Event.START_ARRAY) {
                iterator = parser.getArrayStream().iterator();
                return;
            }
        }

        throw new IOException();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public OMTDComponent next() {
        if (!hasNext()) {
            return null;
        }
        JsonValue value = iterator.next();
        
        return OMTDComponent.load(value.asJsonObject());
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
    
}
