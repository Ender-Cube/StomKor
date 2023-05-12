package me.zax71.stomKor;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

/**
 * A record that contains all relevant data for a parkour map
 * @param instance
 * @param name No spaces allowed
 * @param difficulty
 * @param checkpoints
 */
public record ParkourMap(InstanceContainer instance, String name, String difficulty, Pos[] checkpoints, Pos spawnPoint, Pos finishPoint, Short deathY) {

    public ParkourMap(InstanceContainer instance, String name, String difficulty, Pos[] checkpoints, Pos spawnPoint, Pos finishPoint, Short deathY) {
        this.instance = instance;
        this.name = name;
        this.difficulty = difficulty;
        this.checkpoints = checkpoints;
        this.spawnPoint = spawnPoint;
        this.finishPoint = finishPoint;
        this.deathY = deathY;

        instance.setTimeRate(0);
    }

    public void teleportSpawn(@NotNull Player player) {
        if (player.getInstance() != instance){
            player.setInstance(instance, spawnPoint);
        } else {
            player.teleport(spawnPoint);
        }

        // Reset checkpoints and timer
        Tag<Integer> checkpoint = Tag.Integer("checkpoint");
        Tag<Boolean> startedTimer = Tag.Boolean("startedTimer");
        Tag<Boolean> spectating = Tag.Boolean("spectating");
        player.setTag(startedTimer, false);
        player.setTag(checkpoint, -1);
        player.setTag(spectating, false);

    }

    public void teleportCheckpoint(Player player) {
        Tag<Integer> checkpoint = Tag.Integer("checkpoint");
    }
}
