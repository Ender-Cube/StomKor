package me.zax71.stomKor.commands.arguments;

import dev.rollczi.litecommands.argument.simple.OneArgument;
import dev.rollczi.litecommands.command.LiteInvocation;
import dev.rollczi.litecommands.suggestion.Suggestion;
import me.zax71.stomKor.ParkourMap;
import me.zax71.stomKor.enums.Modes;
import net.minestom.server.network.socket.Server;
import panda.std.Option;
import panda.std.Result;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static me.zax71.stomKor.Main.parkourMaps;

public class ModeArgument implements OneArgument<Modes> {
    private final Server server;

    public ModeArgument(Server server) {
        this.server = server;
    }

    // Acceptable inputs
    @Override
    public Result<Modes, ?> parse(LiteInvocation invocation, String argument) {
        return Option.ofOptional(
                        Arrays.stream(Modes.values()).findFirst()
                )
                .toResult("Map does not exist");
    }

    // List for tab complete
    @Override
    public List<Suggestion> suggest(LiteInvocation invocation) {
        return null;
    }
}
