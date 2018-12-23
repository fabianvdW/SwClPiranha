import datastructures.BitBoard;
import game.*;
import helpers.StringToGameStateConverter;

import java.util.List;
import java.util.Random;

public class Main {
    static long profiler=0L;
    static Random r;
    public static void main(String[] args) {
        BitBoardConstants.setSquareAttackDirectionSquareDestinationAttackLine();
        GameState g2 = new GameState();
        r= new Random();
        long l0=System.currentTimeMillis();
        for(int i=0;i<100000;i++){
            GameState g= playGame(false,g2);
        }
        long l1=System.currentTimeMillis();
        System.out.println(l1-l0);
        System.out.println(profiler);
    }

    public static GameState playGame(boolean verbose,GameState g) {
        g.analyze();
        if (verbose) {
            System.out.println(g);
            System.out.println("Plies: " + g.pliesPlayed);
            System.out.println("Rounds: " + g.roundsPlayed);
            System.out.println("GameState: " + g.gs);
        }
        while (g.gs == GameStatus.INGAME) {
            g= g.possibleFollowingStates.get(r.nextInt(g.possibleFollowingStates.size()));
            g.analyze();
            if (verbose) {
                System.out.println(g);
                System.out.println("Plies: " + g.pliesPlayed);
                System.out.println("Rounds: " + g.roundsPlayed);
                System.out.println("GameState: " + g.gs);
            }
        }
        return g;
    }
}
