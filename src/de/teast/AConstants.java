package de.teast;

import de.teast.aextensions.ajoker.*;
import de.teast.atroops.ATroop;
import game.Game;
import game.Goal;
import game.Player;
import game.goals.ACaptureTheFlagGoal;
import game.goals.AClashOfArmiesGoal;
import game.goals.AFlagEmpireGoal;
import game.goals.ConquerGoal;
import game.players.Human;
import gui.Resources;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Constants for some of my implementations
 * @author Alexander Muth
 */
public class AConstants {
    public static final double MIN_AVOID_DISTANCE = 1;
    public static final double AVOID_DISTANCE_SCALE_MULTIPLIER = 2;
    public static final double RANDOM_DISTANCE_SCALE_MULTIPLIER_MIN = 0.85;
    public static final double RANDOM_DISTANCE_SCALE_MULTIPLIER_MAX = 1.15;
    public static final double DISTANCE_MIDDLE_DIVISOR = 12.0;

    public static final int MIN_STOP_COUNT = 3;
    public static final int MAX_STOP_COUNT = 5;
    public static final double STOP_COUNT_PLAYER_MULTIPLIER = 1;

    public static final int MIN_PATH_COUNT_PER_CASTLES = 3;
    public static final int MAX_PATH_COUNT_PER_CASTLES = 6;

    public static final double CASTLE_EDGE_DISTANCE = 50;
    public static final double CASTLE_EDGE_DISTANCE_PLAYER_MULTIPLIER = 1;
    public static final int MAX_ITER_CLUSTERING = 100;

    public static final int POINTS_PER_ROUND = 50;
    public static final int POINTS_PER_BASE = 50;

    public static final ATroop[] TROOPS = {
            new ATroop("horse.png", "Reiter", 7, 2, 15, 0, 0, 3, 100, 50),
            new ATroop("sword.png", "Schwertkämpfer", 8, 5, 12, 0, 6, 1, 100, 30),
            new ATroop("spear.png", "Speerkämpfer", 4, 3, 10, 0, 0, 1, 40, 10),
            new ATroop("bow.png", "Bogenschütze", 2, 6, 4, 7, 2, 2, 80, 20),
            new ATroop("crossbow.png", "Armbrustschütze", 4, 10, 5, 15, 1, 1, 70, 30),
    };
    public static final int TROOP_IMAGE_INFO_SIZE = 100;
    public static final int TROOP_IMAGE_TROOPS_PANEL_SIZE = 40;
    public static final int TROOP_IMAGE_MOVE_PANEL_SIZE = 40;
    public static final int TROOP_IMAGE_BUY_PANEL_SIZE = 40;

    public static final int FLAG_POINTS = 100;


    public static final int ATROOPJOKER_TROOP_COUNT = 5;
    public static final int APOINTJOKER_POINT_COUNT = POINTS_PER_ROUND * 3;
    public static final int AEXTRACASTLESJOKER_CASTLE_COUNT = 3;

    public static final Game JOKER_INIT_GAME = new Game();
    public static final Player JOKER_INIT_PLAYER = new Human("", Color.BLACK);
    public static final AJoker ATROOPJOKER = new ATroopJoker(JOKER_INIT_GAME, JOKER_INIT_PLAYER);
    public static final AJoker APOINTJOKER = new APointJoker(JOKER_INIT_GAME, JOKER_INIT_PLAYER);
    public static final AJoker AEXTRACASTLESJOKER = new AExtraCastlesJoker(JOKER_INIT_GAME, JOKER_INIT_PLAYER);
    public static final AJoker[] CONQUER_JOKERS = {
            ATROOPJOKER, AEXTRACASTLESJOKER
    };
    public static final AJoker[] CAPTURE_THE_FLAG_JOKERS = CONQUER_JOKERS;
    public static final AJoker[] FLAG_EMPIRE_JOKERS = {
            ATROOPJOKER, APOINTJOKER, AEXTRACASTLESJOKER
    };
    public static final AJoker[] CLASH_OF_ARMIES_JOKERS = {
            APOINTJOKER
    };
    public static final Map<Class<? extends Goal>, AJoker[]> GAME_JOKERS = new HashMap(){{
        put(ConquerGoal.class, CONQUER_JOKERS);
        put(ACaptureTheFlagGoal.class, CAPTURE_THE_FLAG_JOKERS);
        put(AFlagEmpireGoal.class, FLAG_EMPIRE_JOKERS);
        put(AClashOfArmiesGoal.class, CLASH_OF_ARMIES_JOKERS);
    }};
}
