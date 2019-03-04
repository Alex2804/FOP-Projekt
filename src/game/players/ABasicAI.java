package game.players;

import base.Graph;
import game.AAI.AIConstants;
import game.AAI.AIEvalMethods;
import game.AAI.AIMethods;
import game.AI;
import game.Game;
import game.Player;
import game.map.Castle;
import javafx.util.Pair;

import java.awt.Color;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Alexander Muth
 * Basic implementation of a better AI
 */
public class ABasicAI extends AI {
    /**
     * Wrapps a class arround a specific object to store an associated value for it
     * @param <T> The generic type of the stored object
     */
    private static class ValueWrapper<T> {
        int valuePoints;
        T value;
        public ValueWrapper(T value){
            this.value = value;
            valuePoints = 0;
        }
        public ValueWrapper(T value, int valuePoints){
            this.value = value;
            this.valuePoints = valuePoints;
        }
    }

    public ABasicAI(String name, Color color) {
        super(name, color);
    }

    @Override
    protected void actions(Game game) throws InterruptedException {

    }

    /**
     * searches the best target castle dependent on some factors and one castle from the best Region from which to
     * attack (with as many troops as possible) (a region are connected castles)
     * @param castleGraph the graph containing all castles
     * @param player the player which want to attack
     * @return the best target castle or null if there is none or the best castle does not meets the expectations
     * and the castle from the best region to attack
     */
    private static Pair<Castle, Castle> getBestTargetCastle(Graph<Castle> castleGraph, Player player){
        List<Castle> allCastles = castleGraph.getAllValues(); // list with all castles
        // castleAttackTroops: Map which saves a castle and the sum of troop count of all reachable castles
        Map<Castle, Integer> castleAttackTroopsMap = AIMethods.getPossibleAttackTroopCount(castleGraph, player);
        // castleAttackTargetMap: Map which saves all attackable castles from one attacker castle (from playerCastleTroops)
        Map<Castle, List<Castle>> castleAttackTargetMap = new HashMap<>();

        // get all possible targets for connected castles (represented by one castle)
        for(Castle castle : castleAttackTroopsMap.keySet()){
            castleAttackTargetMap.put(castle, AIMethods.getPossibleTargetCastles(castleGraph, player, castle));
        }

        // Remove all targets which has more troops than the attacker castle
        int troopCount;
        Castle next;
        List<Castle> attackCastles;
        for(Map.Entry<Castle, Integer> entry : castleAttackTroopsMap.entrySet()){
            troopCount = entry.getValue();
            attackCastles = castleAttackTargetMap.get(entry.getKey());
            ListIterator<Castle> iterator = attackCastles.listIterator();
            while(iterator.hasNext()){
                next = iterator.next();
                if(next.getTroopCount() >= troopCount){
                    iterator.remove();
                }
            }
        }

        // assign values with the possible target castles
        Map<Castle, Integer> castleAttackEvaluationMap = new HashMap<>();
        List<Castle> targetCastles;
        for(Castle attackCastle : castleAttackTroopsMap.keySet()){
            targetCastles = castleAttackTargetMap.get(attackCastle);
            if(targetCastles != null){
                for(Castle targetCastle : targetCastles){
                    if(castleAttackEvaluationMap.get(targetCastle) == null){
                        castleAttackEvaluationMap.put(targetCastle,
                                AIEvalMethods.evaluateCastle(allCastles, player, targetCastle));
                    }
                }
            }
        }

        // sort the keys of the evaluation, after the evaluation value
        List<Castle> sortedCastleAttackEvaluationKeys = castleAttackEvaluationMap.entrySet().stream().
                                                                sorted(Comparator.comparingInt(Map.Entry::getValue)).
                                                                map(Map.Entry::getKey).
                                                                collect(Collectors.toList());
        // front should be the highest value
        Collections.reverse(sortedCastleAttackEvaluationKeys);

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

        Castle bestTarget = null, bestAttacker = null;
        int bestEvalValue = AIConstants.MIN_ATTACK_VALUE, evalValue, bestTroopDif = 0;
        for(Castle targetCastle : sortedCastleAttackEvaluationKeys){
            evalValue = castleAttackEvaluationMap.get(targetCastle);

            if(evalValue < bestEvalValue){ // break if the evaluation value is lower than before or smaller than the min value that is necessary to attack (at beginning)
                break;
            }
            if(bestTarget == null) { // if first pass
                bestTarget = targetCastle;
                bestEvalValue = evalValue;
            }

            for(Castle attackCastle : castleTargetAttackMap.get(targetCastle)){ // find target and attacker castle with biggest troop difference
                troopCount = castleAttackTroopsMap.get(attackCastle);
                if(bestAttacker == null || troopCount - targetCastle.getTroopCount() > bestTroopDif){
                    bestTroopDif = troopCount - targetCastle.getTroopCount();
                    bestTarget = targetCastle;
                    bestAttacker = attackCastle;
                    bestEvalValue = evalValue;
                }
            }
        }

        return (bestTarget != null && bestAttacker != null) ? new Pair<>(bestTarget, bestAttacker) : null;
    }

    @Override
    public ABasicAI copy() {
        ABasicAI aBasicAI = new ABasicAI(getName(), getColor());
        aBasicAI.fastForward = fastForward;
        aBasicAI.addPoints(getPoints());
        aBasicAI.addTroops(getRemainingTroops());
        aBasicAI.hasJoker = hasJoker;
        return aBasicAI;
    }
}
