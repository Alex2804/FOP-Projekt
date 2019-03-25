package de.teast.aai;

import base.Graph;
import game.GameConstants;
import game.Player;
import game.map.Castle;
import game.map.Kingdom;
import javafx.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Evaluation Methods to choose castles during distribution period for an AI
 * @author Alexander Muth
 */
public class AAIDistributionEvalMethods {
    public static AAIConstants constants = new AAIConstants();

    /**
     * Get the best castles during distribution period
     * @param castleGraph the graph containing all castles
     * @param player the player to assign castles
     * @param castleCount the castles needed to distribute
     * @return a collection with castles, which are the best for the player to choose. The size of the return list is
     * equal or less than the value of {@code castleCount}
     */
    public static Collection<Castle> getBestCastleDistribution(Graph<Castle> castleGraph, Player player, int castleCount){
        List<Castle> allCastles = castleGraph.getAllValues();
        List<Castle> availableCastles = Player.getCastles(allCastles, null);
        castleCount = (availableCastles.size() < castleCount) ? availableCastles.size() : castleCount;

        Set<Castle> bestCastles = new HashSet<>();
        List<Castle> tempAllCastles = new LinkedList<>(allCastles);
        List<Castle> captureKingdom;
        do{
            captureKingdom = canOwnKingdom(tempAllCastles, player, castleCount);
            if(captureKingdom != null && !captureKingdom.isEmpty()){
                tempAllCastles.removeAll(captureKingdom.get(0).getKingdom().getCastles());
                castleCount -= captureKingdom.size();
                bestCastles.addAll(captureKingdom);
            }
        }while(captureKingdom != null && castleCount > 0);
        availableCastles.removeAll(bestCastles);

        if(castleCount <= 0)
            return bestCastles;

        Map<Castle, Integer> castleEvaluationMap = new HashMap<>();
        for(Castle castle : availableCastles){
            castleEvaluationMap.put(castle, evaluateCastle(castleGraph, player, castle));
        }

        int pairSize = castleCount;
        while(pairSize > 0){
            List<List<Castle>> pairs = AAIMethods.getPossibleCastlePairs(castleGraph, null, pairSize);
            List<Pair<List<Castle>, Integer>> pairsValueList = new LinkedList<>();
            for(List<Castle> pair : pairs){
                pairsValueList.add(new Pair<>(pair, evaluateCastlePair(castleGraph, player, pair, castleEvaluationMap)));
            }

            pairsValueList.sort(Comparator.comparingInt(Pair::getValue)); // sort the pairs by value
            Collections.reverse(pairsValueList); // pair with biggest value should be at front
            if(!pairsValueList.isEmpty()){
                for(Pair<List<Castle>, Integer> pair : pairsValueList){
                    for(Castle castle : pair.getKey()){
                        if(bestCastles.contains(castle)) { // test if contained in best castles
                            pair = null; // doesn't add to bestCastles list in if condition bellow if contained
                            break;
                        }
                    }
                    if(pair != null && !pair.getKey().isEmpty() && castleCount >= pair.getKey().size()){ // if pair is not to big and not empty
                        bestCastles.addAll(pair.getKey()); // this is the best pair
                        castleCount -= pair.getKey().size();
                    }else if(pair != null){
                        break; // break if to big pairs for to small amount of castles
                    }
                }
            }

            --pairSize; // reduce pair size by one and do again
        }

        if(castleCount <= 0)
            return bestCastles;

        //should never be executed but is for safety
        List<Pair<Castle, Integer>> castleEvaluationList = new LinkedList<>();
        for(Map.Entry<Castle, Integer> entry : castleEvaluationMap.entrySet()){
            castleEvaluationList.add(new Pair<>(entry.getKey(), entry.getValue()));
        }
        castleEvaluationList.sort(Comparator.comparingInt(Pair::getValue));
        Collections.reverse(castleEvaluationList);

        bestCastles.addAll(castleEvaluationList.stream().map(Pair::getKey).collect(Collectors.toList())
                .subList(0, (castleEvaluationList.size() > castleCount) ? castleCount : castleEvaluationList.size()));
        return bestCastles;
    }

