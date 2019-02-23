package artificialplayer;

import game.*;
import helpers.StringToGameStateConverter;

public class AlphaBeta extends ArtificalPlayer {
    public static void main(String[] args) {
        ArtificalPlayer a = new AlphaBeta();
        a.main(args[0]);
    }

    //Rot ist 1, Blaue ist -1
    public static double alphaBeta(MyGameState g, int depth, int maximizingPlayer, double alpha, double beta) {
        g.analyze();
        if (g.gs != GameStatus.INGAME) {
            if (g.gs == GameStatus.DRAW) {
                return 0;
            } else if (g.gs == GameStatus.RED_WIN) {
                return maximizingPlayer * 300;
            } else {
                return maximizingPlayer * -300;
            }
        }
        if (depth == 0) {
            return BoardRating.rating(g) * maximizingPlayer;
        }
        double val = -100000;
        GameMoveResultObject gmro = g.gmro;
        g.gmro = null;
        for (int i = 0; i < gmro.instances; i++) {
            double rat = -alphaBeta(gmro.states[i], depth - 1, -maximizingPlayer, -beta, -alpha);
            if (rat > val) {
                val = rat;
            }
            if (val > alpha) {
                alpha = val;
            }
            if (alpha > beta) {
                break;
            }
        }
        return val;

    }

    public static GameMove alphaBetaRoot(MyGameState g, int depth, int maximizingPlayer) {
        g.analyze();
        if (g.gs != GameStatus.INGAME) {
            return null;
        }
        if (depth == 0) {
            return null;
        }
        double val = -100000;
        GameMove bestMove = null;
        GameMoveResultObject gmro = g.gmro;
        g.gmro = null;
        for (int i = 0; i < gmro.instances; i++) {
            double rat = -alphaBeta(gmro.states[i], depth - 1, -maximizingPlayer, -1000, 1000);
            if (rat > val) {
                val = rat;
                bestMove = gmro.moves[i];
            }
        }
        return bestMove;
    }

    @Override
    public GameMove requestMove() {
        return AlphaBeta.alphaBetaRoot(this.mg, 3, this.mg.move == GameColor.RED ? 1 : -1);
    }
}

