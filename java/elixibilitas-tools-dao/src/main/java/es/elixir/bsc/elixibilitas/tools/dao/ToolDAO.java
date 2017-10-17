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

package es.elixir.bsc.elixibilitas.tools.dao;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.ReturnDocument;
import es.elixir.bsc.openebench.model.tools.CommandLineTool;
import es.elixir.bsc.openebench.model.tools.DatabasePortal;
import es.elixir.bsc.openebench.model.tools.DesktopApplication;
import es.elixir.bsc.openebench.model.tools.Library;
import es.elixir.bsc.openebench.model.tools.Ontology;
import es.elixir.bsc.openebench.model.tools.Plugin;
import es.elixir.bsc.openebench.model.tools.SOAPServices;
import es.elixir.bsc.openebench.model.tools.SPARQLEndpoint;
import es.elixir.bsc.openebench.model.tools.Script;
import es.elixir.bsc.openebench.model.tools.Suite;
import es.elixir.bsc.openebench.model.tools.Tool;
import es.elixir.bsc.openebench.model.tools.WebAPI;
import es.elixir.bsc.openebench.model.tools.WebApplication;
import es.elixir.bsc.openebench.model.tools.Workbench;
import es.elixir.bsc.openebench.model.tools.Workflow;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.JsonObject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.config.PropertyNamingStrategy;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;
import org.bson.conversions.Bson;
import org.bson.json.JsonWriter;
import org.bson.json.JsonWriterSettings;

/**
 * @author Dmitry Repchevsky
 */

public class ToolDAO {
    
    public final static String COLLECTION = "biotoolz";
    public final static String AUTHORITY = "http://elixir.bsc.es/tool/";
    
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

