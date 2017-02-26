import java.sql.*;

public class PlayersDB {
    private static final int DEFAULT_RATING = 0;
    private static final String DBNAME = "players.db";

    private static Statement statement;

    public static void connect() {
        try {
            Connection base = DriverManager.getConnection("jdbc:sqlite:" + DBNAME);
            statement = base.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS players (nickname TEXT PRIMARY KEY, rating INTEGER, password_hash TEXT);");
        } catch (SQLException e) {
            System.out.println("Error during connecting to database.");
            e.printStackTrace();
        }
    }

    public static boolean exists(String name) throws SQLException {
        return statement.executeQuery("SELECT * FROM players " +
                "WHERE nickname = '" + name + "';").next();
    }

    public static boolean checkPassword(String name, String passwordHash)
            throws SQLException {
        return statement.executeQuery("SELECT * FROM players " +
                " WHERE nickname = '" + name + "' AND password_hash = '"
                + passwordHash + "';").next();
    }

    public static void changePassword(String name, String oldPasswordHash,
                                      String newPasswordHash) throws SQLException {
        statement.execute("UPDATE players SET password_hash = '" + newPasswordHash +
                "' WHERE nickname = '" + name + "' AND password_hash = '" +
                oldPasswordHash + "';");
    }

    public static void updateRating(String name, int newRating)
            throws SQLException {
        statement.execute("UPDATE players SET rating = " + newRating +
                " WHERE nickname = '" + name + "';");
    }

    public static Player getPlayer(String name, String passwordHash) throws SQLException {
        ResultSet baseRet = statement.executeQuery("SELECT * FROM players " +
                " WHERE nickname = '" + name + "' AND password_hash = '"
                + passwordHash + "';");
        String nick = baseRet.getString("nickname");
        int rating = baseRet.getInt("rating");
        String password = baseRet.getString("password_hash");

        return new Player(nick, rating);
    }

    public static void register(String name, String password) throws SQLException {
        statement.execute("INSERT INTO 'players' VALUES ('" + name + "', '"
                + DEFAULT_RATING + "', '" + password + "'); ");
    }
}
