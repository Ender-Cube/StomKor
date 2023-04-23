package me.zax71.stomKor.commands.arguments;

import dev.rollczi.litecommands.argument.ArgumentName;
import dev.rollczi.litecommands.argument.simple.OneArgument;
import dev.rollczi.litecommands.command.LiteInvocation;
import dev.rollczi.litecommands.suggestion.Suggestion;
import me.zax71.stomKor.ParkourMap;
import net.minestom.server.network.socket.Server;
import panda.std.Option;
import panda.std.Result;

import java.util.List;
import java.util.stream.Collectors;

import static me.zax71.stomKor.Main.parkourMaps;

/**
 * Tab autocomplete handler for
 * {@link ParkourMap}
 */
@ArgumentName("parkourMap")
public class ParkourMapArgument implements OneArgument<ParkourMap> {

    private final Server server;

    public ParkourMapArgument(Server server) {
        this.server = server;
    }

    // Acceptable inputs
    @Override
    public Result<ParkourMap, Object> parse(LiteInvocation invocation, String argument) {
        return Option.ofOptional(
                parkourMaps.stream()
                .filter(parkourMap -> parkourMap.name().equals(argument))
                .findFirst()
                )
                .toResult("Map does not exist");
    }

    // List for tab complete
    @Override
    public List<Suggestion> suggest(LiteInvocation invocation) {
        return parkourMaps.stream()
                .map(ParkourMap::name)
                .map(Suggestion::of)
                .collect(Collectors.toList());
    }
}
