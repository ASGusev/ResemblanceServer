import java.util.ArrayList;

public class RandomGameCreator {
    private static final int PLAYERS_PER_GAME = 3;
    private static final int DEFAULT_ROUNDS_NUMBER = 2;

    private static ArrayList<Player> waitingPlayers = new ArrayList<>();

    public static void addPlayer(Player player) {
        synchronized (RandomGameCreator.class) {
            waitingPlayers.add(player);
            if (waitingPlayers.size() == PLAYERS_PER_GAME) {
                Game game = new Game(waitingPlayers, Main.DEFAULT_CARD_SETS[0], DEFAULT_ROUNDS_NUMBER);
                new Thread(game).start();
                waitingPlayers = new ArrayList<>();
            }
        }
    }

    public static void removePlayer(Player player) {
        synchronized (RandomGameCreator.class) {
            waitingPlayers.remove(player);
        }
    }
}
