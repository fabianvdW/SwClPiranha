package artificialplayer;

import game.GameMove;

import java.util.ArrayList;

public class PrincipalVariation {
    public ArrayList<GameMove> stack;
    public ArrayList<Long> hashStack;
    public double score;
    public int depthleft;
    public boolean isBetaCutOff;

    public PrincipalVariation(int depthleft) {
        this.depthleft = depthleft;
        stack = new ArrayList<>(depthleft);
        hashStack = new ArrayList<>(depthleft);
        this.score = -1000000.0;
    }

    public PrincipalVariation clone() {
        PrincipalVariation copy = new PrincipalVariation(this.depthleft);
        copy.stack = (ArrayList<GameMove>) this.stack.clone();
        copy.hashStack = (ArrayList<Long>) this.hashStack.clone();
        return copy;
    }
}
