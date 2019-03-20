package artificialplayer;

import game.GameMove;

public class QuiesenceCacheEntry {
    public long hash;//8 Bytes
    public double score;// 8 Bytes
    public byte birth; // 1Byte
    public GameMove gm; //12 Byte
    public boolean betaNode;
    public boolean alphaNode;
    public byte pliesPlayed;
    boolean isSafe;
    //28 Byte

    public QuiesenceCacheEntry(long hash, double score, byte birth, GameMove gm, boolean betaNode, boolean alphaNode, byte pliesPlayed, boolean isSafe) {
        this.hash = hash;
        this.score = score;
        this.birth = birth;
        this.gm = gm;
        this.betaNode = betaNode;
        this.alphaNode = alphaNode;
        this.pliesPlayed = pliesPlayed;
        this.isSafe = isSafe;
    }
}
