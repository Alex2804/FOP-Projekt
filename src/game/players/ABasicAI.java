package game.players;

import base.Edge;
import base.Graph;
import base.Node;
import game.AI;
import game.AIConstants;
import game.Game;
import game.Player;
import game.map.Castle;
import game.map.Kingdom;
import game.map.PathFinding;
import gui.components.MapPanel;

import java.awt.Color;
import java.util.*;
import java.util.stream.Collectors;

public class ABasicAI extends AI {
    /**
     * Wrapps a class arround a specific object to store an associated value for it
     * @param <T> The generic type of the stored object
     */
    private static class ValueWrapper<T> {
        int valuePoints;
        T value;
        public ValueWrapper(T value){
            this.value = value;
            valuePoints = 0;
        }
        public ValueWrapper(T value, int valuePoints){
            this.value = value;
            this.valuePoints = valuePoints;
        }
    }

    public ABasicAI(String name, Color color) {
        super(name, color);
    }

    @Override
    protected void actions(Game game) throws InterruptedException {

    }

    /**
     * @param game the current Game object
     * @param player Player which attacks
     * @param castle Castle to attack from
     * @return A list with all possible attack targets
     */
    private static List<Castle> getPossibleTargetCastles(Game game, Player player, Castle castle){
        Graph<Castle> graph = game.getMap().getGraph();
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
     * Evaluates a value for passed castle dependent on some factors
     * @param game the game object
     * @param player the player to evaluate for
     * @param castle the castle to evaluate
     * @return a wrapper with the evaluated points and the castle object
     */
    private static int evaluateCastle(Game game, Player player, Castle castle){
        int points = 0;

        points += isLastCastleOfPlayer(game.getMap().getCastles(), castle) ? AIConstants.OPPORTUNITY_ELEMINATE_PLAYER : 0;
        points += isBigThreat(game, player, castle.getOwner()) ? AIConstants.BELONGS_BIG_THREAT : 0;
        if(castle.getKingdom() != null){
            points += isImportantKingdom(player, castle.getKingdom()) ? AIConstants.IMPORTANT_KINGDOM : 0;
            points += isCloseToCaptureKingdom(player, castle.getKingdom()) ? AIConstants.CLOSE_TO_CAPTURE_KINGDOM : 0;
            points += isOwnedByOnePlayer(castle.getKingdom()) ? AIConstants.BREAK_UP_KINGDOM : 0;
            points += isLastCastleInKingdom(player, castle) ? AIConstants.LAST_CASTLE_IN_KINGDOM : 0;
        }


        return points;
    }

    /**
     * Checks if the castle is the last castle, which belongs to the owner in the list of castles
     * @param castles list of castles
     * @param castle castle to check owner
     * @return if the castle is the last castle of the owner
     */
    private static boolean isLastCastleOfPlayer(List<Castle> castles, Castle castle){
        return !castles.stream().anyMatch(c -> c.getOwner() == castle.getOwner() && c != castle);
    }

    /**
     * Checks if a Player is a big threat for another player
     * @param player
     * @param other player to check if it is a big threat
     * @return if the other player is a big threat for player
     */
    private static boolean isBigThreat(Game game, Player player, Player other){
        return player.getCastles(game).size() <= other.getCastles(game).size();
    }

    /**
     * checks if a player is (one of) the strongest player in a kingdom
     * @param player player to check if is (one of) the strongest
     * @param kingdom kingdom to check if player is (one of) the strongest
     * @return if the player is (one of) the strongest players in the kingdom
     */
    private static boolean isImportantKingdom(Player player, Kingdom kingdom){
        kingdom.getCastles();
        Integer otherIndex;
        int playerPoints = 0;
        List<Integer> otherPoints = new ArrayList<>();
        Map<Player, Integer> indices = new HashMap<>();
        for(Castle castle : kingdom.getCastles()){
            if(castle.getOwner() == player){
                playerPoints += 1;
            }else{
                otherIndex = indices.get(castle.getOwner());
                if(otherIndex == null){
                    otherIndex = otherPoints.size();
                    indices.put(castle.getOwner(), otherIndex);
                    otherPoints.add(1);
                }else{
                    otherPoints.set(otherIndex, otherPoints.get(otherIndex) + 1);
                }
            }
        }

        for(int p : otherPoints){
            if(p > playerPoints)
                return false;
        }
        return true;
    }

    /**
     * Checks if a player is close to capture a kingdom
     * @param player player to check for
     * @param kingdom kingdom to check for
     * @return if the player is close to capture the kingdom
     */
    private static boolean isCloseToCaptureKingdom(Player player, Kingdom kingdom){
        double playerCastleCount =  kingdom.getCastles().stream().filter(c -> c.getOwner() != player).collect(Collectors.toList()).size();
        double percentage = playerCastleCount / kingdom.getCastles().size();
        return percentage >= 0.7; // more than 70% of the kingdom is owned by the player
    }

    /**
     * @param kingdom kingdom to check
     * @return if the kingdom is owned by one player
     */
    private static boolean isOwnedByOnePlayer(Kingdom kingdom){
        Player owner = null;
        for(Castle castle : kingdom.getCastles()){
            if(owner == null)
                owner = castle.getOwner();
            else if(owner != castle.getOwner())
                return false;
        }
        return true;
    }

    /**
     * Checks if the castle is the last castle in the kingdom, owned by another player than the passed one
     * @param player
     * @param castle
     * @return if the castle is the last one owned by another player
     */
    private static boolean isLastCastleInKingdom(Player player, Castle castle){
        return !castle.getKingdom().getCastles().stream().anyMatch(c -> c.getOwner() != player && c != castle);
    }

    @Override
    public ABasicAI copy() {
        ABasicAI aBasicAI = new ABasicAI(getName(), getColor());
        aBasicAI.fastForward = fastForward;
        aBasicAI.addPoints(getPoints());
        aBasicAI.addTroops(getRemainingTroops());
        aBasicAI.hasJoker = hasJoker;
        return aBasicAI;
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
