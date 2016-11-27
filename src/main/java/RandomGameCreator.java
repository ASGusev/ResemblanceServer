import java.util.ArrayList;

public class RandomGameCreator {
    private static ArrayList<Long> DEFAULT_CARD_SET = new ArrayList<>();

    public static final int PLAYERS_PER_GAME = 3;
    public static final int DEFAULT_ROUNDS_NUMBER = 3;

    private static ArrayList<Player> waitingPlayers = new ArrayList<>();

    static {
        DEFAULT_CARD_SET.add(1L);
        DEFAULT_CARD_SET.add(2L);
        DEFAULT_CARD_SET.add(3L);
        DEFAULT_CARD_SET.add(4L);
        DEFAULT_CARD_SET.add(5L);
        DEFAULT_CARD_SET.add(6L);
        DEFAULT_CARD_SET.add(7L);
        DEFAULT_CARD_SET.add(8L);
    }

    public static void addPlayer(Player player) {
        waitingPlayers.add(player);
        if (waitingPlayers.size() == PLAYERS_PER_GAME) {
            Game game = new Game((Player[])waitingPlayers.toArray(), DEFAULT_CARD_SET,
                    DEFAULT_ROUNDS_NUMBER);
        }
    }
}
