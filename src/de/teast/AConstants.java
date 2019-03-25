package de.teast;

import de.teast.atroops.ATroop;

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
            new ATroop("castle1.png", "Reiter", 10, 2, 15, 0, 0, 2, 100, 50),
            new ATroop("castle2.png", "Schwertkämpfer", 10, 10, 10, 0, 0, 1, 100, 25),
            new ATroop("castle3.png", "Speerkämpfer", 8, 8, 7, 0, 0, 1, 100, 15),
            new ATroop("castle4.png", "Bogenschütze", 2, 5, 1, 5, 2, 1, 100, 20),
            new ATroop("castle5.png", "Armbrustschütze", 5, 5, 2, 8, 1, 1, 100, 25),
    };
    public static final int TROOP_IMAGE_INFO_SIZE = 100;
    public static final int TROOP_IMAGE_TROOPS_PANEL_SIZE = 40;
    public static final int TROOP_IMAGE_MOVE_PANEL_SIZE = 40;
    public static final int TROOP_IMAGE_BUY_PANEL_SIZE = 40;

    public static final int FLAG_POINTS = 100;


    public static final int ATROOPJOKER_TROOP_COUNT = 4;
    public static final String ATROOPJOKER_NAME = "Truppen Joker";
    public static final String ATROOPJOKER_DESCRIPTION = "Mit diesem Joker bekommt der Spieler wenn er ihn einsetzt " + AConstants.ATROOPJOKER_TROOP_COUNT + " extra Truppen.";
}
