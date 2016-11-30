import java.util.ArrayList;

public class Player {
    public static final int DEFULT_RATING = 500;

    private int rating = DEFULT_RATING;
    private String name = "";
    private long password = 0;
    private Game currentGame;

    Player() {}

    Player(String name, int rating) {
        this.name = name;
        this.rating = rating;
    }

    Player(String name, int rating, long password) {
        this.name = name;
        this.rating = rating;
        this.password = password;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Player && ((Player) other).name == name;
    }

    public String getName() {
        return name;
    }

    public int getRating() {
        return rating;
    }

    //public long getPassword() {
    //    return password;
    //}

    public void setGame(Game game) {
        currentGame = game;
    }

    public Game getGame() {
        return currentGame;
    }

    public void sendCard(Long card) {
        throw new UnsupportedOperationException();
    }

    public void askForAssociation() {
        //TODO: Send the player a message asking him for an association
    };

    public int askForCard(String form) {
        throw new UnsupportedOperationException();
    };

    public void askForVote(ArrayList<Long> cards) {
        //TODO: Send player am message asking hm to vote
    }

    public void sendLeadersAssociation(long card) {
        throw new UnsupportedOperationException();
    }

    public void sendGameStart() {};
}
