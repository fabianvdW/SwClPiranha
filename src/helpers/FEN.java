package helpers;

import datastructures.BitBoard;
import game.GameColor;
import game.MyGameState;


public class FEN {

    public static MyGameState readFEN(String fen) {
        String[] arr = fen.split(" ");
        BitBoard roteFische = new BitBoard(Long.parseLong(arr[0]), Long.parseLong(arr[1]));
        BitBoard blaueFische = new BitBoard(Long.parseLong(arr[2]), Long.parseLong(arr[3]));
        BitBoard kraken = new BitBoard(Long.parseLong(arr[4]), Long.parseLong(arr[5]));
        GameColor move;
        if (arr[6].equalsIgnoreCase("r")) {
            move = GameColor.RED;
        } else {
            move = GameColor.BLUE;
        }
        int pliesPlayed = Integer.parseInt(arr[7]);
        int roundsPlayed = Integer.parseInt(arr[8]);
        MyGameState mg = new MyGameState(roteFische, blaueFische, kraken, move, pliesPlayed, roundsPlayed);
        return mg;
    }

    public static String toFEN(MyGameState mg) {
        String res = "";
        res += mg.roteFische.l0 + " " + mg.roteFische.l1 + " ";
        res += mg.blaueFische.l0 + " " + mg.blaueFische.l1 + " ";
        res += mg.kraken.l0 + " " + mg.kraken.l1 + " ";
        res += mg.move == GameColor.RED ? "r " : "b ";
        res += mg.pliesPlayed + " ";
        res += mg.roundsPlayed;
        return res;
    }
}
