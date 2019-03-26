package de.teast.aextensions.ajoker;

import de.teast.AConstants;
import game.Game;
import game.Player;

public class APointJoker extends AJoker {
    public static final String NAME = "Punkte Joker";
    public static final String DESCRIPTION = "Mit diesem Joker bekommt der Spieler " + AConstants.APOINTJOKER_POINT_COUNT + " extra Punkte.";
    public static final String LOGTEXT = "und bekommt " + AConstants.APOINTJOKER_POINT_COUNT + " extra Punkte!";

    private boolean usable = true;

    public APointJoker(Game game, Player player){
        super(NAME, DESCRIPTION, LOGTEXT, game, player);
    }

    @Override
    public boolean isUsable() {
        return usable;
    }

    @Override
    public void innerUseJoker() {
        player.addPoints(AConstants.APOINTJOKER_POINT_COUNT);
        usable = false;
    }
}
