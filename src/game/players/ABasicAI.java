package game.players;

import base.Graph;
import de.teast.aai.*;
import game.Game;
import game.map.Castle;
import gui.AttackThread;
import javafx.util.Pair;

import java.awt.Color;
import java.util.List;

/**
 * @author Alexander Muth
 * Basic implementation of a better AI than {@link BasicAI} for {@link game.goals.ConquerGoal}
 */
public class ABasicAI extends BasicAI {
    public AAIConstantsWrapper constants = new AAIConstantsWrapper();
    private int timeout = 600;

    public ABasicAI(String name, Color color) {
        super(name, color);
    }

    @Override
    protected void actions(Game game) throws InterruptedException {
        if(constants == null)
            constants = new AAIConstantsWrapper("best401Mittel.txt");
        AAIDefenseEvalMethods.constants = constants;
        AAIDistributeTroopsMethods.constants = constants;
        AAIDistributionEvalMethods.constants = constants;
        AAIKingdomEvalMethods.constants = constants;
        AAITargetEvalMethods.constants = constants;

        Graph<Castle> castleGraph = game.getMap().getGraph();
        if(game.getRound() == 1){
            for(Castle castle : AAIDistributionEvalMethods.getBestCastleDistribution(castleGraph, this, getRemainingTroops())){
                sleep(timeout + 10);
                game.chooseCastle(castle, this);
            }
        } else {
            List<Pair<Castle, Integer>> troopDistribution = AAIDistributeTroopsMethods.distributeTroops(castleGraph, this, getRemainingTroops());
            for(Pair<Castle, Integer> pair : troopDistribution){
                sleep((timeout / 3) + 10);
                game.addTroops(this, pair.getKey(), pair.getValue());
            }

            List<Pair<List<Castle>, Castle>> targets = AAITargetEvalMethods.getTargets(castleGraph, this);
            Castle attacker;
            boolean attackWon;
            int attackTroopCount, targetTroopCount;
            double percentage, worstPercentage = -1;
            for(Pair<List<Castle>, Castle> pair : targets){
                attackTroopCount = AAIMethods.getAttackTroopCount(pair.getKey());
                targetTroopCount = pair.getValue().getTroopCount();
                percentage = attackTroopCount / ((double)(attackTroopCount + targetTroopCount));
                if((worstPercentage >= 0 && percentage > worstPercentage)
                        || (worstPercentage < 0
                            && AAIMethods.getAttackTroopCount(pair.getKey()) > (pair.getValue().getTroopCount() * constants.TROOP_DIFFERENCE_MULTIPLIER))
                        || targets.size() <= 1
                        || attackTroopCount > targetTroopCount * 1.5){
                    attacker = pair.getValue().getNearest(pair.getKey());
                    if(pair.getValue() == null || attacker == null){
                        continue;
                    }
                    AAIDistributeTroopsMethods.makeMoves(game, AAIDistributeTroopsMethods.generateCollectMoves(castleGraph, attacker));

                    AttackThread attackThread = game.startAttack(attacker, pair.getValue(), attackTroopCount, fastForward);
                    if(attackThread == null)
                        continue;
                    if(fastForward)
                        attackThread.fastForward();
                    attackThread.join();
                    attackWon = attackThread.getWinner() == this;
                    if(!attackWon){
                        worstPercentage = percentage;
                    }
                }
            }

            game.getGameInterface().onUpdate();
            AAIDefenseEvalMethods.moveDefenseTroops(game, this);
        }
    }

    public int getTimeout() {
        return timeout;
    }
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
