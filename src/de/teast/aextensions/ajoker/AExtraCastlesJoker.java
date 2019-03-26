package de.teast.aextensions.ajoker;

import de.teast.AConstants;
import game.Game;
import game.Player;

public class AExtraCastlesJoker extends AJoker {
    public static final String NAME = "Extra Burgen Joker";
    public static final String DESCRIPTION = "Mit diesem Joker bekommt der Spieler " + AConstants.AEXTRACASTLESJOKER_CASTLE_COUNT + " extra Burgen während der Anfangsphase";
    public static final String LOGTEXT = "und darf sich " + AConstants.AEXTRACASTLESJOKER_CASTLE_COUNT + " extra Burgen auswählen";

    private boolean usable;

    public AExtraCastlesJoker(Game game, Player player){
        super(NAME, DESCRIPTION, LOGTEXT, game, player);
        usable = true;
    }

    @Override
    public void innerUseJoker() {
        player.addTroops(Math.min(AConstants.AEXTRACASTLESJOKER_CASTLE_COUNT, Player.getCastles(game.getMap().getCastles(), null).size()));
        usable = false;
    }

    @Override
    public boolean isUsable() {
        return usable && game.getRound() == 1 && !game.allCastlesChosen();
    }
}
