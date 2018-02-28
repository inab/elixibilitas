/**
 * *****************************************************************************
 * Copyright (C) 2018 ELIXIR ES, Spanish National Bioinformatics Institute (INB)
 * and Barcelona Supercomputing Center (BSC)
 *
 * Modifications to the initial code base are copyright of their respective
 * authors, or their employers as appropriate.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 *****************************************************************************
 */

package es.elixir.bsc.openebench.galaxy;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.stream.JsonParser;

/**
 * @author Dmitry Repchevsky
 */

public class GalaxyToolsIterator implements Iterator<GalaxyTool> , Closeable, AutoCloseable {

    public static final String API = "api/tools";
    
    private final BufferedInputStream in;
    private final JsonParser parser;
    private final Iterator<JsonValue> iterator;
    private Iterator<JsonValue> elements;
    
    private JsonArray elem;
    
    public GalaxyToolsIterator(final URI server) throws IOException {
        in = new BufferedInputStream(server.resolve(API).toURL().openStream());
        parser = Json.createParser(in);
        if (parser.hasNext() &&
            parser.next() == JsonParser.Event.START_ARRAY) {
            iterator = parser.getArrayStream().iterator();
        } else {
            throw new IOException();
        }
    }
    
    @Override
    public boolean hasNext() {
        while (elements == null || !elements.hasNext()) {
            if (!iterator.hasNext()) {
                return false;
            }
            final JsonValue value = iterator.next();
            if (ValueType.OBJECT != value.getValueType()) {
                return false;
            }
            final JsonObject object = value.asJsonObject();
            final JsonArray array = object.getJsonArray("elems");
            if (array != null) {
                elements = array.iterator();
            }
        }
        return true;
    }

    @Override
    public GalaxyTool next() {
        if (!hasNext()) {
            return null;
        }
        
        final JsonValue value = elements.next();
        if (ValueType.OBJECT != value.getValueType()) {
            return null;
        }
        
        final JsonObject obj = value.asJsonObject();
        final JsonValue repo = obj.get("tool_shed_repository");
        if (repo != null) {
            return GalaxyTool.read(obj);
        }
        
        return null;
    }

    @Override
    public void close() throws IOException {
       in.close();
    }
    
}
