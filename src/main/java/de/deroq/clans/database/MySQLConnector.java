package de.deroq.clans.database;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.ClanSystem;
import de.deroq.clans.util.Executors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.*;
import java.util.concurrent.Callable;

/**
 * @author Miles
 * @since 08.12.2022
 */
public class MySQLConnector implements DatabaseConnector {

    private final ClanSystem clanSystem;

    @Getter
    private final String host;

    @Getter
    private final String database;

    @Getter
    private final String port;

    @Getter
    private final String username;

    @Getter
    private final String password;

    @Getter
    private final MySQL mySQL;

    @Getter
    private Connection connection;

    public MySQLConnector(ClanSystem clanSystem, String host, String database, String port, String username, String password) {
        this.clanSystem = clanSystem;
        this.host = host;
        this.database = database;
        this.port = port;
        this.username = username;
        this.password = password;
        this.mySQL = new MySQLImplementation(this);
    }

    @Override
    public void connect() {
        try {
            this.connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true", username, password);
            clanSystem.getLogger().info("Connected to MySQL");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                clanSystem.getLogger().info("Disconnected from MySQL");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @RequiredArgsConstructor
    static class MySQLImplementation implements MySQL {

        private final MySQLConnector connector;

        @Override
        public ListenableFuture<ResultSet> query(String query, Object... objects) {
            return Executors.asyncExecutor().submit(new Callable<ResultSet>() {
                @Override
                public ResultSet call() {
                    try {
                        PreparedStatement preparedStatement = connector.getConnection().prepareStatement(query);
                        if (objects != null && objects.length != 0) {
                            int i = 1;
                            for (Object object : objects) {
                                preparedStatement.setObject(i, object);
                                i++;
                            }
                        }
                        return preparedStatement.executeQuery();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }


        @Override
        public ListenableFuture<Boolean> update(String query, Object... objects) {
            return Executors.asyncExecutor().submit(new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    try {
                        PreparedStatement preparedStatement = connector.getConnection().prepareStatement(query);
                        if (objects != null && objects.length != 0) {
                            int i = 1;
                            for (Object object : objects) {
                                preparedStatement.setObject(i, object);
                                i++;
                            }
                        }
                        return preparedStatement.executeUpdate() >= 1;
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }
}
