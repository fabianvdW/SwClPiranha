package helpers;

import datastructures.BitBoard;

public class BitMasks {

    public static void main(String[] args) {
        System.out.println(generateNachbarFelder());
    }

    public static String generateNachbarFelder() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                int shift =(j + i * 10);
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
                    int xDiff= Math.abs(newShift%10-shift%10);
                    int yDiff= Math.abs(newShift/10-shift/10);
                    if (newShift <= 99 && newShift >= 0&&xDiff<=1&&yDiff<=1) {
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
