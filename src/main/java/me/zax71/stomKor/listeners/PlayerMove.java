package me.zax71.stomKor.listeners;

import me.zax71.stomKor.Main;
import me.zax71.stomKor.ParkourMap;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

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
        if (player.getInstance() != HUB) {
            ParkourMap currentMap = parkourMaps.stream().filter(parkourMap -> parkourMap.instance().equals(player.getInstance())).findAny().orElse(null);
            Tag<Integer> checkpoint = Tag.Integer("checkpoint");
            if (currentMap == null) {
                return SUCCESS;
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
                    Long timeTakenMS = new Date().getTime()-player.getTag(startTime);

                    player.sendMessage("Well done! You finished " + currentMap.name() + " in " + toHumanReadableTime(timeTakenMS));

                    player.setInstance(HUB, Objects.requireNonNull(getPosFromConfig(CONFIG.node("hub", "spawnPoint"))));
                } else {
                    player.sendMessage("You haven't completed all the checkpoints! You are currently at checkpoint " + player.getTag(checkpoint));
                }

            }
        }

        return SUCCESS;
    }
    private static String toHumanReadableTime(Long milliseconds) {
        if (milliseconds < 60000) {
            return zeroPrefix(TimeUnit.MILLISECONDS.toSeconds(milliseconds)) + "s";
        } else if (milliseconds < 60000*60) {
            String returnedValue =  zeroPrefix(TimeUnit.MILLISECONDS.toMinutes(milliseconds))
                    + ":"
                    + zeroPrefix(TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)))
                    + "s";
            return returnedValue;
        } else {
            String returnedValue = zeroPrefix(TimeUnit.MILLISECONDS.toHours(milliseconds))
                    + ":"
                    + zeroPrefix(TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(milliseconds)))
                    + ":"
                    + zeroPrefix(TimeUnit.MILLISECONDS.toSeconds(milliseconds) - (TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(milliseconds))))
                    + "s";

            return returnedValue;
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
