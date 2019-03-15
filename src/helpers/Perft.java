package helpers;

import game.*;

public class Perft {
    static int nodes=0;
    public static void main(String[] args){
        BitBoardConstants.setSquareAttackDirectionSquareDestinationAttackLine("SwClPiranha/src/game/data.txt");
        MyGameState g = StringToGameStateConverter.readGameState(StringToGameStateConverter.STANDARD_GAME_STATE);
        long t0=System.currentTimeMillis();
        System.out.println(perft(g,5));
        //Perft 5: 250613480
        //Perft 6
        long t1=System.currentTimeMillis();
        System.out.println("Time: "+(t1-t0));
        System.out.println("NPS: "+((nodes+0.0)/((t1-t0+0.0)/1000.0)));
    }

    public static int perft(MyGameState g, int depth){
        if(depth==0){
            return 1;
        }
        nodes++;
        g.analyze();
        if(g.gs!=GameStatus.INGAME){
            return 1;
        }
        int count=0;
        GameMoveResultObject gmro= g.gmro;
        g.gmro=null;
        for(int i=0;i<gmro.instances;i++){
            count+=perft(gmro.states[i],depth-1);
        }
        return count;
    }
}
