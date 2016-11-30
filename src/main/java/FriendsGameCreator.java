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
        preparingGames.remove(name);
    }
}
