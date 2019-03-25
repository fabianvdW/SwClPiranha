package helpers;

import datastructures.BitBoard;
import game.BitBoardConstants;
import game.GameDirection;
import game.MyGameState;

import javax.print.DocFlavor;
import java.util.ArrayList;

public class BitMasks {

    public static void main(String[] args) {
        //System.out.println(BitBoardConstants.RAND);
        //System.out.println(generateZonedBitBoards());
    }

    public static String generateKrakenPositions() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        BitBoard outerIterator = BitBoardConstants.CENTER.clone();
        while (!outerIterator.equalsZero()) {
            int pos1 = outerIterator.numberOfTrailingZeros();
            outerIterator.unsetBitEquals(pos1);
            BitBoard innerIterator = outerIterator.clone();
            while (!innerIterator.equalsZero()) {
                int pos2 = innerIterator.numberOfTrailingZeros();
                innerIterator.unsetBitEquals(pos2);
                if (isValidKrakenPosition(pos1, pos2)) {
                    BitBoard kraken = new BitBoard(0, 1).leftShift(pos1).or(new BitBoard(0, 1).leftShift(pos2));
                    sb.append("new BitBoard(");
                    sb.append(String.format("0x%016x", kraken.l0) + "L,");
                    sb.append(String.format("0x%016x", kraken.l1) + "L");
                    sb.append("),");
                }
            }
        }
        sb.append("};");
        return sb.toString();
    }

    public static boolean isValidKrakenPosition(int pos1, int pos2) {
        int xDiff = pos1 % 10 - pos2 % 10;
        int yDiff = pos1 / 10 - pos2 / 10;
        return pos1 / 10 != pos2 / 10 && pos1 % 10 != pos2 % 10
                && xDiff != yDiff && xDiff != -yDiff;
    }

    public static String generateZonedBitBoards() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < 8; i++) {
            sb.append("{");
            for (int j = 0; j < 100; j++) {
                BitBoard b = BitBoardConstants.NACHBARN[j].clone();
                BitBoard mask = new BitBoard(0, 0);
                switch (i) {
                    case 0: {
                        mask.orEquals(new BitBoard(0, 1).leftShift(j + 11));
                        mask.orEquals(new BitBoard(0, 1).leftShift(j + 10));
                        mask.orEquals(new BitBoard(0, 1).leftShift(j + 9));
                        break;
                    }
                    case 1: {
                        mask.orEquals(new BitBoard(0, 1).leftShift(j + 10));
                        mask.orEquals(new BitBoard(0, 1).leftShift(j + 11));
                        mask.orEquals(new BitBoard(0, 1).leftShift(j + 1));
                        break;
                    }
                    case 2: {
                        mask.orEquals(new BitBoard(0, 1).leftShift(j + 11));
                        mask.orEquals(new BitBoard(0, 1).leftShift(j + 1));
                        if (j > 8) {
                            mask.orEquals(new BitBoard(0, 1).leftShift(j - 9));
                        }
                        break;
                    }
                    case 3: {
                        mask.orEquals(new BitBoard(0, 1).leftShift(j + 1));
                        if (j > 8) {
                            mask.orEquals(new BitBoard(0, 1).leftShift(j - 9));
                        }
                        if (j > 9) {
                            mask.orEquals(new BitBoard(0, 1).leftShift(j - 10));
                        }
                        break;
                    }
                    case 4: {
                        if (j > 8) {
                            mask.orEquals(new BitBoard(0, 1).leftShift(j - 9));
                        }
                        if (j > 9) {
                            mask.orEquals(new BitBoard(0, 1).leftShift(j - 10));
                        }
                        if (j > 10) {
                            mask.orEquals(new BitBoard(0, 1).leftShift(j - 11));
                        }
                        break;
                    }
                    case 5: {
                        if (j > 9) {
                            mask.orEquals(new BitBoard(0, 1).leftShift(j - 10));
                        }
                        if (j > 10) {
                            mask.orEquals(new BitBoard(0, 1).leftShift(j - 11));
                        }
                        if (j > 0) {
                            mask.orEquals(new BitBoard(0, 1).leftShift(j - 1));
                        }
                        break;
                    }
                    case 6: {
                        if (j > 10) {
                            mask.orEquals(new BitBoard(0, 1).leftShift(j - 11));
                        }
                        if (j > 0) {
                            mask.orEquals(new BitBoard(0, 1).leftShift(j - 1));
                        }
                        mask.orEquals(new BitBoard(0, 1).leftShift(j + 9));
                        break;
                    }
                    case 7: {
                        if (j > 0) {
                            mask.orEquals(new BitBoard(0, 1).leftShift(j - 1));
                        }
                        mask.orEquals(new BitBoard(0, 1).leftShift(j + 9));
                        mask.orEquals(new BitBoard(0, 1).leftShift(j + 10));
                        break;
                    }
                }
                b.andEquals(mask);
                sb.append("new BitBoard(");
                sb.append(String.format("0x%016x", b.l0) + "L,");
                sb.append(String.format("0x%016x", b.l1) + "L");
                sb.append("),");
            }
            sb.append("},");
        }
        sb.append("};");
        return sb.toString();
    }

    public static String generateEinheitsUnitsLeftShiftMasksNot() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < 100; i++) {
            BitBoard b = new BitBoard(0, 1).leftShift(i).not();
            sb.append("new BitBoard(");
            sb.append(String.format("0x%016x", b.l0) + "L,");
            sb.append(String.format("0x%016x", b.l1) + "L");
            sb.append("),");
        }
        sb.append("};");
        return sb.toString();
    }

    public static String generateEinheitsUnitsLeftShiftMasks() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < 100; i++) {
            BitBoard b = new BitBoard(0, 1).leftShift(i);
            sb.append("new BitBoard(");
            sb.append(String.format("0x%016x", b.l0) + "L,");
            sb.append(String.format("0x%016x", b.l1) + "L");
            sb.append("),");
        }
        sb.append("};");
        return sb.toString();
    }

    public static String generateRichtungsBitBoardsEinSeitigWithDestinationSquareAttackLine() {
        StringBuilder sb = new StringBuilder();
        //sb.append("{");
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                //sb.append("{");
                int shift = x + y * 10;
                //sb.append("{");
                for (GameDirection direction : GameDirection.values()) {
                    for (int squares = 2; squares <= 9; squares++) {
                        //sb.append("new BitBoard(");
                        BitBoard res = new BitBoard(0, 0);
                        int plusShift = direction.getShift();
                        int lastShift = shift;
                        int newShift = shift + plusShift;
                        int xDiff = Math.abs(lastShift % 10 - newShift % 10);
                        int yDiff = Math.abs(lastShift / 10 - newShift / 10);
                        int count = 1;
                        while (xDiff <= 1 && yDiff <= 1 && newShift >= 0 && newShift <= 99 && count + 1 <= squares) {
                            count++;
                            res.orEquals(new BitBoard(0, 1).leftShift(newShift));
                            lastShift = newShift;
                            newShift += plusShift;
                            xDiff = Math.abs(lastShift % 10 - newShift % 10);
                            yDiff = Math.abs(lastShift / 10 - newShift / 10);
                        }
                        //sb.append(String.format("0x%016x", res.l0) + "L,");
                        sb.append(res.l0 + " ");
                        sb.append(res.l1 + " ");
                        //sb.append(String.format("0x%016x", res.l1) + "L");
                        //sb.append("),");
                    }
                    //sb.append("},");
                }
                //sb.append("},");
            }
        }
        //sb.append("};");
        return sb.toString();
    }

    public static String generateRichtungsBitBoardsEinSeitig() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                sb.append("{");
                int shift = x + y * 10;
                for (GameDirection direction : GameDirection.values()) {
                    sb.append("new BitBoard(");
                    BitBoard res = new BitBoard(0, 0);
                    res.orEquals(new BitBoard(0, 1).leftShift(shift));
                    int plusShift = direction.getShift();
                    int lastShift = shift;
                    int newShift = shift + plusShift;
                    int xDiff = Math.abs(lastShift % 10 - newShift % 10);
                    int yDiff = Math.abs(lastShift / 10 - newShift / 10);
                    while (xDiff <= 1 && yDiff <= 1 && newShift >= 0 && newShift <= 99) {
                        res.orEquals(new BitBoard(0, 1).leftShift(newShift));
                        lastShift = newShift;
                        newShift += plusShift;
                        xDiff = Math.abs(lastShift % 10 - newShift % 10);
                        yDiff = Math.abs(lastShift / 10 - newShift / 10);
                    }
                    sb.append(String.format("0x%016x", res.l0) + "L,");
                    sb.append(String.format("0x%016x", res.l1) + "L");
                    sb.append("),");
                }
                sb.append("},");
            }
        }
        sb.append("};");
        return sb.toString();
    }

    public static String generateRichtungsBitBoardsZweiSeitig() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                sb.append("{");
                int shift = x + y * 10;
                for (int i = 0; i < 4; i++) {
                    sb.append("new BitBoard(");
                    BitBoard res = new BitBoard(0, 0);
                    res.orEquals(new BitBoard(0, 1).leftShift(shift));
                    GameDirection g1 = GameDirection.values()[i];
                    for (int j = 0; j < 2; j++) {
                        int plusShift = g1.getShift() * (j == 0 ? 1 : -1);
                        int lastShift = shift;
                        int newShift = shift + plusShift;
                        int xDiff = Math.abs(lastShift % 10 - newShift % 10);
                        int yDiff = Math.abs(lastShift / 10 - newShift / 10);
                        while (xDiff <= 1 && yDiff <= 1 && newShift >= 0 && newShift <= 99) {
                            res.orEquals(new BitBoard(0, 1).leftShift(newShift));
                            lastShift = newShift;
                            newShift += plusShift;
                            xDiff = Math.abs(lastShift % 10 - newShift % 10);
                            yDiff = Math.abs(lastShift / 10 - newShift / 10);
                        }
                    }
                    sb.append(String.format("0x%016x", res.l0) + "L,");
                    sb.append(String.format("0x%016x", res.l1) + "L");
                    sb.append("),");
                }
                sb.append("},");
            }
        }
        sb.append("};");
        return sb.toString();
    }

    public static String generateNachbarFelder() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                int shift = (j + i * 10);
                BitBoard b = new BitBoard(0, 1);
                BitBoard res = new BitBoard(0, 0);
                sb.append("new BitBoard(");
                for (int c = 0; c < 8; c++) {
                    int newShift = shift;
                    switch (c) {
                        case 0: {
                            //Left
                            newShift++;
                            break;
                        }
                        case 1: {
                            //Right
                            newShift--;
                            break;
                        }
                        case 2: {
                            //Up
                            newShift += 10;
                            break;
                        }
                        case 3: {
                            //Down
                            newShift -= 10;
                            break;
                        }
                        case 4: {
                            //Up-Right
                            newShift += 9;
                            break;
                        }
                        case 5: {
                            //Up-Left
                            newShift += 11;
                            break;
                        }
                        case 6: {
                            //Down-Right
                            newShift -= 11;
                            break;
                        }
                        case 7: {
                            //Down-Left
                            newShift -= 9;
                            break;
                        }
                    }
                    int xDiff = Math.abs(newShift % 10 - shift % 10);
                    int yDiff = Math.abs(newShift / 10 - shift / 10);
                    if (newShift <= 99 && newShift >= 0 && xDiff <= 1 && yDiff <= 1) {
                        res.orEquals(b.leftShift(newShift));
                    }
                }
                sb.append(String.format("0x%016x", res.l0) + "L,");
                sb.append(String.format("0x%016x", res.l1) + "L");
                sb.append("), ");
                System.out.println(res);
            }
        }
        sb.append("};");
        return sb.toString();
    }

    public static String getColumns() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < 10; i++) {
            sb.append("new BitBoard(");
            BitBoard b = new BitBoard(0, 0);
            for (int j = 0; j < 10; j++) {
                int shift = 9 - i + j * 10;
                b.orEquals(new BitBoard(0, 1).leftShift(shift));
            }
            sb.append(String.format("0x%016x", b.l0) + "L,");
            sb.append(String.format("0x%016x", b.l1) + "L");
            sb.append("), ");
        }
        sb.append("};");
        return sb.toString();
    }

    public static String getRows() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < 10; i++) {
            sb.append("new BitBoard(");
            BitBoard b = new BitBoard(0, 0);
            for (int j = 0; j < 10; j++) {
                int shift = i * 10 + j;
                b.orEquals(new BitBoard(0, 1).leftShift(shift));
            }
            sb.append(String.format("0x%016x", b.l0) + "L,");
            sb.append(String.format("0x%016x", b.l1) + "L");
            sb.append("), ");
        }
        sb.append("};");
        return sb.toString();
    }

    public static String getRoteFischeStartingPosition() {
        StringBuilder sb = new StringBuilder();
        sb.append("new BitBoard(");
        BitBoard b = new BitBoard(0, 0);
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                int shift = 99 - (j + i * 10);
                if (i > 0 && i < 9 && (j == 0 || j == 9)) {
                    b.orEquals(new BitBoard(0, 1).leftShift(shift));
                }
            }
        }
        sb.append(String.format("0x%016x", b.l0) + "L,");
        sb.append(String.format("0x%016x", b.l1) + "L");
        sb.append(");");
        return sb.toString();
    }

    public static String getBlaueFischeStartingPosition() {
        StringBuilder sb = new StringBuilder();
        sb.append("new BitBoard(");
        BitBoard b = new BitBoard(0, 0);
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                int shift = 99 - (j + i * 10);
                if (j > 0 && j < 9 && (i == 0 || i == 9)) {
                    b.orEquals(new BitBoard(0, 1).leftShift(shift));
                }
            }
        }
        sb.append(String.format("0x%016x", b.l0) + "L,");
        sb.append(String.format("0x%016x", b.l1) + "L");
        sb.append(");");
        return sb.toString();
    }

    public static String getNBitsFromTheRightMask() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < 65; i++) {
            long l = 0;
            for (int j = 0; j < i; j++) {
                l += 1L << j;
            }
            sb.append(String.format("0x%016x", l) + "L,");
        }
        sb.append("};");
        return sb.toString();
    }

    public static String getNBitsFromTheLeftMask() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < 65; i++) {
            long l = 0;
            for (int j = 0; j < i; j++) {
                l += 1L << (63 - j);
            }
            sb.append(String.format("0x%016x", l) + "L,");
        }
        sb.append("};");
        return sb.toString();
    }
}
