import java.io.*;
import java.net.Socket;
import java.util.stream.Stream;

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
    final public static int LEADERS_ASSOCIATION_TYPE = 13;

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
                readJoinRandomGameMessage(in);
                break;
            case QUIT_RANDOM_GAME_TYPE:
                readQuitRandomGameMessage(in);
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
        long hashPassword = 0;
        try {
            synchronized (in) {
                login = in.readUTF();
                hashPassword = in.readLong();
            }
            //applyRegister(login, hashPassword);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readLoginMessage(DataInputStream in) {
        String login = null;
        long hashPassword = 0;
        try {
            synchronized (in) {
                login = in.readUTF();
                hashPassword = in.readLong();
            }
            //applyLogin(login, hashPassword);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readJoinRandomGameMessage(DataInputStream stream) {
        //RandomGameCreator.addPlayer(<getting player>);
    }

    private void readQuitRandomGameMessage(DataInputStream stream) {
        //RandomGameCreator.removePlayer(<Getting player>);
    }

    private void readLeadAssociationMessage(DataInputStream stream) {
        long card;
        String association;
        try {
            card = stream.readLong();
            association = stream.readUTF();
        } catch (IOException e) {

        }
        //Game.ChoiceMessage message = new Game.ChoiceMessage(<player>, card, association);
        //TODO: Add message to player's game
    }

    private void readChoiceMessage(DataInputStream stream) {
        long card;
        try {
            card = stream.readLong();
            //Game.ChoiceMessage message = new Game.ChoiceMessage(<player>, card);
            //TODO: Add message to pllayer's Game
        } catch (IOException e) {}
    }

    private void readVoteMessage(DataInputStream stream) {
        long card;
        try {
            card = stream.readLong();
            //Game.ChoiceMessage message = new Game.ChoiceMessage(<player>, card);
            //TODO: Add message to pllayer's Game
        } catch (IOException e) {}
    }

    private void applyTest(String textMessage) {
        System.out.println(textMessage);
    }

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
}
