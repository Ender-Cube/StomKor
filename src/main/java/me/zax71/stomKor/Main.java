package me.zax71.stomKor;

import com.google.gson.Gson;
import dev.rollczi.litecommands.minestom.LiteMinestomFactory;
import me.zax71.stomKor.commands.ReloadCommand;
import me.zax71.stomKor.commands.arguments.ParkourMapArgument;
import me.zax71.stomKor.commands.arguments.PlayerArgument;
import me.zax71.stomKor.listeners.AsyncPlayerPreLogin;
import me.zax71.stomKor.listeners.InventoryClose;
import me.zax71.stomKor.listeners.PlayerBlockBreak;
import me.zax71.stomKor.listeners.PlayerDisconnect;
import me.zax71.stomKor.listeners.PlayerLogin;
import me.zax71.stomKor.listeners.PlayerMove;
import me.zax71.stomKor.listeners.PlayerSpawn;
import me.zax71.stomKor.listeners.PlayerUseItem;
import me.zax71.stomKor.listeners.RedisSub;
import me.zax71.stomKor.utils.FullbrightDimension;
import net.endercube.EndercubeCommon.EndercubeGame;
import net.endercube.EndercubeCommon.utils.ConfigUtils;
import net.endercube.EndercubeCommon.utils.SQLWrapper;
import net.hollowcube.polar.PolarLoader;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerSwapItemEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.biomes.Biome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static me.zax71.stomKor.API.initAPI;

public class Main {
    public static CommentedConfigurationNode config;
    public static ConfigUtils configUtils;
    public static List<ParkourMap> parkourMaps = new ArrayList<>();
    public static List<Map<String, String>> playerMapQueue = new ArrayList<>();
    public static SQLWrapper SQL;
    public static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        EndercubeGame parkourGame = new EndercubeGame();

        parkourGame
                .addEvent(new PlayerLogin())
                .addEvent(new PlayerDisconnect())
                .addEvent(new AsyncPlayerPreLogin())
                .addEvent(new PlayerBlockBreak())
                .addEvent(new InventoryClose())
                .addEvent(new PlayerMove())
                .addEvent(new PlayerSpawn())
                .addEvent(new PlayerUseItem())
                .addEvent(InventoryPreClickEvent.class, event -> event.setCancelled(true))
                .addEvent(PlayerSwapItemEvent.class, event -> event.setCancelled(true))
                .setPlayer(ParkourPlayer::new)
                .setSQLName("playerTimes")
                .build();

        config = parkourGame.getConfig();
        configUtils = parkourGame.getConfigUtils();
        SQL = parkourGame.getSQL();

        // Create the team to turn off collisions
        MinecraftServer.getTeamManager().createBuilder("noCollision")
                .collisionRule(TeamsPacket.CollisionRule.NEVER)
                .updateTeamPacket()
                .build();

        initWorlds();
        initRedis();
        initCommands();
        initAPI();
    }

    private static void redisLogParkourMaps() {
        // Init Redis
        Jedis redis = new Jedis(
                configUtils.getOrSetDefault(config.node("database", "redis", "hostname"), "localhost"),
                Integer.parseInt(configUtils.getOrSetDefault(config.node("database", "redis", "port"), "6379"))
        );

        // Create a list of serialised parkour maps
        List<HashMap<String, String>> serializableParkourMaps = new ArrayList<>();
        for (ParkourMap map : parkourMaps) {
            serializableParkourMaps.add(map.serialise());
        }

        // Turn that in to a JSON
        String parkourMapsJson = new Gson().toJson(serializableParkourMaps);

        // Write to the Redis DB
        redis.set("parkourMaps", parkourMapsJson);
    }

    private static void initRedis() {
        // Init Redis
        Jedis redis = new Jedis(
                configUtils.getOrSetDefault(config.node("database", "redis", "hostname"), "localhost"),
                Integer.parseInt(configUtils.getOrSetDefault(config.node("database", "redis", "port"), "6379"))
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
                // TODO: Will reimplement in future update. Not multi server ready
                // .commandInstance(new SpectateCommand())
                .argument(ParkourMap.class, new ParkourMapArgument(MinecraftServer.getServer()))
                .argument(ParkourPlayer.class, new PlayerArgument(MinecraftServer.getServer()))
                .register();
    }

    private static void initWorlds() {
        // Register minecraft:the_void
        MinecraftServer.getBiomeManager().addBiome(Biome
                .builder()
                .name(NamespaceID.from("minecraft:the_void"))
                .build()
        );

        // Create limbo Instance
        InstanceContainer limboInstance = MinecraftServer.getInstanceManager().createInstanceContainer(
                FullbrightDimension.INSTANCE
        );
        limboInstance.setTimeRate(0);

        // Load all the maps
        for (File worldFile : Objects.requireNonNull(EndercubeGame.getPath("config/worlds/maps").toFile().listFiles())) {

            // Get the name of the map by removing .polar from the file name
            String name = worldFile.getName();
            name = name.substring(0, name.length() - 6);

            ConfigurationNode configNode = config.node("maps", name);

            logger.info("loading map " + name);
            try {
                parkourMaps.add(new ParkourMap(
                        MinecraftServer.getInstanceManager().createInstanceContainer(
                                FullbrightDimension.INSTANCE,
                                new PolarLoader(Path.of(worldFile.getPath()))
                        ),
                        name,
                        configNode.node("difficulty").getString(),
                        configUtils.getPosListFromConfig(configNode.node("checkpoints")),
                        configUtils.getPosFromConfig(configNode.node("spawn")),
                        configUtils.getPosFromConfig(configNode.node("finish")),
                        configNode.node("death-y").get(Short.class),
                        configUtils.getItemStackFromConfig(configNode.node("UIMaterial")),
                        configNode.node("order").getInt()
                ));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}