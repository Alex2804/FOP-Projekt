package de.teast.aai;

import base.Graph;
import de.teast.autils.ATriplet;
import game.Player;
import game.map.Castle;
import game.map.Kingdom;
import javafx.util.Pair;

import java.util.*;

/**
 * @author Alexander Muth
 * Evaluation Methods to choose targets for an AI
 */
public class AAITargetEvalMethods {
    public static List<Pair<List<Castle>, Castle>> getTargets(Graph<Castle> castleGraph, Player player){
        List<ATriplet<List<Castle>, Castle, Double>> evalList = evaluateCastles(castleGraph, player);
        evalList.sort(Comparator.comparingDouble(ATriplet::getThird));
        Collections.reverse(evalList);

        List<Pair<List<Castle>, Castle>> returnList = new LinkedList<>();
        for(ATriplet<List<Castle>, Castle, Double> triplet : evalList){
            if(triplet.getThird() >= AAIConstants.MIN_ATTACK_VALUE){
                returnList.add(new Pair<>(triplet.getFirst(), triplet.getSecond()));
            }else{
                break;
            }
        }
        return returnList;
    }

    public static List<ATriplet<List<Castle>, Castle, Double>> evaluateCastles(Graph<Castle> castleGraph, Player player){
        Map<List<Castle>, List<Pair<Castle, Double>>> targetAttackMap = new HashMap<>();
        for(List<Castle> connected : AAIMethods.getConnectedCastles(castleGraph, player)){
            targetAttackMap.put(connected,
                                shouldAttackCastles(castleGraph, player, connected,
                                        evaluateTargetCastles(castleGraph, player, connected)));
        }

        HashMap<Castle, Pair<List<Castle>, Double>> targetAttackerEvalMap = new HashMap<>(); // (target, (connected-attacker, eval-value))
        for(Map.Entry<List<Castle>, List<Pair<Castle, Double>>> entry : targetAttackMap.entrySet()){
            for(Pair<Castle, Double> pair : entry.getValue()){
                if(!targetAttackerEvalMap.containsKey(pair.getKey())
                        || (targetAttackerEvalMap.containsKey(pair.getKey())
                        && targetAttackerEvalMap.get(pair.getKey()).getValue() < pair.getValue())){
                    targetAttackerEvalMap.put(pair.getKey(), new Pair<>(entry.getKey(), pair.getValue()));
                }
            }
        }

        List<ATriplet<List<Castle>, Castle, Double>> returnList = new LinkedList<>();
        for(Map.Entry<Castle, Pair<List<Castle>, Double>> entry : targetAttackerEvalMap.entrySet()){
            returnList.add(new ATriplet<>(entry.getValue().getKey(), entry.getKey(), entry.getValue().getValue()));
        }
        return returnList;
    }

    public static List<Pair<Castle, Double>> shouldAttackCastles(Graph<Castle> castleGraph, Player player, List<Castle> attackerRegion, List<Pair<Castle, Integer>> evalTargets){
        double points, temp;
        Castle castle;
        List<Castle> newRegion = new LinkedList<>(attackerRegion);
        List<Pair<Castle, Double>> returnList = new LinkedList<>();
        for(Pair<Castle, Integer> pair : evalTargets){
            castle = pair.getKey();
            newRegion.add(castle);
            points = 0;

            temp = (AAIMethods.getOtherNeighbours(castleGraph, player, newRegion).size()
                    - AAIMethods.getOtherNeighbours(castleGraph, player, attackerRegion).size())
                    * AAIConstants.EDGE_DIFFERENCE_MULTIPLIER;
            points += temp;

            temp = (AAIMethods.getNeighboursAttackTroopCount(castleGraph, player, newRegion)
                    - AAIMethods.getNeighboursAttackTroopCount(castleGraph, player, attackerRegion))
                    * AAIConstants.NEIGHBOUR_TROOP_DIFFERENCE_MULTIPLIER;
            points += temp;

            temp = (castle.getTroopCount()
                    - AAIMethods.getAttackTroopCount(attackerRegion))
                    * AAIConstants.TARGET_TROOP_DIFFERNECE_MULTIPLIER;
            points += temp;

            returnList.add(new Pair<>(castle, pair.getValue() - points));
            newRegion.remove(castle);
        }
        return returnList;
    }

    public static List<Pair<Castle, Integer>> evaluateTargetCastles(Graph<Castle> castleGraph, Player player, List<Castle> connectedCastles){
        List<Pair<Castle, Integer>> returnList = new LinkedList<>();
        for(Castle possibleTarget : AAIMethods.getOtherNeighbours(castleGraph, player, connectedCastles)){
            returnList.add(new Pair<>(possibleTarget, evaluateCastle(castleGraph, player, possibleTarget)));
        }
        return returnList;
    }

