import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.function.Consumer;

public class Player {
    private int rating;
    private final String name;
    //private String password = null;
    private Game game;
    private MessageModule.ClientThread messageThread;

    Player(String name, int rating, String password) {
        this.name = name;
        this.rating = rating;
        //this.password = password;
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
        if (this.rating != rating) {
            this.rating = rating;
            try {
                PlayersDB.updateRating(name, rating);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
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
        sendWritten(stream -> {
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
        sendWritten(stream -> {
            try {
                stream.writeInt(Message.LEAD_REQUEST_TYPE);
                stream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    };

    public void askForCard(String form) {
        sendWritten(stream -> {
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
        sendWritten(stream -> {
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
        sendWritten(stream -> {
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
        sendWritten(stream -> {
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
        sendWritten(stream -> {
            try {
                stream.writeInt(Message.GAME_CANCELED_TYPE);
                stream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    protected void sendPasswordChangeResponseMessage(int code) {
        sendWritten(stream -> {
            try {
                stream.writeInt(Message.PASSWORD_CHANGE_RESPONSE_TYPE);
                stream.writeInt(code);
                stream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void sendMessage(byte[] message) {
        messageThread.sendMessage(message);
    }

    private void sendWritten(Consumer<DataOutputStream> writer) {
        ByteArrayOutputStream byteOS = new ByteArrayOutputStream(100);
        DataOutputStream out = new DataOutputStream(byteOS);
        writer.accept(out);
        messageThread.sendMessage(byteOS.toByteArray());
    }
}
