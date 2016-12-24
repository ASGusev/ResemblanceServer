import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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
        messageThread.sendWritten(stream -> {
            try {
                stream.writeInt(Message.SEND_CARD_TYPE);
                stream.writeLong(card);
                stream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void askForAssociation() {
        messageThread.sendWritten(stream -> {
            try {
                stream.writeInt(Message.LEAD_REQUEST_TYPE);
                stream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    };

    public void askForCard(String form) {
        messageThread.sendWritten(stream -> {
            try {
                stream.writeInt(Message.CHOICE_REQUEST_TYPE);
                stream.writeUTF(form);
                stream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    };

    public void askForVote(String form, long[] cards) {
        messageThread.sendWritten(stream -> {
            try {
                stream.writeInt(Message.VOTE_REQUEST_TYPE);
                stream.writeUTF(form);
                stream.writeInt(cards.length);
                for (long card: cards) {
                    stream.writeLong(card);
                }
                stream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /*
    public void sendRoundEndMessage(long association, int[] scores) {
        messageThread.sendWritten(stream -> {
            try {
                stream.writeInt(Message.ROUND_END_TYPE);
                stream.writeLong(association);
                stream.writeInt(scores.length);
                for (int score: scores) {
                    stream.writeInt(score);
                }
                stream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    */
    public void sendRating() {
        messageThread.sendWritten(stream -> {
            try {
                stream.writeInt(Message.RATING_TYPE);
                stream.writeInt(rating);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void sendNewFriendGamePlayer(String name) {
        messageThread.sendWritten(stream -> {
            try {
                stream.writeInt(Message.NEW_PLAYER_TYPE);
                stream.writeUTF(name);
                stream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void sendMessage(byte[] message) {
        messageThread.sendMessage(message);
    }
}
