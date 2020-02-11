/**
 * *****************************************************************************
 * Copyright (C) 2019 ELIXIR ES, Spanish National Bioinformatics Institute (INB)
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
package es.elixir.bsc.openebench.query;

import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import es.elixir.bsc.elixibilitas.dao.AbstractDAO;
import static es.elixir.bsc.elixibilitas.dao.AbstractDAO.LICENSE;
import es.elixir.bsc.elixibilitas.dao.MetricsDAO;
import es.elixir.bsc.elixibilitas.dao.ToolsDAO;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.BsonNull;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;
import org.bson.conversions.Bson;
import org.bson.json.JsonWriter;

/**
 * @author Dmitry Repchevsky
 */

public class MongoQueries {
    
    public static int aggregateToolsCount(
            ToolsDAO toolsDAO, String id, String text, String name, 
            String description, List<String> types, String[] edam_terms) {
        return aggregateToolsCount(toolsDAO, id, text, name, description, null, types, edam_terms);
    }
    
    public static int aggregateToolsCount(
            ToolsDAO toolsDAO, String id, String text, String name, 
            String description, List<String> tags, List<String> types, String[] edam_terms) {

        final MongoCollection<Document> col = toolsDAO.database.getCollection(toolsDAO.collection);
        final ArrayList<Bson> aggregation = new ArrayList();
        
        aggregation.add(Aggregates.match(Filters.ne("vetoed", true)));
        
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
        
        if (tags != null && !tags.isEmpty()) {
            aggregation.add(Aggregates.match(Filters.in("tags", tags)));
        }
                
        if (edam_terms != null) {
            final int edam_prefix_length = "http://edamontology.org/".length();

            final String[] operations = Arrays.stream(edam_terms).filter(x -> x.startsWith("operation", edam_prefix_length)).toArray(String[]::new);
            if (operations.length > 0) {
                aggregation.add(Aggregates.match(Filters.in("semantics.operations", operations)));
            }

            final String[] topics = Arrays.stream(edam_terms).filter(x -> x.startsWith("topic", edam_prefix_length)).toArray(String[]::new);
            if (topics.length > 0) {
                aggregation.add(Aggregates.match(Filters.in("semantics.topics", topics)));
            }
        }
                
        if (types != null && !types.isEmpty()) {
            final List<Bson> tlist = new ArrayList<>();
            for (String type : types) {
                tlist.add(Filters.eq("_id.type", type));
            }
            aggregation.add(Aggregates.match(Filters.or(tlist)));
        }

        if (id != null && !id.isEmpty()) {
            aggregation.add(toolsDAO.createIdFilter(id));
        }

        aggregation.add(Aggregates.group(new BasicDBObject("_id", "$_id.id")));
        aggregation.add(Aggregates.group(new BasicDBObject("_id", BsonNull.VALUE),
                Accumulators.sum("count", 1)));

        final AggregateIterable<Document> iterator = col.aggregate(aggregation).allowDiskUse(true);
        final Document doc = iterator.first();
        if (doc != null) {
            final Integer count = doc.get("count", Integer.class);
            return count == null ? 0 : count;
        }
        return 0;
    }

    public static void aggregateTools(
            ToolsDAO toolsDAO, MetricsDAO metricsDAO,
            Writer writer, String id, Long skip, 
            Long limit, String text, String name, String description,
            List<String> types, List<String> projections, String[] edam_terms) {
        aggregateTools(toolsDAO, metricsDAO, writer, id, skip, limit, text, name, description, null, types, projections, edam_terms);
    }
    
