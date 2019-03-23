package game.goals;

import base.Edge;
import de.teast.AClustering;
import de.teast.AConstants;
import de.teast.APath;
import de.teast.atroops.ATroop;
import de.teast.atroops.ATroops;
import de.teast.autils.ATriplet;
import game.Goal;
import game.Player;
import game.map.Castle;
import game.map.Kingdom;
import javafx.util.Pair;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class AClashOfArmiesGoal extends Goal {
    Map<Castle, List<ATroops>> castleTroops = new HashMap<>();

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
            if(castle.getOwner() != null && player == null){
                player = castle.getOwner();
            }else if(castle.getOwner() != null && player != castle.getOwner()){
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
    public void generatePath(Castle source, Castle destination){
        int stopCount = ThreadLocalRandom.current().nextInt((int)(AConstants.MIN_STOP_COUNT * (AConstants.STOP_COUNT_PLAYER_MULTIPLIER * getGame().getPlayers().size())),
                                                            (int)((AConstants.MAX_STOP_COUNT + 1) * (AConstants.STOP_COUNT_PLAYER_MULTIPLIER * getGame().getPlayers().size())));
        getPath().generateStops(source, destination, stopCount,getGame().getMap().getScale());
    }
    public void generatePaths(){
        List<Pair<Castle, Castle>> pairs = new LinkedList<>();
        int i=0;
        List<Castle> bases = getBases();
        for(Castle castle : bases){
            for(ListIterator<Castle> iterator = bases.listIterator(++i); iterator.hasNext();){
                pairs.add(new Pair<>(castle, iterator.next()));
            }
        }

        int pathCount;
        List<ATriplet<Castle, Castle, Integer>> pairCount = new LinkedList<>();
        for(Pair<Castle, Castle> pair : pairs){
            pathCount = ThreadLocalRandom.current().nextInt(AConstants.MIN_PATH_COUNT_PER_CASTLES,
                                                            AConstants.MAX_PATH_COUNT_PER_CASTLES + 1);
            pairCount.add(new ATriplet<>(pair.getKey(), pair.getValue(), pathCount));
        }

        while(!pairCount.isEmpty()){
            ATriplet<Castle, Castle, Integer> next;
            for(ListIterator<ATriplet<Castle, Castle, Integer>> iterator = pairCount.listIterator(); iterator.hasNext();){
                next = iterator.next();
                if(next.getThird() <= 0){
                    iterator.remove();
                    continue;
                }
                generatePath(next.getFirst(), next.getSecond());
                next.setThird(next.getThird() - 1);
            }
        }
    }

    public boolean isBase(Castle castle){
        return getPath().getCastleMap().containsKey(castle);
    }
    public void addBase(Castle castle){
        getPath().addCastle(castle);
    }
    public List<Castle> getBases(){
        return new LinkedList<>(getPath().getCastleMap().keySet());
    }
    public List<Castle> getBases(Player player){
        List<Castle> returnList = new LinkedList<>();
        for(Castle base : getBases()){
            if(base.getOwner() == player){
                returnList.add(base);
            }
        }
        return returnList;
    }
    public void generateBases(){
        List<Kingdom> kingdomList = new LinkedList<>();
        Kingdom kingdom;
        int i=0;
        for(Castle base : AClustering.generateBases(getGame().getPlayers(), getGame().getMap().getSize())){
            if(base.getKingdom() != null) {
                kingdom = base.getKingdom();
            }else {
                kingdom = new Kingdom((i++) % 5, base.getLocationOnMap().x, base.getLocationOnMap().y);
                base.setKingdom(kingdom);
            }
            kingdomList.add(kingdom);
            addBase(base);
        }
        getGame().getMap().setKingdoms(kingdomList);
    }

    public List<ATroops> getTroops(Castle castle){
        List<ATroops> returnList = castleTroops.get(castle);
        return (returnList == null) ? new LinkedList<>() : returnList;
    }

    public List<Edge<Castle>> tryMove(Castle source, Castle destination, List<Edge<Castle>> path){
        if(path == null || path.isEmpty())
            return null;
        Castle castleA, castleB;
        for(Edge<Castle> edge : path){
            castleA = edge.getNodeA().getValue();
            castleB = edge.getNodeB().getValue();
            if(castleA != source && castleA != destination && castleA.getOwner() != source.getOwner() && castleA.getOwner() != null){
                return null;
            }else if((castleB != source && castleB != destination && castleB.getOwner() != source.getOwner() && castleB.getOwner() != null)){
                return null;
            }
        }
        int requiredSpeed = path.size();
        for(ATroops troops : getTroops(source)){
            if(troops.troopCount() > 0 && troops.troop().speed >= requiredSpeed){
                return path;
            }
        }
        return null;
    }

    public void move(Castle source, Castle destination, List<Edge<Castle>> path){

    }

    public void addTroops(Castle base){

    }

    public boolean enoughPoints(Player player){
        return false;
    }
}
