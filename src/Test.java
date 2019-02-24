import artificialplayer.BoardRating;
import game.BitBoardConstants;
import game.MyGameState;
import helpers.FEN;

public class Test {
    public static void main(String[] args) {
        BitBoardConstants.setSquareAttackDirectionSquareDestinationAttackLine("SwClPiranha/src/game/data.txt");
        MyGameState mg = FEN.readFEN("33554464 1731076056188203008 20937968640 2251842763358398 0 70385924046848 r 16 8");
        mg.analyze();
        System.out.println(mg);
        System.out.println(BoardRating.rating(mg));
        //System.out.println(BoardRating.calculateBiggestSchwarmRatioBonus(0));
    }
}
