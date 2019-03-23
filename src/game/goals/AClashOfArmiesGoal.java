package game.goals;

import de.teast.APath;
import game.Game;
import game.Goal;
import game.Player;
import game.map.Castle;

import java.util.List;

public class AClashOfArmiesGoal extends Goal {
    public AClashOfArmiesGoal(){
        super("Clash of Armies", "Der Spieler, welcher bis zuletzt seine Burg halten kann gewinnt");
    }

    @Override
    public boolean isCompleted() {
        return this.getWinner() != null;
    }
    @Override
    public boolean isCompleted(List<Castle> castles) {
        return this.getWinner(castles) != null;
    }

    @Override
    public Player getWinner() {
        return getWinner(getGame().getMap().getCastles());
    }
    @Override
    public Player getWinner(List<Castle> castles) {
        Player player = null;
        for(Castle castle : castles){
            if(castle.getOwner() == null){
                return null;
            }else if(player == null){
                player = castle.getOwner();
            }else if(player != castle.getOwner()){
                return null;
            }
        }
        return player;
    }

    @Override
    public boolean hasLost(Player player) {
        return player.getNumRegions(getGame()) == 0;
    }

    @Override
    public boolean hasLost(Player player, List<Castle> castles, int round) {
        return player.getNumRegions(castles) == 0;
    }

    public APath getPath(){
        return (APath)getGame().getMap().getGraph();
    }
}
