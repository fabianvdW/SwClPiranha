package artificialplayer;

import datastructures.BitBoard;
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
        return redEval(g.pliesPlayed, roteFische, roteSchwaerme, GameColor.RED, brc, bsoRot, blaueFische, (bsoBlau.biggestSchwarm.size / (blaueFische.popCount() + 0.0))) - redEval(g.pliesPlayed, blaueFische, blaueSchwaerme, GameColor.BLUE, brc, bsoBlau, roteFische, bsoRot.biggestSchwarm.size / (roteFische.popCount() + 0.0));
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

    public static double redEval(int pliesPlayed, BitBoard fische, ArrayList<Schwarm> schwaerme, GameColor gc, BoardRatingConstants brc, BiggestSchwarmObject bso, BitBoard gegnerFische, double gegnerRatio) {
        double unSkewedPhase = (pliesPlayed + 0.0) / 60.0;
        double phase = 1 - Math.pow(1 - unSkewedPhase, 2);

        double fischEval = 0;
        double abstandZuMitteEval = 0;
        double abstandZuBiggestSchwarm = 0;
        double biggestSchwarmEval = 0;
        double absoluteSchwarmEval = 0;
        double randFische = 0;

        int fischAnzahl = fische.popCount();
        fischEval = brc.anzahlFische.getOutput(fischAnzahl, phase);

        Pos spielFeldMitte = new Pos(4.5, 4.5);
        for (Schwarm s : schwaerme) {
            double dist = spielFeldMitte.distance(new Pos(s.averageX, s.averageY));
            abstandZuMitteEval += brc.abstandZuMitte.getOutput(Math.pow(dist / MAX_DIST, 2), phase) * s.size;
        }
        abstandZuMitteEval /= fischAnzahl + 0.0;

        if (bso.biggestSchwarm.size / (fischAnzahl + 0.0) > 0.5) {
            double dist = spielFeldMitte.distance(new Pos(bso.biggestSchwarm.averageX, bso.biggestSchwarm.averageY));
            abstandZuMitteEval += brc.abstandZuMitte.getOutput(Math.pow(dist / MAX_DIST, 2), phase);
        }

        Pos biggestSchwarmCoordinates = new Pos(bso.biggestSchwarm.averageX, bso.biggestSchwarm.averageY);
        if (bso.biggestSchwarm.size < fischAnzahl) {
            int f = fischAnzahl - bso.biggestSchwarm.size;
            for (Schwarm s : schwaerme) {
                if (s != bso.biggestSchwarm) {
                    double dist = biggestSchwarmCoordinates.distance(new Pos(s.averageX, s.averageY));
                    abstandZuBiggestSchwarm += brc.abstandZuBiggestSchwarm.getOutput(Math.pow(dist / MAX_DIST, 2), phase) * s.size;
                }

            }
            abstandZuBiggestSchwarm /= f + 0.0;
        }

        double ratio = bso.biggestSchwarm.size / (fischAnzahl + 0.0);
        biggestSchwarmEval = brc.biggestSchwarm.getOutput(Math.pow(ratio, 2), phase);
        if (ratio > 0.5) {
            int uselessFische=fischAnzahl-bso.biggestSchwarm.size;
            biggestSchwarmEval *= 1 + bso.biggestSchwarm.calculateSichereFische() / (bso.biggestSchwarm.size + 0.0)-8*Math.pow(gegnerRatio, 3)*uselessFische/(fischAnzahl+0.0);
        }

        absoluteSchwarmEval = brc.absolutSchwarm.getOutput(Math.pow(bso.biggestSchwarm.size / 16.0 + 0.5, 2), Math.pow(unSkewedPhase, 3));

        randFische = brc.randFische.getOutput(fische.and(BitBoardConstants.RAND).popCount(), phase);


        if (GlobalFlags.VERBOSE) {
            System.out.println("Phase: " + phase);
            System.out.println("Eval for " + gc);
            System.out.println("FischEval: " + fischEval);
            System.out.println("Abstand zu Mitte: " + abstandZuMitteEval);
            System.out.println("Abstand zu BS: " + abstandZuBiggestSchwarm);
            System.out.println("Biggest Schwarm: " + biggestSchwarmEval);
            System.out.println("Maximal Schwarm Size: " + absoluteSchwarmEval);
            System.out.println("Rand Fische: " + randFische);
            //System.out.println("Insg: " + (biggestSchwarmEval + abstandZuBiggestSchwarmEval + abstandZuMitteEval + biggestSchwarmBonusEval + fischEval));
        }
        return abstandZuMitteEval + fischEval + abstandZuBiggestSchwarm + biggestSchwarmEval + absoluteSchwarmEval + randFische;
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
