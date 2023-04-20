package me.zax71.stomKor.commands;

import dev.rollczi.litecommands.argument.Arg;
import dev.rollczi.litecommands.command.execute.Execute;
import dev.rollczi.litecommands.command.route.Route;
import me.zax71.stomKor.ParkourMap;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;

@Route(name = "map")
public class MapCommand {

    @Execute
    void execute(CommandSender sender, @Arg ParkourMap map) {
        sender.sendMessage("You selected " + map.name());
    }
}
