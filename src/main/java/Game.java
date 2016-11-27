import java.sql.Array;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Game {
    public static Map<Long,Game> activeGames;

    private int playersNumber = 0;
    private Player[] players = null;
    private ArrayDeque<Integer> deck = null;
    private int[] scores = null;
    private static final int INITIAL_CARDS_NUMBER = 6;
    private int roundsNumber = 0;

    static {
        activeGames = new ConcurrentHashMap<>();
    }

    Game(Player[] newPlayers, int newCardsNumber, int newRoundsNumber) {
        playersNumber = newPlayers.length;
        players = newPlayers;
        roundsNumber = newRoundsNumber;

        //Generating card deck
        ArrayList<Integer> cards = new ArrayList<Integer>(newCardsNumber);
        for (int i = 0; i < newCardsNumber; i++) {
            cards.add(i);
        }
        Collections.shuffle(cards);
        deck = new ArrayDeque<Integer>(cards);
    }

    public void playGame() {
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
    }

    public class Association {
        private int card;
        private String form;

        Association(int newCard, String newForm) {
            card = newCard;
            form = newForm;
        }

        public int getCard() {
            return card;
        }

        public String getForm() {
            return form;
        }
    }

    private class Round {
        private int leader = -1;
        private Association association = null;
        private ArrayList<Integer> choices = null;
        private int votes[] = null;

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
            association = players[leader].askForAssociation();
            choices.set(leader, association.getCard());
            //TODO: leader's association
        }

        private void playChoice() {
            choices = new ArrayList<Integer>(playersNumber);
            for (int i = 0; i < playersNumber; i++) {
                if (i != leader) {
                    //Very-very bad
                    players[i].askForCard(association.getForm());
                }
            }
            //TODO: here we need to somehow get players' choice
        }

        private void playVote() {
            votes = new int[playersNumber];
            for (int i = 0; i < playersNumber; i++) {
                if (i != leader) {
                    //Very-very bad
                    votes[i] = players[i].getVote(choices);
                }
            }
            //TODO: here we need to get players' votes
        }

        private void countScores() {
            //Sending all the players the leader's association
            for (int i = 0; i < playersNumber; i++) {
                if (i != leader) {
                    players[i].sendLeadersAssociation(association.getCard());
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
