package de.teast.aai;

import base.Graph;
import de.teast.autils.ATriplet;
import game.Player;
import game.map.Castle;
import game.map.Kingdom;
import javafx.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Alexander Muth
 * Evaluation Methods to choose targets for an AI
 */
public class AAITargetEvalMethods {
    /**
     * Returns the best Target and attack castle
     * @param castleGraph graph object, containing all edges and castles
     * @param player the player to get the best target for
     * @return a {@link Pair} with the attacker castle (representing connected castles) as first and the target castle
     * as second argument
     */
    public static Pair<Castle, Castle> getBestTargetCastle(Graph<Castle> castleGraph, Player player){
        return getBestTargetCastle(castleGraph, getBestTargetCastles(castleGraph, player));
    }
    /**
     * Returns the best Target and attack castle
     * @param castleGraph graph object, containing all edges and castles
     * @param evaluationResults result of {@link #getBestTargetCastles(Graph, Player)}
     *                          (must be sorted like returned by this method!);
     * @return a {@link Pair} with the attacker castle (representing connected castles) as first and the target castle
     * as second argument
     */
    public static Pair<Castle, Castle> getBestTargetCastle(Graph<Castle> castleGraph, List<ATriplet<Castle, Integer, Castle>> evaluationResults){
        double bestValue = Double.MIN_VALUE, value;
        int troopDifference;
        Castle lastTargetCastle = null, bestTargetCastle = null, bestAttackerCastle = null;
        for(ATriplet<Castle, Integer, Castle> quadruplet : evaluationResults){
            if(lastTargetCastle != quadruplet.getFirst()) { // list must be sorted after troop count (eval value only changes with other target castle change)
                troopDifference = quadruplet.getFirst().getTroopCount() - AAIMethods.getAttackTroopCount(AAIMethods.getConnectedCastles(castleGraph, quadruplet.getThird()));
                value = (quadruplet.getSecond() * AAIConstants.EVALUATION_VALUE_MULTIPLIER)
                        - (troopDifference * AAIConstants.TROOP_DIFFERENCE_MULTIPLIER);
                if(lastTargetCastle == null || value > bestValue) {
                    bestValue = value;
                    bestTargetCastle = quadruplet.getFirst();
                    bestAttackerCastle = quadruplet.getThird();
                }
                lastTargetCastle = quadruplet.getFirst();
            }
        }

        return (bestTargetCastle == null) ? null : new Pair<>(bestTargetCastle, bestAttackerCastle);
    }
    /**
     * searches the best target castle dependent on some factors
     * attack (with as many troops as possible) (a region are connected castles)
     * @param castleGraph the graph containing all castles
     * @param player the player which want to attack
     * @return {@link ATriplet} with the target castle as first, evaluation value as second, attacker castle
     * (representing an region) as third argument.
     * The {@link ATriplet} are sorted with the castles with the biggest evaluation value at beginning
     */
    public static List<ATriplet<Castle, Integer, Castle>> getBestTargetCastles(Graph<Castle> castleGraph, Player player){
        List<Castle> allCastles = castleGraph.getAllValues(); // list with all castles
        // castleAttackTroops: Map which saves a castle and the sum of troop count of all reachable castles
        Map<Castle, Integer> castleAttackTroopsMap = AAIMethods.getPossibleAttackTroopCount(castleGraph, player);
        // castleAttackTargetMap: Map which saves all attackable castles from one attacker castle (from playerCastleTroops)
        Map<Castle, List<Castle>> castleAttackTargetMap = new HashMap<>();

        // get all possible targets for connected castles (represented by one castle)
        for(Castle castle : castleAttackTroopsMap.keySet()){
            castleAttackTargetMap.put(castle, AAIMethods.getPossibleTargetCastles(castleGraph, player, castle));
        }

        // Remove all targets which has more troops than the attacker castle
        int troopCount;
        Castle next;
        ListIterator<Castle> iterator;
        List<Castle> attackCastles;
        for(Map.Entry<Castle, Integer> entry : castleAttackTroopsMap.entrySet()){
            troopCount = entry.getValue();
            attackCastles = castleAttackTargetMap.get(entry.getKey());
            iterator = attackCastles.listIterator();
            while(iterator.hasNext()){
                next = iterator.next();
                if(next.getTroopCount() >= troopCount){
                    iterator.remove();
                }
            }
        }

        // assign values with the possible target castles
        Map<Castle, Integer> castleTargetEvaluationMap = new HashMap<>();
        List<Castle> targetCastles;
        for(Castle attackCastle : castleAttackTroopsMap.keySet()){
            targetCastles = castleAttackTargetMap.get(attackCastle);
            for(Castle targetCastle : targetCastles){
                if(castleTargetEvaluationMap.get(targetCastle) == null){
                    castleTargetEvaluationMap.put(targetCastle, evaluateCastle(allCastles, player, targetCastle));
                }
            }
        }

        // link target castles with attack castles
        Map<Castle, List<Castle>> castleTargetAttackMap = new HashMap<>();
        List<Castle> tempCastles;
        for(Castle attackCastle : castleAttackTargetMap.keySet()){
            targetCastles = castleAttackTargetMap.get(attackCastle);
            for(Castle targetCastle : targetCastles){
                tempCastles = castleTargetAttackMap.get(targetCastle);
                if(tempCastles == null)
                    tempCastles = new LinkedList<>();
                tempCastles.add(attackCastle);
                castleTargetAttackMap.put(targetCastle, tempCastles);
            }
        }

        // sort the keys of the evaluation, for the evaluation value
        List<Castle> sortedCastleAttackEvaluationKeys = castleTargetEvaluationMap.entrySet().stream().
                sorted(Comparator.comparingInt(Map.Entry::getValue)).
                map(Map.Entry::getKey).
                collect(Collectors.toList());
        // front should be the highest value
        Collections.reverse(sortedCastleAttackEvaluationKeys);

        List<ATriplet<Castle, Integer, Castle>> returnList = new LinkedList<>();
        List<Pair<Castle, Integer>> attackerTroopsList;
        int evalValue;
        for(Castle targetCastle : sortedCastleAttackEvaluationKeys){
            evalValue = castleTargetEvaluationMap.get(targetCastle);
            if(evalValue >= AAIConstants.MIN_ATTACK_VALUE) {
                attackerTroopsList = new LinkedList<>();
                for(Castle attackCastle : castleTargetAttackMap.get(targetCastle)){
                    if(attackCastle.getTroopCount() >= targetCastle.getTroopCount()) {
                        attackerTroopsList.add(new Pair<>(attackCastle, castleAttackTroopsMap.get(attackCastle)));
                    }
                }
                attackerTroopsList = attackerTroopsList.stream()
                        .sorted(Comparator.comparingInt(Pair::getValue))
                        .collect(Collectors.toList());
                Collections.reverse(attackerTroopsList);
                for(Pair<Castle, Integer> attackerTroopPair : attackerTroopsList){
                    returnList.add(new ATriplet<>(targetCastle, evalValue, attackerTroopPair.getKey()));
                }
            }
        }
        return returnList;
    }

