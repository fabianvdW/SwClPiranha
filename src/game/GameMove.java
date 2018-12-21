package game;

public class GameMove {
    public int from;
    public int to;
    public GameDirection dir;

    public GameMove(int from, int to, GameDirection dir) {
        this.from = from;
        this.to = to;
        this.dir = dir;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("("+(9-from%10)+","+(9-from/10)+") -->"+"("+(9-to%10)+","+(9-to/10)+")\n");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof GameMove){
            GameMove g= (GameMove) o;
            return g.from==this.from&&g.to==this.to&&g.dir==this.dir;
        }
        return false;
    }

    @Override
    public int hashCode(){
        return this.from+this.to*100;
    }
}
