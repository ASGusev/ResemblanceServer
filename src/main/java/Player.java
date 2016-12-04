import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Player {
    public static final int DEFAULT_RATING = 500;

    private int rating = DEFAULT_RATING;
    private String name = "";
    private String password = null;
    private Game currentGame;
    private MessageModule.ClientThread messageThread;

    Player() {}

    Player(String name, int rating) {
        this.name = name;
        this.rating = rating;
    }

    Player(String name, int rating, String password) {
        this.name = name;
        this.rating = rating;
        this.password = password;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Player && name.equals(((Player) other).name);
    }

    public String getName() {
        return name;
    }

    public int getRating() {
        return rating;
    }

    public void setGame(Game game) {
        currentGame = game;
    }

    public Game getGame() {
        return currentGame;
    }

    public void setClientThread(MessageModule.ClientThread thread) {
        messageThread = thread;
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

    public void sendGameStart() {
        ByteArrayOutputStream byteOS = new ByteArrayOutputStream(100);
        DataOutputStream out = new DataOutputStream(byteOS);
        try {
            out.writeInt(Message.START_GAME_TYPE);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //TODO: pass byteOS.toByteArray(); to player's send message
    };
}