    public static int evaluateCastle(Graph<Castle> castleGraph, Player player, Castle castle){
        int points = 0;

        points += canEleminateEnemyPlayer(castleGraph, castle) ? AAIConstants.OPPORTUNITY_ELEMINATE_PLAYER : 0;
        points += belongsBigThreat(castleGraph, player, castle) ? AAIConstants.BELONGS_BIG_THREAT : 0;
        points += hasFewNeighbours(castleGraph, player, castle) ? AAIConstants.HAS_FEW_NEIGHBOURS : 0;
        points += canUniteSplittedRegions(castleGraph, player, castle) ? AAIConstants.UNITE_SPLITTED_REGIONS : 0;
        if(castle.getKingdom() != null) {
            points += isCloseToCaptureKingdom(player, castle.getKingdom()) ? AAIConstants.CLOSE_TO_CAPTURE_KINGDOM : 0;
            points += isOwnedByOnePlayer(castle.getKingdom()) ? AAIConstants.BREAK_UP_KINGDOM : 0;
        }

        return points;
    }

    /**
     * @param castleGraph graph containing all nodes and edges
     * @param player the player to check
     * @param castle The castle to check
     * @return if 2 regions could get united by an attack
     */
    public static boolean canUniteSplittedRegions(Graph<Castle> castleGraph, Player player, Castle castle){
        Set<Castle> passed = new HashSet<>();
        for(Castle neighbour : AAIMethods.getNeighbours(castleGraph, player, castle)){
            if(!passed.isEmpty() && !passed.contains(neighbour)) {
                return true;
            } else {
                passed.addAll(AAIMethods.getConnectedCastles(castleGraph, neighbour));
                passed.add(neighbour);
            }
        }
        return false;
    }

    /**
     * @param castleGraph the graph containing all edges and (castle)nodes
     * @param castle the castle to check
     * @return If the castle is the last castle of its owner
     */
    public static boolean canEleminateEnemyPlayer(Graph<Castle> castleGraph, Castle castle){
        if(castle.getOwner() == null)
            return false;
        return castle.getOwner().getCastleNodes(castleGraph).size() <= 1;
    }

    /**
     * @param castleGraph the graph containing all edges and (castle)nodes
     * @param player the player to check if the owner is a big threat to
     * @param castle the castle to check its owner
     * @return if the owner is a big threat to the player
     */
    public static boolean belongsBigThreat(Graph<Castle> castleGraph, Player player, Castle castle){
        if(castle.getOwner() == null)
            return false;
        List<Castle> allCastles = castleGraph.getAllValues();
        List<Castle> enemyCastles = castle.getOwner().getCastles(allCastles);
        List<Castle> playerCastles = player.getCastles(allCastles);
        int enemyKingdomCount = AAIDistributeTroopsMethods.ownedKingdomCount(enemyCastles);
        int playerKingdomCount = AAIDistributeTroopsMethods.ownedKingdomCount(playerCastles);
        int enemyTroopCount = AAIMethods.getAttackTroopCount(enemyCastles);
        int playerTroopCount = AAIMethods.getAttackTroopCount(playerCastles);

        double points = 0;

        points += (enemyKingdomCount - playerKingdomCount) * AAIConstants.BIG_THREAT_KINGDOM_MULTIPLIER;
        points += (enemyCastles.size() - playerCastles.size()) * AAIConstants.BIG_THREAT_CASTLE_MULTIPLIER;
        points += (enemyTroopCount - playerTroopCount) * AAIConstants.BIG_THREAT_TROOP_MULTIPLIER;

        return points > 0;
    }

    /**
     * Checks if a player is close to capture a kingdom
     * @param player player to check
     * @param kingdom kingdom to check
     * @return if the player is close to capture the kingdom
     */
    public static boolean isCloseToCaptureKingdom(Player player, Kingdom kingdom){
        List<Castle> castles = kingdom.getCastles();
        if(castles.isEmpty())
            return false;
        int playerCastleCount = 0;
        for(Castle kingdomCastle : castles){
            if(kingdomCastle.getOwner() == player)
                ++playerCastleCount;
        }
        double percentage = ((double)playerCastleCount) / castles.size();
        return percentage >= 0.8 || (castles.size() - playerCastleCount <= 1);
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
     * @param castleGraph the graph containing all castles and edges
     * @param player the player to check for
     * @param castle the castle to check the neighbours
     * @return if the passed {@code castle} has few enemy neighbours
     */
    public static boolean hasFewNeighbours(Graph<Castle> castleGraph, Player player, Castle castle){
        return AAIMethods.getOtherNeighbours(castleGraph, player, castle).size() <= AAIConstants.FEW_NEIGHBOUR_COUNT;
    }
}
