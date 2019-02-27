package base;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import game.map.Castle;

/**
 * Diese Klasse representiert einen generischen Graphen mit einer Liste aus Knoten und Kanten.
 *
 * @param <T> Die zugrundeliegende Datenstruktur, beispielsweise {@link game.map.Castle}
 */
public class Graph<T> {

    private List<Edge<T>> edges;
    private List<Node<T>> nodes;

    /**
     * Konstruktor für einen neuen, leeren Graphen
     */
    public Graph() {
        this.nodes = new ArrayList<>();
        this.edges = new LinkedList<>();
    }

    /**
     * Einen neuen Knoten zum Graphen hinzufügen
     * @param value Der Wert des Knotens
     * @return Der erstellte Knoten
     */
    public Node<T> addNode(T value) {
        Node<T> node = new Node<>(value);
        this.nodes.add(node);
        return node;
    }

    /**
     * Eine neue Kante zwischen zwei Knoten hinzufügen. Sollte die Kante schon existieren, wird die vorhandene Kante zurückgegeben.
     * @param nodeA Der erste Knoten
     * @param nodeB Der zweite Knoten
     * @return Die erstellte oder bereits vorhandene Kante zwischen beiden gegebenen Knoten
     */
    public Edge<T> addEdge(Node<T> nodeA, Node<T> nodeB) {
        Edge<T> edge = getEdge(nodeA, nodeB);
        if(edge != null) {
            return edge;
        }

        edge = new Edge<>(nodeA, nodeB);
        this.edges.add(edge);
        return edge;
    }

    /**
     * Gibt die Liste aller Knoten zurück
     * @return die Liste aller Knoten
     */
    public List<Node<T>> getNodes() {
        return this.nodes;
    }

    /**
     * Gibt die Liste aller Kanten zurück
     * @return die Liste aller Kanten
     */
    public List<Edge<T>> getEdges() {
        return this.edges;
    }

    /**
     * Diese Methode gibt alle Werte der Knoten in einer Liste mittels Streams zurück.
     * @see java.util.stream.Stream#map(Function)
     * @see java.util.stream.Stream#collect(Collector)
     * @return Eine Liste aller Knotenwerte
     */
    public List<T> getAllValues() {
        return nodes.stream().map(Node::getValue).collect(Collectors.toList());
    }

    /**
     * Diese Methode gibt alle Kanten eines Knotens als Liste mittels Streams zurück.
     * @param node Der Knoten für die dazugehörigen Kanten
     * @see java.util.stream.Stream#filter(Predicate)
     * @see java.util.stream.Stream#collect(Collector)
     * @return Die Liste aller zum Knoten zugehörigen Kanten
     */
    public List<Edge<T>> getEdges(Node<T> node) {
        return edges.stream().filter(e -> e.contains(node)).collect(Collectors.toList());
    }
    /**
     * @param nodes The nodes for the edges
     * @return all edges for all the nodes
     */
    public List<Edge<T>> getEdges(List<Node<T>> nodes){
        return edges.stream().filter(e -> nodes.stream().anyMatch(n -> e.contains(n))).collect(Collectors.toList());
    }

    /**
     * Diese Methode sucht eine Kante zwischen beiden angegebenen Knoten und gibt diese zurück
     * oder null, falls diese Kante nicht existiert
     * @param nodeA Der erste Knoten
     * @param nodeB Der zweite Knoten
     * @return Die Kante zwischen beiden Knoten oder null
     */
    public Edge<T> getEdge(Node<T> nodeA, Node<T> nodeB) {
        return edges.stream().filter(e -> e.contains(nodeA) && e.contains(nodeB)).findFirst().orElse(null);
    }

    /**
     * Gibt den ersten Knoten mit dem angegebenen Wert zurück oder null, falls dieser nicht gefunden wurde
     * @param value Der zu suchende Wert
     * @return Ein Knoten mit dem angegebenen Wert oder null
     */
    public Node<T> getNode(T value) {
        return nodes.stream().filter(n -> value.equals(n.getValue())).findFirst().orElse(null);
    }
    /**
     * Gibt eine Liste an Knoten mit einem der angegebenen Werte zurück
     * @param values die zu suchenden Werte
     * @return Eine Liste mit Knoten mit den angegebenen werten
     */
    public List<Node<T>> getNodes(List<T> values) {
        return nodes.stream().filter(n -> values.stream().anyMatch(m -> n.getValue().equals(m))).collect(Collectors.toList());
    }
    
    /**
     * Überprüft, ob alle Knoten in dem Graphen erreichbar sind.
     * @return true, wenn alle Knoten erreichbar sind, false wenn nicht
     */
    public boolean allNodesConnected() {
        if(nodes.size() == 0){
            return true;
        }
        return (nodes.size() == allNodesConnectedHelper(nodes.get(0), new ArrayList<>(Arrays.asList(nodes.get(0))), getEdges(nodes.get(0))).size());
    }

    /**
     * helper function for allNodesConnected
     * checks recursively if all nodes are reachable
     * @param node current node
     * @param nodes passed nodes
     * @param edges edges of current node
     * @return a list with all reachable nodes from the current node
     */
    private List<Node<T>> allNodesConnectedHelper(Node<T> node, List<Node<T>> nodes, List<Edge<T>> edges){
        Node<T> other;
        for(Edge<T> edge : edges){
            other = edge.getOtherNode(node);
            if(!nodes.contains(other)){
                nodes.add(other);
                nodes = allNodesConnectedHelper(other, nodes, getEdges(other));
            }
        }
        return nodes;
    }
}
