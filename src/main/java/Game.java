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
    private final ArrayDeque<ChoiceMessage> choiceMessages = new ArrayDeque<>();
    private final Date clock = new Date();
    private int leader = 0;

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

        for(Player player: players) {
            player.setGame(this);
            player.sendMessage(gameStartMessage);
        }

        //Handing players their initial cards
        for (int i = 0; i < INITIAL_CARDS_NUMBER; i++) {
            for (Player player: players) {
                player.sendCard(deck.getLast());
                deck.removeLast();
            }
        }

        //Playing game, round-by-round
        for (int round = 0; round < roundsNumber; round++) {
            askLeader();
            playChoice();
            playVote();
            countScores();
            leader = (leader + 1) % playersNumber;

            //Sending all the players the leader's association
            if (round < roundsNumber - 1) {
                broadcastRoundEndMessage();
            }

            //Sending new cards if possible
            if (deck.size() >= playersNumber) {
                for (Player p: players) {
                    p.sendCard(deck.getLast());
                    deck.removeLast();
                }
            }
        }

        //Preparing a message with game results
        int[] oldRatings = new int[playersNumber];
        for (int i = 0; i < playersNumber; i++) {
            oldRatings[i] = players.get(i).getRating();
        }
        //TODO: Recalculate ratings
        int[] newRatings = new int[playersNumber];
        for (int i = 0; i < playersNumber; i++) {
            newRatings[i] = players.get(i).getRating();
        }

        byte[] gameFinishMessage = makeGameFinishMessage(oldRatings, newRatings);
        for (Player player: players) {
            player.sendMessage(gameFinishMessage);
        }
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

    private byte[] makeGameFinishMessage(int[] oldRatings, int[] newRatings) {
        ByteArrayOutputStream byteOS = new ByteArrayOutputStream(100);
        DataOutputStream out = new DataOutputStream(byteOS);
        try {
            out.writeInt(Message.GAME_FINISH_TYPE);
            out.writeLong(association.getCard());
            out.writeInt(playersNumber);
            for (int score: scores) {
                out.writeInt(score);
            }
            for (int rating: oldRatings) {
                out.writeInt(rating);
            }
            for (int rating: newRatings) {
                out.writeInt(rating);
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteOS.toByteArray();
    }

    private void broadcastRoundEndMessage() {
        ByteArrayOutputStream byteOS = new ByteArrayOutputStream(100);
        DataOutputStream out = new DataOutputStream(byteOS);
        try {
            out.writeInt(Message.ROUND_END_TYPE);
            out.writeLong(association.getCard());
            out.writeInt(scores.length);
            for (int score: scores) {
                out.writeInt(score);
            }
            out.flush();

            byte[] message = byteOS.toByteArray();
            for (Player player: players) {
                player.sendMessage(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addChoiceMessage(ChoiceMessage message) {
        synchronized (choiceMessages) {
            choiceMessages.addLast(message);
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
                synchronized (choiceMessages) {
                    if (!choiceMessages.isEmpty()) {
                        received = true;
                        ChoiceMessage message = choiceMessages.getLast();
                        choiceMessages.removeLast();
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
            synchronized (choiceMessages) {
                while (!choiceMessages.isEmpty()) {
                    ChoiceMessage message = choiceMessages.getFirst();
                    choiceMessages.removeFirst();

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
            synchronized (choiceMessages) {
                while (!choiceMessages.isEmpty()) {
                    ChoiceMessage message = choiceMessages.getFirst();
                    choiceMessages.removeFirst();

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
    }
}
