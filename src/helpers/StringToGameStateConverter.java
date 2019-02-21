package helpers;

import datastructures.BitBoard;
import game.GameColor;
import game.MyGameState;

public class StringToGameStateConverter {

    /*
    public final static String[][] STANDARD_GAME_STATE = {
            {" ", "b", "b", "b", "b", "b", "b", "b", "b", " "},
            {"r", " ", " ", " ", " ", " ", " ", " ", " ", "r"},
            {"r", " ", " ", " ", " ", " ", " ", " ", " ", "r"},
            {"r", " ", " ", " ", " ", "k", " ", " ", " ", "r"},
            {"r", " ", " ", " ", " ", " ", " ", " ", " ", "r"},
            {"r", " ", " ", " ", " ", " ", " ", " ", " ", "r"},
            {"r", " ", " ", " ", "k", " ", " ", " ", " ", "r"},
            {"r", " ", " ", " ", " ", " ", " ", " ", " ", "r"},
            {"r", " ", " ", " ", " ", " ", " ", " ", " ", "r"},
            {" ", "b", "b", "b", "b", "b", "b", "b", "b", " "}
    };
    */
    //Blau am Zug
    public final static String[][] STANDARD_GAME_STATE = {
            {" ", " ", " ", " ", " ", "b", " ", " ", " ", " "},
            {" ", " ", " ", " ", "r", " ", "b", " ", " ", " "},
            {" ", " ", " ", " ", " ", "b", " ", "r", " ", " "},
            {"r", " ", " ", "r", "b", "b", " ", " ", " ", "r"},
            {" ", " ", "k", " ", " ", " ", "b", "r", " ", " "},
            {" ", " ", " ", "r", " ", "r", "b", " ", " ", " "},
            {" ", " ", "r", " ", " ", " ", "b", " ", "r", " "},
            {" ", " ", " ", "b", "k", "r", "b", " ", " ", " "},
            {" ", " ", " ", " ", "b", " ", " ", "r", " ", " "},
            {" ", " ", " ", "b", "b", "b", "b", " ", " ", " "}
    };

    public static MyGameState readGameState(String[][] arr) {
        BitBoard roteFische = new BitBoard(0, 0);
        BitBoard blaueFische = new BitBoard(0, 0);
        BitBoard kraken = new BitBoard(0, 0);
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                String s = arr[y][x];
                int shift = 99 - (y * 10 + x);
                if (s.equalsIgnoreCase("b")) {
                    blaueFische.orEquals(new BitBoard(0, 1).leftShift(shift));
                } else if (s.equalsIgnoreCase("r")) {
                    roteFische.orEquals(new BitBoard(0, 1).leftShift(shift));
                } else if (s.equalsIgnoreCase("k")) {
                    kraken.orEquals(new BitBoard(0, 1).leftShift(shift));
                }
            }

        }
        return new MyGameState(roteFische, blaueFische, kraken, GameColor.RED,0,0);
    }
}
