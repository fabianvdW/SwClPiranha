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
}
