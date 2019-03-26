package game.goals;

import de.teast.AConstants;
import de.teast.aextensions.ajoker.AJoker;
import game.GameConstants;
import game.Goal;
import game.Player;
import game.map.Castle;
import game.map.MapSize;
import game.players.Human;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Alexander Muth
 */
public class ACaptureTheFlagGoal extends Goal {
    private static final boolean adoptCastles = false;

    protected Map<Player, Castle> flagPlayerCastleMap = new HashMap<>();
    protected Map<Castle, Player> flagCastlePlayerMap = new HashMap<>();

    public ACaptureTheFlagGoal(){
        super("Eroberung der Flagge", "Der Spieler, welcher als letztes noch seine Flagge hat gewinnt");
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
        if(getGame().getRound() < 2)
            return null;
        for(Map.Entry<Player, Castle> entry : flagPlayerCastleMap.entrySet()){
            if(entry.getKey() != entry.getValue().getOwner() || !castles.contains(entry.getValue())) {
                flagPlayerCastleMap.remove(entry.getKey());
                flagCastlePlayerMap.remove(entry.getValue());
            }
        }
        if(flagPlayerCastleMap.size() == 1)
            return (Player)flagPlayerCastleMap.keySet().toArray()[0];
        else
            return null;
    }

    @Override
    public boolean hasLost(Player player) {
        return hasLost(player, getGame().getMap().getCastles(), getGame().getRound());
    }
    @Override
    public boolean hasLost(Player player, List<Castle> castles, int round) {
        if(round < 2)
            return false;
        Castle castle = flagPlayerCastleMap.get(player);
        return castle == null || castle.getOwner() != player || !castles.contains(castle);
    }

    @Override
    public MapSize[] getSupportedMapSizes() {
        return new MapSize[]{MapSize.SMALL, MapSize.MEDIUM, MapSize.LARGE};
    }
    @Override
    public AJoker[] getSupportedJokers() {
        return AConstants.CAPTURE_THE_FLAG_JOKERS;
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
     * Choose castle of player for the flag if there is none
     * @param player the player
     * @param castle the castle
     */
    public void chooseCastleForFlag(Player player, Castle castle){
        if(castle.getOwner() != player || flagPlayerCastleMap.containsKey(player))
            return;
        flagPlayerCastleMap.put(player, castle);
        flagCastlePlayerMap.put(castle, player);

        getGame().getGameInterface().onLogText("%PLAYER% hat seine Flagge platziert!", player);
        if(player.getRemainingTroops() <= 0)
            getGame().nextTurn();
    }

    /**
     * Remove a conquered castle from the Maps, if there was the flag
     * @param conqueredCastle the conquered castle
     * @param conqueror the player who has conquerored the castle
     */
    public void conquered(Castle conqueredCastle, Player conqueror){
        Player player = flagCastlePlayerMap.get(conqueredCastle);
        if(player != null) {
            flagCastlePlayerMap.remove(conqueredCastle);
            flagPlayerCastleMap.remove(player);
            if (adoptCastles){
                getGame().getGameInterface().onLogText("%PLAYER% hat die Flagge von %PLAYER% erobert und übernimmt alle Burgen!", conqueror, player);
            }else{
                getGame().getGameInterface().onLogText("%PLAYER% hat seine Flaggen verloren und ist ausgeschieden!", player);
            }
            List<Castle> castles = player.getCastles(getGame());
            for(Castle castle : castles){
                if(adoptCastles) {
                    castle.removeTroops(castle.getTroopCount() - 1);
                    castle.setOwner(conqueror);
                }else{
                    castle.removeTroops(castle.getTroopCount());
                    castle.setOwner(null);
                }
            }
            getGame().getGameInterface().onUpdate();
        }
    }

    /**
     * @return A list containing all Castles with flags
     */
    public List<Castle> getFlagsChoosen(){
        List<Castle> returnList = new LinkedList<>();
        for(Map.Entry<Castle, Player> entry : flagCastlePlayerMap.entrySet()){
            returnList.add(entry.getKey());
        }
        return returnList;
    }

    /**
     * @param player the player to check
     * @return if a player has chosen a castle for the flag
     */
    public boolean hasChosen(Player player){
        return flagPlayerCastleMap.containsKey(player);
    }
}
