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
    private int timeout = 0;

    public ABasicAI(String name, Color color) {
        super(name, color);
    }

    @Override
    protected void actions(Game game) throws InterruptedException {
        Graph<Castle> castleGraph = game.getMap().getGraph();
        if(game.getRound() == 1){
            for(Castle castle : AAIDistributionEvalMethods.getBestCastleDistribution(castleGraph, this, getRemainingTroops())){
                sleep(timeout + 10); // +10 or exception is thrown (necessary to update gui)
                game.chooseCastle(castle, this);
            }
        } else {
            List<Pair<Castle, Integer>> troopDistribution = AAIDistributeTroopsMethods.distributeTroops(castleGraph, this, getRemainingTroops());
            for(Pair<Castle, Integer> pair : troopDistribution){
                sleep(timeout / 3);
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
                            && AAIMethods.getAttackTroopCount(pair.getKey()) > (pair.getValue().getTroopCount() * AAIConstants.TROOP_DIFFERENCE_MULTIPLIER))){
                    attacker = pair.getValue().getNearest(pair.getKey());
                    AAIDistributeTroopsMethods.makeMoves(game, AAIDistributeTroopsMethods.generateCollectMoves(castleGraph, attacker));

                    AttackThread attackThread = game.startAttack(attacker, pair.getValue(), attackTroopCount);
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

    @Override
    public ABasicAI copy() {
        ABasicAI aBasicAI = new ABasicAI(getName(), getColor());
        aBasicAI.fastForward = fastForward;
        aBasicAI.addPoints(getPoints());
        aBasicAI.addTroops(getRemainingTroops());
        aBasicAI.hasJoker = hasJoker;
        return aBasicAI;
    }

    public int getTimeout() {
        return timeout;
    }
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
