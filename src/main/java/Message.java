import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;

public class Message {
    final public static int TEST_TYPE = 0;
    final public static int REGISTER_TYPE = 1;
    final public static int LOGIN_TYPE = 2;
    private final static int JOIN_RANDOM_GAME_TYPE = 3;
    final public static int QUIT_RANDOM_GAME_TYPE = 4;
    final public static int START_GAME_TYPE = 5;
    final public static int SEND_CARD_TYPE = 6;
    final public static int LEAD_REQUEST_TYPE = 7;
    private final static int LEAD_ASSOCIATION_TYPE = 8;
    final public static int CHOICE_REQUEST_TYPE = 9;
    private final static int CHOICE_TYPE = 10;
    final public static int VOTE_REQUEST_TYPE = 11;
    private final static int VOTE_TYPE = 12;
    final public static int ROUND_END_TYPE = 13;
    final public static int RATING_TYPE = 14;
    private final static int CREATE_FRIEND_GAME_TYPE = 15;
    private final static int JOIN_FRIEND_GAME_TYPE = 16;
    final public static int FRIEND_GAME_PLAYER_TYPE = 17;
    private final static int REMOVE_PLAYER_TYPE = 18;
    private final static int CANCEL_FRIEND_GAME_TYPE = 19;
    private final static int START_FRIEND_GAME_TYPE = 20;
    final public static int GAME_FINISH_TYPE = 21;
    final public static int GAME_CANCELED_TYPE = 22;
    private final static int QUIT_FIEND_GAME_TYPE = 23;
    private final static int PASSWORD_CHANGE_REQUEST_TYPE = 24;
    final public static int PASSWORD_CHANGE_RESPONSE_TYPE = 25;

    private final MessageModule.ClientThread client;
    private final int type;

    Message(MessageModule.ClientThread client, int type) {
        this.type = type;
        this.client = client;
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
                readRemoveFriendGamePlayerMessage(in);
                break;
            case QUIT_FIEND_GAME_TYPE:
                readQuitFriendGameMessage();
                break;
            case PASSWORD_CHANGE_REQUEST_TYPE:
                readPasswordChangeRequestMessage(in);
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
            client.getPlayer().getGame().setLeadersAssociation(card, association);
        } catch (IOException e) {}
    }

    private void readChoiceMessage(DataInputStream stream) {
        long card;
        try {
            card = stream.readLong();
            client.getPlayer().getGame().setChoice(client.getPlayer(), card);
        } catch (IOException e) {}
    }

    private void readVoteMessage(DataInputStream stream) {
        long card;
        try {
            card = stream.readLong();
            client.getPlayer().getGame().setVote(client.getPlayer(), card);
        } catch (IOException e) {}
    }

    private void readCreateGameMessage(DataInputStream stream) {
        try {
            int roundsNumber = stream.readInt();
            int setSize = stream.readInt();
            ArrayList <Long> cards = new ArrayList<Long>();
            for (int i = 0; i < setSize; i++) {
                cards.add(stream.readLong());
            }
            long expectationTime = stream.readLong();
            FriendsGameCreator.addGame(client.getPlayer(), roundsNumber, cards,
                    expectationTime);
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
                        sendFriendPlayerMessage(true, client.getPlayer().getName());
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

    private void readRemoveFriendGamePlayerMessage(DataInputStream stream) {
        try {
            String playerName = stream.readUTF();
            FriendsGameCreator.removePlayer(playerName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readQuitFriendGameMessage() {
        try {
            FriendsGameCreator.getGameCreator(client.getPlayer().getName())
                    .sendFriendPlayerMessage(false,
                            client.getPlayer().getName());
            FriendsGameCreator.removePlayer(client.getPlayer().getName());
        } catch (NullPointerException e) {
            System.out.println("Attempt to remove non-existent player.");
        }
    }

    private void readPasswordChangeRequestMessage(DataInputStream stream) {
        final int PASSWORD_CHANGE_SUCCESS = 1;
        final int PASSWORD_CHANGE_FAILURE = 2;
        try {
            String oldPassword = stream.readUTF();
            String newPassword = stream.readUTF();
            if (PlayersDB.checkPassword(client.getPlayer().getName(), oldPassword)) {
                PlayersDB.changePassword(client.getPlayer().getName(),
                        oldPassword, newPassword);
                client.getPlayer().sendPasswordChangeResponseMessage(
                        PASSWORD_CHANGE_SUCCESS);
            } else {
                client.getPlayer().sendPasswordChangeResponseMessage(PASSWORD_CHANGE_FAILURE);
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    //-----------------------------------------------------------------------

    private void applyTest(String textMessage) {
        System.out.println(textMessage);
    }

    private void applyRegister(String nickname, String hashPassword) {
        final int networkError = -1;
        final int successfulRegistration = 0;
        final int nicknameError = 1;

        if (nickname == null || hashPassword == null) {
            sendRegisterMessage(networkError);
            return;
        }

        try {
            if (PlayersDB.exists(nickname)) {
                sendRegisterMessage(nicknameError);
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            PlayersDB.register(nickname, hashPassword);
        } catch (SQLException e) {
            e.printStackTrace();
        }

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
                sendLoginMessage(nicknameError);
                return;
            }
            if (!PlayersDB.checkPassword(nickname, hashPassword)) {
                sendLoginMessage(passwordError);
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
