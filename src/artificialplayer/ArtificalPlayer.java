package artificialplayer;

import datastructures.BitBoard;
import game.BitBoardConstants;
import game.GameMove;
import game.MyGameState;
import helpers.logging.Log;
import helpers.logging.LogLevel;

import java.io.*;
import java.util.Scanner;

public abstract class ArtificalPlayer {
    MyGameState mg;

    public ArtificalPlayer() {
    }

    public abstract GameMove requestMove();

    public void main(String logName) {
        BitBoardConstants.setSquareAttackDirectionSquareDestinationAttackLine("data.txt");
        Scanner s = new Scanner(System.in);
        Log l = new Log(logName + ".txt");
        l.log(LogLevel.INFO, "started program!");
        System.out.println("ready");
        try {
            while (true) {
                String input = s.nextLine().trim();
                l.log(LogLevel.INFO, input);
                String[] arr = input.split(" ");
                if (arr[0].equalsIgnoreCase("newgame")) {
                    long l0 = Long.parseLong(arr[1]);
                    long l1 = Long.parseLong(arr[2]);
                    mg = new MyGameState(new BitBoard(l0, l1));
                } else if (arr[0].equalsIgnoreCase("requestmove")) {
                    GameMove res = this.requestMove();
                    System.out.println(res.from + " " + res.to);
                    l.log(LogLevel.INFO, "sent " + res.from + " " + res.to);
                } else if (arr[0].equalsIgnoreCase("makemove")) {
                    int from = Integer.parseInt(arr[1]);
                    int to = Integer.parseInt(arr[2]);
                    mg.analyze();
                    for (int i = 0; i < mg.gmro.instances; i++) {
                        if (mg.gmro.moves[i].from == from && mg.gmro.moves[i].to == to) {
                            mg = mg.gmro.states[i];
                            break;
                        }
                    }
                } else if (arr[0].equalsIgnoreCase("end")) {
                    l.onClose();
                    break;
                }
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String sStackTrace = sw.toString(); // stack trace as a string
            l.log(LogLevel.ERROR, sStackTrace);
            l.onClose();
        }
    }
}
