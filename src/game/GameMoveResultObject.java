package game;

import datastructures.BitBoard;

import java.io.Serializable;

public class GameMoveResultObject implements Serializable {
    static final long serialVersionUID = 42L;

    public BitBoard attackBoard;
    public GameMove[] moves;
    public MyGameState[] states;
    public int instances;

    public GameMoveResultObject() {
        this.attackBoard = new BitBoard(0, 0);
        this.moves = new GameMove[105];
        this.states = new MyGameState[105];
        instances = 0;
    }
}
