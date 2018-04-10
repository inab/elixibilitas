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
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
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
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.config.PropertyNamingStrategy;
import org.bson.BsonArray;
import org.bson.BsonNull;
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

public class ToolsDAO extends AbstractDAO<Document> implements Serializable {
    
    public final static String COLLECTION = "tools";
    
    public ToolsDAO(MongoDatabase database, String baseURI) {
        super(baseURI, database, COLLECTION);
    }

    /**
     * Create a primary key from the tool URI
     * 
     * @param id the tool identifier in a form "$nmsp:$id/$type/$authority"
     * @return the primary key for the tool
     */
    @Override
    protected Document createPK(String id) {
        final String[] nodes = id.split("/");
        if (nodes.length > 2) {
            final String[] _id = nodes[0].split(":");
            if (_id.length > 2) {
                return new Document("id", _id[1])
                    .append("nmsp", _id[0])
                    .append("version", _id[2])
                    .append("type", nodes[1])
                    .append("host", nodes[2]);                
            } else if (_id.length > 1) {
                return new Document("id", _id[1])
                    .append("nmsp", _id[0])
                    .append("type", nodes[1])
                    .append("host", nodes[2]);
            }

        }
        return null;
    }

    @Override
    protected String getURI(Document pk) {

        StringBuilder sb = new StringBuilder(baseURI);
        
        sb.append(pk.get("nmsp")).append(':');
        sb.append(pk.get("id"));
        
        final String version = pk.get("version", String.class);
        if (version != null && !version.isEmpty()) {
            sb.append(':').append(version);
        }

        sb.append('/');
        sb.append(pk.get("type")).append('/');
        sb.append(pk.get("host"));
        
        
        return sb.toString();
    }

    @Override
    protected String getType(Document pk) {
        return pk.getString("type");
    }

    @Override
    protected String getLabel(Document pk) {
        return pk.getString("id");
    }
    
    @Override
    protected String getVersion(Document pk) {
        return pk.getString("version");
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
        final Document _id = (Document)doc.remove("_id");
        final String type = getType(_id);

        doc.append("@id", getURI(_id));
        doc.append("@type", type);
        doc.append("@label", getLabel(_id));
        doc.append("@version", getVersion(_id));
                        
        final Jsonb jsonb = JsonbBuilder.create(
                new JsonbConfig().withPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE));
        
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
       
