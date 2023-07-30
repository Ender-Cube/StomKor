package me.zax71.stomKor.commands;

import dev.rollczi.litecommands.argument.Arg;
import dev.rollczi.litecommands.command.execute.Execute;
import dev.rollczi.litecommands.command.route.Route;
import me.zax71.stomKor.ParkourMap;
import me.zax71.stomKor.utils.ComponentUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.command.CommandSender;

import static me.zax71.stomKor.Main.SQLite;
import static me.zax71.stomKor.utils.ComponentUtils.toHumanReadableTime;


@Route(name = "leaderboard")
public class LeaderboardCommand {
    @Execute
    void execute(CommandSender sender, @Arg ParkourMap map) {
        if (map != null) {
            sender.sendMessage(createLeaderboard(map));
        }
    }

    private static TextComponent createLeaderboard(ParkourMap map) {
        Component placementComponent = Component.text()
                .append(leaderboardEntry("#FFD700", map, 1))
                .append(leaderboardEntry("#808080", map, 2))
                .append(leaderboardEntry("#CD7F32", map, 3))
                .append(Component.newline())
                .build();

        for (int i = 0; i < 10 - 3; i++) {
            placementComponent = placementComponent.append(leaderboardEntry("#AAAAAA", map, i + 4));
        }

        return Component.text()
                .append(ComponentUtils.centerComponent(MiniMessage.miniMessage().deserialize("<bold><gradient:#FF416C:#FF4B2B>All Time Leaderboard For " + map.name())))
                .append(Component.newline())
                .append(Component.newline())
                .append(placementComponent)
                .build();
    }

    private static Component leaderboardEntry(String color, ParkourMap map, int placement) {
        String placementToNameGap;

        if (SQLite.getPlayerOverall(map.name(), placement) == null) {
            return Component.empty();
        }
        if (placement >= 10) {
            placementToNameGap = " ";
        } else {
            placementToNameGap = "  ";
        }
        return MiniMessage.miniMessage().deserialize("<" + color + ">#<bold>" + placement + placementToNameGap + SQLite.getPlayerOverall(map.name(), placement) + "</bold> " + toHumanReadableTime(SQLite.getTimeOverall(map.name(), placement)))
                .append(Component.newline());
    }
}