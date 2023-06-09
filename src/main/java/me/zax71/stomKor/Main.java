package me.zax71.stomKor;

import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.minestom.LiteMinestomFactory;
import me.zax71.stomKor.blocks.Sign;
import me.zax71.stomKor.blocks.Skull;
import me.zax71.stomKor.commands.*;
import me.zax71.stomKor.commands.arguments.ParkourMapArgument;
import me.zax71.stomKor.commands.arguments.PlayerArgument;
import me.zax71.stomKor.listeners.PlayerBlockBreak;
import me.zax71.stomKor.listeners.PlayerLogin;
import me.zax71.stomKor.listeners.PlayerMove;
import me.zax71.stomKor.listeners.PlayerPluginMessage;
import me.zax71.stomKor.utils.FullbrightDimension;
import me.zax71.stomKor.utils.SQLiteHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import net.kyori.ansi.ANSIComponentRenderer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.*;
import net.minestom.server.utils.NamespaceID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static me.zax71.stomKor.API.initAPI;
import static me.zax71.stomKor.utils.ConfigUtils.*;

public class Main {
    public static InstanceContainer HUB;
    public static CommentedConfigurationNode CONFIG;

    public static HoconConfigurationLoader LOADER;
    public static List<ParkourMap> parkourMaps = new ArrayList<>();
    public static SQLiteHandler SQLite;
    public static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static LiteCommands<CommandSender> liteCommands;


    public static void main(String[] args) {
        initConfig();

        // Server Initialization
        MinecraftServer minecraftServer = MinecraftServer.init();

        // Register events
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        EventNode<Event> entityNode = EventNode.type("listeners", EventFilter.ALL);
        entityNode
                .addListener(new PlayerLogin())
                .addListener(new PlayerBlockBreak())
                .addListener(new PlayerPluginMessage())
                .addListener(new PlayerMove());
        globalEventHandler.addChild(entityNode);

        // Register block handlers
        MinecraftServer.getBlockManager().registerHandler(NamespaceID.from("minecraft:sign"), Sign::new);
        MinecraftServer.getBlockManager().registerHandler(NamespaceID.from("minecraft:skull"), Skull::new);

        switch (getOrSetDefault(CONFIG.node("connection", "mode"), "online")) {
            case "online" -> MojangAuth.init();
            case "velocity" -> {
                String velocitySecret = getOrSetDefault(CONFIG.node("connection", "velocitySecret"), "");
                if (!Objects.equals(velocitySecret, "")) {
                    VelocityProxy.enable(velocitySecret);
                }
            }
        }

        // Start the server on port 25565
        minecraftServer.start("0.0.0.0", Integer.parseInt(getOrSetDefault(CONFIG.node("connection", "port"), "25565")));
        logger.info("Starting server on port " + Integer.parseInt(getOrSetDefault(CONFIG.node("connection", "port"), "25565")) + " with " + getOrSetDefault(CONFIG.node("connection", "mode"), "online") + " encryption");

        initWorlds();
        initCommands();
        initAPI();
        SQLite = new SQLiteHandler("database.db");

    }
    public static Path getPath(String path) {
        try {
            return Path.of(new File(Main.class.getProtectionDomain().getCodeSource().getLocation()
                    .toURI()).getPath()).getParent().resolve(path);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static void initCommands() {
        liteCommands = LiteMinestomFactory.builder(MinecraftServer.getServer(), MinecraftServer.getCommandManager())
                .commandInstance(new MapCommand())
                .commandInstance(new ReloadCommand())
                .commandInstance(new HubCommand())
                .commandInstance(new LeaderboardCommand())
                .commandInstance(new SpectateCommand())
                .argument(ParkourMap.class, new ParkourMapArgument(MinecraftServer.getServer()))
                .argument(Player.class, new PlayerArgument(MinecraftServer.getServer()))
                .register();
    }

    private static void initWorlds() {
        // Fail and stop server if hub doesn't exist
        if (!Files.exists(getPath("config/worlds/hub"))) {
            logger.error("Missing HUB world, please place an Anvil world in ./config/worlds/hub and restart the server");
            MinecraftServer.stopCleanly();
            return;
        }

        // Load hub if it exists
        HUB = MinecraftServer.getInstanceManager().createInstanceContainer(
                FullbrightDimension.INSTANCE,
                new AnvilLoader(getPath("config/worlds/hub"))
        );
        HUB.setTimeRate(0);

        // Load all the maps
        for (File worldFile : Objects.requireNonNull(getPath("config/worlds/maps").toFile().listFiles())) {
            if (worldFile.isDirectory()) {
                String name = worldFile.getName();
                ConfigurationNode configNode = CONFIG.node("maps", name);

                logger.info("loading map " + name);
                try {
                    parkourMaps.add(new ParkourMap(
                            MinecraftServer.getInstanceManager().createInstanceContainer(
                                    FullbrightDimension.INSTANCE,
                                    new AnvilLoader(worldFile.getPath())
                            ),
                            name,
                            configNode.node("difficulty").getString(),
                            getPosListFromConfig(configNode.node("checkpoints")),
                            getPosFromConfig(configNode.node("spawn")),
                            getPosFromConfig(configNode.node("finish")),
                            configNode.node("death-y").get(Short.class)
                    ));
                } catch (SerializationException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}