        return docs.get(0).toJson();
    }
    
    public String getTools(String id) {
        final List<Document> docs = getBSON(id);
        if (docs.isEmpty()) {
            return null;
        }
                        
        final Iterator<Document> iter = docs.iterator();

        final StringBuilder sb = new StringBuilder();
        
        sb.append('[');
        do {
            final Document doc = iter.next();
            sb.append(doc.toJson());
        } while (iter.hasNext() && sb.append(',') != null);
        sb.append(']');
        
        return sb.toString();        
    }

    public JsonArray getJSONArray(String id) {
        final JsonArrayBuilder builder = Json.createArrayBuilder();
        
        final List<Document> docs = getBSON(id);
        for (Document doc : docs) {
            JsonObjectBuilder ob = Json.createObjectBuilder(doc);
            builder.add(ob);
        }

        return builder.build();
    }

    private List<Document> getBSON(String id) {
        List<Document> list = new ArrayList<>();
        
        try {
            final MongoCollection<Document> col = database.getCollection(collection);

            final Bson query = createFindQuery(id);
            if (query != null) {
                try (MongoCursor<Document> cursor = col.find(query).iterator()) {
                    while(cursor.hasNext()) {
                        Document doc = cursor.next();
                        final Document _id = (Document)doc.remove("_id");
                        doc.append("@id", getURI(_id));
                        doc.append("@type", getType(_id));
                        doc.append("@label", getLabel(_id));
                        doc.append("@version", getVersion(_id));
                        doc.append("@license", LICENSE);
                        list.add(doc);
                    }
                }
            }
        } catch(Exception ex) {
            Logger.getLogger(ToolsDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;        
    }

    public String put(String user, Tool tool) {
        final Jsonb jsonb = JsonbBuilder.create(
                new JsonbConfig().withPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE));
        final String json = jsonb.toJson(tool);
        
        return put(user, tool.id.toString().substring(baseURI.length()), json);
    }


    public String update(String user, Tool tool, String id) {
        final Jsonb jsonb = JsonbBuilder.create(
                new JsonbConfig().withPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE));
        final String json = jsonb.toJson(tool);
        
        return update(user, id, json);
    }

    private Bson createFindQuery(String id) {
        final String[] nodes = id.split("/");
        if (nodes.length > 0) {
            final String[] _id = nodes[0].split(":");
            if (_id.length > 2) {
                if (nodes.length > 2) {
                    return Filters.eq("_id",
                        new BasicDBObject("id", _id[1])
                        .append("nmsp", _id[0])
                        .append("version", _id[2])
                        .append("type", nodes[1])
                        .append("host", nodes[2]));
                }
                if (nodes.length > 1) {
                    return Filters.and(Filters.eq("_id.id", _id[1]),
                            Filters.eq("_id.nmsp", _id[0]),
                            Filters.eq("_id.version", _id[2]),
                            Filters.eq("_id.type", nodes[1]));
                }
                return Filters.and(Filters.eq("_id.nmsp", _id[0]), Filters.eq("_id.id", _id[1]), Filters.eq("_id.version", _id[2]));
           } else if (_id.length > 1) {
                if (nodes.length > 2) {
                    return Filters.and(Filters.eq("_id.id", _id[1]),
                                Filters.eq("_id.nmsp", _id[0]),
                                Filters.eq("_id.type", nodes[1]),
                                Filters.eq("_id.host", nodes[2]));
                }
                if (nodes.length > 1) {
                    return Filters.and(Filters.eq("_id.id", _id[1]),
                            Filters.eq("_id.nmsp", _id[0]),
                            Filters.eq("_id.type", nodes[1]));
                }
                return Filters.and(Filters.eq("_id.id", _id[1]), Filters.eq("_id.nmsp", _id[0]));
           } else {
               return Filters.eq("_id.id", _id[0]);
           }
        }
        
        return null;
    }
    
    public int search_count(String id, String text, String name, String description) {
        final MongoCollection<Document> col = database.getCollection(collection);

        ArrayList<Bson> aggregation = new ArrayList();
        if (text != null && !text.isEmpty()) {
            aggregation.add(Aggregates.match(Filters.or(Filters.regex("description", text, "i"),
                                    Filters.regex("name", text, "i"))));
        }

        if (name != null && !name.isEmpty()) {
            aggregation.add(Aggregates.match(Filters.regex("name", name, "i")));
        }

        if (description != null && !description.isEmpty()) {
            aggregation.add(Aggregates.match(Filters.regex("description", description, "i")));
        }

        if (id != null && !id.isEmpty()) {
            aggregation.add(createIdFilter(id));
        }

        aggregation.add(Aggregates.group(new BasicDBObject("_id", "$_id.id"), Accumulators.push("tools", "$$ROOT")));

        aggregation.add(Aggregates.sort(Sorts.ascending("tools.name")));

        aggregation.add(Aggregates.unwind("$tools"));
        aggregation.add(Aggregates.replaceRoot("$tools"));
        aggregation.add(Aggregates.group(new BasicDBObject("_id", BsonNull.VALUE),
                Accumulators.sum("count", 1)));

        final AggregateIterable<Document> iterator = col.aggregate(aggregation);
        final Document doc = iterator.first();
        
        return doc.get("count", Integer.class);
    }
    
    public void write(Writer writer) {
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
                
                FindIterable iterator = col.find();

                try (MongoCursor<Document> cursor = iterator.iterator()) {
                    if (cursor.hasNext()) {
                        do {
                            final Document doc = cursor.next();

                            Document _id = (Document) doc.remove("_id");
                            doc.append("@id", getURI(_id));
                            doc.append("@type", getType(_id));
                            doc.append("@label", getLabel(_id));
                            doc.append("@version", getVersion(_id));
                            doc.append("@license", LICENSE);

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
     * Find tools and write them into the reader.
     * 
     * @param writer writer to write metrics into.
     * @param id
     * @param skip mongodb skip parameter (aka from).
     * @param limit mongodb limit parameter (limits number of tools to be written).
     * @param text text to search either in 'name' or 'description' property.
     * @param name text to search in the 'name' property.
     * @param description text to search in the 'descriptino' property.
     * @param projections - properties to write or null for all.
     */
    public void search(Writer writer, String id, Long skip, Long limit, 
            String text, String name, String description, List<String> projections) {
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
                
                ArrayList<Bson> aggregation = new ArrayList();
                if (text != null && !text.isEmpty()) {
                    aggregation.add(Aggregates.match(Filters.or(Filters.regex("description", text, "i"),
                                            Filters.regex("name", text, "i"))));
                }

                if (name != null && !name.isEmpty()) {
                    aggregation.add(Aggregates.match(Filters.regex("name", name, "i")));
                }

                if (description != null && !description.isEmpty()) {
                    aggregation.add(Aggregates.match(Filters.regex("description", description, "i")));
                }
                
                if (id != null && !id.isEmpty()) {
                    aggregation.add(createIdFilter(id));
                }

                if (projections != null && projections.size() > 0) {
                    BasicDBObject bson = new BasicDBObject();
                    bson.append("@timestamp", true);
                    for (String field : projections) {
                        bson.append(field, true);
                    }
                    aggregation.add(Aggregates.project(bson));
                }

                aggregation.add(Aggregates.group(new BasicDBObject("_id", "$_id.id"), Accumulators.push("tools", "$$ROOT")));

                aggregation.add(Aggregates.sort(Sorts.ascending("tools.name")));
                
                if (skip != null) {
                    aggregation.add(Aggregates.skip(skip.intValue()));
                }
                if (limit != null) {
                    aggregation.add(Aggregates.limit(limit.intValue()));
                }

                aggregation.add(Aggregates.unwind("$tools"));
                aggregation.add(Aggregates.replaceRoot("$tools"));
                AggregateIterable<Document> iterator = col.aggregate(aggregation).useCursor(true);

                try (MongoCursor<Document> cursor = iterator.iterator()) {
                    if (cursor.hasNext()) {
                        do {
                            final Document doc = cursor.next();

                            Document _id = (Document) doc.remove("_id");
                            doc.append("@id", getURI(_id));
                            doc.append("@type", getType(_id));
                            doc.append("@label", getLabel(_id));
                            doc.append("@version", getVersion(_id));
                            doc.append("@license", LICENSE);

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
    
    public int aggregate_count(String id, String text, String name, String description) {

        final MongoCollection<Document> col = database.getCollection(collection);
        final ArrayList<Bson> aggregation = new ArrayList();
        if (text != null && !text.isEmpty()) {
            aggregation.add(Aggregates.match(Filters.or(Filters.regex("description", text, "i"),
                                    Filters.regex("name", text, "i"))));
        }

        if (name != null && !name.isEmpty()) {
            aggregation.add(Aggregates.match(Filters.regex("name", name, "i")));
        }

        if (description != null && !description.isEmpty()) {
            aggregation.add(Aggregates.match(Filters.regex("description", description, "i")));
        }


        if (id != null && !id.isEmpty()) {
            aggregation.add(createIdFilter(id));
        }

        aggregation.add(Aggregates.group(new BasicDBObject("_id", "$_id.id")));
        aggregation.add(Aggregates.group(new BasicDBObject("_id", BsonNull.VALUE),
                Accumulators.sum("count", 1)));

        final AggregateIterable<Document> iterator = col.aggregate(aggregation);
        final Document doc = iterator.first();
        
        return doc.get("count", Integer.class);
        
    }
        
    public void aggregate(Writer writer, String id, Long skip, 
            Long limit, String text, String name, String description,
            List<String> projections) {
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

                ArrayList<Bson> aggregation = new ArrayList();
                if (text != null && !text.isEmpty()) {
                    aggregation.add(Aggregates.match(Filters.or(Filters.regex("description", text, "i"),
                                            Filters.regex("name", text, "i"))));
                }
                
                if (name != null && !name.isEmpty()) {
                    aggregation.add(Aggregates.match(Filters.regex("name", name, "i")));
                }
                
                if (description != null && !description.isEmpty()) {
                    aggregation.add(Aggregates.match(Filters.regex("description", description, "i")));
                }

                if (id != null && !id.isEmpty()) {
                    aggregation.add(createIdFilter(id));
                }

                if (projections != null && projections.size() > 0) {
                    BasicDBObject bson = new BasicDBObject();
                    bson.append("@timestamp", true);
                    for (String field : projections) {
                        bson.append(field, true);
                    }
                    aggregation.add(Aggregates.project(bson));
                }
                aggregation.add(Aggregates.group(new BasicDBObject("_id", "$_id.id"),
                        Accumulators.push("entries", "$$ROOT")));
                
                aggregation.add(Aggregates.sort(Sorts.ascending("entries.name")));

                
                if (skip != null) {
                    aggregation.add(Aggregates.skip(skip.intValue()));
                }
                if (limit != null) {
                    aggregation.add(Aggregates.limit(limit.intValue()));
                }

                AggregateIterable<Document> iterator = col.aggregate(aggregation).useCursor(true);

                try (MongoCursor<Document> cursor = iterator.iterator()) {
                    if (cursor.hasNext()) {
                        do {
                            final Document doc = cursor.next();

                            writer.append("{\"id\": \"");
                            final String tid = doc.get("_id", Document.class).get("_id", String.class);
                            writer.append(tid);
                            writer.append("\",\n\"entities\": [");
                            
                            Map<String, List<Document>> map = new TreeMap<>();
                            
                            List<Document> tools = doc.get("entries", List.class);
                            for (Document tool : tools) {
                                Document _id = (Document) tool.remove("_id");
                                tool.append("@id", getURI(_id));
                                tool.append("@type", getType(_id));
                                tool.append("@label", getLabel(_id));
                                tool.append("@version", getVersion(_id));
                                tool.append("@license", LICENSE);
                                
                                final String type = getType(_id);
                                List<Document> list = map.get(type);
                                if (list == null) {
                                    map.put(type, list = new ArrayList<>());
                                }
                                list.add(tool);
                            }

                            // we must have at least one entity (otherwise entire group would be empty)
                            Iterator<Map.Entry<String, List<Document>>> groups = map.entrySet().iterator();
                            do {
                                final Map.Entry<String, List<Document>> entry = groups.next();
                                writer.append("{ \"type\": \"");
                                writer.append(entry.getKey());
                                writer.append("\",\n\"tools\":[");
                            
                                final Iterator<Document> iter = entry.getValue().iterator();
                                do {
                                    iter.next().toJson(codec);
                                    jwriter.flush();
                                } while (iter.hasNext() && writer.append(",\n") != null);
                                writer.append("]\n}");
                            } while (groups.hasNext() && writer.append(",\n") != null);
                            writer.append("]\n}");
                            
                        } while (cursor.hasNext() && writer.append(",\n") != null);
                    }
                }
                writer.write("]\n");
            }

        } catch(IOException ex) {
            Logger.getLogger(ToolsDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Bson createIdFilter(String id) {
        final String[] nodes = id.split(":", -1);
        if (nodes.length > 2) {
            if (nodes[0].isEmpty()) {
                return Aggregates.match(
                    Filters.and(Filters.eq("_id.id", nodes[1]),
                        Filters.eq("_id.version", nodes[2])));

            } else if (nodes[1].isEmpty()) {
                return Aggregates.match(Filters.eq("_id.nmsp", nodes[0]));
            } else {
                return Aggregates.match(
                    Filters.and(Filters.eq("_id.nmsp", nodes[0]),
                        Filters.eq("_id.id", nodes[1]),
                        Filters.eq("_id.version", nodes[2])));
            }
        } else if (nodes.length > 1) {
            if (nodes[1].isEmpty()) {
                return Aggregates.match(Filters.eq("_id.nmsp", nodes[0]));
            } else {
                return Aggregates.match(
                            Filters.and(Filters.eq("_id.nmsp", nodes[0]),
                                        Filters.eq("_id.id", nodes[1])));
            }
        } else {
            return Aggregates.match(Filters.eq("_id.id", nodes[0]));
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
                final String id = getURI((Document) doc.remove("_id"));

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
    
    public long count(String field, String text) {
        final MongoCollection<Document> col = database.getCollection(COLLECTION);
        return col.count(Filters.and(
                Filters.exists(field, true), 
                Filters.ne(field, new BsonArray()),
                Filters.regex(field, Pattern.compile(text == null ? "" : text))));
    }
}
