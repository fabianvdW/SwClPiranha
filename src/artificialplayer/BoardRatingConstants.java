package artificialplayer;

import java.io.Serializable;

public class BoardRatingConstants implements Serializable {
    public static int size= 11;
    public double anzahlFischeMultiplier = 0.2;
    public double biggestSchwarmDurch16PowMultiplier = 2.0;
    public double fischAmRandMultiplier = -0.15;
    public double fischeImCenterMultiplier = 0.3;
    public double schwarmRatioMultiplier = 2.0;
    public double schwarmRatioM = 5.0;
    public double schwarmRatioB = -2.5;
    public double abstandZuMitteMultiplier = -0.25;
    public double abstandZuMitteVerschiebung = -3.1;
    public double normedAbstandZuBiggestSchwarmVerschiebung = -5.5;
    public double abstandZuBiggestMultiplier = -1.3;

    public BoardRatingConstants() {

    }

    public BoardRatingConstants(double[] consts) {
        this.anzahlFischeMultiplier = consts[0];
        this.biggestSchwarmDurch16PowMultiplier = consts[1];
        this.fischAmRandMultiplier = consts[2];
        this.fischeImCenterMultiplier = consts[3];
        this.schwarmRatioMultiplier = consts[4];
        this.schwarmRatioM = consts[5];
        this.schwarmRatioB = consts[6];
        this.abstandZuMitteMultiplier = consts[7];
        this.abstandZuMitteVerschiebung = consts[8];
        this.normedAbstandZuBiggestSchwarmVerschiebung = consts[9];
        this.abstandZuBiggestMultiplier = consts[10];
    }
}
