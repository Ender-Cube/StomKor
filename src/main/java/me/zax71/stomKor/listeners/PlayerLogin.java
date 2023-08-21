package me.zax71.stomKor.listeners;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.zax71.stomKor.ParkourMap;
import me.zax71.stomKor.ParkourPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Objects;

import static me.zax71.stomKor.Main.logger;
import static me.zax71.stomKor.Main.parkourMaps;


public class PlayerLogin implements EventListener<PlayerLoginEvent> {
    @Override
    public @NotNull Class<PlayerLoginEvent> eventType() {
        return PlayerLoginEvent.class;
    }

    @Override
    public @NotNull Result run(@NotNull PlayerLoginEvent event) {
        ParkourPlayer player = (ParkourPlayer) event.getPlayer();

        Tag<String> playerRedisMessageMapTag = Tag.String("playerRedisMessageMap");

        // Deserialize the data from AsyncPlayerPreLoginEvent
        HashMap<String, String> playerRedisMessageMap = new Gson().fromJson(player.getTag(playerRedisMessageMapTag), new TypeToken<HashMap<String, String>>() {
        }.getType());

        logger.info(player.getUsername() + " is going to be sent to " + playerRedisMessageMap.get("map"));


        ParkourMap map = parkourMaps
                .stream()
                .filter((currentMap) -> currentMap.name().equals(playerRedisMessageMap.get("map")))
                .findFirst()
                .orElse(null);

        // Get the instance in the parkourMaps list from the string of the map name
        event.setSpawningInstance(Objects.requireNonNull(map).instance());

        player.setRespawnPoint(map.spawnPoint());
        player.startParkourSession();
        player.setGameMode(GameMode.ADVENTURE);

        // Tell players, and the log, that someone joined
        Component playerJoinMessage = player.getName().append(Component.text(" joined the server")).color(NamedTextColor.YELLOW);
        Audiences.players().sendMessage(playerJoinMessage);
        logger.info(ANSIComponentSerializer.ansi().serialize(playerJoinMessage));

        // Initialise the spectating tag
        player.setTag(Tag.Boolean("spectating"), false);

        // Initialise finishedMap tag
        player.setTag(Tag.Boolean("finishedMap"), false);

        // Set team
        player.setTeam(MinecraftServer.getTeamManager().getTeam("noCollision"));
        return Result.SUCCESS;
    }
}
