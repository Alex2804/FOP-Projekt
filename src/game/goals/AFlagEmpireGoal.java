package game.goals;

import de.teast.AConstants;
import de.teast.aextensions.ajoker.AJoker;
import game.GameConstants;
import game.Goal;
import game.Player;
import game.map.Castle;
import game.map.MapSize;
import game.players.Human;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Alexander Muth
 */
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
        return getWinner(new LinkedList<>(getGame().getMap().getCastles()));
    }

    @Override
    public Player getWinner(List<Castle> castles) {
        if(flags.size() < castles.size()){
            return null;
        }
        List<Player> bestPlayers = new LinkedList<>();
        int flagCount, bestFlagCount = -1;
        for(Player player : getGame().getPlayers()){
            flagCount = getFlags(player).size();
            if(flagCount > bestFlagCount){
                bestPlayers.clear();
                bestPlayers.add(player);
            }else if(flagCount == bestFlagCount){
                bestPlayers.add(player);
            }
        }

        if(bestPlayers.size() == 1){
            return bestPlayers.get(0);
        }else if(bestPlayers.isEmpty()){
            return null;
        }

        int maxPoints = getGame().getPlayers().stream().mapToInt(Player::getPoints).max().orElse(-1);
        bestPlayers = getGame().getPlayers().stream().filter(p -> p.getPoints() >= maxPoints).collect(Collectors.toList());
        if(bestPlayers.size() == 1){
            return bestPlayers.get(0); // return player with most points
        }

        return getGame().getPlayers().stream().max(Comparator.comparingInt(p -> p.getCastles(getGame()).size())).orElse(null); // return player with most castles (or any player)
    }

    @Override
    public boolean hasLost(Player player) {
        return hasLost(player, getGame().getMap().getCastles(), getGame().getRound());
    }

    @Override
    public boolean hasLost(Player player, List<Castle> castles, int round) {
        return player.getCastles(castles).isEmpty() && round > 1;
    }

    @Override
    public MapSize[] getSupportedMapSizes() {
        return new MapSize[]{MapSize.SMALL, MapSize.MEDIUM, MapSize.LARGE};
    }
    @Override
    public AJoker[] getSupportedJokers() {
        return AConstants.FLAG_EMPIRE_JOKERS;
    }
    @Override
    public Class<?>[] getSupportedPlayerTypes() {
        return new Class[]{Human.class};
    }
    @Override
    public int getMaxPlayerCount() {
        return 4;
    }

    /**
     * @param castle the castle to get the flag
     * @return the Player, which has placed a flag on {@code castle}
     */
    public Player getFlag(Castle castle){
        return flags.get(castle);
    }
    /**
     *
     * @param player the player to get all flags for
     * @return all castles where the {@code player} has placed a flag
     */
    public List<Castle> getFlags(Player player){
        List<Castle> returnList = new LinkedList<>();
        for(Castle castle : flags.keySet()){
            if(castle.getOwner() == player){
                returnList.add(castle);
            }
        }
        return returnList;
    }

    /**
     * @param castle the castle to check
     * @return if there is a flag at the passed {@code castle}
     */
    public boolean isFlagSet(Castle castle){
        return getFlag(castle) != null;
    }
    /**
     * @param castle the castle to place the flag at
     * @param player the player to place the flag for
     * @return if the flag was set
     */
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
