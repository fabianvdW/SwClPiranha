package artificialplayer;

import client.Logic;
import datastructures.BitBoard;
import game.*;
import helpers.FEN;

public class AlphaBeta extends ArtificalPlayer {
    public static int nodesExamined;
    public static int depth0Nodes;
    public static double[] gaDna = {0.9025510163556673, 2.657765703271754, -0.6639782702608146, 4.614780818192207, 1.2196253480425627, 5.049480353040995, 0.38931830719247384, -2.081605428423991, -2.3353965528221905, -5.049202306669282, -2.727337976086494};
    public static BoardRatingConstants brc = new BoardRatingConstants(gaDna);
    public static Search currentSearch = new Search(null, -1);

    public static void main(String[] args) {
        ArtificalPlayer a = new AlphaBeta();
        a.main(args[0]);
    }

    public static PrincipalVariation search(MyGameState mg, int time) {
        currentSearch = new Search(mg, 100);
        currentSearch.start();
        try {
            Thread.sleep(time - 100);
        } catch (Exception e) {
            e.printStackTrace();
        }
        currentSearch.stop = true;
        return currentSearch.currentBestPv;
    }

    @Override
    public PrincipalVariation requestMove(int time) {
        //
        return search(this.mg, time);
    }

    //Rot ist 1, Blaue ist -1
    public static PrincipalVariation alphaBeta(MyGameState g, int depth, int maximizingPlayer, double alpha, double beta) {
        PrincipalVariation currPv = new PrincipalVariation(depth);
        if (currentSearch.stop) {
            return currPv;
        }
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
            currPv.score = BoardRating.rating(g, AlphaBeta.brc) * maximizingPlayer;
            return currPv;
        }
        PrincipalVariation bestPv = new PrincipalVariation(depth);
        GameMoveResultObject gmro = g.gmro;
        g.gmro = null;
        //Probe tablebase
        int moveOrderingIndex = 0;
        boolean pvmoveFound = false;
        if (depth >= 1) {
            CacheEntry ce = Search.cache[(int) (g.hash & Search.cacheMask)];
            if (ce != null && ce.hash == g.hash) {
                //Cache-hit
                if (ce.depth >= depth) {
                    ce.birth = Search.birthTime;
                    currPv.stack.add(ce.gm);
                    currPv.hashStack.add(ce.hash);
                    currPv.score = ce.score;
                    return currPv;
                } else {
                    //Move ordering
                    //Swap move and state from pos 0
                    moveOrderingIndex = 1;
                    int index = -1;
                    for (int i = 0; i < gmro.instances; i++) {
                        if (gmro.moves[i].from == ce.gm.from && gmro.moves[i].to == ce.gm.to) {
                            index = i;
                            break;
                        }
                    }
                    pvmoveFound = ce.pvNode && ce.birth == Search.birthTime;
                    GameMove atPos0 = gmro.moves[0];
                    MyGameState atPos0S = gmro.states[0];
                    gmro.moves[0] = gmro.moves[index];
                    gmro.states[0] = gmro.states[index];
                    gmro.moves[index] = atPos0;
                    gmro.states[index] = atPos0S;

                }
            }
        }
        //Search for Killer Moves and then for Captures
        //Killer move
        if (depth >= 2
                && g.pliesPlayed > 20
                ) {
            for (int i = moveOrderingIndex; i < gmro.instances; i++) {
                GameMove move = gmro.moves[i];
                MyGameState nextGameState = gmro.states[i];

                BitBoard gegnerFischeNow = g.move == GameColor.BLUE ? g.roteFische : g.blaueFische;
                BitBoard gegnerFischeGleich = g.move == GameColor.BLUE ? nextGameState.roteFische : nextGameState.blaueFische;
                if (BoardRating.getBiggestSchwarm(g, g.move) < BoardRating.getBiggestSchwarm(nextGameState, g.move)
                        || (gegnerFischeNow.popCount() > gegnerFischeGleich.popCount())
                        && BoardRating.getBiggestSchwarm(g, nextGameState.move) > BoardRating.getBiggestSchwarm(nextGameState, nextGameState.move)) {
                    //Found Killer move
                    GameMove atPosIndex = gmro.moves[moveOrderingIndex];
                    MyGameState atPosIndexState = gmro.states[moveOrderingIndex];
                    gmro.moves[moveOrderingIndex] = move;
                    gmro.states[moveOrderingIndex] = nextGameState;
                    gmro.moves[i] = atPosIndex;
                    gmro.states[i] = atPosIndexState;
                    moveOrderingIndex++;
                }
            }
        }
        //Captures
        for (int i = moveOrderingIndex; i < gmro.instances; i++) {
            GameMove move = gmro.moves[i];
            MyGameState nextGameState = gmro.states[i];
            if (g.blaueFische.popCount() + g.roteFische.popCount() > nextGameState.blaueFische.popCount() + nextGameState.roteFische.popCount()) {
                //Found Capture move
                GameMove atPosIndex = gmro.moves[moveOrderingIndex];
                MyGameState atPosIndexState = gmro.states[moveOrderingIndex];
                gmro.moves[moveOrderingIndex] = move;
                gmro.states[moveOrderingIndex] = nextGameState;
                gmro.moves[i] = atPosIndex;
                gmro.states[i] = atPosIndexState;
                moveOrderingIndex++;
            }
        }

