package de.deroq.clans.config;

import lombok.Getter;

import java.io.File;

/**
 * @author Miles
 * @since 09.12.2022
 */
public class MySQLConfig extends Config {

    @Getter
    private String host = "localhost";

    @Getter
    private String database = "clansystem";

    @Getter
    private String port = "3306";

    @Getter
    private String username = "root";

    @Getter
    private String password = "123456";

    public MySQLConfig(File file) {
        super(file);
        init();
        save();
    }
}
