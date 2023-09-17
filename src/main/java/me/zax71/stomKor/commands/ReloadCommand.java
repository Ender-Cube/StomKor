package me.zax71.stomKor.commands;

import dev.rollczi.litecommands.command.execute.Execute;
import dev.rollczi.litecommands.command.route.Route;
import net.minestom.server.command.CommandSender;

@Route(name = "reload")
public class ReloadCommand {

    @Execute
    void execute(CommandSender sender) {
        sender.sendMessage("Probably didn't reload config, i cba to fix it. Just restart the server");
    }
}
