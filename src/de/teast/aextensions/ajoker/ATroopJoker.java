package de.teast.aextensions.ajoker;

import de.teast.AConstants;
import game.Game;
import game.Player;

/**
 * Joker which adds troops to the player if used
 * @author Alexander Muth
 */
public class ATroopJoker extends AJoker{
    public static final String NAME = "Truppen Joker";
    public static final String DESCRIPTION = "Mit diesem Joker bekommt der Spieler " + AConstants.ATROOPJOKER_TROOP_COUNT + " extra Truppen.";
    public static final String LOGTEXT = "und bekommt " + AConstants.ATROOPJOKER_TROOP_COUNT + " extra Truppen!";

    private boolean usable = true;

    public ATroopJoker(Game game, Player player){
        super(NAME, DESCRIPTION, LOGTEXT, game, player);
    }

    @Override
    public boolean isUsable() {
        return usable && game.allCastlesChosen();
    }

    @Override
    public void innerUseJoker() {
        player.addTroops(AConstants.ATROOPJOKER_TROOP_COUNT);
        usable = false;
    }
}
