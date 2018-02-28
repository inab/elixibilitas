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

package es.elixir.bsc.openebench.tools;

import es.elixir.bsc.openebench.model.tools.Tool;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
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

/**
 * @author Dmitry Repchevsky
 */

public class OpenEBenchRepository {
    
    public static final String URI_BASE = "https://openebench.bsc.es/monitor/tool/";
    
    private static volatile Map<String, Tool> tools;
    
    public static Map<String, Tool> getTools() {
        Map<String, Tool> toolz = OpenEBenchRepository.tools;
        if (toolz == null) {
            synchronized(ToolsComparator.class) {
                toolz = OpenEBenchRepository.tools;
                if (toolz == null) {
                    OpenEBenchRepository.tools = toolz = load();
                }
            }
        }
        return toolz;
    }

    private static Map<String, Tool> load() {
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
                        toolz.put(tool.getHomepage().toString(), tool);
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ToolsComparator.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return toolz;
    }    

    public static Tool getByHomepage(String homepage) {
        return getTools().get(homepage);
    }
}
