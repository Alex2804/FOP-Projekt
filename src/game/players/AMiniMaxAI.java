package game.players;

import base.Edge;
import base.Graph;
import base.Node;
import game.AI;
import game.Game;
import game.map.Castle;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AMiniMaxAI extends AI {

    public AMiniMaxAI(String name, Color color) {
        super(name, color);
    }

    @Override
    protected void actions(Game game) throws InterruptedException {

    }

    @Override
    public AMiniMaxAI copy() {
        AMiniMaxAI miniMaxAI = new AMiniMaxAI(getName(), getColor());
        miniMaxAI.fastForward = fastForward;
        miniMaxAI.addPoints(getPoints());
        miniMaxAI.addTroops(getRemainingTroops());
        miniMaxAI.hasJoker = hasJoker;
        return miniMaxAI;
    }

    /**
     * Copy a castle graph with all nodes and edges (castles are copied also)
     * @param graph the graph to copy
     * @return a copy of the graph
     */
    public static Graph<Castle> copyCastleGraph(Graph<Castle> graph) {
        Graph<Castle> copyGraph = new Graph<>();
        Map<Node<Castle>, Node<Castle>> map = new HashMap<>();
        for(Node<Castle> node : graph.getNodes()) {
            map.put(node, copyGraph.addNode(node.getValue().copy()));
        }
        Node<Castle> nodeA, nodeB;
        for(Edge<Castle> edge : graph.getEdges()) {
            nodeA = map.get(edge.getNodeA());
            nodeB = map.get(edge.getNodeB());
            if(nodeA != null && nodeB != null)
                graph.addEdge(nodeA, nodeB);
        }
        return copyGraph;
    }
}
