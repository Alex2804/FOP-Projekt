package de.teast;

import base.Edge;
import base.Graph;
import base.GraphAlgorithm;
import base.Node;
import game.map.Castle;

/**
 * Class to get the edge count (range) from one castle to another
 * @author Alexander Muth
 */
public class ARangePathFinding extends GraphAlgorithm<Castle> {
    public ARangePathFinding(Graph<Castle> castleGraph, Castle source){
        super(castleGraph, castleGraph.getNode(source));
    }

    /**
     * @param destination the destination
     * @return the range (count of edges) to the destination
     */
    public int getRange(Castle destination){
        return getPath(getGraph().getNode(destination)).size();
    }
    /**
     * @param castleGraph the castle graph
     * @param source the source castle
     * @param destination the destination castle
     * @return the range in the {@code castleGraph} from {@code source} to {@code destination}
     *
     * @see #getRange(Castle)
     */
    public static int getRange(Graph<Castle> castleGraph, Castle source, Castle destination){
        if(source == destination)
            return 0;
        ARangePathFinding pathFinding = new ARangePathFinding(castleGraph, source);
        pathFinding.run();
        return pathFinding.getRange(destination);
    }

    @Override
    protected double getValue(Edge<Castle> edge) {
        return 1;
    }
    @Override
    protected boolean isPassable(Edge<Castle> edge) {
        return true;
    }
    @Override
    protected boolean isPassable(Node<Castle> node) {
        return true;
    }
}
