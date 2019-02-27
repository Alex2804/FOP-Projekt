package game.map;

import base.*;
import game.GameConstants;
import gui.Resources;
import javafx.util.Pair;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Diese Klasse representiert das Spielfeld. Sie beinhaltet das Hintergrundbild, welches mit Perlin noise erzeugt wurde,
 * eine Liste mit Königreichen und alle Burgen und deren Verbindungen als Graphen.
 *
 * Die Karte wird in mehreren Schritten generiert, siehe dazu {@link #generateRandomMap(int, int, int, int, int)}
 */
public class GameMap {

    private BufferedImage backgroundImage;
    private Graph<Castle> castleGraph;
    private List<Kingdom> kingdoms;

    // Map Generation
    private double[][] noiseValues;
    private int width, height, scale;

    /**
     * Erzeugt eine neue leere Karte. Der Konstruktor sollte niemals direkt aufgerufen werden.
     * Um eine neue Karte zu erstellen, muss {@link #generateRandomMap(int, int, int, int, int)} verwendet werden
     * @param width die Breite der Karte
     * @param height die Höhe der Karte
     * @param scale der Skalierungsfaktor
     */
    private GameMap(int width, int height, int scale) {
        this.castleGraph = new Graph<>();
        this.width = width;
        this.height = height;
        this.scale = scale;
    }

    /**
     * Wandelt einen Noise-Wert in eine Farbe um. Die Methode kann nach belieben angepasst werden
     * @param value der Perlin-Noise-Wert
     * @return die resultierende Farbe
     */
    private Color doubleToColor(double value) {
        if (value <= 0.40)
            return GameConstants.COLOR_WATER;
        else if (value <= 0.5)
            return GameConstants.COLOR_SAND;
        else if (value <= 0.7)
            return GameConstants.COLOR_GRASS;
        else if (value <= 0.8)
            return GameConstants.COLOR_STONE;
        else
            return GameConstants.COLOR_SNOW;
    }

    /**
     * Hier wird das Hintergrund-Bild mittels Perlin-Noise erzeugt.
     * Siehe auch: {@link PerlinNoise}
     */
    private void generateBackground() {
        PerlinNoise perlinNoise = new PerlinNoise(width, height, scale);
        Dimension realSize = perlinNoise.getRealSize();

        noiseValues = new double[realSize.width][realSize.height];
        backgroundImage = new BufferedImage(realSize.width, realSize.height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < realSize.width; x++) {
            for (int y = 0; y < realSize.height; y++) {
                double noiseValue = perlinNoise.getNoise(x, y);
                noiseValues[x][y] = noiseValue;
                backgroundImage.setRGB(x, y, doubleToColor(noiseValue).getRGB());
            }
        }
    }

    /**
     * Hier werden die Burgen erzeugt.
     * Dabei wir die Karte in Felder unterteilt, sodass auf jedes Feld maximal eine Burg kommt.
     * Sollte auf einem Feld keine Position für eine Burg existieren (z.B. aufgrund von Wasser oder angrenzenden Burgen), wird dieses übersprungen.
     * Dadurch kann es vorkommen, dass nicht alle Burgen generiert werden
     * @param castleCount die maximale Anzahl der zu generierenden Burgen
     */
    private void generateCastles(int castleCount) {
        double square = Math.ceil(Math.sqrt(castleCount));
        double length = width + height;

        int tilesX = (int) Math.max(1, (width / length + 0.5) * square) + 5;
        int tilesY = (int) Math.max(1, (height / length + 0.5) * square) + 5;
        int tileW = (width * scale / tilesX);
        int tileH = (height * scale / tilesY);

        if (tilesX * tilesY < castleCount) {
            throw new IllegalArgumentException(String.format("CALCULATION Error: tilesX=%d * tilesY=%d < castles=%d", tilesX, tilesY, castleCount));
        }

        // Add possible tiles
        List<Point> possibleFields = new ArrayList<>(tilesX * tilesY);
        for (int x = 0; x < tilesX - 1; x++) {
            for (int y = 0; y < tilesY - 1; y++) {
                possibleFields.add(new Point(x, y));
            }
        }

        // Generate castles
        List<String> possibleNames = generateCastleNames();
        int castlesGenerated = 0;
        while (possibleFields.size() > 0 && castlesGenerated < castleCount) {
            Point randomField = possibleFields.remove((int) (Math.random() * possibleFields.size()));
            int x0 = (int) ((randomField.x + 0.5) * tileW);
            int y0 = (int) ((randomField.y + 0.5) * tileH);

            for (int x = (int) (0.5 * tileW); x >= 0; x--) {
                boolean positionFound = false;
                for (int y = (int) (0.5 * tileH); y >= 0; y--) {
                    int x_mid = (int) (x0 + x + 0.5 * tileW);
                    int y_mid = (int) (y0 + y + 0.5 * tileH);
                    if (noiseValues[x_mid][y_mid] >= 0.6) {
                        String name = possibleNames.isEmpty() ? "Burg " + (castlesGenerated + 1) :
                            possibleNames.get((int) (Math.random() * possibleNames.size()));
                        Castle newCastle = new Castle(new Point(x0 + x, y0 + y), name);
                        boolean doesIntersect = false;

                        for (Castle r : castleGraph.getAllValues()) {
                            if (r.distance(newCastle) < Math.max(tileW, tileH)) {
                                doesIntersect = true;
                                break;
                            }
                        }

                        if (!doesIntersect) {
                            possibleNames.remove(name);
                            castleGraph.addNode(newCastle);
                            castlesGenerated++;
                            positionFound = true;
                            break;
                        }
                    }
                }

                if (positionFound)
                    break;
            }
        }
    }

    /**
     * Hier werden die Kanten erzeugt. Dazu werden zunächst alle Burgen durch eine Linie verbunden und anschließend
     * jede Burg mit allen anderen in einem bestimmten Radius nochmals verbunden
     * Wrapper für {@link GameMap#generateEdges(List, Graph, int)}
     */
    private void generateEdges() {
        generateEdges(castleGraph.getNodes(), castleGraph, GameConstants.MAX_EDGE_COUNT_CASTLES);
    }
    /**
     * Generates the edges for the given nodes in the given graph
     * @param nodes The nodes to generate edges for
     * @param graph The graph to generate edges in
     * @param maxEdgeCount maximum edge count for one castle
     */
    private static void generateEdges(List<Node<Castle>> nodes, Graph<Castle> graph, int maxEdgeCount) {
        if(nodes.isEmpty())
            return;
        List<Node<Castle>> tempNodes1 = new LinkedList<>(nodes), tempNodes2 = new LinkedList<>(nodes);
        if(tempNodes1.isEmpty())
            return;
        Node<Castle> currentNode = tempNodes1.get(0), nextNode;
        tempNodes1.remove(0);
        while(!tempNodes1.isEmpty()){
            nextNode = getNearestNode(currentNode, tempNodes1);
            tempNodes1.remove(nextNode);
            if(tempNodes1.isEmpty()){
                tempNodes2 = new LinkedList<>(nodes);
                tempNodes2.remove(nextNode);
                currentNode = getNearestNode(nextNode, tempNodes2);
            }
            graph.addEdge(currentNode, nextNode);
            currentNode = nextNode;
        }

        tempNodes1 = nodes;
        List<Edge<Castle>> edges;
        int edgeCount;
        Random random = new Random();
        for(Node<Castle> node : tempNodes1){
            tempNodes2 = new LinkedList<>(tempNodes1);
            tempNodes2.remove(node); // remove current edge
            edges = graph.getEdges(node);
            edgeCount = random.nextInt(maxEdgeCount + 1) - edges.size(); // random edge count
            while(!edges.isEmpty()){
                tempNodes2.remove(edges.remove(0).getOtherNode(node)); // remove nodes with existing edges
            }
            while(edgeCount-- > 0){
                nextNode = getNearestNode(node, tempNodes2); // edge to nearest other node
                tempNodes2.remove(nextNode); // remove other node
                graph.addEdge(node, nextNode); // add edge
            }
        }
    }
    /**
     * This method has the same behavior as {@link GameMap#generateEdges()}, with the difference, that this
     * method connects first the castles in the kingdoms with each other and then the kingdoms wtih the other kingdoms
     * If not every castle belongs to a kingdom or {@link GameMap#kingdoms} is empty, {@link GameMap#generateEdges()}
     * gets called.
     */
    public void generateKingdomEdges() {
        if(kingdoms.isEmpty()
                || getCastles().isEmpty()
                || getCastles().stream().anyMatch(c -> c.getKingdom() == null)) {
            generateEdges();
            return;
        }
        for(Kingdom kingdom : kingdoms) {
            if(!kingdom.getCastles().isEmpty()){
                generateEdges(castleGraph.getNodes(kingdom.getCastles()), castleGraph, GameConstants.MAX_EDGE_COUNT_CASTLES); // Generate edges inside the kingdoms
            }
        }

        List<Kingdom> tempKingdoms1 = new LinkedList<>(kingdoms), tempKingdoms2 = new LinkedList<>(kingdoms);
        Kingdom currentKingdom = tempKingdoms1.get(0), nextKingdom;
        Pair<Castle, Castle> nextCastles;
        tempKingdoms1.remove(0);
        while(!tempKingdoms1.isEmpty()){
            nextKingdom = getNearestKingdom(currentKingdom, tempKingdoms1);
            tempKingdoms1.remove(nextKingdom);
            if(tempKingdoms1.isEmpty()){
                tempKingdoms2.remove(nextKingdom);
                currentKingdom = getNearestKingdom(nextKingdom, tempKingdoms2);
            }
            nextCastles = getNearestCastles(currentKingdom, nextKingdom);
            castleGraph.addEdge(castleGraph.getNode(nextCastles.getKey()), castleGraph.getNode(nextCastles.getValue()));
            currentKingdom = nextKingdom;
        }

        tempKingdoms1 = kingdoms;
        List<Edge<Castle>> edges;
        int edgeCount;
        List<Pair<Castle, Castle>> castleRelation = new LinkedList<>();
        List<Castle> castles, otherKingdomCastles;
        Kingdom otherKingdom;
        Castle kingdomCastle, otherCastle;
        int rand, i = 0, m;
        Random random = new Random();
        for(Kingdom kingdom : tempKingdoms1){
            castles = kingdom.getCastles();
            tempKingdoms2 = new LinkedList<>(tempKingdoms1);
            tempKingdoms2.remove(kingdom); // remove current edge
            edges = castleGraph.getEdges(castleGraph.getNodes(kingdom.getCastles()));
            edgeCount = random.nextInt(GameConstants.MAX_EDGE_COUNT_KINGDOMS + 1); // random edge count
            for(Edge<Castle> edge : edges){
                if(!castles.contains(edge.getNodeA().getValue()) || !castles.contains(edge.getNodeB().getValue())){ //TODO: Nullpointer
                    castleRelation.add(new Pair<>(edge.getNodeB().getValue(), edge.getNodeA().getValue()));
                    --edgeCount;
                }
            }
            m = 0; // max iterator
            while(edgeCount-- > 0 && tempKingdoms1.size() > 1 && m++ < 50){
                kingdomCastle = castles.get(random.nextInt(castles.size())); // random castle
                rand = random.nextInt(tempKingdoms1.size()); // random other kingdom
                otherKingdom = tempKingdoms1.get(rand != i ? rand : (rand+1)%tempKingdoms1.size()); // other kingdom not this kingdom
                otherKingdomCastles = getNearestCastles(kingdomCastle, otherKingdom.getCastles(), 3);
                while(!otherKingdomCastles.isEmpty()){
                    otherCastle = otherKingdomCastles.get(0);
                    kingdomCastle = getNearestCastle(otherCastle, castles);
                    if(castleRelation.contains(new Pair<>(kingdomCastle, otherCastle))
                            || castleRelation.contains(new Pair<>(otherCastle, kingdomCastle))){
                        otherKingdomCastles.remove(0);
                        edgeCount++;
                    }else{
                        castleGraph.addEdge(castleGraph.getNode(kingdomCastle), castleGraph.getNode(otherCastle));
                        castleRelation.add(new Pair<>(kingdomCastle, otherCastle));
                        otherKingdomCastles.clear();
                    }
                }
            }
            ++i;
        }
    }

    /**
     * finds the node from nodes containing the castle with the smallest distance to the castle of node
     * @param node One node of edge
     * @param nodes Available nodes (mustn't contain node)
     * @return the node with the smallest distance
     */
    private static Node<Castle> getNearestNode(Node<Castle> node, List<Node<Castle>> nodes){
        Node<Castle> bestNode = null;
        double smallestDistance = Double.MAX_VALUE, currentDistance;
        for(Node<Castle> n : nodes){
            currentDistance = node.getValue().distance(n.getValue());
            if(currentDistance < smallestDistance || bestNode == null){
                smallestDistance = currentDistance;
                bestNode = n;
            }
        }
        return bestNode;
    }
    /**
     * finds the kingdom from kingdoms with the smallest distance to kingdom
     * @param kingdom Kingdom to get the Kingdom from kingdoms with the smallest distance
     * @param kingdoms List of kingdoms to get the one with the smallest distance to kingdom
     * @return the Kingdom (from kingdoms) with the smallest distance to kingdom
     */
    private static Kingdom getNearestKingdom(Kingdom kingdom, List<Kingdom> kingdoms){
        Kingdom nextKingdom = null;
        double smallestDistance = Double.MAX_VALUE, currentDistance = smallestDistance;
        Pair<Castle, Castle> castles;
        for(Kingdom k : kingdoms){
            castles = getNearestCastles(kingdom, k);
            currentDistance = castles.getKey().distance(castles.getValue());
            if(currentDistance < smallestDistance || nextKingdom == null){
                smallestDistance = currentDistance;
                nextKingdom = k;
            }
        }
        return nextKingdom;
    }
    /**
     * Wrapper for {@link GameMap#getNearestCastles(List, List)}
     * @param kingdom1 All castles of this Kindom are the first argument for {@link GameMap#getNearestCastles(List, List)}
     * @param kingdom2 All castles of this Kindom are the second argument for {@link GameMap#getNearestCastles(List, List)}
     * @return a {@link Pair<Castle, Castle>} of castles (one from each kingdom) with the smallest distance.
     */
    private static Pair<Castle, Castle> getNearestCastles(Kingdom kingdom1, Kingdom kingdom2){
        return getNearestCastles(kingdom1.getCastles(), kingdom2.getCastles());
    }
    /**
     * Searches the castles from the list with the smallest distance to each other (one castle from each list)
     * @param castles1 List of castles
     * @param castles2 List of castles
     * @return the castles with the smallest distance
     */
    private static Pair<Castle, Castle> getNearestCastles(List<Castle> castles1, List<Castle> castles2){
        Castle bestCastle1 = null, bestCastle2 = null;
        double smallestDistance = Double.MAX_VALUE, currentDistance = smallestDistance;
        for(Castle castle1 : castles1){
            for(Castle castle2 : castles2){
                currentDistance = castle1.distance(castle2);
                if(currentDistance < smallestDistance){
                    smallestDistance = currentDistance;
                    bestCastle1 = castle1;
                    bestCastle2 = castle2;
                }
            }
        }
        return new Pair<Castle, Castle>(bestCastle1, bestCastle2);
    }
    /**
     * Searches the number of castles with the smallest distance to the passed castle
     * @param castle Castle to get the distance to
     * @param castles Castles to get the Castles with the smallest distance from
     * @param count number of castles in return list
     * @return a List of Castles with the smallest distance to castle and the size count or castles.size
     */
    private static List<Castle> getNearestCastles(Castle castle, List<Castle> castles, int count){
        return castles.stream().
                sorted(Comparator.comparingDouble(c -> castle.distance(c))).
                collect(Collectors.toList()).
                subList(0, count <= castles.size() ? count : castles.size());
    }
    /**
     * finds the castle from castles with the smallest distance to castle
     * @param castle Castle to get the one with the smallest distance for
     * @param castles Castles to get the one with the smallest distance for
     * @return Castle from castles with the smallest distance to castle
     */
    private static Castle getNearestCastle(Castle castle, List<Castle> castles){
        Castle bestCastle = null;
        double smallestDistance = Double.MAX_VALUE, currentDistance;
        for(Castle c : castles){
            currentDistance = castle.distance(c);
            if(currentDistance < smallestDistance || bestCastle == null){
                smallestDistance = currentDistance;
                bestCastle = c;
            }
        }
        return bestCastle;
    }

    /**
     * Hier werden die Burgen in Königreiche unterteilt. Dazu wird der {@link Clustering} Algorithmus aufgerufen.
     * @param kingdomCount die Anzahl der zu generierenden Königreiche
     */
    private void generateKingdoms(int kingdomCount) {
        if(kingdomCount > 0 && kingdomCount < castleGraph.getAllValues().size()) {
            Clustering clustering = new Clustering(this, castleGraph.getAllValues(), kingdomCount);
            kingdoms = clustering.getPointsClusters();
        } else {
            kingdoms = new ArrayList<>();
        }
    }

    /**
     * Eine neue Spielfeldkarte generieren.
     * Dazu werden folgende Schritte abgearbeitet:
     *   1. Das Hintergrundbild generieren
     *   2. Burgen generieren
     *   3. Kanten hinzufügen
     *   4. Burgen in Köngireiche unterteilen
     * @param width die Breite des Spielfelds
     * @param height die Höhe des Spielfelds
     * @param scale die Skalierung
     * @param castleCount die maximale Anzahl an Burgen
     * @param kingdomCount die Anzahl der Königreiche
     * @return eine neue GameMap-Instanz
     */
    public static GameMap generateRandomMap(int width, int height, int scale, int castleCount, int kingdomCount) {

        width = Math.max(width, 15);
        height = Math.max(height, 10);

        if (scale <= 0 || castleCount <= 0)
            throw new IllegalArgumentException();

        System.out.println(String.format("Generating new map, castles=%d, width=%d, height=%d, kingdoms=%d", castleCount, width, height, kingdomCount));
        GameMap gameMap = new GameMap(width, height, scale);
        gameMap.generateBackground();
        gameMap.generateCastles(castleCount);
        gameMap.generateKingdoms(kingdomCount);
        //gameMap.generateEdges();
        gameMap.generateKingdomEdges();

        if(!gameMap.getGraph().allNodesConnected()) {
            System.out.println("Fehler bei der Verifikation: Es sind nicht alle Knoten miteinander verbunden!");
            return null;
        }

        return gameMap;
    }

    /**
     * Generiert eine Liste von Zufallsnamen für Burgen. Dabei wird ein Prefix (Schloss, Burg oder Festung) an einen
     * vorhandenen Namen aus den Resourcen angefügt. Siehe auch: {@link Resources#getCastleNames()}
     * @return eine Liste mit Zufallsnamen
     */
    private List<String> generateCastleNames() {
        String[] prefixes = {"Schloss", "Burg", "Festung"};
        List<String> names = Resources.getInstance().getCastleNames();
        List<String> nameList = new ArrayList<>(names.size());

        for (String name : names) {
            String prefix = prefixes[(int) (Math.random() * prefixes.length)];
            nameList.add(prefix + " " + name);
        }

        return nameList;
    }

    public int getWidth() {
        return this.backgroundImage.getWidth();
    }

    public int getHeight() {
        return this.backgroundImage.getHeight();
    }

    public BufferedImage getBackgroundImage() {
        return this.backgroundImage;
    }

    public Dimension getSize() {
        return new Dimension(this.getWidth(), this.getHeight());
    }

    public List<Castle> getCastles() {
        return castleGraph.getAllValues();
    }

    public Graph<Castle> getGraph() {
        return this.castleGraph;
    }

    public List<Edge<Castle>> getEdges() {
        return this.castleGraph.getEdges();
    }

    public List<Kingdom> getKingdoms() {
        return this.kingdoms;
    }
}
