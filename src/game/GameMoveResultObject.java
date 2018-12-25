package game;

public class GameMoveResultObject {
    public GameMove[] moves;
    public GameState[] states;
    public int instances;
    public GameMoveResultObject(){
        this.moves= new GameMove[90];
        this.states=new GameState[90];
        instances=0;
    }
}
