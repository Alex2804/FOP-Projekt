package de.teast.aai;

import base.Graph;
import game.Game;
import game.Player;
import game.map.Castle;
import javafx.util.Pair;

import java.util.*;

/**
 * @author Alexander Muth
 * Class to move troops depending on the situation
 */
public class AAIDistributeTroopsMethods {
    /**
     * @author Alexander Muth
     * Class to save troop movement without moving
     */
    public static class TroopMover{
        public Castle source, destination;
        public int troopCount;

        /**
         * @param source the source castle
         * @param destination the destination castle
         * @param troopCount the count of troops to move
         */
        public TroopMover(Castle source, Castle destination, int troopCount){
            this.source = source;
            this.destination = destination;
            this.troopCount = troopCount;
        }

        /**
         * @param game the game object where to move the troops
         * @return if the troops where moved successfully
         */
        public boolean move(Game game){
            if(source == null || destination == null || game == null)
                return false;
            if(source.getTroopCount() < troopCount)
                troopCount = source.getTroopCount() - 1;
            if(troopCount <= 1 || source.getOwner() != destination.getOwner())
                return false;
            game.moveTroops(source, destination, troopCount);
            return true;
        }
    }

    /**
     * Moves the troops for defense
     * @param game the game object
     * @param player the player to move the troops for
     */
    public static void moveDefenseTroops(Game game, Player player){
        for(TroopMover mover : distributeDefenseTroops(game.getMap().getGraph(), player)){
            mover.move(game);
        }
    }

    /**
     * @param castleGraph the graph, containing all castles and edges
     * @param player the player to distribute for
     * @return a List of {@link TroopMover}, to move the Troops in the optimal way
     */
    public static List<TroopMover> distributeDefenseTroops(Graph<Castle> castleGraph, Player player){
        List<Pair<Castle, Integer>> troopDistribution = AAIDefenseEvalMethods.getBestTroopDistribution(castleGraph, player);

        return generateMoves(castleGraph, troopDistribution, player);
    }

    /**
     * Generates {@link TroopMover} objects, to distribute the troops, for the given distribution
     * @param castleGraph the graph, containing all castles and edges
     * @param troopDistribution the castles with the number of distributed troops
     * @param player the player to distribute for
     * @return a list of generated {@link TroopMover}
     */
    public static List<TroopMover> generateMoves(Graph<Castle> castleGraph, List<Pair<Castle, Integer>> troopDistribution, Player player){
        List<Castle> castles = new LinkedList<>();
        Map<Castle, Integer> needTroopsHashMap = new HashMap<>();
        Map<Castle, Integer> hasTroopsHashMap = new HashMap<>();
        int temp, sum = 0;
        for(Pair<Castle, Integer> pair : troopDistribution){
            temp = pair.getKey().getTroopCount() - pair.getValue();
            sum += temp;
            if(temp > 0){ // castle has more troops than needed
                hasTroopsHashMap.put(pair.getKey(), temp);
                castles.add(pair.getKey());
            }else if(temp < 0) { // castle has less troops than needed
                needTroopsHashMap.put(pair.getKey(), temp);
                castles.add(pair.getKey());
            }
        }

        if(sum != 0){
            System.err.println("Needed Troops isn't equal to usable troops in AAIDistributeTroopsMethods#generateMoves (sum = " + sum + ")");
        }

        List<TroopMover> returnList = new LinkedList<>();
        List<List<Castle>> connectedList = AAIMethods.getConnectedCastles(castleGraph, castles, player);
        List<Castle> needTroops = new LinkedList<>();
        List<Castle> hasTroops = new LinkedList<>();
        Integer hasValue;
        int needValue;
        ListIterator<Castle> iterator;
        Castle next;
        for(List<Castle> connectedCastles : connectedList){
            needTroops.clear();
            hasTroops.clear();
            for(Castle castle : connectedCastles){
                if(hasTroopsHashMap.containsKey(castle)){
                    hasTroops.add(castle);
                }else{
                    needTroops.add(castle);
                }
            }

            for(Castle castle : needTroops){
                needValue = needTroopsHashMap.get(castle);

                iterator = hasTroops.listIterator();
                if(!iterator.hasNext()){
                    System.err.println("No Castle with troops anymore");
                    return null;
                }
                while(iterator.hasNext()){
                    next = iterator.next();
                    hasValue = hasTroopsHashMap.get(next);
                    if(hasValue != null){
                        temp = (hasValue > needValue) ? needValue : hasValue;
                        returnList.add(new TroopMover(castle, next, temp));
                        if(temp == hasValue){
                            hasTroopsHashMap.remove(next);
                            iterator.remove();
                        }else{
                            hasTroopsHashMap.put(next, hasValue - temp);
                        }
                        needValue -= hasValue;
                    }
                }

                if(needValue > 0){
                    System.err.println("Not enough Troops ("+ needTroopsHashMap.get(castle) +") for castle: "  + castle.getName() + " (" + castle + ")");
                    return null;
                }
                needTroopsHashMap.remove(castle);
            }
        }


        return returnList;
    }
}
