package game;

import datastructures.BitBoard;

import java.util.ArrayList;
import java.util.HashMap;

public class GameLogic {
    static GameDirection[] directions= GameDirection.values();
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
        BitBoard neighboringFieldsAndMeineFische = meineFische;
        boolean b=false;
        do {
            result++;
            fischBit = neighboringFieldsAndMeineFische.numberOfTrailingZeros();
            meineFische.unsetBitEquals(fischBit);
            neighboringFields.orEquals(BitBoardConstants.NACHBARN[fischBit]);
            //neighboringFieldsAndMeineFische = neighboringFields.and(meineFische);
            b=BitBoard.inplaceWithParameterAndEqualsZero(neighboringFieldsAndMeineFische,neighboringFields,meineFische);
            //InplaceWithParamater-And-EqualsZero
        } while (!b);
        return result;
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
        gs.possibleMoves= new ArrayList<>(80);
        gs.possibleFollowingStates= new ArrayList<>(80);
        BitBoard fischIterator = meineFische.clone();
        while (!fischIterator.equalsZero()) {
            int fischPos = fischIterator.numberOfTrailingZeros();
            for (int i = 0; i < 4; i++) {
                GameDirection dir = GameLogic.directions[i];
                //Check for possible moves
                //BitBoard attackLine = BitBoardConstants.SQUARE_ATTACK_DIRECTION_SQUARES_TWO_SIDED[fischPos][i].and(meineFische.or(gegnerFische));
                //int squares = attackLine.popCount();
                int squares=BitBoardConstants.SQUARE_ATTACK_DIRECTION_SQUARES_TWO_SIDED[fischPos][i].popCountOnMeAndOredBitBoards(meineFische,gegnerFische);
                for (int j = 0; j < 2; j++) {
                    int destination = fischPos + dir.getShift() * squares * (j == 0 ? 1 : -1);
                    if (destination <= 99 && destination >= 0) {
                        //BitBoard destinationSquare = new BitBoard(0, 1).leftShift(destination);
                        BitBoard destinationSquare= BitBoardConstants.EINHEITS_UNIT_LEFT_SHIFT[destination];
                        //Check that destinationSquare is on attackLine and destinationSquare is not fish of my color or Kraken
                        //Second Argument was: !BitBoardConstants.SQUARE_ATTACK_DIRECTION_SQUARES_TWO_SIDED[fischPos][i].and(destinationSquare).equalsZero()
                        //First Argument was: destinationSquare.and(meineFische.or(gs.kraken)).equalsZero()
                        if (destinationSquare.orEqualsZero(meineFische,gs.kraken)&&!BitBoardConstants.SQUARE_ATTACK_DIRECTION_SQUARES_TWO_SIDED[fischPos][i].andEqualsZero(destinationSquare)) {
                            //Check that there is no enemy fish on the line
                            //Second Argument was: BitBoardConstants.SQUARE_ATTACK_DIRECTION_SQUARE_DESTINATION_ATTACK_LINE[fischPos][i + (j == 0 ? 0 : 4)][squares - 2].and(gegnerFische).equalsZero()
                            if (squares < 2 || BitBoardConstants.SQUARE_ATTACK_DIRECTION_SQUARE_DESTINATION_ATTACK_LINE[fischPos][i + (j == 0 ? 0 : 4)][squares - 2].andEqualsZero(gegnerFische)) {
                                //Valid move
                                GameMove gm = new GameMove(fischPos, destination, (j == 0 ? dir : GameLogic.directions[i + 4]));
                                gs.possibleMoves.add(gm);
                                gs.possibleFollowingStates.add(makeMove(gs,gm,gc));
                                //result.put(gm, makeMove(gs, gm, gc));
                            }
                        }
                    }
                }

            }
            fischIterator.unsetBitEquals(fischPos);
        }
        return;
    }

    public static GameState makeMove(GameState gs, GameMove gm, GameColor gc) {
        BitBoard leftShiftTo = BitBoardConstants.EINHEITS_UNIT_LEFT_SHIFT[gm.to];
        BitBoard leftShiftToNot=BitBoardConstants.EINHEITS_UNIT_LEFT_SHIFT_NOT[gm.to];
        BitBoard leftShiftFromNot=BitBoardConstants.EINHEITS_UNIT_LEFT_SHIFT_NOT[gm.from];
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
