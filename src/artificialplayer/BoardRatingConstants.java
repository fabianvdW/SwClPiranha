package artificialplayer;

import java.io.Serializable;

public class BoardRatingConstants implements Serializable {
    static final long serialVersionUID = 42L;

    public static int size = 21;
    Feature anzahlFische;
    Feature abstandZuMitte;
    Feature abstandZuBiggestSchwarm;
    Feature biggestSchwarm;
    Feature absolutSchwarm;
    Feature randFische;
    Feature abstandZuGegnerBiggestSchwarm;

    public BoardRatingConstants(double[] consts) {
        this.anzahlFische = new Feature(consts[0], consts[1], consts[2]);
        this.abstandZuMitte = new Feature(consts[3], consts[4], consts[5]);
        this.abstandZuBiggestSchwarm = new Feature(consts[6], consts[7], consts[8]);
        this.biggestSchwarm = new Feature(consts[9], consts[10], consts[11]);
        this.absolutSchwarm = new Feature(consts[12], consts[13], consts[14]);
        this.randFische = new Feature(consts[15], consts[16], consts[17]);
        this.abstandZuGegnerBiggestSchwarm= new Feature(consts[18],consts[19],consts[20]);
    }
}
