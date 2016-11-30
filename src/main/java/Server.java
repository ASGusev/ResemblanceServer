
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {
    private static HashMap<String, ClientThread> clientTreadFromName = new HashMap<>();
    private static HashMap<Socket, String> clientNameFromSocket = new HashMap<>();

    public static void start() {
        Thread curThread = new Thread() {
            @Override
            public void run() {
                System.out.println(3);
                int port = 6679;
                try {
                    ServerSocket ss = new ServerSocket(port);
                    System.out.println("Wait a client");
                    System.out.println();

                    while (true) {
                        Socket socket = ss.accept();
                        System.out.println("Got a client");
                        System.out.println();

                        ClientThread curClient = new ClientThread(socket);
                        curClient.start();
                    }
                } catch(Exception е) { System.exit(-1);е.printStackTrace(); }
            }
        };
        curThread.start();
    }


    public static String getName(Socket socket) {
        if (socket == null) {
            return null;
        }
        return  clientNameFromSocket.get(socket);
    }

    public static void sendMessage(String name, String message) {
        if (name != null && message != null) {
            ClientThread clientThread = clientTreadFromName.get(name);
            if (clientThread != null) {
                clientThread.sendMessage(message);
            }
        }
    }
    public static void sendMessage(Socket socket, String message) {
        sendMessage(clientNameFromSocket.get(socket), message);
    }
    public static void sendMessage(String name, Message message) {
        sendMessage(name, message.getStringMessage());
    }
    public static void sendMessage(Socket socket, Message message) {
        sendMessage(socket, message.getStringMessage());
    }

    private static class ClientThread extends Thread {
        Socket socket;
        DataInputStream in = null;
        DataOutputStream out = null;
        Integer cnt = 0;

        ClientThread(Socket socket) {
            this.socket = socket;
            try {
                in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            }
            catch (IOException e) {
                System.out.println("Bad socket: " + socket);
                this.stop();
            }

        }

        public Socket getSocket() {
            return socket;
        }
        public String getCleintName() {
            return Server.getName(socket);
        }

        @Override
        public void run() {
            //System.exit(-1);
            while(true) {
                try {
                    if (!socket.isConnected())
                        System.exit(-1);
                    String textNewMessage = in.readUTF();
                    System.exit(-1);
                    Message newMessage = new Message(textNewMessage, socket);
                    newMessage.applyMessage();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        synchronized private void sendMessage(String message) {
            try {
                out.writeUTF(message);
                out.flush();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public static Message TEST_MESSAGE = new Message("test test");
}
