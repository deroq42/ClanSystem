package de.deroq.clans.database;

import com.google.common.util.concurrent.ListenableFuture;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.sql.ResultSet;
/**
 * @author Miles
 * @since 08.12.2022
 */
public interface DatabaseConnector {

    void connect();

    void disconnect();

    MySQL getMySQL();

    Mongo getMongo();

    interface MySQL {

        ListenableFuture<ResultSet> query(String query, Object... objects);

        ListenableFuture<Boolean> update(String query, Object... objects);
    }

    interface Mongo {

        ListenableFuture<Boolean> insert(MongoCollection<Document> collection, Bson filter, Document document);

        ListenableFuture<Document> query(MongoCollection<Document> collection, Bson filter);

        MongoCollection<Document> getCollection(String name);
    }
}
