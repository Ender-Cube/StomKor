package me.zax71.stomKor;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * A record that contains all relevant data for a parkour map
 *
 * @param instance
 * @param name        No spaces allowed
 * @param difficulty
 * @param checkpoints
 */
public record ParkourMap(InstanceContainer instance, String name, String difficulty, Pos[] checkpoints, Pos spawnPoint,
                         Pos finishPoint, Short deathY, @Nullable ItemStack inventoryMaterial, int order) {

    public ParkourMap(InstanceContainer instance, String name, String difficulty, Pos[] checkpoints, Pos spawnPoint,
                      Pos finishPoint, Short deathY, ItemStack inventoryMaterial, int order) {
        this.instance = instance;
        this.name = name;
        this.difficulty = difficulty;
        this.checkpoints = checkpoints;
        this.spawnPoint = spawnPoint;
        this.finishPoint = finishPoint;
        this.deathY = deathY;
        this.inventoryMaterial = inventoryMaterial;
        this.order = order;

        instance.setTimeRate(0);
    }

    /**
     * Resets and initializes all the tags required for a parkour session
     *
     * @param player The player to start their session
     */
    public void startParkourSession(@NotNull Player player) {
        // Reset tags to default value
        player.setTag(Tag.Boolean("startedTimer"), false);
        player.setTag(Tag.Integer("checkpoint"), -1);
        player.setTag(Tag.Boolean("spectating"), false);
        player.setTag(Tag.Boolean("finishedMap"), false);

        // Reset spectating
        player.setGameMode(GameMode.ADVENTURE);
    }

    /**
     * Teleports a player to the spawn of a parkour instance and initializes the parkour session
     *
     * @param player the player to be influenced
     */
    public void teleportSpawn(@NotNull Player player) {
        if (player.getInstance() != instance) {
            player.setInstance(instance, spawnPoint);
        } else {
            player.teleport(spawnPoint);
        }
        startParkourSession(player);
    }

    /**
     * A sort of serializer. Does not include all values
     *
     * @return A HashMap containing the name of the map, difficulty and number of checkpoints
     */
    public HashMap<String, String> serialise() {
        HashMap<String, String> parkourMapHashMap = new HashMap<>();

        parkourMapHashMap.put("name", this.name);
        parkourMapHashMap.put("difficulty", this.difficulty);
        parkourMapHashMap.put("checkpoints", String.valueOf(this.checkpoints.length));
        parkourMapHashMap.put("order", String.valueOf(this.order));

        if (inventoryMaterial == null) {
            parkourMapHashMap.put("materialType", "BARRIER");
        } else {
            parkourMapHashMap.put("materialType", String.valueOf(inventoryMaterial.material()));
        }


        if (inventoryMaterial == null) {
            parkourMapHashMap.put("materialDisplayName", name);
        } else if (inventoryMaterial.getDisplayName() == null) {
            parkourMapHashMap.put("materialDisplayName", name);
        } else {
            parkourMapHashMap.put("materialDisplayName",
                    MiniMessage.miniMessage().serialize(inventoryMaterial.getDisplayName())
            );
        }


        return parkourMapHashMap;
    }
}
