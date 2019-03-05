package game.players;

import game.AI;
import game.Game;

import java.awt.Color;

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
