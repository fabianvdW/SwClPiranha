package datastructures;

public class BitBoard {
    public final static long[] GET_N_BITS_FROM_RIGHT = {0x0000000000000000L, 0x0000000000000001L, 0x0000000000000003L, 0x0000000000000007L, 0x000000000000000fL, 0x000000000000001fL, 0x000000000000003fL, 0x000000000000007fL, 0x00000000000000ffL, 0x00000000000001ffL, 0x00000000000003ffL, 0x00000000000007ffL, 0x0000000000000fffL, 0x0000000000001fffL, 0x0000000000003fffL, 0x0000000000007fffL, 0x000000000000ffffL, 0x000000000001ffffL, 0x000000000003ffffL, 0x000000000007ffffL, 0x00000000000fffffL, 0x00000000001fffffL, 0x00000000003fffffL, 0x00000000007fffffL, 0x0000000000ffffffL, 0x0000000001ffffffL, 0x0000000003ffffffL, 0x0000000007ffffffL, 0x000000000fffffffL, 0x000000001fffffffL, 0x000000003fffffffL, 0x000000007fffffffL, 0x00000000ffffffffL, 0x00000001ffffffffL, 0x00000003ffffffffL, 0x00000007ffffffffL, 0x0000000fffffffffL, 0x0000001fffffffffL, 0x0000003fffffffffL, 0x0000007fffffffffL, 0x000000ffffffffffL, 0x000001ffffffffffL, 0x000003ffffffffffL, 0x000007ffffffffffL, 0x00000fffffffffffL, 0x00001fffffffffffL, 0x00003fffffffffffL, 0x00007fffffffffffL, 0x0000ffffffffffffL, 0x0001ffffffffffffL, 0x0003ffffffffffffL, 0x0007ffffffffffffL, 0x000fffffffffffffL, 0x001fffffffffffffL, 0x003fffffffffffffL, 0x007fffffffffffffL, 0x00ffffffffffffffL, 0x01ffffffffffffffL, 0x03ffffffffffffffL, 0x07ffffffffffffffL, 0x0fffffffffffffffL, 0x1fffffffffffffffL, 0x3fffffffffffffffL, 0x7fffffffffffffffL, 0xffffffffffffffffL,};

    public final static long[] GET_N_BITS_FROM_LEFT = {0x0000000000000000L, 0x8000000000000000L, 0xc000000000000000L, 0xe000000000000000L, 0xf000000000000000L, 0xf800000000000000L, 0xfc00000000000000L, 0xfe00000000000000L, 0xff00000000000000L, 0xff80000000000000L, 0xffc0000000000000L, 0xffe0000000000000L, 0xfff0000000000000L, 0xfff8000000000000L, 0xfffc000000000000L, 0xfffe000000000000L, 0xffff000000000000L, 0xffff800000000000L, 0xffffc00000000000L, 0xffffe00000000000L, 0xfffff00000000000L, 0xfffff80000000000L, 0xfffffc0000000000L, 0xfffffe0000000000L, 0xffffff0000000000L, 0xffffff8000000000L, 0xffffffc000000000L, 0xffffffe000000000L, 0xfffffff000000000L, 0xfffffff800000000L, 0xfffffffc00000000L, 0xfffffffe00000000L, 0xffffffff00000000L, 0xffffffff80000000L, 0xffffffffc0000000L, 0xffffffffe0000000L, 0xfffffffff0000000L, 0xfffffffff8000000L, 0xfffffffffc000000L, 0xfffffffffe000000L, 0xffffffffff000000L, 0xffffffffff800000L, 0xffffffffffc00000L, 0xffffffffffe00000L, 0xfffffffffff00000L, 0xfffffffffff80000L, 0xfffffffffffc0000L, 0xfffffffffffe0000L, 0xffffffffffff0000L, 0xffffffffffff8000L, 0xffffffffffffc000L, 0xffffffffffffe000L, 0xfffffffffffff000L, 0xfffffffffffff800L, 0xfffffffffffffc00L, 0xfffffffffffffe00L, 0xffffffffffffff00L, 0xffffffffffffff80L, 0xffffffffffffffc0L, 0xffffffffffffffe0L, 0xfffffffffffffff0L, 0xfffffffffffffff8L, 0xfffffffffffffffcL, 0xfffffffffffffffeL, 0xffffffffffffffffL,};

