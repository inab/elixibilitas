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
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndReplaceOptions;
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
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonPatch;
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

public class ToolsDAO extends AbstractDAO implements Serializable {
    
    public final static String COLLECTION = "biotoolz";
    public final static String AUTHORITY = "http://elixir.bsc.es/tool/";
    
    public ToolsDAO(MongoDatabase database) {
        super(database, COLLECTION);
    }

    public List<Tool> get() {
        List<Tool> tools = new ArrayList<>();

        try {
            final MongoCollection<Document> col = database.getCollection(collection);
            FindIterable<Document> iterator = col.find();
            try (MongoCursor<Document> cursor = iterator.iterator()) {
                while (cursor.hasNext()) {
                    tools.add(deserialize(cursor.next()));
                }
            }
        } catch(Exception ex) {
            Logger.getLogger(ToolsDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        return tools;
    }
    
    public Tool get(String id) {
        final List<Document> docs = getBSON(id);
        return docs.isEmpty() ? null : deserialize(docs.get(0));
    }
    
    private Tool deserialize(Document doc) {
        final Document _id = (Document) doc.remove("_id");
        final String type = _id.getString("type");

        doc.append("@id", createID(_id));
        doc.append("@type", type);
                        
        final Jsonb jsonb = JsonbBuilder.create(new JsonbConfig()
                    .withPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE));
        
        final String json = doc.toJson();
        
        switch(type) {
            case CommandLineTool.TYPE: return jsonb.fromJson(json, CommandLineTool.class);
            case WebApplication.TYPE: return jsonb.fromJson(json, WebApplication.class);
            case DatabasePortal.TYPE: return jsonb.fromJson(json, DatabasePortal.class);
            case DesktopApplication.TYPE: return jsonb.fromJson(json, DesktopApplication.class);
            case Library.TYPE: return jsonb.fromJson(json, Library.class);
            case Ontology.TYPE: return jsonb.fromJson(json, Ontology.class);
            case Workflow.TYPE: return jsonb.fromJson(json, Workflow.class);
            case Plugin.TYPE: return jsonb.fromJson(json, Plugin.class);
            case SPARQLEndpoint.TYPE: return jsonb.fromJson(json, SPARQLEndpoint.class);
            case SOAPServices.TYPE: return jsonb.fromJson(json, SOAPServices.class);
            case Script.TYPE: return jsonb.fromJson(json, Script.class);
            case WebAPI.TYPE: return jsonb.fromJson(json, WebAPI.class);
            case Workbench.TYPE: return jsonb.fromJson(json, Workbench.class);
            case Suite.TYPE: return jsonb.fromJson(json, Suite.class);
        }
        
        return jsonb.fromJson(json, Tool.class);
    }
    
    public String getJSON(String id) {
        final List<Document> docs = getBSON(id);
        
        if (docs.isEmpty()) {
            return null;
        }

        final Document doc = docs.get(0);
        final Document _id = (Document) doc.remove("_id");
        doc.append("@id", createID(_id));
        doc.append("@type", _id.getString("type"));
            
        return doc.toJson();        
    }
    
    public String getJSONArray(String uri) {
        final List<Document> docs = getBSON(uri);
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
    
    private List<Document> getBSON(String uri) {
        List<Document> list = new ArrayList<>();
        
        try {
            final MongoCollection<Document> col = database.getCollection(collection);

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
            Logger.getLogger(ToolsDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;        
    }
    
    public void put(String user, JsonObject json) {
        final String id = json.getString("@id");
        patch(user, id, json.toString());
    }

    public void put(String user, String id, String json) {
        patch(user, id, json);
    }

    public void put(String user, Tool tool) {
        
        final Jsonb jsonb = JsonbBuilder.create(new JsonbConfig()
                    .withPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE));
        final String json = jsonb.toJson(tool);
        
        patch(user, tool.id.toString(), json);
    }

    public String patch(String user, String id, String json) {
        final String src = put(id, json);
        log.log(user, id.substring(AUTHORITY.length()), src, json);

        return json;
    }
    
    public String patch(String user, String id, JsonPatch patch) {
        final String result = patch(id, patch);
        if (result != null) {
            log.log(user, id, patch);
        }
        return result;
    }
    
    public String patch(String id, JsonPatch patch) {
        try  {
            MongoCollection<Document> col = database.getCollection(collection);

            Bson pk = createPK(id);
            if (pk != null) {
                final Document doc = col.find(Filters.eq("_id", pk)).first();
                if (doc != null) {
                    final JsonObject target = Json.createReader(new StringReader(doc.toJson())).readObject();
                    final JsonObject patched = patch.apply(target);
                    final StringWriter writer = new StringWriter();
                    Json.createWriter(writer).writeObject(patched);

                    FindOneAndReplaceOptions opt = new FindOneAndReplaceOptions().returnDocument(ReturnDocument.AFTER);
                    Document result = col.findOneAndReplace(Filters.eq("_id", pk), Document.parse(writer.toString()), opt);

                    final Document _id = (Document) result.remove("_id");
                    result.append("@id", createID(_id));
                    result.append("@type", _id.getString("type"));

                    return result.toJson();
                }
            }
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ToolsDAO.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(ToolsDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private String put(String id, String json) {
        BasicDBObject pk = createPK(id);
        if (pk == null) {
            return null;
        }

        try {
            MongoCollection<Document> col = database.getCollection(COLLECTION);

            Document bson = Document.parse(json);

            // do not store @id and @type, but compound mongodb primary key
            bson.append("_id", pk);
            bson.remove("@id");
            bson.remove("@type");

            FindOneAndUpdateOptions opt = new FindOneAndUpdateOptions().upsert(true)
                .projection(Projections.excludeId()).returnDocument(ReturnDocument.BEFORE);

            Document doc = col.findOneAndUpdate(Filters.eq("_id", pk),
                    new Document("$set", bson), opt);

            if (doc == null) {
                return null;
            }

            doc.append("@id", id);
            doc.append("@type", pk.getString("type"));

            return doc.toJson();
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ToolsDAO.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(ToolsDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private Bson createFindQuery(String id) {
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
    
    /**
     * Create a primary key from the tool URI
     * 
     * @param uri
     * @return the primary key for the tool
     */
    private BasicDBObject createPK(String uri) {
        final String path = URI.create(uri).getPath();
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
    
    private String createID(Document _id) {
        StringBuilder sb = new StringBuilder(AUTHORITY);
        
        sb.append(_id.getString("id")).append('/');
        sb.append(_id.getString("type")).append('/');
        sb.append(_id.getString("host"));
        
        return sb.toString();
    }
    
    /**
     * Find tools and write them into the reader.
     * 
     * @param writer writer to write metrics into.
     * @param skip mongodb skip parameter (aka from).
     * @param limit mongodb limit parameter (limits number of tools to be written).
     * @param text text to search.
     * @param projections - properties to write or null for all.
     */
    public void write(Writer writer, Integer skip, Integer limit, String text, List<String> projections) {
        try {
            final MongoCollection<Document> col = database.getCollection(collection);
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
                
                FindIterable<Document> iterator = text == null || text.isEmpty() ? col.find() :
                        col.find(Filters.or(Filters.regex("description", text, "i"),
                                            Filters.regex("name", text, "i")));

                iterator = iterator.sort(new BasicDBObject("name", 1));
                
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
            Logger.getLogger(ToolsDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Write tools ids those semantic annotations found in the map.
     * 
     * @param writer the writer to write ids.
     * @param map the map that has semantic ids as its keys.
     */
    public void filter(Writer writer, Map<String, List<Map.Entry<String, String>>> map) {

        final MongoCollection<Document> col = database.getCollection(COLLECTION);

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
            Logger.getLogger(ToolsDAO.class.getName()).log(Level.SEVERE, null, ex);
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
    private boolean writeId(Writer writer, List list, String id, boolean first) throws IOException {
        if (list != null && list.size() > 0 && (first || writer.append(",\n") != null)) {
            writer.append("{\"@id\":\"").append(id).append("\"}");
            return true;
        }
        return false;
    }    
}