    /**
     * Evaluates a value for passed castle dependent on some factors
     * @param castles the available castles
     * @param player the player to evaluate for
     * @param castle the castle to evaluate
     * @return the evaluated value for the castle object
     */
    public static int evaluateCastle(List<Castle> castles, Player player, Castle castle){
        int points = 0;

        points += isLastCastleOfPlayer(castles, castle) ? AAIConstants.OPPORTUNITY_ELEMINATE_PLAYER : 0;
        points += isBigThreat(castles, player, castle.getOwner()) ? AAIConstants.BELONGS_BIG_THREAT : 0;
        if(castle.getKingdom() != null){
            points += isImportantKingdom(player, castle.getKingdom()) ? AAIConstants.IMPORTANT_KINGDOM : 0;
            boolean closeToCapture = isCloseToCaptureKingdom(player, castle.getKingdom());
            points += closeToCapture ? AAIConstants.CLOSE_TO_CAPTURE_KINGDOM : 0;
            points += isOwnedByOnePlayer(castle.getKingdom()) ? AAIConstants.BREAK_UP_KINGDOM : 0;
            points += isLastCastleInKingdom(player, castle) ? AAIConstants.LAST_CASTLE_IN_KINGDOM : 0;
        }

        return points;
    }

    /**
     * Checks if the castle is the last castle, which belongs to the owner in the list of castles
     * @param castles list of castles
     * @param castle castle to check owner
     * @return if the castle is the last castle of the owner
     */
    public static boolean isLastCastleOfPlayer(List<Castle> castles, Castle castle){
        return castles.stream().noneMatch(c -> c.getOwner() == castle.getOwner() && c != castle);
    }

    /**
     * Checks if a Player is a big threat for another player
     * @param player the player to check
     * @param other player to check if it is a big threat
     * @return if the other player is a big threat for player
     */
    public static boolean isBigThreat(List<Castle> castles, Player player, Player other){
        return player.getCastles(castles).size() <= other.getCastles(castles).size();
    }

    /**
     * checks if a player is (one of) the strongest player in a kingdom
     * @param player player to check if is (one of) the strongest
     * @param kingdom kingdom to check if player is (one of) the strongest
     * @return if the player is (one of) the strongest players in the kingdom
     */
    public static boolean isImportantKingdom(Player player, Kingdom kingdom){
        Integer otherIndex;
        int playerPoints = 0;
        List<Integer> otherPoints = new ArrayList<>();
        Map<Player, Integer> indices = new HashMap<>();
        for(Castle castle : kingdom.getCastles()){
            if(castle.getOwner() == player){
                playerPoints += 1;
            }else{
                otherIndex = indices.get(castle.getOwner());
                if(otherIndex == null){
                    otherIndex = otherPoints.size();
                    indices.put(castle.getOwner(), otherIndex);
                    otherPoints.add(1);
                }else{
                    otherPoints.set(otherIndex, otherPoints.get(otherIndex) + 1);
                }
            }
        }

        for(int p : otherPoints){
            if(p > playerPoints)
                return false;
        }
        return true;
    }

    /**
     * Checks if a player is close to capture a kingdom
     * @param player player to check for
     * @param kingdom kingdom to check for
     * @return if the player is close to capture the kingdom
     */
    public static boolean isCloseToCaptureKingdom(Player player, Kingdom kingdom){
        double playerCastleCount =  kingdom.getCastles().stream().filter(c -> c.getOwner() != player).collect(Collectors.toList()).size();
        double percentage = playerCastleCount / kingdom.getCastles().size();
        return percentage >= 0.7; // more than 70% of the kingdom is owned by the player
    }

    /**
     * @param kingdom kingdom to check
     * @return if the kingdom is owned by one player
     */
    public static boolean isOwnedByOnePlayer(Kingdom kingdom){
        Player owner = null;
        for(Castle castle : kingdom.getCastles()){
            if(owner == null)
                owner = castle.getOwner();
            else if(owner != castle.getOwner())
                return false;
        }
        return true;
    }

    /**
     * Checks if the castle is the last castle in the kingdom, owned by another player than the passed one
     * @param player the player to check if their are other players in the kingdom
     * @param castle the castle to check if it is the last
     * @return if the castle is the last one owned by another player
     */
    public static boolean isLastCastleInKingdom(Player player, Castle castle){
        return castle.getKingdom().getCastles().stream().noneMatch(c -> c.getOwner() != player && c != castle);
    }
}
