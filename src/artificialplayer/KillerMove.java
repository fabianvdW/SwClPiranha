package artificialplayer;

import game.GameMove;

public class KillerMove {
    static int lastDeleted = 0;
    GameMove gm;

    public KillerMove(GameMove gm) {
        this.gm = gm;
    }
}
