package me.zax71.stomKor.listeners;

import me.zax71.stomKor.ParkourPlayer;
import me.zax71.stomKor.inventories.MapInventory;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

public class InventoryClose implements EventListener<InventoryCloseEvent> {
    @Override
    public @NotNull Class<InventoryCloseEvent> eventType() {
        return InventoryCloseEvent.class;
    }

    @Override
    public @NotNull Result run(@NotNull InventoryCloseEvent event) {
        ParkourPlayer player = (ParkourPlayer) event.getPlayer();
        if (player.getTag(Tag.Boolean("spectating"))) {
            event.setNewInventory(MapInventory.getInventory());
        }


        return Result.SUCCESS;
    }
}
