package de.teast;

import base.Edge;
import base.Graph;
import base.GraphAlgorithm;
import base.Node;
import game.map.Castle;

public class ARangePathFinding extends GraphAlgorithm<Castle> {
    public ARangePathFinding(Graph<Castle> castleGraph, Castle source){
        super(castleGraph, castleGraph.getNode(source));
    }

    public int getRange(Castle destination){
        return getPath(getGraph().getNode(destination)).size();
    }
    public static int getRange(Graph<Castle> castleGraph, Castle source, Castle destination){
        if(source == destination)
            return 0;
        ARangePathFinding pathFinding = new ARangePathFinding(castleGraph, source);
        pathFinding.run();
        return pathFinding.getRange(destination);
    }

    @Override
    protected double getValue(Edge edge) {
        return 1;
    }
    @Override
    protected boolean isPassable(Edge edge) {
        return true;
    }
    @Override
    protected boolean isPassable(Node node) {
        return true;
    }
}
