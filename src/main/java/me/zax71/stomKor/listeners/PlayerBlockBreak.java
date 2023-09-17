package me.zax71.stomKor.listeners;

import me.zax71.stomKor.ParkourPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;

import static me.zax71.stomKor.Main.config;
import static me.zax71.stomKor.Main.configUtils;

// Stops players from breaking blocks in all worlds
public class PlayerBlockBreak implements EventListener<PlayerBlockBreakEvent> {
    @Override
    public @NotNull Class<PlayerBlockBreakEvent> eventType() {
        return PlayerBlockBreakEvent.class;
    }

    @Override
    public @NotNull Result run(@NotNull PlayerBlockBreakEvent event) {

        final ConfigurationNode protectionErrorNode = config.node("messages", "protectionError");
        ParkourPlayer player = (ParkourPlayer) event.getPlayer();

        // https://github.com/Minestom/Minestom/discussions/1596
        event.setCancelled(true);

        // Send an error message
        Component message = MiniMessage.miniMessage().deserialize(
                configUtils.getOrSetDefault(protectionErrorNode, "<bold><red>Hey!</bold> <grey>Sorry, but you can't break that block here")
        );
        player.sendMessage(message);

        return Result.SUCCESS;
    }
}
