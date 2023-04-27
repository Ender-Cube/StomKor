package me.zax71.stomKor.utils;

import net.minestom.server.entity.Player;
import net.minestom.server.entity.fakeplayer.FakePlayer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
    public void addTime(String player, Long time) {
        String sql = "INSERT INTO playerTimes(player,time) VALUES(?,?)";

        try {
            PreparedStatement preparedStatement = CONNECTION.prepareStatement(sql);
            preparedStatement.setString(1, player);
            preparedStatement.setLong(2, time);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the specified players best times ordered by an index
     * @param player player to retrieve data from
     * @param index time to get, 1 for best
     */
    public void getTimePlayer(String player, int index) {
        String sql = "SELECT * FROM playerTimes WHERE player = ? ORDER BY time DESC LIMIT 1 OFFSET ?;";

        try {
            PreparedStatement preparedStatement = CONNECTION.prepareStatement(sql);
            preparedStatement.setString(1, player);
            preparedStatement.setInt(2, index-1);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                System.out.println(resultSet.getLong("time"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the overall best times ordered by an index
     * @param index time to get, 1 for best
     */
    public List getTimeOverall(int index) {
        //TODO unbreak mec
        List<Object> outList = new ArrayList <Object>();
        String sql = "SELECT * FROM playerTimes ORDER BY time DESC LIMIT 1 OFFSET ?;";

        try {
            PreparedStatement preparedStatement = CONNECTION.prepareStatement(sql);
            preparedStatement.setString(1, player);
            preparedStatement.setInt(2, index-1);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                outList.add(new FakePlayer())
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Removes all but the top ten times per player for all players
     */
    public void pruneDatabase() {
        // Thanks, ChatGPT <3
        String sql = """
                DELETE FROM playerTimes
                WHERE (player, time) NOT IN (
                    SELECT player, time
                    FROM (
                        SELECT player, time,
                               ROW_NUMBER() OVER (PARTITION BY player ORDER BY time ASC) AS row_num
                        FROM playerTimes
                    ) AS t
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
                    time bigint NOT NULL,
                    course text NOT NULL
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
