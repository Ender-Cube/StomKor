package me.zax71.stomKor;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.InstanceContainer;

/**
 * A record that contains all relevant data for a parkour map
 * @param instance
 * @param name
 * @param difficulty
 * @param checkpoints
 */
public record ParkourMap(InstanceContainer instance, String name, String difficulty, Pos[] checkpoints) {

    public ParkourMap(InstanceContainer instance, String name, String difficulty, Pos[] checkpoints) {
        this.instance = instance;
        this.name = name;
        this.difficulty = difficulty;
        this.checkpoints = checkpoints;
    }
}
