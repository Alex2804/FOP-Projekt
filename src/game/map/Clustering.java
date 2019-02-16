package game.map;

import game.GameConstants;
import javafx.util.Pair;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Diese Klasse teilt Burgen in Königreiche auf
 */
public class Clustering {

    private Random random;
    private final List<Castle> allCastles;
    private final int kingdomCount;
    private final GameMap gameMap;

    /**
     * Ein neues Clustering-Objekt erzeugen.
     * @param gameMap Die zu generierende map
     * @param castles Die Liste von Burgen, die aufgeteilt werden sollen
     * @param kingdomCount Die Anzahl von Königreichen die generiert werden sollen
     */
    public Clustering(GameMap gameMap, List<Castle> castles, int kingdomCount) {
        if (kingdomCount < 2)
            throw new IllegalArgumentException("Ungültige Anzahl an Königreichen");

        this.random = new Random();
        this.kingdomCount = kingdomCount;
        this.allCastles = Collections.unmodifiableList(castles);
        this.gameMap = gameMap;
    }

    /**
     * Gibt eine Liste von Königreichen zurück.
     * Jedes Königreich sollte dabei einen Index im Bereich 0-5 bekommen, damit die Burg richtig angezeigt werden kann.
     * Siehe auch {@link Kingdom#getType()}
     */
    public List<Kingdom> getPointsClusters() {
        List<Kingdom> kingdoms = new ArrayList<>(kingdomCount);
        for(int i=0; i<kingdomCount; i++){
            Kingdom kingdom = new Kingdom(i%6);
            kingdom.setLocation(random.nextInt(gameMap.getWidth()), random.nextInt(gameMap.getHeight()));
            kingdoms.add(kingdom);
        }
        boolean changed = true;
        Map<Castle, Kingdom> map = new HashMap<>();
        while(changed){
            changed = false;
            Kingdom tempKingdom;
            for(Castle castle : allCastles){
                Kingdom kingdom = getNearest(kingdoms, castle);
                tempKingdom = map.get(castle);
                if(kingdom != tempKingdom){
                    tempKingdom.removeCastle(castle);
                    map.replace(castle, kingdom);
                    kingdom.addCastle(castle);
                    changed = true;
                }
            }
            Point oldLocation;
            for(Kingdom kingdom : kingdoms){
                oldLocation = kingdom.getLocation();
                int x = kingdom.getCastles().stream().mapToInt(castle -> castle.getLocationOnMap().x).sum();
                int y = kingdom.getCastles().stream().mapToInt(castle -> castle.getLocationOnMap().y).sum();
                if(!kingdom.getCastles().isEmpty()){
                    Point newLocation = new Point(x / kingdom.getCastles().size(), y / kingdom.getCastles().size());
                    if(!newLocation.equals(oldLocation)){
                        kingdom.setLocation(newLocation);
                        changed = true;
                    }
                }
            }
        }
        return kingdoms;
    }
    /**
     * @param kingdoms list of kingdoms to get the nearest from
     * @param castle castle to get the nearest kingdom
     * @return the nearest kingdom
     */
    private Kingdom getNearest(List<Kingdom> kingdoms, Castle castle){
        Kingdom nearest = null;
        double dist, nearestDist = -1;
        for (Kingdom kingdom : kingdoms) {
            dist = castle.distance(kingdom.getLocation());
            if(dist < nearestDist){
                nearest = kingdom;
                nearestDist = dist;
            }
        }
        return  nearest;
    }
}
