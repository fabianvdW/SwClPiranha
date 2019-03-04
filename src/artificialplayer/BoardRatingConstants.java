package artificialplayer;

import java.io.Serializable;

public class BoardRatingConstants implements Serializable {
    public static int size = 42;
    Feature anzahlFische;
    Feature biggestSchwarmDurch16;
    Feature randFische;
    Feature centerFische;
    Feature schwarmRatio;
    Feature abstandZuMitte;
    Feature schwarmAbstand;


    public BoardRatingConstants(double[] consts) {

        this.anzahlFische = new Feature(consts[0], consts[1], consts[2], consts[3], consts[4], consts[5]);
        this.biggestSchwarmDurch16 = new Feature(consts[6], consts[7], consts[8], consts[9], consts[10], consts[11]);
        this.randFische = new Feature(consts[12], consts[13], consts[14], consts[15], consts[16], consts[17]);
        this.centerFische = new Feature(consts[18], consts[19], consts[20], consts[21], consts[22], consts[23]);
        this.schwarmRatio = new Feature(consts[24], consts[25], consts[26], consts[27], consts[28], consts[29]);
        this.abstandZuMitte = new Feature(consts[30], consts[31], consts[32], consts[33], consts[34], consts[35]);
        this.schwarmAbstand = new Feature(consts[36], consts[37], consts[38], consts[39], consts[40], consts[41]);
    }
}
