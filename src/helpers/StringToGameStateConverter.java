package helpers;

import datastructures.BitBoard;
import game.GameColor;
import game.GameState;
import game.GameStatus;

public class StringToGameStateConverter {

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

    public static GameState readGameState(String[][] arr) {
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
        return new GameState(roteFische, blaueFische, kraken, GameColor.RED,0,0);
    }
}
