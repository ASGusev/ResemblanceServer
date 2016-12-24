import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FriendsGameCreator {
    private static final Map<String,FriendsGameCreator> gameByPlayer =
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
        gameByPlayer.put(creator.getName(), game);
    }

    public static void addPlayer(String creatorName, Player player) {
        gameByPlayer.get(creatorName).players.add(player);
        gameByPlayer.put(player.getName(), gameByPlayer.get(creatorName));
    }

    public static void startGame(String name) {
        FriendsGameCreator gameCreator = gameByPlayer.get(name);

        Game game = new Game(gameCreator.players, gameCreator.cards, gameCreator.roundsNumber);
        new Thread(game).start();
        for (Player player: gameCreator.players) {
            gameByPlayer.remove(player.getName());
        }
    }

    public static void removeGame(String name) {
        FriendsGameCreator creator = gameByPlayer.get(name);
        //Tell all the players except the leader that the game is cancelled
        for (int i = 1; i < creator.players.size(); i++) {
            creator.players.get(i).sendGameCancelledMessage();
            gameByPlayer.remove(creator.players.get(i).getName());
        }
        gameByPlayer.remove(name);
    }

    public static Player getGameCreator(String name) {
        return gameByPlayer.get(name).players.get(0);
    }

    public static boolean gameExists(String creatorName) {
        if (!gameByPlayer.containsKey(creatorName)) {
            return false;
        }
        FriendsGameCreator gameCreator = gameByPlayer.get(creatorName);
        return creatorName.equals(gameCreator.players.get(0).getName());
    }

    public static void removePlayer(String playerName) {
        FriendsGameCreator gameCreator = gameByPlayer.get(playerName);
        int playerIndex = 0;
        while (!gameCreator.players.get(playerIndex).getName().equals(playerName)) {
            playerIndex++;
        }
        Player player = gameCreator.players.get(playerIndex);
        player.sendGameCancelledMessage();
        gameCreator.players.remove(playerIndex);
        gameByPlayer.remove(playerName);
    }
}
