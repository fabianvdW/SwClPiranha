package game;

import datastructures.BitBoard;

import java.util.ArrayList;
import java.util.HashMap;

public class GameLogic {
    static GameDirection[] directions = GameDirection.values();
    static int someValue;

    //Diese Methode berechnet nicht den größten Schwarm, sondern nur den Schwarm ausgehend von dem Fisch am Weitesten rechts unten.
    public static int getSchwarm(GameState gs, GameColor gc) {
        BitBoard meineFische;
        if (gc == GameColor.RED) {
            meineFische = gs.roteFische.clone();
        } else {
            meineFische = gs.blaueFische.clone();
        }
        if (meineFische.equalsZero()) {
            return 0;
        }
        int result = 0;
        int fischBit;
        BitBoard neighboringFields = new BitBoard(0, 0);
        BitBoard neighboringFieldsAndMeineFische = meineFische.clone();
        boolean b = false;
        do {
            result++;
            fischBit = neighboringFieldsAndMeineFische.numberOfTrailingZeros();
            meineFische.unsetBitEquals(fischBit);
            neighboringFields.orEquals(BitBoardConstants.NACHBARN[fischBit]);
            //neighboringFieldsAndMeineFische = neighboringFields.and(meineFische);
            b = BitBoard.inplaceWithParameterAndEqualsZero(neighboringFieldsAndMeineFische, neighboringFields, meineFische);
            //InplaceWithParamater-And-EqualsZero
        } while (!b);
        return result;
    }

    //Diese Methode berechnet nicht den größten Schwarm, sondern nur den Schwarm ausgehend von dem Fisch am Weitesten rechts unten.
    public static BitBoard getSchwarmBoard(BitBoard meineFische, GameColor gc) {
        meineFische=meineFische.clone();
        if (meineFische.equalsZero()) {
            return new BitBoard(0,0);
        }
        BitBoard result= new BitBoard(0,0);
        int fischBit;
        BitBoard neighboringFields = new BitBoard(0, 0);
        BitBoard neighboringFieldsAndMeineFische = meineFische.clone();
        boolean b = false;
        do {
            fischBit = neighboringFieldsAndMeineFische.numberOfTrailingZeros();
            meineFische.unsetBitEquals(fischBit);

            result.orEquals(new BitBoard(0,1).leftShift(fischBit));
            neighboringFields.orEquals(BitBoardConstants.NACHBARN[fischBit]);
            //neighboringFieldsAndMeineFische = neighboringFields.and(meineFische);
            b = BitBoard.inplaceWithParameterAndEqualsZero(neighboringFieldsAndMeineFische, neighboringFields, meineFische);
            //InplaceWithParamater-And-EqualsZero
        } while (!b);
        return result;
    }

    public static void initGmro(GameState gs) {
        gs.gmro = new GameMoveResultObject();
    }

    public static void addToGmro(GameState gs, GameMove gm, GameState ng) {
        gs.gmro.moves[gs.gmro.instances] = gm;
        gs.gmro.states[gs.gmro.instances] = ng;
        gs.gmro.instances++;
    }

    public static void getPossibleMoves(GameState gs, GameColor gc) {
        if (gs.gs != GameStatus.INGAME) {
            return;
        }
        BitBoard meineFische;
        BitBoard gegnerFische;
        if (gc == GameColor.RED) {
            meineFische = gs.roteFische;
            gegnerFische = gs.blaueFische;
        } else {
            meineFische = gs.blaueFische;
            gegnerFische = gs.roteFische;
        }
        //Jeden Fisch durchgehen
        //Es gibt 8 Richtungen es braucht also einen Array [100][8]
        //gs.possibleMoves= new ArrayList<>(80);
        //gs.possibleFollowingStates= new ArrayList<>(80);
        //gs.gmro= new GameMoveResultObject();
        initGmro(gs);
        BitBoard fischIterator = meineFische.clone();
        while (!fischIterator.equalsZero()) {
            int fischPos = fischIterator.numberOfTrailingZeros();
            for (int i = 0; i < 4; i++) {
                //Check for possible moves
                int squares = BitBoardConstants.SQUARE_ATTACK_DIRECTION_SQUARES_TWO_SIDED[fischPos][i].popCountOnMeAndOredBitBoards(meineFische, gegnerFische);
                checkDirection(fischPos, GameLogic.directions[i], squares, gs, gc, meineFische, gegnerFische, i, i);
                checkDirection(fischPos, GameLogic.directions[i + 4], squares, gs, gc, meineFische, gegnerFische, i, i + 4);
            }
            fischIterator.unsetBitEquals(fischPos);
        }
        return;
    }

    public static void checkDirection(int fischPos, GameDirection dir, int squares, GameState gs, GameColor gc, BitBoard meineFische, BitBoard gegnerFische, int iOriginal, int newI) {
        int destination = fischPos + dir.getShift() * squares;
        if (destination <= 99 && destination >= 0) {
            //BitBoard destinationSquare = new BitBoard(0, 1).leftShift(destination);
            BitBoard destinationSquare= BitBoardConstants.EINHEITS_UNIT_LEFT_SHIFT[destination];
            //Check that destinationSquare is on attackLine and destinationSquare is not fish of my color or Kraken
            if (destinationSquare.orEqualsZero(meineFische, gs.kraken) && !BitBoardConstants.SQUARE_ATTACK_DIRECTION_SQUARES_TWO_SIDED[fischPos][iOriginal].andEqualsZero(destinationSquare)) {
                //Check that there is no enemy fish on the line
                //Second Argument was: BitBoardConstants.SQUARE_ATTACK_DIRECTION_SQUARE_DESTINATION_ATTACK_LINE[fischPos][i + (j == 0 ? 0 : 4)][squares - 2].and(gegnerFische).equalsZero()
                if (squares < 2 || BitBoardConstants.SQUARE_ATTACK_DIRECTION_SQUARE_DESTINATION_ATTACK_LINE[fischPos][newI][squares - 2].andEqualsZero(gegnerFische)) {
                    //Valid move
                    GameMove gm = new GameMove(fischPos, destination, dir);
                    addToGmro(gs, gm, makeMove(gs, gm, gc));
                }
            }
        }
    }

    public static GameState makeMove(GameState gs, GameMove gm, GameColor gc) {
        BitBoard leftShiftTo = BitBoardConstants.EINHEITS_UNIT_LEFT_SHIFT[gm.to];
        BitBoard leftShiftToNot = BitBoardConstants.EINHEITS_UNIT_LEFT_SHIFT_NOT[gm.to];
        BitBoard leftShiftFromNot = BitBoardConstants.EINHEITS_UNIT_LEFT_SHIFT_NOT[gm.from];
        if (gc == GameColor.RED) {
            BitBoard newRed = gs.roteFische.and(leftShiftFromNot);
            newRed.orEquals(leftShiftTo);
            BitBoard newBlau = gs.blaueFische.and(leftShiftToNot);
            return new GameState(newRed, newBlau, gs.kraken, GameColor.BLUE, gs.pliesPlayed + 1, gs.roundsPlayed);
        } else {
            BitBoard newBlau = gs.blaueFische.and(leftShiftFromNot);
            newBlau.orEquals(leftShiftTo);
            BitBoard newRed = gs.roteFische.and(leftShiftToNot);
            return new GameState(newRed, newBlau, gs.kraken, GameColor.RED, gs.pliesPlayed + 1, gs.roundsPlayed + 1);
        }
    }
}
