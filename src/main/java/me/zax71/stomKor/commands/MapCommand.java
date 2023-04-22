package me.zax71.stomKor.commands;

import dev.rollczi.litecommands.argument.Arg;
import dev.rollczi.litecommands.command.execute.Execute;
import dev.rollczi.litecommands.command.route.Route;
import me.zax71.stomKor.ParkourMap;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

import static me.zax71.stomKor.Main.HUB;

@Route(name = "map")
public class MapCommand {

    @Execute
    void execute(CommandSender sender, @Arg ParkourMap map) {
        Player player = (Player) sender;

        player.sendMessage("Teleporting to " + map.name());

        map.teleport(player);
    }
}
