package me.zax71.stomKor.commands.arguments;

import dev.rollczi.litecommands.argument.ArgumentName;
import dev.rollczi.litecommands.argument.simple.OneArgument;
import dev.rollczi.litecommands.command.LiteInvocation;
import dev.rollczi.litecommands.suggestion.Suggestion;
import me.zax71.stomKor.ParkourMap;
import panda.std.Result;

import java.util.List;

@ArgumentName("player")
public class ParkourMapArgument implements OneArgument<ParkourMap> {

    @Override
    public List<Suggestion> suggest(LiteInvocation invocation) {
        return OneArgument.super.suggest(invocation);
    }
}
