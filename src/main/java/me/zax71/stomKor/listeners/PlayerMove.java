package me.zax71.stomKor.listeners;

import io.leangen.geantyref.TypeToken;
import me.zax71.stomKor.ParkourMap;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.serialize.SerializationException;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static me.zax71.stomKor.Main.*;
import static me.zax71.stomKor.utils.ComponentUtils.toHumanReadableTime;
import static me.zax71.stomKor.utils.ConfigUtils.getPosFromConfig;
import static net.minestom.server.event.EventListener.Result.SUCCESS;

public class PlayerMove implements EventListener<PlayerMoveEvent> {

    @Override
    public @NotNull Class<PlayerMoveEvent> eventType() {
        return PlayerMoveEvent.class;
    }

    @Override
    public @NotNull net.minestom.server.event.EventListener.Result run(@NotNull PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Tag<Boolean> spectating = Tag.Boolean("spectating");

        // No checks should take place if spectating
        if (player.getTag(spectating)) {
            return SUCCESS;
        }

        // If in hub
        if (player.getInstance() == HUB) {
            try {
                if (player.getPosition().y() < CONFIG.node("hub", "death-y").get(Integer.class)) {
                    player.teleport(Objects.requireNonNull(getPosFromConfig(CONFIG.node("hub", "spawnPoint"))));
                }
            } catch (SerializationException e) {
                throw new RuntimeException(e);
            }
            return SUCCESS;
        }

        // If not in hub (so in a parkour world)
        ParkourMap currentMap = parkourMaps.stream().filter(parkourMap -> parkourMap.instance().equals(player.getInstance())).findAny().orElse(null);
        Tag<Integer> checkpoint = Tag.Integer("checkpoint");
        Tag<Boolean> startedTimer = Tag.Boolean("startedTimer");

        // If we can't find a map matching the instance, return
        if (currentMap == null) {
            return SUCCESS;
        }

        // Start timer on player move
        if (!player.getTag(startedTimer)) {
            Tag<Long> startTime = Tag.Long("startTime");
            player.setTag(startTime, System.currentTimeMillis());
            player.setTag(startedTimer, true);
            player.sendMessage("Started Timer");
        }

        // See if player is below the death barrier and if so, teleport them to spawn or current checkpoint
        if (player.getPosition().y() < currentMap.deathY()) {

            String[] deathMessages;
            try {
                deathMessages = CONFIG.node("messages", "deathMessages").get(new TypeToken<String[]>() {});
            } catch (SerializationException e) {
                throw new RuntimeException(e);
            }
            Random rand = new Random();

            if (deathMessages != null) {
                int messageInt = rand.nextInt(deathMessages.length);
                player.sendMessage(deathMessages[messageInt]);
            }

            if (player.getTag(checkpoint) == -1) {
                player.teleport(currentMap.spawnPoint());
            } else {
                player.teleport(currentMap.checkpoints()[player.getTag(checkpoint)]);
            }

        }

        // See if player is at checkpoint:
        if (player.getTag(checkpoint) < currentMap.checkpoints().length-1) {
            // Check if the player is at their current checkpoint
            if (player.getPosition().sameBlock(currentMap.checkpoints()[player.getTag(checkpoint)+1])) {

                // Increment the checkpoint tag, send message and play a sound
                player.setTag(checkpoint, player.getTag(checkpoint)+1);
                player.sendMessage("Checkpoint " + (player.getTag(checkpoint)+1) + " completed!");
                player.playSound(Sound.sound(SoundEvent.ENTITY_PLAYER_LEVELUP, Sound.Source.PLAYER, 1f, 1f));
            }
        }



        // See if player is at finish and has completed all checkpoints
        if (player.getPosition().sameBlock(currentMap.finishPoint())) {
            if (player.getTag(checkpoint).equals(currentMap.checkpoints().length-1)) {

                // Calculate time by taking away the tag we set at the beginning from time now
                Tag<Long> startTime = Tag.Long("startTime");
                Long timeTakenMS = System.currentTimeMillis()-player.getTag(startTime);
                player.sendMessage("Stopped Timer");

                player.sendMessage("Well done! You finished " + currentMap.name() + " in " + toHumanReadableTime(timeTakenMS));
                SQLite.addTime(player, currentMap.name(), timeTakenMS);
                player.setInstance(HUB, Objects.requireNonNull(getPosFromConfig(CONFIG.node("hub", "spawnPoint"))));
            } else {
                player.sendMessage("You haven't completed all the checkpoints! You are currently at checkpoint " + (player.getTag(checkpoint)+1));
            }

        }
        return SUCCESS;
    }



}
