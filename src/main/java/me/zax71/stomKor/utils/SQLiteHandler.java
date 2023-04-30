package me.zax71.stomKor.utils;

import net.minestom.server.entity.Player;
import net.minestom.server.entity.fakeplayer.FakePlayer;
import net.minestom.server.utils.mojang.MojangUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static me.zax71.stomKor.Main.getPath;

public class SQLiteHandler {

    String filename;
    private Connection CONNECTION;

    /**
     * Initializes a database with the given name
     * @param filename the name for the database file. Should end in .db or .sqlite
     */
    public SQLiteHandler(String filename) {
        this.filename = filename;
        createDatabase("database.db");
        createTable();
    }

    /**
     * Adds a time to the database
     * @param player The player the time belongs to
     * @param time The time in milliseconds
     */
    public void addTime(Player player, String course, Long time) {
        String sql = "INSERT INTO playerTimes(player,course,time) VALUES(?,?,?)";

        try {
            PreparedStatement preparedStatement = CONNECTION.prepareStatement(sql);
            preparedStatement.setString(1, String.valueOf(player.getUuid()));
            preparedStatement.setString(2, course);
            preparedStatement.setLong(3, time);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the specified players best times ordered by an index
     * @param player player to retrieve data from
     * @param index time to get, 1 for best
     * @return the nth best time of that player
     */
    public Long getTimePlayer(Player player, String course, int index) {
        String sql = "SELECT * FROM playerTimes WHERE player = ? AND course = ? ORDER BY time ASC LIMIT 1 OFFSET ?;";

        try {
            PreparedStatement preparedStatement = CONNECTION.prepareStatement(sql);
            preparedStatement.setString(1, String.valueOf(player.getUuid()));
            preparedStatement.setString(2, course);
            preparedStatement.setInt(3, index-1);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                return resultSet.getLong("time");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * Retrieves the overall nth best time
     * @param course The course to get data for
     * @param index The nth time you want
     * @return The time
     */
    public Long getTimeOverall(String course, int index) {
        List<Object> outList = new ArrayList <Object>();
        String sql = "SELECT * FROM playerTimes WHERE course = ? ORDER BY time ASC LIMIT 1 OFFSET ?;";

        try {
            PreparedStatement preparedStatement = CONNECTION.prepareStatement(sql);
            preparedStatement.setString(1, course);
            preparedStatement.setInt(2, index-1);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                return resultSet.getLong("time");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    /**
     * Retrieves the player with the overall nth best time
     * @param course the course to get data for
     * @param index The nth time you want
     * @return the player
     */
    public String getPlayerOverall(String course, int index) {
        String sql = "SELECT * FROM playerTimes WHERE course = ? ORDER BY time ASC LIMIT 1 OFFSET ?;";

        try {
            PreparedStatement preparedStatement = CONNECTION.prepareStatement(sql);
            preparedStatement.setString(1, course);
            preparedStatement.setInt(2, index-1);
            ResultSet resultSet = preparedStatement.executeQuery();
            UUID uuid = (UUID.fromString(resultSet.getString("player"));
            while (resultSet.next()) {
                return MojangUtils.fromUuid(String.valueOf(uuid)).getAsString();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }
    /**
     * Removes all but the top ten times per player for all players
     */
    public void pruneDatabase() {
        // Thanks, ChatGPT <3
        String sql = """
                DELETE FROM playerTimes
                  WHERE (player, course, time) NOT IN (
                    SELECT player, course, time
                    FROM (
                      SELECT player, course, time,
                             ROW_NUMBER() OVER (PARTITION BY player, course ORDER BY time ASC) AS row_num
                      FROM playerTimes
                    ) AS subQuery
                    WHERE row_num <= 10
                  );
                  
                                
                """;

        try {
            CONNECTION.createStatement().execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates the table - only called in init
     */
    private void createTable() {
        String createTable = """
                CREATE TABLE IF NOT EXISTS playerTimes (
                    player text NOT NULL,
                    course text NOT NULL,
                    time bigint NOT NULL
                    
                );
                """;

        try {
            Statement statement = CONNECTION.createStatement();
            statement.execute(createTable);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates the database file
     * @param fileName name for the file with .db of .sqlite extension
     */
    private void createDatabase(String fileName) {

        String url = "jdbc:sqlite:" + getPath("config").toAbsolutePath() + "/" + fileName;

        try {
            CONNECTION = DriverManager.getConnection(url);
            if (CONNECTION != null) {
                DatabaseMetaData meta = CONNECTION.getMetaData();
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }
}
