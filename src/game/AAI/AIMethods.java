package game.AAI;

import base.Edge;
import base.Graph;
import base.Node;
import game.Player;
import game.map.Castle;
import game.map.PathFinding;
import gui.components.MapPanel;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Alexander Muth
 * methods for an AI
 */
public class AIMethods {
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

    /**
     * @param graph the current graph
     * @param player Player which attacks
     * @param castle Castle to attack from
     * @return A list with all possible attack targets
     */
    public static List<Castle> getPossibleTargetCastles(Graph<Castle> graph, Player player, Castle castle){
        PathFinding pathFinding = new PathFinding(graph, castle, MapPanel.Action.ATTACKING, player);

        List<Castle> returnList = new LinkedList<>();
        List<Edge<Castle>> path;
        for(Node<Castle> node : graph.getNodes()){
            castle = node.getValue();
            if(castle.getOwner() != player){
                path = pathFinding.getPath(castle);
                if(path != null && !path.isEmpty()){
                    returnList.add(castle);
                }
            }
        }
        return returnList;
    }

    /**
     * Count all troops for connected castles which can used for attacking (every castle must have at least one troop)
     * saves this number in a map with one castle of this connected castle (every other castle is reachable from this
     * one)
     * @param graph the current graph
     * @param player the current player
     * @return the map with the castle and the troop count
     */
    public static Map<Castle, Integer> getPossibleAttackTroopCount(Graph<Castle> graph, Player player){
        Map<Castle, Integer> map = new HashMap<>();
        List<Castle> playerCastles = player.getCastles(graph.getAllValues());
        List<Castle> passed = new LinkedList<>();
        List<Castle> connected;
        for(Castle castle : playerCastles){
            if(!passed.contains(castle)){
                connected = getConnectedCastles(graph, castle);
                passed.add(castle);
                passed.addAll(connected);
                map.put(castle, getAllAttackTroops(connected));
            }
        }
        return map;
    }

    /**
     * @param castles a list of castles to get the sum of troops
     * @return the number of all troops minus one per castle
     */
    public static int getAllAttackTroops(List<Castle> castles){
        return castles.stream().mapToInt(Castle::getTroopCount).sum() - castles.size();
    }

    /**
     * @param graph the current graph
     * @param castle
     * @return a list of castles, which are reachable from castle and belongs to the same player
     */
    public static List<Castle> getConnectedCastles(Graph<Castle> graph, Castle castle){
        return getConnectedCastlesHelper(graph, graph.getNode(castle), new LinkedList<>());
    }
    /**
     * helper for {@link AIMethods#getConnectedCastles(Graph, Castle)}
     * @param graph graph to get edges from
     * @param node current node
     * @param passed passed nodes
     * @return a list with connected nodes
     */
    private static List<Castle> getConnectedCastlesHelper(Graph<Castle> graph, Node<Castle> node, List<Node<Castle>> passed){
        List<Castle> returnList = new LinkedList<>();
        Node<Castle> otherNode;
        for(Edge<Castle> edge : graph.getEdges(node)){
            otherNode = edge.getOtherNode(node);
            if(!passed.contains(otherNode)){
                passed.add(otherNode);
                returnList.add(otherNode.getValue());
                returnList.addAll(getConnectedCastlesHelper(graph, otherNode, passed));
            }
        }
        return returnList;
    }
}
