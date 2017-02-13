import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {
    public static final List<Long> DEFAULT_CARD_SETS[] = new List[1];

    public static void main(String[] args) throws IOException {
        ArrayList<Long> defaultCardSet = new ArrayList<Long>();
        for (long i = 1; i < 96; i++) {
            defaultCardSet.add(i);
        }
        DEFAULT_CARD_SETS[0] = Collections.unmodifiableList(defaultCardSet);

        MessageModule.run();
    }
}
