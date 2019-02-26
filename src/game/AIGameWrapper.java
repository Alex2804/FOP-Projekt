package game;

import base.Graph;
import game.map.Castle;

import java.util.List;
import java.util.Queue;

/**
 * @author Alexander Muth
 * This class is a wrapper for the game class for AI algorithm
 * It is only for the conquer goal!
 */
public class AIGameWrapper {

    private Game game;
    private List<Player> players;
    private int round;
    private Queue<Player> playerQueue;
    private Player startingPlayer;
    private Player currentPlayer;
    private Graph<Castle> graph;


    public AIGameWrapper(Game game){

    }

    public static AIGameWrapper getWrapper(Game game) {
        return null;
    }

    private void initWrapper(Game game){
        if(game == null)
            return;
        for(Player player : game.getPlayers()){
            players.add(player.copy());
        }
    }
}
