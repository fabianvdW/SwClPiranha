package artificialplayer;

import game.GameMove;

public class CacheEntry {
    //20Bytes
    public long hash;
    public double score;
    public byte birth;
    public byte depth;
    public GameMove gm;

    public CacheEntry(long hash, double score, byte birth, byte depth, GameMove gm) {
        this.hash = hash;
        this.score = score;
        this.birth = birth;
        this.depth = depth;
        this.gm = gm;
    }
}
