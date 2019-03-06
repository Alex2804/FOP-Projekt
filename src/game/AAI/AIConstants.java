package game.AAI;

public class AIConstants {
    // Constants for AITargetEvalMethods
    public static int OPPORTUNITY_ELEMINATE_PLAYER = 5;
    public static int BELONGS_BIG_THREAT = 3;
    public static int IMPORTANT_KINGDOM = 3;
    public static int CLOSE_TO_CAPTURE_KINGDOM = 6;
    public static int BREAK_UP_KINGDOM = 5;
    public static int LAST_CASTLE_IN_KINGDOM = 3;
    public static int MIN_ATTACK_VALUE = 10;

    // Constants for AIDistributionEvalMethods
    public static int SURROUNDED_BY_OWN_CASTLES = 3;
    public static int NO_ENEMY_NEIGHBOUR = 2;
    public static int FIRST_CASTLE_IN_KINGDOM = 4;

    // Constants for AIKingdomEvalMethods
    public static int SMALL_KINGDOM = 2;
    public static int HAS_FEW_EDGES_TO_OTHER_KINGDOMS = 3;
}
