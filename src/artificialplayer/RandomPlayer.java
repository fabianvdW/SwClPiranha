package artificialplayer;

import game.GameMove;

public class RandomPlayer extends ArtificalPlayer {
    public static void main(String[] args) {
        ArtificalPlayer a = new RandomPlayer();
        a.main(args[0]);
    }

    @Override
    public GameMove requestMove() {
        this.mg.analyze();
        return this.mg.gmro.moves[(int) (this.mg.gmro.instances * Math.random())];
    }
}
