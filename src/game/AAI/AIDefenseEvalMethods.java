package game.AAI;

import base.Graph;
import de.teast.autils.ATriplet;
import game.Player;
import game.map.Castle;
import javafx.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class AIDefenseEvalMethods {
    /**
     * @param castleGraph the graph containing all castles and edges
     * @param player the player to distribute for
     * @return a list of pairs, with the castles as keys and the best troop count for every castle as value
     */
    public static List<Pair<Castle, Integer>> getBestTroopDistribution(Graph<Castle> castleGraph, Player player){
        List<List<Castle>> connectedCastles = AIMethods.getConnectedCastles(castleGraph, player);
        Map<Castle, Integer> troopCountTemp = new HashMap<>();
        List<Castle> temp;
        List<Pair<Castle, Integer>> tempRated;
        List<ATriplet<Castle, Integer, Double>> tempDistribution; // first: castle; second: troops; third: tempCount % 1
        int troopCount, countLeft;
        double tempCount;
        for(List<Castle> connected : connectedCastles){ // for all regions owned BY the player
            troopCount = AIMethods.getAttackTroopCount(connected);
            temp = new LinkedList<>(); // should contain all attackable castles
            for(Castle castle : connected){
                if(AIMethods.hasOtherNeighbours(castleGraph, player, castle)) {
                    temp.add(castle);
                }
            }

            // evalutate the rating for the castles and save it in tempRated
            tempRated = new LinkedList<>();
            for(Castle castle : temp){
                tempRated.add(new Pair<>(castle, evaluateCastle(castleGraph, player, castle)));
            }

            int sum = 0; // sum all ratings, to get the percentage of each castle
            for(Pair<Castle, Integer> pair : tempRated){
                sum += pair.getValue();
            }
            tempDistribution = new LinkedList<>();
            countLeft = troopCount;
            for(Pair<Castle, Integer> pair : tempRated){ // distribute troops dependent on the percentage of the rating from sum
                tempCount = troopCount * (pair.getValue() / ((double)sum));
                tempDistribution.add(new ATriplet<>(pair.getKey(), (int)tempCount, (tempCount % 1)));
                countLeft -= (int)tempCount;
            }

            tempDistribution = tempDistribution.stream().sorted(Comparator.comparingDouble(ATriplet::getThird)).collect(Collectors.toList());
            Collections.reverse(tempDistribution);
            ListIterator<ATriplet<Castle, Integer, Double>> iterator = tempDistribution.listIterator();
            ATriplet<Castle, Integer, Double> next;
            while(countLeft-- > 0 && !tempDistribution.isEmpty()){ // distribute remaining troops
                if(!iterator.hasNext()){
                    iterator = tempDistribution.listIterator();
                }
                next = iterator.next();
                next.setSecond(next.getSecond() + 1);
            }

            sum = 0;
            for(ATriplet<Castle, Integer, Double> triplet : tempDistribution){
                sum += triplet.getSecond();
                troopCountTemp.put(triplet.getFirst(), triplet.getSecond());
            }
            if(sum != troopCount){
                System.err.println("Fehler in AIDefenseEvalMethods.getBestTroopDistribution: Die summe aller truppen" +
                        " sollte " + troopCount + " sein," +
                        " ist aber " + sum);
            }
        }

        List<Pair<Castle, Integer>> returnList = new LinkedList<>();
        Integer troops;
        for(List<Castle> connected : connectedCastles){
            for(Castle castle : connected){
                troops = troopCountTemp.get(castle); // save troops + 1 if distributet or 1 if not (1 for all castles without enemy neighbour)
                returnList.add(new Pair<>(castle, (troops == null) ? 1 : (troops + 1)));
            }
        }
        return returnList;
    }
    /**
     * evaluates a value, how important the defense of this castle is. The higher the value the higher is the importance
     * @param castleGraph the graph containing all nodes and edges
     * @param player the player to calculate the value for
     * @param castle the castle to calculate the value for
     * @return the calculated value
     */
    public static int evaluateCastle(Graph<Castle> castleGraph, Player player, Castle castle){
        int points = 0;

        points += (int)(AIMethods.getNeighbours(castleGraph, castle, player).size() * AIConstants.EDGE_COUNT_MULTIPLIER);
        points += (int)(getThreateningNeighboursTroopCount(castleGraph, castle) * AIConstants.THREATENING_TROOP_COUNT_MULTIPLIER);

        return points;
    }

    /**
     * @param castleGraph graph containing all edges and nodes (neighbours)
     * @param castle the castle to check
     * @return The sum of all troops, that can attack {@code castle}
     */
    public static int getThreateningNeighboursTroopCount(Graph<Castle> castleGraph, Castle castle){
        int troopCount = 0;
        List<Castle> passedNeighbours = new LinkedList<>();
        List<Castle> connected;
        for(Castle neighbour : AIMethods.getOtherNeighbours(castleGraph, castle, castle.getOwner())){
            if(!passedNeighbours.contains(neighbour)){
                connected = AIMethods.getConnectedCastles(castleGraph, neighbour);
                passedNeighbours.addAll(connected);
                troopCount += AIMethods.getAttackTroopCount(connected);
            }
        }
        return troopCount;
    }
}
