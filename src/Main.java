import datastructures.BitBoard;
import game.BitBoardConstants;
import game.GameColor;
import game.GameLogic;
import game.GameState;
import helpers.StringToGameStateConverter;

public class Main {
    public static void main(String[] args){
        BitBoardConstants.setSquareAttackDirectionSquareDestinationAttackLine();
        GameState g= StringToGameStateConverter.readGameState(StringToGameStateConverter.STANDARD_GAME_STATE);
        System.out.println(g);
        System.out.println(GameLogic.getPossibleMoves(g,GameColor.RED));
        System.out.println(GameLogic.getPossibleMoves(g,GameColor.RED).size());
    }
}
