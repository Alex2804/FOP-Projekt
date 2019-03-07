package game.AAI;

import base.Graph;
import game.Game;
import game.Player;
import game.map.Castle;
import javafx.util.Pair;

import java.util.LinkedList;
import java.util.List;

public class AIDistributeTroopsMethods {
    public static class TroopMover{
        public Castle source, destination;
        public int troopCount;
        private Game game;

        public TroopMover(Game game, Castle source, Castle destination, int troopCount){
            this.game = game;
            this.source = source;
            this.destination = destination;
            this.troopCount = troopCount;
        }

        public boolean move(){
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
