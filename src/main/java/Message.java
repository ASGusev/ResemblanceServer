import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;

public class Message {
    final public static int TEST_TYPE = 0;
    final public static int REGISTER_TYPE = 1;
    final public static int LOGIN_TYPE = 2;
    final public static int JOIN_RANDOM_GAME_TYPE = 3;
    final public static int QUIT_RANDOM_GAME_TYPE = 4;
    final public static int START_GAME_TYPE = 5;
    final public static int SEND_CARD_TYPE = 6;
    final public static int LEAD_REQUEST_TYPE = 7;
    final public static int LEAD_ASSOCIATION_TYPE = 8;
    final public static int CHOICE_REQUEST_TYPE = 9;
    final public static int CHOICE_TYPE = 10;
    final public static int VOTE_REQUEST_TYPE = 11;
    final public static int VOTE_TYPE = 12;
    final public static int ROUND_END_TYPE = 13;
    final public static int RATING_TYPE = 14;
    final public static int CREATE_FRIEND_GAME_TYPE = 15;
    final public static int JOIN_FRIEND_GAME_TYPE = 16;
    final public static int NEW_PLAYER_TYPE = 17;
    final public static int REMOVE_PLAYER_TYPE = 18;
    final public static int CANCEL_FRIEND_GAME_TYPE = 19;
    final public static int START_FRIEND_GAME_TYPE = 20;
    final public static int GAME_FINISH_TYPE = 21;
    final public static int GAME_CANCELED_TYPE = 22;

    MessageModule.ClientThread client;
    private int type = 0;

    Message(int type) {
        this.type = type;
    }
    Message(MessageModule.ClientThread client) { this.client = client; }
    Message(MessageModule.ClientThread client, int type) { this.type = type; this.client = client; }

    public int getType() {
        return type;
    }
    public MessageModule.ClientThread getClient() {
        return client;
    }

    public void setType(int newType) {
        type = newType;
    }
    public void setClient(MessageModule.ClientThread newClient) {
        client = newClient;
    }


    public void readMessage(DataInputStream in) {
        switch (type) {
            case TEST_TYPE:
                readTestMessage(in);
                break;
            case REGISTER_TYPE:
                readRegisterMessage(in);
                break;
            case LOGIN_TYPE:
                readLoginMessage(in);
                break;
            case JOIN_RANDOM_GAME_TYPE:
                readJoinRandomGameMessage();
                break;
            case QUIT_RANDOM_GAME_TYPE:
                readQuitRandomGameMessage();
                break;
            case LEAD_ASSOCIATION_TYPE:
                readLeadAssociationMessage(in);
                break;
            case CHOICE_TYPE:
                readChoiceMessage(in);
                break;
            case VOTE_TYPE:
                readVoteMessage(in);
                break;
            case CREATE_FRIEND_GAME_TYPE:
                readCreateGameMessage(in);
                break;
            case JOIN_FRIEND_GAME_TYPE:
                readJoinFriendGameMessage(in);
                break;
            case START_FRIEND_GAME_TYPE:
                readStartFriendGameMessage();
                break;
            case CANCEL_FRIEND_GAME_TYPE:
                readCancelFriendGameMessage();
                break;
            case REMOVE_PLAYER_TYPE:
                readRemoveFriendGamePlayer(in);
                break;
        }
    }