    /**
     * @param castles castles to check kingdoms from
     * @param player the player which want to capture a kingdom
     * @param castleCount the castles which can be captured at this move
     * @return null if their is no kingdom which can be owned with the given castle count or a list with the castles,
     * which are necessary to own the kingdom. If a list is returned, the size is equal or smaller to the value of
     * {@code castleCount} and only castles from one kingdom and castles from {@code castles} are contained
     */
    public static List<Castle> canOwnKingdom(List<Castle> castles, Player player, int castleCount){
        Kingdom kingdom;
        HashSet<Kingdom> passedKingdoms = new HashSet<>();
        List<Castle> tempCastles;
        for(Castle castle : castles){
            kingdom = castle.getKingdom();
            if(kingdom != null && !passedKingdoms.contains(kingdom)){
                passedKingdoms.add(kingdom);

                tempCastles = new LinkedList<>();
                for(Castle kingdomCastle : kingdom.getCastles()){
                    if(kingdomCastle.getOwner() == null && castles.contains(kingdomCastle)){
                        tempCastles.add(kingdomCastle);
                    }else if(kingdomCastle.getOwner() != player){
                        tempCastles = null;
                        break;
                    }
                }

                if(tempCastles != null && !tempCastles.isEmpty() && tempCastles.size() <= castleCount)
                    return tempCastles;
            }
        }

        return null;
    }

    /**
     * This Method sums up all values of the castles in the pair and adds an extra value for the first castle in
     * a kingdom, if an other player could capture the kingdom and if other players has castles in the kingdom
     * @param castleGraph the graph containing all castles and edges
     * @param player the player to check the value for
     * @param castles the castles to sum up the values
     * @param evaluationMap the map containing the castles connected with the values
     * @return the sum of all castle evaluations plus some extra one
     * @see #evaluateCastle(Graph, Player, Castle)
     */
    public static int evaluateCastlePair(Graph<Castle> castleGraph, Player player, List<Castle> castles, Map<Castle, Integer> evaluationMap){
        int points = 0;
        Integer tempValue;
        boolean otherCanCaptureKingdom = false, otherHasCastleInKingdom = false, isFirstCastleInKingdom = false,
                canSplitEnemyRegion = false, isConnectedToPlayerCastles = false;
        for(Castle castle : castles){
            tempValue = evaluationMap.get(castle);
            if(tempValue == null)
                tempValue = evaluateCastle(castleGraph, player, castle);
            points += tempValue;

            if(!otherCanCaptureKingdom && otherCanCaptureKingdom(player, castle)){
                otherCanCaptureKingdom = true;
                points += constants.OTHER_CAN_CAPTURE_KINGDOM;
            }
            if(!otherHasCastleInKingdom && otherHasCastleInKingdom(player, castle)){
                otherHasCastleInKingdom = true;
                points += constants.OTHER_HAS_CASTLES_IN_KINGDOM;
            }
            if(!isFirstCastleInKingdom && isFirstCastleInKingdom(player, castle)){
                isFirstCastleInKingdom = true;
                points += constants.FIRST_CASTLE_IN_KINGDOM;
            }
            if(!canSplitEnemyRegion && canSplitEnemyRegion(castleGraph, player, castle)){
                canSplitEnemyRegion = true;
                points += constants.SPLIT_ENEMY_REGION;
            }
            if(!isConnectedToPlayerCastles && AAIMethods.isConnectedToPlayerCastles(castleGraph, player, castle)){
                isConnectedToPlayerCastles = true;
                points += constants.CONNECTED_TO_OWN_CASTLES;
            }
        }

        points += AAIMethods.getNeighbours(castleGraph, null, castles).size() * constants.FREE_NEIGHBOURS_MULTIPLIER;

        points += hasExpandPossibilities(castleGraph, castles) ? constants.EXPAND_POSSIBILITIES : 0;

        return points;
    }

    /**
     * @param castleGraph graph conatining all edges and castles
     * @param castles pair of castles
     * @return if there are expand possibilities from the pair (list of castles)
     */
    public static boolean hasExpandPossibilities(Graph<Castle> castleGraph, List<Castle> castles){
        int nullNeighbourCount = 0;
        for(Castle castle : castles){
            nullNeighbourCount += AAIMethods.getNeighbours(castleGraph, null, castle).size();
        }
        return nullNeighbourCount >= constants.EXPAND_POSSIBILITIES_COUNT;
    }

