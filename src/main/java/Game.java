import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Game implements Runnable {
    public static Map<Long,Game> activeGames;

    private static final long PERIOD = 3000;
    private static final long CHOICE_WAIT_TIME = 60 * 1000;
    private static final long VOTE_WAIT_TIME = 60 * 1000;

    private int playersNumber = 0;
    private ArrayList<Player> players = null;
    private ArrayDeque<Long> deck = null;
    private int[] scores = null;
    private static final int INITIAL_CARDS_NUMBER = 6;
    private int roundsNumber = 0;
    private final ArrayDeque<ChoiceMessage> messages = new ArrayDeque<>();
    private final Date clock = new Date();

    static {
        activeGames = new ConcurrentHashMap<>();
    }

    Game(ArrayList<Player> newPlayers, ArrayList<Long> cards, int newRoundsNumber) {
        playersNumber = newPlayers.size();
        players = newPlayers;
        roundsNumber = newRoundsNumber;

        Collections.shuffle(cards);

        deck = new ArrayDeque<Long>(cards);
    }

    public void run() {
        for(Player p: players) {
            p.sendGameStart();
        }
        scores = new int[playersNumber];

        //Handing players their initial cards
        for (int i = 0; i < INITIAL_CARDS_NUMBER; i++) {
            for (Player p: players) {
                p.sendCard(deck.getLast());
                deck.removeLast();
            }
        }

        int leader = 0;
        for (int round = 0; round < roundsNumber; round++) {
            Round curRound = new Round(leader);
            curRound.playRound();
            leader = (leader + 1) % playersNumber;
        }

        //TODO: Recalculate ratings
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

    private class Round {
        private int leader = -1;
        private Association association = null;
        private ArrayList<Long> choices = new ArrayList<Long>(playersNumber);
        private long votes[] = new long[playersNumber];

        Round(int newLeader) {
            leader = newLeader;
        }

        protected void playRound() {
            askLeader();
            playChoice();
            playVote();
            countScores();
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
            choices.set(leader, association.getCard());
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

                        choices.set(playerIndex, message.getCard());
                        receivedChoices++;
                    }
                }
            }
            //TODO: do something with disconnected players
        }

        private void playVote() {
            for (int i = 0; i < playersNumber; i++) {
                if (i != leader) {
                    players.get(i).askForVote(choices);
                }
            }

            long startTime = clock.getTime();
            int receivedVoices = 0;
            while (clock.getTime() < startTime && receivedVoices < playersNumber - 1) {
                try {
                    Thread.sleep(PERIOD);
                } catch (InterruptedException e) {}
                synchronized (messages) {
                    while (!messages.isEmpty()) {
                        ChoiceMessage message = messages.getFirst();
                        messages.removeFirst();

                        int playerIndex = players.indexOf(message.getPlayer());

                        votes[playerIndex] = message.getCard();
                    }
                }
            }
            //TODO: we still need to do something with disconnected players
        }

        private void countScores() {
            //Sending all the players the leader's association
            for (int i = 0; i < playersNumber; i++) {
                if (i != leader) {
                    players.get(i).sendLeadersAssociation(association.getCard());
                }
            }

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
                            scores[choices.indexOf(votes[i])] += 1;
                        }
                    }
                }
            }
        }
    }
}
