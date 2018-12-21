import game.GameColor;
import game.GameLogic;
import game.GameState;
import helpers.StringToGameStateConverter;

public class Main {
    public static void main(String[] args){
        GameState g= StringToGameStateConverter.readGameState(StringToGameStateConverter.STANDARD_GAME_STATE);
        System.out.println(g);
    }
}
