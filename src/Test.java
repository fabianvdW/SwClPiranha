import artificialplayer.AlphaBeta;
import artificialplayer.BoardRating;
import artificialplayer.PrincipalVariation;
import artificialplayer.Search;
import game.BitBoardConstants;
import game.GameColor;
import game.MyGameState;
import helpers.FEN;
import helpers.Perft;
import sun.misc.Perf;

public class Test {
    public static void main(String[] args) {
        BitBoardConstants.setSquareAttackDirectionSquareDestinationAttackLine("SwClPiranha/src/game/data.txt");
        MyGameState mg = FEN.readFEN("327752 594545521771676928 6979339264 53893551620160 0 140737496743936 b 27 13");
        System.out.println(mg);
        int i =150554053;
                // Perft.perft(mg, 5);
        System.out.println(i);
        long curr = System.currentTimeMillis();
        PrincipalVariation pv = AlphaBeta.alphaBetaRoot(mg, 5, mg.move == GameColor.RED ? 1 : -1);
        System.out.println(pv.stack.get(0));
        System.out.println(pv.score);
        long now = System.currentTimeMillis();
        System.out.println((now - curr));
        System.out.println(AlphaBeta.nodesExamined);
        System.out.println(AlphaBeta.depth0Nodes);
        System.out.println((AlphaBeta.depth0Nodes + 0.0) / i);
        //Search.investigateCache();
        //System.out.println(BoardRating.calculateBiggestSchwarmRatioBonus(0));
    }
}
