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

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.ReturnDocument;
import es.elixir.bsc.elixibilitas.model.metrics.Metrics;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.JsonbException;
import javax.json.bind.config.PropertyNamingStrategy;
import org.bson.BsonArray;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;
import org.bson.json.JsonWriter;
import org.bson.json.JsonWriterSettings;

/**
 * Utility class to get/put Metrics into MongoDB.
 * 
 * @author Dmitry Repchevsky
 */

public class MetricsDAO implements Serializable {
    
    public final static String COLLECTION = "metrics2";
    public final static String LOG_COLLECTION = "metrics2.log";
    
    public final static String AUTHORITY = "http://elixir.bsc.es/metrics/";
    
    public static long count(MongoClient mc) {
        final MongoDatabase db = mc.getDatabase("elixibilitas");
        final MongoCollection<Document> col = db.getCollection(COLLECTION);
        return col.count();
    }

    public static long count(MongoClient mc, String query) {
        final MongoDatabase db = mc.getDatabase("elixibilitas");
        final MongoCollection<Document> col = db.getCollection(COLLECTION);
        return col.count(Document.parse(query));
    }

    public static List<Metrics> get(MongoClient mc) {
        List<Metrics> metrics = new ArrayList<>();

        try {
            final MongoDatabase db = mc.getDatabase("elixibilitas");
            final MongoCollection<Document> col = db.getCollection(COLLECTION);
            
            for (Document doc : col.find()) {
                metrics.add(deserialize(doc));
            }
        } catch(Exception ex) {
            Logger.getLogger(MetricsDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        return metrics;
    }
    
    public static Metrics get(MongoClient mc, String id) {
        final Document doc = getBSON(mc, id);
        return doc != null ? deserialize(doc) : null;
    }
    
    public static String getJSON(MongoClient mc, String id) {
        final Document doc = getBSON(mc, id);
        return doc != null ? doc.toJson() : null;
    }
    
    private static Document getBSON(MongoClient mc, String id) {
        try {
            final MongoDatabase db = mc.getDatabase("elixibilitas");
            final MongoCollection<Document> col = db.getCollection(COLLECTION);

            Document doc = col.find(Filters.eq("_id", id)).first();
            if (doc != null) {
                doc.append("@id", AUTHORITY + doc.remove("_id"));
                doc.append("@type", "metrics");

                return doc;
            }
        } catch(Exception ex) {
            Logger.getLogger(MetricsDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static void put(MongoClient mc, String user, JsonObject json) {
        final String id = json.getString("@id");
        put(mc, user, id, json.toString());
    }

    public static void put(MongoClient mc, String user, String id, String json) {
        patch(mc, user, id, json);
    }

    public static String put(MongoClient mc, String user, String id, Metrics metrics) {
        final Jsonb jsonb = JsonbBuilder.create(new JsonbConfig()
                    .withPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE));
        final String json = jsonb.toJson(metrics);
        
        return patch(mc, user, id, json);
    }
    
    public static String patch(MongoClient mc, String user, String id, String json) {
        final String src = put(mc, id, json);
        log(mc, user, id, src, json);

        return json;
    }
    
    public static String patch(MongoClient mc, String user, String id, JsonPatch patch) {
        final String result = patch(mc, id, patch);
        if (result != null) {
            log(mc, user, id, patch);
        }
        return result;
    }

    public static String patch(MongoClient mc, String id, JsonPatch patch) {
        try  {
            MongoDatabase db = mc.getDatabase("elixibilitas");
            MongoCollection<Document> col = db.getCollection(COLLECTION);

            final Document doc = col.find(Filters.eq("_id", id)).first();
            if (doc != null) {
                final JsonObject target = Json.createReader(new StringReader(doc.toJson())).readObject();
                final JsonObject patched = patch.apply(target);
                final StringWriter writer = new StringWriter();
                Json.createWriter(writer).writeObject(patched);

                FindOneAndReplaceOptions opt = new FindOneAndReplaceOptions().returnDocument(ReturnDocument.AFTER);
                Document result = col.findOneAndReplace(Filters.eq("_id", id), Document.parse(writer.toString()), opt);

                doc.append("@id", doc.remove("_id"));
                doc.append("@type", "metrics");

                return result.toJson();
            }
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(MetricsDAO.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(MetricsDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private static String put(MongoClient mc, String id, String json) {
        try {
            MongoDatabase db = mc.getDatabase("elixibilitas");
            MongoCollection<Document> col = db.getCollection(COLLECTION);

            FindOneAndUpdateOptions opt = new FindOneAndUpdateOptions().upsert(true)
                    .projection(Projections.excludeId()).returnDocument(ReturnDocument.BEFORE);

            Document bson = Document.parse(json);
            bson.append("_id", id);
            
            Document doc = col.findOneAndUpdate(Filters.eq("_id", id),
                    new Document("$set", bson), opt);

            return doc != null ? doc.toJson() : null;
        } catch(Exception ex) {
            Logger.getLogger(MetricsDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static JsonArray findLog(MongoClient mc, String id, String jpointer) {

        try {
            final MongoDatabase db = mc.getDatabase("elixibilitas");
            final MongoCollection<Document> col = db.getCollection(LOG_COLLECTION);
            
            AggregateIterable<Document> iterator = col.aggregate(Arrays.asList(
                                        Aggregates.match(new BasicDBObject("_id.id", id)),
                                        Aggregates.unwind("$patch"),
                                        Aggregates.match(new BasicDBObject("patch.path", jpointer)),
                                        Aggregates.project(new BasicDBObject("_id.date", 1).append("patch.value", 1))));
            
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
            Logger.getLogger(MetricsDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;

    }
    
    private static void log(MongoClient mc, String user, String id, String src, String tgt) {
        
        JsonPatch patch = JsonLog.createJsonPatch(src, tgt);
        log(mc, user, id, patch);
    }
    
    /**
     * Unrolls JsonPatch in a way that all add operations become primitives
     * 
     * @param patch the original patch
     * @return unrolled patch
     */
    private static JsonPatch unroll(JsonPatch patch) {
        JsonArrayBuilder builder = Json.createArrayBuilder();
        JsonArray operations = patch.toJsonArray();
        for (int i = 0, n = operations.size(); i < n; i++) {
            JsonObject operation = operations.getJsonObject(i);
            JsonValue value = operation.get("value");
            if (value != null) {
                JsonObjectBuilder obuilder = Json.createObjectBuilder();
                switch(value.getValueType()) {
                    case OBJECT: obuilder.add("value", Json.createObjectBuilder());
                                 break;
                    case ARRAY: obuilder.add("value", Json.createArrayBuilder());
                                break;
                    default: builder.add(Json.createObjectBuilder(operation));
                             continue;

                }
                String path = operation.getString("path", "");
                String op = operation.getString("op", "");
                switch(op) {
                    case "replace": 
                            builder.add(obuilder.add("op", "remove").add("path", path));
                            // replace == remove + add
                    case "add": 
                            builder.add(obuilder.add("op", "add").add("path", path));
                    
                            JsonPatch subPatch = unroll(Json.createDiff(Json.createObjectBuilder().build(), 
                                                (JsonStructure)value));
                            JsonArray subOperations = subPatch.toJsonArray();
                            for (int j = 0, m = subOperations.size(); j < m; j++) {
                                JsonObject subOperation = subOperations.getJsonObject(j);
                                builder.add(obuilder.add("op", "add")
                                                    .add("path", path + subOperation.getString("path", ""))
                                                    .add("value", subOperation.get("value")));
                            }
                }
            }
        }

        return Json.createPatch(builder.build());
    }

    private static void log(MongoClient mc, String user, String id, JsonPatch patch) {
        final JsonArray array = patch.toJsonArray();

        if (!array.isEmpty()) {
            final StringWriter writer = new StringWriter();
            Json.createWriter(writer).writeArray(array);

            MongoDatabase db = mc.getDatabase("elixibilitas");
            MongoCollection<Document> col = db.getCollection(LOG_COLLECTION);

            Document bson = new Document();
            bson.append("_id", new BasicDBObject("id", id).append("date", ZonedDateTime.now(ZoneId.of("Z")).toString()));
            bson.append("src", user);
            bson.append("patch", BsonArray.parse(writer.toString()));

            col.insertOne(bson);
        }
    }
    
    private static Metrics deserialize(Document doc) {

        doc.append("@id", doc.remove("_id"));
        doc.append("@type", "metrics");
                        
        final Jsonb jsonb = JsonbBuilder.create(new JsonbConfig()
                    .withPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE));
        
        final String json = doc.toJson();
        
        try {
            return jsonb.fromJson(json, Metrics.class);
        } catch(JsonbException ex) {
            Logger.getLogger(MetricsDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    /**
     * Find metrics and write them into the reader.
     * 
     * @param mc - Mongodb client connection.
     * @param writer - writer to write metrics into.
     * @param projections - properties to write or null for all.
     */
    public static void write(MongoClient mc, Writer writer, List<String> projections) {
        try {
            final MongoDatabase db = mc.getDatabase("elixibilitas");
            final MongoCollection<Document> col = db.getCollection(COLLECTION);
            try (JsonWriter jwriter = new JsonWriter(writer, new JsonWriterSettings(true))) {

                final DocumentCodec codec = new DocumentCodec() {
                    @Override
                    public void encode(BsonWriter writer,
                       Document document,
                       EncoderContext encoderContext) {
                            super.encode(jwriter, document, encoderContext);
                    }
                };

                writer.write("[");
                
                FindIterable<Document> iterator = col.find();

                if (projections != null && projections.size() > 0) {
                    BasicDBObject bson = new BasicDBObject();
                    for (String field : projections) {
                        bson.append(field, true);
                    }
                    iterator = iterator.projection(bson);
                }

                try (MongoCursor<Document> cursor = iterator.iterator()) {
                    if (cursor.hasNext()) {
                        do {
                            final Document doc = cursor.next();

                            doc.append("@id", doc.remove("_id"));
                            doc.append("@type", "metrics");

                            doc.toJson(codec);
                            jwriter.flush();
                        } while (cursor.hasNext() && writer.append(",\n") != null);
                    }
                }
                writer.write("]\n");
            }

        } catch(IOException ex) {
            Logger.getLogger(MetricsDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
