package me.zax71.stomKor.listeners;

import me.zax71.stomKor.ParkourMap;
import me.zax71.stomKor.ParkourPlayer;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

import static me.zax71.stomKor.Main.logger;

public class PlayerUseItem implements EventListener<PlayerUseItemEvent> {

    private static final Potion INVIS = new Potion(PotionEffect.INVISIBILITY, (byte) 0, Integer.MAX_VALUE, (byte) 0);

    @Override
    public @NotNull Class<PlayerUseItemEvent> eventType() {
        return PlayerUseItemEvent.class;
    }


    @Override
    public @NotNull net.minestom.server.event.EventListener.Result run(@NotNull PlayerUseItemEvent event) {
        ParkourPlayer player = (ParkourPlayer) event.getPlayer();
        ParkourMap map = player.getParkourMap();


        if (map == null) {
            return Result.SUCCESS;
        }

        switch (event.getItemStack().getTag(Tag.String("action"))) {
            // TODO: restart confirm toggle option?
            case "restart" -> {
                map.teleportSpawn(player);
            }
            case "checkpoint" -> {
                player.gotoCheckpoint();
            }
            case "hub" -> {
                player.gotoHub();
            }
            case "showPlayers" -> {
                player.playSound(Sound.sound(
                        SoundEvent.UI_BUTTON_CLICK,
                        Sound.Source.PLAYER,
                        1f,
                        1f)
                );

                player.getInventory().setItemStack(4, PlayerSpawn.VISIBILITY_ITEM_VISIBLE);
                this.showPlayers(player);

            }
            case "hidePlayers" -> {
                player.playSound(Sound.sound(
                        SoundEvent.UI_BUTTON_CLICK,
                        Sound.Source.PLAYER,
                        1f,
                        1f)
                );

                player.getInventory().setItemStack(4, PlayerSpawn.VISIBILITY_ITEM_INVISIBLE);
                this.hidePlayers(player);

            }
            default -> logger.error("A player used an item with an invalid tag");

        }

        return Result.SUCCESS;
    }

    private void showPlayers(ParkourPlayer player) {
        player.updateViewerRule(viewabilityPlayer -> true);
    }

    private void hidePlayers(ParkourPlayer player) {
        player.updateViewerRule(viewabilityPlayer -> false);
    }
}