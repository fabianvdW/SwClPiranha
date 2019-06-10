package artificialplayer;

import datastructures.BitBoard;
import evaltuning.TexelParser;
import evaltuning.TexelTuning;
import game.*;
import helpers.GlobalFlags;

import java.util.ArrayList;

public class BoardRating {
    public final static double MAX_DIST = 6.36396103068;

    public static double rating(MyGameState g, BoardRatingConstants brc) {
        BitBoard roteFische = g.roteFische;
        ArrayList<Schwarm> roteSchwaerme = berechneSchwaerme(g.roteFische);
        for (Schwarm s : roteSchwaerme) {
            s.calculateAverage();
        }
        BitBoard blaueFische = g.blaueFische;
        ArrayList<Schwarm> blaueSchwaerme = berechneSchwaerme(g.blaueFische);
        for (Schwarm s : blaueSchwaerme) {
            s.calculateAverage();
        }

        BiggestSchwarmObject bsoRot = getBiggestSchwarm(roteSchwaerme);
        BiggestSchwarmObject bsoBlau = getBiggestSchwarm(blaueSchwaerme);
        return redEval(g.pliesPlayed, roteFische, roteSchwaerme, GameColor.RED, brc, bsoRot,bsoBlau,blaueFische.popCount()) - redEval(g.pliesPlayed, blaueFische, blaueSchwaerme, GameColor.BLUE, brc, bsoBlau, bsoRot,roteFische.popCount());
    }

    public static BiggestSchwarmObject getBiggestSchwarm(ArrayList<Schwarm> schwaerme) {
        Schwarm biggestSchwarm = schwaerme.get(0);
        Pos spielFeldMitte = new Pos(4.5, 4.5);
        double dist = 0;
        for (int i = 1; i < schwaerme.size(); i++) {
            if (schwaerme.get(i).size > biggestSchwarm.size) {
                biggestSchwarm = schwaerme.get(i);
                dist = spielFeldMitte.distance(new Pos(biggestSchwarm.averageX, biggestSchwarm.averageY));
            } else if (schwaerme.get(i).size == biggestSchwarm.size) {
                double dist2 = spielFeldMitte.distance(new Pos(biggestSchwarm.averageX, biggestSchwarm.averageY));
                if (dist2 < dist) {
                    biggestSchwarm = schwaerme.get(i);
                    dist = dist2;
                }
            }
        }
        return new BiggestSchwarmObject(biggestSchwarm);
    }

