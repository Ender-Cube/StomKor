package me.zax71.stomKor;

import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.http.util.RateLimiter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static me.zax71.stomKor.Main.SQL;
import static me.zax71.stomKor.Main.config;
import static me.zax71.stomKor.Main.configUtils;
import static me.zax71.stomKor.Main.parkourMaps;
import static net.endercube.EndercubeCommon.utils.ComponentUtils.toHumanReadableTime;

public class API {


    public static void initAPI() {

        var rateLimiter = new RateLimiter(TimeUnit.MINUTES);

        Gson gson = new Gson();

        // Get a list of parkour maps for checking later
        List<String> mapNames = new ArrayList<>();
        for (ParkourMap map : parkourMaps) {
            mapNames.add(map.name());
        }

        // Init the server
        var app = Javalin.create()
                .start(Integer.parseInt(configUtils.getOrSetDefault(config.node("API", "port"), "8080")));

        // Add some API info to index
        app.get("/", ctx -> {
            rateLimiter.incrementCounter(ctx, Integer.parseInt(configUtils.getOrSetDefault(config.node("API", "rateLimit"), "50")));
            ctx.result("""
                    Welcome to the Endercube Parkour API,
                                        
                    Here are some examples of how to use it:
                                        
                    Get a leaderboard for a specific map
                    /api/v1/leaderboard/<map>/<count>
                                        
                    See a leaderboard for a specific player and map
                    /api/v1/playerLeaderboard/<map>/<UUID>/<count>
                                        
                    See a list of all maps
                    /api/v1/maps
                    """);
        });

        // See a list of all maps
        app.get("/api/v1/maps", ctx -> {
            rateLimiter.incrementCounter(ctx, Integer.parseInt(configUtils.getOrSetDefault(config.node("API", "rateLimit"), "50")));
            ctx.result(gson.toJson(mapNames));
        });

        // Get a leaderboard for a specific map
        app.get("/api/v1/leaderboard/{map}/{count}", ctx -> {
            rateLimiter.incrementCounter(ctx, Integer.parseInt(configUtils.getOrSetDefault(config.node("API", "rateLimit"), "50")));

            String map = ctx.pathParam("map");

            int count;
            try {
                count = Integer.parseInt(ctx.pathParam("count"));
            } catch (NumberFormatException e) {
                ctx.result("Error: Count must be a number").status(400);
                return;
            }

            if (!mapNames.contains(map)) {
                ctx.result("Error: map not found").status(400);
                return;
            }

            Long time = SQL.getTimeOverall(map, count);
            String player = SQL.getPlayerOverall(map, 1);


            if (time == null) {
                ctx.result("Error: No database entry matching request").status(400);
                return;
            }

            ctx.result(player + " : " + toHumanReadableTime(time));
        });

        // See a leaderboard for a specific player and map
        app.get("/api/v1/playerLeaderboard/{map}/{UUID}/{count}", ctx -> {
            rateLimiter.incrementCounter(ctx, Integer.parseInt(configUtils.getOrSetDefault(config.node("API", "rateLimit"), "50")));
            String map = ctx.pathParam("map");
            String UUID = ctx.pathParam("UUID");

            int count;
            try {
                count = Integer.parseInt(ctx.pathParam("count"));
            } catch (NumberFormatException e) {
                ctx.result("Error: Count must be a number").status(400);
                return;
            }

            if (!mapNames.contains(map)) {
                ctx.result("Error: Map not found").status(400);
                return;
            }

            Long time = SQL.getTimeUUID(UUID, map, count);
            if (time == null) {
                ctx.result("Error: No database entry matching request").status(400);
                return;
            }


            ctx.result(toHumanReadableTime(time));
        });
    }
}