        for (int i = 0; i < gmro.instances; i++) {
            currPv.stack.add(gmro.moves[i]);
            currPv.hashStack.add(g.hash);
            PrincipalVariation followingPv;
            if (depth <= 2 || !pvmoveFound || i == 0) {
                followingPv = alphaBeta(gmro.states[i], depth - 1, -maximizingPlayer, -beta, -alpha);
            } else {
                followingPv = alphaBeta(gmro.states[i], depth - 1, -maximizingPlayer, -alpha - 1, -alpha);
                double rat = followingPv.score * -1;
                if (rat >= alpha && rat <= beta) {
                    followingPv = alphaBeta(gmro.states[i], depth - 1, -maximizingPlayer, -beta, -rat);
                }
            }
            double rat = followingPv.score * -1;
            if (rat > bestPv.score) {
                bestPv = currPv.clone();
                bestPv.score = rat;
                bestPv.stack.addAll(followingPv.stack);
                bestPv.hashStack.addAll(followingPv.hashStack);
            }
            if (bestPv.score > alpha) {
                alpha = bestPv.score;
            }
            if (alpha > beta) {
                break;
            }
            currPv.stack.remove(currPv.stack.size() - 1);
            currPv.hashStack.remove(currPv.hashStack.size() - 1);
        }
        //Make entry
        if (depth >= 1) {
            int cacheIndex = (int) (g.hash & Search.cacheMask);
            if (Search.cache[cacheIndex] == null) {
                Search.cache[cacheIndex] = new CacheEntry(g.hash, bestPv.score, Search.birthTime, (byte) depth, bestPv.stack.get(0), false);
            } else {
                CacheEntry ce = Search.cache[cacheIndex];
                if (!ce.pvNode && ce.depth - (Search.birthTime - ce.birth) <= depth) {
                    Search.cache[cacheIndex] = new CacheEntry(g.hash, bestPv.score, Search.birthTime, (byte) depth, bestPv.stack.get(0), false);
                }
            }
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
        int moveOrderingIndex = 0;
        boolean pvmoveFound = false;
        //Probe tablebase
        if (depth >= 1) {
            CacheEntry ce = Search.cache[(int) (g.hash & Search.cacheMask)];
            if (ce != null && ce.hash == g.hash) {
                //Cache-hit
                if (ce.depth >= depth) {
                    ce.birth = Search.birthTime;
                    pv.stack.add(ce.gm);
                    pv.hashStack.add(ce.hash);
                    pv.score = ce.score;
                    return pv;
                } else {
                    //Move ordering
                    //Swap move and state from pos 0
                    moveOrderingIndex = 1;
                    int index = -1;
                    for (int i = 0; i < gmro.instances; i++) {
                        if (gmro.moves[i].from == ce.gm.from && gmro.moves[i].to == ce.gm.to) {
                            index = i;
                            break;
                        }
                    }
                    pvmoveFound = ce.pvNode && ce.birth == Search.birthTime;
                    GameMove atPos0 = gmro.moves[0];
                    MyGameState atPos0S = gmro.states[0];
                    gmro.moves[0] = gmro.moves[index];
                    gmro.states[0] = gmro.states[index];
                    gmro.moves[index] = atPos0;
                    gmro.states[index] = atPos0S;

                }
            }
        }
        //Move ordering pt.2
        //Search for Killer Moves and then for Captures
        //Killer move
        if (depth >= 2
                && g.pliesPlayed > 20
                ) {
            for (int i = moveOrderingIndex; i < gmro.instances; i++) {
                GameMove move = gmro.moves[i];
                MyGameState nextGameState = gmro.states[i];

                BitBoard gegnerFischeNow = g.move == GameColor.BLUE ? g.roteFische : g.blaueFische;
                BitBoard gegnerFischeGleich = g.move == GameColor.BLUE ? nextGameState.roteFische : nextGameState.blaueFische;
                if (BoardRating.getBiggestSchwarm(g, g.move) < BoardRating.getBiggestSchwarm(nextGameState, g.move)
                        || (gegnerFischeNow.popCount() > gegnerFischeGleich.popCount())
                        && BoardRating.getBiggestSchwarm(g, nextGameState.move) > BoardRating.getBiggestSchwarm(nextGameState, nextGameState.move)) {
                    //Found Killer move
                    GameMove atPosIndex = gmro.moves[moveOrderingIndex];
                    MyGameState atPosIndexState = gmro.states[moveOrderingIndex];
                    gmro.moves[moveOrderingIndex] = move;
                    gmro.states[moveOrderingIndex] = nextGameState;
                    gmro.moves[i] = atPosIndex;
                    gmro.states[i] = atPosIndexState;
                    moveOrderingIndex++;
                }
            }
        }
        //Captures
        for (int i = moveOrderingIndex; i < gmro.instances; i++) {
            GameMove move = gmro.moves[i];
            MyGameState nextGameState = gmro.states[i];
            if (g.blaueFische.popCount() + g.roteFische.popCount() > nextGameState.blaueFische.popCount() + nextGameState.roteFische.popCount()) {
                //Found Capture move
                GameMove atPosIndex = gmro.moves[moveOrderingIndex];
                MyGameState atPosIndexState = gmro.states[moveOrderingIndex];
                gmro.moves[moveOrderingIndex] = move;
                gmro.states[moveOrderingIndex] = nextGameState;
                gmro.moves[i] = atPosIndex;
                gmro.states[i] = atPosIndexState;
                moveOrderingIndex++;
            }
        }

        for (int i = 0; i < gmro.instances; i++) {
            pv.stack.add(gmro.moves[i]);
            pv.hashStack.add(g.hash);
            //PrincipalVariation currentPv = alphaBeta(gmro.states[i], depth - 1, -maximizingPlayer, -1000, 1000);
            PrincipalVariation currentPv;
            if (depth <= 2 || !pvmoveFound || i == 0) {
                if (i != 0) {
                    currentPv = alphaBeta(gmro.states[i], depth - 1, -maximizingPlayer, -bestPv.score, 1000);
                } else {
                    currentPv = alphaBeta(gmro.states[i], depth - 1, -maximizingPlayer, -1000, 1000);
                }
            } else {
                currentPv = alphaBeta(gmro.states[i], depth - 1, -maximizingPlayer, -bestPv.score - 1, -bestPv.score);
                double rat = currentPv.score * -1;
                if (rat >= bestPv.score) {
                    currentPv = alphaBeta(gmro.states[i], depth - 1, -maximizingPlayer, -1000, -rat);
                }
            }
            double rat = currentPv.score * -1;
            if (rat > bestPv.score) {
                bestPv = pv.clone();
                bestPv.score = rat;
                bestPv.stack.addAll(currentPv.stack);
                bestPv.hashStack.addAll(currentPv.hashStack);
            }
            pv.stack.remove(pv.stack.size() - 1);
            pv.hashStack.remove(pv.hashStack.size() - 1);
        }
        return bestPv;
    }


}
