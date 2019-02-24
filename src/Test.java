import artificialplayer.AlphaBeta;
import artificialplayer.BoardRating;
import artificialplayer.PrincipalVariation;
import game.BitBoardConstants;
import game.GameColor;
import game.MyGameState;
import helpers.FEN;
import helpers.Perft;

public class Test {
    public static void main(String[] args) {
        BitBoardConstants.setSquareAttackDirectionSquareDestinationAttackLine("SwClPiranha/src/game/data.txt");
        MyGameState mg = FEN.readFEN("33652832 722265891363357696 33957085184 4611686018427388414 0 17729624997888 b 3 1");
        System.out.println(mg);
        //System.out.println(Perft.perft(mg, 2));
        long curr = System.currentTimeMillis();
        PrincipalVariation pv = AlphaBeta.search(mg,  2000);
        System.out.println(pv.stack.get(0));
        System.out.println(pv.score);
        long now = System.currentTimeMillis();
        System.out.println((now - curr));
        System.out.println(AlphaBeta.nodesExamined);
        System.out.println(AlphaBeta.depth0Nodes);
        //System.out.println(BoardRating.calculateBiggestSchwarmRatioBonus(0));
    }
}
