import datastructures.BitBoard;
import game.BitBoardConstants;
import game.GameStatus;
import game.MyGameState;
import helpers.FEN;
import org.junit.Assert;
import org.junit.Test;

public class HashingTest {
    @Test
    public void testHashFunction() {
        BitBoardConstants.setSquareAttackDirectionSquareDestinationAttackLine("SwClPiranha/src/game/data.txt");
        for (int i = 0; i < 100; i++) {
            MyGameState mg = new MyGameState();
            mg.analyze();
            while (mg.gs == GameStatus.INGAME) {
                mg = mg.gmro.states[(int) (Math.random() * mg.gmro.instances)];
                mg.analyze();
                MyGameState mg2Clone = FEN.readFEN(FEN.toFEN(mg));
                mg2Clone.hash = MyGameState.calculateHash(mg2Clone);
                Assert.assertEquals(mg.hash, mg2Clone.hash);
            }
        }
    }
}
