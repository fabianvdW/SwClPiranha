package helpers;

import game.*;

import java.util.HashMap;

public class Perft {
    static int nodes=0;
    public static void main(String[] args){
        BitBoardConstants.setSquareAttackDirectionSquareDestinationAttackLine();
        GameState g = StringToGameStateConverter.readGameState(StringToGameStateConverter.STANDARD_GAME_STATE);
        long t0=System.currentTimeMillis();
        System.out.println(perft(g,5));
        long t1=System.currentTimeMillis();
        System.out.println("NPS: "+((nodes+0.0)/((t1-t0)/1000)));
    }

    public static int perft(GameState g, int depth){
        if(depth==0){
            return 1;
        }
        nodes++;
        g.analyze();
        if(g.gs!=GameStatus.INGAME){
            return 1;
        }
        int count=0;
        for(int i=0;i<g.possibleFollowingStates.size();i++){
            count+=perft(g.possibleFollowingStates.get(i).clone(),depth-1);
        }
        return count;
    }
}
