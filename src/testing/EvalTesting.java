package testing;

import artificialplayer.BoardRating;
import artificialplayer.BoardRatingConstants;
import evaltuning.Genome;
import game.BitBoardConstants;
import game.MyGameState;
import helpers.FEN;

public class EvalTesting {
    public static void main(String[] args) {
        BitBoardConstants.setSquareAttackDirectionSquareDestinationAttackLine("SwClPiranha/src/game/data.txt");
        MyGameState mg = FEN.readFEN("16809984 5764620742945669120 8589934594 -9209790869228468128 1 8388608 r 38 19");
        System.out.println(mg);
        mg.analyze();
        System.out.println(BoardRating.rating(mg, new BoardRatingConstants(Genome.standardDna)));
    }
}
