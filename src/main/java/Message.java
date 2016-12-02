import java.io.*;
import java.net.Socket;

public class Message {
    final public static int TEST_TYPE = 0;
    final public static int REGISTER_TYPE = 1;
    final public static int LOGIN_TYPE = 2;

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
