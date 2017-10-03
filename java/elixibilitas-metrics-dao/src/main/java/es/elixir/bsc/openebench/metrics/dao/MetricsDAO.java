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

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.ReturnDocument;
import es.elixir.bsc.elixibilitas.model.metrics.Metrics;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.JsonbException;
import javax.json.bind.config.PropertyNamingStrategy;
import org.bson.Document;

/**
 * Utility class to get/put Metrics into MongoDB.
 * 
 * @author Dmitry Repchevsky
 */

public class MetricsDAO implements Serializable {
    
    public final static String AUTHORITY = "http://elixir.bsc.es/metrics/";
    
    public static List<Metrics> get(MongoClient mc) {
        List<Metrics> metrics = new ArrayList<>();

        try {
            final MongoDatabase db = mc.getDatabase("elixibilitas");
            final MongoCollection<Document> col = db.getCollection("metrics2");
            
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
            final MongoCollection<Document> col = db.getCollection("metrics2");

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
    
    public static String put(MongoClient mc, String id, Metrics metrics) {
        final Jsonb jsonb = JsonbBuilder.create(new JsonbConfig()
                    .withPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE));
        final String json = jsonb.toJson(metrics);
        
        return put(mc, id, json);
    }
    
    public static String put(MongoClient mc, String id, String json) {
        try {
            MongoDatabase db = mc.getDatabase("elixibilitas");
            MongoCollection<Document> col = db.getCollection("metrics2");

            FindOneAndUpdateOptions opt = new FindOneAndUpdateOptions().upsert(true)
                    .projection(Projections.excludeId()).returnDocument(ReturnDocument.AFTER);

            Document bson = Document.parse(json);
            bson.append("_id", id);
            
            Document doc = col.findOneAndUpdate(Filters.eq("_id", id),
                    new Document("$set", bson), opt);

            return doc.toJson();
        } catch(Exception ex) {
            Logger.getLogger(MetricsDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
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
}