    public static double redEval(int pliesPlayed, BitBoard fische, ArrayList<Schwarm> schwaerme, GameColor gc, BoardRatingConstants brc, BiggestSchwarmObject bso, BiggestSchwarmObject gegnerSchwarm,int gegnerFische) {
        double unSkewedPhase = (pliesPlayed + 0.0) / 60.0;
        double phase = 1 - Math.pow(1 - unSkewedPhase, 2);

        double fischEval = 0;
        double abstandZuMitteEval = 0;
        double abstandZuBiggestSchwarm = 0;
        double biggestSchwarmEval = 0;
        double absoluteSchwarmEval = 0;
        double randFische = 0;
        double abstandZuGegnerBiggestSchwarm=0;

        int fischAnzahl = fische.popCount();
        UsedFeature fischAnzahlUsed = new UsedFeature(fischAnzahl, brc.anzahlFische, phase);
        fischEval = fischAnzahlUsed.value;

        if (fischAnzahl == 2) {
            fischEval += 20;
        } else if (fischAnzahl == 3) {
            fischEval += 10;
        } else if (fischAnzahl == 4) {
            fischEval += 5;
        }

        Pos spielFeldMitte = new Pos(4.5, 4.5);
        Pos gegnerSchwarmPos= new Pos(gegnerSchwarm.biggestSchwarm.averageX,gegnerSchwarm.biggestSchwarm.averageY);
        double cummulatedInput = 0.0;
        double cummulatedInputGegner=0.0;
        for (Schwarm s : schwaerme) {
            double dist = spielFeldMitte.distance(new Pos(s.averageX, s.averageY));
            cummulatedInput += Math.pow(dist / MAX_DIST, 2) * s.size;
            double dist2= gegnerSchwarmPos.distance(new Pos(s.averageX,s.averageY));
            cummulatedInputGegner+=Math.pow(dist2/MAX_DIST, 2)*s.size;
        }
        cummulatedInput /= fischAnzahl + 0.0;
        cummulatedInputGegner/=fischAnzahl+0.0;
        cummulatedInputGegner*=Math.pow(gegnerSchwarm.biggestSchwarm.size/(gegnerFische+0.0),3);
        UsedFeature abstandZuGegnerSchwarmUsed= new UsedFeature(cummulatedInputGegner, brc.abstandZuGegnerBiggestSchwarm, phase);
        abstandZuGegnerBiggestSchwarm=abstandZuGegnerSchwarmUsed.value;
        if (bso.biggestSchwarm.size / (fischAnzahl + 0.0) > 0.5) {
            double dist = spielFeldMitte.distance(new Pos(bso.biggestSchwarm.averageX, bso.biggestSchwarm.averageY));
            cummulatedInput += Math.pow(dist / MAX_DIST, 2);
        }

        UsedFeature abstandZuMitteUsed = new UsedFeature(cummulatedInput, brc.abstandZuMitte, phase);
        abstandZuMitteEval = abstandZuMitteUsed.value;

        Pos biggestSchwarmCoordinates = new Pos(bso.biggestSchwarm.averageX, bso.biggestSchwarm.averageY);
        cummulatedInput = 0.0;
        if (bso.biggestSchwarm.size < fischAnzahl) {
            int f = fischAnzahl - bso.biggestSchwarm.size;
            for (Schwarm s : schwaerme) {
                if (s != bso.biggestSchwarm) {
                    double dist = biggestSchwarmCoordinates.distance(new Pos(s.averageX, s.averageY));
                    cummulatedInput += Math.pow(dist / MAX_DIST, 2) * s.size;
                }

            }
            cummulatedInput /= f + 0.0;
        }
        UsedFeature abstandZuBiggestSchwarmUsed = new UsedFeature(cummulatedInput, brc.abstandZuBiggestSchwarm, phase);
        abstandZuBiggestSchwarm = abstandZuBiggestSchwarmUsed.value;

        double ratio = bso.biggestSchwarm.size / (fischAnzahl + 0.0);
        cummulatedInput = Math.pow(ratio, 2);
        if (ratio > 0.5) {
            int uselessFische = fischAnzahl - bso.biggestSchwarm.size;
            cummulatedInput *= 1 + bso.biggestSchwarm.calculateSichereFische() / (bso.biggestSchwarm.size + 0.0); //- 8 * Math.pow(gegnerRatio, 3) * uselessFische / (fischAnzahl + 0.0);
        }
        UsedFeature biggestSchwarmUsed = new UsedFeature(cummulatedInput, brc.biggestSchwarm, phase);
        biggestSchwarmEval = biggestSchwarmUsed.value;


        UsedFeature absoluteSchwarmUsed = new UsedFeature(Math.pow(bso.biggestSchwarm.size / 16.0 + 0.5, 2), brc.absolutSchwarm, Math.pow(unSkewedPhase, 3));
        absoluteSchwarmEval = absoluteSchwarmUsed.value;

        UsedFeature randFischeUsed = new UsedFeature(fische.and(BitBoardConstants.RAND).popCount(), brc.randFische, phase);
        randFische = randFischeUsed.value;

        if (GlobalFlags.VERBOSE) {
            System.out.println("Phase: " + phase);
            System.out.println("Eval for " + gc);
            System.out.println("FischEval: " + fischEval);
            System.out.println("Abstand zu Mitte: " + abstandZuMitteEval);
            System.out.println("Abstand zu BS: " + abstandZuBiggestSchwarm);
            System.out.println("Biggest Schwarm: " + biggestSchwarmEval);
            System.out.println("Maximal Schwarm Size: " + absoluteSchwarmEval);
            System.out.println("Rand Fische: " + randFische);
            System.out.println("Abstand zu Gegner Schwarm: "+abstandZuGegnerBiggestSchwarm);
            //System.out.println("Insg: " + (biggestSchwarmEval + abstandZuBiggestSchwarmEval + abstandZuMitteEval + biggestSchwarmBonusEval + fischEval));
        }
        if (GlobalFlags.TEXEL_TUNING) {
            TexelParser.lastEval[0] = fischAnzahlUsed;
            TexelParser.lastEval[1] = abstandZuMitteUsed;
            TexelParser.lastEval[2] = abstandZuBiggestSchwarmUsed;
            TexelParser.lastEval[3] = biggestSchwarmUsed;
            TexelParser.lastEval[4] = absoluteSchwarmUsed;
            TexelParser.lastEval[5] = randFischeUsed;
        }
        //Center-Fische-Feature
        return abstandZuMitteEval + fischEval + abstandZuBiggestSchwarm + biggestSchwarmEval + absoluteSchwarmEval + randFische+abstandZuGegnerBiggestSchwarm;
    }

