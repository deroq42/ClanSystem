package de.deroq.clans.database;

import com.google.common.util.concurrent.ListenableFuture;

import java.sql.ResultSet;
/**
 * @author Miles
 * @since 08.12.2022
 */
public interface DatabaseConnector {

    void connect();

    void disconnect();

    MySQL getMySQL();

    interface MySQL {

        ListenableFuture<ResultSet> query(String query, Object... objects);

        void update(String query, Object... objects);
    }
}
