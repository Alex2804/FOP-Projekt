package de.teast.aai;

import base.Graph;
import game.map.Castle;
import game.map.Kingdom;

import java.util.List;

/**
 * @author Alexander Muth
 * Evaluation Methos to choose the best kingdom for an AI
 */
public class AAIKingdomEvalMethods {
    public static AAIConstantsWrapper constants = new AAIConstantsWrapper();

    /**
     * Evaluates a value for passed kingdom dependent on some factors
     * @param castleGraph the available castles
     * @param kingdom the kingdom to evaluate
     * @return the evaluated value for the kingdom object
     */
    public static int evaluateKingdom(Graph<Castle> castleGraph, Kingdom kingdom){
        List<Castle> castles = castleGraph.getAllValues();
        List<Kingdom> kingdoms = AAIMethods.getAllKingdoms(castles);
        int points = 0;

        points += isSmallKingdom(kingdoms, kingdom) ? constants.SMALL_KINGDOM : 0;
        points += hasFewEdgesToOtherKingdoms(castleGraph, kingdoms, kingdom) ? constants.HAS_FEW_EDGES_TO_OTHER_KINGDOMS : 0;

        return points;
    }

    /**
     * @param kingdoms all kingdoms to compare
     * @param kingdom the kingdom to compare
     * @return If the kingdom is small compared to the other kingdoms
     */
    public static boolean isSmallKingdom(List<Kingdom> kingdoms, Kingdom kingdom){
        int sizeSum = 0;
        for(Kingdom k : kingdoms){
            sizeSum += k.getCastles().size();
        }
        double averageSize = ((double) sizeSum) / kingdoms.size();
        return (kingdom.getCastles().size() < averageSize * 0.8); // is small if it's 20% smaller than the average size
    }

    /**
     * @param castleGraph graph conatining all edges and castles
     * @param kingdoms list with all kingdoms
     * @param kingdom kingdom to evaluate
     * @return if the kingdom has few edges compared to the other kingdoms
     */
    public static boolean hasFewEdgesToOtherKingdoms(Graph<Castle> castleGraph, List<Kingdom> kingdoms, Kingdom kingdom){
        int edgeCountSum = 0;
        for(Kingdom k : kingdoms){
            edgeCountSum += k.getEdgeCountToOtherKingdoms(castleGraph);
        }
        double averageEdgeCount = ((double) edgeCountSum) / kingdoms.size();

        return (kingdom.getEdgeCountToOtherKingdoms(castleGraph) < averageEdgeCount);
    }
}
