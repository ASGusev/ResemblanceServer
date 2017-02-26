import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {
    public static List<Long> DEFAULT_CARD_SET;

    public static void main(String[] args) throws IOException {
        PlayersDB.connect();
        ArrayList<Long> defaultCardSet = new ArrayList<Long>();
        for (long i = 1; i < 96; i++) {
            defaultCardSet.add(i);
        }
        DEFAULT_CARD_SET = Collections.unmodifiableList(defaultCardSet);

        MessageModule.run();
    }
}
