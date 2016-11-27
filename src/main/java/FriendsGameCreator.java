import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FriendsGameCreator {
    public static Map<String,FriendsGameCreator> preparingGames;

    private ArrayList<Player> players;
    private int roundsNumber;
    private ArrayList<Long> cards;

    static {
        preparingGames = new ConcurrentHashMap<>();
    }

    FriendsGameCreator (Player creator, int roundsNumber, ArrayList<Long> cards) {
        players = new ArrayList<>();
        players.add(creator);
        this.roundsNumber = roundsNumber;
        this.cards = cards;
        preparingGames.put(creator.getName(), this);
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void startGame() {
        Game game = new Game((Player[])players.toArray(), cards, roundsNumber);
        new Thread(game).start();
        preparingGames.remove(players.get(0).getName());
    }
}
