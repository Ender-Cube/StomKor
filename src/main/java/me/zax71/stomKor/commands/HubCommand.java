package me.zax71.stomKor.commands;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import dev.rollczi.litecommands.command.execute.Execute;
import dev.rollczi.litecommands.command.route.Route;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.command.CommandSender;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.PluginMessagePacket;
import net.minestom.server.sound.SoundEvent;

/**
 * Teleports the player to hub
 */
@Route(name = "hub")
public class HubCommand {

    @Execute
    void execute(CommandSender sender) {
        Player player = (Player) sender;

        player.playSound(Sound.sound(
                SoundEvent.BLOCK_NOTE_BLOCK_PLING,
                Sound.Source.PLAYER,
                1f,
                1f)
        );

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF("hub");
        player.sendPacket(new PluginMessagePacket("BungeeCord", out.toByteArray()));
    }
}
