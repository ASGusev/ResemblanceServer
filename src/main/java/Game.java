import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class Game implements Runnable {
    private static final long CHOICE_WAIT_TIME = 60 * 1000;
    private static final int INITIAL_CARDS_NUMBER = 6;

    private final ArrayList<Player> players;
    private final ArrayDeque<Long> deck;
    private final int[] scores;
    private final int roundsNumber;
    private int leader = 0;
    private long leadersCard;
    private String leadersAssociation;
    private final long[] choices;
    private final long[] votes;
    private final Lock choiceLock;
    private final Condition choiceExpectation;
    private int receivedChoices;
    private int receivedVotes;

    Game(ArrayList<Player> players, List<Long> cards, int roundsNumber) {
        this.players = players;
        this.roundsNumber = roundsNumber;

        Collections.shuffle(cards);

        deck = new ArrayDeque<Long>(cards);
        choices = new long[players.size()];
        scores = new int[players.size()];
        votes = new long[players.size()];
        choiceLock = new ReentrantLock();
        choiceExpectation = choiceLock.newCondition();
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
            leader = (leader + 1) % players.size();

            //Sending all the players the leader's association
            if (round < roundsNumber - 1) {
                broadcastRoundEndMessage();
            }

            //Sending new cards if possible
            if (deck.size() >= players.size()) {
                for (Player p: players) {
                    p.sendCard(deck.getLast());
                    deck.removeLast();
                }
            }
        }

        //Preparing a message with game results
        int[] oldRatings = new int[players.size()];
        for (int i = 0; i < players.size(); i++) {
            oldRatings[i] = players.get(i).getRating();
        }

        updateRatings();

        int[] newRatings = new int[players.size()];
        for (int i = 0; i < players.size(); i++) {
            newRatings[i] = players.get(i).getRating();
        }

        byte[] gameFinishMessage = makeGameFinishMessage(oldRatings, newRatings);
        for (Player player: players) {
            player.sendMessage(gameFinishMessage);
        }
    }

    public void setLeadersAssociation(long card, String form) {
        choiceLock.lock();
        try {
            leadersCard = card;
            leadersAssociation = form;
            choiceExpectation.signal();
        } finally {
            choiceLock.unlock();
        }
    }

    public void setChoice(Player player, long card) {
        choiceLock.lock();
        try {
            choices[players.indexOf(player)] = card;
            receivedChoices++;
            if (receivedChoices == players.size()){
                choiceExpectation.signal();
            }
        } finally {
            choiceLock.unlock();
        }
    }

    public void setVote(Player player, long card) {
        choiceLock.lock();
        try {
            votes[players.indexOf(player)] = card;
            receivedVotes++;
            if (receivedVotes == players.size() - 1) {
                choiceExpectation.signal();
            }
        } finally {
            choiceLock.unlock();
        }
    }

    private byte[] makeGameStartMessage() {
        ByteArrayOutputStream byteOS = new ByteArrayOutputStream(100);
        DataOutputStream out = new DataOutputStream(byteOS);
        try {
            out.writeInt(Message.START_GAME_TYPE);
            out.writeInt(roundsNumber);
            out.writeInt(players.size());
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
            out.writeLong(leadersCard);
            out.writeInt(players.size());
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
            out.writeLong(leadersCard);
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

    private void askLeader() {
        choiceLock.lock();
        try {
            leadersCard = -1;
            leadersAssociation = null;
            players.get(leader).askForAssociation();
        }
        finally {
            choiceLock.unlock();
        }
        while (leadersCard == -1) {
            choiceLock.lock();
            try {
                choiceExpectation.await(CHOICE_WAIT_TIME, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
            } finally {
                choiceLock.unlock();
            }
        }
        choices[leader] = leadersCard;
    }

    private void playChoice() {
        choiceLock.lock();
        try {
            for (int i = 0; i < players.size(); i++) {
                if (i != leader) {
                    players.get(i).askForCard(leadersAssociation);
                }
            }
            receivedChoices = 1;
        } finally {
            choiceLock.unlock();
        }
        long deadline = System.currentTimeMillis() + CHOICE_WAIT_TIME;
        while (System.currentTimeMillis() < deadline &&
                receivedChoices < players.size()) {
            choiceLock.lock();
            try {
                choiceExpectation.await(deadline - System.currentTimeMillis(),
                        TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
            } finally {
                choiceLock.unlock();
            }
        }
    }

    private void playVote() {
        choiceLock.lock();
        try {
            for (int i = 0; i < players.size(); i++) {
                if (i != leader) {
                    players.get(i).askForVote(leadersAssociation, choices);
                }
            }
            receivedVotes = 0;
        } finally {
            choiceLock.unlock();
        }

        long deadline = System.currentTimeMillis() + CHOICE_WAIT_TIME;
        while (System.currentTimeMillis() < deadline &&
                receivedVotes < players.size() - 1) {
            choiceLock.lock();
            try {
                choiceExpectation.await(deadline - System.currentTimeMillis(),
                        TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
            } finally {
                choiceLock.unlock();
            }
        }
    }

    private void countScores() {
        int guessed = 0;
        for (int i = 0; i < players.size(); i++) {
            if (i != leader && votes[i] == leadersCard) {
                guessed++;
            }
        }
        if (guessed == 0 || guessed == players.size() - 1) {
            for (int i = 0; i < players.size(); i++) {
                if (i != leader && votes[i] == leadersCard) {
                    scores[i] += 2;
                }
            }
        } else {
            scores[leader] += 3;
            for (int i = 0; i < players.size(); i++) {
                if (i != leader) {
                    if (votes[i] == leadersCard) {
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

    private void updateRatings() {
        final double EPS = 1e-7d;
        double averageScore = Arrays.stream(scores).
                mapToObj(Integer::new).
                collect(Collectors.averagingInt(a -> a));
        double averageRating = players.stream()
                .collect(Collectors.averagingInt(Player::getRating));
        for (int i = 0; i < players.size(); i++) {
            if (scores[i] > averageScore + EPS) {
                int curRating = players.get(i).getRating();
                double addition = (scores[i] - averageScore) * 10;
                if (curRating > averageRating * 1.3) {
                    addition /= 1.5;
                } else if (curRating < averageRating * 0.7) {
                    addition *= 1.5;
                }
                players.get(i).setRating(curRating + (int)(Math.rint(addition) + EPS));
            }
        }
    }
}
