package testing;

import artificialplayer.BoardRating;
import datastructures.BitBoard;
import evaltuning.TexelTuning;
import game.*;
import helpers.FEN;
import helpers.GlobalFlags;
import helpers.logging.Log;
import helpers.logging.LogLevel;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class TestInstance {
    public static ArrayList<TestGames> threads = new ArrayList<>();
    public static int millisTime;
    static Log l;
    static Log ergebnisLog;
    static ArrayList<BitBoard> krakenPositions;

    public static synchronized void fillKrakenPositions() {
        krakenPositions = new ArrayList<>();
        for (int i = 0; i < BitBoardConstants.KRAKEN_POSITIONS.length; i++) {
            krakenPositions.add(BitBoardConstants.KRAKEN_POSITIONS[i]);
        }
    }

    public static synchronized BitBoard drawKrakenPosition() {
        if (krakenPositions == null || krakenPositions.size() == 0) {
            fillKrakenPositions();
            drawKrakenPosition();
        }
        int index = (int) (krakenPositions.size() * Math.random());
        BitBoard res = krakenPositions.get(index);
        krakenPositions.remove(index);
        return res;
    }

    public static void main(String[] args) {
        BitBoardConstants.setSquareAttackDirectionSquareDestinationAttackLine("data.txt");
        int games = Integer.parseInt(args[0]);
        int processors = Integer.parseInt(args[1]);
        String jarFile = args[2];
        String p1Name = args[3];
        String jarFile2 = args[4];
        String p2Name = args[5];
        millisTime = Integer.parseInt(args[6]);
        l = new Log("spielleiter-error.log");
        ergebnisLog = new Log("spielleiterergebnisse.log");
        System.out.println("Started Tests!");
        System.out.println("Games: " + games);
        System.out.println("Processors: " + processors);
        int gamesPerProcessors = games / processors;
        System.out.println("Games per Processor: " + gamesPerProcessors);
        System.out.println("Time per Move (in millis): " + millisTime);

        File file = new File(GlobalFlags.TEXEL_PATH);
        FileWriter fr = null;
        try {
            fr = new FileWriter(file, true);
            TexelTuning.br = new BufferedWriter(fr);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < processors; i++) {
            threads.add(new TestGames(i + "", jarFile, jarFile2, gamesPerProcessors, p1Name, p2Name));
        }
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                printErgebnisse(p1Name, p2Name, true);
                l.flush();
            }
        }, 30000, 30000);
        try {
            for (int i = 0; i < processors; i++) {
                threads.get(i).join();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        timer.cancel();
        printErgebnisse(p1Name, p2Name, false);
        l.onClose();
        try {
            TexelTuning.br.close();
            fr.close();
            Thread.sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public static void printErgebnisse(String p1Name, String p2Name, boolean zwischenergebniss) {
        //Collect from threads
        PlayerStatistics p1 = new PlayerStatistics();
        PlayerStatistics p2 = new PlayerStatistics();
        for (int i = 0; i < TestInstance.threads.size(); i++) {
            TestGames g = TestInstance.threads.get(i);
            p1.combine(g.p1Stats);
            p2.combine(g.p2Stats);
        }
        System.out.println("-----------------------------------");
        System.out.println(zwischenergebniss ? "Zwischenergebnis: " : "Endergebnis: ");
        System.out.println("Name \t\t Wins \t Draws \t Loss  Crashes  Think Time     Elo Gain");
        double N = (p1.wins + p2.wins + p1.draws + 0.0);
        double score = p1.wins + p1.draws / 2.0;
        double winrate = score / N;
        double margin = 1.64 * Math.sqrt(N * winrate * (1 - winrate));

        double eloGainP1 = -400.0 * Math.log10(N / score - 1.0);
        eloGainP1 = Math.round(eloGainP1 * 100.0) / 100.0;
        double errorMargin = -400.0 * Math.log10(N / (N * winrate + margin) - 1) - eloGainP1;
        errorMargin = Math.round(errorMargin * 100.0) / 100.0;
        double p1WinsAreOfRed = 0;
        double p2WinsAreOfRed = 0;
        if (p1.wins != 0) {
            p1WinsAreOfRed = (p1.winsOnRed + 0.0) / p1.wins;
        }
        if (p2.wins != 0) {
            p2WinsAreOfRed = (p2.winsOnRed + 0.0) / p2.wins;
        }
        p1WinsAreOfRed = Math.round(p1WinsAreOfRed * 100) / 100.0;
        p2WinsAreOfRed = Math.round(p2WinsAreOfRed * 100) / 100.0;
        System.out.println(p1Name + "\t " + p1.wins + "\t " + p1.draws + " \t" + p2.wins + " \t" + p1.crashes + "\t " + Math.round((p1.timeUsed / (p1.moves + 0.0)) * 100.0) / 100.0 + "\t" + eloGainP1 + "\u00B1 " + errorMargin);
        System.out.println("WinPcOnRed");
        System.out.println("" + p1WinsAreOfRed);
        System.out.println(p2Name + "\t " + p2.wins + "\t " + p1.draws + " \t" + p1.wins + " \t" + p2.crashes + "\t " + Math.round((p2.timeUsed / (p2.moves + 0.0) * 100.0)) / 100.0 + "\t" + (-1.0 * eloGainP1) + "\u00B1 " + errorMargin);
        System.out.println("WinPcOnRed");
        System.out.println("" + p2WinsAreOfRed);

        TestInstance.ergebnisLog.log(LogLevel.INFO, "Player 1:\n" + p1.toString() + "\n");
        TestInstance.ergebnisLog.log(LogLevel.INFO, "Player 2:\n" + p2.toString() + "\n\n\n");
        TestInstance.ergebnisLog.flush();
    }

    public static GameMove parseGameMove(BufferedReader input, TestGames thread) {
        long curr = System.currentTimeMillis();
        DateFormat sdf = new SimpleDateFormat("dd-MM-yy HH:mm:ss.SSS");
        String timeStamp1 = sdf.format(new Date());
        try {
            while (System.currentTimeMillis() - curr <= 2 * millisTime) {
                if (input.ready()) {
                    String move = input.readLine();
                    String[] parsedMove = move.split(" ");
                    int from = Integer.parseInt(parsedMove[0]);
                    int to = Integer.parseInt(parsedMove[1]);
                    if (parsedMove.length == 3) {
                        boolean isMate = Boolean.parseBoolean(parsedMove[2]);
                        thread.mateFound = isMate;
                    }
                    return new GameMove(from, to, GameDirection.UP);
                }
                Thread.sleep(5);
            }
            String timeStamp2 = sdf.format(new Date());
            l.log(LogLevel.ERROR, timeStamp1);
            l.log(LogLevel.ERROR, timeStamp2);
            System.out.println(timeStamp1);
            System.out.println(timeStamp2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static MyGameState checkGameMove(GameMove move, MyGameState mg) {
        for (int i = 0; i < mg.gmro.instances; i++) {
            if (mg.gmro.moves[i].from == move.from && mg.gmro.moves[i].to == move.to) {
                return mg.gmro.states[i];
            }
        }
        return null;
    }
}

class TestGames extends Thread {
    private String name;
    private String p1;
    private String p2;
    private String p1Name;
    private String p2Name;
    private int games;
    PlayerStatistics p1Stats;
    PlayerStatistics p2Stats;
    boolean mateFound;

    public TestGames(String name, String p1, String p2, int games, String p1Name, String p2Name) {
        this.name = name;
        this.p1 = p1;
        this.p2 = p2;
        this.p1Name = p1Name;
        this.p2Name = p2Name;
        this.games = games;
        this.p1Stats = new PlayerStatistics();
        this.p2Stats = new PlayerStatistics();
        mateFound = false;
        start();
    }

    public void cleanUp(Process p1, Process p2, BufferedWriter p1Writer, BufferedReader p1Reader, BufferedWriter p2Writer, BufferedReader p2Reader) {
        try {
            p1Writer.write("end\n");
            p1Writer.flush();
            p2Writer.write("end\n");
            p2Writer.flush();
            Thread.sleep(5);
            p1Writer.close();
            p1Reader.close();
            p2Writer.close();
            p2Reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Program has 5 seconds time for managing logs
        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                p1.destroy();
                p2.destroy();
            }
        }, 5000);
    }

    public void waitReady(BufferedReader input1, BufferedReader input2) {
        //Terminate after 10 seconds
        long curr = System.currentTimeMillis();
        boolean ready1 = false;
        boolean ready2 = false;
        try {
            while (System.currentTimeMillis() - curr <= 10000 && (!ready1 || !ready2)) {
                if (!ready1 && input1.ready()) {
                    String move = input1.readLine();
                    if (move.equalsIgnoreCase("ready")) {
                        ready1 = true;
                    }
                }
                if (!ready2 && input2.ready()) {
                    String move = input2.readLine();
                    if (move.equalsIgnoreCase("ready")) {
                        ready2 = true;
                    }
                }
                Thread.sleep(5);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            MyGameState startState = null;
            A:
            for (int i = 0; i < games; i++) {
                StringBuilder sb = new StringBuilder();
                mateFound = false;
                if (this.name.equalsIgnoreCase("0") && i % 5 == 0) {
                    System.out.println("" + i + "/" + games);
                }
                Process p1 = Runtime.getRuntime().exec("java -Dfile.encoding=UTF-8 -XX:MaxGCPauseMillis=100 -Xmx800m -Xms800m -Xmn700m -XX:+UseConcMarkSweepGC -XX:-UseParNewGC -XX:+ExplicitGCInvokesConcurrent -jar " + this.p1 + " ./logs/" + this.p1Name + "p" + this.name + "g" + i);
                OutputStream os = p1.getOutputStream();
                BufferedWriter p1Writer = new BufferedWriter(new OutputStreamWriter(os));
                BufferedReader p1Input = new BufferedReader(new InputStreamReader(p1.getInputStream()));

                Process p2 = Runtime.getRuntime().exec("java -Dfile.encoding=UTF-8 -XX:MaxGCPauseMillis=100 -Xmx800m -Xms800m -Xmn700m -XX:+UseConcMarkSweepGC -XX:-UseParNewGC -XX:+ExplicitGCInvokesConcurrent -jar " + this.p2 + " ./logs/" + this.p2Name + "p" + this.name + "g" + i);
                OutputStream p2os = p2.getOutputStream();
                BufferedWriter p2Writer = new BufferedWriter(new OutputStreamWriter(p2os));
                BufferedReader p2Input = new BufferedReader(new InputStreamReader(p2.getInputStream()));

                //Wait for processes to be ready!
                waitReady(p1Input, p2Input);
                MyGameState mg;
                if (i % 2 == 0) {
                    mg = new MyGameState(TestInstance.drawKrakenPosition());
                    startState = mg.clone();
                } else {
                    mg = startState;
                }

                p1Writer.write("newgame " + mg.kraken.l0 + " " + mg.kraken.l1 + "\n");
                p1Writer.flush();
                p2Writer.write("newgame " + mg.kraken.l0 + " " + mg.kraken.l1 + "\n");
                p2Writer.flush();
                //Thread.sleep(500);
                boolean player1IsRed = i % 2 == 0;
                mg.analyze();
                while (mg.gs == GameStatus.INGAME) {
                    //Request next move
                    if (player1IsRed && mg.move == GameColor.RED || !player1IsRed && mg.move == GameColor.BLUE) {
                        p1Writer.write("requestmove " + (TestInstance.millisTime - 105) + "\n");
                        p1Writer.flush();
                        //Expect answer
                        long currentTime = System.currentTimeMillis();
                        GameMove move = TestInstance.parseGameMove(p1Input, this);
                        long afterTime = System.currentTimeMillis();
                        if (move == null) {
                            System.out.println("Timeout " + p1Name + "in p" + this.name + "g" + i);
                            TestInstance.l.log(LogLevel.ERROR, "Timeout " + p1Name + "in p" + this.name + "g" + i);
                            this.p1Stats.crashes++;
                            cleanUp(p1, p2, p1Writer, p1Input, p2Writer, p2Input);
                            continue A;
                        }
                        //Legality check
                        mg = TestInstance.checkGameMove(move, mg);
                        if (mg == null) {
                            System.out.println("Illegal move " + p1Name + "in p" + this.name + "g" + i);
                            TestInstance.l.log(LogLevel.ERROR, "Illegal move " + p1Name + "in p" + this.name + "g" + i);
                            this.p1Stats.crashes++;
                            cleanUp(p1, p2, p1Writer, p1Input, p2Writer, p2Input);
                            continue A;
                        }
                        this.p1Stats.moves++;
                        this.p1Stats.timeUsed += afterTime - currentTime;
                        p1Writer.write("makemove " + move.from + " " + move.to + "\n");
                        p1Writer.flush();
                        p2Writer.write("makemove " + move.from + " " + move.to + "\n");
                        p2Writer.flush();
                    } else {
                        p2Writer.write("requestmove " + (TestInstance.millisTime - 105) + "\n");
                        p2Writer.flush();
                        //Expect answer
                        long currentTime = System.currentTimeMillis();
                        GameMove move = TestInstance.parseGameMove(p2Input, this);
                        long afterTime = System.currentTimeMillis();
                        if (move == null) {
                            System.out.println("Timeout " + p2Name + "in p" + this.name + "g" + i);
                            TestInstance.l.log(LogLevel.ERROR, "Timeout " + p2Name + "in p" + this.name + "g" + i);
                            this.p2Stats.crashes++;
                            cleanUp(p1, p2, p1Writer, p1Input, p2Writer, p2Input);
                            continue A;
                        }
                        //Legality check
                        mg = TestInstance.checkGameMove(move, mg);
                        if (mg == null) {
                            System.out.println("Illegal move " + p2Name + "in p" + this.name + "g" + i);
                            TestInstance.l.log(LogLevel.ERROR, "Illegal move " + p2Name + "in p" + this.name + "g" + i);
                            this.p2Stats.crashes++;
                            cleanUp(p1, p2, p1Writer, p1Input, p2Writer, p2Input);
                            continue A;
                        }
                        this.p2Stats.moves++;
                        this.p2Stats.timeUsed += afterTime - currentTime;
                        p1Writer.write("makemove " + move.from + " " + move.to + "\n");
                        p1Writer.flush();
                        p2Writer.write("makemove " + move.from + " " + move.to + "\n");
                        p2Writer.flush();
                    }
                    if (mg.pliesPlayed > 4 && !mateFound) {
                        sb.append(FEN.toFEN(mg) + "\n");
                    }
                    mg.analyze();
                }
                int roteFische = mg.roteFische.popCount();
                int roterSchwarm = BoardRating.getBiggestSchwarm(mg, GameColor.RED);
                int blaueFische = mg.blaueFische.popCount();
                int blauerSchwarm = BoardRating.getBiggestSchwarm(mg, GameColor.BLUE);
                if (mg.gs == GameStatus.DRAW) {
                    sb.append("Draw\n");
                    this.p1Stats.draws += 1;
                    this.p2Stats.draws += 1;
                    if (player1IsRed) {
                        this.p1Stats.drawsOnRed++;
                        this.p2Stats.drawsOnBlue++;
                    } else {
                        this.p1Stats.drawsOnBlue++;
                        this.p2Stats.drawsOnRed++;
                    }
                } else if (mg.gs == GameStatus.RED_WIN) {
                    sb.append("Red\n");
                    if (player1IsRed) {
                        this.p1Stats.averageSchwarmSizeWhenWon += roterSchwarm;
                        this.p1Stats.averageFischeWhenWon += roteFische;
                        this.p1Stats.schwarmSizeOccurencesWhenWon[roterSchwarm] += 1;
                        this.p2Stats.averageSchwarmSizeWhenLost += blauerSchwarm;
                        this.p2Stats.averageFischeWhenLost += blaueFische;
                        this.p2Stats.schwarmSizeOccurencesWhenLost[blauerSchwarm] += 1;

                        if (blauerSchwarm > roterSchwarm) {
                            this.p1Stats.wonWhenSchwarmWasSmaller++;
                            this.p2Stats.lostWhenSchwarmWasBigger++;
                        }
                        this.p1Stats.winsOnRed++;
                        this.p1Stats.wins++;
                        this.p2Stats.losses++;
                        this.p2Stats.lossesOnBlue++;
                    } else {
                        this.p2Stats.averageSchwarmSizeWhenWon += roterSchwarm;
                        this.p2Stats.averageFischeWhenWon += roteFische;
                        this.p2Stats.schwarmSizeOccurencesWhenWon[roterSchwarm] += 1;
                        this.p1Stats.averageSchwarmSizeWhenLost += blauerSchwarm;
                        this.p1Stats.averageFischeWhenLost += blaueFische;
                        this.p1Stats.schwarmSizeOccurencesWhenLost[blauerSchwarm] += 1;

                        if (blauerSchwarm > roterSchwarm) {
                            this.p2Stats.wonWhenSchwarmWasSmaller++;
                            this.p1Stats.lostWhenSchwarmWasBigger++;
                        }

                        this.p2Stats.winsOnRed++;
                        this.p2Stats.wins++;
                        this.p1Stats.lossesOnBlue++;
                        this.p1Stats.losses++;
                    }
                } else if (mg.gs == GameStatus.BLUE_WIN) {
                    sb.append("Blue\n");
                    if (player1IsRed) {
                        this.p1Stats.averageSchwarmSizeWhenLost += roterSchwarm;
                        this.p1Stats.averageFischeWhenLost += roteFische;
                        this.p1Stats.schwarmSizeOccurencesWhenLost[roterSchwarm] += 1;

                        this.p2Stats.averageSchwarmSizeWhenWon += blauerSchwarm;
                        this.p2Stats.averageFischeWhenWon += blaueFische;
                        this.p2Stats.schwarmSizeOccurencesWhenWon[blauerSchwarm] += 1;

                        if (roterSchwarm > blauerSchwarm) {
                            this.p2Stats.wonWhenSchwarmWasSmaller++;
                            this.p1Stats.lostWhenSchwarmWasBigger++;
                        }
                        this.p2Stats.wins++;
                        this.p2Stats.winsOnBlue++;
                        this.p1Stats.losses++;
                        this.p1Stats.lossesOnRed++;
                    } else {
                        this.p2Stats.averageSchwarmSizeWhenLost += roterSchwarm;
                        this.p2Stats.averageFischeWhenLost += roteFische;
                        this.p2Stats.schwarmSizeOccurencesWhenLost[roterSchwarm] += 1;

                        this.p1Stats.averageSchwarmSizeWhenWon += blauerSchwarm;
                        this.p1Stats.averageFischeWhenWon += blaueFische;
                        this.p1Stats.schwarmSizeOccurencesWhenWon[blauerSchwarm] += 1;

                        if (roterSchwarm > blauerSchwarm) {
                            this.p1Stats.wonWhenSchwarmWasSmaller++;
                            this.p2Stats.lostWhenSchwarmWasBigger++;
                        }

                        this.p1Stats.wins++;
                        this.p1Stats.winsOnBlue++;
                        this.p2Stats.losses++;
                        this.p2Stats.lossesOnRed++;
                    }
                }
                TexelTuning.writeGame(sb.toString());
                cleanUp(p1, p2, p1Writer, p1Input, p2Writer, p2Input);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

class PlayerStatistics {
    int wins;
    int winsOnRed;
    int winsOnBlue;
    int averageFischeWhenWon;
    int averageSchwarmSizeWhenWon;
    int[] schwarmSizeOccurencesWhenWon = new int[17];
    int draws;
    int drawsOnRed;
    int drawsOnBlue;
    int losses;
    int lossesOnRed;
    int lossesOnBlue;
    int averageFischeWhenLost;
    int averageSchwarmSizeWhenLost;
    int[] schwarmSizeOccurencesWhenLost = new int[17];

    int lostWhenSchwarmWasBigger;
    int wonWhenSchwarmWasSmaller;
    int moves;
    int timeUsed;
    int crashes;

    public PlayerStatistics() {
        this.wins = 0;
        this.winsOnRed = 0;
        this.winsOnBlue = 0;
        this.averageFischeWhenWon = 0;
        this.averageSchwarmSizeWhenWon = 0;
        this.draws = 0;
        this.drawsOnRed = 0;
        this.drawsOnBlue = 0;
        this.losses = 0;
        this.lossesOnRed = 0;
        this.lossesOnBlue = 0;
        this.averageFischeWhenLost = 0;
        this.averageSchwarmSizeWhenLost = 0;
        this.moves = 0;
        this.timeUsed = 0;
        this.crashes = 0;
        this.wonWhenSchwarmWasSmaller = 0;
        this.lostWhenSchwarmWasBigger = 0;
    }

    public void combine(PlayerStatistics other) {
        this.wins += other.wins;
        this.winsOnRed += other.winsOnRed;
        this.winsOnBlue += other.winsOnBlue;
        this.averageFischeWhenWon += other.averageFischeWhenWon;
        this.averageSchwarmSizeWhenWon += other.averageSchwarmSizeWhenWon;
        for (int i = 0; i < 17; i++) {
            this.schwarmSizeOccurencesWhenWon[i] += other.schwarmSizeOccurencesWhenWon[i];
        }
        this.draws += other.draws;
        this.drawsOnRed += other.drawsOnRed;
        this.drawsOnBlue += other.drawsOnBlue;
        this.losses += other.losses;
        this.lossesOnRed += other.lossesOnRed;
        this.lossesOnBlue += other.lossesOnBlue;
        this.averageFischeWhenLost += other.averageFischeWhenLost;
        this.averageSchwarmSizeWhenLost += other.averageSchwarmSizeWhenLost;
        for (int i = 0; i < 17; i++) {
            this.schwarmSizeOccurencesWhenLost[i] += other.schwarmSizeOccurencesWhenLost[i];
        }
        this.moves += other.moves;
        this.timeUsed += other.timeUsed;
        this.crashes += other.crashes;
        this.wonWhenSchwarmWasSmaller += other.wonWhenSchwarmWasSmaller;
        this.lostWhenSchwarmWasBigger += other.lostWhenSchwarmWasBigger;
    }

    @Override
    public String toString() {
        double n = this.wins + this.draws + this.losses + 0.0;
        String res = "";
        res += "Insg Spiele: " + n + "\n";
        res += "-------------------------------------\n";
        res += "Wins(R/B): " + this.wins + "(" + this.winsOnRed + "/" + this.winsOnBlue + ")\n";
        double aFw = 0;
        double aSw = 0;
        if (n != 0) {
            aFw = this.averageFischeWhenWon / (this.wins + 0.0);
            aSw = this.averageSchwarmSizeWhenWon / (this.wins + 0.0);
        }
        res += "Avg. Fische @Win: " + aFw + "\n";
        res += "Avg. Schwarm @Win: " + aSw + "\n";
        res += "Wins wenn Schwarm kleiner war: " + this.wonWhenSchwarmWasSmaller + "\n";
        res += "-------------------------------------\n";
        res += "Draws(R/B): " + this.draws + "(" + this.drawsOnRed + "/" + this.drawsOnBlue + ")\n";
        res += "-------------------------------------\n";
        res += "Losses(R/B): " + this.losses + "(" + this.lossesOnRed + "/" + this.lossesOnBlue + ")\n";
        double aFl = 0;
        double aSl = 0;
        if (n != 0) {
            aFl = this.averageFischeWhenLost / (this.losses + 0.0);
            aSl = this.averageSchwarmSizeWhenLost / (this.losses + 0.0);
        }
        res += "Avg. Fische @Loss: " + aFl + "\n";
        res += "Avg. Schwarm @Loss: " + aSl + "\n";
        res += "Losses wenn Schwarm groesser war: " + this.lostWhenSchwarmWasBigger + "\n";
        res += "-------------------------------------\n";


        return res;
    }
}
