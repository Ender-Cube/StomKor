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

import java.util.Objects;

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
            if (player.getPosition().sameBlock(currentMap.finishPoint()) && player.getTag(checkpoint).equals(currentMap.checkpoints().length)) {
                player.sendMessage("Well done! You finished " + currentMap.name() + " in <timer is getting added soontm>");

                player.setInstance(HUB, Objects.requireNonNull(getPosFromConfig(CONFIG.node("hub", "spawnPoint"))));
            }
        }

        return SUCCESS;
    }
}
