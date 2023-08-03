package me.zax71.stomKor;

import com.google.gson.Gson;
import dev.rollczi.litecommands.minestom.LiteMinestomFactory;
import me.zax71.stomKor.blocks.Sign;
import me.zax71.stomKor.blocks.Skull;
import me.zax71.stomKor.commands.HubCommand;
import me.zax71.stomKor.commands.LeaderboardCommand;
import me.zax71.stomKor.commands.ReloadCommand;
import me.zax71.stomKor.commands.arguments.ParkourMapArgument;
import me.zax71.stomKor.commands.arguments.PlayerArgument;
import me.zax71.stomKor.listeners.*;
import me.zax71.stomKor.utils.FullbrightDimension;
import me.zax71.stomKor.utils.SQLiteHandler;
import net.hollowcube.polar.PolarLoader;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.utils.NamespaceID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.*;

import static me.zax71.stomKor.API.initAPI;
import static me.zax71.stomKor.utils.ConfigUtils.*;

public class Main {
    public static InstanceContainer LIMBO;
    public static CommentedConfigurationNode CONFIG;

    public static HoconConfigurationLoader LOADER;
    public static List<ParkourMap> parkourMaps = new ArrayList<>();
    public static List<Map<String, String>> playerMapQueue = new ArrayList<>();
    public static SQLiteHandler SQLite;
    public static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        initConfig();

        // Server Initialization
        MinecraftServer minecraftServer = MinecraftServer.init();

        // Register events
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        EventNode<Event> entityNode = EventNode.type("listeners", EventFilter.ALL);
        entityNode
                .addListener(new PlayerLogin())
                .addListener(new AsyncPlayerPreLogin())
                .addListener(new PlayerBlockBreak())
                .addListener(new InventoryClose())
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

        // Create the team to turn off collisions and make players partially visible
        MinecraftServer.getTeamManager().createBuilder("noCollision")
                .collisionRule(TeamsPacket.CollisionRule.NEVER)
                .seeInvisiblePlayers()
                .updateTeamPacket()
                .build();


        initWorlds();
        initRedis();
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

    private static void redisLogParkourMaps() {
        // Init Redis
        Jedis redis = new Jedis(
                getOrSetDefault(CONFIG.node("database", "redis", "hostname"), "localhost"),
                Integer.parseInt(getOrSetDefault(CONFIG.node("database", "redis", "port"), "6379"))
        );

        // Create a list of serialised parkour maps
        List<HashMap<String, String>> serialiseableParkourMaps = new ArrayList<>();

        for (ParkourMap map : parkourMaps) {
            serialiseableParkourMaps.add(map.serialise());
        }

        // Turn that in to a JSON
        String parkourMapsJson = new Gson().toJson(serialiseableParkourMaps);

        // Write to the Redis DB
        redis.set("parkourMaps", parkourMapsJson);
    }

    private static void initRedis() {
        // Init Redis
        Jedis redis = new Jedis(
                getOrSetDefault(CONFIG.node("database", "redis", "hostname"), "localhost"),
                Integer.parseInt(getOrSetDefault(CONFIG.node("database", "redis", "port"), "6379"))
        );

        // Create subscriber thread
        Thread newThread = new Thread(() -> redis.subscribe(new RedisSub(), "endercube/proxy/map/switch"));
        newThread.start();
        logger.info("Started Redis subscribe thread");

        redisLogParkourMaps();
    }

    private static void initCommands() {
        LiteMinestomFactory.builder(MinecraftServer.getServer(), MinecraftServer.getCommandManager())
                .commandInstance(new ReloadCommand())
                .commandInstance(new HubCommand())
                .commandInstance(new LeaderboardCommand())
                // TODO: Will reimplement in future update. Not multi server ready
                // .commandInstance(new SpectateCommand())
                .argument(ParkourMap.class, new ParkourMapArgument(MinecraftServer.getServer()))
                .argument(Player.class, new PlayerArgument(MinecraftServer.getServer()))
                .register();
    }

    private static void initWorlds() {
        // Create limbo Instance
        LIMBO = MinecraftServer.getInstanceManager().createInstanceContainer(
                FullbrightDimension.INSTANCE
        );
        LIMBO.setTimeRate(0);

        // Load all the maps
        for (File worldFile : Objects.requireNonNull(getPath("config/worlds/maps").toFile().listFiles())) {

            // Get the name of the map by removing .polar from the file name
            String name = worldFile.getName();
            name = name.substring(0, name.length() - 6);

            ConfigurationNode configNode = CONFIG.node("maps", name);

            logger.info("loading map " + name);
            try {
                parkourMaps.add(new ParkourMap(
                        MinecraftServer.getInstanceManager().createInstanceContainer(
                                FullbrightDimension.INSTANCE,
                                new PolarLoader(Path.of(worldFile.getPath()))
                        ),
                        name,
                        configNode.node("difficulty").getString(),
                        getPosListFromConfig(configNode.node("checkpoints")),
                        getPosFromConfig(configNode.node("spawn")),
                        getPosFromConfig(configNode.node("finish")),
                        configNode.node("death-y").get(Short.class),
                        getItemStackFromConfig(configNode.node("UIMaterial"))
                ));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}