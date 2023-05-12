package me.zax71.stomKor.commands;

import dev.rollczi.litecommands.command.execute.Execute;
import dev.rollczi.litecommands.command.route.Route;
import net.minestom.server.command.CommandSender;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.tag.Tag;

import java.util.Objects;

import static me.zax71.stomKor.Main.CONFIG;
import static me.zax71.stomKor.Main.HUB;
import static me.zax71.stomKor.utils.ConfigUtils.getPosFromConfig;

/**
 * Teleports the player to hub
 */
@Route(name = "hub")
public class HubCommand {

    @Execute
    void execute(CommandSender sender) {
        Tag<Boolean> spectating = Tag.Boolean("spectating");
        Player player = (Player) sender;
        Pos instanceSpawnPoint = Objects.requireNonNull(getPosFromConfig(CONFIG.node("hub", "spawnPoint")));

        player.sendMessage("Teleporting to hub");
        player.setTag(spectating, false);
        player.setGameMode(GameMode.ADVENTURE);

        if (player.getInstance() != HUB){
            player.setInstance(HUB, instanceSpawnPoint);
        } else {
            player.teleport(instanceSpawnPoint);
        }
    }
}
