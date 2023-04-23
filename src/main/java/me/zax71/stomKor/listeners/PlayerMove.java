package me.zax71.stomKor.listeners;

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
import java.util.concurrent.TimeUnit;

import static me.zax71.stomKor.Main.*;
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

        // See if player is below the death barrier
        if (player.getPosition().y() < currentMap.deathY()) {
            player.teleport(currentMap.spawnPoint());
        }

        // See if player is at checkpoint
        // Can be optimised as player cannot be at checkpoint 5 before doing 4
        int i = 0;
        for (Pos checkingPos : currentMap.checkpoints()) {
            i++;
            if (player.getPosition().sameBlock(checkingPos)) {

                // Increment the checkpoint tag if the player is at a future checkpoint relative to their last one
                if (player.getTag(checkpoint) < i) {
                    player.setTag(checkpoint, i);
                    player.sendMessage("Checkpoint " + i + " completed!");
                    player.playSound(Sound.sound(SoundEvent.ENTITY_PLAYER_LEVELUP, Sound.Source.PLAYER, 1f, 1f));
                }
            }
        }

        // See if player is at finish and has completed all checkpoints
        if (player.getPosition().sameBlock(currentMap.finishPoint())) {
            if (player.getTag(checkpoint).equals(currentMap.checkpoints().length)) {

                // Calculate time by taking away the tag we set at the beginning from time now
                Tag<Long> startTime = Tag.Long("startTime");
                Long timeTakenMS = System.currentTimeMillis()-player.getTag(startTime);
                player.sendMessage("Stopped Timer");

                player.sendMessage("Well done! You finished " + currentMap.name() + " in " + toHumanReadableTime(timeTakenMS));

                player.setInstance(HUB, Objects.requireNonNull(getPosFromConfig(CONFIG.node("hub", "spawnPoint"))));
            } else {
                player.sendMessage("You haven't completed all the checkpoints! You are currently at checkpoint " + player.getTag(checkpoint));
            }

        }
        return SUCCESS;
    }
    private static String toHumanReadableTime(Long milliseconds) {
        if (milliseconds < 60000) {
            return zeroPrefix(milliseconds / 1000.0) + "s";

        } else if (milliseconds < 60000*60) {
            return zeroPrefix(TimeUnit.MILLISECONDS.toMinutes(milliseconds))
                    + ":"
                    + zeroPrefix(milliseconds / 1000.0 - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)))
                    + "s";

        } else {
            return zeroPrefix(TimeUnit.MILLISECONDS.toHours(milliseconds))
                    + ":"
                    + zeroPrefix(TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(milliseconds)))
                    + ":"
                    + zeroPrefix(milliseconds / 1000.0 - (TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(milliseconds))))
                    + "s";
        }

    }

    private static String zeroPrefix(Double number) {
        if (number < 10) {
            return "0" + new DecimalFormat("#.###").format(number);
        } else {
            return String.valueOf(new DecimalFormat("#.###").format(number));
        }
    }

    private static String zeroPrefix(Long number) {
        if (number < 10) {
            return "0" + number;
        } else {
            return String.valueOf(number);
        }
    }
}
