import datastructures.BitBoard;
import game.*;
import helpers.StringToGameStateConverter;

import java.util.List;
import java.util.Random;

public class Main {
    static long profiler=0L;
    public static void main(String[] args) {
        BitBoardConstants.setSquareAttackDirectionSquareDestinationAttackLine();
        GameState g2 = new GameState();
        long l0=System.currentTimeMillis();
        for(int i=0;i<100000;i++){
            playGame(false,g2);
        }
        long l1=System.currentTimeMillis();
        System.out.println(l1-l0);
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
            g=g.gmro.states[(int)(Math.random()*g.gmro.instances)];
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
