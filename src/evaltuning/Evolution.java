package evaltuning;

import artificialplayer.AlphaBeta;
import artificialplayer.PrincipalVariation;
import artificialplayer.Search;
import game.*;

import java.util.ArrayList;

public class Evolution {
    public final static int processes = 4;
    public Genome[] population;
    public Genome[] best8;
    public int generation = 0;
    public double mutateStaerke;
    public ArrayList<Match> matchQueue = new ArrayList<>(32);
    public ArrayList<Match> matchResultsQueue = new ArrayList<>(32);

    public Evolution() {
        this.mutateStaerke = 1.5;
        this.population = new Genome[64];
        this.best8 = new Genome[8];
        for (int i = 0; i < this.population.length; i++) {
            this.population[i] = new Genome();
        }
    }

    public synchronized Match getMatch() {
        Match res = matchQueue.get(0);
        matchQueue.remove(0);
        return res;
    }

    public synchronized void addMatch(Match m) {
        matchResultsQueue.add(m);
    }

    public void doGeneration() {
        //Play in queue
        makeQueue(this.population, fillIndex(64), 2);
        Genome[][] winnersAndLosers = playQueue();
        this.population = winnersAndLosers[0];

        makeQueue(this.population, fillIndex(32), 2);
        winnersAndLosers = playQueue();
        this.population = winnersAndLosers[0];

        makeQueue(this.population, fillIndex(16), 2);
        winnersAndLosers = playQueue();
        this.population = winnersAndLosers[0];

        makeQueue(this.population, fillIndex(8), 2);
        winnersAndLosers = playQueue();
        this.population = winnersAndLosers[0];
        Genome[] top8toTop5 = winnersAndLosers[1];

        makeQueue(this.population, fillIndex(4), 2);
        winnersAndLosers = playQueue();
        this.population = winnersAndLosers[0];
        Genome[] top4ToTop3 = winnersAndLosers[1];

        makeQueue(this.population, fillIndex(2), 3);
        winnersAndLosers = playQueue();
        this.population = winnersAndLosers[0];
        Genome top2 = winnersAndLosers[1][0];
        assert (this.population.length == 1);
        best8[0] = this.population[0];
        best8[1] = top2;
        best8[2] = top4ToTop3[0];
        best8[3] = top4ToTop3[1];
        best8[4] = top8toTop5[0];
        best8[5] = top8toTop5[1];
        best8[6] = top8toTop5[2];
        best8[7] = top8toTop5[3];
        //Re seed next generation
        this.population = new Genome[64];
        this.population[0] = best8[0];
        for (int i = 1; i <= 9; i++) {
            this.population[i] = best8[0].mutate(this.mutateStaerke);
        }
        for (int i = 10; i <= 15; i++) {
            this.population[i] = best8[0].crossover(best8[1]);
        }
        for (int i = 16; i <= 19; i++) {
            this.population[i] = best8[0].crossover(best8[2]);
        }
        for (int i = 20; i <= 23; i++) {
            this.population[i] = best8[0].crossover(best8[3]);
        }
        int index = 24;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 2; j++) {
                this.population[index + j + i * 2] = best8[0].crossover(best8[4 + i]);
            }
        }
        assert (this.population[32] == null);
        this.population[32] = best8[1];
        for (int i = 33; i <= 39; i++) {
            this.population[i] = best8[1].mutate(this.mutateStaerke);
        }
        index = 40;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                this.population[index + j + i * 2] = best8[1].crossover(best8[2 + i]);
            }
        }
        index = 44;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 1; j++) {
                this.population[index + j + i] = best8[1].crossover(best8[4 + i]);
            }
        }
        assert (this.population[48] == null);
        this.population[48] = best8[2];
        for (int i = 49; i <= 52; i++) {
            this.population[i] = best8[2].mutate(this.mutateStaerke);
        }
        this.population[53] = best8[2].crossover(best8[3]);
        this.population[54] = best8[3];
        for (int i = 55; i <= 58; i++) {
            this.population[i] = best8[3].mutate(this.mutateStaerke);
        }
        this.population[59] = best8[3].crossover(best8[4]);
        this.population[60] = best8[4];
        this.population[61] = best8[5];
        this.population[62] = best8[6];
        this.population[63] = best8[7];
        this.generation += 1;
        this.mutateStaerke -= 0.01;
        if (this.mutateStaerke < 0.3) {
            this.mutateStaerke = 0.3;
        }
    }

    public Genome[][] playQueue() {
        int aspiredSize = this.matchQueue.size();
        Genome[] winners = new Genome[aspiredSize];
        Genome[] losers = new Genome[aspiredSize];
        Genome[][] res = new Genome[2][aspiredSize];
        res[0] = winners;
        res[1] = losers;
        //Start the processes
        QueuePlayer[] processes = new QueuePlayer[4];
        for (int i = 0; i < Evolution.processes; i++) {
            processes[i] = new QueuePlayer(this);
        }
        try {
            for (int i = 0; i < processes.length; i++) {
                processes[i].join();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-2);
        }
        //Finished matches are here
        assert (this.matchQueue.size() == 0);
        assert (this.matchResultsQueue.size() == aspiredSize);
        for (int i = 0; i < aspiredSize; i++) {
            Match m = matchResultsQueue.get(i);
            if (m.result) {
                winners[i] = m.g1;
                losers[i] = m.g2;
            } else {
                winners[i] = m.g2;
                losers[i] = m.g1;
            }
        }
        this.matchResultsQueue.clear();
        return res;
    }

    public void makeQueue(Genome[] population, ArrayList<Integer> indexes, int firstTo) {
        while (indexes.size() > 0) {
            int g1 = indexes.get((int) (Math.random() * indexes.size()));
            indexes.remove(new Integer(g1));
            int g2 = indexes.get((int) (Math.random() * indexes.size()));
            indexes.remove(new Integer(g2));
            matchQueue.add(new Match(population[g1], population[g2], firstTo));
        }
    }

    public ArrayList<Integer> fillIndex(int until) {
        ArrayList<Integer> res = new ArrayList<>(until);
        for (int i = 0; i < until; i++) {
            res.add(i);
        }
        return res;
    }

    public String printBest8() {
        String res = "";
        for (int i = 0; i < best8.length; i++) {
            String place = "";
            if (i == 0) {
                place = "1";
            } else if (i == 1) {
                place = "2";
            } else if (i == 2) {
                place = "3-4";
            } else {
                place = "5-8";
            }
            res += "Top " + place + " : \n" + best8[i].toString() + "\n---------------------\n";
        }
        return res;
    }

    public static void main(String[] args) {
        BitBoardConstants.setSquareAttackDirectionSquareDestinationAttackLine("SwClPiranha/src/game/data.txt");
        Evolution ev = new Evolution();
        for (int i = 0; i < 300; i++) {
            long now = System.currentTimeMillis();
            ev.doGeneration();
            long curr = System.currentTimeMillis();
            System.out.println("Generation " + (i + 1) + " done in " + (curr - now) + " ms! Best 8: ");
            System.out.println(ev.printBest8());
        }
    }

    public static boolean playGame(Match m) {
        int firstTo = m.firstTo;
        Genome g1 = m.g1;
        Genome g2 = m.g2;
        double p1Score = 0;
        double p2Score = 0;
        boolean g1Starts = Math.random() < 0.5;
        while (p1Score == p2Score || p1Score < firstTo && p2Score < firstTo) {
            //Play a game
            MyGameState mg = new MyGameState();
            mg.analyze();
            A:
            while (mg.gs == GameStatus.INGAME) {
                GameMoveResultObject gmro = mg.gmro;
                Search s = new Search(mg, 3);
                if (mg.move == GameColor.RED) {
                    if (g1Starts) {
                        AlphaBeta.brc = g1.brc;
                    } else {
                        AlphaBeta.brc = g2.brc;
                    }
                } else {
                    if (g1Starts) {
                        AlphaBeta.brc = g2.brc;
                    } else {
                        AlphaBeta.brc = g1.brc;
                    }
                }
                s.run();
                PrincipalVariation pv = s.currentBestPv;
                GameMove mv = pv.stack.get(0);
                for (int i = 0; i < gmro.instances; i++) {
                    if (gmro.moves[i].to == mv.to && gmro.moves[i].from == mv.from) {
                        mg = gmro.states[i];
                        mg.analyze();
                        continue A;
                    }
                }
                System.exit(-1);
            }
            if (mg.gs == GameStatus.DRAW) {
                p1Score += 0.5;
                p2Score += 0.5;
            } else if (mg.gs == GameStatus.RED_WIN) {
                if (g1Starts) {
                    p1Score += 1;
                } else {
                    p2Score += 1;
                }
            } else if (mg.gs == GameStatus.BLUE_WIN) {
                if (g1Starts) {
                    p2Score += 1;
                } else {
                    p1Score += 1;
                }
            }
            g1Starts = !g1Starts;
        }
        return p1Score > p2Score;
    }
}

class QueuePlayer extends Thread {
    Evolution ev;

    public QueuePlayer(Evolution ev) {
        this.ev = ev;
        start();
    }

    public void run() {
        while (ev.matchQueue.size() > 0) {
            Match m = ev.getMatch();
            m.result = Evolution.playGame(m);
            ev.addMatch(m);
        }
    }
}

class Match {
    Genome g1;
    Genome g2;
    int firstTo;
    boolean result;

    public Match(Genome g1, Genome g2, int firstTo) {
        this.g1 = g1;
        this.g2 = g2;
        this.firstTo = firstTo;
    }
}