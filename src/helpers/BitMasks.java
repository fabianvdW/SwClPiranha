package helpers;

import datastructures.BitBoard;

public class BitMasks {

    public static void main(String[] args){
        System.out.println(getBlaueFischeStartingPosition());
    }

    public static String getRoteFischeStartingPosition(){
        StringBuilder sb= new StringBuilder();
        sb.append("new BitBoard(");
        BitBoard b = new BitBoard(0,0);
        for(int i=0;i<10;i++){
            for(int j=0;j<10;j++){
                int shift=99-(j +i*10);
                if(i>0 && i<9 && (j==0||j==9)){
                    b.orEquals(new BitBoard(0,1).leftShift(shift));
                }
            }
        }
        sb.append(String.format("0x%016x",b.l0)+ "L,");
        sb.append(String.format("0x%016x",b.l1)+ "L");
        sb.append(");");
        return sb.toString();
    }

    public static String getBlaueFischeStartingPosition(){
        StringBuilder sb= new StringBuilder();
        sb.append("new BitBoard(");
        BitBoard b = new BitBoard(0,0);
        for(int i=0;i<10;i++){
            for(int j=0;j<10;j++){
                int shift=99-(j +i*10);
                if(j>0 && j<9 && (i==0||i==9)){
                    b.orEquals(new BitBoard(0,1).leftShift(shift));
                }
            }
        }
        sb.append(String.format("0x%016x",b.l0)+ "L,");
        sb.append(String.format("0x%016x",b.l1)+ "L");
        sb.append(");");
        return sb.toString();
    }
    public static String getNBitsFromTheRightMask(){
        StringBuilder sb= new StringBuilder();
        sb.append("{");
        for(int i=0;i<65;i++){
            long l= 0;
            for(int j=0;j<i;j++){
                l+=1L<<j;
            }
            sb.append(String.format("0x%016x",l)+ "L,");
        }
        sb.append("};");
        return sb.toString();
    }

    public static String getNBitsFromTheLeftMask(){
        StringBuilder sb= new StringBuilder();
        sb.append("{");
        for(int i=0;i<65;i++){
            long l= 0;
            for(int j=0;j<i;j++){
                l+=1L<<(63-j);
            }
            sb.append(String.format("0x%016x",l)+ "L,");
        }
        sb.append("};");
        return sb.toString();
    }
}
