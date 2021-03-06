package game;

import datastructures.BitBoard;

public class GameLogic {
    static GameDirection[] directions = GameDirection.values();

    //Diese Methode berechnet nicht den größten Schwarm, sondern nur den Schwarm ausgehend von dem Fisch am Weitesten rechts unten.
    public static int getSchwarm(MyGameState gs, GameColor gc) {
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
    public static BitBoard getSchwarmBoard(BitBoard meineFische) {
        meineFische = meineFische.clone();
        if (meineFische.equalsZero()) {
            return new BitBoard(0, 0);
        }
        BitBoard result = new BitBoard(0, 0);
        int fischBit;
        BitBoard neighboringFields = new BitBoard(0, 0);
        BitBoard neighboringFieldsAndMeineFische = meineFische.clone();
        boolean b = false;
        do {
            fischBit = neighboringFieldsAndMeineFische.numberOfTrailingZeros();
            meineFische.unsetBitEquals(fischBit);

            result.orEquals(new BitBoard(0, 1).leftShift(fischBit));
            neighboringFields.orEquals(BitBoardConstants.NACHBARN[fischBit]);
            //neighboringFieldsAndMeineFische = neighboringFields.and(meineFische);
            b = BitBoard.inplaceWithParameterAndEqualsZero(neighboringFieldsAndMeineFische, neighboringFields, meineFische);
            //InplaceWithParamater-And-EqualsZero
        } while (!b);
        return result;
    }

    public static void initGmro(MyGameState gs) {
        gs.gmro = new GameMoveResultObject();
    }

    public static void addToGmro(MyGameState gs, GameMove gm, MyGameState ng) {
        try {
            gs.gmro.moves[gs.gmro.instances] = gm;
            gs.gmro.states[gs.gmro.instances] = ng;
            gs.gmro.instances++;
            gs.gmro.attackBoard.orEquals(BitBoardConstants.EINHEITS_UNIT_LEFT_SHIFT[gm.to]);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(gs);
        }
    }

    public static void getPossibleMoves(MyGameState gs, GameColor gc) {
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

    public static void checkDirection(int fischPos, GameDirection dir, int squares, MyGameState gs, GameColor gc, BitBoard meineFische, BitBoard gegnerFische, int iOriginal, int newI) {
        int destination = fischPos + dir.getShift() * squares;
        if (destination <= 99 && destination >= 0) {
            //BitBoard destinationSquare = new BitBoard(0, 1).leftShift(destination);
            BitBoard destinationSquare = BitBoardConstants.EINHEITS_UNIT_LEFT_SHIFT[destination];
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

    public static MyGameState makeNullMove(MyGameState gs) {
        MyGameState newState = new MyGameState(gs.roteFische.clone(), gs.blaueFische.clone(), gs.kraken, (gs.move == GameColor.RED ? GameColor.BLUE :
                GameColor.RED), gs.pliesPlayed + 1, gs.roundsPlayed + (gs.move == GameColor.BLUE ? 1 : 0));
        newState.hash = gs.hash ^ ZobristHashing.SIDE_TO_MOVE_IS_BLUE;
        return newState;
    }

    public static MyGameState makeMove(MyGameState gs, GameMove gm, GameColor gc) {
        BitBoard leftShiftTo = BitBoardConstants.EINHEITS_UNIT_LEFT_SHIFT[gm.to];
        BitBoard leftShiftToNot = BitBoardConstants.EINHEITS_UNIT_LEFT_SHIFT_NOT[gm.to];
        BitBoard leftShiftFromNot = BitBoardConstants.EINHEITS_UNIT_LEFT_SHIFT_NOT[gm.from];
        if (gc == GameColor.RED) {
            BitBoard newRed = gs.roteFische.and(leftShiftFromNot);
            newRed.orEquals(leftShiftTo);
            BitBoard newBlau = gs.blaueFische.and(leftShiftToNot);
            //Update hash
            long hash = gs.hash;
            hash ^= ZobristHashing.ZOBRIST_KEYS[gm.from / 10][gm.from % 10][0];
            hash ^= ZobristHashing.ZOBRIST_KEYS[gm.to / 10][gm.to % 10][0];
            if (!gs.blaueFische.equals(newBlau)) {
                hash ^= ZobristHashing.ZOBRIST_KEYS[gm.to / 10][gm.to % 10][1];
            }
            hash ^= ZobristHashing.SIDE_TO_MOVE_IS_BLUE;
            MyGameState newGameState = new MyGameState(newRed, newBlau, gs.kraken, GameColor.BLUE, gs.pliesPlayed + 1, gs.roundsPlayed);
            newGameState.hash = hash;
            return newGameState;
        } else {
            BitBoard newBlau = gs.blaueFische.and(leftShiftFromNot);
            newBlau.orEquals(leftShiftTo);
            BitBoard newRed = gs.roteFische.and(leftShiftToNot);
            //Update hash
            long hash = gs.hash;
            hash ^= ZobristHashing.ZOBRIST_KEYS[gm.from / 10][gm.from % 10][1];
            hash ^= ZobristHashing.ZOBRIST_KEYS[gm.to / 10][gm.to % 10][1];
            if (!gs.roteFische.equals(newRed)) {
                hash ^= ZobristHashing.ZOBRIST_KEYS[gm.to / 10][gm.to % 10][0];
            }
            hash ^= ZobristHashing.SIDE_TO_MOVE_IS_BLUE;
            MyGameState newGameState = new MyGameState(newRed, newBlau, gs.kraken, GameColor.RED, gs.pliesPlayed + 1, gs.roundsPlayed + 1);
            newGameState.hash = hash;
            return newGameState;
        }
    }
}
