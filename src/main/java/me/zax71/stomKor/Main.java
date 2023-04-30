package me.zax71.stomKor;

import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.minestom.LiteMinestomFactory;
import me.zax71.stomKor.blocks.Sign;
import me.zax71.stomKor.blocks.Skull;
import me.zax71.stomKor.commands.HubCommand;
import me.zax71.stomKor.commands.MapCommand;
import me.zax71.stomKor.commands.ReloadCommand;
import me.zax71.stomKor.commands.arguments.ParkourMapArgument;
import me.zax71.stomKor.listeners.PlayerBlockBreak;
import me.zax71.stomKor.listeners.PlayerLogin;
import me.zax71.stomKor.listeners.PlayerMove;
import me.zax71.stomKor.utils.FullbrightDimension;
import me.zax71.stomKor.utils.SQLiteHandler;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.instance.*;
import net.minestom.server.utils.NamespaceID;
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

import static me.zax71.stomKor.utils.ConfigUtils.*;

public class Main {
    public static InstanceContainer HUB;
    public static CommentedConfigurationNode CONFIG;

    public static HoconConfigurationLoader LOADER;
    public static List<ParkourMap> parkourMaps = new ArrayList<>();
    public static SQLiteHandler SQLite;

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
                .addListener(new PlayerMove());
        globalEventHandler.addChild(entityNode);

        // Register block handlers
        MinecraftServer.getBlockManager().registerHandler(NamespaceID.from("minecraft:sign"), Sign::new);
        MinecraftServer.getBlockManager().registerHandler(NamespaceID.from("minecraft:skull"), Skull::new);

        // Register commands

        // Offline mode bad
        MojangAuth.init();

        // Start the server on port 25565
        minecraftServer.start("0.0.0.0", 25565);

        initWorlds();
        initCommands();
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
                .argument(ParkourMap.class, new ParkourMapArgument(MinecraftServer.getServer()))
                .register();
    }

    private static void initWorlds() {
        // Fail and stop server if hub doesn't exist
        if (!Files.exists(getPath("config/worlds/hub"))) {
            System.out.println("ERROR: Missing HUB world, please place an Anvil world in ./config/worlds/hub and restart the server");
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

                System.out.println("loading map " + name);
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