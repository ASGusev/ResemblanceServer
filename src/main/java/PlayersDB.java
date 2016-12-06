import java.sql.*;

public class PlayersDB {
    private static Connection base;
    private static final String DBNAME = "players.db";
    private static Statement statement;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            base = DriverManager.getConnection("jdbc:sqlite:" + DBNAME);
            statement = base.createStatement();
        } catch (ClassNotFoundException e) {
            System.out.println("Error during loading class.");
        } catch (SQLException e) {
            System.out.println("Error during connecting to database.");
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

    public static void updateRating(String name, String passwordHash, int newRating)
            throws SQLException {
        statement.execute("UPDATE players SET rating = " + newRating +
                " WHERE nickname = '" + name + "' AND password_hash = '" +
                passwordHash + "';");
    }

    public static Player getPlayer(String name, String passwordHash) throws SQLException {
        ResultSet baseRet = statement.executeQuery("SELECT * FROM players " +
                " WHERE nickname = '" + name + "' AND password_hash = '"
                + passwordHash + "';");
        String nick = baseRet.getString("nickname");
        int rating = baseRet.getInt("rating");
        String password = baseRet.getString("password_hash");

        return new Player(nick, rating, password);
    }

    public static void register(String name, String password) throws SQLException {
        statement.execute("INSERT INTO 'players' VALUES ('" + name + "', '"
                + Player.DEFAULT_RATING + "', '" + password + "'); ");
    }
}
