package game;

import de.teast.aextensions.ajoker.AJoker;
import game.map.Castle;
import game.map.MapSize;

import java.util.List;

public abstract class Goal {

    private Game game;
    private final String description;
    private final String name;

    public Goal(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public abstract boolean isCompleted();
    /**
     * @param castles castles of the game
     * @return if the game is completed
     */
    public abstract boolean isCompleted(List<Castle> castles);
    public abstract Player getWinner();
    /**
     * @param castles castles of the game
     * @return the winner or null if there is no winner
     */
    public abstract Player getWinner(List<Castle> castles);
    public abstract boolean hasLost(Player player);
    /**
     *
     * @param player player to check
     * @param castles castles of the game
     * @param round round of the game
     * @return if the player has lost
     */
    public abstract boolean hasLost(Player player, List<Castle> castles, int round);

    public final  String getDescription() {
        return this.description;
    }

    public final String getName() {
        return this.name;
    }

    protected Game getGame() {
        return this.game;
    }

    public abstract MapSize[] getSupportedMapSizes();
    public abstract AJoker[] getSupportedJokers();
    public abstract Class<?>[] getSupportedPlayerTypes();
    public abstract int getMaxPlayerCount();
}
