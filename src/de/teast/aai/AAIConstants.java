package de.teast.aai;

import game.map.MapSize;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Constants for {@link de.teast.aai} and {@link game.players.ABasicAI}. If you take it seriously, the parameters are
 * no constants but for some reasons I want to load them from config files and store them to config files!
 * @author Alexander Muth
 */
public class AAIConstants {
    private static double[] medium = {6.0, 6.0, 7.0, 5.0, 0.0, 0.0, 1.0, 1.0, 4.0, 6.298966597976107, 9.0, 5.0, 5.0, 6.0, 8.0, 9.0, 1.0, 0.0, 7.362039874555077, 9.172684010663328, 1.3783264945773832, 6.0, 2.400567670138262, 6.465853316449159, 5.615348468475134, 5.0, 7.892995464390045, 2.6259517286719003, 6.2991551867509745, 2.659675184797293, 6.0, 5.563109520820051};
    private static double[] big = {5.0, 6.0, 3.0, 6.0, 0.0, 1.0, 4.0, 2.0, 2.0, 1.4752127404432303, 5.0, 6.0, 4.0, 3.0, 2.0, 7.0, 8.0, 3.0, 1.8153204608350926, 8.532511779495959, 1.6250023681209447, 3.0, 5.73009538579182, 3.1108923283187195, 2.377936694818117, 8.0, 6.104972755944558, 6.279130483552143, 5.245168240758728, 6.281462102456815, 6.0, 4.2710780188193045};

    private static int valueCount = 32;

    public AAIConstants(){
        super();
    }
    public AAIConstants(MapSize mapSize){
        super();
        if(mapSize == MapSize.SMALL){
            load(medium);
        }else if(mapSize == MapSize.MEDIUM){
            load(medium);
        }else if(mapSize == MapSize.LARGE){
            load(big);
        }
    }
    public AAIConstants(double[] values){
        super();
        load(values);
    }
    public AAIConstants(boolean randomValues){
        super();
        if(randomValues)
            generateRandom();
    }
    public AAIConstants(String filename){
        super();
        try {
            read(filename);
        } catch (IOException e) {
            System.err.println("File \"" + filename + "\" not found. Using hardcoded constant values");
        }
    }

    // Constants for AAIDistributionEvalMethods
    public int SURROUNDED_BY_OWN_CASTLES = 5;
    public int NO_ENEMY_NEIGHBOUR = 1;
    // Constants for AAIDistributionEvalMethods (pair evaluation)
    public int FIRST_CASTLE_IN_KINGDOM = 2;
    public int OTHER_CAN_CAPTURE_KINGDOM = 8;
    public int OTHER_HAS_CASTLES_IN_KINGDOM = 5;
    public int SPLIT_ENEMY_REGION = 7;
    public int CONNECTED_TO_OWN_CASTLES = 4;
    public int EXPAND_POSSIBILITIES = 3;
    public int EXPAND_POSSIBILITIES_COUNT = 4; // count of neighbours (owner == null) that expand possibilities are given
    public double FREE_NEIGHBOURS_MULTIPLIER = 4.0;

    // Constants for AAIKingdomEvalMethods
    public int SMALL_KINGDOM = 4;
    public int HAS_FEW_EDGES_TO_OTHER_KINGDOMS = 5;

    // Constants for AAITargetEvalMethods
    public int OPPORTUNITY_ELEMINATE_PLAYER = 8;
    public int BELONGS_BIG_THREAT = 3;
    public int CLOSE_TO_CAPTURE_KINGDOM = 4; // 15
    public int BREAK_UP_KINGDOM = 4;
    public int HAS_FEW_NEIGHBOURS = 4;
    public int UNITE_SPLITTED_REGIONS = 3;
    // big threat?
    public double BIG_THREAT_KINGDOM_MULTIPLIER = 3; // 19
    public double BIG_THREAT_CASTLE_MULTIPLIER = 2;
    public double BIG_THREAT_TROOP_MULTIPLIER = 0.7;
    // few neighbours
    public int FEW_NEIGHBOUR_COUNT = 1;
    // shouldAttackCastles
    public double EDGE_DIFFERENCE_MULTIPLIER = 3.0;
    public double NEIGHBOUR_TROOP_DIFFERENCE_MULTIPLIER = 2.5;
    public double TARGET_TROOP_DIFFERNECE_MULTIPLIER = 3.0;
    // getTargets
    public int MIN_ATTACK_VALUE = 5;

    // Constants for AAIDefenseEvalMethods
    public double EDGE_COUNT_MULTIPLIER = 3.0;
    public double THREATENING_TROOP_COUNT_MULTIPLIER = 2.0;

