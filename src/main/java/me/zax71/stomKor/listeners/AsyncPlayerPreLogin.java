package me.zax71.stomKor.listeners;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.AsyncPlayerPreLoginEvent;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.Map;

import static me.zax71.stomKor.Main.logger;
import static me.zax71.stomKor.Main.playerMapQueue;


public class AsyncPlayerPreLogin implements EventListener<AsyncPlayerPreLoginEvent> {
    @Override
    public @NotNull Class<AsyncPlayerPreLoginEvent> eventType() {
        return AsyncPlayerPreLoginEvent.class;
    }

    @Override
    public @NotNull net.minestom.server.event.EventListener.Result run(@NotNull AsyncPlayerPreLoginEvent event) {

        Player player = event.getPlayer();

        // Get the player's playerRedisMessageMap from the queue
        @Nullable
        Map<String, String> playerRedisMessageMap = playerMapQueue
                .stream()
                .filter(playerMapHashMap -> playerMapHashMap.get("player").equals(player.getUuid().toString()))
                .findFirst()
                .orElse(null);

        // Remove that player's map object form the queue
        playerMapQueue.removeIf(playerMapHashMap -> playerMapHashMap.get("player").equals(player.getUuid().toString()));

        // If the player does not have a map, send them back to hub
        if (playerRedisMessageMap == null) {
            logger.warn(player.getUsername() + " is being sent back to hub due to messages arriving out of order");
            player.kick("Messages have arrived in the wrong order. Please try again");
            return Result.SUCCESS;
        }

        Tag<String> playerRedisMessageMapTag = Tag.String("playerRedisMessageMap");

        // Set that in JSON to a tag to be read in the PlayerLoginEvent
        player.setTag(playerRedisMessageMapTag, new JSONObject(playerRedisMessageMap).toString());
        return Result.SUCCESS;
    }
}
