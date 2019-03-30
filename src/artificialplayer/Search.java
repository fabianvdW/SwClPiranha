package artificialplayer;

import game.GameColor;
import game.GameMove;
import game.MyGameState;


public class Search extends Thread {
    public PrincipalVariation currentBestPv;
    public static CacheEntry[] cache = new CacheEntry[4 * 524288];
    public static int cacheMask = 4 * 524288 - 1;

    public QuiesenceCacheEntry[] quiesenceCache = new QuiesenceCacheEntry[2 * 524288];
    public static int quiesenceCacheMask = 2 * 524288 - 1;
    //Power of 2
    public KillerMove[][] killers;
    public int lastKillerDeleted = 0;
    public int[][] historyHeuristic;
    public int[][] bfHeuristic;
    public int depth;
    public boolean stop = false;
    public int birthTimeQuiesence = 0;
    public MyGameState mg;

    public Search(MyGameState mg, int depth) {
        this.mg = mg;
        this.depth = depth;
        killers = new KillerMove[100][3];
        historyHeuristic = new int[100][100];
        bfHeuristic = new int[100][100];
    }

    public void run() {
        killers = new KillerMove[100][3];
        for (int depth = 1; depth <= this.depth; depth++) {
            historyHeuristic = new int[100][100];
            bfHeuristic = new int[100][100];
            //AlphaBeta.nodesExamined = 0;
            //AlphaBeta.depth0Nodes = 0;
            //System.out.println("Depth: " + depth + " searched");
            PrincipalVariation pv = AlphaBeta.alphaBeta(true,this, this.mg, depth, mg.move == GameColor.RED ? 1 : -1, -100000, 100000, 0);
            if (stop) {
                break;
            }
            //Delete currentBestPv out of tt
            if (currentBestPv != null) {
                for (int i = currentBestPv.stack.size() - 1; i >= 0; i--) {
                    cache[(int) (currentBestPv.hashStack.get(i) & Search.cacheMask)].pvNode = false;
                }
            }

            currentBestPv = pv;
            //System.out.println("Depth: " + depth + " searched!");
            //System.out.println("Score: " + pv.score);
            for (int i = currentBestPv.stack.size() - 1; i >= 0; i--) {
                cache[(int) (currentBestPv.hashStack.get(i) & Search.cacheMask)] = new CacheEntry(currentBestPv.hashStack.get(i), currentBestPv.score * (i % 2 == 0 ? 1 : -1), (byte) (this.mg.pliesPlayed + i),
                        (byte) (depth - i), currentBestPv.stack.get(i), true, false, false);
            }
            birthTimeQuiesence++;
            //
            //cache[(int) (this.mg.hash & Search.cacheMask)] = new CacheEntry(mg.hash, currentBestPv.score, Search.birthTime, (byte) depth, currentBestPv.stack.get(0));
        }
    }

    public static void investigateCache() {
        int leer = 0;
        for (int i = 0; i < cache.length; i++) {
            if (cache[i] == null) {
                leer++;
            }
        }
        System.out.println("Cache: " + cache.length);
        System.out.println("Leer: " + leer);
        System.out.println((leer + 0.0) / cache.length);
    }
}