    //Sequenz links
    public long l0;
    //Sequenz rechts
    public long l1;

    //128-Bits aber wir nutzen nur 100
    public BitBoard(long l0, long l1) {
        this.l0 = l0;
        this.l1 = l1;
    }

    public BitBoard and(BitBoard b) {
        return new BitBoard(l0 & b.l0, l1 & b.l1);
    }

    public void andEquals(BitBoard b) {
        l0 &= b.l0;
        l1 &= b.l1;
    }

    public BitBoard or(BitBoard b) {
        return new BitBoard(l0 | b.l0, l1 | b.l1);
    }

    public void orEquals(BitBoard b) {
        l0 |= b.l0;
        l1 |= b.l1;
    }

    public BitBoard not() {
        return new BitBoard(~l0, ~l1);
    }

    public void notEquals() {
        l0 = ~l0;
        l1 = ~l1;
    }

    //amount<=64
    public BitBoard rightShift(int amount) {
        if (amount <= 63) {
            return new BitBoard(l0 >>> amount, l1 >>> amount | ((l0 & GET_N_BITS_FROM_RIGHT[amount]) << (64 - amount)));
        } else {
            return new BitBoard(0, l0 >>> (amount - 64));
        }
    }

    public void rightShiftEquals(int amount) {
        if (amount <= 63) {
            l1 = l1 >>> amount | ((l0 & GET_N_BITS_FROM_RIGHT[amount]) << (64 - amount));
            l0 = l0 >>> amount;
        } else {
            l1 = l0 >>> (amount - 64);
            l0 = 0;
        }
    }

    public BitBoard leftShift(int amount) {
        if (amount <= 63) {
            return new BitBoard(l0 << amount | ((l1 & GET_N_BITS_FROM_LEFT[amount]) >>> (64 - amount)), l1 << amount);
        } else {
            return new BitBoard(l1 << (amount - 64), 0);
        }
    }

    public void leftShiftEquals(int amount) {
        if (amount <= 63) {
            l0 = l0 << amount | ((l1 & GET_N_BITS_FROM_LEFT[amount]) >>> (64 - amount));
            l1 = l1 << amount;
        } else {
            l0 = l1 << (amount - 64);
            l1 = 0;
        }
    }

    public BitBoard xOr(BitBoard b) {
        return new BitBoard(b.l0 ^ l0, b.l1 ^ l1);
    }

    public void xOrEquals(BitBoard b) {
        l0 ^= b.l0;
        l1 ^= b.l1;
    }

    public int popCount() {
        return Long.bitCount(l0) + Long.bitCount(l1);
    }

    public boolean equalsZero() {
        return l1 == 0 && l0 == 0;
    }

    public int numberOfTrailingZeros() {
        int l1Trail = Long.numberOfTrailingZeros(l1);
        if (l1Trail == 64) {
            return 64 + Long.numberOfTrailingZeros(l0);
        } else {
            return l1Trail;
        }
    }

    public BitBoard unsetBit(int bit) {
        if (bit <= 63) {
            return new BitBoard(l0, l1 & ~(1L << bit));
        } else {
            return new BitBoard(l0 & ~(1L << (bit - 64)), l1);
        }
    }

    public void unsetBitEquals(int bit) {
        if (bit <= 63) {
            l1 &= ~(1L << bit);
        } else {
            l0 &= ~(1L << (bit - 64));
        }
    }

    public String getBinaryString() {
        String s = "";
        for (int i = 0; i < Long.numberOfLeadingZeros(l0); i++) {
            s += "0";
        }
        s += Long.toBinaryString(l0);
        for (int i = 0; i < Long.numberOfLeadingZeros(l1); i++) {
            s += "0";
        }
        s += Long.toBinaryString(l1);
        return s;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append("|");
            for (int j = 0; j < 10; j++) {
                //Bit oben links ist das rechteste
                int num = 99 - (i * 10 + j);
                BitBoard rs = this.rightShift(num);
                boolean isSet = (rs.l1 & 1) != 0;
                sb.append("\t");
                if (isSet) {
                    sb.append("X");
                } else {
                    sb.append(" ");
                }
                sb.append(num);
                sb.append("\t");
                sb.append("|");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public BitBoard clone() {
        return new BitBoard(this.l0, this.l1);
    }

}
