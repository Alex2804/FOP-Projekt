package game.map;

import game.Player;

import java.awt.*;
import java.util.List;

/**
 * Diese Klasse representiert eine Burg.
 * Jede Burg hat Koordinaten auf der Karte und einen Namen.
 * Falls die Burg einen Besitzer hat, hat sie auch eine Anzahl von zugewiesenen Truppen.
 * Die Burg kann weiterhin Teil eines Königreichs sein.
 */
public class Castle {

    private int troopCount;
    private Player owner;
    private Kingdom kingdom;
    private Point location;
    private String name;

    /**
     * Eine neue Burg erstellen
     * @param location die Koordinaten der Burg
     * @param name der Name der Burg
     */
    public Castle(Point location, String name) {
        this.location = location;
        this.troopCount = 0;
        this.owner = null;
        this.kingdom = null;
        this.name = name;
    }

    public Castle copy() {
        Castle castle = new Castle(location, name);
        castle.troopCount = troopCount;
        castle.owner = owner;
        castle.kingdom = kingdom;
        return castle;
    }

    public Player getOwner() {
        return this.owner;
    }

    public Kingdom getKingdom() {
        return this.kingdom;
    }

    public int getTroopCount() {
        return this.troopCount;
    }

    /**
     * Truppen von dieser Burg zur angegebenen Burg bewegen.
     * Dies funktioniert nur, wenn die Besitzer übereinstimmen und bei der aktuellen Burg mindestens eine Truppe übrig bleibt
     * @param target the target castle
     * @param troops the amount of TROOPS
     */
    public void moveTroops(Castle target, int troops) {

        // Troops can only be moved to own regions
        if(target.owner != this.owner)
            return;

        // At least one unit must remain in the source region
        if(this.troopCount - troops < 1)
            return;

        this.troopCount -= troops;
        target.troopCount += troops;
    }

    public Point getLocationOnMap() {
        return this.location;
    }

    public void setLocation(Point location){
        this.location = location;
    }
    /**
     * Berechnet die eukldische Distanz zu dem angegebenen Punkt
     * @param dest die Zielkoordinate
     * @return die euklidische Distanz
     */
    public double distance(Point dest) {
        return Math.sqrt(Math.pow(this.location.x - dest.x, 2) + Math.pow(this.location.y - dest.y, 2));
    }

    /**
     * Berechnet die eukldische Distanz zu der angegebenen Burg
     * @param next die Zielburg
     * @return die euklidische Distanz
     * @see #distance(Point)
     */
    public double distance(Castle next) {
        Point otherLocation = next.getLocationOnMap();
        return this.distance(otherLocation);
    }

    /**
     * @param castles a list of castles
     * @return the castle from {@code castles} with the smallest distance to this castle or null if {@code castles} is
     * empty.
     */
    public Castle getNearest(List<Castle> castles){
        Castle nearest = null;
        double smallestDistance = Double.MAX_VALUE;
        double distance;
        for(Castle castle : castles){
            distance = distance(castle);
            if(nearest == null || distance < smallestDistance){
                smallestDistance = distance;
                nearest = castle;
            }
        }
        return nearest;
    }

    public void setOwner(Player player) {
        this.owner = player;
    }

    public void addTroops(int i) {
        if(i <= 0)
            return;

        this.troopCount += i;
    }

    public String getName() {
        return this.name;
    }

    public void removeTroops(int i) {
        this.troopCount = Math.max(0, this.troopCount - i);
    }

    /**
     * Gibt den Burg-Typen zurück. Falls die Burg einem Königreich angehört, wird der Typ des Königreichs zurückgegeben, ansonsten 0
     * @return der Burg-Typ für die Anzeige
     */
    public int getType() {
        return this.kingdom == null ? 0 : this.kingdom.getType();
    }

    /**
     * Die Burg einem Königreich zuordnen
     * @param kingdom Ein Königreich oder null
     */
    public void setKingdom(Kingdom kingdom) {
        replaceKingdom(kingdom);
    }
    /**
     * Die Burg einem Königreich zuordnen. Die Methode ruft {@link Castle#setKingdom(Kingdom)} auf und entfernt
     * zusätzlich das castle aus dem alten kingdom
     * @param kingdom Ein Königreich oder null
     */
    public void replaceKingdom(Kingdom kingdom) {
        if(kingdom == this.kingdom)
            return;
        if(this.kingdom != null)
            this.kingdom.removeCastle(this);
        this.kingdom = kingdom;
        if(kingdom != null)
            kingdom.addCastle(this);
    }
}
