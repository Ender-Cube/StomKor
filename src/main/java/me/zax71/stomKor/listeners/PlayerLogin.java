package me.zax71.stomKor.listeners;

import me.zax71.stomKor.Main;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;


public class PlayerLogin implements EventListener<PlayerLoginEvent> {
    @Override
    public @NotNull Class<PlayerLoginEvent> eventType() {
        return PlayerLoginEvent.class;
    }

    @Override
    public @NotNull Result run(@NotNull PlayerLoginEvent event) {
        Player player = event.getPlayer();
        System.out.println( PlainTextComponentSerializer.plainText().serialize(player.getName()) + " joined the server");
        event.setSpawningInstance(Main.HUB);
        player.setRespawnPoint(new Pos(0.5, 70, 0.5));
        return Result.SUCCESS;
    }
}
