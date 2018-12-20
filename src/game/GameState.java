package game;

import datastructures.BitBoard;

public class GameState {
    public BitBoard roteFische;
    public BitBoard blaueFische;
    public BitBoard kraken;

    public GameState() {
        //Pre calculated values from BitMasks
        this.roteFische = new BitBoard(0x0000000002018060L, 0x1806018060180400L);
        this.blaueFische = new BitBoard(0x00000007f8000000L, 0x00000000000001feL);
        this.kraken = GameState.generateRandomKraken();
    }

    public GameState(BitBoard kraken) {
        this();
        this.kraken = kraken;
    }

    public GameState(BitBoard roteFische, BitBoard blaueFische, BitBoard kraken) {
        //Make sure those BitBoards get cloned
        this.roteFische = roteFische;
        this.blaueFische = blaueFische;
        this.kraken = kraken;
    }

    public static BitBoard generateRandomKraken() {
        int pos1;
        do{
           pos1=getRandomKrakenPosition();
            //Make sure pos is in the middle 6x6 squares
        }while(pos1%10==9||pos1%10==8||pos1%10==1||pos1%10==0);
        int pos2;
        int diff;
        do{
            pos2= getRandomKrakenPosition();
            diff=Math.abs(pos1-pos2);
            //Make sure pos is in the middle 6x6 squares || make sure it is not on same vertical, horizontal or diagonal line
        }while(pos2%10==9||pos2%10==8||pos2%10==1||pos2%10==0||diff%10==0||pos1-pos1%10==pos2-pos2%10||diff%11==0||diff%9==0);
        BitBoard b= new BitBoard(0,0);
        b.orEquals(new BitBoard(0,1).leftShift(pos1));
        b.orEquals(new BitBoard(0,1).leftShift(pos2));
        return b;
    }

    public static int getRandomKrakenPosition() {
        return (int) (Math.random() * 56) + 22;
    }

}
