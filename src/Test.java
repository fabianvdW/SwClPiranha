import artificialplayer.AlphaBeta;
import artificialplayer.BoardRating;
import artificialplayer.PrincipalVariation;
import artificialplayer.Search;
import game.*;
import helpers.FEN;
import helpers.Perft;
import sun.misc.Perf;

public class Test {
    public static void main(String[] args) {
        BitBoardConstants.setSquareAttackDirectionSquareDestinationAttackLine("SwClPiranha/src/game/data.txt");
        MyGameState mg = FEN.readFEN("33620064 1875187945726018560 34225520640 510 1 34359738368 b 1 0");
        System.out.println(mg);
        long curr = System.currentTimeMillis();
        mg.analyze();
        A:
        while (mg.gs == GameStatus.INGAME) {
            GameMoveResultObject gmro = mg.gmro;
            if (mg.move == GameColor.RED) {
                mg = gmro.states[0];
                mg.analyze();
            } else {
                Search s = new Search(mg);
                s.run();
                PrincipalVariation pv = s.currentBestPv;
                GameMove mv = pv.stack.get(0);
                for (int i = 0; i < gmro.instances; i++) {
                    if (gmro.moves[i].to == mv.to && gmro.moves[i].from == mv.from) {
                        mg = gmro.states[i];
                        mg.analyze();
                        continue A;
                    }
                }
                System.exit(-3);
            }
        }
        long now = System.currentTimeMillis();
        System.out.println(mg);
        System.out.println(mg.pliesPlayed);
        System.out.println(mg.hash);
        System.out.println((now - curr));
        System.out.println(AlphaBeta.nodesExamined);
        System.out.println(AlphaBeta.depth0Nodes);
        Search.investigateCache();
        //System.out.println(BoardRating.calculateBiggestSchwarmRatioBonus(0));
    }
}
