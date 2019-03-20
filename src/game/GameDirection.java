package game;

import java.io.Serializable;

public enum GameDirection implements Serializable {
    //Only the primary 4 Directions are used
    //Therefore UP = -Down
    //UP_LEFT = -DOWN_RIGHT
    //LEFT = -RIGHT
    //DOWN_LEFT= -UP_RIGHT
    UP(10),UP_LEFT(11),LEFT(1),DOWN_LEFT(-9),DOWN(-10),DOWN_RIGHT(-11),RIGHT(-1),UP_RIGHT(9);

    private int shift;

    GameDirection(int shift){
        this.shift=shift;
    }

    public int getShift() {
        return shift;
    }
}
