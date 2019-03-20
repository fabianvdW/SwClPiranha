package artificialplayer;

import datastructures.BitBoard;
import game.*;


public class AlphaBeta extends ArtificalPlayer {
    public static int nodesExamined;
    public static int depth0Nodes;
    public static int quiesenceNodes;
    public static boolean nullmove = false;
    public static int killerMovesFound = 0;
    public static int noKillerMovesFound = 0;

    public static int[] indexs = new int[95];
    //(v2)
    //public static double[] gaDna = {1.1929413659945325, -0.8731702020117803, -0.18857558152186485, -0.28516712873434763, 0.2632768554065141, 0.8993662608977158, 0.13348138971818577, -0.22333247871946682, -0.4039322894096489, 0.26511382721538596, 0.1665940335462095, 0.8675391276945661, 1.3508070853980805, 0.9575173529821485, 1.4240074329668702, -0.20112452911349518, -0.49828852243518695, -0.10684710598696326, -0.9626061894702808, -0.19868752897198622, 1.6581937255059032};
    // (v3)
    public static double[] gaDna = {
            //1,Phase,(1-Phase)
            0.8, 0, 0.3,
            0, 0, -2.0,
            -3.5, 0, -7.0,
            1.0, 4.0, 0,
            0, 8.0, 0,
            0, 0, -0.4
    };

    public static BoardRatingConstants brc = new BoardRatingConstants(gaDna);
    public static Search currentSearch = new Search(null, -1);

    public static void main(String[] args) {
        ArtificalPlayer a = new AlphaBeta();
        a.main(args[0]);
    }

