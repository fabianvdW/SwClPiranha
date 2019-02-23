import artificialplayer.BoardRating;
import game.MyGameState;
import helpers.FEN;

public class Test {
    public static void main(String[] args) {
        MyGameState mg = FEN.readFEN("33652832 1731077155695628288 34225520640 2097660 0 36037593111986176 b 3 1");
        System.out.println(mg);
        System.out.println(BoardRating.rating(mg));
        //System.out.println(BoardRating.calculateBiggestSchwarmRatioBonus(0));
    }
}
