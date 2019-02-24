package artificialplayer;

import game.*;
import helpers.StringToGameStateConverter;

import java.util.TimerTask;

public class AlphaBeta extends ArtificalPlayer {
    public static int nodesExamined;
    public static int depth0Nodes;

    public static void main(String[] args) {
        ArtificalPlayer a = new AlphaBeta();
        a.main(args[0]);
    }

    public static PrincipalVariation search(MyGameState mg, int time) {
        Search s = new Search(mg);
        s.start();
        try {
            Thread.sleep(time - 100);
        } catch (Exception e) {
            e.printStackTrace();
        }
        new java.util.Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                s.stop();
            }
        }, 1);
        return s.currentBestPv;
    }

    @Override
    public PrincipalVariation requestMove(int time) {
        //
        return search(this.mg, time);
    }

    //Rot ist 1, Blaue ist -1
    public static PrincipalVariation alphaBeta(MyGameState g, int depth, int maximizingPlayer, double alpha, double beta) {
        PrincipalVariation currPv = new PrincipalVariation(depth);
        nodesExamined++;
        g.analyze();
        if (g.gs != GameStatus.INGAME) {
            if (g.gs == GameStatus.DRAW) {
                currPv.score = 0;
                return currPv;
            } else if (g.gs == GameStatus.RED_WIN) {
                currPv.score = maximizingPlayer * 300;
                return currPv;
            } else {
                currPv.score = maximizingPlayer * -300;
                return currPv;
            }
        }
        if (depth == 0) {
            depth0Nodes++;
            currPv.score = BoardRating.rating(g) * maximizingPlayer;
            return currPv;
        }
        PrincipalVariation bestPv = new PrincipalVariation(depth);
        GameMoveResultObject gmro = g.gmro;
        g.gmro = null;
        for (int i = 0; i < gmro.instances; i++) {
            currPv.stack.add(gmro.moves[i]);
            PrincipalVariation followingPv = alphaBeta(gmro.states[i], depth - 1, -maximizingPlayer, -beta, -alpha);
            double rat = followingPv.score * -1;
            if (rat > bestPv.score) {
                bestPv = currPv.clone();
                bestPv.score = rat;
                bestPv.stack.addAll(followingPv.stack);
            }
            if (bestPv.score > alpha) {
                alpha = bestPv.score;
            }
            if (alpha > beta) {
                break;
            }
            currPv.stack.remove(currPv.stack.size() - 1);
        }
        return bestPv;

    }

    public static PrincipalVariation alphaBetaRoot(MyGameState g, int depth, int maximizingPlayer) {
        PrincipalVariation pv = new PrincipalVariation(depth);
        nodesExamined++;
        g.analyze();
        if (g.gs != GameStatus.INGAME) {
            return null;
        }
        if (depth == 0) {
            return null;
        }
        PrincipalVariation bestPv = new PrincipalVariation(depth);
        GameMoveResultObject gmro = g.gmro;
        g.gmro = null;
        for (int i = 0; i < gmro.instances; i++) {
            pv.stack.add(gmro.moves[i]);
            PrincipalVariation currentPv = alphaBeta(gmro.states[i], depth - 1, -maximizingPlayer, -1000, 1000);
            double rat = currentPv.score * -1;
            if (rat > bestPv.score) {
                bestPv = pv.clone();
                bestPv.score = rat;
                bestPv.stack.addAll(currentPv.stack);
            }
            pv.stack.remove(pv.stack.size() - 1);
        }
        return bestPv;
    }


}

class Search extends Thread {
    public PrincipalVariation currentBestPv;

    MyGameState mg;

    public Search(MyGameState mg) {
        this.mg = mg;
    }

    public void run() {
        for (int depth = 1; depth < 100; depth++) {
            currentBestPv = AlphaBeta.alphaBetaRoot(this.mg, depth, mg.move == GameColor.RED ? 1 : -1);
            //System.out.println("Depth: "+depth+" searched!");
        }
    }
}
