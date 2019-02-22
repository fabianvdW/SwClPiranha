package testing;

import game.*;

import javax.print.DocFlavor;
import java.io.*;
import java.util.ArrayList;

public class TestInstance {
    public static ArrayList<TestGames> threads = new ArrayList<>();
    public static int millisTime;

    public static void main(String[] args) {
        BitBoardConstants.setSquareAttackDirectionSquareDestinationAttackLine("data.txt");
        int games = Integer.parseInt(args[0]);
        int processors = Integer.parseInt(args[1]);
        String jarFile = args[2];
        String p1Name = args[3];
        String jarFile2 = args[4];
        String p2Name = args[5];
        millisTime = Integer.parseInt(args[6]);
        System.out.println("Started Tests!");
        System.out.println("Games: " + games);
        System.out.println("Processors: " + processors);
        int gamesPerProcessors = games / processors;
        System.out.println("Games per Processor: " + gamesPerProcessors);
        System.out.println("Time per Move (in millis): " + millisTime);
        for (int i = 0; i < processors; i++) {
            threads.add(new TestGames(i + "", jarFile, jarFile2, gamesPerProcessors, p1Name, p2Name));
        }
        try {
            for (int i = 0; i < processors; i++) {
                threads.get(i).join();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Final statistitcs
        int p1Wins = 0;
        int p1Crashes = 0;
        int p2Wins = 0;
        int p2Crashes = 0;
        int draws = 0;
        double timeP1 = 0.0;
        int movesP1 = 0;
        double timeP2 = 0.0;
        int movesP2 = 0;
        for (int i = 0; i < processors; i++) {
            p1Wins += threads.get(i).p1Wins;
            p2Wins += threads.get(i).p2Wins;
            p1Crashes += threads.get(i).p1Crashes;
            p2Crashes += threads.get(i).p2Crashes;
            draws += threads.get(i).draws;
            timeP1 += threads.get(i).timeUsedP1;
            movesP1 += threads.get(i).movesP1;
            timeP2 += threads.get(i).timeUsedP2;
            movesP2 += threads.get(i).movesP2;
        }
        System.out.println("-----------------------------------");
        System.out.println("Name \t\t Wins \t Draws \t Loss    Crashes    Average Think Time(in millis)");
        System.out.println(p1Name + "\t " + p1Wins + "\t " + draws + " \t" + p2Wins + " \t" + p1Crashes + "\t " + timeP1 / (movesP1 + 0.0));
        System.out.println(p2Name + "\t " + p2Wins + "\t " + draws + " \t" + p1Wins + " \t" + p2Crashes + "\t " + timeP2 / (movesP2 + 0.0));
    }

    public static void printStream(InputStream in) {
        String line;
        BufferedReader input = new BufferedReader(new InputStreamReader(in));
        try {
            while ((line = input.readLine()) != null) {
                System.out.println(line);
            }
            System.out.println("done");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static GameMove parseGameMove(BufferedReader input) {
        long curr = System.currentTimeMillis();
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
    int p1Wins;
    int p2Wins;
    int p1Crashes;
    int p2Crashes;
    int draws;
    long timeUsedP1;
    long timeUsedP2;
    int movesP1;
    int movesP2;
    private String p1;
    private String p2;
    private String p1Name;
    private String p2Name;
    private int games;

    public TestGames(String name, String p1, String p2, int games, String p1Name, String p2Name) {
        this.name = name;
        this.p1Wins = 0;
        this.p1Crashes = 0;
        this.p2Crashes = 0;
        this.p2Wins = 0;
        this.draws = 0;
        this.p1 = p1;
        this.p2 = p2;
        this.p1Name = p1Name;
        this.p2Name = p2Name;
        timeUsedP1 = 0;
        timeUsedP2 = 0;
        movesP1 = 0;
        movesP2 = 0;
        this.games = games;
        start();
    }

    public void cleanUp(Process p1, Process p2, BufferedWriter p1Writer, BufferedReader p1Reader, BufferedWriter p2Writer, BufferedReader p2Reader) {
        p1.destroy();
        p2.destroy();
        try {
            p1Writer.close();
            p1Reader.close();
            p2Writer.close();
            p2Reader.close();
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
                Process p1 = Runtime.getRuntime().exec("java -jar " + this.p1 + " " + this.p1Name);
                OutputStream os = p1.getOutputStream();
                BufferedWriter p1Writer = new BufferedWriter(new OutputStreamWriter(os));
                BufferedReader p1Input = new BufferedReader(new InputStreamReader(p1.getInputStream()));

                Process p2 = Runtime.getRuntime().exec("java -jar " + this.p2 + " " + this.p2Name);
                OutputStream p2os = p2.getOutputStream();
                BufferedWriter p2Writer = new BufferedWriter(new OutputStreamWriter(p2os));
                BufferedReader p2Input = new BufferedReader(new InputStreamReader(p2.getInputStream()));

                MyGameState mg = new MyGameState();
                p1Writer.write("newgame " + mg.kraken.l0 + " " + mg.kraken.l1 + "\n");
                p1Writer.flush();
                p2Writer.write("newgame " + mg.kraken.l0 + " " + mg.kraken.l1 + "\n");
                p2Writer.flush();
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
                            System.out.println("Timeout " + p1Name);
                            p1Crashes++;
                            cleanUp(p1, p2, p1Writer, p1Input, p2Writer, p2Input);
                            continue A;
                        }
                        //Legality check
                        mg = TestInstance.checkGameMove(move, mg);
                        if (mg == null) {
                            System.out.println("Illegal move " + p1Name);
                            p1Crashes++;
                            cleanUp(p1, p2, p1Writer, p1Input, p2Writer, p2Input);
                            continue A;
                        }
                        this.movesP1++;
                        this.timeUsedP1 += afterTime - currentTime;
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
                            System.out.println("Timeout " + p2Name);
                            p2Crashes++;
                            cleanUp(p1, p2, p1Writer, p1Input, p2Writer, p2Input);
                            continue A;
                        }
                        //Legality check
                        mg = TestInstance.checkGameMove(move, mg);
                        if (mg == null) {
                            System.out.println("Illegal move " + p2Name);
                            p2Crashes++;
                            cleanUp(p1, p2, p1Writer, p1Input, p2Writer, p2Input);
                            continue A;
                        }
                        this.movesP2++;
                        this.timeUsedP2 += afterTime - currentTime;
                        p1Writer.write("makemove " + move.from + " " + move.to + "\n");
                        p1Writer.flush();
                        p2Writer.write("makemove " + move.from + " " + move.to + "\n");
                        p2Writer.flush();
                    }
                    mg.analyze();
                }
                if (mg.gs == GameStatus.DRAW) {
                    draws += 1;
                } else if (mg.gs == GameStatus.RED_WIN) {
                    if (player1IsRed) {
                        p1Wins++;
                    } else {
                        p2Wins++;
                    }
                } else if (mg.gs == GameStatus.BLUE_WIN) {
                    if (player1IsRed) {
                        p2Wins++;
                    } else {
                        p1Wins++;
                    }
                }
                cleanUp(p1, p2, p1Writer, p1Input, p2Writer, p2Input);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
