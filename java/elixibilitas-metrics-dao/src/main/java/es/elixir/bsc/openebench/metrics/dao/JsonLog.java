/**
 * *****************************************************************************
 * Copyright (C) 2017 ELIXIR ES, Spanish National Bioinformatics Institute (INB)
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

package es.elixir.bsc.openebench.metrics.dao;

import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonPatch;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

/**
 * @author Dmitry Repchevsky
 */

public class JsonLog {
    
    public static JsonPatch createJsonPatch(String src, String tgt) {
        final JsonStructure src_obj = Json.createReader(new StringReader(src == null || src.isEmpty() ? "{}" : src )).read();
        final JsonStructure tgt_obj = Json.createReader(new StringReader(tgt == null || tgt.isEmpty() ? "{}" : tgt )).read();

        JsonArrayBuilder builder = Json.createArrayBuilder();
        
        unroll(builder, "", Json.createDiff(tgt_obj, src_obj), true);
        unroll(builder, "", Json.createDiff(src_obj, tgt_obj), false);
        
        return Json.createPatch(builder.build());
    }
    
    /**
     * Unrolls JsonPatch in a way that all add operations become primitives
     * 
     * @param patch the original patch
     * @return unrolled patch
     */
    private static void unroll(JsonArrayBuilder builder, String root, JsonPatch patch, boolean inv) {
        
        JsonArray operations = patch.toJsonArray();
        for (int i = 0, n = operations.size(); i < n; i++) {
            JsonObject operation = operations.getJsonObject(i);
            
            String op = operation.getString("op", "");
            String path = operation.getString("path", "");
            JsonValue value = operation.get("value");
            
            if (value != null) {
                ValueType type = value.getValueType();
                if (type == ValueType.OBJECT || type == ValueType.ARRAY) {
                    switch(op) {
                        case "replace": 
                                if (inv) {
                                    break;
                                }
                        case "add":
                                if (inv) {
                                    unroll(builder, root + path, Json.createDiff(Json.createObjectBuilder().build(), 
                                                    (JsonStructure)value), inv);
                                    builder.add(Json.createObjectBuilder()
                                            .add("op", "remove")
                                            .add("path", root + path));                                    
                                } else {
                                    builder.add(Json.createObjectBuilder()
                                            .add("op", "add")
                                            .add("path", root + path)
                                            .add("value", type == ValueType.OBJECT ? "{}" : "[]"));
                                    unroll(builder, root + path, Json.createDiff(Json.createObjectBuilder().build(), 
                                                    (JsonStructure)value), inv);
                                }
                    }
                } else if (inv) {
                    if ("add".equals(op)) {
                        builder.add(Json.createObjectBuilder()
                                .add("op", "remove")
                                .add("path", root + path)); 
                    }
                } else {
                    builder.add(Json.createObjectBuilder()
                            .add("op", op)
                            .add("path", root + path)
                            .add("value", value));                    
                }
            }
        }
    }

}
