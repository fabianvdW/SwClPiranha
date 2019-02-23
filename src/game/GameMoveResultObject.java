package game;

public class GameMoveResultObject {
    public GameMove[] moves;
    public MyGameState[] states;
    public int instances;
    public GameMoveResultObject(){
        this.moves= new GameMove[90];
        this.states=new MyGameState[90];
        instances=0;
    }
}
