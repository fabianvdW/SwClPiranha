package artificialplayer;

import datastructures.BitBoard;
import game.BitBoardConstants;
import game.GameColor;
import game.GameLogic;
import game.GameState;

import java.util.ArrayList;

public class BoardRating {

    public static double rating(GameState g) {
        //Größter momentaner Schwarm
        //Schwarm-Anzahl
        int anzahlRoteFische = g.roteFische.popCount();
        ArrayList<Schwarm> roteSchwaerme = berechneSchwaerme(g, GameColor.RED);
        double redInsg= 0;
        double redSchwarmSum = 0;
        for (Schwarm s : roteSchwaerme) {
            redSchwarmSum += Math.pow(((s.size + 0.0) / anzahlRoteFische), 2);
        }
        double redSchwarmDist=0;
        for (int i0 = 0; i0 < roteSchwaerme.size(); i0++) {
            Schwarm s0 = roteSchwaerme.get(i0);
            for (int i1 = i0 + 1; i1 < roteSchwaerme.size(); i1++) {
                Schwarm s1 = roteSchwaerme.get(i1);
                double mittlereSchwarmGroesse= (s0.size+s1.size)/2.0;
                //Minimale Distanz ausrechnen
                int minDist = 9;
                for (int p0 = 0; p0 < s0.size; p0++) {
                    Pos pos0 = s0.positions.get(p0);
                    for (int p1 = 0; p1 < s1.size; p1++) {
                        Pos pos1 = s1.positions.get(p1);
                        int dist = Math.max(Math.abs(pos0.x - pos1.x), Math.abs(pos0.y - pos1.y));
                        if (dist < minDist) {
                            minDist = dist;
                        }
                    }
                }
                redSchwarmDist += (1 - minDist / 9.0)/(anzahlRoteFische-mittlereSchwarmGroesse);
            }
        }
        redInsg=redSchwarmDist+redSchwarmSum;


        //System.out.println("RedSchwarmSum: "+redSchwarmSum);
        //System.out.println("RedSchwarmDist: "+redSchwarmDist);
        //System.out.println("RedInsg: "+redInsg);
        int anzahlblaueFische = g.blaueFische.popCount();
        ArrayList<Schwarm> blaueSchwaerme = berechneSchwaerme(g, GameColor.BLUE);
        double blueInsg = 0;
        double blueSchwarmSum=0;
        double blueSchwarmDist=0;
        for (Schwarm s : blaueSchwaerme) {
            blueSchwarmSum += Math.pow(((s.size + 0.0) / anzahlblaueFische), 2);
        }

        for (int i0 = 0; i0 < blaueSchwaerme.size(); i0++) {
            Schwarm s0 = blaueSchwaerme.get(i0);
            for (int i1 = i0 + 1; i1 < blaueSchwaerme.size(); i1++) {
                Schwarm s1 = blaueSchwaerme.get(i1);
                double mittlereSchwarmGroesse= (s0.size+s1.size)/2.0;
                //Minimale Distanz ausrechnen
                int minDist = 9;
                for (int p0 = 0; p0 < s0.size; p0++) {
                    Pos pos0 = s0.positions.get(p0);
                    for (int p1 = 0; p1 < s1.size; p1++) {
                        Pos pos1 = s1.positions.get(p1);
                        int dist = Math.max(Math.abs(pos0.x - pos1.x), Math.abs(pos0.y - pos1.y));
                        if (dist < minDist) {
                            minDist = dist;
                        }
                    }
                }
                blueSchwarmDist += (1 - minDist / 9.0)/(anzahlblaueFische-mittlereSchwarmGroesse);
            }
        }
        blueInsg=blueSchwarmSum+blueSchwarmDist;
        //System.out.println("BlueSchwarmSum: "+blueSchwarmSum);
        //System.out.println("BlueSchwarmDist: "+blueSchwarmDist);
        //System.out.println("BlueInsg: "+blueInsg);
        return redInsg-blueInsg;
    }

    public static ArrayList<Schwarm> berechneSchwaerme(GameState g, GameColor gc) {
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

    public Schwarm(int size, ArrayList<Pos> positions) {
        this.size = size;
        this.positions = positions;
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
