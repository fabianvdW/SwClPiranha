package artificialplayer;

import datastructures.BitBoard;
import game.*;
import helpers.GlobalFlags;

import java.util.ArrayList;

public class BoardRating {
    public static double rating(MyGameState g) {
        //GameMoveResultObject currMove = g.gmro;
        //GameLogic.getPossibleMoves(g, g.move == GameColor.RED ? GameColor.BLUE : GameColor.RED);
        //GameMoveResultObject enemyMove = g.gmro;
        //GameMoveResultObject roteZuege = g.move == GameColor.RED ? currMove : enemyMove;
        //GameMoveResultObject blaueZuege = g.move == GameColor.BLUE ? currMove : enemyMove;
        BitBoard roteFische = g.roteFische;
        ArrayList<Schwarm> roteSchwaerme = berechneSchwaerme(g, GameColor.RED);
        //BitBoard rotKontrolliertesGebiet = berechneKontrolliertesGebiet(roteFische, g.kraken);
        BitBoard blaueFische = g.blaueFische;
        ArrayList<Schwarm> blaueSchwaerme = berechneSchwaerme(g, GameColor.BLUE);
        //BitBoard blauKontrolliertesGebiet = berechneKontrolliertesGebiet(blaueFische, g.kraken);
        return redEval(g.pliesPlayed, roteFische, roteSchwaerme, GameColor.RED) - redEval(g.pliesPlayed, blaueFische, blaueSchwaerme, GameColor.BLUE);
    }

    public static BitBoard berechneKontrolliertesGebiet(BitBoard fische, BitBoard kraken) {
        BitBoard res = new BitBoard(0, 0);
        BitBoard fClone = fische.clone();
        while (!fClone.equalsZero()) {
            int pos = fClone.numberOfTrailingZeros();
            fClone.unsetBitEquals(pos);
            res.orEquals(BitBoardConstants.NACHBARN[pos]);
        }
        res.andEquals(kraken.not());
        return res;
    }

    public static double redEval(int pliesPlayed, BitBoard fische, ArrayList<Schwarm> schwaerme, GameColor gc) {
        int anzahlFische = fische.popCount();
        for (Schwarm s : schwaerme) {
            s.calculateAverage();
        }
        Schwarm biggestSchwarm = schwaerme.get(0);
        int biggestSchwarmIndex = 0;
        int biggestSchwaermeFound = 1;
        double averageXBS = 0;
        double averageYBS = 0;
        for (int i = 1; i < schwaerme.size(); i++) {
            if (schwaerme.get(i).size > biggestSchwarm.size) {
                biggestSchwarm = schwaerme.get(i);
                biggestSchwarmIndex = i;
                biggestSchwaermeFound = 1;
                averageXBS = biggestSchwarm.averageX;
                averageYBS = biggestSchwarm.averageY;
            } else if (schwaerme.get(i).size == biggestSchwarm.size) {
                biggestSchwaermeFound++;
                averageXBS += schwaerme.get(i).averageX;
                averageYBS += schwaerme.get(i).averageY;
            }
        }
        double phase = (pliesPlayed + 1) / 61.0;
        //biggestSchwarm
        double fischEval = 0.2 * anzahlFische + Math.pow(2.0 * (biggestSchwarm.size + 0.0) / 16.0, 2);
        fischEval -= 0.15 * fische.and(BitBoardConstants.RAND).popCount();
        fischEval += 0.3 * fische.and(BitBoardConstants.CENTER).popCount();
        double ratio = (biggestSchwarm.size + 0.0) / anzahlFische;
        //fischEval = phase < 0.5 ? fischEval * 0.5 : fischEval * phase;
        double biggestSchwarmEval = phase * calculateBiggestSchwarmRatioBonus(ratio) * 2.0;

        double abstandZuMitteEval = 0.0;
        //Schwarm abhängig von Distanz zur Mitte bestrafen
        for (int i = 0; i < schwaerme.size(); i++) {
            double abstand = Math.sqrt(Math.pow(schwaerme.get(i).averageX - 4.5, 2) + Math.pow(schwaerme.get(i).averageY - 4.5, 2));
            abstandZuMitteEval += -1.0 / 4.0 * Math.pow((abstand - 3.1), 2) * Math.signum(abstand - 3.1) * schwaerme.get(i).size / (anzahlFische + 0.0);
        }
        if (phase > 0.5 && biggestSchwarm.size > 4) {
            biggestSchwarm.averageX = averageXBS / biggestSchwaermeFound;
            biggestSchwarm.averageY = averageYBS / biggestSchwaermeFound;
        }
        double abstandZuBiggestSchwarmEval = 0.0;
        for (int i = 0; i < schwaerme.size(); i++) {
            if (i == biggestSchwarmIndex) {
                continue;
            }
            double abstand = Math.max(Math.abs(biggestSchwarm.averageX - schwaerme.get(i).averageX), Math.abs(biggestSchwarm.averageY - schwaerme.get(i).averageY));
            //System.out.println(schwaerme.get(i).size);
            //System.out.println(abstand);
            double normedAbstand = abstand - 5.5;
            abstandZuBiggestSchwarmEval += (1 - phase) * -1.3 * Math.pow(normedAbstand, 2) * Math.signum(normedAbstand) * (schwaerme.get(i).size + 0.0) / (anzahlFische + 0.0);
            //abstandZuBiggestSchwarmEval -= (1 - phase) * 1.3 * Math.pow(abstand, 2) * 1 / 6.0 * (schwaerme.get(i).size + 0.0) / (anzahlFische + 0.0);
        }
        //Gegnergebiet Berechnungen
        //Bonus, wenn man Felder in gegnerischem Gebiet kontrolliert.
        double gegnerGebietEval = 0.0;
        //Kleiner Abzug, wenn man in gegnerischem Gebiet ist
        //gegnerGebietEval -= 0.1 * fische.and(gegnerGebiet).popCount();
        /*int treffer = gmro.attackBoard.and(gegnerGebiet).popCount();
        double anzahlGegnerFische = gegnerFische.popCount();
        gegnerGebietEval += 0.03 * treffer;
        if (treffer >= 1) {
            for (Schwarm s : gegnerSchwaerme) {
                if (!s.gebiet.and(gmro.attackBoard).equalsZero()) {
                    gegnerGebietEval += 4.0 * Math.pow((s.size + 0.0) / 16.0, 2);
                }
            }
        }
        //gegnerGebietEval = (phase + 0.2) * gegnerGebietEval;
        */
        if (GlobalFlags.VERBOSE) {
            System.out.println("Eval for " + gc);
            System.out.println("FischEval: " + fischEval);
            System.out.println("Biggest Schwarm: " + biggestSchwarmEval);
            System.out.println("Abstand zu BS: " + abstandZuBiggestSchwarmEval);
            System.out.println("Abstand zu Mitte: " + abstandZuMitteEval);
            System.out.println("Gegnergebiet: " + gegnerGebietEval);
            System.out.println("Insg: " + (biggestSchwarmEval + abstandZuBiggestSchwarmEval + abstandZuMitteEval + gegnerGebietEval + fischEval));
        }
        return biggestSchwarmEval + abstandZuBiggestSchwarmEval + abstandZuMitteEval + gegnerGebietEval + fischEval;
    }

