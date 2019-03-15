package testing;

import artificialplayer.AlphaBeta;
import artificialplayer.BoardRating;
import artificialplayer.BoardRatingConstants;
import evaltuning.Genome;
import game.BitBoardConstants;
import game.MyGameState;
import helpers.FEN;

public class EvalTesting {
    public static void main(String[] args) {
        BitBoardConstants.setSquareAttackDirectionSquareDestinationAttackLine("SwClPiranha/src/game/data.txt");
        MyGameState mg = FEN.readFEN("8736 5766296523843305472 8325824513 0 2 9007199254740992 r 28 14");
        System.out.println(mg);
        mg.analyze();
        System.out.println(BoardRating.rating(mg, new BoardRatingConstants(AlphaBeta.gaDna)));
    }
}
