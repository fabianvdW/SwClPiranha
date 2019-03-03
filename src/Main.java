import artificialplayer.AlphaBeta;
import game.*;

public class Main {
    static long profiler=0L;
    public static void main(String[] args) {
        BitBoardConstants.setSquareAttackDirectionSquareDestinationAttackLine("SwClPiranha/src/game/data.txt");
        MyGameState g2 = new MyGameState();
        long l0=System.currentTimeMillis();
        for(int i=0;i<1000000;i++){
            playGame(false,g2);
        }
        long l1=System.currentTimeMillis();
        System.out.println(l1-l0);
    }

    public static MyGameState playGame(boolean verbose, MyGameState g) {
        g.analyze();
        if (verbose) {
            System.out.println(g);
            System.out.println("Plies: " + g.pliesPlayed);
            System.out.println("Rounds: " + g.roundsPlayed);
            System.out.println("MyGameState: " + g.gs);
        }
        while (g.gs == GameStatus.INGAME) {
            g=g.gmro.states[(int)(Math.random()*g.gmro.instances)];
            g.analyze();
            if (verbose) {
                System.out.println(g);
                System.out.println("Plies: " + g.pliesPlayed);
                System.out.println("Rounds: " + g.roundsPlayed);
                System.out.println("MyGameState: " + g.gs);
            }
        }
        return g;
    }
}
