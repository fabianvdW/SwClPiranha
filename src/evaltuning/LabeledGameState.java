package evaltuning;

import game.MyGameState;

import java.io.Serializable;

public class LabeledGameState implements Serializable {
    static final long serialVersionUID = 42L;
    MyGameState mg;
    double label;

    public LabeledGameState(MyGameState mg, double label) {
        this.mg = mg;
        this.label = label;
    }
}
