package testing;


import artificialplayer.AlphaBeta;
import artificialplayer.BoardRating;
import artificialplayer.PrincipalVariation;
import artificialplayer.Search;
import datastructures.BitBoard;
import game.BitBoardConstants;
import game.GameColor;
import game.GameMove;
import game.MyGameState;
import helpers.FEN;
import helpers.GlobalFlags;
import helpers.Perft;
import helpers.StringToGameStateConverter;
import sc.plugin2019.Board;

import java.util.Arrays;

public class Fix {
    public static String[] fens = {
            "8 70368744177664 268439552 504403158273884162 0 18018796555993088 r 52 26",
    };
    public static int[] depths = {
            8};

    public static void main(String[] args) {
        GlobalFlags.VERBOSE = true;
        BitBoardConstants.setSquareAttackDirectionSquareDestinationAttackLine("SwClPiranha/src/game/data.txt");
/*
        MyGameState g = FEN.readFEN("8 180179238186385408 10241 18023332108566528 0 72075186223972352 r 50 25");
        g = FEN.readFEN("8 180284791302651904 10241 18023332108566528 0 72075186223972352 b 51 25");
        PrincipalVariation p = AlphaBeta.alphaBetaRoot(g, 6, -1, -100000, 100000);
        System.out.println(p.score);
        printPV(p);
        System.exit(-1);
*/
       // MyGameState g = new MyGameState(new BitBoard(4096L, 140737488355328L));
        /*MyGameState g2= StringToGameStateConverter.readGameState(StringToGameStateConverter.GAME_STATE);
        System.out.println(g2);
        System.out.println(Perft.perft(g2, 5));
        System.exit(0);*/
        MyGameState g = FEN.readFEN("8624603140 589338234064913 128 -9178336006202458112 1024 144115188075855872 r 36 18");
        System.out.println(g);
        System.out.println(BoardRating.rating(g, AlphaBeta.brc));
        System.exit(0);
        //System.out.println(g);
        /*System.out.println(AlphaBeta.alphaBeta(new Search(g, 10), g, 6, 1, -100000.0, 100000.0, 0).score);
        System.exit(-1);*/
        Search se = new Search(g, 7);
        se.run();
        System.out.println(AlphaBeta.nodesExamined);
        System.out.println(AlphaBeta.depth0Nodes);
        System.out.println(AlphaBeta.quiesenceNodes);
        System.out.println(se.currentBestPv.score);
        System.out.println(Arrays.toString(AlphaBeta.indexs));
        System.out.println(AlphaBeta.killerMovesFound);
        System.out.println(AlphaBeta.noKillerMovesFound);
        System.out.println(se.currentBestPv.stack.get(0));
        System.exit(-1);
        byte birth = 0;
        for (int i = 0; i < fens.length; i++) {
            MyGameState mg = FEN.readFEN(fens[i]);
            System.out.println(mg);
            Search s = new Search(mg, depths[i]);
            s.run();
            birth += 2;
            PrincipalVariation pv = s.currentBestPv;
            GameMove m = pv.stack.get(0);
            System.out.println("Move found in Direction: " + m.dir);
            //System.out.println("From: (" + m.from + ")" + " TO: " + m.to);
            System.out.println(m.toString());
            System.out.println("PV:");
            printPV(pv);
            System.out.println("PV Score: " + pv.score);
        }
    }

    public static void printPV(PrincipalVariation pv) {
        for (int j = 0; j < pv.stack.size(); j++) {
            GameMove mv = pv.stack.get(j);
            //System.out.println(mv.from + " " + mv.to);
            System.out.println(mv.toString());
        }
    }

    public static String makeMove(String fen, int from, int to) {
        String res = "";
        MyGameState mg = FEN.readFEN(fen);
        mg.analyze();
        for (int i = 0; i < mg.gmro.instances; i++) {
            if (mg.gmro.moves[i].from == from && mg.gmro.moves[i].to == to) {
                return FEN.toFEN(mg.gmro.states[i]);
            }
        }
        return null;
    }
}
