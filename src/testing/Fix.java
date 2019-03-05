package testing;


import artificialplayer.AlphaBeta;
import artificialplayer.PrincipalVariation;
import artificialplayer.Search;
import game.BitBoardConstants;
import game.GameMove;
import game.MyGameState;
import helpers.FEN;

public class Fix {
    public static String[] fens = {
            "33652832 1731072757650162688 34225520640 510 1 8388608 r 0 0",
            "33652832 1875187395970204672 34225520640 268435710 1 8388608 r 2 1",
            "33628256 1875187395970204672 34225520640 140737756790910 1 8388608 r 4 2",
            "33628256 6486872314885964800 29930569728 140737756790910 1 8388608 r 6 3",
            "33628256 6486450102420898816 29930569728 268501086 1 8388608 r 8 4"
    };
    public static int[] depths = {
            4, 4,
            5, 5,5};

    public static void main(String[] args) {
        BitBoardConstants.setSquareAttackDirectionSquareDestinationAttackLine("SwClPiranha/src/game/data.txt");
        for (int i = 0; i < fens.length; i++) {
            MyGameState mg = FEN.readFEN(fens[i]);
            System.out.println(mg);
            Search s = new Search(mg, depths[i]);
            s.run();
            Search.birthTime += 2;
            PrincipalVariation pv = s.currentBestPv;
            GameMove m = pv.stack.get(0);
            System.out.println("Move found in Direction: " + m.dir);
            System.out.println("From: (" + (9 - m.from % 10) + "," + (m.from / 10) + ")");
            System.out.println("PV Score: " + pv.score);
        }
    }
}
