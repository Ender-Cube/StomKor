package me.zax71.stomKor;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.PluginMessagePacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static me.zax71.stomKor.Main.parkourMaps;

public class ParkourPlayer extends Player {

    public ParkourPlayer(@NotNull UUID uuid, @NotNull String username, @NotNull PlayerConnection playerConnection) {

        super(uuid, username, playerConnection);
    }

    /**
     * @return The player's {@link ParkourMap}
     */
    @Nullable
    public ParkourMap getParkourMap() {
        return parkourMaps
                .stream()
                .filter(searchMap -> searchMap.instance() == this.getInstance())
                .findFirst()
                .orElse(null);
    }

    /**
     * Teleports the player back to the checkpoint they are on
     */
    public void gotoCheckpoint() {
        if (this.getTag(Tag.Integer("checkpoint")) == -1) {
            this.teleport(this.getParkourMap().spawnPoint());
        } else {
            this.teleport(this.getParkourMap().checkpoints()[this.getTag(Tag.Integer("checkpoint"))]);
        }
    }

    /**
     * Resets and initializes all the tags required for a parkour session
     */
    public void startParkourSession() {
        // Reset tags to default value
        this.setTag(Tag.Boolean("startedTimer"), false);
        this.setTag(Tag.Integer("checkpoint"), -1);
        this.setTag(Tag.Boolean("spectating"), false);
        this.setTag(Tag.Boolean("finishedMap"), false);

        // Reset spectating
        this.setGameMode(GameMode.ADVENTURE);
    }

    /**
     * Sends the player to the hub server and plays a sound
     */
    public void gotoHub() {
        this.playSound(Sound.sound(
                SoundEvent.BLOCK_NOTE_BLOCK_PLING,
                Sound.Source.PLAYER,
                1f,
                1f)
        );

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF("hub");
        this.sendPacket(new PluginMessagePacket("BungeeCord", out.toByteArray()));
    }
}
