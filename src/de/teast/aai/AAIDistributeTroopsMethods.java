package de.teast.aai;

import base.Graph;
import game.Game;
import game.Player;
import game.map.Castle;
import game.map.Kingdom;
import javafx.util.Pair;

import java.util.*;

/**
 * Class to move and distribute troops
 * @author Alexander Muth
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
         * @param troopCount the count of TROOPS to move
         */
        public ATroopMover(Castle source, Castle destination, int troopCount){
            this.source = source;
            this.destination = destination;
            this.troopCount = troopCount;
        }

        /**
         * Moves {@link #troopCount} TROOPS from {@link #source} to {@link #destination} if possible
         * @param game the game object where to move the TROOPS
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

    public static AAIConstants constants = new AAIConstants();

    /**
     * Generates {@link ATroopMover} objects, to distribute the TROOPS, for the given distribution (only TROOPS of
     * castles in {@code troopDistribution} are used)
     * @param castleGraph the graph, containing all castles and edges
     * @param troopDistribution the castles with the number of distributed TROOPS
     * @param player the player to distribute for
     * @return a list of generated {@link ATroopMover}
     */
    public static List<ATroopMover> generateMoves(Graph<Castle> castleGraph, List<Pair<Castle, Integer>> troopDistribution, Player player){
        List<Castle> castles = new LinkedList<>();
        Map<Castle, Integer> needTroopsHashMap = new HashMap<>();
        Map<Castle, Integer> hasTroopsHashMap = new HashMap<>();
        int temp;
        for(Pair<Castle, Integer> pair : troopDistribution){
            temp = pair.getKey().getTroopCount() - 1 - pair.getValue();
            if(temp > 0){ // castle has more TROOPS than needed
                hasTroopsHashMap.put(pair.getKey(), temp);
                castles.add(pair.getKey());
            }else if(temp < 0) { // castle has less TROOPS than needed
                needTroopsHashMap.put(pair.getKey(), Math.abs(temp));
                castles.add(pair.getKey());
            }
        }
        if(castles.isEmpty())
            return new LinkedList<>();

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

                needTroopsHashMap.remove(castle);
            }
        }


        return returnList;
    }

    /**
     * Collects all Troops at one castle
     * @param castleGraph the graph containing all castles and edges
     * @param targetCastle target where to collect all TROOPS
     * @return a list of {@link ATroopMover} objects which are necessary to collect all TROOPS in the target castle or null
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

    /**
     * This method makes the moves of the passed {@link ATroopMover}.
     * @param game the game object
     * @param troopMovers A list containing {@link ATroopMover} objects
     */
    public static void makeMoves(Game game, List<ATroopMover> troopMovers){
        for(ATroopMover troopMover : troopMovers){
            troopMover.move(game);
        }
    }

    /**
     * Distributes TROOPS to the castles of the {@code player}
     * @param castleGraph graph object containing all castles and edges
     * @param player player to distribute the TROOPS for
     * @param troopCount available troop count
     * @return a list of pairs, with the castles as keys and the troop count to distribute as values
     */
    public static List<Pair<Castle, Integer>> distributeTroops(Graph<Castle> castleGraph, Player player, int troopCount){
        List<Pair<List<Castle>, Double>> connectedEvalValue = new LinkedList<>();
        double sum = 0, evalValue;
        for(List<Castle> connected : AAIMethods.getConnectedCastles(castleGraph, player)){
            evalValue = evaluateConnectedCastles(castleGraph, player, connected);
            sum += evalValue;
            connectedEvalValue.add(new Pair<>(connected, evalValue));
        }

        Map<List<Castle>, Integer> connectedTroopCountMap = new HashMap<>();
        double percentage;
        int tempTroopCount;
        for(Pair<List<Castle>, Double> pair : connectedEvalValue){
            percentage = pair.getValue() / sum;
            tempTroopCount = (int)(troopCount * percentage);
            troopCount -= tempTroopCount;
            connectedTroopCountMap.put(pair.getKey(), tempTroopCount);
        }

        if(troopCount > 0){
            connectedEvalValue.sort(Comparator.comparingDouble(Pair::getValue));
            Collections.reverse(connectedEvalValue);
            while(troopCount > 0){
                for(Pair<List<Castle>, Double> pair : connectedEvalValue){
                    connectedTroopCountMap.put(pair.getKey(), connectedTroopCountMap.get(pair.getKey()) + 1);
                    if(--troopCount <= 0) {
                        break;
                    }
                }
            }
        }

        List<Pair<Castle, Integer>> returnList = new LinkedList<>();
        for(Map.Entry<List<Castle>, Integer> entry : connectedTroopCountMap.entrySet()){
            if(!entry.getKey().isEmpty() && entry.getValue() > 0){
                returnList.add(new Pair<>(AAIMethods.getCastleWithMostTroops(entry.getKey()), entry.getValue()));
            }
        }
        return returnList;
    }

    /**
     * @param castleGraph graph object containing all castles and edges
     * @param player the player to evaluate for
     * @param connectedCastles a list of connected castles
     * @return an evaluation value of connected castles
     */
    public static double evaluateConnectedCastles(Graph<Castle> castleGraph, Player player, List<Castle> connectedCastles){
        double points = 0;

        points += connectedCastles.size() * constants.CASTLE_COUNT_MULTIPLIER;
        points += ownedKingdomCount(connectedCastles) * constants.OWNED_KINGDOM_MULTIPLIER;

        points += evaluateAttackPossibilities(castleGraph, player, connectedCastles);

        return points;
    }

    /**
     * @param castles A list of castles
     * @return the count of kingdoms occupied by the passed castles
     */
    public static int ownedKingdomCount(List<Castle> castles){
        boolean interrupted;
        int count = 0;
        Set<Castle> connectedCastlesSet = new HashSet<>(castles);
        Set<Kingdom> passedKingdoms = new HashSet<>();
        for(Castle castle : castles){
            if(castle.getKingdom() != null && !passedKingdoms.contains(castle.getKingdom())){
                passedKingdoms.add(castle.getKingdom());
                interrupted = false;
                for(Castle kingdomCastle : castle.getKingdom().getCastles()){
                    if(!connectedCastlesSet.contains(kingdomCastle)){
                        interrupted = true;
                        break;
                    }
                }
                if(!interrupted){
                    ++count;
                }
            }
        }
        return count;
    }

    /**
     * @param castleGraph graph containing all nodes and edges
     * @param player the player to evaluate for
     * @param connectedCastles a list of connected castles
     * @return an evaluation value for attack possibilities
     */
    public static int evaluateAttackPossibilities(Graph<Castle> castleGraph, Player player, List<Castle> connectedCastles){
        int points = 0;

        points += canUniteSplittedRegions(castleGraph, player, connectedCastles) ? constants.CAN_UNITE_SPLITTED_REGIONS : 0;

        return points;
    }

    /**
     * @param castleGraph graph containing all nodes and edges
     * @param player the player to check for
     * @param connectedCastles a list of connected castles
     * @return if 2 regions could get united by an attack
     */
    public static boolean canUniteSplittedRegions(Graph<Castle> castleGraph, Player player, List<Castle> connectedCastles){
        Set<Castle> connectedCastlesSet = new HashSet<>(connectedCastles);
        for(Castle neighbour : AAIMethods.getOtherNeighbours(castleGraph, player, connectedCastles)){
            for(Castle otherNeighbour : AAIMethods.getNeighbours(castleGraph, player, neighbour)){
                if(!connectedCastlesSet.contains(otherNeighbour)
                        && AAIMethods.getConnectedCastles(castleGraph, otherNeighbour).size() <= connectedCastles.size()){
                    return true;
                }
            }
        }
        return false;
    }
}