    public static double calculateBiggestSchwarmRatioBonus(double ratio) {
        double res = 5 * ratio - 2.5;
        return Math.pow(res, 2) * Math.signum(res);
    }

    public static int getBiggestSchwarm(MyGameState g, GameColor gc) {
        BitBoard fischClone = gc == GameColor.RED ? g.roteFische.clone() : g.blaueFische.clone();
        int max = -1;
        while (!fischClone.equalsZero()) {
            BitBoard b = GameLogic.getSchwarmBoard(fischClone, gc);
            int count = b.popCount();
            if (count > max) {
                max = count;
            }
            fischClone.andEquals(b.not());
        }
        return max;
    }

    public static ArrayList<Schwarm> berechneSchwaerme(MyGameState g, GameColor gc) {
        BitBoard fischClone = gc == GameColor.RED ? g.roteFische.clone() : g.blaueFische.clone();
        ArrayList<Schwarm> res = new ArrayList<>();
        while (!fischClone.equalsZero()) {
            BitBoard b = GameLogic.getSchwarmBoard(fischClone, gc);
            fischClone.andEquals(b.not());
            res.add(toSchwarm(b));
        }
        return res;
    }

    public static Schwarm toSchwarm(BitBoard b) {
        Schwarm s = new Schwarm(0, new ArrayList<>(), b);
        while (!b.equalsZero()) {
            s.size++;
            s.positions.add(new Pos(b.numberOfTrailingZeros()));
            b.unsetBitEquals(b.numberOfTrailingZeros());
        }
        return s;
    }
}

class Schwarm {
    int size;
    ArrayList<Pos> positions;
    double averageX;
    double averageY;
    BitBoard gebiet;

    public Schwarm(int size, ArrayList<Pos> positions, BitBoard gebiet) {
        this.size = size;
        this.positions = positions;
        this.gebiet = gebiet.clone();
    }

    public void calculateAverage() {
        averageX = 0;
        averageY = 0;
        for (Pos pos : this.positions) {
            averageX += pos.x;
            averageY += pos.y;
        }
        averageX /= this.size + 0.0;
        averageY /= this.size + 0.0;
    }

}

class Pos {
    int x;
    int y;

    public Pos(int bit) {
        this.x = bit % 10;
        this.y = bit / 10;
    }
}
