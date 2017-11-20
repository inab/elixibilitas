package es.elixir.bsc.elixibilitas.dao;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.ReturnDocument;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonPatch;
import org.bson.Document;

/**
 * @author Dmitry Repchevsky
 * 
 * @param <T> BSON type of the primary key
 */

public abstract class AbstractDAO<T> {
    
    public final String baseURI;
    protected final MongoDatabase database;
    protected final String collection;
    protected final JsonLog log;
    
    public AbstractDAO(final String baseURI,
                       final MongoDatabase database,
                       final String collection) {
    
        this.baseURI = baseURI;
        this.database = database;
        this.collection = collection;
        log = new JsonLog(database, collection + ".log");
    }
    
    protected abstract T createPK(String uri);
    protected abstract String getURI(T pk);
    protected abstract String getType(T pk);
    
    public long count() {
        return database.getCollection(collection).count();
    }

    public long count(String query) {
        final MongoCollection<Document> col = database.getCollection(collection);
        return col.count(Document.parse(query));
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
            bson.remove("@id");
            bson.remove("@type");
            
            FindOneAndReplaceOptions opt = new FindOneAndReplaceOptions().upsert(false).
                    projection(Projections.excludeId()).returnDocument(ReturnDocument.BEFORE);
            
            Document doc = col.findOneAndReplace(Filters.eq("_id", pk), bson, opt);
            if (doc != null) {
                // add @id and @type to both, "before" and "after", 
                // so log have no these properties.

                final String uri = getURI(pk);
                final String type = getType(pk);

                doc.append("@id", uri);
                doc.append("@type", type);

                bson.append("@id", uri);
                bson.append("@type", type);
                
                final String result = bson.toJson();
                log.log(user, id, doc.toJson(), result);
                return result;
            }
        } catch(Exception ex) {
            Logger.getLogger(AbstractDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public void update(String user, JsonObject json) {
        final String id = json.getString("@id");
        update(user, id, json.toString());
    }

    /**
     * Updates the document using mongodb 'upsert' operation.
     * 
     * @param user origin of the update operation
     * @param id id of the document
     * @param json Json document to update
     * 
     * @return 
     */
    public String update(String user, String id, String json) {
        try {
            MongoCollection<Document> col = database.getCollection(collection);

            Document bson = Document.parse(json);

            final T pk = createPK(id);
            bson.append("_id", pk);

            bson.remove("@id");
            bson.remove("@type");
            
            Document before = col.find(Filters.eq("_id", pk)).projection(Projections.excludeId()).first();

            FindOneAndUpdateOptions opt = new FindOneAndUpdateOptions().upsert(true)
                    .projection(Projections.excludeId()).returnDocument(ReturnDocument.AFTER);
            
            final Document after = col.findOneAndUpdate(Filters.eq("_id", pk),
                                        new Document("$set", bson), opt);

            if (after != null) {
                final String uri = getURI(pk);
                final String type = getType(pk);

                if (before == null) {
                    before = new Document();
                }
                
                before.append("@id", uri);
                before.append("@type", type);


                after.append("@id", uri);
                after.append("@type", type);

                final String result = after.toJson();
                log.log(user, id, before.toJson(), result);
                return result;
            }
        } catch(Exception ex) {
            Logger.getLogger(MetricsDAO.class.getName()).log(Level.SEVERE, null, ex);
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
     * Apply Json patch to the mongodb metrics document.
     * 
     * @param id metrics id
     * @param patch Json patch to apply
     * 
     * @return resulted Json metrics document.
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

                FindOneAndReplaceOptions opt = new FindOneAndReplaceOptions()
                        .projection(Projections.excludeId())
                        .returnDocument(ReturnDocument.AFTER);
                Document result = col.findOneAndReplace(Filters.eq("_id", pk), Document.parse(writer.toString()), opt);

                result.append("@id", getURI(pk));
                result.append("@type", getType(pk));

                return result.toJson();
            }
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(MetricsDAO.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(MetricsDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
