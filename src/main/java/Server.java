
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {
    private final static int maxCntToReconnect = 10;
    private final static int sleepTime = 1000;
    private static HashMap<String, ClientThread> clientTreadFromName = new HashMap<>();
    private static HashMap<Socket, String> clientNameFromSocket = new HashMap<>();

    public static void start() {
        Thread curThread = new Thread() {
            @Override
            public void run() {
                System.out.println(3);
                int port = 6662;
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
                } catch(Exception е) { е.printStackTrace(); }
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
        private Socket socket;
        private DataInputStream in = null;
        private DataOutputStream out = null;
        private Boolean connectionError = false;
        private String clientName = null;

        ClientThread(Socket socket) {
            this.socket = socket;
        }

        public Socket getSocket() {
            return socket;
        }
        public String getCleintName() {
            return clientName;
        }
        public boolean isConnected() {
            return !connectionError;
        }

        @Override
        public void run() {

            //initThread

            for (int i = 0; i < maxCntToReconnect; ++i) {
                try {
                    if (in == null) {
                        in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                    }
                    if (out == null) {
                        out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        sleep(sleepTime);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    continue;
                }
                break;
            }
            if (in == null || out == null) {
                connectionError = true;
                return;
            }

            //-----

            while(true) {
                String textNewMessage = null;
                for (int i = 0; i < maxCntToReconnect; ++i) {
                    try {
                        textNewMessage = in.readUTF();
                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            sleep(sleepTime);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        continue;
                    }
                    break;
                }
                if (textNewMessage == null) {
                    synchronized (connectionError) {
                        connectionError = true;
                    }
                    return;
                }
                System.out.println(textNewMessage);
                Message newMessage = new Message(textNewMessage, socket);
                newMessage.applyMessage();
            }
        }

        synchronized private boolean tryToSendMessage(String message) {
            try {
                out.writeUTF(message);
                out.flush();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        private void sendMessage(final String message) {
            synchronized (connectionError) {
                if (connectionError) {
                    return;
                }
            }
            Thread curThread =  new Thread() {
                @Override
                public void run() {
                    boolean messageIsSent = false;
                    for (int i = 0; i < maxCntToReconnect && !messageIsSent; i++) {
                        messageIsSent = tryToSendMessage(message);
                        if (!messageIsSent) {
                            try {
                                sleep(sleepTime);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (!messageIsSent) {
                        synchronized (connectionError) {
                            connectionError = true;
                        }
                    }
                }
            };
            curThread.start();
        }
    }
    public static Message TEST_MESSAGE = new Message("test test");
}
