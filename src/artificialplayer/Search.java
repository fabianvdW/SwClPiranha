package artificialplayer;

import game.GameColor;
import game.MyGameState;


public class Search extends Thread {
    public PrincipalVariation currentBestPv;
    public static CacheEntry[] cache = new CacheEntry[1048576];
    public static int cacheMask = 1048576 - 1;
    //Power of 2
    public boolean stop = false;
    public static byte birthTime = 0;
    MyGameState mg;

    public Search(MyGameState mg) {
        this.mg = mg;
    }

    public void run() {
        for (int depth = 1; depth < 100; depth++) {
            AlphaBeta.nodesExamined = 0;
            AlphaBeta.depth0Nodes = 0;
            PrincipalVariation pv = AlphaBeta.alphaBetaRoot(this.mg, depth, mg.move == GameColor.RED ? 1 : -1);
            if (stop) {
                break;
            }
            currentBestPv = pv;
            //System.out.println("Depth: "+depth+" searched!");
            for (int i = currentBestPv.stack.size() - 1; i >= 0; i--) {
                cache[(int) (currentBestPv.hashStack.get(i) & Search.cacheMask)] = new CacheEntry(currentBestPv.hashStack.get(i), currentBestPv.score, Search.birthTime,
                        (byte) (depth - i), currentBestPv.stack.get(i));
            }
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
