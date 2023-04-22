package me.zax71.stomKor.utils;

import io.leangen.geantyref.TypeToken;
import me.zax71.stomKor.ParkourMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    @Nullable
    public static Pos getPosFromConfig(ConfigurationNode configNode) {
        Double[] pointList;
        try {
            pointList = configNode.get(new TypeToken<Double[]>() {});
        } catch (SerializationException e) {

            throw new RuntimeException(e);
        }

        if (pointList == null) {
            return null;
        }

        if (pointList.length != 3) {
            System.out.println("Position value in config's length is out of bounds");
            return null;
        }

        return new Pos(pointList[0], pointList[1], pointList[2]);
    }
    @Nullable
    public static Pos[] getPosListFromConfig(ConfigurationNode configNode) {
        List<Pos> outArrayList = new ArrayList<>();
        for (ConfigurationNode currentNode : configNode.childrenList()) {
            outArrayList.add(getPosFromConfig(currentNode));
        }
        return outArrayList.toArray(new Pos[0]);
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
