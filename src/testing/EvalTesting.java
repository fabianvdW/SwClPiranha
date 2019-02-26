package testing;

import artificialplayer.BoardRating;
import game.MyGameState;
import helpers.FEN;

public class EvalTesting {
    public static void main(String[] args){
        MyGameState mg = FEN.readFEN("16809984 5764620742945669120 8589934594 -9209790869228468128 1 8388608 r 38 19");
        System.out.println(mg);
        System.out.println(BoardRating.rating(mg));
    }
}
