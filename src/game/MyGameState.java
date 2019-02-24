package game;

import datastructures.BitBoard;
import helpers.StringColor;

public class MyGameState {
    public BitBoard roteFische;
    public BitBoard blaueFische;
    public BitBoard kraken;

    public int pliesPlayed;
    public int roundsPlayed;

    public GameStatus gs;
    public GameColor move;
    //public ArrayList<GameMove> possibleMoves;
    //public ArrayList<MyGameState> possibleFollowingStates;
    public GameMoveResultObject gmro;
    public long hash;

    public MyGameState() {
        //Pre calculated values from BitMasks
        this.roteFische = new BitBoard(0x0000000002018060L, 0x1806018060180400L);
        this.blaueFische = new BitBoard(0x00000007f8000000L, 0x00000000000001feL);
        this.kraken = MyGameState.generateRandomKraken();
        this.move = GameColor.RED;
        this.pliesPlayed = 0;
        this.roundsPlayed = 0;
        this.gs = GameStatus.INGAME;
        this.hash = calculateHash(this);
    }

    public MyGameState(BitBoard kraken) {
        //Pre calculated values from BitMasks
        this.roteFische = new BitBoard(0x0000000002018060L, 0x1806018060180400L);
        this.blaueFische = new BitBoard(0x00000007f8000000L, 0x00000000000001feL);
        this.kraken = kraken;
        this.move = GameColor.RED;
        this.pliesPlayed = 0;
        this.roundsPlayed = 0;
        this.gs = GameStatus.INGAME;
        this.hash = calculateHash(this);
    }

    public MyGameState(BitBoard roteFische, BitBoard blaueFische, BitBoard kraken, GameColor move, int pliesPlayed, int roundsPlayed) {
        //Make sure those BitBoards get cloned
        this.roteFische = roteFische;
        this.blaueFische = blaueFische;
        this.kraken = kraken;
        this.move = move;
        this.pliesPlayed = pliesPlayed;
        this.roundsPlayed = roundsPlayed;
        this.gs = GameStatus.INGAME;
    }

