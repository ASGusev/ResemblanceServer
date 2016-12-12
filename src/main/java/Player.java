import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Player {
    public static final int DEFAULT_RATING = 500;

    private int rating = DEFAULT_RATING;
    private String name = "";
    private String password = null;
    private Game game;
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

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Game getGame() {
        return game;
    }

    public void setClientThread(MessageModule.ClientThread thread) {
        messageThread = thread;
    }

    public void sendCard(Long card) {
        ByteArrayOutputStream byteOS = new ByteArrayOutputStream(100);
        DataOutputStream out = new DataOutputStream(byteOS);
        try {
            out.writeInt(Message.SEND_CARD_TYPE);
            out.writeLong(card);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        messageThread.sendMessage(byteOS.toByteArray());
    }

    public void askForAssociation() {
        ByteArrayOutputStream byteOS = new ByteArrayOutputStream(100);
        DataOutputStream out = new DataOutputStream(byteOS);
        try {
            out.writeInt(Message.LEAD_REQUEST_TYPE);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        messageThread.sendMessage(byteOS.toByteArray());
    };

    public void askForCard(String form) {
        ByteArrayOutputStream byteOS = new ByteArrayOutputStream(100);
        DataOutputStream out = new DataOutputStream(byteOS);
        try {
            out.writeInt(Message.CHOICE_REQUEST_TYPE);
            out.writeUTF(form);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        messageThread.sendMessage(byteOS.toByteArray());
    };

    public void askForVote(String form, long[] cards) {
        ByteArrayOutputStream byteOS = new ByteArrayOutputStream(500);
        DataOutputStream out = new DataOutputStream(byteOS);
        try {
            out.writeInt(Message.VOTE_REQUEST_TYPE);
            out.writeUTF(form);
            out.writeInt(cards.length);
            for (long card: cards) {
                out.writeLong(card);
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        messageThread.sendMessage(byteOS.toByteArray());
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

        messageThread.sendMessage(byteOS.toByteArray());
    };

    public void sendLeadersAssociation(long association) {
        ByteArrayOutputStream byteOS = new ByteArrayOutputStream(100);
        DataOutputStream out = new DataOutputStream(byteOS);
        try {
            out.writeInt(Message.LEADERS_ASSOCIATION_TYPE);
            out.writeLong(association);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        messageThread.sendMessage(byteOS.toByteArray());
    }

    public void sendRating() {
        ByteArrayOutputStream byteOS = new ByteArrayOutputStream(100);
        DataOutputStream out = new DataOutputStream(byteOS);
        try {
            out.writeInt(Message.RATING_TYPE);
            out.writeInt(rating);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        messageThread.sendMessage(byteOS.toByteArray());
    }
}
