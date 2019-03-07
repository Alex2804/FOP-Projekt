package game.players;

import game.AI;
import game.Game;

import java.awt.Color;

/**
 * @author Alexander Muth
 * Basic implementation of a better AI than {@link BasicAI} for {@link game.goals.ConquerGoal}
 */
public class ABasicAI extends AI {

    public ABasicAI(String name, Color color) {
        super(name, color);
    }

    @Override
    protected void actions(Game game) throws InterruptedException {

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
