package evaltuning;

import artificialplayer.BoardRatingConstants;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;

public class TexelGeneticAlgorithm {

    public final static int POPULATION_SIZE = 90;
    public final static int DNA_SIZE = 18;
    public final static double STANDARD_MR = 0.15;
    public final static double STANDARD_MS = 0.4;
    public double mr = 0.15;
    public double mutate_staerke = 1.0;
    public final static int processes = 4;
    public final static Random r = new Random();

    static ArrayList<LabeledGameState> states;

    int stuck = 0;
    double lastStuck = 100;
    double deltaStuck = 0.0003;
    int generations = 0;
    Individuum[] individuen;
    Individuum bestIndividum;

    public TexelGeneticAlgorithm() {
        this.individuen = new Individuum[POPULATION_SIZE];
        for (int i = 0; i < this.individuen.length; i++) {
            this.individuen[i] = new Individuum(getRandomDna());
        }
    }

    public static double[] getRandomDna() {
        double[] res = new double[DNA_SIZE];
        for (int i = 0; i < res.length; i++) {
            res[i] = (Math.random() * 2 - 1) * 6;
        }
        return res;
    }

    public static void shuffleArray(Individuum[] ar) {
        // If running on Java 6 or older, use `new Random()` on RHS here
        Random rnd = ThreadLocalRandom.current();
        for (int i = ar.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            Individuum a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    public void doGeneration() {
        //Initialize all Individuen
        TexelGeneticAlgorithm.shuffleArray(this.individuen);
        int analyzesPerProcessor = POPULATION_SIZE / processes;
        int rest = POPULATION_SIZE - analyzesPerProcessor * processes;
        IndivdiuumAnalyzer[] threads = new IndivdiuumAnalyzer[processes];
        int gamesPlayed = 0;
        for (int i = 0; i < processes; i++) {
            int games = analyzesPerProcessor;
            if (rest > 0) {
                rest -= 1;
                games++;
            }
            threads[i] = new IndivdiuumAnalyzer(this, gamesPlayed, gamesPlayed + games);
            gamesPlayed += games;

        }
        try {
            for (int i = 0; i < processes; i++) {
                threads[i].join();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Arrays.sort(this.individuen, new Comparator<Individuum>() {
            @Override
            public int compare(Individuum o1, Individuum o2) {
                if (o1.loss < o2.loss) {
                    return -1;
                } else if (o1.loss > o2.loss) {
                    return 1;
                }
                return 0;
            }
        });
        if (bestIndividum == null || bestIndividum.loss > this.individuen[0].loss) {
            bestIndividum = this.individuen[0];
        }

        if (bestIndividum.loss < lastStuck - deltaStuck) {
            stuck = 0;
            lastStuck = bestIndividum.loss;
        } else {
            stuck++;
        }
        recalculateRates();

        ScoredIndividuum[] diversity = new ScoredIndividuum[POPULATION_SIZE];
        for (int i = 0; i < POPULATION_SIZE; i++) {
            diversity[i] = new ScoredIndividuum(Individuum.getDistanceToAll(this.individuen, this.individuen[i]), this.individuen[i]);
        }
        Arrays.sort(diversity, new Comparator<ScoredIndividuum>() {
            @Override
            public int compare(ScoredIndividuum o1, ScoredIndividuum o2) {
                if (o1.score > o2.score) {
                    return -1;
                } else if (o1.score < o2.score) {
                    return 1;
                }
                return 0;
            }
        });

        ScoredIndividuum[] individuenSorted = new ScoredIndividuum[POPULATION_SIZE];
        for (int i = 0; i < POPULATION_SIZE; i++) {
            double multiplier = Math.min(stuck * 0.05, 1.0);
            double score = i + multiplier * indexOf(this.individuen[i], diversity);
            individuenSorted[i] = new ScoredIndividuum(score, this.individuen[i]);
        }

        Arrays.sort(individuenSorted, new Comparator<ScoredIndividuum>() {
            @Override
            public int compare(ScoredIndividuum o1, ScoredIndividuum o2) {
                if (o1.score < o2.score) {
                    return -1;
                } else if (o1.score > o2.score) {
                    return 1;
                }
                return 0;
            }
        });
        /*for (int i = 0; i < POPULATION_SIZE; i++) {
            System.out.println(individuenSorted[i]);
        }*/

        Individuum[] nextPopulation = new Individuum[POPULATION_SIZE];
        for (int i = 0; i < 30; i++) {
            nextPopulation[i] = individuenSorted[i].iv;
        }
        for (int i = 30; i < 60; i++) {
            nextPopulation[i] = individuenSorted[i].iv.mutate(mr, mutate_staerke);
        }
        for (int i = 60; i < 75; i++) {
            //Draw Random individuals
            int rIndex1 = (int) (Math.random() * 30);
            int rIndex2 = (int) (Math.random() * 30);
            while (rIndex1 == rIndex2) {
                rIndex2 = (int) (Math.random() * 30);
            }
            nextPopulation[i] = individuenSorted[rIndex1].iv.crossover(individuenSorted[rIndex2].iv);
        }
        for (int i = 75; i < 88; i++) {
            //Draw Random individuals
            int rIndex1 = (int) (Math.random() * 30);
            int rIndex2 = (int) (Math.random() * 30);
            while (rIndex1 == rIndex2) {
                rIndex2 = (int) (Math.random() * 30);
            }
            nextPopulation[i] = individuenSorted[rIndex1].iv.crossover(individuenSorted[rIndex2].iv).mutate(mr, this.mutate_staerke);
        }
        nextPopulation[88] = bestIndividum;
        nextPopulation[89] = new Individuum(getRandomDna());
        this.individuen = nextPopulation;
        generations++;
        System.out.println("Generation: " + this.generations + " done!. Best Individuum: ");
        System.out.println("Loss: " + this.bestIndividum.loss);
        System.out.println(this.bestIndividum.toString());
    }

    public static int indexOf(Individuum iv, ScoredIndividuum[] arr) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].iv == iv) {
                return i;
            }
        }
        System.exit(3);
        return -1;
    }

