package game.AAI;

import base.Edge;
import base.Graph;
import base.Node;
import game.Player;
import game.map.Castle;
import game.map.Kingdom;
import game.map.PathFinding;
import gui.components.MapPanel;
import javafx.util.Pair;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Alexander Muth
 * methods for an AI independent from their implementation
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
     * searches for the best values
     * @param list containing pairs, with keys as values and values as ratings
     * @param count count of elements to search for
     * @param <T> generic type of the value
     * @return a list containing the values with the best rating
     * @see #getBestPairs(Collection, int)
     */
    public static<T> List<T> getBest(Collection<Pair<T, Integer>> list, int count){
        return getBestPairs(list, count).stream().map(Pair::getKey).collect(Collectors.toList());
    }
    public static<T> List<Pair<T, Integer>> getBestPairs(Collection<Pair<T, Integer>> list, int count){
        List<Pair<T, Integer>> returnList = list.stream()
                                                .sorted(Comparator.comparingInt(Pair::getValue))
                                                .collect(Collectors.toList());
        return (returnList.size() <= count) ? returnList : returnList.subList(0, count);
    }
    /**
     * searches for the pair (a pair is an inner list, not the {@link Pair} inside) with the biggest sum
     * of the rating (the rating of each component is the value in the {@link Pair})
     * @param pairs collection of pairs
     * @param <T> generic type of the value
     * @return the keys of the best pair (without the rating)
     */
    public static<T> List<T> getBestPair(Collection<List<Pair<T, Integer>>> pairs){
        int bestRating = Integer.MAX_VALUE, rating;
        Collection<Pair<T, Integer>> bestPair = null;
        for(Collection<Pair<T, Integer>> list : pairs){
            rating = 0;
            for(Pair<T, Integer> pair : list){
                rating += pair.getValue(); // value is the rating
            }
            if(bestPair == null || bestRating < rating){
                bestPair = list;
                bestRating = rating;
            }
        }

        List<T> returnList = new LinkedList<>();
        for(Pair<T, Integer> pair : bestPair){ // foreach is faster than stream
            returnList.add(pair.getKey());
        }
        return returnList; // only return the keys
    }

    /**
     * Collects a list with all possible pairs. Only adjacent castles could be pairs
     * @param castleGraph The graph containing all edges and nodes
     * @param player the player who is the owner of the castles (could be null)
     * @param pairSize the size of the pairs
     * @return a list with list of castles (list of pairs)
     */
    public static List<List<Castle>> getPossibleCastlePairs(Graph<Castle> castleGraph, Player player, int pairSize){
        if(pairSize <= 0)
            return new LinkedList<>();
        List<List<Castle>> returnList = new LinkedList<>();
        List<Castle> temp;
        for(Node<Castle> node : Player.getCastleNodes(castleGraph.getNodes(), player)){
            for(List<Node<Castle>> nodeList : getPossibleCastlePairsHelper(castleGraph, player, pairSize, node, new LinkedList<>())){
                temp = new LinkedList<>();
                for(Node<Castle> n : nodeList){
                    temp.add(n.getValue());
                }
                returnList.add(temp);
            }

        }
        return getPossibleCastlePairsRemoveDuplicates(returnList);
    }
    /**
     * @param castles list to check for duplicates
     * @return a list without all duplicates in {@code castles}
     */
    private static List<List<Castle>> getPossibleCastlePairsRemoveDuplicates(List<List<Castle>> castles){
        Map<Castle, Integer> castleID = new HashMap<>();
        Map<String, List<Castle>> idCastleList = new HashMap<>(); // store unique ids associated with each list
        int currentID = 0;
        Integer id;
        String sortedID;
        List<Integer> ids = new LinkedList<>();
        ListIterator<List<Castle>> iterator = castles.listIterator();
        List<Castle> next;
        while(iterator.hasNext()){
            next = iterator.next();

            for(Castle castle : next){ // for each castle
                id = castleID.get(castle); // get id of castle
                if(id == null){ // if there is no id
                    id = currentID++; // get new id
                    castleID.put(castle, id); // connect castle and id
                }
                ids.add(id); // add id to id list
            }

            sortedID = sortIDs(ids); // sort id's

            if(idCastleList.get(sortedID) == null){ // sorted id not contained
                idCastleList.put(sortedID, next); // add to contained
            }/**else{ // Following line modifies the input list (no need at current implementation)
                iterator.remove(); // remove because pair already exists
            }**/
        }

        return new LinkedList<>(idCastleList.values());
    }
    /**
     * This method sorts ids in ascending order (1;2;3;...;)
     * @param ids the ids to sort and convert to string
     * @return the sorted ids as string
     */
    public static String sortIDs(List<Integer> ids){
        Collections.sort(ids);
        StringBuilder sb = new StringBuilder();
        for(int id : ids){
            sb.append(id).append(";");
        }
        return sb.toString();
    }
    /**
     * Helper function for {@link AIMethods#getPossibleCastlePairs(Graph, Player, int)}. Collects a list with all
     * possible pairs (starting at node). Only adjacent nodes could be pairs
     * @param castleGraph The graph containing all edges and nodes
     * @param player the player who is the owner of the castles (could be null)
     * @param pairSize the size of the pair ({@code pairSize} <= 1 terminates the recursion)
     * @param node the current node
     * @param passed all passed nodes (returned list mustn't contain node twice)
     * @return a list with list of nodes (list of pairs)
     */
    private static List<List<Node<Castle>>> getPossibleCastlePairsHelper(Graph<Castle> castleGraph, Player player, int pairSize, Node<Castle> node, List<Node<Castle>> passed){
        List<List<Node<Castle>>> returnList = new LinkedList<>();
        if(pairSize <= 0) {
            return returnList;
        }else if(pairSize == 1) {
            List<Node<Castle>> temp = new LinkedList<>();
            temp.add(node);
            returnList.add(temp);
        }else{
            passed.add(node);
            List<List<Node<Castle>>> temp;
            for(Node<Castle> neighbour : getNeighbours(castleGraph, node, player)){
                if(!passed.contains(neighbour)) {
                    temp = getPossibleCastlePairsHelper(castleGraph, player, pairSize - 1, neighbour, passed);
                    if(temp != null){
                        for (List<Node<Castle>> list : temp) {
                            list.add(node);
                            returnList.add(list);
                        }
                    }
                }
            }
        }
        return returnList;
    }

    /**
     * @param castleGraph the graph with the edges and nodes
     * @param castle castle to get neighbours from
     * @param player owner (could be null)
     * @return A List with all neighbour castles of castle, which has the player as owner
     * @see #getNeighbours(Graph, Node, Player)
     */
    public static List<Castle> getNeighbours(Graph<Castle> castleGraph, Castle castle, Player player){
        List<Castle> returnList = new LinkedList<>();
        for(Node<Castle> node : getNeighbours(castleGraph, castleGraph.getNode(castle), player)){
            returnList.add(node.getValue());
        }
        return returnList;
    }
    /**
     * @param castleGraph the graph with the edges and nodes
     * @param node node to get neighbours from
     * @param player owner (could be null)
     * @return A List with all neighbour nodes of node, which castles has the player as owner
     * @see #filterNeightbours(Graph, Node, Predicate)
     */
    public static List<Node<Castle>> getNeighbours(Graph<Castle> castleGraph, Node<Castle> node, Player player){
        return filterNeightbours(castleGraph, node, c -> c.getOwner() == player);
    }
    /**
     * @param castleGraph the graph with the edges and nodes
     * @param castle castle to get neighbours from
     * @param player owner (could be null)
     * @return A List with all neighbour castles of castle, which hasn't the player as owner
     * @see #getOtherNeighbours(Graph, Node, Player)
     */
    public static List<Castle> getOtherNeighbours(Graph<Castle> castleGraph, Castle castle, Player player){
        List<Castle> returnList = new LinkedList<>();
        for(Node<Castle> node : getOtherNeighbours(castleGraph, castleGraph.getNode(castle), player)){
            returnList.add(node.getValue());
        }
        return returnList;
    }
    /**
     * @param castleGraph the graph containing the edges and nodes
     * @param node node to get the neighbours from
     * @param player none owner (could be null)
     * @return A List with all neighbour nodes of node, which castles hasn't the player as owner
     * @see #filterNeightbours(Graph, Node, Predicate)
     */
    public static List<Node<Castle>> getOtherNeighbours(Graph<Castle> castleGraph, Node<Castle> node, Player player){
        return filterNeightbours(castleGraph, node, c -> c.getOwner() != player);
    }
    /**
     * Filteres the nodes connected to a node with the given predicate
     * @param castleGraph graph containing edges and nodes
     * @param node node to check neighbours from
     * @param predicate predicate to filter
     * @return a list with the filtered nodes
     */
    public static List<Node<Castle>> filterNeightbours(Graph<Castle> castleGraph, Node<Castle> node, Predicate<Castle> predicate){
        List<Node<Castle>> neighbourList = castleGraph.getNeighbours(node);
        ListIterator<Node<Castle>> iterator = neighbourList.listIterator();
        Node<Castle> next;
        while (iterator.hasNext()){
            next = iterator.next();
            if(!predicate.test(next.getValue())){
                iterator.remove();
            }
        }
        return neighbourList;
    }

    /**
     * @param castleGraph the current graph
     * @param player Player which attacks
     * @param castle Castle to attack from
     * @return A list with all possible attack targets
     */
    public static List<Castle> getPossibleTargetCastles(Graph<Castle> castleGraph, Player player, Castle castle){
        PathFinding pathFinding = new PathFinding(castleGraph, castle, MapPanel.Action.ATTACKING, player);

        List<Castle> returnList = new LinkedList<>();
        List<Edge<Castle>> path;
        for(Node<Castle> node : castleGraph.getNodes()){
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
     * Count all troops for connected castles which can be used for attacks (every castle must keep at least one troop)
     * saves this number in a map with one castle of this connected castle (every other castle is reachable from this
     * one)
     * @param castleGraph the current graph
     * @param player the current player
     * @return the map with the castle and the troop count
     */
    public static Map<Castle, Integer> getPossibleAttackTroopCount(Graph<Castle> castleGraph, Player player){
        Map<Castle, Integer> map = new HashMap<>();
        for(List<Castle> connected : getConnectedCastles(castleGraph, player)){
            if(!connected.isEmpty()){
                map.put(connected.get(0), getAttackTroopCount(connected));
            }
        }
        return map;
    }

    /**
     * @param castles a list of castles to get the sum of troops
     * @return the number of all troops minus one per castle (maximum troop count to attack with)
     */
    public static int getAttackTroopCount(List<Castle> castles){
        int sum = 0;
        for(Castle castle : castles){
            sum += castle.getTroopCount();
        }
        return sum - castles.size();
    }

    /**
     * @param castleGraph the current graph
     * @param player the player to get all connected castles from
     * @return a list, containing lists, which contains all connected castles of the passed player in the graph
     */
    public static List<List<Castle>> getConnectedCastles(Graph<Castle> castleGraph, Player player){
        List<Castle> playerCastles = player.getCastles(castleGraph.getAllValues());
        List<Castle> passed = new LinkedList<>(), connected;
        List<List<Castle>> returnList = new LinkedList<>();
        for(Castle castle : playerCastles){
            if(!passed.contains(castle)){
                connected = getConnectedCastles(castleGraph, castle);
                connected.add(castle);
                passed.addAll(connected);
                returnList.add(connected);
            }
        }
        return returnList;
    }

    /**
     * @param castleGraph the current graph
     * @param castle
     * @return a list of castles, which are reachable from castle and belongs to the same player
     */
    public static List<Castle> getConnectedCastles(Graph<Castle> castleGraph, Castle castle){
        return getConnectedCastlesHelper(castleGraph, castleGraph.getNode(castle), new LinkedList<>());
    }
    /**
     * helper for {@link AIMethods#getConnectedCastles(Graph, Castle)}
     * @param castleGraph graph to get edges from
     * @param node current node
     * @param passed passed nodes
     * @return a list with connected nodes
     */
    private static List<Castle> getConnectedCastlesHelper(Graph<Castle> castleGraph, Node<Castle> node, List<Node<Castle>> passed){
        List<Castle> returnList = new LinkedList<>();
        Node<Castle> otherNode;
        for(Edge<Castle> edge : castleGraph.getEdges(node)){
            otherNode = edge.getOtherNode(node);
            if(!passed.contains(otherNode)){
                passed.add(otherNode);
                returnList.add(otherNode.getValue());
                returnList.addAll(getConnectedCastlesHelper(castleGraph, otherNode, passed));
            }
        }
        return returnList;
    }

    /**
     * @param castles all castles
     * @param kingdom the kingdom whose castles should not be contained in the returned list
     * @return A list with all castles which doesn't belong to {@code kingdom}
     */
    public static List<Castle> getCastlesFromOtherKingdoms(List<Castle> castles, Kingdom kingdom){
        List<Castle> others = new LinkedList<>();
        for(Castle castle : castles){
            if(castle != null && castle.getKingdom() != kingdom){
                others.add(castle);
            }
        }
        return others;
    }

    /**
     * @param castles A list with castles
     * @return A list with all kingdoms, where any castle from castles are in
     */
    public static List<Kingdom> getAllKingdoms(List<Castle> castles){
        List<Kingdom> kingdoms = new LinkedList<>();
        for(Castle castle : castles){
            if(castle.getKingdom() != null && !kingdoms.contains(castle.getKingdom()))
                kingdoms.add(castle.getKingdom());
        }
        return kingdoms;
    }

    /**
     * @param entrys the entrys to transform
     * @param <T> Generic type of the key
     * @param <U> Generic type of the value
     * @return a list of pairs, with the same order and keys/values like the passed entrys
     */
    public static<T, U> List<Pair<T, U>> entrysToPairs(Collection<Map.Entry<T, U>> entrys){
        List<Pair<T, U>> returnList = new LinkedList<>();
        for(Map.Entry<T, U> entry : entrys){
            returnList.add(new Pair<>(entry.getKey(), entry.getValue()));
        }
        return returnList;
    }

    /**
     * @param castleGraph graph containing all neighbours
     * @param player the player which should be the only neighbour
     * @param castle the castle to check the neighbours from
     * @return if the castle has other neighbours than the expected one (owner of castles)
     * @see #hasOtherNeighbours(Graph, List, Castle)
     */
    public static boolean hasOtherNeighbours(Graph<Castle> castleGraph, Player player, Castle castle){
        List<Player> temp = new LinkedList<>();
        temp.add(player);
        return hasOtherNeighbours(castleGraph, temp, castle);
    }
    /**
     * Returns if the castle has other neighbours than the given players
     * @param castleGraph graph containing all neighbours
     * @param players the players which should be the only neighbours
     * @param castle the castle to check the neighbours from
     * @return if the castle has other neighbours than the expected (owner of castles)
     */
    public static boolean hasOtherNeighbours(Graph<Castle> castleGraph, List<Player> players, Castle castle){
        Node<Castle> node = castleGraph.getNode(castle);
        if(node == null)
            return false;
        boolean temp;
        for(Edge<Castle> edge : castleGraph.getEdges(node)){
            temp = false;
            for(Player player : players){
                if(edge.getOtherNode(node).getValue().getOwner() == player)
                    temp = true;
            }
            if(!temp)
                return true;
        }
        return false;
    }
}
