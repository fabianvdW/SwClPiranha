package evaltuning;

import artificialplayer.PrincipalVariation;
import artificialplayer.Search;
import game.*;
import helpers.FEN;
import helpers.GlobalFlags;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class TexelTuning {
    public final static int processors = 4;
    public final static int N = 64000;
    public static BufferedWriter br;


    public static synchronized void writeGame(String s) {
        try {
            br.write(s);
            br.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        BitBoardConstants.setSquareAttackDirectionSquareDestinationAttackLine("SwClPiranha/src/game/data.txt");
        //BitBoardConstants.setSquareAttackDirectionSquareDestinationAttackLine("data.txt");
        File file = new File(GlobalFlags.TEXEL_PATH);
        try {
            FileWriter fr = new FileWriter(file, true);
            br = new BufferedWriter(fr);
            TexelPlayer[] threads = new TexelPlayer[processors];
            for (int i = 0; i < processors; i++) {
                threads[i] = new TexelPlayer(N / processors);
            }
            while (!isFinished(threads)) {
                Thread.sleep(30000);
                System.out.println(collect(threads));
            }
            br.close();
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int collect(TexelPlayer[] arr) {
        int res = 0;
        for (int i = 0; i < arr.length; i++) {
            res += arr[i].k;
        }
        return res;
    }

    public static boolean isFinished(TexelPlayer[] arr) {
        for (int i = 0; i < arr.length; i++) {
            if (!arr[i].finished) {
                return false;
            }
        }
        return true;
    }

    public static void playGame() {
        StringBuilder sb = new StringBuilder();

        MyGameState mg = new MyGameState();
        mg.hash = MyGameState.calculateHash(mg);
        mg.analyze();
        A:
        while (mg.gs == GameStatus.INGAME) {
            GameMoveResultObject gmro = mg.gmro;
            Search s;
            if (mg.pliesPlayed > 30) {
                s = new Search(mg, 5);
            } else {
                s = new Search(mg, 4);
            }
            s.run();
            PrincipalVariation pv = s.currentBestPv;
            if (mg.pliesPlayed > 4 && pv.score < 29000 && pv.score > -29000) {
                sb.append(FEN.toFEN(mg) + "\n");
            }
            GameMove mv = pv.stack.get(0);
            for (int j = 0; j < gmro.instances; j++) {
                if (gmro.moves[j].to == mv.to && gmro.moves[j].from == mv.from) {
                    mg = gmro.states[j];
                    mg.analyze();
                    continue A;
                }
            }
            System.exit(-1);
        }
        if (mg.gs == GameStatus.DRAW) {
            sb.append("Draw\n");
        } else if (mg.gs == GameStatus.RED_WIN) {
            sb.append("Red\n");
        } else if (mg.gs == GameStatus.BLUE_WIN) {
            sb.append("Blue\n");
        }
        writeGame(sb.toString());

    }
}

class TexelPlayer extends Thread {

    int k;
    int n;
    boolean finished;

    public TexelPlayer(int n) {
        k = 0;
        this.n = n;
        finished = false;
        this.start();
    }

    @Override
    public void run() {
        for (; k < n; k++) {
            TexelTuning.playGame();
        }
        finished = true;
    }
}