    // Constants for AAIDistributeTroopsMethods (connected evaluation)
    public double CASTLE_COUNT_MULTIPLIER = 3.0;
    public double OWNED_KINGDOM_MULTIPLIER = 1.0;
    // attack possibilities
    public int CAN_UNITE_SPLITTED_REGIONS = 6;

    // Constants for ABasicAI
    public double TROOP_DIFFERENCE_MULTIPLIER = 1.4;

    /**
     * @return an array with all values
     */
    public double[] save(){
        return new double[]{
                SURROUNDED_BY_OWN_CASTLES, NO_ENEMY_NEIGHBOUR,
                FIRST_CASTLE_IN_KINGDOM, OTHER_CAN_CAPTURE_KINGDOM, OTHER_HAS_CASTLES_IN_KINGDOM, SPLIT_ENEMY_REGION, CONNECTED_TO_OWN_CASTLES, EXPAND_POSSIBILITIES, EXPAND_POSSIBILITIES_COUNT, FREE_NEIGHBOURS_MULTIPLIER,
                SMALL_KINGDOM, HAS_FEW_EDGES_TO_OTHER_KINGDOMS,
                OPPORTUNITY_ELEMINATE_PLAYER, BELONGS_BIG_THREAT, CLOSE_TO_CAPTURE_KINGDOM, BREAK_UP_KINGDOM, HAS_FEW_NEIGHBOURS, UNITE_SPLITTED_REGIONS,
                BIG_THREAT_KINGDOM_MULTIPLIER, BIG_THREAT_CASTLE_MULTIPLIER, BIG_THREAT_TROOP_MULTIPLIER,
                FEW_NEIGHBOUR_COUNT,
                EDGE_DIFFERENCE_MULTIPLIER, NEIGHBOUR_TROOP_DIFFERENCE_MULTIPLIER, TARGET_TROOP_DIFFERNECE_MULTIPLIER,
                MIN_ATTACK_VALUE,
                EDGE_COUNT_MULTIPLIER, THREATENING_TROOP_COUNT_MULTIPLIER,
                CASTLE_COUNT_MULTIPLIER, OWNED_KINGDOM_MULTIPLIER,
                CAN_UNITE_SPLITTED_REGIONS,
                TROOP_DIFFERENCE_MULTIPLIER
        };
    }
    /**
     * @param values values to load
     */
    public void load(double[] values){
        if(values.length < valueCount)
            return;
        SURROUNDED_BY_OWN_CASTLES = (int)values[0];
        NO_ENEMY_NEIGHBOUR = (int)values[1];
        FIRST_CASTLE_IN_KINGDOM = (int)values[2];
        OTHER_CAN_CAPTURE_KINGDOM = (int)values[3];
        OTHER_HAS_CASTLES_IN_KINGDOM = (int)values[4];
        SPLIT_ENEMY_REGION = (int)values[5];
        CONNECTED_TO_OWN_CASTLES = (int)values[6];
        EXPAND_POSSIBILITIES = (int)values[7];
        EXPAND_POSSIBILITIES_COUNT = (int)values[8];
        FREE_NEIGHBOURS_MULTIPLIER = values[9];
        SMALL_KINGDOM = (int)values[10];
        HAS_FEW_EDGES_TO_OTHER_KINGDOMS = (int)values[11];
        OPPORTUNITY_ELEMINATE_PLAYER = (int)values[12];
        BELONGS_BIG_THREAT = (int)values[13];
        CLOSE_TO_CAPTURE_KINGDOM = (int)values[14];
        BREAK_UP_KINGDOM = (int)values[15];
        HAS_FEW_NEIGHBOURS = (int)values[16];
        UNITE_SPLITTED_REGIONS = (int)values[17];
        BIG_THREAT_KINGDOM_MULTIPLIER = values[18];
        BIG_THREAT_CASTLE_MULTIPLIER = values[19];
        BIG_THREAT_TROOP_MULTIPLIER = values[20];
        FEW_NEIGHBOUR_COUNT = (int)values[21];
        EDGE_DIFFERENCE_MULTIPLIER = values[22];
        NEIGHBOUR_TROOP_DIFFERENCE_MULTIPLIER = values[23];
        TARGET_TROOP_DIFFERNECE_MULTIPLIER = values[24];
        MIN_ATTACK_VALUE = (int)values[25];
        EDGE_COUNT_MULTIPLIER = values[26];
        THREATENING_TROOP_COUNT_MULTIPLIER = values[27];
        CASTLE_COUNT_MULTIPLIER = values[28];
        OWNED_KINGDOM_MULTIPLIER = values[29];
        CAN_UNITE_SPLITTED_REGIONS = (int)values[30];
        TROOP_DIFFERENCE_MULTIPLIER = values[31];
    }

