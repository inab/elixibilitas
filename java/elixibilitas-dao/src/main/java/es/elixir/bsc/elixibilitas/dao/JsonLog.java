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

package es.elixir.bsc.elixibilitas.dao;

import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonPatch;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import org.bson.BsonArray;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 * @author Dmitry Repchevsky
 */

public class JsonLog {

    private final MongoDatabase database;
    private final String log_collection;
    
    public JsonLog(final MongoDatabase database, 
                   final String log_collection) {
        this.database = database;
        this.log_collection = log_collection;
    }

    public void log(String user, String id, String src, String tgt) {
        
        JsonPatch patch = JsonLog.createJsonPatch(src, tgt);
        log(user, id, patch);
    }

    public void log(String user, String id, JsonPatch patch) {
        final JsonArray array = patch.toJsonArray();

        if (!array.isEmpty()) {
            final StringWriter writer = new StringWriter();
            Json.createWriter(writer).writeArray(array);

            MongoCollection<Document> col = database.getCollection(log_collection);

            Document bson = new Document();
            bson.append("_id", new BasicDBObject("id", id).append("date", ZonedDateTime.now(ZoneId.of("Z")).toString()));
            bson.append("src", user);
            bson.append("patch", BsonArray.parse(writer.toString()));

            col.insertOne(bson);
        }
    }

    public JsonArray findLog(String id, String jpointer, String from, String to, Integer limit) {

        try {
            final MongoCollection<Document> col = database.getCollection(log_collection);

            Bson query = new BasicDBObject("_id.id", id);
            if (from != null && to == null) {
                query = Filters.and(query, Filters.gte("_id.date", from));
            } else if (from == null && to != null) {
                query = Filters.and(query, Filters.lte("_id.date", to));
            } else if (from != null && to != null) {
                query = Filters.and(query, Filters.gte("_id.date", from), Filters.lte("_id.date", to));
            }

            final ArrayList aggregate = new ArrayList();
            
            aggregate.add(Aggregates.match(query));
            aggregate.add(Aggregates.unwind("$patch"));
            aggregate.add(Aggregates.match(new Document("patch.path", jpointer)));
            if (limit != null) {
                aggregate.add(Aggregates.sort(new Document("_id.date", -1)));
                aggregate.add(Aggregates.limit(limit));
                aggregate.add(Aggregates.sort(new Document("_id.date", 1)));
            }
            aggregate.add(Aggregates.project(new Document("_id.date", 1).append("patch.value", 1)));
            
            AggregateIterable<Document> iterator = col.aggregate(aggregate);
            
            JsonArrayBuilder builder = Json.createArrayBuilder();
            
            try (MongoCursor<Document> cursor = iterator.iterator()) {
                while (cursor.hasNext()) {
                    final Document doc = cursor.next();
                    final Document patch = doc.get("patch", Document.class);
                    if (patch != null) {
                        JsonObjectBuilder jab = Json.createObjectBuilder().add("date", doc.get("_id", Document.class).getString("date"));
                        JsonObject o = Json.createReader(new StringReader(patch.toJson())).readObject();
                        if (o != null) {
                            JsonValue value = o.get("value");
                            if (value == null) {
                                jab.addNull("value");
                            } else {
                                jab.add("value", value.toString());
                            }
                        }
                        builder.add(jab);
                    }
                }
            }
            return builder.build();
        } catch(Exception ex) {
            Logger.getLogger(JsonLog.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

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
                                final JsonStructure empty = type == ValueType.OBJECT ? Json.createObjectBuilder().build() : 
                                                                       Json.createArrayBuilder().build();
                                if (inv) {
                                    unroll(builder, root + path, 
                                            Json.createDiff(empty, (JsonStructure)value), inv);
                                    builder.add(Json.createObjectBuilder()
                                            .add("op", "remove")
                                            .add("path", root + path));                                    
                                } else {
                                    builder.add(Json.createObjectBuilder()
                                            .add("op", "add")
                                            .add("path", root + path)
                                            .add("value", type == ValueType.OBJECT ? "{}" : "[]"));
                                    unroll(builder, root + path, 
                                            Json.createDiff(empty, (JsonStructure)value), inv);
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
