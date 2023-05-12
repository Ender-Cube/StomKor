package me.zax71.stomKor.commands;

import dev.rollczi.litecommands.argument.Arg;
import dev.rollczi.litecommands.command.execute.Execute;
import dev.rollczi.litecommands.command.route.Route;
import me.zax71.stomKor.ParkourMap;
import net.minestom.server.command.CommandSender;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.tag.Tag;

/**
 * Teleports the player to a parkour map
 */
@Route(name = "map")
public class MapCommand {

    @Execute
    void execute(CommandSender sender, @Arg ParkourMap map) {
        Player player = (Player) sender;
        Tag<Boolean> spectating = Tag.Boolean("spectating");

        player.sendMessage("Teleporting to " + map.name());

        player.sendMessage("Teleporting to hub");
        player.setTag(spectating, false);
        player.setGameMode(GameMode.ADVENTURE);

        map.teleportSpawn(player);
    }
}