    /**
     * Evaluates a value for passed castle dependent on some factors
     * @param castleGraph the available castles
     * @param player the player to evaluate for
     * @param castle the castle to evaluate
     * @return the evaluated value for the castle object
     */
    public static int evaluateCastle(Graph<Castle> castleGraph, Player player, Castle castle){
        int points = 0;
        if(castle == null)
            return points;

        List<Player> tempPlayers = new ArrayList<>(2);
        tempPlayers.add(player);
        points += !AAIMethods.hasOtherNeighbours(castleGraph, tempPlayers, castle) ? constants.NO_ENEMY_NEIGHBOUR + constants.SURROUNDED_BY_OWN_CASTLES : 0;
        if(points <= 0){
            tempPlayers.add(null);
            points += !AAIMethods.hasOtherNeighbours(castleGraph, tempPlayers, castle) ? constants.NO_ENEMY_NEIGHBOUR : 0;
        }
        if(castle.getKingdom() != null){
            points += AAIKingdomEvalMethods.evaluateKingdom(castleGraph, castle.getKingdom());
        }

        return points;
    }

    /**
     * @param castleGraph graph object containing all castles and edges
     * @param player the player to check for
     * @param castle the castle to check if it can split regions
     * @return true if player can split regions of an enemy player
     */
    public static boolean canSplitEnemyRegion(Graph<Castle> castleGraph, Player player, Castle castle){
        Map<Player, Set<Castle>> playerConnectedMap = new HashMap<>();
        Set<Castle> passed;
        List<Castle> connected;
        for(Castle neighbour : AAIMethods.getOtherNeighbours(castleGraph, player, castle)){
            if(neighbour.getOwner() != null){
                passed = playerConnectedMap.get(neighbour.getOwner());
                if(passed != null && !passed.contains(neighbour) && ((Castle)passed.toArray()[0]).getOwner() == neighbour.getOwner()) {
                    return true;
                } else {
                    connected = AAIMethods.getConnectedCastles(castleGraph, neighbour);
                    connected.add(neighbour);
                    playerConnectedMap.put(neighbour.getOwner(), new HashSet<>(connected));
                }
            }
        }
        return false;
    }

    /**
     * @param player the player to check if it has any castles in the kingdom
     * @param castle the castle to check it's kingdom
     * @return if the castle is the first castle of the player in it's kingdom (false if the castles kingdom is null)
     */
    public static boolean isFirstCastleInKingdom(Player player, Castle castle){
        if(castle.getKingdom() == null)
            return false;
        for(Castle kingdomCastle : castle.getKingdom().getCastles()){
            if(kingdomCastle.getOwner() == player)
                return false;
        }
        return true;
        //return castle.getKingdom().getCastles().stream().noneMatch(c -> c.getOwner() == player); // less code but slower
    }

    /**
     * @param player the player to check if an other can capture a kingdom
     * @param castle the castle to check it's kingdom
     * @return if the kingdom of the castle could be captured by another player than the passed one in the next round
     */
    public static boolean otherCanCaptureKingdom(Player player, Castle castle){
        if(castle.getKingdom() == null)
            return false;
        int freeCastles = 0;
        Player tempOwner = null;
        for(Castle kingdomCastle : castle.getKingdom().getCastles()){
            tempOwner = (tempOwner == null) ? kingdomCastle.getOwner() : tempOwner;
            if(kingdomCastle.getOwner() == null) {
                ++freeCastles;
            } else if(kingdomCastle.getOwner() == player || kingdomCastle.getOwner() != tempOwner) {
                return false;
            }
            if(freeCastles > GameConstants.CASTLES_AT_BEGINNING)
                return false;
        }
        return true;
    }

    /**
     * @param player the player to check if other players has castles in kingdom
     * @param castle the castle to check the kingdom
     * @return if other players than {@code player} has castles in the kingdom of the passed {@code castle}
     */
    public static boolean otherHasCastleInKingdom(Player player, Castle castle){
        if(castle.getKingdom() == null)
            return false;
        for(Castle kingdomCastle : castle.getKingdom().getCastles()){
            if(kingdomCastle.getOwner() != player && kingdomCastle.getOwner() != null)
                return true;
        }
        return false;
    }
}
