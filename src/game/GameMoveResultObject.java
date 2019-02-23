package game;

public class GameMoveResultObject {
    public GameMove[] moves;
    public MyGameState[] states;
    public int instances;

    public GameMoveResultObject() {
        this.moves = new GameMove[95];
        this.states = new MyGameState[95];
        instances = 0;
    }
}
