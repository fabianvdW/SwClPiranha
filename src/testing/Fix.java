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
            "10 -4575657221408423936 132 9077602408988672 0 72057594042122240 b 53 26",
            "4098 -4575657221408423936 131076 9077602408988672 0 72057594042122240 b 55 27"
    };
    public static int[] depths = {
            7, 5};

    public static void main(String[] args) {
        BitBoardConstants.setSquareAttackDirectionSquareDestinationAttackLine("SwClPiranha/src/game/data.txt");
       // MyGameState g = FEN.readFEN("4098 -4575657221408423936 131076 9077602408988672 0 72057594042122240 b 55 27");
       // System.out.println(AlphaBeta.alphaBeta(g, 5, -1, -1000, 1000));
        //System.exit(-1);
        for (int i = 0; i < fens.length; i++) {
            MyGameState mg = FEN.readFEN(fens[i]);
            System.out.println(mg);
            Search s = new Search(mg, depths[i]);
            s.run();
            Search.birthTime += 2;
            PrincipalVariation pv = s.currentBestPv;
            GameMove m = pv.stack.get(0);
            System.out.println("Move found in Direction: " + m.dir);
            System.out.println("From: (" + m.from + ")" + " TO: " + m.to);
            System.out.println("PV:");
            for (int j = 1; j < pv.stack.size(); j++) {
                GameMove mv = pv.stack.get(j);
                System.out.println(mv.from + " " + mv.to);
            }
            System.out.println("PV Score: " + pv.score);
        }
    }
}
