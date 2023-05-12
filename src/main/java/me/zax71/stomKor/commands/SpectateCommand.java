package me.zax71.stomKor.commands;

import dev.rollczi.litecommands.argument.Arg;
import dev.rollczi.litecommands.command.execute.Execute;
import dev.rollczi.litecommands.command.route.Route;
import me.zax71.stomKor.ParkourMap;
import net.minestom.server.command.CommandSender;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.tag.Tag;

import static me.zax71.stomKor.Main.HUB;
import static me.zax71.stomKor.Main.parkourMaps;

@Route(name = "spectate")
public class SpectateCommand {

    @Execute
    void execute(CommandSender sender, @Arg Player spectatingPlayer) {
        Player player = (Player) sender;
        ParkourMap playersMap = parkourMaps.stream().filter(parkourMap -> parkourMap.instance().equals(spectatingPlayer.getInstance())).findAny().orElse(null);
        Tag<Boolean> spectating = Tag.Boolean("spectating");

        if (playersMap == null) {
            player.sendMessage("The player you selected is not in a map or does not exist");
            return;
        }
        if (player.getInstance() != HUB) {
            player.sendMessage("You must be in the hub to spectate players");
            return;
        }


        player.sendMessage("Spectating " + spectatingPlayer.getUsername() + " in " + playersMap.name());
        spectatingPlayer.sendMessage(player.getUsername() + " is spectating you");

        player.setTag(spectating, true);
        player.setGameMode(GameMode.SPECTATOR);
        player.setInstance(playersMap.instance());
        player.teleport(spectatingPlayer.getPosition());
    }
}
