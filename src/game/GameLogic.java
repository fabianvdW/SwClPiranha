package game;

import datastructures.BitBoard;

public class GameLogic {

    public final static BitBoard[] NACHBARN={new BitBoard(0x0000000000000000L,0x0000000000000c02L), new BitBoard(0x0000000000000000L,0x0000000000001c05L), new BitBoard(0x0000000000000000L,0x000000000000380aL), new BitBoard(0x0000000000000000L,0x0000000000007014L), new BitBoard(0x0000000000000000L,0x000000000000e028L), new BitBoard(0x0000000000000000L,0x000000000001c050L), new BitBoard(0x0000000000000000L,0x00000000000380a0L), new BitBoard(0x0000000000000000L,0x0000000000070140L), new BitBoard(0x0000000000000000L,0x00000000000e0280L), new BitBoard(0x0000000000000000L,0x00000000000c0100L), new BitBoard(0x0000000000000000L,0x0000000000300803L), new BitBoard(0x0000000000000000L,0x0000000000701407L), new BitBoard(0x0000000000000000L,0x0000000000e0280eL), new BitBoard(0x0000000000000000L,0x0000000001c0501cL), new BitBoard(0x0000000000000000L,0x000000000380a038L), new BitBoard(0x0000000000000000L,0x0000000007014070L), new BitBoard(0x0000000000000000L,0x000000000e0280e0L), new BitBoard(0x0000000000000000L,0x000000001c0501c0L), new BitBoard(0x0000000000000000L,0x00000000380a0380L), new BitBoard(0x0000000000000000L,0x0000000030040300L), new BitBoard(0x0000000000000000L,0x00000000c0200c00L), new BitBoard(0x0000000000000000L,0x00000001c0501c00L), new BitBoard(0x0000000000000000L,0x0000000380a03800L), new BitBoard(0x0000000000000000L,0x0000000701407000L), new BitBoard(0x0000000000000000L,0x0000000e0280e000L), new BitBoard(0x0000000000000000L,0x0000001c0501c000L), new BitBoard(0x0000000000000000L,0x000000380a038000L), new BitBoard(0x0000000000000000L,0x0000007014070000L), new BitBoard(0x0000000000000000L,0x000000e0280e0000L), new BitBoard(0x0000000000000000L,0x000000c0100c0000L), new BitBoard(0x0000000000000000L,0x0000030080300000L), new BitBoard(0x0000000000000000L,0x0000070140700000L), new BitBoard(0x0000000000000000L,0x00000e0280e00000L), new BitBoard(0x0000000000000000L,0x00001c0501c00000L), new BitBoard(0x0000000000000000L,0x0000380a03800000L), new BitBoard(0x0000000000000000L,0x0000701407000000L), new BitBoard(0x0000000000000000L,0x0000e0280e000000L), new BitBoard(0x0000000000000000L,0x0001c0501c000000L), new BitBoard(0x0000000000000000L,0x000380a038000000L), new BitBoard(0x0000000000000000L,0x0003004030000000L), new BitBoard(0x0000000000000000L,0x000c0200c0000000L), new BitBoard(0x0000000000000000L,0x001c0501c0000000L), new BitBoard(0x0000000000000000L,0x00380a0380000000L), new BitBoard(0x0000000000000000L,0x0070140700000000L), new BitBoard(0x0000000000000000L,0x00e0280e00000000L), new BitBoard(0x0000000000000000L,0x01c0501c00000000L), new BitBoard(0x0000000000000000L,0x0380a03800000000L), new BitBoard(0x0000000000000000L,0x0701407000000000L), new BitBoard(0x0000000000000000L,0x0e0280e000000000L), new BitBoard(0x0000000000000000L,0x0c0100c000000000L), new BitBoard(0x0000000000000000L,0x3008030000000000L), new BitBoard(0x0000000000000000L,0x7014070000000000L), new BitBoard(0x0000000000000000L,0xe0280e0000000000L), new BitBoard(0x0000000000000001L,0xc0501c0000000000L), new BitBoard(0x0000000000000003L,0x80a0380000000000L), new BitBoard(0x0000000000000007L,0x0140700000000000L), new BitBoard(0x000000000000000eL,0x0280e00000000000L), new BitBoard(0x000000000000001cL,0x0501c00000000000L), new BitBoard(0x0000000000000038L,0x0a03800000000000L), new BitBoard(0x0000000000000030L,0x0403000000000000L), new BitBoard(0x00000000000000c0L,0x200c000000000000L), new BitBoard(0x00000000000001c0L,0x501c000000000000L), new BitBoard(0x0000000000000380L,0xa038000000000000L), new BitBoard(0x0000000000000701L,0x4070000000000000L), new BitBoard(0x0000000000000e02L,0x80e0000000000000L), new BitBoard(0x0000000000001c05L,0x01c0000000000000L), new BitBoard(0x000000000000380aL,0x0380000000000000L), new BitBoard(0x0000000000007014L,0x0700000000000000L), new BitBoard(0x000000000000e028L,0x0e00000000000000L), new BitBoard(0x000000000000c010L,0x0c00000000000000L), new BitBoard(0x0000000000030080L,0x3000000000000000L), new BitBoard(0x0000000000070140L,0x7000000000000000L), new BitBoard(0x00000000000e0280L,0xe000000000000000L), new BitBoard(0x00000000001c0501L,0xc000000000000000L), new BitBoard(0x0000000000380a03L,0x8000000000000000L), new BitBoard(0x0000000000701407L,0x0000000000000000L), new BitBoard(0x0000000000e0280eL,0x0000000000000000L), new BitBoard(0x0000000001c0501cL,0x0000000000000000L), new BitBoard(0x000000000380a038L,0x0000000000000000L), new BitBoard(0x0000000003004030L,0x0000000000000000L), new BitBoard(0x000000000c0200c0L,0x0000000000000000L), new BitBoard(0x000000001c0501c0L,0x0000000000000000L), new BitBoard(0x00000000380a0380L,0x0000000000000000L), new BitBoard(0x0000000070140700L,0x0000000000000000L), new BitBoard(0x00000000e0280e00L,0x0000000000000000L), new BitBoard(0x00000001c0501c00L,0x0000000000000000L), new BitBoard(0x0000000380a03800L,0x0000000000000000L), new BitBoard(0x0000000701407000L,0x0000000000000000L), new BitBoard(0x0000000e0280e000L,0x0000000000000000L), new BitBoard(0x0000000c0100c000L,0x0000000000000000L), new BitBoard(0x0000000008030000L,0x0000000000000000L), new BitBoard(0x0000000014070000L,0x0000000000000000L), new BitBoard(0x00000000280e0000L,0x0000000000000000L), new BitBoard(0x00000000501c0000L,0x0000000000000000L), new BitBoard(0x00000000a0380000L,0x0000000000000000L), new BitBoard(0x0000000140700000L,0x0000000000000000L), new BitBoard(0x0000000280e00000L,0x0000000000000000L), new BitBoard(0x0000000501c00000L,0x0000000000000000L), new BitBoard(0x0000000a03800000L,0x0000000000000000L), new BitBoard(0x0000000403000000L,0x0000000000000000L), };

    public static int getAnzahlFische(GameState gs, GameColor gc) {
        if (gc == GameColor.RED) {
            return gs.roteFische.popCount();
        } else {
            return gs.blaueFische.popCount();
        }
    }

    public static int getSchwarm(GameState gs, GameColor gc){
        BitBoard meineFische;
        if(gc==GameColor.RED){
            meineFische=gs.roteFische;
        }else{
            meineFische=gs.blaueFische;
        }
        if(meineFische.equalsZero()){
            return 0;
        }
        int result=0;
        int fischBit;
        BitBoard neighboringFields= new BitBoard(0,0);
        BitBoard neighboringFieldsAndMeineFische=meineFische;
        do{
            result++;
            fischBit = neighboringFieldsAndMeineFische.numberOfTrailingZeros();
            meineFische=meineFische.unsetBit(fischBit);
            neighboringFields.orEquals(GameLogic.NACHBARN[fischBit]);
            neighboringFieldsAndMeineFische= neighboringFields.and(meineFische);
        }while (!neighboringFieldsAndMeineFische.equalsZero());
        return result;
    }
}
