package evaltuning;

import artificialplayer.AlphaBeta;
import artificialplayer.BoardRating;
import artificialplayer.BoardRatingConstants;
import artificialplayer.UsedFeature;
import game.MyGameState;
import helpers.FEN;
import helpers.GlobalFlags;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class TexelParser {
    static double k = 0.27;
    static int theorticalMax = 0;
    static BinarySearchTree bst;
    public static UsedFeature[] lastEval = new UsedFeature[6];
    static int batchSize = 16000;
    static double lr = -0.1;
    /*
     * Unique GameStates: 3597341
     * Label Collisons when parsing: 64940
     * Parsed GameStates: 3796245
     * States size: 3597341
     */
    public static String[] paths = {
            "./Texel/texeldataGut1.txt",
            "./Texel/texeldataGut2.txt",
            "./Texel/texeldataLTC.txt",
            "./Texel/texeldataSchlecht1.txt",
            "./Texel/texeldataSchlecht2.txt",
    };
    public static String path = "./Texel/bst.txt";

    public static void saveBst() {
        try {
            FileOutputStream f = new FileOutputStream(new File(path));
            ObjectOutputStream o = new ObjectOutputStream(new BufferedOutputStream(f));
            o.writeObject(bst);
            o.close();
            f.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void makeBst() {
        for (String s : paths) {
            readTexelFile(s);
        }
        System.out.println("Unique GameStates: " + BinarySearchTree.nodeCount);
        System.out.println("Label Collisons when parsing: " + BinarySearchTree.labelCollisions);
        System.out.println("Parsed GameStates: " + theorticalMax);
    }

    public static void readBst() {
        try {
            FileInputStream fis = new FileInputStream(new File(path));
            ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(fis));
            bst = (BinarySearchTree) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        //makeBst();
        //saveBst();
        GlobalFlags.TEXEL_TUNING = true;
        readBst();
        ArrayList<LabeledGameState> states = new ArrayList<>(BinarySearchTree.nodeCount);
        bst.traverse(states);
        System.out.println("States size: " + states.size());
        AlphaBeta.brc = new BoardRatingConstants(AlphaBeta.gaDna);
        /*
        for (int i = 0; i < 100; i++) {
            System.out.println("Loss: " + evaluationError(states));
            makeSGD(states);
            System.out.println("New Weights: ");
            System.out.println(Arrays.toString(AlphaBeta.gaDna));
        }*/
        System.out.println("Loss: " + evaluationError(states,AlphaBeta.brc));
        /*
        for (int i = 0; i < 18; i++) {
            double[] best = null;
            double bestScore = 1;
            for (int j = 0; j < 100; j++) {
                double plus = j / 100.0 - 0.5;
                double[] cl = AlphaBeta.gaDna.clone();
                cl[i] += plus;
                AlphaBeta.brc = new BoardRatingConstants(cl);
                double err = evaluationError(states);
                System.out.println("I: " + i + ", J: " + j + " Error: " + err);
                if (err < bestScore) {
                    bestScore = err;
                    best = cl;
                }
            }
            AlphaBeta.gaDna = best;
        }
        */
        System.out.println(Arrays.toString(AlphaBeta.gaDna));
    }

    public static double evaluationError(ArrayList<LabeledGameState> states,BoardRatingConstants brc) {
        double res = 0.0;
        for (LabeledGameState lgs : states) {
            res += Math.pow(lgs.label - sigmoid(BoardRating.rating(lgs.mg, brc)), 2);
        }
        res /= states.size() + 0.0;
        return res;
    }

    public static void makeSGD(ArrayList<LabeledGameState> states) {
        int iterations = (int) (Math.ceil(states.size() / (batchSize + 0.0)));
        for (int i = 0; i < iterations; i++) {
            makeMiniBatch(states, i);
        }
        Collections.shuffle(states);
    }

    public static void makeMiniBatch(ArrayList<LabeledGameState> states, int batchNum) {
        double[] gradient = new double[18];
        int size = states.size();
        for (int i = batchNum * batchSize; i < size && i < (batchNum + 1) * batchSize; i++) {
            LabeledGameState lgs = states.get(i);
            double eval = BoardRating.rating(lgs.mg, AlphaBeta.brc);
            double labelMinusEval = lgs.label - eval;
            double[] evalDeriv = makeEvalDeriv();
            double sigmaDerivAt = sigmoid_deriv(Math.pow(labelMinusEval, 2));
            multiplyDouble(evalDeriv, sigmaDerivAt * 2 * labelMinusEval);
            addDouble(gradient, evalDeriv);
        }
        multiplyDouble(gradient, (1 / (batchSize + 0.0)) * lr);
        addDouble(AlphaBeta.gaDna, gradient);
        AlphaBeta.brc = new BoardRatingConstants(AlphaBeta.gaDna);
    }

    public static double[] makeEvalDeriv() {
        double[] res = new double[18];
        for (int i = 0; i < 6; i++) {
            res[3 * i] = lastEval[i].inputA;
            res[3 * i + 1] = lastEval[i].inputB;
            res[3 * i + 2] = lastEval[i].inputC;
        }
        return res;
    }

    public static void addDouble(double[] addTo, double[] toAdd) {
        for (int i = 0; i < addTo.length; i++) {
            addTo[i] += toAdd[i];
        }
    }

    public static void multiplyDouble(double[] toMultiply, double multiplier) {
        for (int i = 0; i < toMultiply.length; i++) {
            toMultiply[i] *= multiplier;
        }
    }

    public static double sigmoid(double s) {
        return 1.0 / (1.0 + Math.pow(10, -k * s / 4.0));
    }

    public static double sigmoid_deriv(double x) {
        double oben = k * Math.log(10);
        double coshInput = oben * x / 8.0;
        return oben / (16.0 * Math.pow(cosh(coshInput), 2));
    }

    public static double cosh(double x) {
        return (Math.exp(x) + Math.exp(-x)) / 2.0;
    }

    public static void readTexelFile(String path) {
        try {
            BufferedReader bfr = new BufferedReader(new FileReader(path));
            //Read game
            String currentLine = null;
            while ((currentLine = bfr.readLine()) != null) {
                currentLine = currentLine.trim();
                ArrayList<MyGameState> currentGame = new ArrayList<MyGameState>(52);
                do {
                    MyGameState mgs = FEN.readFEN(currentLine);
                    currentGame.add(mgs);
                    currentLine = bfr.readLine().trim();
                }
                while (!currentLine.equalsIgnoreCase("Blue") && !currentLine.equalsIgnoreCase("Red") && !currentLine.equalsIgnoreCase("Draw"));
                double label = 0;
                if (currentLine.equalsIgnoreCase("Blue")) {
                    label = 0;
                } else if (currentLine.equalsIgnoreCase("Red")) {
                    label = 1;
                } else if (currentLine.equalsIgnoreCase("Draw")) {
                    label = 0.5;
                } else {
                    System.exit(-3);
                }
                theorticalMax += currentGame.size();
                for (MyGameState mg : currentGame) {
                    if (bst == null) {
                        bst = new BinarySearchTree(new LabeledGameState(mg, label));
                    } else {
                        bst.insert(new LabeledGameState(mg, label));
                    }
                }
            }
            bfr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class BinarySearchTree implements Serializable {
    static final long serialVersionUID = 42L;
    public static int nodeCount = 0;
    public static int labelCollisions = 0;
    LabeledGameState myObject;
    BinarySearchTree left;
    BinarySearchTree right;
    boolean unUsable = false;

    public BinarySearchTree(LabeledGameState lgs) {
        nodeCount++;
        this.myObject = lgs;
    }

    public void insert(LabeledGameState lgs) {
        if (lgs.mg.hash == this.myObject.mg.hash) {
            if (!lgs.mg.equals(myObject.mg)) {
                //Hash collision
                System.out.println(lgs.mg);
                System.out.println(myObject.mg);
                System.out.println("Hash collision!");
                System.exit(-2);
            } else {
                if (lgs.mg.pliesPlayed != myObject.mg.pliesPlayed) {
                    if (this.right != null) {
                        this.right.insert(lgs);
                    } else {
                        this.right = new BinarySearchTree(lgs);
                    }
                } else if (lgs.label != myObject.label) {
                    labelCollisions++;
                    if (!unUsable) {
                        nodeCount--;
                    }
                    unUsable = true;
                }
            }
        } else if (lgs.mg.hash < this.myObject.mg.hash) {
            if (this.left != null) {
                this.left.insert(lgs);
            } else {
                this.left = new BinarySearchTree(lgs);
            }
        } else {
            if (this.right != null) {
                this.right.insert(lgs);
            } else {
                this.right = new BinarySearchTree(lgs);
            }
        }
    }

    public void traverse(ArrayList<LabeledGameState> lgs) {
        if (this.left != null) {
            this.left.traverse(lgs);
        }
        if (!this.unUsable) {
            lgs.add(myObject);
        }
        if (this.right != null) {
            this.right.traverse(lgs);
        }
    }
}