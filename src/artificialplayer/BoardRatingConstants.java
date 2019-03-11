package artificialplayer;

import java.io.Serializable;

public class BoardRatingConstants implements Serializable {
    static final long serialVersionUID = 42L;

    public static int size = 21;
    Feature anzahlFische;
    Feature biggestSchwarmDurch16;
    Feature randFische;
    Feature centerFische;
    Feature schwarmRatio;
    Feature abstandZuMitte;
    Feature schwarmAbstand;
    Feature biggestSchwarmBonus;


    public BoardRatingConstants(double[] consts) {

        this.anzahlFische = new Feature(consts[0], consts[1], consts[2]);
        this.biggestSchwarmDurch16 = new Feature(consts[3], consts[4], consts[5]);
        this.randFische = new Feature(consts[6], consts[7], consts[8]);
        this.centerFische = new Feature(consts[9], consts[10], consts[11]);
        this.schwarmRatio = new Feature(consts[12], consts[13], consts[14]);
        this.abstandZuMitte = new Feature(consts[15], consts[16], consts[17]);
        this.schwarmAbstand = new Feature(consts[18], consts[19], consts[20]);
        this.biggestSchwarmBonus = new Feature(consts[21], consts[22], consts[23]);
    }
}
