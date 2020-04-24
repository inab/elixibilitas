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
import es.bsc.inb.elixir.openebench.model.tools.Tool;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import org.bson.BsonArray;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;
import org.bson.conversions.Bson;
import org.bson.json.JsonWriter;

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
        final Document document;
        
        final String[] nodes = id.split("/");
        
        final String[] _id = nodes[0].split(":");        
        if (_id.length > 2) {
            document = new Document("id", _id[1])
                    .append("nmsp", _id[0])
                    .append("version", _id[2]);              
        } else if (_id.length > 1) {
            document = new Document("id", _id[1])
                    .append("nmsp", _id[0]);
        } else {
            document = new Document("id", _id[0]);
        }

        if (nodes.length > 2) {
            document.append("type", nodes[1]).append("host", nodes[2]);
        } else if (nodes.length > 1) {
            document.append("type", nodes[1]);
        }

        return document;
    }

    @Override
    public String getURI(Document pk) {

        StringBuilder sb = new StringBuilder(baseURI);
        
        final String nmsp = pk.getString("nmsp");
        if (nmsp != null) {
            sb.append(nmsp).append(':');
        }
        
        sb.append(pk.get("id"));
        
        final String version = pk.get("version", String.class);
        if (version != null && !version.isEmpty()) {
            sb.append(':').append(version);
        }

        final String type = pk.getString("type");
        if (type != null) {
            sb.append('/').append(type);
        }
        final String host = pk.getString("host");
        if (host != null) {
            sb.append('/').append(host);
        }
        
        return sb.toString();
    }
    
    @Override
    public String getType(Document pk) {
        return pk.getString("type");
    }

    @Override
    public String getLabel(Document pk) {
        return pk.getString("id");
    }
    
    @Override
    public String getVersion(Document pk) {
        return pk.getString("version");
    }
    
    public List<Tool> get() {
        List<Tool> tools = new ArrayList<>();

        try {
            final MongoCollection<Document> col = database.getCollection(collection);
            final FindIterable<Document> iterator = col.find().noCursorTimeout(true);
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
    
    /**
     * Finds the tool by its ID.
     * 
     * @param id the tool ID
     * 
     * @return the Tool object
     */
    public Tool get(String id) {
        try {
            final MongoCollection<Document> col = database.getCollection(collection);

            final Bson query = createFindQuery(id);
            if (query != null) {
                final FindIterable<Document> iterator = col.find(query).noCursorTimeout(true);
                try (MongoCursor<Document> cursor = iterator.iterator()) {
                    if(cursor.hasNext()) {
                        return deserialize(cursor.next());
                    }
                }
            }
        } catch(Exception ex) {
            Logger.getLogger(ToolsDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
    
    private Tool deserialize(final Document doc) {
        final Document _id = (Document)doc.remove("_id");
        final String type = getType(_id);

        doc.append("@id", getURI(_id));
        doc.append("@type", type);
        doc.append("@label", getLabel(_id));
        doc.append("@version", getVersion(_id));
        
        try {
            final String json = doc.toJson();
            return jsonb.fromJson(json, Tool.class);
        } catch(Exception ex) {
            Logger.getLogger(ToolsDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
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
                final FindIterable<Document> iterator = col.find(query).noCursorTimeout(true);
                try (MongoCursor<Document> cursor = iterator.iterator()) {
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
        try {
            final String json = jsonb.toJson(tool);
            return put(user, tool.id.toString().substring(baseURI.length()), json);
        } catch(Exception ex) {
            Logger.getLogger(ToolsDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }


    public String upsert(String user, Tool tool, String id) {
        try {
            final String json = jsonb.toJson(tool);
            return upsert(user, id, json);
        } catch(Exception ex) {
            Logger.getLogger(ToolsDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
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
                return Filters.eq("_id",
                    new BasicDBObject("id", _id[0]));
           }
        }
        
        return null;
    }
    
    public void write(Writer writer) {
        try {
            final MongoCollection<Document> col = database.getCollection(collection);
            try (JsonWriter jwriter = new ReusableJsonWriter(writer)) {

                final DocumentCodec codec = new DocumentCodec() {
                    @Override
                    public void encode(BsonWriter writer,
                       Document document,
                       EncoderContext encoderContext) {
                            super.encode(jwriter, document, encoderContext);
                    }
                };

                jwriter.writeStartArray();
                
                final FindIterable iterator = col.find().noCursorTimeout(true);
                try (MongoCursor<Document> cursor = iterator.iterator()) {
                    while (cursor.hasNext()) {
                        final Document doc = cursor.next();

                        Document _id = (Document) doc.remove("_id");
                        doc.append("@id", getURI(_id));
                        doc.append("@type", getType(_id));
                        doc.append("@label", getLabel(_id));
                        doc.append("@version", getVersion(_id));
                        doc.append("@license", LICENSE);

                        doc.toJson(codec);
                    }
                }
                jwriter.writeEndArray();
            }
        } catch(Exception ex) {
            Logger.getLogger(ToolsDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public Bson createIdFilter(String id) {
        if ("::".equals(id)) {
            return Aggregates.match(Filters.and(Filters.eq("_id.nmsp", null),
                        Filters.eq("_id.version", null),
                        Filters.eq("_id.type", null)));
        }
        
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
    public void filter(Writer writer, Map<String, List<String[]>> map) {

        final MongoCollection<Document> col = database.getCollection(COLLECTION);

        final FindIterable<Document> iterator = col.find().noCursorTimeout(true)
                .projection(new BasicDBObject("semantics", true));
        try (MongoCursor<Document> cursor = iterator.iterator()) {

            boolean first = true;
            loop:
            while (cursor.hasNext()) {

                final Document doc = cursor.next();
                final String id = getURI((Document) doc.remove("_id"));

                final Document semantics = doc.get("semantics", Document.class);
                if (semantics != null) {                        
                    List<String> topics = semantics.get("topics", List.class);
                    if (topics != null) {
                        for (String topic : topics) {
                            final List<String[]> list = map.get(topic);
                            if (writeId(writer, list, id, first)) {
                                first = false;
                                continue loop;
                            }
                        }
                    }
                    List<String> operations = semantics.get("operations", List.class);
                    if (operations != null) {
                        for (String operation : operations) {
                            final List<String[]> list = map.get(operation);
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
                            List<String[]> list = map.get(datatype);
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
                            List<String[]> list = map.get(datatype);
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
        return col.countDocuments(Filters.and(
                Filters.exists(field, true), 
                Filters.ne(field, new BsonArray()),
                Filters.regex(field, Pattern.compile(text == null ? "" : text))));
    }
    
    public void group(final Writer writer, final String id) {
        try {
            final MongoCollection<Document> col = database.getCollection(collection);
            final ArrayList<Bson> aggregation = new ArrayList();

            if (id != null && !id.isEmpty()) {
                aggregation.add(createIdFilter(id));
            }
                
            aggregation.add(Aggregates.group(new BasicDBObject("_id", "$_id.id"), Accumulators.push("tools", "$$ROOT")));
            aggregation.add(Aggregates.sort(Sorts.ascending("tools.name")));
            aggregation.add(Aggregates.unwind("$tools"));
            aggregation.add(Aggregates.replaceRoot("$tools"));
            AggregateIterable<Document> iterator = col.aggregate(aggregation).allowDiskUse(true);

            try (MongoCursor<Document> cursor = iterator.iterator()) {
                while (cursor.hasNext()) {
                    final Document doc = cursor.next();
                    Document _id = (Document) doc.remove("_id");
                    writer.write(getURI(_id));
                    writer.write('\n');
                }
            }
        } catch(Exception ex) {
            Logger.getLogger(ToolsDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
