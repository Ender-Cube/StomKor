package me.zax71.stomKor.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static me.zax71.stomKor.Main.CONFIG;
import static me.zax71.stomKor.Main.HUB;
import static me.zax71.stomKor.utils.ConfigUtils.getPosFromConfig;


public class PlayerLogin implements EventListener<PlayerLoginEvent> {
    @Override
    public @NotNull Class<PlayerLoginEvent> eventType() {
        return PlayerLoginEvent.class;
    }

    @Override
    public @NotNull Result run(@NotNull PlayerLoginEvent event) {
        Player player = event.getPlayer();
        event.setSpawningInstance(HUB);
        player.setRespawnPoint(Objects.requireNonNull(getPosFromConfig(CONFIG.node("hub", "spawnPoint"))));
        player.setGameMode(GameMode.ADVENTURE);
        Audiences.server().sendMessage(player.getName().append(Component.text(" joined the server")).color(NamedTextColor.YELLOW));
        return Result.SUCCESS;
    }
}
