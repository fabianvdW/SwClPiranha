package evaltuning;

import artificialplayer.AlphaBeta;
import game.MyGameState;

import java.util.ArrayList;

public class GameHistory {
    ArrayList<MyGameState> history;
    boolean redWin;
    boolean blueWin;
    boolean draw;

    double distance = 0.0;

    public GameHistory(ArrayList<MyGameState> history, boolean redWin, boolean blueWin, boolean draw) {
        this.history = history;
        this.redWin = redWin;
        this.blueWin = blueWin;
        this.draw = draw;
    }

    public void calculateDistance() {
        ArrayList<LabeledGameState> lgs = new ArrayList<>(this.history.size());
        for (MyGameState mg : this.history) {
            lgs.add(new LabeledGameState(mg, redWin ? 1 : (blueWin ? 0 : 0.5)));
        }
        this.distance = TexelParser.evaluationError(lgs, AlphaBeta.brc);
    }
}