    public static int getBiggestSchwarm(MyGameState g, GameColor gc) {
        BitBoard fischClone = gc == GameColor.RED ? g.roteFische.clone() : g.blaueFische.clone();
        int max = -1;
        while (!fischClone.equalsZero()) {
            BitBoard b = GameLogic.getSchwarmBoard(fischClone);
            int count = b.popCount();
            if (count > max) {
                max = count;
            }
            fischClone.andEquals(b.not());
        }
        return max;
    }

    public static BitBoard getBiggestSchwarmBoard(MyGameState g, GameColor gc) {
        BitBoard fischClone = gc == GameColor.RED ? g.roteFische.clone() : g.blaueFische.clone();
        int max = -1;
        BitBoard maxBb = null;
        while (!fischClone.equalsZero()) {
            BitBoard b = GameLogic.getSchwarmBoard(fischClone);
            int count = b.popCount();
            if (count > max) {
                max = count;
                maxBb = b;

            }
            fischClone.andEquals(b.not());
        }
        return maxBb;
    }

    public static ArrayList<Schwarm> berechneSchwaerme(BitBoard fische) {
        BitBoard fischClone = fische.clone();
        ArrayList<Schwarm> res = new ArrayList<>(8);
        while (!fischClone.equalsZero()) {
            BitBoard b = GameLogic.getSchwarmBoard(fischClone);
            fischClone.andEquals(b.not());
            res.add(toSchwarm(b));
        }
        return res;
    }

    public static Schwarm toSchwarm(BitBoard b) {
        Schwarm s = new Schwarm(0, b.clone());
        while (!b.equalsZero()) {
            s.size++;
            int i = b.numberOfTrailingZeros();
            s.averageX += (i % 10);
            s.averageY += (i / 10);
            b.unsetBitEquals(i);
        }
        return s;
    }
}

class BiggestSchwarmObject {
    Schwarm biggestSchwarm;

    public BiggestSchwarmObject(Schwarm biggestSchwarm) {
        this.biggestSchwarm = biggestSchwarm;
    }
}

class Schwarm {
    BitBoard gebiet;
    int size;
    double averageX;
    double averageY;

    public Schwarm(int size, BitBoard gebiet) {
        this.size = size;
        this.gebiet = gebiet;
    }

    public void calculateAverage() {
        //averageX = 0;
        //averageY = 0;
        averageX /= this.size + 0.0;
        averageY /= this.size + 0.0;
    }


    public double calculateSichereFische() {
        double res = 0;
        BitBoard fischIt = this.gebiet.clone();
        while (!fischIt.equalsZero()) {
            int fisch = fischIt.numberOfTrailingZeros();
            BitBoard ohneFische = this.gebiet.and(BitBoardConstants.EINHEITS_UNIT_LEFT_SHIFT_NOT[fisch]);
            int x = GameLogic.getSchwarmBoard(ohneFische).popCount();
            if (x == this.size - 1) {
                res += 1;
            }
            fischIt.unsetBitEquals(fisch);
        }
        return res;
    }


}

class Pos {
    double x;
    double y;

    public Pos(int bit) {
        this.x = bit % 10;
        this.y = bit / 10;
    }

    public Pos(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double distance(Pos p2) {
        return Math.sqrt(Math.pow(p2.x - x, 2) + Math.pow(p2.y - y, 2));
    }
}