    private void readTestMessage(DataInputStream in) {
        String testMessage = null;
        try {
            synchronized (in) {
                testMessage = in.readUTF();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        applyTest(testMessage);
    }

    private void readRegisterMessage(DataInputStream in) {
        String login = null;
        String hashPassword = null;
        try {
            synchronized (in) {
                login = in.readUTF();
                hashPassword = in.readUTF();
            }
            applyRegister(login, hashPassword);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readLoginMessage(DataInputStream in) {
        String login = null;
        String hashPassword = null;
        try {
            synchronized (in) {
                login = in.readUTF();
                hashPassword = in.readUTF();
            }
            applyLogin(login, hashPassword);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readJoinRandomGameMessage() {
        RandomGameCreator.addPlayer(client.getPlayer());
    }

    private void readQuitRandomGameMessage() {
        RandomGameCreator.removePlayer(client.getPlayer());
    }

    private void readLeadAssociationMessage(DataInputStream stream) {
        long card = -1;
        String association = null;
        try {
            card = stream.readLong();
            association = stream.readUTF();
            client.getPlayer().getGame().addChoiceMessage(new
                    Game.ChoiceMessage(client.getPlayer(), card, association));
        } catch (IOException e) {}
    }

    private void readChoiceMessage(DataInputStream stream) {
        long card;
        try {
            card = stream.readLong();
            client.getPlayer().getGame().addChoiceMessage(new
                    Game.ChoiceMessage(client.getPlayer(), card));
        } catch (IOException e) {}
    }

    private void readVoteMessage(DataInputStream stream) {
        long card;
        try {
            card = stream.readLong();
            client.getPlayer().getGame().addChoiceMessage(new
                    Game.ChoiceMessage(client.getPlayer(), card));
        } catch (IOException e) {}
    }

    private void readCreateGameMessage(DataInputStream stream) {
        try {
            int roundsNumber = stream.readInt();
            int setSize = stream.readInt();
            ArrayList <Long> cards = new ArrayList<Long>();
            for (int i = 0; i < setSize; i++) {
                cards.add((long)stream.readInt());
            }
            FriendsGameCreator.addGame(client.getPlayer(), roundsNumber, cards);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readJoinFriendGameMessage(DataInputStream stream) {
        try {
            String gameCreatorName = stream.readUTF();
            if (FriendsGameCreator.gameExists(gameCreatorName)) {
                FriendsGameCreator.addPlayer(gameCreatorName, client.getPlayer());

                FriendsGameCreator.getGameCreator(gameCreatorName).
                        sendNewFriendGamePlayer(client.getPlayer().getName());
            } else {
                client.getPlayer().sendGameCancelledMessage();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readStartFriendGameMessage() {
        FriendsGameCreator.startGame(client.getPlayer().getName());
    }

    private void readCancelFriendGameMessage() {
        FriendsGameCreator.removeGame(client.getPlayer().getName());
    }

    private void readRemoveFriendGamePlayer(DataInputStream stream) {
        try {
            String playerName = stream.readUTF();
            FriendsGameCreator.removePlayer(client.getPlayer().getName(), playerName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //-----------------------------------------------------------------------

    private void applyTest(String textMessage) {
        System.out.println(textMessage);
    }

    private void applyRegister(String nickname, String hashPassword) {
        //System.out.println("1");
        final int networkError = -1;
        final int successfulRegistration = 0;
        final int nicknameError = 1;

        //System.out.println("a");
        if (nickname == null || hashPassword == null) {
            sendRegisterMessage(networkError);
            return;
        }

        //System.out.println("b");
        try {
            if (PlayersDB.exists(nickname)) {
                sendRegisterMessage(nicknameError);
                //System.out.println("c");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //System.out.println("d");
        try {
            PlayersDB.register(nickname, hashPassword);
            //System.out.println("e");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //System.out.println("f");
        sendRegisterMessage(successfulRegistration);
    }

    private void applyLogin(String nickname, String hashPassword) {
        final int networkError = -1;
        final int successfulLogin = 0;
        final int nicknameError = 1;
        final int passwordError = 2;

        if (nickname == null || hashPassword == null) {
            sendLoginMessage(networkError);
            return;
        }

        try {
            if (!PlayersDB.exists(nickname)) {
                sendRegisterMessage(nicknameError);
                return;
            }
            if (!PlayersDB.checkPassword(nickname, hashPassword)) {
                sendRegisterMessage(passwordError);
                return;
            }
            client.setPlayer(PlayersDB.getPlayer(nickname, hashPassword));
            System.out.println("Player " + client.getPlayer().getName()
                    + " logged in.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        sendLoginMessage(successfulLogin);
        client.getPlayer().sendRating();
    }


    //-----------------------------------------------------------------------

    public void sendTestMessage(String textMessage) {
        ByteArrayOutputStream byteOS = new ByteArrayOutputStream(100);
        DataOutputStream out = new DataOutputStream(byteOS);
        try {
            out.writeInt(TEST_TYPE);
            out.writeUTF(textMessage);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        client.sendMessage(byteOS.toByteArray());
    }

    private void sendRegisterMessage(int resultCode) {
        System.out.println("2");
        ByteArrayOutputStream byteOS = new ByteArrayOutputStream(100);
        DataOutputStream out = new DataOutputStream(byteOS);
        try {
            out.writeInt(REGISTER_TYPE);
            out.writeInt(resultCode);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        client.sendMessage(byteOS.toByteArray());
    }

    private void sendLoginMessage(int resultCode) {
        ByteArrayOutputStream byteOS = new ByteArrayOutputStream(100);
        DataOutputStream out = new DataOutputStream(byteOS);
        try {
            out.writeInt(LOGIN_TYPE);
            out.writeInt(resultCode);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        client.sendMessage(byteOS.toByteArray());
    }
}
