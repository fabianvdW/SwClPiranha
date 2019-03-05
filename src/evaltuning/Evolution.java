package evaltuning;

import artificialplayer.AlphaBeta;
import artificialplayer.PrincipalVariation;
import artificialplayer.Search;
import game.*;
import sun.reflect.annotation.ExceptionProxy;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class Evolution implements Serializable {
    static final long serialVersionUID = 42L;
    public static int processes = 4;
    public Genome[] population;
    public Genome[] best8;

    public Genome sufiWinner;
    public int consecutiveWins;

    public int generation = 0;
    public double mutateStaerke;
    public ArrayList<Match> matchQueue = new ArrayList<>(32);
    public ArrayList<Match> matchResultsQueue = new ArrayList<>(32);

    public Evolution() {
        this.mutateStaerke = 1;
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
        Genome[] top16toTop9 = winnersAndLosers[1];

        makeQueue(this.population, fillIndex(8), 2);
        winnersAndLosers = playQueue();
        this.population = winnersAndLosers[0];
        Genome[] top8toTop5 = winnersAndLosers[1];

        makeQueue(this.population, fillIndex(4), 2);
        winnersAndLosers = playQueue();
        this.population = winnersAndLosers[0];
        Genome[] top4ToTop3 = winnersAndLosers[1];

        makeQueue(this.population, fillIndex(2), 10);
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
        for (int i = 0; i < 8; i++) {
            this.population[i] = best8[i];
        }
        for (int i = 0; i < 8; i++) {
            this.population[8 + i] = top16toTop9[i];
        }
        int index = 16;
        for (int i = 0; i < 16; i++) {
            this.population[index + 2 * i] = this.population[i].mutate(this.mutateStaerke, 1);
            this.population[index + 2 * i + 1] = this.population[i].mutate(this.mutateStaerke, 2);
        }
        index = 48;
        Random r = new Random();
        for (int i = 0; i < 15; i++) {
            int index1 = (int) (Math.abs(r.nextGaussian() * 5));
            index1 = Math.min(15, index1);
            int index2;
            do {
                index2 = (int) (Math.abs(r.nextGaussian() * 5));
                index2 = Math.min(15, index2);
            } while (index1 == index2);
            this.population[index + i] = this.population[index1].crossover(this.population[index2]);
        }

        //Sufi
        Match sufi = null;
        if (this.sufiWinner == null) {
            this.sufiWinner = this.population[0];
        } else {
            sufi = new Match(this.sufiWinner, this.population[0], 50);
            int gProProcessor = 50 / processes;
            int p1Additional = 50 % processes;
            MatchPlayer[] matches = new MatchPlayer[processes];
            for (int i = 0; i < processes; i++) {
                matches[i] = new MatchPlayer(new Match(sufi.g1, sufi.g2, gProProcessor + (i == 0 ? p1Additional : 0)));
            }
            for (int i = 0; i < processes; i++) {
                try {
                    matches[i].join();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            for (int i = 0; i < processes; i++) {
                sufi.g1Score += matches[i].m.g1Score;
                sufi.g2Score += matches[i].m.g2Score;
            }

            if (sufi.g1Score >= sufi.g2Score) {
                consecutiveWins++;
            } else {
                this.sufiWinner = this.population[0];
                consecutiveWins = 1;
            }
        }

        System.out.println("Super final has been played! Winner:");
        System.out.println(this.sufiWinner.toString());
        System.out.println("Consecutive Wins: " + this.consecutiveWins);
        if (sufi != null) {
            System.out.println("Last Win: " + sufi.g1Score + " -" + sufi.g2Score);
        }

        this.population[63] = this.sufiWinner.mutate(this.mutateStaerke, 1);
        this.generation += 1;
        this.mutateStaerke -= 0.01;
        if (this.mutateStaerke < 0.1) {
            this.mutateStaerke = 0.1;
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
        QueuePlayer[] processes = new QueuePlayer[Evolution.processes];
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
            } else if (i == 2 || i == 3) {
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
        //ev.savePopulation();
        ev = Evolution.loadPopulation();
        for (int i = 0; i < 1000; i++) {
            long now = System.currentTimeMillis();
            ev.doGeneration();
            long curr = System.currentTimeMillis();
            System.out.println("Generation " + (ev.generation) + " done in " + (curr - now) + " ms! Best 8: ");
            System.out.println(ev.printBest8());
            ev.savePopulation();
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
        m.g1Score = p1Score;
        m.g2Score = p2Score;
        return p1Score > p2Score;
    }

    public void savePopulation() {
        String path = "checkpoint.txt";
        try {
            FileOutputStream f = new FileOutputStream(new File(path));
            ObjectOutputStream o = new ObjectOutputStream(f);
            o.writeObject(this);
            o.close();
            f.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Evolution loadPopulation() {
        String path = "checkpoint.txt";
        try {
            FileInputStream fis = new FileInputStream(new File(path));
            ObjectInputStream ois = new ObjectInputStream(fis);
            Evolution ev = (Evolution) ois.readObject();
            return ev;
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert (false);
        return null;
    }
}

class MatchPlayer extends Thread {
    Match m;

    public MatchPlayer(Match m) {
        this.m = m;
        this.start();
    }

    public void run() {
        Evolution.playGame(m);
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

class Match implements Serializable {
    static final long serialVersionUID = 42L;
    Genome g1;
    Genome g2;
    double g1Score;
    double g2Score;
    int firstTo;
    boolean result;

    public Match(Genome g1, Genome g2, int firstTo) {
        this.g1 = g1;
        this.g2 = g2;
        this.firstTo = firstTo;
    }
}