package artificialplayer;

import game.GameMove;

public class CacheEntry {
    public long hash;//8 Bytes
    public double score;// 8 Bytes
    public byte birth; // 1Byte
    public byte depth;//1 Byte
    public GameMove gm; //12 Byte
    public boolean pvNode;
    //28 Byte

    public CacheEntry(long hash, double score, byte birth, byte depth, GameMove gm, boolean pvNode) {
        this.hash = hash;
        this.score = score;
        this.birth = birth;
        this.depth = depth;
        this.gm = gm;
        this.pvNode = pvNode;
    }
}
