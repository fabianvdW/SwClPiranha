import game.GameColor;
import game.GameLogic;
import game.GameState;

public class Main {
    public static void main(String[] args){
        GameState g= new GameState();
        System.out.println(g);
        System.out.println(GameLogic.getSchwarm(g,GameColor.BLUE));
    }
}
