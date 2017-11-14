package es.elixir.bsc.elixibilitas.dao;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javax.json.JsonArray;
import org.bson.Document;

/**
 * @author Dmitry Repchevsky
 */

public class AbstractDAO {
    
    protected final MongoDatabase database;
    protected final String collection;
    protected final JsonLog log;
    
    public AbstractDAO(final MongoDatabase database,
                       final String collection) {
    
        this.database = database;
        this.collection = collection;
        log = new JsonLog(database, collection + ".log");
    }
    
    public long count() {
        return database.getCollection(collection).count();
    }

    public long count(String query) {
        final MongoCollection<Document> col = database.getCollection(collection);
        return col.count(Document.parse(query));
    }
    
    public JsonArray findLog(String id, String jpointer) {
        return log.findLog(id, jpointer);
    }
}
