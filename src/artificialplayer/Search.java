package artificialplayer;

import client.Logic;
import game.GameColor;
import game.MyGameState;


public class Search extends Thread {
    public PrincipalVariation currentBestPv;
    public static CacheEntry[] cache = new CacheEntry[4194304];
    //Power of 2
    public boolean stop = false;
    public static int mask = 4194304 - 1;
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
                cache[(int) (currentBestPv.hashStack.get(i) & Search.mask)] = new CacheEntry(currentBestPv.hashStack.get(i), currentBestPv.score, Search.birthTime,
                        (byte) (depth - i), currentBestPv.stack.get(i));
            }
            //
            //cache[(int) (this.mg.hash & Search.mask)] = new CacheEntry(mg.hash, currentBestPv.score, Search.birthTime, (byte) depth, currentBestPv.stack.get(0));
        }
    }
}
