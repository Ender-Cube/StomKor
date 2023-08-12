package me.zax71.stomKor.commands;

import dev.rollczi.litecommands.argument.Arg;
import dev.rollczi.litecommands.command.execute.Execute;
import dev.rollczi.litecommands.command.route.Route;
import me.zax71.stomKor.ParkourMap;
import me.zax71.stomKor.ParkourPlayer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.tag.Tag;

@Route(name = "spectate")
public class SpectateCommand {

    @Execute
    void execute(CommandSender sender, @Arg ParkourPlayer spectatingPlayer) {
        Player player = (Player) sender;
        ParkourMap playersMap = spectatingPlayer.getParkourMap();

        if (playersMap == null) {
            player.sendMessage("The player you selected is not in a map or does not exist");
            return;
        }


        player.sendMessage("Spectating " + spectatingPlayer.getUsername() + " in " + playersMap.name());
        spectatingPlayer.sendMessage(player.getUsername() + " is spectating you");

        player.setTag(Tag.Boolean("spectating"), true);
        player.setGameMode(GameMode.SPECTATOR);
        player.setInstance(playersMap.instance());
        player.teleport(spectatingPlayer.getPosition());
    }
}