    public static PrincipalVariation search(MyGameState mg, int time, byte birth) {
        currentSearch = new Search(mg, 100);
        currentSearch.birthTime = birth;
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
    public PrincipalVariation requestMove(int time, byte birth) {
        //
        return search(this.mg, time, birth);
    }

    public static double quiescenseSearch(Search search, double alpha, double beta, MyGameState mg, int maximizingPlayer) {
        if (mg.gmro == null) {
            nodesExamined++;
            quiesenceNodes++;
            mg.analyze();
        }
        if (mg.gs != GameStatus.INGAME) {
            if (mg.gs == GameStatus.DRAW) {
                return 0;
            } else if (mg.gs == GameStatus.RED_WIN) {
                return maximizingPlayer * (30000 - mg.pliesPlayed);
            } else {
                return maximizingPlayer * (-30000 + mg.pliesPlayed);
            }
        }
        double rat = BoardRating.rating(mg, AlphaBeta.brc) * maximizingPlayer;
        if (rat >= beta) {
            return beta;
        }
        if (rat > alpha) {
            alpha = rat;
        }
        GameMoveResultObject gmro = mg.gmro;
        mg.gmro = null;
        int moveOrderingIndex = 0;
        //Cache thingies

        {
            QuiesenceCacheEntry ce = search.quiesenceCache[(int) (mg.hash & Search.quiesenceCacheMask)];
            if (ce != null && ce.hash == mg.hash) {
                //Cache-hit
                int deltaBirth = search.birthTimeQuiesence - ce.birth;

                if (deltaBirth == 0 || ce.isSafe) {
                    if (!ce.betaNode && !ce.alphaNode) {
                        return ce.score;
                    } else {
                        if (ce.betaNode) {
                            if (ce.score > alpha) {
                                alpha = ce.score;
                            }
                        } else {
                            if (ce.score < beta) {
                                beta = ce.score;
                            }
                        }
                    }
                }
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
                GameMove atPos0 = gmro.moves[0];
                MyGameState atPos0S = gmro.states[0];
                gmro.moves[0] = gmro.moves[index];
                gmro.states[0] = gmro.states[index];
                gmro.moves[index] = atPos0;
                gmro.states[index] = atPos0S;
            }
        }

        //Legal moves connect the schwarm
        int meineFische = mg.move == GameColor.RED ? mg.roteFische.popCount() : mg.blaueFische.popCount();
        BitBoard biggestSchwarmBoard = BoardRating.getBiggestSchwarmBoard(mg, mg.move);
        BitBoard biggestSchwarmBoardCl = biggestSchwarmBoard.clone();
        BitBoard nachbarn = new BitBoard(0, 0);
        while (!biggestSchwarmBoardCl.equalsZero()) {
            int fisch = biggestSchwarmBoardCl.numberOfTrailingZeros();
            nachbarn.orEquals(BitBoardConstants.NACHBARN[fisch]);
            biggestSchwarmBoardCl.unsetBitEquals(fisch);
        }
        boolean isSafe = (meineFische - biggestSchwarmBoard.popCount()) * 2 + mg.pliesPlayed < 60;
        GameMove bestMove = null;
        double bestMoveScore = -1000000.0;
        for (int i = 0; i < gmro.instances; i++) {
            GameMove move = gmro.moves[i];
            if (biggestSchwarmBoard.and(BitBoardConstants.EINHEITS_UNIT_LEFT_SHIFT[move.from]).equalsZero() && !nachbarn.and(BitBoardConstants.EINHEITS_UNIT_LEFT_SHIFT[move.to]).equalsZero()) {
                double score = -quiescenseSearch(search, -beta, -alpha, gmro.states[i], -maximizingPlayer);
                if (score >= bestMoveScore) {
                    bestMoveScore = score;
                    bestMove = move;
                }
                if (score >= beta) {
                    int cacheIndex = (int) (mg.hash & Search.quiesenceCacheMask);
                    search.quiesenceCache[cacheIndex] = new QuiesenceCacheEntry(mg.hash, score, (byte) search.birthTimeQuiesence, bestMove, true, false, (byte) (mg.pliesPlayed), isSafe);
                    return beta;
                }
                if (score > alpha) {
                    alpha = score;
                }
            }
        }
        if (bestMove != null) {
            boolean alphaNode = (rat == alpha);
            int cacheIndex = (int) (mg.hash & Search.quiesenceCacheMask);
            if (search.quiesenceCache[cacheIndex] == null ||
                    search.quiesenceCache[cacheIndex].birth < search.birthTimeQuiesence && (!search.quiesenceCache[cacheIndex].isSafe || search.quiesenceCache[cacheIndex].pliesPlayed < mg.pliesPlayed)
                    || search.quiesenceCache[cacheIndex].pliesPlayed < mg.pliesPlayed) {
                search.quiesenceCache[cacheIndex] = new QuiesenceCacheEntry(mg.hash, alpha, (byte) (search.birthTimeQuiesence), bestMove, false, alphaNode, (byte) (mg.pliesPlayed), isSafe);
            }
        }
        return alpha;
    }

    //Rot ist 1, Blaue ist -1
    public static PrincipalVariation alphaBeta(Search search, MyGameState g, int depth, int maximizingPlayer,
                                               double alpha, double beta, int currentDepth) {


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
                double diff = BoardRating.getBiggestSchwarm(g, GameColor.RED) - BoardRating.getBiggestSchwarm(g, GameColor.BLUE);
                currPv.score = maximizingPlayer * (30000 - g.pliesPlayed + diff / 100.0);
                return currPv;
            } else {
                double diff = BoardRating.getBiggestSchwarm(g, GameColor.BLUE) - BoardRating.getBiggestSchwarm(g, GameColor.RED);
                currPv.score = maximizingPlayer * (-30000 + g.pliesPlayed - diff / 100.0);
                return currPv;
            }
        }
        if (depth == 0) {
            depth0Nodes++;
            if (g.pliesPlayed < 60 || true) {
                currPv.score = BoardRating.rating(g, AlphaBeta.brc) * maximizingPlayer;
            } else {
                currPv.score = quiescenseSearch(search, alpha, beta, g, maximizingPlayer);
            }
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
                int deltaBirth = search.birthTime - ce.birth;

                if (ce.depth >= depth && !(g.pliesPlayed + depth >= 60 && g.pliesPlayed - deltaBirth + ce.depth < 60)) {
                    if (!ce.betaNode && !ce.alphaNode) {

                        ce.birth = search.birthTime;
                        currPv.stack.add(ce.gm);
                        currPv.hashStack.add(ce.hash);
                        currPv.score = ce.score;
                        return currPv;
                    } else {
                        if (ce.betaNode) {
                            if (ce.score > alpha) {
                                alpha = ce.score;
                            }
                        } else {
                            if (ce.score < beta) {
                                beta = ce.score;
                            }
                        }
                    }
                }
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
                pvmoveFound = ce.pvNode && ce.birth == search.birthTime;
                GameMove atPos0 = gmro.moves[0];
                MyGameState atPos0S = gmro.states[0];
                gmro.moves[0] = gmro.moves[index];
                gmro.states[0] = gmro.states[index];
                gmro.moves[index] = atPos0;
                gmro.states[index] = atPos0S;


            }
        }
        //Make Null move
        if (nullmove && !pvmoveFound && depth >= 4 && currentDepth > 0 && depth + g.pliesPlayed < 60 && (g.move == GameColor.RED || BoardRating.getBiggestSchwarm(g, GameColor.RED) < g.roteFische.popCount())) {
            double rat = alphaBeta(search, GameLogic.makeNullMove(g), depth - 4, -maximizingPlayer, -beta, -beta + 0.0001, currentDepth + 1).score * -1;
            if (rat >= beta) {
                bestPv.score = rat;
                return bestPv;
            }
        }

        //Killer heuristic
        boolean found = false;
        for (int i = moveOrderingIndex; i < gmro.instances; i++) {
            GameMove move = gmro.moves[i];
            MyGameState nextGameState = gmro.states[i];
            if (search.killers[currentDepth][0] != null && search.killers[currentDepth][0].gm.equals(move)
                    || search.killers[currentDepth][1] != null && search.killers[currentDepth][1].gm.equals(move) ||
                    search.killers[currentDepth][2] != null && search.killers[currentDepth][2].gm.equals(move) ||
                    currentDepth >= 2 && (search.killers[currentDepth - 2][0] != null && search.killers[currentDepth - 2][0].gm.equals(move)
                            || search.killers[currentDepth - 2][1] != null && search.killers[currentDepth - 2][1].gm.equals(move)
                            || search.killers[currentDepth - 2][2] != null && search.killers[currentDepth - 2][2].gm.equals(move))) {
                found = true;
                GameMove atPosIndex = gmro.moves[moveOrderingIndex];
                MyGameState atPosIndexState = gmro.states[moveOrderingIndex];
                gmro.moves[moveOrderingIndex] = move;
                gmro.states[moveOrderingIndex] = nextGameState;
                gmro.moves[i] = atPosIndex;
                gmro.states[i] = atPosIndexState;
                moveOrderingIndex++;
            }
        }
        if (found) {
            killerMovesFound++;
        } else {
            noKillerMovesFound++;
        }

        //Captures
        /*int captureMoveIndex = moveOrderingIndex;
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
        }*/

        //History heuristic
        double[] ratings = new double[gmro.instances];
        for (int i = 0; i < gmro.instances; i++) {
            GameMove mv = gmro.moves[i];
            ratings[i] = search.historyHeuristic[mv.from][mv.to] / (search.bfHeuristic[mv.from][mv.to] + 0.0);
        }
        //Sort array
        int n = gmro.instances;
        for (int i = moveOrderingIndex; i < n - 1; i++) {
            for (int j = moveOrderingIndex; j < n - 1 - i; j++) {
                if (ratings[j] < ratings[j + 1]) {
                    //Swap
                    double curr = ratings[j];
                    ratings[j] = ratings[j + 1];
                    ratings[j + 1] = curr;
                    GameMove currM = gmro.moves[j];
                    MyGameState currMgs = gmro.states[j];
                    gmro.moves[j] = gmro.moves[j + 1];
                    gmro.states[j] = gmro.states[j + 1];
                    gmro.moves[j + 1] = currM;
                    gmro.states[j + 1] = currMgs;
                }
            }
        }

        int index = -1;
        for (int i = 0; i < gmro.instances; i++) {
            currPv.stack.add(gmro.moves[i]);
            currPv.hashStack.add(g.hash);
            PrincipalVariation followingPv;
            /*if (depth >= 3 && !pvmoveFound && i >= moveOrderingIndex && i >= gmro.instances / 2) {
                followingPv = alphaBeta(gmro.states[i], depth - 2, -maximizingPlayer, -beta, -alpha, currentDepth + 1);
                double rat = followingPv.score * -1;
                if (rat >= alpha) {
                    followingPv = alphaBeta(gmro.states[i], depth - 1, -maximizingPlayer, -beta, -alpha, currentDepth + 1);
                }
            } else */
            if (depth <= 2 || !pvmoveFound || i == 0) {
                followingPv = alphaBeta(search, gmro.states[i], depth - 1, -maximizingPlayer, -beta, -alpha, currentDepth + 1);
            } else {
                followingPv = alphaBeta(search, gmro.states[i], depth - 1, -maximizingPlayer, -alpha - 0.0001, -alpha, currentDepth + 1);
                double rat = followingPv.score * -1;
                if (rat >= alpha && rat <= beta) {
                    followingPv = alphaBeta(search, gmro.states[i], depth - 1, -maximizingPlayer, -beta, -alpha, currentDepth + 1);
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
                index = i;
            }
            if (alpha >= beta) {
                //Place in killer heuristic
                if (search.killers[currentDepth][0] == null) {
                    search.killers[currentDepth][0] = new KillerMove(gmro.moves[i]);
                } else if (search.killers[currentDepth][1] == null) {
                    search.killers[currentDepth][1] = new KillerMove(gmro.moves[i]);
                } else if (search.killers[currentDepth][2] == null) {
                    search.killers[currentDepth][2] = new KillerMove(gmro.moves[i]);
                } else {
                    if (!(search.killers[currentDepth][0].gm != null && search.killers[currentDepth][0].gm.equals(gmro.moves[i])) ||
                            search.killers[currentDepth][1].gm != null && search.killers[currentDepth][1].gm.equals(gmro.moves[i]) ||
                            search.killers[currentDepth][2].gm != null && search.killers[currentDepth][2].gm.equals(gmro.moves[i])) {

                        search.killers[currentDepth][search.lastKillerDeleted] = new KillerMove(gmro.moves[i]);
                        search.lastKillerDeleted += 1;
                        if (search.lastKillerDeleted == 3) {
                            search.lastKillerDeleted = 0;
                        }
                    }
                }
                GameMove moveFound = gmro.moves[i];
                search.historyHeuristic[moveFound.from][moveFound.to] += depth;
                break;
            } else {
                GameMove moveFound = gmro.moves[i];
                search.bfHeuristic[moveFound.from][moveFound.to] += depth;
            }
            currPv.stack.remove(currPv.stack.size() - 1);
            currPv.hashStack.remove(currPv.hashStack.size() - 1);
        }
        if (false && index != -1) {
            indexs[index] += 1;
        }
        //Make entry
        if (depth >= 1) {
            boolean betaNode = bestPv.score >= beta;
            boolean alphaNode = bestPv.score < alpha;

            bestPv.isBetaCutOff = betaNode;

            int cacheIndex = (int) (g.hash & Search.cacheMask);
            if (Search.cache[cacheIndex] == null) {
                Search.cache[cacheIndex] = new CacheEntry(g.hash, bestPv.score, search.birthTime, (byte) depth, bestPv.stack.get(0), false, betaNode, alphaNode);
            } else {
                CacheEntry ce = Search.cache[cacheIndex];
                if (!ce.pvNode && ce.depth - (search.birthTime - ce.birth) <= depth) {
                    Search.cache[cacheIndex] = new CacheEntry(g.hash, bestPv.score, search.birthTime, (byte) depth, bestPv.stack.get(0), false, betaNode, alphaNode);
                }
            }
        }
        return bestPv;

    }

    public static PrincipalVariation alphaBetaRoot(Search search, MyGameState g, int depth, int maximizingPlayer,
                                                   double alpha, double beta) {
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
                int deltaBirth = search.birthTime - ce.birth;
                if (ce.depth >= depth && !(g.pliesPlayed + depth >= 60 && g.pliesPlayed - deltaBirth + ce.depth < 60)) {
                    if (!ce.betaNode && !ce.alphaNode) {
                        ce.birth = search.birthTime;
                        pv.stack.add(ce.gm);
                        pv.hashStack.add(ce.hash);
                        pv.score = ce.score;
                        return pv;
                    } else {
                        if (ce.betaNode) {
                            if (ce.score > alpha) {
                                alpha = ce.score;
                            }
                        } else {
                            if (ce.score < beta) {
                                beta = ce.score;
                            }
                        }
                    }
                }
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
                pvmoveFound = ce.pvNode && ce.birth == search.birthTime;
                GameMove atPos0 = gmro.moves[0];
                MyGameState atPos0S = gmro.states[0];
                gmro.moves[0] = gmro.moves[index];
                gmro.states[0] = gmro.states[index];
                gmro.moves[index] = atPos0;
                gmro.states[index] = atPos0S;


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
                    currentPv = alphaBeta(search, gmro.states[i], depth - 1, -maximizingPlayer, -beta, -bestPv.score, 1);
                } else {
                    currentPv = alphaBeta(search, gmro.states[i], depth - 1, -maximizingPlayer, -beta, -alpha, 1);
                }
            } else {
                currentPv = alphaBeta(search, gmro.states[i], depth - 1, -maximizingPlayer, -bestPv.score - 0.0001, -bestPv.score, 1);
                double rat = currentPv.score * -1;
                if (rat >= alpha && rat <= beta) {
                    currentPv = alphaBeta(search, gmro.states[i], depth - 1, -maximizingPlayer, -beta, -alpha, 1);
                }
            }
            double rat = currentPv.score * -1;

            if (rat > bestPv.score) {
                bestPv = pv.clone();
                bestPv.score = rat;
                bestPv.stack.addAll(currentPv.stack);
                bestPv.hashStack.addAll(currentPv.hashStack);
            }
            if (bestPv.score > alpha) {
                alpha = bestPv.score;
            }
            if (alpha >= beta) {
                break;
            }
            pv.stack.remove(pv.stack.size() - 1);
            pv.hashStack.remove(pv.hashStack.size() - 1);
        }
        return bestPv;
    }


}
