import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FriendsGameCreator {
    private static Map<String,FriendsGameCreator> preparingGames =
            new ConcurrentHashMap<>();

    private ArrayList<Player> players;
    private int roundsNumber;
    private ArrayList<Long> cards;

    private FriendsGameCreator (Player creator, int roundsNumber, ArrayList<Long> cards) {
        players = new ArrayList<>();
        players.add(creator);
        this.roundsNumber = roundsNumber;
        this.cards = cards;
    }

    public static void addGame(Player creator, int roundsNumber, ArrayList<Long> cards) {
        FriendsGameCreator game = new FriendsGameCreator(creator, roundsNumber, cards);
        preparingGames.put(creator.getName(), game);
    }

    public static void addPlayer(String creatorName, Player player) {
        preparingGames.get(creatorName).players.add(player);
    }

    public static void startGame(String name) {
        FriendsGameCreator gameCreator = preparingGames.get(name);

        Game game = new Game(gameCreator.players, gameCreator.cards, gameCreator.roundsNumber);
        new Thread(game).start();
        preparingGames.remove(gameCreator.players.get(0).getName());
    }

    public static void removeGame(String name) {
        FriendsGameCreator creator = preparingGames.get(name);
        //Tell all the players except the leader that the game is cancelled
        for (int i = 1; i < creator.players.size(); i++) {
            creator.players.get(i).sendGameCancelledMessage();
        }
        preparingGames.remove(name);
    }

    public static Player getGameCreator(String name) {
        return preparingGames.get(name).players.get(0);
    }

    public static boolean gameExists(String creatorName) {
        return preparingGames.containsKey(creatorName);
    }

    public static void removePlayer(String creatorName, String playerName) {
        FriendsGameCreator gameCreator = preparingGames.get(creatorName);
        int playerIndex = 0;
        while (!gameCreator.players.get(playerIndex).getName().equals(playerName)) {
            playerIndex++;
        }
        Player player = gameCreator.players.get(playerIndex);
        player.sendGameCancelledMessage();
        gameCreator.players.remove(playerIndex);
    }
}
