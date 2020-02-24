package es.elixir.bsc.elixibilitas.dao;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.ReturnDocument;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonPatch;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.config.PropertyNamingStrategy;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.json.JsonWriter;
import org.bson.json.JsonWriterSettings;

/**
 * @author Dmitry Repchevsky
 * 
 * @param <T> BSON type of the primary key
 */

public abstract class AbstractDAO<T> {
    
    public final static String LICENSE = "https://creativecommons.org/licenses/by/4.0/";

    protected Jsonb jsonb;
    
    public final String baseURI;
    public final MongoDatabase database;
    public final String collection;
    protected final JsonLog log;
    
    public AbstractDAO(final String baseURI,
                       final MongoDatabase database,
                       final String collection) {
    
        this.baseURI = baseURI;
        this.database = database;
        this.collection = collection;
        log = new JsonLog(database, collection + ".log");
        
        jsonb = JsonbBuilder.create(new JsonbConfig()
                .withPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE));
    }
    
    protected abstract T createPK(String uri);
    protected abstract String getURI(T pk);
    protected abstract String getType(T pk);
    protected abstract String getLabel(T pk);
    protected abstract String getVersion(T pk);
    
    public long count() {
        return database.getCollection(collection).count();
    }

    public long count(String query) {
        final MongoCollection<Document> col = database.getCollection(collection);
        return col.countDocuments(Document.parse(query));
    }
    
    public JsonArray findLog(String id, String jpointer, String from, String to, Integer limit) {
        return log.findLog(id, jpointer, from, to, limit);
    }
    
    /**
     * Inserts or replaces mongodb document in the collection
     * 
     * @param user origin of the update operation (i.e. "biotools")
     * @param id id of the document
     * @param json Json document to insert
     * 
     * @return resulted Json document
     */
    public String put(String user, String id, String json) {
        try {
            MongoCollection<Document> col = database.getCollection(collection);

            Document bson = Document.parse(json);

            final T pk = createPK(id);
            bson.append("_id", pk);
            
            final String timestamp = ZonedDateTime.now(ZoneId.of("Z")).toString();
            bson.append("@timestamp", timestamp);

            bson.remove("@id");
            bson.remove("@type");
            bson.remove("@label");
            bson.remove("@version");
            
            FindOneAndReplaceOptions opt = new FindOneAndReplaceOptions().upsert(true).
                    projection(Projections.excludeId()).returnDocument(ReturnDocument.BEFORE);
            
            Document doc = col.findOneAndReplace(Filters.eq("_id", pk), bson, opt);
            if (doc == null) {
                doc = new Document();
            }
            
            bson.remove("_id");

            // add @id, @type and @timestamp to both, "before" and "after", 
            // so log have no these properties.

            final String uri = getURI(pk);
            final String type = getType(pk);
            final String label = getLabel(pk);
            final String version = getVersion(pk);

            doc.append("@id", uri);
            doc.append("@type", type);
            doc.append("@label", label);
            doc.append("@version", version);
            doc.append("@license", LICENSE);
            doc.append("@timestamp", timestamp);

            bson.append("@id", uri);
            bson.append("@type", type);
            bson.append("@label", label);
            bson.append("@version", version);
            bson.append("@license", LICENSE);

            final String result = bson.toJson();
            log.log(user, id, doc.toJson(), result);
            return result;

        } catch(Exception ex) {
            Logger.getLogger(AbstractDAO.class.getName()).log(Level.SEVERE, id, ex);
        }
        return null;
    }
    
    public void upsert(String user, JsonObject json) {
        String id = json.getString("@id", null);
        if (id != null && id.startsWith(baseURI)) {
            id = id.substring(baseURI.length());
            AbstractDAO.this.upsert(user, id, json.toString());
        }
    }

    /**
     * Updates the document using mongodb 'upsert' operation.
     * 
     * @param user origin of the update operation
     * @param id ID of the document
     * @param json JSON document to update
     * 
     * @return updated JSON document or null if no update were performed.
     */
    public String upsert(String user, String id, String json) {
        try {
            MongoCollection<Document> col = database.getCollection(collection);

            Document bson = Document.parse(json);

            final String timestamp = bson.get("@timestamp", String.class);
            
            final T pk = createPK(id);
            bson.append("_id", pk);

            final String newTimestamp = ZonedDateTime.now(ZoneId.of("Z")).toString();
            bson.append("@timestamp", newTimestamp);
            
            bson.remove("@id");
            bson.remove("@type");
            bson.remove("@label");
            bson.remove("@version");
            
            Document before = col.find(Filters.eq("_id", pk)).projection(Projections.excludeId()).first();

            FindOneAndUpdateOptions opt = new FindOneAndUpdateOptions().upsert(true)
                    .projection(Projections.excludeId()).returnDocument(ReturnDocument.AFTER);
            
            Bson query = timestamp == null ? Filters.eq("_id", pk) :
                    Filters.and(Filters.eq("_id", pk), Filters.lte("@timestamp", timestamp));
            
            final Document after = col.findOneAndUpdate(query, new Document("$set", bson), opt);

            if (after != null) {
                final String uri = getURI(pk);
                final String type = getType(pk);
                final String label = getLabel(pk);
                final String version = getVersion(pk);

                if (before == null) {
                    before = new Document();
                }
                
                before.append("@id", uri);
                before.append("@type", type);
                before.append("@label", label);
                before.append("@version", version);
                before.append("@license", LICENSE);
                before.append("@timestamp", newTimestamp);

                after.append("@id", uri);
                after.append("@type", type);
                after.append("@label", label);
                after.append("@version", version);

                after.append("@license", LICENSE);

                final String result = after.toJson();
                log.log(user, id, before.toJson(), result);
                return result;
            }
        } catch(Exception ex) {
            Logger.getLogger(AbstractDAO.class.getName()).log(Level.SEVERE, id, ex);
        }
        return null;
    }
    
    public void merge(String user, JsonObject json) {
        String id = json.getString("@id", null);
        if (id != null && id.startsWith(baseURI)) {
            id = id.substring(baseURI.length());
            merge(user, id, json.toString());
        }
    }
    
    /**
     * Updates the document merging all properties.
     * 
     * @param user origin of the update operation
     * @param id ID of the document
     * @param json JSON document to update
     * 
     * @return updated JSON document or null if no update were performed.
     */
    public String merge(String user, String id, String json) {
        try {
            MongoCollection<Document> col = database.getCollection(collection);

            Document bson = Document.parse(json);

            final T pk = createPK(id);
            
            final String timestamp = ZonedDateTime.now(ZoneId.of("Z")).toString();
            bson.append("@timestamp", timestamp);

            bson.remove("@id");
            bson.remove("@type");
            bson.remove("@label");
            bson.remove("@version");

            Document doc = col.find(Filters.eq("_id", pk))
                    .projection(Projections.excludeId()).first();
            
            Document before;
            
            if (doc == null) {
                before = new Document();
                FindOneAndUpdateOptions opt = new FindOneAndUpdateOptions().upsert(true)
                    .projection(Projections.excludeId()).returnDocument(ReturnDocument.AFTER);
                doc = col.findOneAndUpdate(Filters.eq("_id", pk), new Document("$set", bson), opt);
            } else {
                merge(doc, bson);
                
                FindOneAndReplaceOptions opt = new FindOneAndReplaceOptions()
                        .projection(Projections.excludeId()).returnDocument(ReturnDocument.BEFORE);
                before = col.findOneAndReplace(Filters.eq("_id", pk), doc, opt);
                if (before == null) {
                    before = new Document();
                }
            }
            
            final String uri = getURI(pk);
            final String type = getType(pk);
            final String label = getLabel(pk);
            final String version = getVersion(pk);

            before.append("@id", uri);
            before.append("@type", type);
            before.append("@label", label);
            before.append("@version", version);
            before.append("@license", LICENSE);
            before.append("@timestamp", timestamp);

            doc.append("@id", uri);
            doc.append("@type", type);
            doc.append("@label", label);
            doc.append("@version", version);

            doc.append("@license", LICENSE);

            final String result = doc.toJson();
            log.log(user, id, before.toJson(), result);
            return result;

        } catch (IllegalArgumentException ex) {
            Logger.getLogger(AbstractDAO.class.getName()).log(Level.SEVERE, id, ex);
        } catch(Exception ex) {
            Logger.getLogger(AbstractDAO.class.getName()).log(Level.SEVERE, id, ex);
        }
        return null;
    }
    
    public String patch(String user, String id, JsonPatch patch) {
        final String result = patch(id, patch);
        if (result != null) {
            log.log(user, id, patch);
        }
        return result;
    }

    /**
     * Apply Json patch to the mongodb document.
     * 
     * @param id document id
     * @param patch Json patch to apply
     * 
     * @return resulted Json document document.
     */
    private String patch(String id, JsonPatch patch) {
        try  {
            MongoCollection<Document> col = database.getCollection(collection);

            final T pk = createPK(id);
            final Document doc = col.find(Filters.eq("_id", pk))
                    .projection(Projections.excludeId()).first();
            if (doc != null) {
                final JsonObject target = Json.createReader(new StringReader(doc.toJson())).readObject();
                final JsonObject patched = patch.apply(target);
                
                final StringWriter writer = new StringWriter();
                Json.createWriter(writer).writeObject(patched);

                Document patchedDoc = Document.parse(writer.toString());

                final String timestamp = ZonedDateTime.now(ZoneId.of("Z")).toString();
                patchedDoc.append("@timestamp", timestamp);
                
                FindOneAndReplaceOptions opt = new FindOneAndReplaceOptions()
                        .projection(Projections.excludeId())
                        .returnDocument(ReturnDocument.AFTER);
                Document result = col.findOneAndReplace(Filters.eq("_id", pk), patchedDoc, opt);

                result.append("@id", getURI(pk));
                result.append("@type", getType(pk));
                result.append("@label", getLabel(pk));
                result.append("@version", getVersion(pk));

                result.append("@license", LICENSE);

                return result.toJson();
            }
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(AbstractDAO.class.getName()).log(Level.SEVERE, id, ex);
        } catch(Exception ex) {
            Logger.getLogger(AbstractDAO.class.getName()).log(Level.SEVERE, id, ex);
        }
        return null;
    }
    
    /**
     * Deep documents merge
     * 
     * @param doc1 original document
     * @param doc2 merging document
     */
    private void merge(Map<String, Object> doc1, Map<String, Object> doc2) {
        for (Document.Entry<String, Object> entry : doc2.entrySet()) {
            final String key = entry.getKey();
            final Object val2 = entry.getValue();
            final Object val1 = doc1.get(key);
            if (val1 != null) {
                if (val1 instanceof Map && val2 instanceof Map) {
                    merge((Map<String, Object>)val1, (Map<String, Object>)val2);
                    continue;
                }
                if (val1 instanceof List && val2 instanceof List) {
                    final List list1 = (List)val1;
                    final List list2 = (List)val2;
                    for (Object val : list2) {
                        if (val instanceof Map || val instanceof List) {
                            // if the list has objects - replace the entire list
                            doc1.put(key, val2);
                            break;
                        }
                        if (!list1.contains(val)) {
                            list1.add(val);
                        }
                    }
                    continue;
                }    
            }
            doc1.put(key, val2);
        }
    }
    
    public <U extends Writer> U statistics(U writer) {
        final HashMap<String, HashSet<String>> map = new HashMap<>();
        
        try {
            final MongoCollection<Document> col = database.getCollection(collection);
            final FindIterable<Document> itarable = col.find().noCursorTimeout(true);
            try (MongoCursor<Document> cursor = itarable.iterator()) {
                while (cursor.hasNext()) {
                    final Document doc = cursor.next();
                    final T _id = (T)doc.remove("_id");
                    final String id = getLabel(_id);
                    HashSet<String> metrics = map.get(id);
                    if (metrics == null) {
                        map.put(id, metrics = new HashSet<>());
                    }
                    final JsonObject obj = Json.createObjectBuilder(doc).build();
                    travers(metrics, "", obj);
                }
            }
        }
        catch(Exception ex) {
            Logger.getLogger(AbstractDAO.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

        int max = 0;
        final TreeMap<String, AtomicInteger> metrics = new TreeMap<>();
        for (HashSet<String> set : map.values()) {
            for (String path : set) {
                final AtomicInteger count = metrics.get(path);
                if (count == null) {
                    metrics.put(path, new AtomicInteger(1));
                    max = Math.max(max, 1);
                } else {
                    max = Math.max(max, count.addAndGet(1));
                }
            }
        }
        
        try (JsonWriter jwriter = new ReusableJsonWriter(writer)) {
            jwriter.writeStartDocument();
            
            for (Map.Entry<String, AtomicInteger> entry : metrics.entrySet()) {
                final String path = entry.getKey();
                final int count = path.endsWith(":null") ? max - entry.getValue().get() : entry.getValue().get();
                jwriter.writeInt32(path, count);
            }
            jwriter.writeEndDocument();
        } catch (Exception ex) {
            Logger.getLogger(AbstractDAO.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return writer;
    }
    
    private void travers(HashSet<String> metrics, String path, JsonValue value) {
        final JsonValue.ValueType type = value.getValueType();
        if (type == JsonValue.ValueType.OBJECT) {
            final JsonObject object = value.asJsonObject();
            for (Entry<String, JsonValue> entry : object.entrySet()) {
                travers(metrics, path + "/" + entry.getKey(), entry.getValue());
            }
        } else if (type == JsonValue.ValueType.ARRAY){
            final JsonArray array = value.asJsonArray();
            if (!array.isEmpty()) {
                metrics.add(path);
            }
        } else if (type != JsonValue.ValueType.NULL) {
            if (type == JsonValue.ValueType.NUMBER ||
                      (value instanceof JsonString && 
                      ((JsonString)value).getChars().length() > 0)) {
                metrics.add(path);
            } else if (type == JsonValue.ValueType.TRUE) {
                metrics.add(path + ":true");
            } else if (type == JsonValue.ValueType.FALSE) {
                metrics.add(path + ":false");
            } else if (type == JsonValue.ValueType.STRING) {
                metrics.add(path);
            }
            metrics.add(path + ":null"); // acually NOT null!!!
        }
    }
    
    public static class ReusableJsonWriter extends JsonWriter {
        
        public ReusableJsonWriter(Writer writer) {
            super(writer, JsonWriterSettings.builder().indent(true).build());
        }

        @Override
        protected boolean checkState(final State[] validStates) {
            return true;
        }
    }
}
