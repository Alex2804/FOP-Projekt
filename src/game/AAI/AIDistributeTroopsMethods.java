package game.AAI;

import game.Game;
import game.map.Castle;

/**
 * @author Alexander Muth
 * Class to move troops depending on the situation
 */
public class AIDistributeTroopsMethods {
    /**
     * @author Alexander Muth
     * Class to save troop movement without moving
     */
    public static class TroopMover{
        public Castle source, destination;
        public int troopCount;

        /**
         * @param source the source castle
         * @param destination the destination castle
         * @param troopCount the count of troops to move
         */
        public TroopMover(Castle source, Castle destination, int troopCount){
            this.source = source;
            this.destination = destination;
            this.troopCount = troopCount;
        }

        /**
         * @param game the game object where to move the troops
         * @return if the troops where moved successfully
         */
        public boolean move(Game game){
            if(source == null || destination == null || game == null)
                return false;
            if(source.getTroopCount() < troopCount)
                troopCount = source.getTroopCount() - 1;
            if(troopCount <= 1 || source.getOwner() != destination.getOwner())
                return false;
            game.moveTroops(source, destination, troopCount);
            return true;
        }
    }
}
