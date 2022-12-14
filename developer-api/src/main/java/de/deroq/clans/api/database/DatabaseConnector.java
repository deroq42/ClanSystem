package de.deroq.clans.api.database;

import com.google.common.util.concurrent.ListenableFuture;

import java.sql.ResultSet;

/**
 * @author Miles
 * @since 23.12.2022
 */
public interface DatabaseConnector {

    void connect();

    void disconnect();

    MySQL getMySQL();

    interface MySQL {

        ListenableFuture<ResultSet> query(String query, Object... objects);

        ListenableFuture<Boolean> update(String query, Object... objects);
    }

    /*interface Mongo {

        ListenableFuture<Boolean> insert(MongoCollection<Document> collection, Bson filter, Document document);

        ListenableFuture<Document> query(MongoCollection<Document> collection, Bson filter);

        MongoCollection<Document> getCollection(String name);
    }*/
}
