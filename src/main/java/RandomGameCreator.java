import java.util.ArrayList;

public class RandomGameCreator {public static final int PLAYERS_PER_GAME = 3;
    public static final int DEFAULT_ROUNDS_NUMBER = 3;

    private static ArrayList<Player> waitingPlayers = new ArrayList<>();

    public static void addPlayer(Player player) {
        waitingPlayers.add(player);
        if (waitingPlayers.size() == PLAYERS_PER_GAME) {
            Player[] playersArr = new Player[waitingPlayers.size()];
            for(int i = 0; i < waitingPlayers.size(); i++) {
                playersArr[i] = waitingPlayers.get(i);
            }
            Game game = new Game(playersArr, Main.DEFAULT_CARD_SETS[0], DEFAULT_ROUNDS_NUMBER);
            new Thread(game).start();
            waitingPlayers = new ArrayList<>();
        }
    }
}
