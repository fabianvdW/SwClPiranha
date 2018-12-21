import game.GameColor;
import game.GameLogic;
import game.GameState;
import helpers.StringToGameStateConverter;
import org.junit.Assert;
import org.junit.Test;

public class GameLogicTest {
    @Test
    public void schwarmCountTest(){
        String[][] g={
                {" "," "," "," "," "," "," "," "," "," "},
                {"r"," "," "," "," "," "," "," "," ","r"},
                {"r"," "," "," "," "," "," "," "," ","r"},
                {"r"," "," "," "," ","k"," "," "," ","r"},
                {"r"," "," "," "," "," "," "," "," ","r"},
                {"r"," "," "," "," "," "," "," "," ","r"},
                {"r"," "," "," ","k"," "," "," "," ","r"},
                {"r"," "," "," "," "," "," "," "," ","r"},
                {"r"," "," "," "," "," "," "," "," ","r"},
                {" "," "," "," "," "," "," "," "," "," "}
        };
        GameState gs=StringToGameStateConverter.readGameState(g);
        Assert.assertEquals(GameLogic.getSchwarm(gs,GameColor.RED),8);
        Assert.assertEquals(GameLogic.getSchwarm(gs,GameColor.BLUE),0);

        String[][] g2={
                {" "," "," "," "," "," "," "," "," "," "},
                {" "," "," "," "," "," "," "," ","r","r"},
                {" "," "," "," "," "," "," "," ","r","r"},
                {" "," "," "," "," ","k"," "," ","r","r"},
                {" "," "," "," "," "," "," "," ","r","r"},
                {" "," "," "," "," "," "," "," ","r","r"},
                {" "," "," "," ","k"," "," "," ","r","r"},
                {" "," "," "," "," "," "," "," ","r","r"},
                {" "," "," "," "," "," "," "," ","r","r"},
                {" "," "," "," "," "," "," "," "," "," "}
        };
        gs=StringToGameStateConverter.readGameState(g2);
        Assert.assertEquals(GameLogic.getSchwarm(gs,GameColor.RED),16);
        Assert.assertEquals(GameLogic.getSchwarm(gs,GameColor.BLUE),0);

        String[][] g3={
                {" "," "," "," "," "," "," "," "," "," "},
                {" "," "," "," "," "," "," "," "," "," "},
                {" "," "," "," ","r","r","r"," "," "," "},
                {" "," "," "," ","r","k","r"," "," "," "},
                {" "," "," ","r"," "," ","r"," "," "," "},
                {" "," ","r"," "," "," ","r"," "," "," "},
                {" ","r"," "," ","k"," "," ","r"," "," "},
                {"r"," "," "," "," "," "," "," ","r"," "},
                {" "," "," "," "," "," "," "," "," ","r"},
                {" "," "," "," "," "," "," "," "," "," "}
        };
        gs=StringToGameStateConverter.readGameState(g3);
        Assert.assertEquals(14,GameLogic.getSchwarm(gs,GameColor.RED));
        Assert.assertEquals(0,GameLogic.getSchwarm(gs,GameColor.BLUE));

        String[][] g4={
                {" "," "," "," "," "," "," "," "," "," "},
                {" "," "," "," "," "," "," "," "," "," "},
                {" "," "," "," ","r","r","r"," "," "," "},
                {" "," "," "," "," ","k","r"," "," "," "},
                {" "," "," ","r"," "," ","r"," "," "," "},
                {" "," ","r"," "," "," ","r"," "," "," "},
                {" ","r"," "," ","k"," "," ","r"," "," "},
                {"r"," "," "," "," "," "," "," ","r"," "},
                {" "," "," "," "," "," "," "," "," ","r"},
                {" "," "," "," "," "," "," "," "," "," "}
        };
        gs=StringToGameStateConverter.readGameState(g4);
        Assert.assertEquals(9,GameLogic.getSchwarm(gs,GameColor.RED));
        Assert.assertEquals(0,GameLogic.getSchwarm(gs,GameColor.BLUE));


    }
}
