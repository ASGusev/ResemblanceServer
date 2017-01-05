
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

public class MessageModule {
    private final static int MAX_CNT_TO_RECONNECT = 10;
    private final static int SLEEP_TIME = 1000;
    private final static int PORT = 6662;

    public static void run() {
        try {
            ServerSocket ss = new ServerSocket(PORT);
            System.out.println("Waiting for clients.");
            System.out.println();

            while (!Thread.currentThread().isInterrupted()) {
                Socket socket = ss.accept();
                System.out.println("Got a client");
                System.out.println();

                ClientThread curClient = new ClientThread(socket);
                curClient.start();
            }
        } catch(Exception е) { е.printStackTrace(); }
    }


    public static class ClientThread extends Thread {
        private final Socket socket;
        private final DataInputStream in;
        private final DataOutputStream out;
        private volatile Boolean connectionError = false;
        private Player player = null;

        ClientThread(Socket socket) {
            this.socket = socket;
            DataInputStream inputStream = null;
            for (int i = 0; i < MAX_CNT_TO_RECONNECT && inputStream == null; ++i) {
                try {
                    inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        sleep(SLEEP_TIME);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            in = inputStream;
            DataOutputStream outputStream = null;
            try {
                outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            out = outputStream;
        }

        public Player getPlayer() {
            return player;
        }

        public void setPlayer(Player newPlayer) {
            player = newPlayer;
            player.setClientThread(this);
        }

        @Override
        public void run() {

            //initThread


            if (in == null || out == null) {
                connectionError = true;
                return;
            }

            //-----

            while(true) {
                int typeMessage = -1;
                for (int i = 0; i < MAX_CNT_TO_RECONNECT; ++i) {
                    try {
                        synchronized (in) {
                            typeMessage = in.readInt();
                        }
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            sleep(SLEEP_TIME);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                if (typeMessage == -1) {
                    connectionError = true;
                    return;
                }
                Message newMessage = new Message(this, typeMessage);
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
                    for (int i = 0; i < MAX_CNT_TO_RECONNECT && !messageIsSent; i++) {
                        messageIsSent = tryToSendMessage(message);
                        if (!messageIsSent) {
                            try {
                                sleep(SLEEP_TIME);
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
