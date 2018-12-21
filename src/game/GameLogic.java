package game;

import datastructures.BitBoard;

import java.util.HashMap;

public class GameLogic {

    //Diese Methode berechnet nicht den größten Schwarm, sondern nur den Schwarm ausgehend von dem Fisch am Weitesten rechts unten.
    public static int getSchwarm(GameState gs, GameColor gc) {
        BitBoard meineFische;
        if (gc == GameColor.RED) {
            meineFische = gs.roteFische;
        } else {
            meineFische = gs.blaueFische;
        }
        if (meineFische.equalsZero()) {
            return 0;
        }
        int result = 0;
        int fischBit;
        BitBoard neighboringFields = new BitBoard(0, 0);
        BitBoard neighboringFieldsAndMeineFische = meineFische;
        do {
            result++;
            fischBit = neighboringFieldsAndMeineFische.numberOfTrailingZeros();
            meineFische = meineFische.unsetBit(fischBit);
            neighboringFields.orEquals(BitBoardConstants.NACHBARN[fischBit]);
            neighboringFieldsAndMeineFische = neighboringFields.and(meineFische);
        } while (!neighboringFieldsAndMeineFische.equalsZero());
        return result;
    }

    public static HashMap<GameMove, GameState> getPossibleMoves(GameState gs, GameColor gc) {
        if (gs.gs != GameStatus.INGAME) {
            return null;
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
        HashMap<GameMove, GameState> result = new HashMap<>();
        BitBoard fischIterator = meineFische.clone();
        while (!fischIterator.equalsZero()) {
            int fischPos = fischIterator.numberOfTrailingZeros();
            for (int i = 0; i < 4; i++) {
                GameDirection dir = GameDirection.values()[i];
                //Check for possible moves
                BitBoard attackLine = BitBoardConstants.SQUARE_ATTACK_DIRECTION_SQUARES_TWO_SIDED[fischPos][dir.ordinal()].and(meineFische.or(gegnerFische));
                int squares = attackLine.popCount();
                for (int j = 0; j < 2; j++) {
                    int destination = fischPos + dir.getShift() * squares * (j == 0 ? 1 : -1);
                    if (destination <= 99 && destination >= 0 && Math.abs(destination / 10 - fischPos / 10) <= squares && Math.abs(destination % 10 - fischPos % 10) <= squares) {
                        BitBoard destinationSquare = new BitBoard(0, 1).leftShift(destination);
                        //Check that destinationSquare is on attackLine and destinationSquare is not fish of my color or Kraken
                        if (!BitBoardConstants.SQUARE_ATTACK_DIRECTION_SQUARES_TWO_SIDED[fischPos][dir.ordinal()].and(destinationSquare).equalsZero()&&destinationSquare.and(meineFische.or(gs.kraken)).equalsZero()) {
                            //Check that there is no enemy fish on the line

                            if (squares < 2 || BitBoardConstants.SQUARE_ATTACK_DIRECTION_SQUARE_DESTINATION_ATTACK_LINE[fischPos][dir.ordinal() + (j == 0 ? 0 : 4)][squares - 2].and(gegnerFische).equalsZero()) {
                                //Valid move
                                GameMove gm = new GameMove(fischPos, destination, (j == 0 ? dir : GameDirection.values()[i + 4]));
                                result.put(gm, makeMove(gs, gm, gc));
                            }
                        }
                    }
                }

            }
            fischIterator.unsetBitEquals(fischPos);
        }
        return result;
    }

    public static GameState makeMove(GameState gs, GameMove gm, GameColor gc) {
        BitBoard einheitsUnit = new BitBoard(0, 1);
        BitBoard leftShift = einheitsUnit.leftShift(gm.to);
        if (gc == GameColor.RED) {
            BitBoard newRed = gs.roteFische.and(einheitsUnit.leftShift(gm.from).not());
            newRed.orEquals(leftShift);
            BitBoard newBlau = gs.blaueFische.and(leftShift.not());
            return new GameState(newRed, newBlau, gs.kraken, GameColor.BLUE, gs.pliesPlayed + 1, gs.roundsPlayed);
        } else {
            BitBoard newBlau = gs.blaueFische.and(einheitsUnit.leftShift(gm.from).not());
            newBlau.orEquals(leftShift);
            BitBoard newRed = gs.roteFische.and(leftShift.not());
            return new GameState(newRed, newBlau, gs.kraken, GameColor.RED, gs.pliesPlayed + 1, gs.roundsPlayed + 1);
        }
    }
}
