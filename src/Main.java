import datastructures.BitBoard;
import game.*;
import helpers.StringToGameStateConverter;

import java.util.List;

public class Main {
    public static void main(String[] args){
        BitBoardConstants.setSquareAttackDirectionSquareDestinationAttackLine();
        GameState g= StringToGameStateConverter.readGameState(StringToGameStateConverter.STANDARD_GAME_STATE);
        g.analyze();
        System.out.println(g);
        System.out.println("Plies: "+g.pliesPlayed);
        System.out.println("Rounds: "+g.roundsPlayed);
        System.out.println("GameState: "+g.gs);
        while(g.gs==GameStatus.INGAME){
            Object[] values = g.possibleMoves.values().toArray();
            g= (GameState) values[(int)(values.length*Math.random())];
            g.analyze();
            System.out.println(g);
            System.out.println("Plies: "+g.pliesPlayed);
            System.out.println("Rounds: "+g.roundsPlayed);
            System.out.println("GameState: "+g.gs);
        }
    }
}