    /**
     * generates random values for this object
     */
    public void generateRandom(){
        SURROUNDED_BY_OWN_CASTLES = ThreadLocalRandom.current().nextInt(0, 10);
        NO_ENEMY_NEIGHBOUR = ThreadLocalRandom.current().nextInt(0, 10);
        FIRST_CASTLE_IN_KINGDOM = ThreadLocalRandom.current().nextInt(0, 10);
        OTHER_CAN_CAPTURE_KINGDOM = ThreadLocalRandom.current().nextInt(0, 10);
        OTHER_HAS_CASTLES_IN_KINGDOM = ThreadLocalRandom.current().nextInt(0, 10);
        SPLIT_ENEMY_REGION = ThreadLocalRandom.current().nextInt(0, 10);
        CONNECTED_TO_OWN_CASTLES = ThreadLocalRandom.current().nextInt(0, 10);
        EXPAND_POSSIBILITIES = ThreadLocalRandom.current().nextInt(0, 10);
        EXPAND_POSSIBILITIES_COUNT = ThreadLocalRandom.current().nextInt(0, 10);
        FREE_NEIGHBOURS_MULTIPLIER = ThreadLocalRandom.current().nextDouble(0, 10); // DOUBLE!!!
        SMALL_KINGDOM = ThreadLocalRandom.current().nextInt(0, 10);
        HAS_FEW_EDGES_TO_OTHER_KINGDOMS = ThreadLocalRandom.current().nextInt(0, 10);
        OPPORTUNITY_ELEMINATE_PLAYER = ThreadLocalRandom.current().nextInt(0, 10);
        BELONGS_BIG_THREAT = ThreadLocalRandom.current().nextInt(0, 10);
        CLOSE_TO_CAPTURE_KINGDOM = ThreadLocalRandom.current().nextInt(0, 10);
        BREAK_UP_KINGDOM = ThreadLocalRandom.current().nextInt(0, 10);
        HAS_FEW_NEIGHBOURS = ThreadLocalRandom.current().nextInt(0, 10);
        UNITE_SPLITTED_REGIONS = ThreadLocalRandom.current().nextInt(0, 10);
        BIG_THREAT_KINGDOM_MULTIPLIER = ThreadLocalRandom.current().nextDouble(0, 10); // DOUBLE!!!
        BIG_THREAT_CASTLE_MULTIPLIER = ThreadLocalRandom.current().nextDouble(0, 10); // DOUBLE!!!
        BIG_THREAT_TROOP_MULTIPLIER = ThreadLocalRandom.current().nextDouble(0, 10); // DOUBLE!!!
        FEW_NEIGHBOUR_COUNT = ThreadLocalRandom.current().nextInt(0, 10);
        EDGE_DIFFERENCE_MULTIPLIER = ThreadLocalRandom.current().nextDouble(0, 10); // DOUBLE!!!
        NEIGHBOUR_TROOP_DIFFERENCE_MULTIPLIER = ThreadLocalRandom.current().nextDouble(0, 10); // DOUBLE!!!
        TARGET_TROOP_DIFFERNECE_MULTIPLIER = ThreadLocalRandom.current().nextDouble(0, 10); // DOUBLE!!!
        MIN_ATTACK_VALUE = ThreadLocalRandom.current().nextInt(0, 10);
        EDGE_COUNT_MULTIPLIER = ThreadLocalRandom.current().nextDouble(0, 10); // DOUBLE!!!
        THREATENING_TROOP_COUNT_MULTIPLIER = ThreadLocalRandom.current().nextDouble(0, 10); // DOUBLE!!!
        CASTLE_COUNT_MULTIPLIER = ThreadLocalRandom.current().nextDouble(0, 10); // DOUBLE!!!
        OWNED_KINGDOM_MULTIPLIER = ThreadLocalRandom.current().nextDouble(0, 10); // DOUBLE!!!
        CAN_UNITE_SPLITTED_REGIONS = ThreadLocalRandom.current().nextInt(0, 10);
        TROOP_DIFFERENCE_MULTIPLIER = ThreadLocalRandom.current().nextDouble(0, 10); // DOUBLE!!!
    }

    /**
     * writes the values to file
     * @param filename the filename of the file to write to
     * @throws IOException
     */
    public void write (String filename) throws IOException {
        double[] x = save();
        BufferedWriter outputWriter = new BufferedWriter(new FileWriter(filename));
        for (double x1 : x) {
            outputWriter.write(Double.toString(x1));
            outputWriter.newLine();
        }
        outputWriter.close();
    }
    /**
     * reads the values from a file
     * @param filename the filename of the file to read from
     * @throws IOException
     */
    public void read(String filename) throws IOException {
        List<Double> lines = new ArrayList<>(32);
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            for (String line; (line = br.readLine()) != null; ) {
                lines.add(Double.parseDouble(line));
            }
        }
        double[] values = new double[lines.size()];
        int i=0;
        for(Double value : lines){
            values[i++] = value;
        }
        load(values);
    }
}
