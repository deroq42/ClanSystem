package de.deroq.clans.config;

import lombok.Getter;

import java.io.File;

/**
 * @author Miles
 * @since 09.12.2022
 */
public class MainConfig extends Config {

    @Getter
    private boolean mySQL = true;

    @Getter
    private boolean mongoDB = false;

    @Getter
    private boolean cassandra = false;

    public MainConfig(File file) {
        super(file);
        init();
        save();
    }
}
