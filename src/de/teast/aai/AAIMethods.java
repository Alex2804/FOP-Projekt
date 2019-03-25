package de.teast.aai;

import base.Edge;
import base.Graph;
import base.Node;
import game.Player;
import game.map.Castle;
import game.map.Kingdom;

import java.util.*;
import java.util.function.Predicate;

/**
 * methods for an AI
 * @author Alexander Muth
 */
public class AAIMethods {
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
            for(List<Node<Castle>> nodeList : getPossibleCastlePairsHelper(castleGraph, player, pairSize, node, new HashSet<>())){
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

            idCastleList.put(sortedID, next);
            /*if(idCastleList.get(sortedID) == null){ // sorted id not contained
                idCastleList.put(sortedID, next); // add to contained
            }else{ // Following line modifies the input list (no need at current implementation)
                iterator.remove(); // remove because pair already exists
            }*/
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
     * Helper function for {@link AAIMethods#getPossibleCastlePairs(Graph, Player, int)}. Collects a list with all
     * possible pairs (starting at node). Only adjacent nodes could be pairs
     * @param castleGraph The graph containing all edges and nodes
     * @param player the player who is the owner of the castles (could be null)
     * @param pairSize the size of the pair ({@code pairSize} <= 1 terminates the recursion)
     * @param node the current node
     * @param passed all passed nodes (returned list mustn't contain node twice)
     * @return a list with list of nodes (list of pairs)
     */
    private static List<List<Node<Castle>>> getPossibleCastlePairsHelper(Graph<Castle> castleGraph, Player player, int pairSize, Node<Castle> node, HashSet<Node<Castle>> passed){
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
            for(Node<Castle> neighbour : getNeighbours(castleGraph, player, node)){
                if(!passed.contains(neighbour)) {
                    temp = getPossibleCastlePairsHelper(castleGraph, player, pairSize - 1, neighbour, passed);
                    for (List<Node<Castle>> list : temp) {
                        list.add(node);
                        returnList.add(list);
                    }
                }
            }
        }
        return returnList;
    }

    /**
     * @param castleGraph the graph with edges and nodes
     * @param castle the castle to get all neighbours for
     * @return A List with all Neighbour castles of {@code castle}
     */
    public static List<Castle> getAllNeighbours(Graph<Castle> castleGraph, Castle castle){
        List<Node<Castle>> neighbours = filterNeighbours(castleGraph, castleGraph.getNode(castle), c -> true);
        List<Castle> returnList = new LinkedList<>();
        for(Node<Castle> neighbour : neighbours){
            returnList.add(neighbour.getValue());
        }
        return returnList;
    }
    /**
     * @param castleGraph the graph with the edges and nodes
     * @param player the player to get the neighbour castles owned by this player
     * @param castles the castles to get the neighbours from
     * @return A List with all neighbour castles of all castles from {@code castles}, which has {@code player}
     * as owner and aren't contained by {@code castles}.
     */
    public static List<Castle> getNeighbours(Graph<Castle> castleGraph, Player player, List<Castle> castles){
        Set<Castle> castleSet = new HashSet<>(castles);
        List<Castle> returnList = new LinkedList<>();
        for(Castle castle : castles){
            for(Castle neighbour : getNeighbours(castleGraph, player, castle)){
                if(!castleSet.contains(neighbour)){
                    castleSet.add(neighbour);
                    returnList.add(neighbour);
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
     * @see #getNeighbours(Graph, Player, Node)
     */
    public static List<Castle> getNeighbours(Graph<Castle> castleGraph, Player player, Castle castle){
        List<Castle> returnList = new LinkedList<>();
        for(Node<Castle> node : getNeighbours(castleGraph, player, castleGraph.getNode(castle))){
            returnList.add(node.getValue());
        }
        return returnList;
    }
    /**
     * @param castleGraph the graph with the edges and nodes
     * @param node node to get neighbours from
     * @param player owner (could be null)
     * @return A List with all neighbour nodes of node, which castles has the player as owner
     * @see #filterNeighbours(Graph, Node, Predicate)
     */
    public static List<Node<Castle>> getNeighbours(Graph<Castle> castleGraph, Player player, Node<Castle> node){
        return filterNeighbours(castleGraph, node, c -> c.getOwner() == player);
    }
    /**
     * @param castleGraph the graph with the edges and nodes
     * @param player the player to get the neighbour castles owned by other players
     * @param castles the castles to get the other neighbours from
     * @return A List with all neighbour castles of all castles from {@code castles}, which doesn't have {@code player}
     * as owner and aren't contained by {@code castles}.
     */
    public static List<Castle> getOtherNeighbours(Graph<Castle> castleGraph, Player player, List<Castle> castles){
        Set<Castle> castleSet = new HashSet<>(castles);
        List<Castle> otherNeighbours = new LinkedList<>();
        for(Castle castle : castles){
            for(Castle neighbour : getOtherNeighbours(castleGraph, player, castle)){
                if(!castleSet.contains(neighbour)){
                    castleSet.add(neighbour);
                    otherNeighbours.add(neighbour);
                }
            }
        }
        return otherNeighbours;
    }
    /**
     * @param castleGraph the graph with the edges and nodes
     * @param castle castle to get neighbours from
     * @param player owner (could be null)
     * @return A List with all neighbour castles of castle, which hasn't the player as owner
     * @see #getOtherNeighbours(Graph, Node, Player)
     */
    public static List<Castle> getOtherNeighbours(Graph<Castle> castleGraph, Player player, Castle castle){
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
     * @see #filterNeighbours(Graph, Node, Predicate)
     */
    public static List<Node<Castle>> getOtherNeighbours(Graph<Castle> castleGraph, Node<Castle> node, Player player){
        return filterNeighbours(castleGraph, node, c -> c.getOwner() != player);
    }
    /**
     * Filters the nodes connected to a node with the given predicate
     * @param castleGraph graph containing edges and nodes
     * @param node node to check neighbours from
     * @param predicate predicate to filter
     * @return a list with the filtered nodes
     */
    public static List<Node<Castle>> filterNeighbours(Graph<Castle> castleGraph, Node<Castle> node, Predicate<Castle> predicate){
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
     * @param castles a list of castles to get the sum of TROOPS
     * @return the number of all TROOPS minus one per castle (maximum troop count to attack with)
     */
    public static int getAttackTroopCount(List<Castle> castles){
        int sum = 0;
        Set<Castle> passed = new HashSet<>();
        for(Castle castle : castles){
            if(!passed.contains(castle)) {
                passed.add(castle);
                sum += castle.getTroopCount();
            }
        }
        return sum - castles.size();
    }

    /**
     * @param castleGraph the current graph
     * @param player the player to get all connected castles from
     * @return a list, containing lists, which contains all connected castles of the passed player in the graph
     */
    public static List<List<Castle>> getConnectedCastles(Graph<Castle> castleGraph, Player player){
        return getConnectedCastles(castleGraph, castleGraph.getAllValues(), player);
    }
    /**
     * @param castleGraph the current graph
     * @param castles all castles, which could be contained in the returned list
     * @param player the player to get all connected castles from
     * @return a list, containing lists, which contains all connected castles of the passed player in the graph, which
     * are contained in castles
     */
    public static List<List<Castle>> getConnectedCastles(Graph<Castle> castleGraph, List<Castle> castles, Player player){
        List<Castle> playerCastles = player.getCastles(castleGraph.getAllValues());
        Set<Castle> passed = new HashSet<>();
        List<Castle> connected;
        List<List<Castle>> returnList = new LinkedList<>();
        for(Castle castle : playerCastles){
            if(!passed.contains(castle)){
                connected = getConnectedCastles(castleGraph, castle);
                connected.add(castle);
                passed.addAll(connected);
                returnList.add(connected);
            }
        }
        // Filter all castle's, which are not in castles
        if(castleGraph.getNodes().size() != castles.size()) { // filter only if not all nodes are allowed
            Set<Castle> usable = new HashSet<>(castles);
            ListIterator<Castle> iterator;
            for(List<Castle> list : returnList){
                iterator = list.listIterator();
                while(iterator.hasNext()){
                    if(!usable.contains(iterator.next())){
                        iterator.remove();
                    }
                }
            }
        }
        return returnList;
    }
    /**
     * @param castleGraph the current graph
     * @param castle the castle to get all connected
     * @return a list of castles, which are reachable from castle and belongs to the same player
     * as the passed {@code castle} (The list is empty if there are no connected castles and {@code castle} is not
     * contained in the returned list)
     */
    public static List<Castle> getConnectedCastles(Graph<Castle> castleGraph, Castle castle){
        Node<Castle> node = castleGraph.getNode(castle);
        return getConnectedCastlesHelper(castleGraph, node, new HashSet<>(Collections.singletonList(node)));
    }
    /**
     * helper for {@link AAIMethods#getConnectedCastles(Graph, Castle)}
     * @param castleGraph graph to get edges from
     * @param node current node
     * @param passed passed nodes
     * @return a list with connected castles (empty list if there are none)
     */
    private static List<Castle> getConnectedCastlesHelper(Graph<Castle> castleGraph, Node<Castle> node, HashSet<Node<Castle>> passed){
        List<Castle> returnList = new LinkedList<>();
        Node<Castle> otherNode;
        for(Edge<Castle> edge : castleGraph.getEdges(node)){
            otherNode = edge.getOtherNode(node);
            if(!passed.contains(otherNode) && otherNode.getValue().getOwner() == node.getValue().getOwner()){
                passed.add(otherNode);
                returnList.add(otherNode.getValue());
                returnList.addAll(getConnectedCastlesHelper(castleGraph, otherNode, passed));
            }
        }
        return returnList;
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
     * @param castleGraph graph containing all neighbours
     * @param player the player which should be the only neighbour
     * @param castle the castle to check the neighbours from
     * @return if the castle has other neighbours than the expected one (owner of castles)
     * @see #hasOtherNeighbours(Graph, List, Castle)
     */
    public static boolean hasOtherNeighbours(Graph<Castle> castleGraph, Player player, Castle castle){
        return hasOtherNeighbours(castleGraph, Collections.singletonList(player), castle);
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

    /**
     * @param castleGraph graph object containing all castles and edges
     * @param player the player to check for
     * @param castle the castle to check the neighbours
     * @return if the {@code castle} has any neighbour castle with the {@code player} as owner
     */
    public static boolean isConnectedToPlayerCastles(Graph<Castle> castleGraph, Player player, Castle castle){
        for(Castle neighbour : getNeighbours(castleGraph, player, castle)){
            if(neighbour.getOwner() == player)
                return true;
        }
        return false;
    }
    /**
     * @param castles the castles to search
     * @return The castle with the highest troop count or null if {@code castles} is empty.
     */
    public static Castle getCastleWithMostTroops(List<Castle> castles){
        Castle bestCastle = null;
        for(Castle castle : castles){
            if(bestCastle == null || castle.getTroopCount() > bestCastle.getTroopCount()){
                bestCastle = castle;
            }
        }
        return bestCastle;
    }

    /**
     * @param castleGraph the graph containing all edges and (castle)nodes
     * @param player the player to check other players
     * @param connectedCastles the region to check the neighbours
     * @return the amount of TROOPS, which can attack the region of {@code connectedCastles}
     */
    public static int getNeighboursAttackTroopCount(Graph<Castle> castleGraph, Player player, List<Castle> connectedCastles){
        int sum = 0;
        Set<Castle> passed = new HashSet<>(connectedCastles);
        List<Castle> connected;
        for(Castle neighbour : getOtherNeighbours(castleGraph, player, connectedCastles)){
            if(!passed.contains(neighbour)){
                connected = getConnectedCastles(castleGraph, neighbour);
                connected.add(neighbour);
                passed.addAll(connected);
                sum += getAttackTroopCount(connected);
            }
        }
        return sum;
    }
}
