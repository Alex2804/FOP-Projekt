package de.teast.aai;

import base.Graph;
import de.teast.autils.ATriplet;
import game.Player;
import game.map.Castle;
import game.map.Kingdom;
import javafx.util.Pair;

import java.util.*;

/**
 * Evaluation Methods to choose targets for an AI
 * @author Alexander Muth
 */
public class AAITargetEvalMethods {
    public static AAIConstants constants = new AAIConstants();

    /**
     * This method searches all targets for {@code player}, evaluates them and sorts them after their priority and
     * win chance
     * @param castleGraph the graph containing all edges and castles
     * @param player the player to get the targets for
     * @return a {@link List} of {@link Pair}s, with the connected attacker castles as keys and the target castle as values
     */
    public static List<Pair<List<Castle>, Castle>> getTargets(Graph<Castle> castleGraph, Player player){
        List<ATriplet<List<Castle>, Castle, Double>> evalList = evaluateCastles(castleGraph, player);
        evalList.sort(Comparator.comparingDouble(ATriplet::getThird));
        Collections.reverse(evalList);

        List<Pair<List<Castle>, Castle>> returnList = new LinkedList<>();
        for(ATriplet<List<Castle>, Castle, Double> triplet : evalList){
            if(triplet.getThird() >= constants.MIN_ATTACK_VALUE
                    || (AAIMethods.getAttackTroopCount(triplet.getFirst()) > triplet.getSecond().getTroopCount() * 1.3)){
                returnList.add(new Pair<>(triplet.getFirst(), triplet.getSecond()));
            }else{
                break;
            }
        }
        return returnList;
    }

    /**
     * Evaluates all possible targets, of{@code player}
     * @param castleGraph the graph containing all castles and edges
     * @param player the player to evaluate for
     * @return a {@link List} of {@link ATriplet}s, which contains the connected attackers as first, the target castle
     * as second and the evaluated value as third value
     */
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

    /**
     * Evaluates a value of the target {@link Castle}s, if they should get attacked by the player
     * @param castleGraph graph conatining all castles and edges
     * @param player the player to evaluate for
     * @param attackerRegion the region from which to evaluate an attack
     * @param evalTargets the targets
     * @return a {@link List} containing {@link Pair} with the evaluated target as key and the evaluated value as value
     */
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
                    * constants.EDGE_DIFFERENCE_MULTIPLIER;
            points += temp;

            temp = (AAIMethods.getNeighboursAttackTroopCount(castleGraph, player, newRegion)
                    - AAIMethods.getNeighboursAttackTroopCount(castleGraph, player, attackerRegion))
                    * constants.NEIGHBOUR_TROOP_DIFFERENCE_MULTIPLIER;
            points += temp;

            temp = (castle.getTroopCount()
                    - AAIMethods.getAttackTroopCount(attackerRegion))
                    * constants.TARGET_TROOP_DIFFERNECE_MULTIPLIER;
            points += temp;

            returnList.add(new Pair<>(castle, pair.getValue() - points));
            newRegion.remove(castle);
        }
        return returnList;
    }

    /**
     * Evaluates a target value for all targets, which are reachable from {@code connectedCastles}
     * @param castleGraph the castle graph containing all castles and edges
     * @param player the player to evaluate for
     * @param connectedCastles the castles to evaluate the reachable targets for
     * @return a {@link List} of {@link Pair}s with the evaluated target castle as key and the evaluation value as value
     */
    public static List<Pair<Castle, Integer>> evaluateTargetCastles(Graph<Castle> castleGraph, Player player, List<Castle> connectedCastles){
        List<Pair<Castle, Integer>> returnList = new LinkedList<>();
        for(Castle possibleTarget : AAIMethods.getOtherNeighbours(castleGraph, player, connectedCastles)){
            returnList.add(new Pair<>(possibleTarget, evaluateCastle(castleGraph, player, possibleTarget)));
        }
        return returnList;
    }

    /**
     * @param castleGraph the castle graph containing all edges and castles
     * @param player the player to evaluate for
     * @param castle the castle to evaluate
     * @return the evaluated value for the passed {@code castle}
     */
    public static int evaluateCastle(Graph<Castle> castleGraph, Player player, Castle castle){
        int points = 0;

        points += canEliminateEnemyPlayer(castleGraph, castle) ? constants.OPPORTUNITY_ELEMINATE_PLAYER : 0;
        points += belongsBigThreat(castleGraph, player, castle) ? constants.BELONGS_BIG_THREAT : 0;
        points += hasFewNeighbours(castleGraph, player, castle) ? constants.HAS_FEW_NEIGHBOURS : 0;
        points += canUniteSplittedRegions(castleGraph, player, castle) ? constants.UNITE_SPLITTED_REGIONS : 0;
        if(castle.getKingdom() != null) {
            points += isCloseToCaptureKingdom(player, castle.getKingdom()) ? constants.CLOSE_TO_CAPTURE_KINGDOM : 0;
            points += isOwnedByOnePlayer(castle.getKingdom()) ? constants.BREAK_UP_KINGDOM : 0;
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
    public static boolean canEliminateEnemyPlayer(Graph<Castle> castleGraph, Castle castle){
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

        points += (enemyKingdomCount - playerKingdomCount) * constants.BIG_THREAT_KINGDOM_MULTIPLIER;
        points += (enemyCastles.size() - playerCastles.size()) * constants.BIG_THREAT_CASTLE_MULTIPLIER;
        points += (enemyTroopCount - playerTroopCount) * constants.BIG_THREAT_TROOP_MULTIPLIER;

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
        return AAIMethods.getOtherNeighbours(castleGraph, player, castle).size() <= constants.FEW_NEIGHBOUR_COUNT;
    }
}
