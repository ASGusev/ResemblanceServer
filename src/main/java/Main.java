import java.io.*;
import java.util.ArrayList;

public class Main {
    public static ArrayList<Long> DEFAULT_CARD_SETS[] = new ArrayList[1];

    public static void main(String[] args) throws IOException {
        DEFAULT_CARD_SETS[0] = new ArrayList<>();
        for (long i = 9; i < 46; i++) {
            DEFAULT_CARD_SETS[0].add(i);
        }

        MessageModule.start();
        Object waitingObject = new Object();
        while (true) {
            try {
                synchronized (waitingObject) {
                    waitingObject.wait();
                }
            } catch (InterruptedException e) {}
        }
    }
}
