import java.io.*;
import java.util.ArrayList;

public class Main {
    public static ArrayList<Long> DEFAULT_CARD_SETS[] = new ArrayList[1];

    public static void main(String[] args) throws IOException {
        DEFAULT_CARD_SETS[0] = new ArrayList<>();
        DEFAULT_CARD_SETS[0].add(1L);
        DEFAULT_CARD_SETS[0].add(2L);
        DEFAULT_CARD_SETS[0].add(3L);
        DEFAULT_CARD_SETS[0].add(4L);
        DEFAULT_CARD_SETS[0].add(5L);
        DEFAULT_CARD_SETS[0].add(6L);
        DEFAULT_CARD_SETS[0].add(7L);
        DEFAULT_CARD_SETS[0].add(8L);

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
