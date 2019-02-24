package game;

import datastructures.BitBoard;

public class GameMoveResultObject {
    public BitBoard attackBoard;
    public GameMove[] moves;
    public MyGameState[] states;
    public int instances;

    public GameMoveResultObject() {
        this.attackBoard = new BitBoard(0, 0);
        this.moves = new GameMove[95];
        this.states = new MyGameState[95];
        instances = 0;
    }
}
