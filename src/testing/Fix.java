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
            "4609 4648348134143950848 1073758208 422349904019456 256 9007199254740992 b 51 25",
            "4609 4647785459068436480 1073741824 288652726055731200 256 9007199254740992 b 53 26"
    };
    public static int[] depths = {
            6, 7};

    public static void main(String[] args) {
        BitBoardConstants.setSquareAttackDirectionSquareDestinationAttackLine("SwClPiranha/src/game/data.txt");
        /*MyGameState g = FEN.readFEN("4609 4647785459068436480 1073741824 288652726055731200 256 9007199254740992 b 53 26");
        PrincipalVariation p = AlphaBeta.alphaBetaRoot(g, 7, -1, -100000, 100000);
        System.out.println(p.score);
        printPV(p);
        System.exit(-1);*/
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
            printPV(pv);
            System.out.println("PV Score: " + pv.score);
        }
    }

    public static void printPV(PrincipalVariation pv) {
        for (int j = 0; j < pv.stack.size(); j++) {
            GameMove mv = pv.stack.get(j);
            System.out.println(mv.from + " " + mv.to);
        }
    }

    public static String makeMove(String fen, int from, int to) {
        String res = "";
        MyGameState mg = FEN.readFEN(fen);
        mg.analyze();
        for (int i = 0; i < mg.gmro.instances; i++) {
            if (mg.gmro.moves[i].from == from && mg.gmro.moves[i].to == to) {
                return FEN.toFEN(mg.gmro.states[i]);
            }
        }
        return null;
    }
}
