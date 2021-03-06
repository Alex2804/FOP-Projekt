package game.map;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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
            Kingdom kingdom = new Kingdom(i%6); // id's from 1 to 5 dependent on the index (if more than 5 kingdoms, same id multiple times)
            kingdom.setLocation(random.nextInt(gameMap.getWidth() + 1), random.nextInt(gameMap.getHeight() + 1)); // +1 because bounds are exclusive
            kingdoms.add(kingdom);
        }

        boolean changed = true;
        while(changed) {
            changed = false;
            Kingdom tempKingdom;
            for (Castle castle : allCastles) {
                Kingdom kingdom = getNearest(kingdoms, castle);
                tempKingdom = castle.getKingdom();
                if (kingdom != tempKingdom) {
                    castle.replaceKingdom(kingdom);
                    changed = true;
                }
            }
            Point oldLocation, newLocation;
            for (Kingdom kingdom : kingdoms) {
                oldLocation = kingdom.getLocation();
                int x = kingdom.getCastles().stream().mapToInt(castle -> castle.getLocationOnMap().x).sum();
                int y = kingdom.getCastles().stream().mapToInt(castle -> castle.getLocationOnMap().y).sum();
                if (!kingdom.getCastles().isEmpty()) {
                    newLocation = new Point(x / kingdom.getCastles().size(), y / kingdom.getCastles().size());
                    if (!newLocation.equals(oldLocation)) {
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
        double dist, nearestDist = Double.MAX_VALUE;
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
