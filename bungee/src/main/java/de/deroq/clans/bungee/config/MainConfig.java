package de.deroq.clans.bungee.config;

import lombok.Getter;

import java.io.File;

/**
 * @author Miles
 * @since 09.12.2022
 */
public class MainConfig extends Config {

    @Getter
    private int clanPlayerLimit = 30;

    public MainConfig(File file) {
        super(file);
        init();
        save();
    }
}
