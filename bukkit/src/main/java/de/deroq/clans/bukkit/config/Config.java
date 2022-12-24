package de.deroq.clans.bukkit.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Miles
 * @since 23.12.2022
 */
@AllArgsConstructor
public abstract class Config {

    @Getter
    @Setter
    protected transient File file;

    public void init() {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void save() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(gson.toJson(this));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
