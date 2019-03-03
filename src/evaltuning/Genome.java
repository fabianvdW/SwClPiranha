package evaltuning;

import artificialplayer.BoardRatingConstants;
import sun.security.jgss.wrapper.GSSNameElement;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

public class Genome {
    public static double[] standardDna = {0.2, 2.0, -0.15, 0.3, 2.0, 5.0, -2.5, -0.25, -3.1, -5.5, -1.3};
    public static Random r = new Random();
    public double[] dna;
    public BoardRatingConstants brc;

    public Genome(double[] dna) {
        this.dna = dna;
        this.brc = new BoardRatingConstants(this.dna);
    }

    public Genome() {
        this.dna = new double[BoardRatingConstants.size];
        for (int i = 0; i < this.dna.length; i++) {
            this.dna[i] = Math.random() * 6 - 3.0;
        }
        this.brc = new BoardRatingConstants(this.dna);
    }

    public Genome mutate(double mutate_staerke) {
        double[] nextDna = this.dna.clone();
        int mutate = Math.random() < 0.4 ? 2 : 1;
        for (int i = 0; i < mutate; i++) {
            int index = (int) (Math.random() * this.dna.length);
            nextDna[index] += r.nextGaussian() * mutate_staerke;
        }
        return new Genome(nextDna);
    }

    public Genome crossover(Genome g1) {
        double[] nextDna = this.dna.clone();
        for (int i = 0; i < this.dna.length; i++) {
            if (Math.random() < 0.5) {
                nextDna[i] = g1.dna[i];
            }
        }
        return new Genome(nextDna);
    }

    public double l1Distance() {
        double res = 0.0;
        for (int i = 0; i < this.dna.length; i++) {
            res += Math.abs(this.dna[i] - standardDna[i]);
        }
        return res;
    }

    public double l2Distance() {
        double res = 0.0;
        for (int i = 0; i < this.dna.length; i++) {
            res += Math.pow(this.dna[i] - standardDna[i], 2);
        }
        return res;
    }

    @Override
    public String toString() {
        String res = "";
        res += Arrays.toString(this.dna) + "\n";
        res += "L1 Distance: " + this.l1Distance() + "\n";
        res += "L2 Distance: " + this.l2Distance();
        return res;
    }
}
