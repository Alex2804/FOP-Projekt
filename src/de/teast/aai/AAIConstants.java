package de.teast.aai;

public class AAIConstants {
    // Constants for AAIDistributionEvalMethods
    public static int SURROUNDED_BY_OWN_CASTLES = 3;
    public static int NO_ENEMY_NEIGHBOUR = 1;
    // Constants for AAIDistributionEvalMethods (pair evaluation)
    public static int FIRST_CASTLE_IN_KINGDOM = 2;
    public static int OTHER_CAN_CAPTURE_KINGDOM = 4;
    public static int OTHER_HAS_CASTLES_IN_KINGDOM = 2;
    public static int SPLIT_ENEMY_REGION = 2;
    public static int CONNECTED_TO_OWN_CASTLES = 7;

    // Constants for AAIKingdomEvalMethods
    public static int SMALL_KINGDOM = 4;
    public static int HAS_FEW_EDGES_TO_OTHER_KINGDOMS = 5;

    // Constants for AAITargetEvalMethods
    public static int OPPORTUNITY_ELEMINATE_PLAYER = 5;
    public static int BELONGS_BIG_THREAT = 3;
    public static int IMPORTANT_KINGDOM = 3;
    public static int CLOSE_TO_CAPTURE_KINGDOM = 6;
    public static int BREAK_UP_KINGDOM = 5;
    public static int LAST_CASTLE_IN_KINGDOM = 3;
    public static int MIN_ATTACK_VALUE = 10;
    public static double EVALUATION_VALUE_MULTIPLIER = 1;
    public static double TROOP_DIFFERENCE_MULTIPLIER = 0.5;

    // Constants for AAIDefenseEvalMethods
    public static double EDGE_COUNT_MULTIPLIER = 0.05;
    public static double THREATENING_TROOP_COUNT_MULTIPLIER = 1;
}