    public static List<Tool> get(MongoClient mc) {
        List<Tool> tools = new ArrayList<>();

        try {
            final MongoDatabase db = mc.getDatabase("elixibilitas");
            final MongoCollection<Document> col = db.getCollection(COLLECTION);
            FindIterable<Document> iterator = col.find().projection(new BasicDBObject());
            try (MongoCursor<Document> cursor = iterator.iterator()) {
                while (cursor.hasNext()) {
                    tools.add(deserialize(cursor.next()));
                }
            }
        } catch(Exception ex) {
            Logger.getLogger(ToolDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        return tools;
    }
    
    public static Tool get(MongoClient mc, String id) {
        final List<Document> docs = getBSON(mc, id);
        return docs.isEmpty() ? null : deserialize(docs.get(0));
    }
    
    private static Tool deserialize(Document doc) {
        final Document _id = (Document) doc.remove("_id");
        final String type = _id.getString("type");

        doc.append("@id", createID(_id));
        doc.append("@type", type);
                        
        final Jsonb jsonb = JsonbBuilder.create(new JsonbConfig()
                    .withPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE));
        
        final String json = doc.toJson();
        
        switch(type) {
            case "cmd": return jsonb.fromJson(json, CommandLineTool.class);
            case "web": return jsonb.fromJson(json, WebApplication.class);
            case "db": return jsonb.fromJson(json, DatabasePortal.class);
            case "app": return jsonb.fromJson(json, DesktopApplication.class);
            case "lib": return jsonb.fromJson(json, Library.class);
            case "ontology": return jsonb.fromJson(json, Ontology.class);
            case "workflow": return jsonb.fromJson(json, Workflow.class);
            case "plugin": return jsonb.fromJson(json, Plugin.class);
            case "sparql": return jsonb.fromJson(json, SPARQLEndpoint.class);
            case "soap": return jsonb.fromJson(json, SOAPServices.class);
            case "script": return jsonb.fromJson(json, Script.class);
            case "rest": return jsonb.fromJson(json, WebAPI.class);
            case "workbench": return jsonb.fromJson(json, Workbench.class);
            case "suite": return jsonb.fromJson(json, Suite.class);
        }
        
        return jsonb.fromJson(json, Tool.class);
    }
    
    public static String getJSON(MongoClient mc, String id) {
        final List<Document> docs = getBSON(mc, id);
        
        if (docs.isEmpty()) {
            return null;
        }

        final Document doc = docs.get(0);
        final Document _id = (Document) doc.remove("_id");
        doc.append("@id", createID(_id));
        doc.append("@type", _id.getString("type"));
            
        return doc.toJson();        
    }
    
    public static String getJSONArray(MongoClient mc, String uri) {
        final List<Document> docs = getBSON(mc, uri);
        if (docs.isEmpty()) {
            return null;
        }
                        
        final Iterator<Document> iter = docs.iterator();

        final StringBuilder sb = new StringBuilder();
        
        sb.append('[');
        do {
            final Document doc = iter.next();
            final Document _id = (Document) doc.remove("_id");
            doc.append("@id", createID(_id));
            doc.append("@type", _id.getString("type"));
                        
            sb.append(doc.toJson());
        } while (iter.hasNext() && sb.append(',') != null);
        sb.append(']');
        
        return sb.toString();
    }
    
    private static List<Document> getBSON(MongoClient mc, String uri) {
        List<Document> list = new ArrayList<>();
        
        try {
            final MongoDatabase db = mc.getDatabase("elixibilitas");
            final MongoCollection<Document> col = db.getCollection(COLLECTION);

            final Bson query = createFindQuery(uri);
            if (query != null) {
                try (MongoCursor<Document> cursor = col.find(query).iterator()) {
                    while(cursor.hasNext()) {
                        Document doc = cursor.next();
                        list.add(doc);
                    }
                }
            }
        } catch(Exception ex) {
            Logger.getLogger(ToolDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;        
    }
    
    public static void put(MongoClient mc, JsonObject json) {
        final String id = json.getString("@id");
        put(mc, id, json.toString());
    }

    public static void put(MongoClient mc, Tool tool) {
        
        final Jsonb jsonb = JsonbBuilder.create(new JsonbConfig()
                    .withPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE));
        final String json = jsonb.toJson(tool);
        
        put(mc, tool.id.toString(), json);
    }
    
    public static void put(MongoClient mc, String id, String json) {
        try {
            MongoDatabase db = mc.getDatabase("elixibilitas");
            MongoCollection<Document> col = db.getCollection(COLLECTION);
            
            FindOneAndUpdateOptions opt = new FindOneAndUpdateOptions().upsert(true)
                .projection(Projections.excludeId()).returnDocument(ReturnDocument.AFTER);

            Bson pk = createPK(id);
            if (pk != null) {
                Document bson = Document.parse(json);
                
                // do not store @id and @type, but compound mongodb primary key
                bson.append("_id", pk);
                bson.remove("@id");
                bson.remove("@type");

                col.findOneAndUpdate(Filters.eq("_id", pk),
                        new Document("$set", bson), opt);
            }
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ToolDAO.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(ToolDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static Bson createFindQuery(String id) {
        final URI uri = URI.create(id);
        final String path = uri.getPath();
        if (path != null) {
            final String[] nodes = path.split("/");
            if (nodes.length > 4) {
                return Filters.eq("_id", 
                    new BasicDBObject("id", nodes[2])
                    .append("type", nodes[3])
                    .append("host", nodes[4]));

            }
            if (nodes.length > 3) {
                return Filters.and(Filters.eq("_id.id", nodes[2]), Filters.eq("_id.type", nodes[3]));
            }
            if (nodes.length > 2) {
                return Filters.eq("_id.id", nodes[2]);
            }
        }
        return null;
    }
    
    private static Bson createPK(String id) {
        final URI uri = URI.create(id);
        final String path = uri.getPath();
        if (path != null) {
            final String[] nodes = path.split("/");
            if (nodes.length > 4) {
                return new BasicDBObject("id", nodes[2])
                    .append("type", nodes[3])
                    .append("host", nodes[4]);

            }
        }
        return null;
    }
    
    private static String createID(Document _id) {
        StringBuilder sb = new StringBuilder(AUTHORITY);
        
        sb.append(_id.getString("id")).append('/');
        sb.append(_id.getString("type")).append('/');
        sb.append(_id.getString("host"));
        
        return sb.toString();
    }
    
    public static void write(MongoClient mc, Writer writer, Integer skip, Integer limit, List<String> projections) {
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
                
                FindIterable<Document> iterator = col.find().sort(new BasicDBObject("name", 1));
                
                if (skip != null) {
                    iterator = iterator.skip(skip);
                }
                if (limit != null) {
                    iterator.limit(limit);
                }

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

                            Document _id = (Document) doc.remove("_id");
                            doc.append("@id", createID(_id));
                            doc.append("@type", _id.getString("type"));

                            doc.toJson(codec);
                            jwriter.flush();
                        } while (cursor.hasNext() && writer.append(",\n") != null);
                    }
                }
                writer.write("]\n");
            }

        } catch(IOException ex) {
            Logger.getLogger(ToolDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Write those tools ids those semantic annotations found in the map.
     * 
     * @param mc mongodb connection.
     * @param writer the writer to write ids.
     * @param map the map that has semantic ids as its keys.
     */
    public static void filter(MongoClient mc, Writer writer, Map<String, List<Map.Entry<String, String>>> map) {

        final MongoDatabase db = mc.getDatabase("elixibilitas");
        final MongoCollection<Document> col = db.getCollection(COLLECTION);

        FindIterable<Document> iterator = col.find().projection(new BasicDBObject("semantics", true));
        try (MongoCursor<Document> cursor = iterator.iterator()) {

            boolean first = true;
            loop:
            while (cursor.hasNext()) {

                final Document doc = cursor.next();
                final String id = createID((Document) doc.remove("_id"));

                Document semantics = doc.get("semantics", Document.class);
                if (semantics != null) {                        
                    List<String> topics = semantics.get("topics", List.class);
                    if (topics != null) {
                        for (String topic : topics) {
                            final List<Map.Entry<String, String>> list = map.get(topic);
                            if (writeId(writer, list, id, first)) {
                                first = false;
                                continue loop;
                            }
                        }
                    }
                    List<String> operations = semantics.get("operations", List.class);
                    if (operations != null) {
                        for (String operation : operations) {
                            final List<Map.Entry<String, String>> list = map.get(operation);
                            if (writeId(writer, list, id, first)) {
                                first = false;
                                continue loop;
                            }
                        }
                    }
                    List<Document> inputs = semantics.get("inputs", List.class);
                    if (inputs != null) {
                        for (Document input : inputs) {
                            final String datatype = input.getString("datatype");
                            List<Map.Entry<String, String>> list = map.get(datatype);
                            if (writeId(writer, list, id, first)) {
                                first = false;
                                continue loop;
                            }
                        }
                    }
                    List<Document> outputs = semantics.get("outputs", List.class);
                    if (outputs != null) {
                        for (Document output : outputs) {
                            final String datatype = output.getString("datatype");
                            List<Map.Entry<String, String>> list = map.get(datatype);
                            if (writeId(writer, list, id, first)) {
                                first = false;
                                continue loop;
                            }
                        }
                    }
                }
            }
        } catch(Exception ex) {
            Logger.getLogger(ToolDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Conditionally writes the identifier to the writer.
     * 
     * @param writer the writer stream to write the identifier.
     * @param list the search result.
     * @param id the identifier to write.
     * @param first whether this is the first identifier to write.
     * 
     * @return whether the identifier has been written;
     * 
     * @throws IOException 
     */
    private static boolean writeId(Writer writer, List list, String id, boolean first) throws IOException {
        if (list != null && list.size() > 0 && (first || writer.append(",\n") != null)) {
            writer.append("{\"@id\":\"").append(id).append("\"}");
            return true;
        }
        return false;
    }    
}
