package de.teast.aai;

import base.Graph;
import game.Game;
import game.Player;
import game.map.Castle;
import javafx.util.Pair;

import java.util.*;

/**
 * @author Alexander Muth
 * Class to move troops
 */
public class AAIDistributeTroopsMethods {
    /**
     * @author Alexander Muth
     * Class to save troop movement without moving
     */
    public static class ATroopMover {
        public Castle source, destination;
        public int troopCount;

        /**
         * @param source the source castle
         * @param destination the destination castle
         * @param troopCount the count of troops to move
         */
        public ATroopMover(Castle source, Castle destination, int troopCount){
            this.source = source;
            this.destination = destination;
            this.troopCount = troopCount;
        }

        /**
         * Moves {@link #troopCount} troops from {@link #source} to {@link #destination} if possible
         * @param game the game object where to move the troops
         */
        public void move(Game game){
            if(source == null || destination == null || game == null)
                return;
            if(source.getTroopCount() < troopCount)
                troopCount = source.getTroopCount() - 1;
            if(troopCount <= 1 || source.getOwner() != destination.getOwner())
                return;
            game.moveTroops(source, destination, troopCount);
        }
    }

    /**
     * Generates {@link ATroopMover} objects, to distribute the troops, for the given distribution (only troops of
     * castles in {@code troopDistribution} are used)
     * @param castleGraph the graph, containing all castles and edges
     * @param troopDistribution the castles with the number of distributed troops
     * @param player the player to distribute for
     * @return a list of generated {@link ATroopMover}
     */
    public static List<ATroopMover> generateMoves(Graph<Castle> castleGraph, List<Pair<Castle, Integer>> troopDistribution, Player player){
        List<Castle> castles = new LinkedList<>();
        Map<Castle, Integer> needTroopsHashMap = new HashMap<>();
        Map<Castle, Integer> hasTroopsHashMap = new HashMap<>();
        int temp, sum = 0;
        for(Pair<Castle, Integer> pair : troopDistribution){
            temp = pair.getKey().getTroopCount() - 1 - pair.getValue();
            sum += temp;
            if(temp > 0){ // castle has more troops than needed
                hasTroopsHashMap.put(pair.getKey(), temp);
                castles.add(pair.getKey());
            }else if(temp < 0) { // castle has less troops than needed
                needTroopsHashMap.put(pair.getKey(), Math.abs(temp));
                castles.add(pair.getKey());
            }
        }
        if(castles.isEmpty())
            return new LinkedList<>();

        if(sum != 0){
            System.err.println("Needed Troops isn't equal to usable troops in AAIDistributeTroopsMethods#generateMoves (sum = " + sum + ")");
        }

        List<ATroopMover> returnList = new LinkedList<>();
        List<List<Castle>> connectedList = AAIMethods.getConnectedCastles(castleGraph, castles, player);
        List<Castle> needTroops = new LinkedList<>();
        List<Castle> hasTroops = new LinkedList<>();
        int needValue, hasValue;
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
                    temp = (hasValue > needValue) ? needValue : hasValue;
                    returnList.add(new ATroopMover(next, castle, temp));
                    if(temp == hasValue){
                        hasTroopsHashMap.remove(next);
                        iterator.remove();
                    }else{
                        hasTroopsHashMap.put(next, hasValue - temp);
                    }
                    needValue -= hasValue;
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

    /**
     * Collects all Troops at one castle
     * @param castleGraph the graph containing all castles and edges
     * @param targetCastle target where to collect all troops
     * @return a list of {@link ATroopMover} objects which are necessary to collect all troops in the target castle or null
     */
    public static List<ATroopMover> generateCollectMoves(Graph<Castle> castleGraph, Castle targetCastle){
        if(targetCastle == null || targetCastle.getOwner() == null)
            return null;
        List<ATroopMover> returnList = new LinkedList<>();
        for(Castle castle : AAIMethods.getConnectedCastles(castleGraph, targetCastle)){
            returnList.add(new ATroopMover(castle, targetCastle, castle.getTroopCount()-1));
        }
        return returnList;
    }

    public static void makeMoves(Game game, List<ATroopMover> troopMovers){
        for(ATroopMover troopMover : troopMovers){
            troopMover.move(game);
        }
    }
}
