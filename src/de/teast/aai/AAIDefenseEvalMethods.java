package de.teast.aai;

import base.Graph;
import game.Game;
import game.Player;
import game.map.Castle;
import javafx.util.Pair;

import java.util.*;

/**
 * Class to move troops for defense dependent on the situation
 * @author Alexander Muth
 */
public class AAIDefenseEvalMethods {
    public static AAIConstants constants = new AAIConstants();

    /**
     * Moves the TROOPS for defense
     * @param game the game object
     * @param player the player to move the TROOPS for
     */
    public static void moveDefenseTroops(Game game, Player player){
        AAIDistributeTroopsMethods.makeMoves(game, distributeDefenseTroops(game.getMap().getGraph(), player));
    }

    /**
     * @param castleGraph the graph, containing all castles and edges
     * @param player the player to distribute for
     * @return a List of {@link AAIDistributeTroopsMethods.ATroopMover}, to move the Troops in the optimal way
     */
    public static List<AAIDistributeTroopsMethods.ATroopMover> distributeDefenseTroops(Graph<Castle> castleGraph, Player player){
        List<Pair<Castle, Integer>> troopDistribution = getBestDefenseTroopDistribution(castleGraph, player);
        return AAIDistributeTroopsMethods.generateMoves(castleGraph, troopDistribution, player);
    }

    /**
     * @param castleGraph the graph containing all castles and edges
     * @param player the player to distribute for
     * @return a list of pairs, with the castles as keys and the best troop count for every castle as value
     */
    public static List<Pair<Castle, Integer>> getBestDefenseTroopDistribution(Graph<Castle> castleGraph, Player player){
        List<List<Castle>> connectedCastles = AAIMethods.getConnectedCastles(castleGraph, player);

        List<Pair<Castle, Integer>> returnList = new LinkedList<>();
        for(List<Castle> connected : connectedCastles){
            returnList.addAll(evalConnectedTroopDistribution(castleGraph, connected));
        }
        return returnList;
    }
    /**
     * Evaluates the troop distribution for connected castles (the troop which must be at every castle is not included!).
     * @param castleGraph graph containing all edges and nodes
     * @param castles a list of connected castles
     * @return a list of {@link Pair}, with the castles as key and troop count as values
     */
    public static List<Pair<Castle, Integer>> evalConnectedTroopDistribution(Graph<Castle> castleGraph, List<Castle> castles){
        List<Pair<Castle, Double>> evalValueList = new LinkedList<>();
        double tempValue, valueSum = 0;
        int troopCount = AAIMethods.getAttackTroopCount(castles);

        for(Castle castle : castles){
            tempValue = evaluateCastle(castleGraph, castle);
            if(tempValue > 0) {
                evalValueList.add(new Pair<>(castle, tempValue));
                valueSum += tempValue;
            }
        }
        evalValueList.sort(Comparator.comparingDouble(Pair::getValue));
        Collections.reverse(evalValueList);

        Map<Castle, Integer> castleTroopCountMap = new HashMap<>();
        double percentage;
        int troopCountTemp;
        for(Pair<Castle, Double> pair : evalValueList){
            percentage = pair.getValue() / valueSum;
            troopCountTemp = (int)(percentage * troopCount);
            castleTroopCountMap.put(pair.getKey(), troopCountTemp);
            troopCount -= troopCountTemp;
        }
        while(troopCount > 0 && !evalValueList.isEmpty()) {
            for(Pair<Castle, Double> pair : evalValueList){
                if(troopCount <= 0)
                    break;
                castleTroopCountMap.put(pair.getKey(), castleTroopCountMap.get(pair.getKey()) + 1);
                --troopCount;
            }
        }
        if(evalValueList.isEmpty()){
            for(Castle castle : castles){
                if(castle.getTroopCount() > 1){
                    castleTroopCountMap.put(castle, castle.getTroopCount() - 1);
                    troopCount -= castle.getTroopCount() - 1;
                }
            }
        }

        List<Pair<Castle, Integer>> returnList = new LinkedList<>();
        for(Map.Entry<Castle, Integer> entry : castleTroopCountMap.entrySet()){
            returnList.add(new Pair<>(entry.getKey(), entry.getValue()));
            castles.remove(entry.getKey());
        }
        for(Castle castle : castles){
            returnList.add(new Pair<>(castle, 0));
        }
        return returnList;
    }

    /**
     * evaluates a value, how important the defense of this castle is. The higher the value the higher is the importance
     * @param castleGraph the graph containing all nodes and edges
     * @param castle the castle to calculate the value for
     * @return the calculated value
     */
    public static double evaluateCastle(Graph<Castle> castleGraph, Castle castle){
        double points = 0;

        points += (getEnemyEdgeCount(castleGraph, castle) * constants.EDGE_COUNT_MULTIPLIER);
        points += (getThreateningNeighboursTroopCount(castleGraph, castle) * constants.THREATENING_TROOP_COUNT_MULTIPLIER);

        return points;
    }

    /**
     * @param castleGraph graph containing all edges and nodes (neighbours)
     * @param castle the castle to check
     * @return The sum of all TROOPS, that can attack {@code castle}
     */
    public static int getThreateningNeighboursTroopCount(Graph<Castle> castleGraph, Castle castle){
        int troopCount = 0;
        HashSet<Castle> passedNeighbours = new HashSet<>();
        List<Castle> connected;
        for(Castle neighbour : AAIMethods.getOtherNeighbours(castleGraph, castle.getOwner(), castle)){
            if(!passedNeighbours.contains(neighbour)){
                connected = AAIMethods.getConnectedCastles(castleGraph, neighbour);
                passedNeighbours.addAll(connected);
                troopCount += AAIMethods.getAttackTroopCount(connected);
            }
        }
        return troopCount;
    }

    /**
     * @param castleGraph graph containing all edges and nodes (neighbours)
     * @param castle the castle to check
     * @return the count of edges to enemy players
     */
    public static int getEnemyEdgeCount(Graph<Castle> castleGraph, Castle castle){
        return AAIMethods.getOtherNeighbours(castleGraph, castle.getOwner(), castle).size();
    }
}
