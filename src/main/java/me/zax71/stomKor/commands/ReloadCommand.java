package me.zax71.stomKor.commands;

import dev.rollczi.litecommands.argument.Arg;
import dev.rollczi.litecommands.command.execute.Execute;
import dev.rollczi.litecommands.command.route.Route;
import me.zax71.stomKor.ParkourMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.entity.Player;
import org.spongepowered.configurate.ConfigurateException;

import static me.zax71.stomKor.Main.CONFIG;
import static me.zax71.stomKor.Main.LOADER;
import static me.zax71.stomKor.utils.ConfigUtils.initConfig;

@Route(name = "reload")
public class ReloadCommand {

    @Execute
    void execute(CommandSender sender) {
        initConfig();
        sender.sendMessage("Probably didn't reload config, i cba to fix it. Just restart the server");
    }
}
