package me.zax71.stomKor.utils;

import net.minestom.server.MinecraftServer;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

import static me.zax71.stomKor.Main.*;

public class ConfigUtils {
    public static void saveConfig() {
        try {
            LOADER.save(CONFIG);
        } catch (final ConfigurateException e) {
            System.err.println("Unable to save your messages configuration! Sorry! " + e.getMessage());
            MinecraftServer.stopCleanly();
        }
    }

    public static String getOrSetDefault(ConfigurationNode node, String value) {
        if (node.getString() == null) {
            node.raw(value);
            System.out.println("Setting config");
            return value;
        }

        return node.getString();
    }

    public static void initConfig() {

        // Create config directories
        if (!Files.exists(getPath("config/worlds/maps"))) {
            System.out.println("Creating configuration files");

            try {
                Files.createDirectories(getPath("config/worlds/"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        LOADER = HoconConfigurationLoader.builder()
                .path(getPath("config/config.conf"))
                .build();

        try {
            CONFIG = LOADER.load();
        } catch (ConfigurateException e) {
            System.err.println("An error occurred while loading config.conf: " + e.getMessage());
            if (e.getCause() != null) {
                e.getCause().printStackTrace();
            }
            MinecraftServer.stopCleanly();
        }
    }
}
