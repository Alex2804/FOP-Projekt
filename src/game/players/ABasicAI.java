package game.players;

import base.Graph;
import de.teast.aai.AAIDefenseEvalMethods;
import de.teast.aai.AAIDistributeTroopsMethods;
import de.teast.aai.AAIDistributionEvalMethods;
import game.Game;
import game.map.Castle;
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

            /**
            boolean attackWon;
            do {
                // 2. Move troops from inside to border
                for (Castle castle : this.getCastles(game)) {
                    if (!castleNearEnemy.contains(castle) && castle.getTroopCount() > 1) {
                        Castle fewestTroops = getCastleWithFewestTroops(castleNearEnemy);
                        game.moveTroops(castle, fewestTroops, castle.getTroopCount() - 1);
                    }
                }

                // 3. attack!
                attackWon = false;
                for (Castle castle : castleNearEnemy) {
                    if(castle.getTroopCount() < 2)
                        continue;

                    Node<Castle> node = graph.getNode(castle);
                    for (Edge<Castle> edge : graph.getEdges(node)) {
                        Castle otherCastle = edge.getOtherNode(node).getValue();
                        if (otherCastle.getOwner() != this && castle.getTroopCount() >= otherCastle.getTroopCount()) {
                            AttackThread attackThread = game.startAttack(castle, otherCastle, castle.getTroopCount());
                            if(fastForward)
                                attackThread.fastForward();

                            attackThread.join();
                            attackWon = attackThread.getWinner() == this;
                            break;
                        }
                    }

                    if(attackWon)
                        break;
                }
            } while(attackWon);**/

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
