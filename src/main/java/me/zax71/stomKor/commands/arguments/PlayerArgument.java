package me.zax71.stomKor.commands.arguments;

import dev.rollczi.litecommands.argument.ArgumentName;
import dev.rollczi.litecommands.argument.simple.OneArgument;
import dev.rollczi.litecommands.command.LiteInvocation;
import dev.rollczi.litecommands.suggestion.Suggestion;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.network.socket.Server;
import panda.std.Option;
import panda.std.Result;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Tab autocomplete handler for
 * {@link Player}
 */
@ArgumentName("player")
public class PlayerArgument implements OneArgument<Player> {

    private final Server server;

    public PlayerArgument(Server server) {
        this.server = server;
    }

    // Acceptable inputs
    @Override
    public Result<Player, Object> parse(LiteInvocation invocation, String argument) {
        return Option.ofOptional(
                        MinecraftServer.getConnectionManager().getOnlinePlayers().stream()
                                .filter(player -> player.getUsername().equals(argument))
                                .findFirst()
                )
                .toResult("Player does not exist");
    }

    // List for tab complete
    @Override
    public List<Suggestion> suggest(LiteInvocation invocation) {
        return MinecraftServer.getConnectionManager().getOnlinePlayers().stream()
                .map(Player::getUsername)
                .map(Suggestion::of)
                .collect(Collectors.toList());
    }
}
