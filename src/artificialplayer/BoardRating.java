package artificialplayer;

import datastructures.BitBoard;
import game.GameColor;
import game.GameLogic;
import game.MyGameState;

import java.util.ArrayList;

public class BoardRating {
    public static double rating(MyGameState g) {
        return redEval(g) - redEval(new MyGameState(g.blaueFische, g.roteFische, g.kraken, g.move, g.pliesPlayed, g.roundsPlayed));
    }

    public static double redEval(MyGameState g) {
        double eval = 0.0;
        int pliesPlayed = g.pliesPlayed;
        int anzahlRoteFische = g.roteFische.popCount();
        ArrayList<Schwarm> roteSchwaerme = berechneSchwaerme(g, GameColor.RED);
        for (Schwarm s : roteSchwaerme) {
            s.calculateAverage();
        }
        Schwarm biggestSchwarm = roteSchwaerme.get(0);
        int biggestSchwarmIndex = 0;
        for (int i = 1; i < roteSchwaerme.size(); i++) {
            if (roteSchwaerme.get(i).size > biggestSchwarm.size) {
                biggestSchwarm = roteSchwaerme.get(i);
                biggestSchwarmIndex = i;
            }
        }
        double ratio = (biggestSchwarm.size + 0.0) / anzahlRoteFische;
        double phase = (pliesPlayed + 1) / 61.0;
        eval += phase * calculateBiggestSchwarmRatioBonus(ratio);
        for (int i = 0; i < roteSchwaerme.size(); i++) {
            if (i == biggestSchwarmIndex) {
                continue;
            }
            double abstand = Math.max(Math.abs(biggestSchwarm.averageX - roteSchwaerme.get(i).averageX), Math.abs(biggestSchwarm.averageY - roteSchwaerme.get(i).averageY));
            double normedAbstand = abstand - 5.5;
            eval += (1 - phase) * -1 * Math.pow(normedAbstand, 2) * Math.signum(normedAbstand) * (roteSchwaerme.get(i).size + 0.0) / (anzahlRoteFische + 0.0);
        }
        //Schwarm abhängig von Distanz zur Mitte bestrafen
        for (int i = 0; i < roteSchwaerme.size(); i++) {
            double abstand = Math.sqrt(Math.pow(roteSchwaerme.get(i).averageX - 4.5, 2) + Math.pow(roteSchwaerme.get(i).averageY - 4.5, 2));
            eval += -1.0 / 3.0 * Math.pow((abstand - 3.1), 2) * Math.signum(abstand - 3.1) * roteSchwaerme.get(i).size / (anzahlRoteFische + 0.0);
        }
        return eval;
    }

    public static double calculateBiggestSchwarmRatioBonus(double ratio) {
        double res = 5 * ratio - 2.5;
        return Math.pow(res, 2) * Math.signum(res);
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
        Schwarm s = new Schwarm(0, new ArrayList<>());
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

    public Schwarm(int size, ArrayList<Pos> positions) {
        this.size = size;
        this.positions = positions;

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
