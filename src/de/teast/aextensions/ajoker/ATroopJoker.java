package de.teast.aextensions.ajoker;

import de.teast.AConstants;
import game.Game;
import game.Player;

public class ATroopJoker extends AJoker{
    private boolean usable = true;

    public ATroopJoker(Game game, Player player){
        super(AConstants.ATROOPJOKER_NAME, AConstants.ATROOPJOKER_DESCRIPTION, game, player);
    }

    @Override
    public boolean isUsable() {
        return usable;
    }

    @Override
    public void useJoker() {
        player.addTroops(AConstants.ATROOPJOKER_TROOP_COUNT);
        usable = false;
    }
}