    public static void aggregateTools(
            ToolsDAO toolsDAO, MetricsDAO metricsDAO,
            Writer writer, String id, Long skip, 
            Long limit, String text, String name, String description, List<String> tags,
            List<String> types, List<String> projections, String[] edam_terms) {
        try {
            final MongoCollection<Document> col = toolsDAO.database.getCollection(toolsDAO.collection);
            try (JsonWriter jwriter = new AbstractDAO.ReusableJsonWriter(writer)) {

                final DocumentCodec codec = new DocumentCodec() {
                    @Override
                    public void encode(BsonWriter writer,
                       Document document,
                       EncoderContext encoderContext) {
                            super.encode(jwriter, document, encoderContext);
                    }
                };

                jwriter.writeStartArray();

                ArrayList<Bson> aggregation = new ArrayList();
                
                aggregation.add(Aggregates.match(Filters.ne("vetoed", true)));
                
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
                
                if (tags != null && !tags.isEmpty()) {
                    aggregation.add(Aggregates.match(Filters.in("tags", tags)));
                }
                
                if (edam_terms != null) {
                    final int edam_prefix_length = "http://edamontology.org/".length();
                    
                    final String[] operations = Arrays.stream(edam_terms).filter(x -> x.startsWith("operation", edam_prefix_length)).toArray(String[]::new);
                    if (operations.length > 0) {
                        aggregation.add(Aggregates.match(Filters.in("semantics.operations", operations)));
                    }
                    
                    final String[] topics = Arrays.stream(edam_terms).filter(x -> x.startsWith("topic", edam_prefix_length)).toArray(String[]::new);
                    if (topics.length > 0) {
                        aggregation.add(Aggregates.match(Filters.in("semantics.topics", topics)));
                    }
                }

                if (types != null && !types.isEmpty()) {
                    final List<Bson> tlist = new ArrayList<Bson>();
                    for (String type : types) {
                        tlist.add(Filters.eq("_id.type", type));
                    }
                    aggregation.add(Aggregates.match(Filters.or(tlist)));
                }

                if (id != null && !id.isEmpty()) {
                    aggregation.add(toolsDAO.createIdFilter(id));
                }

                if (projections != null && projections.size() > 0) {
                    BasicDBObject bson = new BasicDBObject();
                    bson.append("name", true);
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

                AggregateIterable<Document> iterator = col.aggregate(aggregation).allowDiskUse(true);

                try (MongoCursor<Document> cursor = iterator.iterator()) {
                    if (cursor.hasNext()) {
                        do {
                            final Document doc = cursor.next();
                            
                            jwriter.writeStartDocument();
                            
                            final String tid = doc.get("_id", Document.class).get("_id", String.class);
                            jwriter.writeString("id", tid);
                            
                            final Document metrics = metricsDAO.getBSON(tid);
                            if (metrics != null) {
                                jwriter.writeName("metrics");
                                metrics.toJson(codec);
                            }
                            
                            jwriter.writeStartArray("entities");
                            
                            Map<String, List<Document>> map = new TreeMap<>();
                            
                            List<Document> tools = doc.get("entries", List.class);
                            for (Document tool : tools) {
                                Document _id = (Document) tool.remove("_id");
                                tool.append("@id", toolsDAO.getURI(_id));
                                tool.append("@type", toolsDAO.getType(_id));
                                tool.append("@label", toolsDAO.getLabel(_id));
                                tool.append("@version", toolsDAO.getVersion(_id));
                                tool.append("@license", LICENSE);
                                
                                final String type = toolsDAO.getType(_id);
                                List<Document> list = map.get(type != null ? type : "");
                                if (list == null) {
                                    map.put(type != null ? type : "", list = new ArrayList<>());
                                }
                                list.add(tool);
                            }

                            // we must have at least one entity (otherwise entire group would be empty)
                            Iterator<Map.Entry<String, List<Document>>> groups = map.entrySet().iterator();
                            do {
                                final Map.Entry<String, List<Document>> entry = groups.next();
                                final String type = entry.getKey();
                                jwriter.writeStartDocument();
                                
                                if (type.isEmpty()) {
                                    jwriter.writeNull("type");
                                } else {
                                    jwriter.writeString("type", type);
                                }
                                
                                jwriter.writeStartArray("tools");
                            
                                final Iterator<Document> iter = entry.getValue().iterator();
                                do {
                                    iter.next().toJson(codec);
                                } while (iter.hasNext());
                                jwriter.writeEndArray();
                                jwriter.writeEndDocument();
                            } while (groups.hasNext());
                            
                            jwriter.writeEndArray();
                            jwriter.writeEndDocument();
                            
                        } while (cursor.hasNext());
                    }
                }
                jwriter.writeEndArray();
            }

        } catch(Exception ex) {
            Logger.getLogger(ToolsDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
