import java.util.ArrayList;

public class Main {
    public static ArrayList<Long> DEFAULT_CARD_SET = new ArrayList<>();

    static {
        DEFAULT_CARD_SET.add(1L);
        DEFAULT_CARD_SET.add(2L);
        DEFAULT_CARD_SET.add(3L);
        DEFAULT_CARD_SET.add(4L);
        DEFAULT_CARD_SET.add(5L);
        DEFAULT_CARD_SET.add(6L);
        DEFAULT_CARD_SET.add(7L);
        DEFAULT_CARD_SET.add(8L);
    }

    public static void main(String[] args) {
        //Starting network part

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
