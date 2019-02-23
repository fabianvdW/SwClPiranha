package artificialplayer;

import datastructures.BitBoard;
import game.BitBoardConstants;
import game.GameMove;
import game.MyGameState;

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
        File f = new File("log" + logName + ".txt");
        //try {
        //BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        while (true) {
            String input = s.nextLine().trim();
            //bw.write("INFO: " + input + "\n");
            //bw.flush();
            String[] arr = input.split(" ");
            if (arr[0].equalsIgnoreCase("newgame")) {
                long l0 = Long.parseLong(arr[1]);
                long l1 = Long.parseLong(arr[2]);
                mg = new MyGameState(new BitBoard(l0, l1));
            } else if (arr[0].equalsIgnoreCase("requestmove")) {
                GameMove res = this.requestMove();
                //bw.write("SENT: " + res.from + " " + res.to+"\n");
                //bw.flush();
                System.out.println(res.from + " " + res.to);
            } else if (arr[0].equalsIgnoreCase("makemove")) {
                int from = Integer.parseInt(arr[1]);
                int to = Integer.parseInt(arr[2]);
                mg.analyze();
                //bw.write("INFO: " + mg.gmro.instances+"\n");
                //bw.flush();
                for (int i = 0; i < mg.gmro.instances; i++) {
                    //bw.write("INFO: " + i+"\n");
                    //bw.flush();
                    if (mg.gmro.moves[i].from == from && mg.gmro.moves[i].to == to) {
                        mg = mg.gmro.states[i];
                        break;
                    }
                }
            }
            //bw.write("INFO: Finished with Loop!"+"\n");
            //bw.flush();
        }
        //} catch (Exception e) {
        //   e.printStackTrace();
        //}
    }
}
