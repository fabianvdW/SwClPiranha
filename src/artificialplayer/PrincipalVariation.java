package artificialplayer;

import game.GameMove;

import java.util.ArrayList;

public class PrincipalVariation {
    public ArrayList<GameMove> stack;
    public double score;
    int depthleft;

    public PrincipalVariation(int depthleft) {
        this.depthleft = depthleft;
        stack = new ArrayList<>(depthleft);
        this.score = -10000.0;
    }

    public PrincipalVariation clone() {
        PrincipalVariation copy = new PrincipalVariation(this.depthleft);
        copy.stack = (ArrayList<GameMove>) this.stack.clone();
        return copy;
    }
}
