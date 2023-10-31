package me.zax71.stomKor.listeners;

import io.leangen.geantyref.TypeToken;
import me.zax71.stomKor.ParkourMap;
import me.zax71.stomKor.ParkourPlayer;
import me.zax71.stomKor.inventories.MapInventory;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Random;

import static me.zax71.stomKor.Main.SQL;
import static me.zax71.stomKor.Main.config;
import static net.endercube.EndercubeCommon.utils.ComponentUtils.toHumanReadableTime;
import static net.minestom.server.event.EventListener.Result.SUCCESS;

public class PlayerMove implements EventListener<PlayerMoveEvent> {

    @Override
    public @NotNull Class<PlayerMoveEvent> eventType() {
        return PlayerMoveEvent.class;
    }

    /**
     * Gets a random death message from config
     *
     * @return The selected death message. "" if none are specified
     */
    private String getRandomDeathMessage() {
        // Get a random death message from config
        String[] deathMessages;
        try {
            deathMessages = config.node("messages", "deathMessages").get(new TypeToken<>() {
            });
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
        Random rand = new Random();

        // Send the message
        if (deathMessages != null) {
            int messageInt = rand.nextInt(deathMessages.length);
            return deathMessages[messageInt];
        }

        return "";
    }

    @Override
    public @NotNull net.minestom.server.event.EventListener.Result run(@NotNull PlayerMoveEvent event) {
        ParkourPlayer player = (ParkourPlayer) event.getPlayer();
        Tag<Boolean> spectating = Tag.Boolean("spectating");
        Scheduler scheduler = player.scheduler();

        // No checks should take place if spectating
        if (player.getTag(spectating)) {
            return SUCCESS;
        }

        ParkourMap currentMap = player.getParkourMap();

        // If we can't find a map matching the instance, return
        if (currentMap == null) {
            return SUCCESS;
        }

        // Start timer on player move
        if (!player.getTag(Tag.Boolean("startedTimer"))) {
            player.setTag(Tag.Long("startTime"), System.currentTimeMillis());
            player.setTag(Tag.Boolean("startedTimer"), true);

            // Start the action bar timer task
            player.setTag(Tag.Transient("actionbarTimerTask"),
                    scheduler.submitTask(() -> {
                        long timeTaken = System.currentTimeMillis() - player.getTag(Tag.Long("startTime"));
                        player.sendActionBar(Component.text(toHumanReadableTime(timeTaken), NamedTextColor.WHITE));
                        return TaskSchedule.millis(15);
                    })
            );
        }

        // See if player is below the death barrier and if so, teleport them to spawn or current checkpoint and send death message
        if (player.getPosition().y() < currentMap.deathY()) {
            player.sendMessage(getRandomDeathMessage());
            player.gotoCheckpoint();

        }

        // See if player is at next checkpoint and deal with it
        if (player.getTag(Tag.Integer("checkpoint")) < currentMap.checkpoints().length - 1) {
            // Check if the player is at their current checkpoint
            if (player.getPosition().sameBlock(currentMap.checkpoints()[player.getTag(Tag.Integer("checkpoint")) + 1])) {

                // Increment the checkpoint tag, send message and play a sound
                player.setTag(Tag.Integer("checkpoint"), player.getTag(Tag.Integer("checkpoint")) + 1);
                player.sendMessage("Checkpoint " + (player.getTag(Tag.Integer("checkpoint")) + 1) + " completed!");
                player.playSound(Sound.sound(SoundEvent.ENTITY_PLAYER_LEVELUP, Sound.Source.PLAYER, 1f, 1f));
            }
        }


        // See if player is at finish and has completed all checkpoints
        if (player.getPosition().sameBlock(currentMap.finishPoint())) {
            if (player.getTag(Tag.Integer("checkpoint")).equals(currentMap.checkpoints().length - 1)) {

                // Set the player to spectator and set finishedMap
                player.setTag(Tag.Boolean("spectating"), true);
                player.setTag(Tag.Boolean("finishedMap"), true);
                player.setGameMode(GameMode.SPECTATOR);

                player.teleport(player.getPosition().add(1, 1, 0));

                // Stop the action bar timer
                Task actionbarTimerTask = player.getTag(Tag.Transient("actionbarTimerTask"));
                actionbarTimerTask.cancel();

                // Calculate time by taking away the tag we set at the beginning from time now
                long timeTakenMS = System.currentTimeMillis() - player.getTag(Tag.Long("startTime"));
                player.sendMessage("Stopped Timer");

                player.sendMessage("Well done! You finished " + currentMap.name() + " in " + toHumanReadableTime(timeTakenMS));
                SQL.addTime(player, currentMap.name(), timeTakenMS);


                player.openInventory(MapInventory.getInventory());
            } else {
                player.sendMessage("You haven't completed all the checkpoints! You are currently at checkpoint " + (player.getTag(Tag.Integer("checkpoint")) + 1));
            }
        }
        return SUCCESS;
    }
}
