import datastructures.BitBoard;
import org.junit.Assert;
import org.junit.Test;

public class BitBoardTest {
    @Test
    public void leftShiftTest(){
        long l0= 1;
        long l1= 1;
        BitBoard b= new BitBoard(l0,l1);
        BitBoard shifted= b.leftShift(14);
        Assert.assertEquals(shifted.l1,1L<<14);
        Assert.assertEquals(shifted.l0,1L<<14);
        shifted=b.leftShift(63);
        Assert.assertEquals(shifted.l1,Long.MIN_VALUE);
        Assert.assertEquals(shifted.l0,Long.MIN_VALUE);
        shifted=b.leftShift(64);
        Assert.assertEquals(shifted.l1,0L);
        Assert.assertEquals(shifted.l0,1L);
        l1= Long.MIN_VALUE;
        b= new BitBoard(0L,l1);
        shifted=b.leftShift(1);
        Assert.assertEquals(shifted.l1,0L);
        Assert.assertEquals(shifted.l0,1L);
    }

    @Test
    public void rightShiftTest(){
        long l0=Long.MIN_VALUE;
        long l1=Long.MIN_VALUE;
        BitBoard b= new BitBoard(l0,l1);
        BitBoard shifted= b.rightShift(14);
        Assert.assertEquals(shifted.l0,Long.MIN_VALUE>>>14);
        Assert.assertEquals(shifted.l1,Long.MIN_VALUE>>>14);
        shifted=b.rightShift(63);
        Assert.assertEquals(shifted.l0,1L);
        Assert.assertEquals(shifted.l1,1L);
        shifted=b.rightShift(64);
        Assert.assertEquals(shifted.l0,0L);
        Assert.assertEquals(shifted.l1,Long.MIN_VALUE);
        l0=1;
        l1=0;
        b=new BitBoard(l0,l1);
        shifted=b.rightShift(1);
        Assert.assertEquals(shifted.l0,0L);
        Assert.assertEquals(shifted.l1,Long.MIN_VALUE);
    }
}