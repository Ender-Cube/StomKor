package me.zax71.stomKor.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import org.jetbrains.annotations.NotNull;

import static me.zax71.stomKor.Main.logger;

public class PlayerDisconnect implements EventListener<PlayerDisconnectEvent> {

    @Override
    public @NotNull Class<PlayerDisconnectEvent> eventType() {
        return PlayerDisconnectEvent.class;
    }

    @Override
    public @NotNull Result run(@NotNull PlayerDisconnectEvent event) {
        Player player = event.getPlayer();

        // Tell players, and the log, that someone left
        Component playerLeaveMessage = player.getName().append(Component.text(" left the server")).color(NamedTextColor.YELLOW);
        Audiences.players().sendMessage(playerLeaveMessage);
        logger.info(ANSIComponentSerializer.ansi().serialize(playerLeaveMessage));

        return Result.SUCCESS;
    }
}