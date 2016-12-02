
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class MessageModule {
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

    public static class ClientThread extends Thread {
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

            new Message(this, Message.TEST_TYPE).sendTestMessage("test message from server");

            while(true) {
                int typeMessage = -1;
                for (int i = 0; i < maxCntToReconnect; ++i) {
                    try {
                        synchronized (in) {
                            typeMessage = in.readInt();
                        }
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
                if (typeMessage == -1) {
                    synchronized (connectionError) {
                        connectionError = true;
                    }
                    return;
                }
                Message newMessage = new Message(typeMessage);
                newMessage.readMessage(in);
            }
        }

        synchronized private boolean tryToSendMessage(byte[] message) {
            try {
                out.write(message);
                out.flush();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        public void sendMessage(final byte[] message) {
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
}
