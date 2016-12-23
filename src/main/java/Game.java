import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

public class Game implements Runnable {
    private static final long PERIOD = 3000;
    private static final long CHOICE_WAIT_TIME = 60 * 1000;
    private static final long VOTE_WAIT_TIME = 60 * 1000;
    private static final int INITIAL_CARDS_NUMBER = 2;

    private int playersNumber = 0;
    private ArrayList<Player> players = null;
    private ArrayDeque<Long> deck = null;
    private int[] scores = null;
    private int roundsNumber = 0;
    private final ArrayDeque<ChoiceMessage> messages = new ArrayDeque<>();
    private final Date clock = new Date();
    int leader = 0;

    private Association association = null;
    private long[] choices = null;
    private long[] votes = null;

    Game(ArrayList<Player> newPlayers, ArrayList<Long> cards, int newRoundsNumber) {
        playersNumber = newPlayers.size();
        players = newPlayers;
        roundsNumber = newRoundsNumber;

        Collections.shuffle(cards);

        deck = new ArrayDeque<Long>(cards);
        choices = new long[playersNumber];
        scores = new int[playersNumber];
        votes = new long[playersNumber];
    }

    public void run() {
        byte[] gameStartMessage = makeGameStartMessage();

        for(Player p: players) {
            p.setGame(this);
            p.sendMessage(gameStartMessage);
        }

        //Handing players their initial cards
        for (int i = 0; i < INITIAL_CARDS_NUMBER; i++) {
            for (Player p: players) {
                p.sendCard(deck.getLast());
                deck.removeLast();
            }
        }

        //Playing game, round-by-round
        for (int round = 0; round < roundsNumber; round++) {
            //System.out.println("Round.");
            askLeader();
            //System.out.println("Lead.");
            playChoice();
            //System.out.println("Chosen.");
            playVote();
            //System.out.println("Voted.");
            countScores();
            //System.out.println("Scored.");
            leader = (leader + 1) % playersNumber;

            if (deck.size() >= playersNumber) {
                for (Player p: players) {
                    p.sendCard(deck.getLast());
                    deck.removeLast();
                }
            }
        }

        //TODO: Recalculate ratings
    }

    private byte[] makeGameStartMessage() {
        ByteArrayOutputStream byteOS = new ByteArrayOutputStream(100);
        DataOutputStream out = new DataOutputStream(byteOS);
        try {
            out.writeInt(Message.START_GAME_TYPE);
            out.writeInt(roundsNumber);
            out.writeInt(playersNumber);
            for (Player p: players) {
                out.writeUTF(p.getName());
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteOS.toByteArray();
    }

    public void addMessage(ChoiceMessage message) {
        synchronized (messages) {
            messages.addLast(message);
        }
    }

    public class Association {
        private long card;
        private String form;

        Association(long newCard, String newForm) {
            card = newCard;
            form = newForm;
        }

        public long getCard() {
            return card;
        }

        public String getForm() {
            return form;
        }
    }

    public static class ChoiceMessage {
        private Player player;
        private long card;
        private String association;

        ChoiceMessage(Player player, long card) {
            this(player, card, null);
        }

        ChoiceMessage(Player player, long card, String association) {
            this.player = player;
            this.card = card;
            this.association = association;
        }

        public Player getPlayer() {
            return player;
        }

        public long getCard() {
            return card;
        }

        public String getAssociation() {
            return association;
        }
    }

    private void askLeader() {
        players.get(leader).askForAssociation();
        boolean received = false;
        while (!received) {
            try {
                Thread.sleep(PERIOD);
            } catch (InterruptedException e) {
            } finally {
                synchronized (messages) {
                    if (!messages.isEmpty()) {
                        received = true;
                        ChoiceMessage message = messages.getLast();
                        messages.removeLast();
                        association = new Association(message.getCard(),
                                message.getAssociation());
                    }
                }
            }
        }
        choices[leader] = association.getCard();
    }

    private void playChoice() {
        for (int i = 0; i < playersNumber; i++) {
            if (i != leader) {
                players.get(i).askForCard(association.getForm());
            }
        }

        long startTime = clock.getTime();
        int receivedChoices = 1;
        while (clock.getTime() < startTime + CHOICE_WAIT_TIME &&
                receivedChoices < playersNumber) {
            try {
                Thread.sleep(PERIOD);
            } catch (InterruptedException e){}
            synchronized (messages) {
                while (!messages.isEmpty()) {
                    ChoiceMessage message = messages.getFirst();
                    messages.removeFirst();

                    int playerIndex = players.indexOf(message.getPlayer());

                    choices[playerIndex] = message.getCard();
                    receivedChoices++;
                }
            }
        }
        //TODO: do something with disconnected players
    }

    private void playVote() {
        for (int i = 0; i < playersNumber; i++) {
            if (i != leader) {
                players.get(i).askForVote(association.getForm(), choices);
            }
        }

        long startTime = clock.getTime();
        int receivedVoices = 0;
        while (clock.getTime() < startTime + VOTE_WAIT_TIME &&
                receivedVoices < playersNumber - 1) {
            try {
                Thread.sleep(PERIOD);
            } catch (InterruptedException e) {}
            synchronized (messages) {
                while (!messages.isEmpty()) {
                    ChoiceMessage message = messages.getFirst();
                    messages.removeFirst();

                    int playerIndex = players.indexOf(message.getPlayer());

                    votes[playerIndex] = message.getCard();
                    receivedVoices++;
                }
            }
        }
        //TODO: again, do something with disconnected players
    }

    private void countScores() {
        //Updating scores
        int guessed = 0;
        for (int i = 0; i < playersNumber; i++) {
            if (i != leader && votes[i] == association.getCard()) {
                guessed++;
            }
        }
        if (guessed == 0 || guessed == playersNumber - 1) {
            for (int i = 0; i < playersNumber; i++) {
                if (i != leader && votes[i] == association.getCard()) {
                    scores[i] += 2;
                }
            }
        } else {
            scores[leader] += 3;
            for (int i = 0; i < playersNumber; i++) {
                if (i != leader) {
                    if (votes[i] == association.getCard()) {
                        scores[i] += 3;
                    } else {
                        int voteIndex = 0;
                        while (choices[voteIndex] != votes[i]) {
                            voteIndex++;
                        }
                        scores[voteIndex] += 1;
                    }
                }
            }
        }

        //Sending all the players the leader's association
        for (int i = 0; i < playersNumber; i++) {
            players.get(i).sendRoundEndMessage(association.getCard(), scores);
        }
    }
}
