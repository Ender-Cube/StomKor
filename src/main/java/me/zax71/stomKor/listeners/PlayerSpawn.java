package me.zax71.stomKor.listeners;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

/**
 * This listener adds items to a player's inventory upon spawning
 */
public class PlayerSpawn implements EventListener<PlayerSpawnEvent> {

    private final ItemStack CHECKPOINT_ITEM = ItemStack.builder(Material.ARROW)
            .displayName(Component.text("Teleport to current checkpoint"))
            .build()
            .withTag(Tag.String("action"), "checkpoint");
    private final ItemStack RESTART_ITEM = ItemStack.builder(Material.BLAZE_POWDER)
            .displayName(Component.text("Restart the whole map!"))
            .build()
            .withTag(Tag.String("action"), "restart");
    private final ItemStack HUB_ITEM = ItemStack.builder(Material.RED_BED)
            .displayName(Component.text("Teleport back to hub"))
            .build()
            .withTag(Tag.String("action"), "hub");
    public static final ItemStack VISIBILITY_ITEM_INVISIBLE = ItemStack.builder(Material.ENDER_PEARL)
            .displayName(Component.text("Click to make players ghosts"))
            .build()
            .withTag(Tag.String("action"), "showPlayers");
    public static final ItemStack VISIBILITY_ITEM_VISIBLE = ItemStack.builder(Material.ENDER_EYE)
            .displayName(Component.text("Click to hide players"))
            .build()
            .withTag(Tag.String("action"), "hidePlayers");


    @Override
    public @NotNull Class<PlayerSpawnEvent> eventType() {
        return PlayerSpawnEvent.class;
    }

    @Override
    public @NotNull net.minestom.server.event.EventListener.Result run(@NotNull PlayerSpawnEvent event) {
        Player player = event.getPlayer();

        player.getInventory().setItemStack(0, CHECKPOINT_ITEM);
        player.getInventory().setItemStack(1, RESTART_ITEM);
        player.getInventory().setItemStack(4, VISIBILITY_ITEM_INVISIBLE);
        player.getInventory().setItemStack(8, HUB_ITEM);


        return Result.SUCCESS;
    }
}