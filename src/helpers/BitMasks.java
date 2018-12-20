package helpers;

public class BitMasks {

    public static void main(String[] args){
        System.out.println(getNBitsFromTheLeftMask());
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
