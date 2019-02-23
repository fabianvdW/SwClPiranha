import game.BitBoardConstants;
import game.GameColor;
import game.GameLogic;
import game.MyGameState;
import helpers.Perft;
import helpers.StringToGameStateConverter;
import org.junit.Assert;
import org.junit.Test;

public class GameLogicTest {
    @Test
    public void schwarmCountTest() {
        BitBoardConstants.setSquareAttackDirectionSquareDestinationAttackLine("SwClPiranha/src/game/data.txt");
        String[][] g = {
                {" ", " ", " ", " ", " ", " ", " ", " ", " ", " "},
                {"r", " ", " ", " ", " ", " ", " ", " ", " ", "r"},
                {"r", " ", " ", " ", " ", " ", " ", " ", " ", "r"},
                {"r", " ", " ", " ", " ", "k", " ", " ", " ", "r"},
                {"r", " ", " ", " ", " ", " ", " ", " ", " ", "r"},
                {"r", " ", " ", " ", " ", " ", " ", " ", " ", "r"},
                {"r", " ", " ", " ", "k", " ", " ", " ", " ", "r"},
                {"r", " ", " ", " ", " ", " ", " ", " ", " ", "r"},
                {"r", " ", " ", " ", " ", " ", " ", " ", " ", "r"},
                {" ", " ", " ", " ", " ", " ", " ", " ", " ", " "}
        };
        MyGameState gs = StringToGameStateConverter.readGameState(g);
        Assert.assertEquals(GameLogic.getSchwarm(gs, GameColor.RED), 8);
        Assert.assertEquals(GameLogic.getSchwarm(gs, GameColor.BLUE), 0);

        String[][] g2 = {
                {" ", " ", " ", " ", " ", " ", " ", " ", " ", " "},
                {" ", " ", " ", " ", " ", " ", " ", " ", "r", "r"},
                {" ", " ", " ", " ", " ", " ", " ", " ", "r", "r"},
                {" ", " ", " ", " ", " ", "k", " ", " ", "r", "r"},
                {" ", " ", " ", " ", " ", " ", " ", " ", "r", "r"},
                {" ", " ", " ", " ", " ", " ", " ", " ", "r", "r"},
                {" ", " ", " ", " ", "k", " ", " ", " ", "r", "r"},
                {" ", " ", " ", " ", " ", " ", " ", " ", "r", "r"},
                {" ", " ", " ", " ", " ", " ", " ", " ", "r", "r"},
                {" ", " ", " ", " ", " ", " ", " ", " ", " ", " "}
        };
        gs = StringToGameStateConverter.readGameState(g2);
        Assert.assertEquals(GameLogic.getSchwarm(gs, GameColor.RED), 16);
        Assert.assertEquals(GameLogic.getSchwarm(gs, GameColor.BLUE), 0);

        String[][] g3 = {
                {" ", " ", " ", " ", " ", " ", " ", " ", " ", " "},
                {" ", " ", " ", " ", " ", " ", " ", " ", " ", " "},
                {" ", " ", " ", " ", "r", "r", "r", " ", " ", " "},
                {" ", " ", " ", " ", "r", "k", "r", " ", " ", " "},
                {" ", " ", " ", "r", " ", " ", "r", " ", " ", " "},
                {" ", " ", "r", " ", " ", " ", "r", " ", " ", " "},
                {" ", "r", " ", " ", "k", " ", " ", "r", " ", " "},
                {"r", " ", " ", " ", " ", " ", " ", " ", "r", " "},
                {" ", " ", " ", " ", " ", " ", " ", " ", " ", "r"},
                {" ", " ", " ", " ", " ", " ", " ", " ", " ", " "}
        };
        gs = StringToGameStateConverter.readGameState(g3);
        Assert.assertEquals(14, GameLogic.getSchwarm(gs, GameColor.RED));
        Assert.assertEquals(0, GameLogic.getSchwarm(gs, GameColor.BLUE));

        String[][] g4 = {
                {" ", " ", " ", " ", " ", " ", " ", " ", " ", " "},
                {" ", " ", " ", " ", " ", " ", " ", " ", " ", " "},
                {" ", " ", " ", " ", "r", "r", "r", " ", " ", " "},
                {" ", " ", " ", " ", " ", "k", "r", " ", " ", " "},
                {" ", " ", " ", "r", " ", " ", "r", " ", " ", " "},
                {" ", " ", "r", " ", " ", " ", "r", " ", " ", " "},
                {" ", "r", " ", " ", "k", " ", " ", "r", " ", " "},
                {"r", " ", " ", " ", " ", " ", " ", " ", "r", " "},
                {" ", " ", " ", " ", " ", " ", " ", " ", " ", "r"},
                {" ", " ", " ", " ", " ", " ", " ", " ", " ", " "}
        };
        gs = StringToGameStateConverter.readGameState(g4);
        Assert.assertEquals(9, GameLogic.getSchwarm(gs, GameColor.RED));
        Assert.assertEquals(0, GameLogic.getSchwarm(gs, GameColor.BLUE));

        String[][] g5 = {
                {" ", "b", "b", "b", "b", "b", "b", "b", "b", " "},
                {"r", " ", " ", " ", " ", " ", " ", " ", " ", "r"},
                {"r", " ", " ", " ", " ", " ", " ", " ", " ", "r"},
                {"r", " ", " ", " ", " ", "k", " ", " ", " ", "r"},
                {"r", " ", " ", " ", " ", " ", " ", " ", " ", "r"},
                {"r", " ", " ", " ", " ", " ", " ", " ", " ", "r"},
                {"r", " ", " ", " ", "k", " ", " ", " ", " ", "r"},
                {"r", " ", " ", " ", " ", " ", " ", " ", " ", "r"},
                {"r", " ", " ", " ", " ", " ", " ", " ", " ", "r"},
                {" ", "b", "b", "b", "b", "b", "b", "b", "b", " "}
        };
        gs = StringToGameStateConverter.readGameState(g5);
        Assert.assertEquals(Perft.perft(gs, 1), 48);
        Assert.assertEquals(Perft.perft(gs, 2), 2244);
        Assert.assertEquals(Perft.perft(gs, 3), 109086);
        Assert.assertEquals(Perft.perft(gs, 4), 5131516);
        Assert.assertEquals(Perft.perft(gs, 5), 250613480);

        String[][] g6 = {
                {" ", "b", " ", " ", " ", " ", " ", " ", "b", " "},
                {" ", "r", " ", "b", "b", "b", "b", " ", "r", " "},
                {" ", " ", " ", "b", "r", "r", "b", " ", " ", " "},
                {" ", " ", "r", " ", " ", "k", "r", " ", "b", " "},
                {" ", " ", "r", " ", " ", " ", " ", " ", "r", " "},
                {" ", " ", " ", " ", "k", " ", "r", " ", " ", " "},
                {" ", " ", " ", " ", "r", " ", " ", " ", " ", " "},
                {" ", " ", " ", "b", " ", "b", " ", " ", " ", " "},
                {" ", " ", " ", " ", "b", " ", "b", " ", " ", " "},
                {" ", " ", " ", " ", " ", " ", " ", " ", " ", " "}};
        gs = StringToGameStateConverter.readGameState(g6);
        Assert.assertEquals(Perft.perft(gs, 1), 37);
        Assert.assertEquals(Perft.perft(gs, 2), 1057);
        Assert.assertEquals(Perft.perft(gs, 3), 38912);
        Assert.assertEquals(Perft.perft(gs, 4), 1265852);
        Assert.assertEquals(Perft.perft(gs, 5), 46863294);

    }
}
