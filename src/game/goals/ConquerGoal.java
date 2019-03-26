package game.goals;

import de.teast.AConstants;
import de.teast.aextensions.ajoker.AJoker;
import game.Game;
import game.GameConstants;
import game.Goal;
import game.Player;
import game.map.Castle;
import game.map.MapSize;

import java.util.List;

public class ConquerGoal extends Goal {

    public ConquerGoal() {
        super("Eroberung", "Derjenige Spieler gewinnt, der als erstes alle Gebiete erobert hat.");
    }

    @Override
    public boolean isCompleted() {
        return this.getWinner() != null;
    }
    @Override
    public boolean isCompleted(List<Castle> castles) {
        return this.getWinner(castles) != null;
    }

    @Override
    public Player getWinner() {
        Game game = this.getGame();
        if(game.getRound() < 2)
            return null;

        return getWinner(game.getMap().getCastles());
    }
    @Override
    public Player getWinner(List<Castle> castles){
        Player p = null;
        for(Castle c : castles){
            if(c.getOwner() == null)
                return null;
            else if(p == null)
                p = c.getOwner();
            else if(p != c.getOwner())
                return null;
        }
        return p;
    }

    @Override
    public boolean hasLost(Player player) {
        if (getGame().getRound() < 2)
            return false;

        return player.getNumRegions(getGame()) == 0;
    }

    @Override
    public boolean hasLost(Player player, List<Castle> castles, int round) {
        if(round < 2)
            return false;

        return player.getNumRegions(castles) == 0;
    }

    @Override
    public MapSize[] getSupportedMapSizes() {
        return new MapSize[]{MapSize.SMALL, MapSize.MEDIUM, MapSize.LARGE};
    }
    @Override
    public AJoker[] getSupportedJokers() {
        return AConstants.CONQUER_JOKERS;
    }
    @Override
    public Class<?>[] getSupportedPlayerTypes() {
        return GameConstants.PLAYER_TYPES;
    }
    @Override
    public int getMaxPlayerCount() {
        return 4;
    }
}
