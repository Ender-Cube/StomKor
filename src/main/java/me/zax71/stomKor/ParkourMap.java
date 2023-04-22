package me.zax71.stomKor;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

import static me.zax71.stomKor.Main.HUB;

/**
 * A record that contains all relevant data for a parkour map
 * @param instance
 * @param name No spaces allowed
 * @param difficulty
 * @param checkpoints
 */
public record ParkourMap(InstanceContainer instance, String name, String difficulty, Pos[] checkpoints, Pos spawnPoint, Pos finishPoint) {

    public ParkourMap(InstanceContainer instance, String name, String difficulty, Pos[] checkpoints, Pos spawnPoint, Pos finishPoint) {
        this.instance = instance;
        this.name = name;
        this.difficulty = difficulty;
        this.checkpoints = checkpoints;
        this.spawnPoint = spawnPoint;
        this.finishPoint = finishPoint;

        instance.setTimeRate(0);
    }

    public void teleport(@NotNull Player player) {
        if (player.getInstance() != instance){
            player.setInstance(instance, spawnPoint);

            // Reset/init checkpoints.  No need to reset them when you are in the world already
            Tag<Integer> checkpoint = Tag.Integer("checkpoint");
            player.setTag(checkpoint, 0);
        } else {
            player.teleport(spawnPoint);
        }

        Tag<Long> startTime = Tag.Long("startTime");
        player.setTag(startTime, new Date().getTime());

    }
}
