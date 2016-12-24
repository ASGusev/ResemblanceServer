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

    @Override
    public int hashCode() {
        return name.hashCode();
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

    public void sendRating() {
        messageThread.sendWritten(stream -> {
            try {
                stream.writeInt(Message.RATING_TYPE);
                stream.writeInt(rating);
                stream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void sendFriendPlayerMessage(boolean joined, String name) {
        messageThread.sendWritten(stream -> {
            try {
                stream.writeInt(Message.FRIEND_GAME_PLAYER_TYPE);
                stream.writeBoolean(joined);
                stream.writeUTF(name);
                stream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    protected void sendGameCancelledMessage() {
        messageThread.sendWritten(stream -> {
            try {
                stream.writeInt(Message.GAME_CANCELED_TYPE);
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
