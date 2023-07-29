package me.zax71.stomKor.utils;

import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

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
            logger.info("Setting config");
            saveConfig();
            return value;
        }

        return node.getString();
    }

    @Nullable
    public static ItemStack getItemStackFromConfig(ConfigurationNode configNode) {
        String materialString = configNode.node("material").getString();
        String name = configNode.node("name").getString();

        if (materialString == null) {
            materialString = "minecraft:barrier";
            logger.warn("Please set a material for the map above");
        }
        if (name == null) {
            name = "Please set a name in config for this";
            logger.warn("Please set a name for the map above");
        }

        Material material = Material.fromNamespaceId(materialString);

        if (material == null) {
            logger.warn("The material, " + materialString + " in config of the map above is invalid");
            return null;
        }

        return ItemStack.of(material)
                .withDisplayName(MiniMessage.miniMessage().deserialize(name));
    }


    @Nullable
    public static Pos getPosFromConfig(ConfigurationNode configNode) {
        Float[] pointList;
        try {
            pointList = configNode.get(new TypeToken<Float[]>() {
            });
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }

        if (pointList == null) {
            return null;
        }

        if (pointList.length == 3) {
            return new Pos(pointList[0], pointList[1], pointList[2]);
        }

        if (pointList.length == 5) {
            return new Pos(pointList[0], pointList[1], pointList[2], pointList[3], pointList[4]);
        }

        logger.warn("Position value in config's length is out of bounds");
        return null;
    }

    @Nullable
    public static Pos[] getPosListFromConfig(ConfigurationNode configNode) {
        List<Pos> outArrayList = new ArrayList<>();

        // Loop through the list at the specific node and add it to our out array list
        for (ConfigurationNode currentNode : configNode.childrenList()) {
            outArrayList.add(getPosFromConfig(currentNode));
        }
        return outArrayList.toArray(new Pos[0]);
    }

    public static void initConfig() {

        // Create config directories
        if (!Files.exists(getPath("config/worlds/maps"))) {
            logger.info("Creating configuration files");

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
