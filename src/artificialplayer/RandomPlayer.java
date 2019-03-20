package artificialplayer;

public class RandomPlayer extends ArtificalPlayer {
    public static void main(String[] args) {
        ArtificalPlayer a = new RandomPlayer();
        a.main(args[0]);
    }

    @Override
    public PrincipalVariation requestMove(int time,byte birth) {
        this.mg.analyze();
        PrincipalVariation pv = new PrincipalVariation(1);
        pv.stack.add(this.mg.gmro.moves[(int) (this.mg.gmro.instances * Math.random())]);
        return pv;
    }
}
