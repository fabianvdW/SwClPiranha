package artificialplayer;

import client.Logic;
import datastructures.BitBoard;
import evaltuning.Genome;
import game.*;
import helpers.FEN;
import testing.Fix;

public class AlphaBeta extends ArtificalPlayer {
    public static int nodesExamined;
    public static int depth0Nodes;
    //public static double[] gaDna = {0.9025510163556673, 2.657765703271754, -0.6639782702608146, 4.614780818192207, 1.2196253480425627, 5.049480353040995, 0.38931830719247384, -2.081605428423991, -2.3353965528221905, -5.049202306669282, -2.727337976086494};
    //public static double[] gaDna = {0.11574546378222712, -0.07257960137971395, -0.02589816978425911, 0.7361787925306954, 0.20392160516771846, 7.205857172121344, -1.8707970159460117, -0.1599954301371371, -0.1253664821091645, -9.571692778767243, -0.046159350880481065};
    //public static double[] gaDna = {0.8852835659011121, -0.19056148413398566, -0.2751304122215384, 0.8801229550653944, -0.05722176773584106, 0.05000721632742493, 0.8250244175932502, -0.4031217265653704, -0.3272060761468655, 0.9162996886969947, 0.3070632221889533, 0.2689495522580865, -0.4817318710600673, 0.5532767967852428, 0.9026973039998654, -2.102267087470157, -0.8720734113523638, -0.7064984701028318, -1.0885779449064283, -1.1311317901443596, -0.8548301880079731};
    public static double[] gaDna = {1.1929413659945325, -0.8731702020117803, -0.18857558152186485, -0.28516712873434763, 0.2632768554065141, 0.8993662608977158, 0.13348138971818577, -0.22333247871946682, -0.4039322894096489, 0.26511382721538596, 0.1665940335462095, 0.8675391276945661, 1.3508070853980805, 0.9575173529821485, 1.4240074329668702, -0.20112452911349518, -0.49828852243518695, -0.10684710598696326, -0.9626061894702808, -0.19868752897198622, 1.6581937255059032};
    //public static double[] gaDna = {1.2574005844383807, -0.6738170355996104, 0.10734008736744978, -0.28516712873434763, 0.2632768554065141, -0.24528170922148942, -0.055627017080429865, -0.32148229581442894, -0.32430421340082316, 0.26511382721538596, 0.17652304814498984, 1.174721988436177, 1.095740436984743, 0.7951677218766458, 1.4240074329668702, -0.7247895710080169, -0.49828852243518695, 0.16002337645230139, -0.9909624628580254, -0.19868752897198622, 1.5166589656238028};

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
                currPv.score = maximizingPlayer * (30000 - g.pliesPlayed);
                return currPv;
            } else {
                currPv.score = maximizingPlayer * (-30000 + g.pliesPlayed);
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
                int deltaBirth = Search.birthTime - ce.birth;

                if (ce.depth >= depth && !(g.pliesPlayed + depth >= 60 && g.pliesPlayed - deltaBirth + ce.depth < 60)) {
                    if (!ce.betaNode && !ce.alphaNode) {
                        ce.birth = Search.birthTime;
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
                pvmoveFound = ce.pvNode && ce.birth == Search.birthTime;
                GameMove atPos0 = gmro.moves[0];
                MyGameState atPos0S = gmro.states[0];
                gmro.moves[0] = gmro.moves[index];
                gmro.states[0] = gmro.states[index];
                gmro.moves[index] = atPos0;
                gmro.states[index] = atPos0S;


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
            if (alpha >= beta) {
                break;
            }
            currPv.stack.remove(currPv.stack.size() - 1);
            currPv.hashStack.remove(currPv.hashStack.size() - 1);
        }
        //Make entry
        if (depth >= 1) {
            boolean betaNode = bestPv.score > beta;
            boolean alphaNode = bestPv.score < alpha;

            bestPv.isBetaCutOff = betaNode;
            int cacheIndex = (int) (g.hash & Search.cacheMask);
            if (Search.cache[cacheIndex] == null) {
                Search.cache[cacheIndex] = new CacheEntry(g.hash, bestPv.score, Search.birthTime, (byte) depth, bestPv.stack.get(0), false, betaNode, alphaNode);
            } else {
                CacheEntry ce = Search.cache[cacheIndex];
                if (!ce.pvNode && ce.depth - (Search.birthTime - ce.birth) <= depth) {
                    Search.cache[cacheIndex] = new CacheEntry(g.hash, bestPv.score, Search.birthTime, (byte) depth, bestPv.stack.get(0), false, betaNode, alphaNode);
                }
            }
        }
        return bestPv;

    }

    public static PrincipalVariation alphaBetaRoot(MyGameState g, int depth, int maximizingPlayer, double alpha, double beta) {
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
                int deltaBirth = Search.birthTime - ce.birth;
                if (ce.depth >= depth && !(g.pliesPlayed + depth >= 60 && g.pliesPlayed - deltaBirth + ce.depth < 60)) {
                    if (!ce.betaNode && !ce.alphaNode) {
                        ce.birth = Search.birthTime;
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
                pvmoveFound = ce.pvNode && ce.birth == Search.birthTime;
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
                    currentPv = alphaBeta(gmro.states[i], depth - 1, -maximizingPlayer, -beta, -bestPv.score);
                } else {
                    currentPv = alphaBeta(gmro.states[i], depth - 1, -maximizingPlayer, -beta, -alpha);
                }
            } else {
                currentPv = alphaBeta(gmro.states[i], depth - 1, -maximizingPlayer, -bestPv.score - 1, -bestPv.score);
                double rat = currentPv.score * -1;
                if (rat >= bestPv.score) {
                    currentPv = alphaBeta(gmro.states[i], depth - 1, -maximizingPlayer, -beta, -alpha);
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
