package de.teast.aextensions.ajoker;

import game.Game;
import game.Player;

/**
 * Abstract class representing a joker
 * @author Alexander Muth
 */
public abstract class AJoker {
    protected String name, description, logText;
    protected Game game;
    protected Player player;

    public AJoker(String name, String description, String logText, Game game, Player player){
        this.name = name;
        this.description = description;
        this.logText = logText;
        this.game = game;
        this.player = player;
    }

    /**
     * uses this joker
     */
    public void useJoker(){
        innerUseJoker();
        game.getGameInterface().onLogText("%PLAYER% hat einen "+name+" eingesetzt " + logText, player);
    }
    /**
     * uses this joker
     */
    public abstract void innerUseJoker();
    /**
     * @return if the joker is usable
     */
    public abstract boolean isUsable();

    /**
     * @return the name of this {@link AJoker}
     */
    public String getJokerName(){
        return name;
    }
    /**
     * @return a description of this {@link AJoker}
     */
    public String getJokerDescription(){
        return description;
    }

    /**
     * @return the game of this joker
     */
    public Game getGame(){
        return game;
    }
    /**
     * @return the owner of this joker
     */
    public Player getPlayer(){
        return player;
    }
}
