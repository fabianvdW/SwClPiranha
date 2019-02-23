package testing;

import game.*;
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

    static int p1Wins;
    static int p2Wins;
    static int p1Crashes;
    static int p2Crashes;
    static int draws;
    static long timeUsedP1;
    static long timeUsedP2;
    static int movesP1;
    static int movesP2;
    static Log l;

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
        System.out.println("Started Tests!");
        System.out.println("Games: " + games);
        System.out.println("Processors: " + processors);
        int gamesPerProcessors = games / processors;
        System.out.println("Games per Processor: " + gamesPerProcessors);
        System.out.println("Time per Move (in millis): " + millisTime);
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
        }, 10000, 10000);
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
            Thread.sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public static void printErgebnisse(String p1Name, String p2Name, boolean zwischenergebniss) {
        System.out.println("-----------------------------------");
        System.out.println(zwischenergebniss ? "Zwischenergebnis: " : "Endergebnis: ");
        System.out.println("Name \t\t Wins \t Draws \t Loss  Crashes   Think Time  Elo Gain");
        double N = (p1Wins + p2Wins + draws + 0.0);
        double score = p1Wins + draws / 2.0;
        double winrate = score / N;
        double margin = 1.64 * Math.sqrt(N * winrate * (1 - winrate));

        double eloGainP1 = -400.0 * Math.log10(N / score - 1.0);
        eloGainP1 = Math.round(eloGainP1 * 100.0) / 100.0;
        double errorMargin = -400.0 * Math.log10(N / (N * winrate + margin) - 1) - eloGainP1;
        errorMargin = Math.round(errorMargin * 100.0) / 100.0;
        System.out.println(p1Name + "\t " + p1Wins + "\t " + draws + " \t" + p2Wins + " \t" + p1Crashes + "\t " + Math.round((timeUsedP1 / (movesP1 + 0.0)) * 100.0) / 100.0 + "\t\t " + eloGainP1 + "\u00B1 " + errorMargin);
        System.out.println(p2Name + "\t " + p2Wins + "\t " + draws + " \t" + p1Wins + " \t" + p2Crashes + "\t " + Math.round((timeUsedP2 / (movesP2 + 0.0) * 100.0)) / 100.0 + "\t\t " + (-1.0 * eloGainP1) + "\u00B1 " + errorMargin);
    }

    public static GameMove parseGameMove(BufferedReader input) {
        long curr = System.currentTimeMillis();
        DateFormat sdf = new SimpleDateFormat("dd-MM-yy HH:mm:ss.SSS");
        String timeStamp1 = sdf.format(new Date());
        try {
            while (System.currentTimeMillis() - curr <= millisTime) {
                if (input.ready()) {
                    String move = input.readLine();
                    String[] parsedMove = move.split(" ");
                    int from = Integer.parseInt(parsedMove[0]);
                    int to = Integer.parseInt(parsedMove[1]);
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

    public TestGames(String name, String p1, String p2, int games, String p1Name, String p2Name) {
        this.name = name;
        this.p1 = p1;
        this.p2 = p2;
        this.p1Name = p1Name;
        this.p2Name = p2Name;
        this.games = games;
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
            A:
            for (int i = 0; i < games; i++) {
                if (this.name.equalsIgnoreCase("0") && i % 5 == 0) {
                    System.out.println("" + i + "/" + games);
                }
                Process p1 = Runtime.getRuntime().exec("java -jar " + this.p1 + " ./logs/" + this.p1Name + "p" + this.name + "g" + i);
                OutputStream os = p1.getOutputStream();
                BufferedWriter p1Writer = new BufferedWriter(new OutputStreamWriter(os));
                BufferedReader p1Input = new BufferedReader(new InputStreamReader(p1.getInputStream()));

                Process p2 = Runtime.getRuntime().exec("java -jar " + this.p2 + " ./logs/" + this.p2Name + "p" + this.name + "g" + i);
                OutputStream p2os = p2.getOutputStream();
                BufferedWriter p2Writer = new BufferedWriter(new OutputStreamWriter(p2os));
                BufferedReader p2Input = new BufferedReader(new InputStreamReader(p2.getInputStream()));

                //Wait for processes to be ready!
                waitReady(p1Input, p2Input);
                MyGameState mg = new MyGameState();
                p1Writer.write("newgame " + mg.kraken.l0 + " " + mg.kraken.l1 + "\n");
                p1Writer.flush();
                p2Writer.write("newgame " + mg.kraken.l0 + " " + mg.kraken.l1 + "\n");
                p2Writer.flush();
                //Thread.sleep(500);
                boolean player1IsRed = Math.random() * 2 < 1;
                mg.analyze();
                while (mg.gs == GameStatus.INGAME) {
                    //Request next move
                    if (player1IsRed && mg.move == GameColor.RED || !player1IsRed && mg.move == GameColor.BLUE) {
                        p1Writer.write("requestmove \n");
                        p1Writer.flush();
                        //Expect answer
                        long currentTime = System.currentTimeMillis();
                        GameMove move = TestInstance.parseGameMove(p1Input);
                        long afterTime = System.currentTimeMillis();
                        if (move == null) {
                            System.out.println("Timeout " + p1Name + "in p" + this.name + "g" + i);
                            TestInstance.l.log(LogLevel.ERROR, "Timeout " + p1Name + "in p" + this.name + "g" + i);
                            TestInstance.p1Crashes++;
                            cleanUp(p1, p2, p1Writer, p1Input, p2Writer, p2Input);
                            continue A;
                        }
                        //Legality check
                        mg = TestInstance.checkGameMove(move, mg);
                        if (mg == null) {
                            System.out.println("Illegal move " + p1Name + "in p" + this.name + "g" + i);
                            TestInstance.l.log(LogLevel.ERROR, "Illegal move " + p1Name + "in p" + this.name + "g" + i);
                            TestInstance.p1Crashes++;
                            cleanUp(p1, p2, p1Writer, p1Input, p2Writer, p2Input);
                            continue A;
                        }
                        TestInstance.movesP1++;
                        TestInstance.timeUsedP1 += afterTime - currentTime;
                        p1Writer.write("makemove " + move.from + " " + move.to + "\n");
                        p1Writer.flush();
                        p2Writer.write("makemove " + move.from + " " + move.to + "\n");
                        p2Writer.flush();
                    } else {
                        p2Writer.write("requestmove \n");
                        p2Writer.flush();
                        //Expect answer
                        long currentTime = System.currentTimeMillis();
                        GameMove move = TestInstance.parseGameMove(p2Input);
                        long afterTime = System.currentTimeMillis();
                        if (move == null) {
                            System.out.println("Timeout " + p2Name + "in p" + this.name + "g" + i);
                            TestInstance.l.log(LogLevel.ERROR, "Timeout " + p2Name + "in p" + this.name + "g" + i);
                            TestInstance.p2Crashes++;
                            cleanUp(p1, p2, p1Writer, p1Input, p2Writer, p2Input);
                            continue A;
                        }
                        //Legality check
                        mg = TestInstance.checkGameMove(move, mg);
                        if (mg == null) {
                            System.out.println("Illegal move " + p2Name + "in p" + this.name + "g" + i);
                            TestInstance.l.log(LogLevel.ERROR, "Illegal move " + p2Name + "in p" + this.name + "g" + i);
                            TestInstance.p2Crashes++;
                            cleanUp(p1, p2, p1Writer, p1Input, p2Writer, p2Input);
                            continue A;
                        }
                        TestInstance.movesP2++;
                        TestInstance.timeUsedP2 += afterTime - currentTime;
                        p1Writer.write("makemove " + move.from + " " + move.to + "\n");
                        p1Writer.flush();
                        p2Writer.write("makemove " + move.from + " " + move.to + "\n");
                        p2Writer.flush();
                    }
                    mg.analyze();
                }
                if (mg.gs == GameStatus.DRAW) {
                    TestInstance.draws += 1;
                } else if (mg.gs == GameStatus.RED_WIN) {
                    if (player1IsRed) {
                        TestInstance.p1Wins++;
                    } else {
                        TestInstance.p2Wins++;
                    }
                } else if (mg.gs == GameStatus.BLUE_WIN) {
                    if (player1IsRed) {
                        TestInstance.p2Wins++;
                    } else {
                        TestInstance.p1Wins++;
                    }
                }
                cleanUp(p1, p2, p1Writer, p1Input, p2Writer, p2Input);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
