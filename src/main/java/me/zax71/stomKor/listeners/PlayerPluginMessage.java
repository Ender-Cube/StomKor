package me.zax71.stomKor.listeners;

import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerPluginMessageEvent;
import org.jetbrains.annotations.NotNull;

import static me.zax71.stomKor.Main.logger;

public class PlayerPluginMessage implements EventListener<PlayerPluginMessageEvent> {
    @Override
    public @NotNull Class<PlayerPluginMessageEvent> eventType() {
        return PlayerPluginMessageEvent.class;
    }

    @Override
    public @NotNull Result run(@NotNull PlayerPluginMessageEvent event) {

        logger.info("Got plugin message: " + event.getMessageString());

        return Result.SUCCESS;
    }
}