    public void recalculateRates() {
        this.mr = STANDARD_MR - 0.01 * this.generations + 0.01 * stuck;
        this.mr = Math.max(0.075, this.mr);

        this.mutate_staerke = STANDARD_MS * (1 + 0.03 * stuck);
        this.mutate_staerke = Math.min(4.0, this.mutate_staerke);
    }

    public static void main(String[] args) {
        TexelParser.readBst();
        states = new ArrayList<>(BinarySearchTree.nodeCount);
        TexelParser.bst.traverse(states);
        System.out.println("States size: " + states.size());
        TexelGeneticAlgorithm tga = new TexelGeneticAlgorithm();
        for (int i = 0; i < 10000; i++) {
            tga.doGeneration();
        }
    }


}

class ScoredIndividuum {
    double score;
    Individuum iv;

    public ScoredIndividuum(double score, Individuum iv) {
        this.score = score;
        this.iv = iv;
    }

    @Override
    public String toString() {
        return "" + score;
    }
}

class IndivdiuumAnalyzer extends Thread {
    int start;
    int stop;
    TexelGeneticAlgorithm tga;

    public IndivdiuumAnalyzer(TexelGeneticAlgorithm tga, int start, int stop) {
        this.tga = tga;
        this.start = start;
        this.stop = stop;
        this.start();
    }

    @Override
    public void run() {
        for (int i = start; i < stop; i++) {
            Individuum iv = tga.individuen[i];
            if (!iv.analyzed) {
                iv.loss = Individuum.getLoss(iv.brc);
                iv.analyzed = true;
            }
        }
    }

}

class Individuum {
    double[] dna;
    BoardRatingConstants brc;
    double loss;
    boolean analyzed = false;

    public Individuum(double[] dna) {
        this.dna = dna;
        brc = new BoardRatingConstants(this.dna);
    }

    public double distance(Individuum i2) {
        double res = 0.0;
        for (int i = 0; i < this.dna.length; i++) {
            res += Math.pow(this.dna[i] - i2.dna[i], 2);
        }
        return res;
    }

    public static double getDistanceToAll(Individuum[] arr, Individuum a) {
        double res = 0.0;
        for (Individuum iv : arr) {
            if (iv != a) {
                res += a.distance(iv);
            }
        }
        return res;
    }

    public Individuum mutate(double mr, double ms) {
        double[] dnaClone = this.dna.clone();
        for (int i = 0; i < this.dna.length; i++) {
            if (Math.random() < mr) {
                if (Math.random() < 0.8) {
                    dnaClone[i] += TexelGeneticAlgorithm.r.nextGaussian() * ms;
                } else if (Math.random() < 0.5) {
                    dnaClone[i] *= Math.random() + 0.5;
                } else {
                    dnaClone[i] = (Math.random() * 2 - 1) * 3;
                }
            }
        }
        return new Individuum(dnaClone);
    }

    public Individuum crossover(Individuum i2) {
        double[] copyDna = new double[this.dna.length];
        for (int i = 0; i < copyDna.length; i++) {
            if (Math.random() <= 0.5) {
                copyDna[i] = this.dna[i];
            } else {
                copyDna[i] = i2.dna[i];
            }
        }
        return new Individuum(copyDna);
    }

    public static double getLoss(BoardRatingConstants brc) {
        return TexelParser.evaluationError(TexelGeneticAlgorithm.states, brc);
    }


    @Override
    public String toString() {
        return Arrays.toString(this.dna);
    }
}
