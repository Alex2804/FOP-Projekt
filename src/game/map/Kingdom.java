package game.map;

import base.Edge;
import base.Graph;
import game.Player;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Diese Klasse representiert ein Königreich. Jedes Königreich hat eine Liste von Burgen sowie einen Index {@link #type} im Bereich von 0-5
 *
 */
public class Kingdom {

    private List<Castle> castles;
    private int type;
    private Point location;

    /**
     * Erstellt ein neues Königreich
     * @param type der Typ des Königreichs (im Bereich 0-5)
     */
    public Kingdom(int type) {
        this.castles = new LinkedList<>();
        this.type = type;
        location = new Point(0, 0);
    }

    /**
     * Erstellt ein neues Königreich
     * @param type der Type des Königreiches (im Bereich 0-5)
     * @param x die x position des Königreiches
     * @param y die y position des Königreiches
     */
    public Kingdom(int type, int x, int y){
        this.castles = new LinkedList<>();
        this.type = type;
        location = new Point(x, y);
    }

    /**
     * sets the location
     * @param location the new location
     */
    public void setLocation(Point location){
        this.location = location;
    }
    /**
     * sets the location
     * @param x new x location
     * @param y new y location
     */
    public void setLocation(int x, int y){
        setLocation(new Point(x, y));
    }

    /**
     * @return the location of the kingdom
     */
    public Point getLocation(){
        return location;
    }

    /**
     * Eine Burg zum Königreich hinzufügen
     * @param castle die Burg, die hinzugefügt werden soll
     */
    public void addCastle(Castle castle) {
        if(!this.castles.contains(castle))
            this.castles.add(castle);
    }

    /**
     * Gibt den Typen des Königreichs zurück. Dies wird zur korrekten Anzeige benötigt
     * @return der Typ des Königreichs.
     */
    public int getType() {
        return this.type;
    }

    /**
     * Eine Burg aus dem Königreich entfernen
     * @param castle die zu entfernende Burg
     */
    public void removeCastle(Castle castle) {
        this.castles.remove(castle);
    }

    /**
     * Gibt den Spieler zurück, der alle Burgen in dem Köngreich besitzt.
     * Sollte es keinen Spieler geben, der alle Burgen besitzt, wird null zurückgegeben.
     * @return der Besitzer oder null
     */
    public Player getOwner() {
        if(castles.isEmpty())
            return null;

        Player owner = castles.get(0).getOwner();
        for(Castle castle : castles) {
            if(castle.getOwner() != owner)
                return null;
        }

        return owner;
    }

    /**
     * Gibt alle Burgen zurück, die in diesem Königreich liegen
     * @return Liste von Burgen im Königreich
     */
    public List<Castle> getCastles() {
        return this.castles;
    }

    /**
     * @return the sum of all castles TROOPS
     */
    public int getTroopCount(){
        int sum = 0;
        for(Castle castle : castles){
            sum += castle.getTroopCount();
        }
        return sum;
    }
    /**
     * @return the sum of all castles TROOPS, which can be used to attack
     */
    public int getAttackTroopCount(){
        return getTroopCount() - castles.size();
    }

    /**
     * @param castleGraph the graph containing all castles and edges
     * @return the count of edges to other kingdoms
     */
    public int getEdgeCountToOtherKingdoms(Graph<Castle> castleGraph){
        int edgeCountSum = 0;
        for(Edge<Castle> edge : castleGraph.getEdges(castleGraph.getNodes(castles))){
            if(edge.getNodeB().getValue().getKingdom() != this
                    || edge.getNodeA().getValue().getKingdom() != this){
                edgeCountSum++;
            }
        }
        return edgeCountSum;
    }
}
