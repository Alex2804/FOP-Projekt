package game.goals;

import de.teast.AConstants;
import game.Goal;
import game.Player;
import game.map.Castle;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AFlagEmpireGoal extends Goal {
    Map<Castle, Player> flags = new HashMap<>();

    public AFlagEmpireGoal(){
        super("Flaggen Imperium", "Der Spieler, welcher am Ende die meisten Flaggen hat gewinnt. Sollten 2 oder mehr Spieler die gleiche Anzahl an Flaggen haben, gewinnt der Spieler mit mehr Punkten");
    }

    @Override
    public boolean isCompleted() {
        return flags.size() >= getGame().getMap().getGraph().getNodes().size();
    }

    @Override
    public boolean isCompleted(List<Castle> castles) {
        for(Castle castle : castles){
            if(!flags.containsKey(castle)){
                return false;
            }
        }
        return true;
    }

    @Override
    public Player getWinner() {
        return getWinner(new LinkedList<>(flags.keySet()));
    }

    @Override
    public Player getWinner(List<Castle> castles) {
        Player player = null;
        for(Castle castle : flags.keySet()){
            if((castle.getOwner() == null) || (player != null && player != castle.getOwner()))
                return null;
            else if(player == null)
                player = castle.getOwner();
        }
        return player;
    }

    @Override
    public boolean hasLost(Player player) {
        return hasLost(player, getGame().getMap().getCastles(), getGame().getRound());
    }

    @Override
    public boolean hasLost(Player player, List<Castle> castles, int round) {
        return player.getCastles(castles).isEmpty() && round > 1;
    }

    public Player getFlag(Castle castle){
        return flags.get(castle);
    }
    public List<Castle> getFlags(Player player){
        List<Castle> returnList = new LinkedList<>();
        for(Castle castle : flags.keySet()){
            if(castle.getOwner() == player){
                returnList.add(castle);
            }
        }
        return returnList;
    }

    public boolean isFlagSet(Castle castle){
        return getFlag(castle) != null;
    }
    public boolean setFlag(Castle castle, Player player){
        if(isFlagSet(castle) || player.getPoints() < AConstants.FLAG_POINTS)
            return false;
        flags.put(castle, player);
        player.addPoints(-AConstants.FLAG_POINTS);
        getGame().getGameInterface().onLogText("%PLAYER% platziert eine Flagge auf " + castle.getName() + " für " + AConstants.FLAG_POINTS + " Punkte.", player);
        getGame().getGameInterface().onUpdate();
        return true;
    }
}