    public static long calculateHash(MyGameState mg) {
        long res = 0L;
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                int shift = y * 10 + x;
                if ((mg.roteFische.rightShift(shift).l1 & 1) == 1) {
                    res ^= ZobristHashing.ZOBRIST_KEYS[y][x][0];
                } else if ((mg.blaueFische.rightShift(shift).l1 & 1) == 1) {
                    res ^= ZobristHashing.ZOBRIST_KEYS[y][x][1];
                } else if ((mg.kraken.rightShift(shift).l1 & 1) == 1) {
                    res ^= ZobristHashing.ZOBRIST_KEYS[y][x][2];
                }
            }
        }
        if (mg.move == GameColor.BLUE) {
            res ^= ZobristHashing.SIDE_TO_MOVE_IS_BLUE;
        }
        return res;
    }

    public void analyze() {
        if (pliesPlayed % 2 == 0) {
            int roteFischeAmount = this.roteFische.popCount();
            int roterSchwarm = GameLogic.getSchwarm(this, GameColor.RED);
            int blaueFischeAmount = this.blaueFische.popCount();
            int blauerSchwarm = GameLogic.getSchwarm(this, GameColor.BLUE);
            if (roterSchwarm == roteFischeAmount && blauerSchwarm == blaueFischeAmount) {
                if (roteFischeAmount > blaueFischeAmount) {
                    this.gs = GameStatus.RED_WIN;
                    return;
                } else if (blaueFischeAmount > roteFischeAmount) {
                    this.gs = GameStatus.BLUE_WIN;
                    return;
                } else {
                    this.gs = GameStatus.DRAW;
                    return;
                }
            } else if (roterSchwarm == roteFischeAmount) {
                this.gs = GameStatus.RED_WIN;
                return;
            } else if (blauerSchwarm == blaueFischeAmount) {
                this.gs = GameStatus.BLUE_WIN;
                return;
            }
        }

        if (roundsPlayed == 30) {
            //Ermittle größten Schwarm
            int roterGroessterSchwarm = 0;
            int blauerGroessterSchwarm = 0;
            BitBoard rFischeClone = this.roteFische.clone();
            while (!rFischeClone.equalsZero()) {
                BitBoard b = GameLogic.getSchwarmBoard(rFischeClone, GameColor.RED);
                int count = b.popCount();
                if (count > roterGroessterSchwarm) {
                    roterGroessterSchwarm = count;
                }
                rFischeClone.andEquals(b.not());
            }
            BitBoard bFischeClone = this.blaueFische.clone();
            while (!bFischeClone.equalsZero()) {
                BitBoard b = GameLogic.getSchwarmBoard(bFischeClone, GameColor.BLUE);
                int count = b.popCount();
                if (count > blauerGroessterSchwarm) {
                    blauerGroessterSchwarm = count;
                }
                bFischeClone.andEquals(b.not());
            }
            if (roterGroessterSchwarm > blauerGroessterSchwarm) {
                this.gs = GameStatus.RED_WIN;
            } else if (roterGroessterSchwarm < blauerGroessterSchwarm) {
                this.gs = GameStatus.BLUE_WIN;
            } else {
                this.gs = GameStatus.DRAW;
            }
            return;
        }

        GameLogic.getPossibleMoves(this, this.move);
        if (this.gmro.instances == 0) {
            if (this.move == GameColor.RED) {
                this.gs = GameStatus.BLUE_WIN;
            } else {
                this.gs = GameStatus.RED_WIN;
            }
        }

    }

    public static BitBoard generateRandomKraken() {
        int pos1;
        do {
            pos1 = getRandomKrakenPosition();
            //Make sure pos is in the middle 6x6 squares
        } while (pos1 % 10 == 9 || pos1 % 10 == 8 || pos1 % 10 == 1 || pos1 % 10 == 0);
        int pos2;
        int diff;
        do {
            pos2 = getRandomKrakenPosition();
            diff = Math.abs(pos1 - pos2);
            //Make sure pos is in the middle 6x6 squares || make sure it is not on same vertical, horizontal or diagonal line
        }
        while (pos2 % 10 == 9 || pos2 % 10 == 8 || pos2 % 10 == 1 || pos2 % 10 == 0 || diff % 10 == 0 || pos1 / 10 == pos2 / 10 || diff % 11 == 0 || diff % 9 == 0);
        BitBoard b = new BitBoard(0, 0);
        b.orEquals(new BitBoard(0, 1).leftShift(pos1));
        b.orEquals(new BitBoard(0, 1).leftShift(pos2));
        return b;
    }

    public static int getRandomKrakenPosition() {
        return (int) (Math.random() * 56) + 22;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MyGameState) {
            MyGameState g = (MyGameState) o;
            return g.roteFische == this.roteFische && g.blaueFische == this.blaueFische && g.kraken == this.kraken;
        }
        return false;
    }

    @Override
    public int hashCode() {
        BitBoard b = this.kraken.xOr(this.roteFische).xOr(this.blaueFische);
        return Long.hashCode(b.l1) + Long.hashCode(b.l0);
    }

    @Override
    public MyGameState clone() {
        return new MyGameState(this.roteFische.clone(), this.blaueFische.clone(), this.kraken.clone(), this.move, this.pliesPlayed, this.roundsPlayed);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append("|");
            for (int j = 0; j < 10; j++) {
                //Bit oben links ist das rechteste
                int num = 99 - (i * 10 + j);
                sb.append("\t");
                BitBoard roteFische = this.roteFische.rightShift(num);
                BitBoard blaueFische = this.blaueFische.rightShift(num);
                BitBoard kraken = this.kraken.rightShift(num);
                if ((roteFische.l1 & 1) != 0) {
                    sb.append(StringColor.RED);
                    sb.append("\uD83D\uDC1F");
                } else if ((blaueFische.l1 & 1) != 0) {
                    sb.append(StringColor.BLUE);
                    sb.append("\uD83D\uDC1F");
                } else if ((kraken.l1 & 1) != 0) {
                    sb.append(StringColor.GREEN);
                    sb.append("\uD83D\uDC19");
                }
                sb.append(StringColor.RESET);
                sb.append("\t");
                sb.append("|");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
