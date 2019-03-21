package de.teast.aai;

public class AAIConstants {
    // Constants for AAIDistributionEvalMethods
    public static int SURROUNDED_BY_OWN_CASTLES = 3;
    public static int NO_ENEMY_NEIGHBOUR = 0;
    // Constants for AAIDistributionEvalMethods (pair evaluation)
    public static int FIRST_CASTLE_IN_KINGDOM = 1;
    public static int OTHER_CAN_CAPTURE_KINGDOM = 4;
    public static int OTHER_HAS_CASTLES_IN_KINGDOM = 1;
    public static int SPLIT_ENEMY_REGION = 2;
    public static int CONNECTED_TO_OWN_CASTLES = 7;
    public static int EXPAND_POSSIBILITIES = 5;
    public static int EXPAND_POSSIBILITIES_COUNT = 4; // count of neighbours (owner == null) that expand possibilities are given
    public static double FREE_NEIGHBOURS_MULTIPLIER = 2;

    // Constants for AAIKingdomEvalMethods
    public static int SMALL_KINGDOM = 4;
    public static int HAS_FEW_EDGES_TO_OTHER_KINGDOMS = 5;

    // Constants for AAITargetEvalMethods
    public static int OPPORTUNITY_ELEMINATE_PLAYER = 5;
    public static int BELONGS_BIG_THREAT = 3;
    public static int CLOSE_TO_CAPTURE_KINGDOM = 6;
    public static int BREAK_UP_KINGDOM = 4;
    public static int HAS_FEW_NEIGHBOURS = 4;
    public static int UNITE_SPLITTED_REGIONS = 3;
    // big threat?
    public static double BIG_THREAT_KINGDOM_MULTIPLIER = 3;
    public static double BIG_THREAT_CASTLE_MULTIPLIER = 1;
    public static double BIG_THREAT_TROOP_MULTIPLIER = 0.7;
    // few neighbours
    public static int FEW_NEIGHBOUR_COUNT = 1;
    // shouldAttackCastles
    public static double EDGE_DIFFERENCE_MULTIPLIER = 4;
    public static double NEIGHBOUR_TROOP_DIFFERENCE_MULTIPLIER = 0.5;
    public static double TARGET_TROOP_DIFFERNECE_MULTIPLIER = 5;
    // getTargets
    public static int MIN_ATTACK_VALUE = 8;

    // Constants for AAIDefenseEvalMethods
    public static double EDGE_COUNT_MULTIPLIER = 0.05;
    public static double THREATENING_TROOP_COUNT_MULTIPLIER = 1;

    // Constants for AAIDistributeTroopsMethods (connected evaluation)
    public static double CASTLE_COUNT_MULTIPLIER = 1;
    public static double OWNED_KINGDOM_MULTIPLIER = 4;
    // attack possibilities
    public static int CAN_UNITE_SPLITTED_REGIONS = 5;

    // Constants for ABasicAI
    public static double TROOP_DIFFERENCE_MULTIPLIER = 1.3;
}
