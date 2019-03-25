package de.teast;

import de.teast.autils.APoint;
import de.teast.autils.AVector2D;
import game.Player;
import game.map.Castle;
import gui.Resources;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Replaces {@link game.map.Clustering} for {@link game.goals.AClashOfArmiesGoal} (does not create clusters but doesn't
 * matter :D)
 * @author Alexander Muth
 *
 * @see game.goals.AClashOfArmiesGoal
 */
public class AClustering {
    /**
     * Generates random {@link Castle}s and moves them with an negative gravity to each other, until they doesn't move
     * anymore or the max iteration count is reached.
     * @param players the players to generate the bases for
     * @param mapSize the size of the map
     * @return a {@link List} of the generated bases.
     */
    public static List<Castle> generateBases(List<Player> players, Dimension mapSize){
        List<Castle> bases = generateRandomCastles(players, mapSize);
        Map<Castle, Point> basesPositions = new HashMap<>();
        for(Castle base : bases){
            basesPositions.put(base, base.getLocationOnMap());
        }

        int i=0;
        boolean changed = true;
        Point oldPos;
        while(changed && (i++ < AConstants.MAX_ITER_CLUSTERING)){
            changed = false;
            for(Castle base : generateBasesHelper(bases, players.size(), mapSize)){
                oldPos = basesPositions.put(base, base.getLocationOnMap());
                if(oldPos == null || !oldPos.equals(base.getLocationOnMap())){
                    changed = true;
                }
            }
        }

        return bases;
    }

    /**
     * Moves the {@link Castle}s with an negative gravity to each other
     * @param bases the castles to move
     * @param playerCount the count of the players
     * @param mapSize the size of the map
     * @return a {@link List} of the moved {@link Castle}s
     */
    private static List<Castle> generateBasesHelper(List<Castle> bases, int playerCount, Dimension mapSize){

        Map<Castle, List<AVector2D>> baseVectorsMap = new HashMap<>();
        for(Castle base : bases){
            baseVectorsMap.put(base, generateCastleVectors(base, bases));
        }

        Map<Castle, AVector2D> baseGravityVectorMap = new HashMap<>();
        AVector2D gravityVector;
        for(Map.Entry<Castle, List<AVector2D>> entry : baseVectorsMap.entrySet()){
            gravityVector = new AVector2D(0, 0);
            for(AVector2D vector : entry.getValue()){
                gravityVector = gravityVector.add(vector);
            }
            baseGravityVectorMap.put(entry.getKey(), gravityVector.negate());
        }

        APoint newPos;
        Dimension castleDim = Resources.getInstance().getCastleSize(0);
        double castleEdgeDistance = AConstants.CASTLE_EDGE_DISTANCE * (AConstants.CASTLE_EDGE_DISTANCE_PLAYER_MULTIPLIER * playerCount);
        for(Map.Entry<Castle, AVector2D> entry : baseGravityVectorMap.entrySet()){
            newPos = new APoint(entry.getKey().getLocationOnMap());
            newPos = newPos.add(scaleCastleVectors(entry.getKey(), entry.getValue(), mapSize));
            newPos.x += (newPos.x < castleEdgeDistance) ? castleEdgeDistance : 0;
            newPos.x -= (newPos.x > mapSize.width - castleEdgeDistance - castleDim.width) ? castleEdgeDistance + castleDim.width : 0;
            newPos.y += (newPos.y < castleEdgeDistance) ? castleEdgeDistance : 0;
            newPos.y -= (newPos.y > mapSize.height - castleEdgeDistance - castleDim.height) ? castleEdgeDistance + castleDim.height : 0;
            entry.getKey().setLocation(newPos);
        }

        return new LinkedList<>(baseGravityVectorMap.keySet());
    }

    /**
     * generates random {@link Castle}s inside the map for the players
     * @param players the players to generate castles for
     * @param mapSize the size of the map
     * @return a {@link List} containing the generated {@link Castle}s.
     */
    public static List<Castle> generateRandomCastles(List<Player> players, Dimension mapSize){
        List<Castle> returnList = new LinkedList<>();
        Point position;
        Point posMin, posMax;
        Castle castle;
        //int i=0;
        for(Player player : players){
            /*if((i++) % 4 == 0){
                posMin = new Point(0, 0);
                posMax = new Point(mapSize.width / 2, mapSize.height / 2);
            }else if((i++) % 4 == 1){
                posMin = new Point((mapSize.width / 2) + 1, (mapSize.height / 2) + 1);
                posMax = new Point(mapSize.width, mapSize.height);
            }else if((i++) % 4 == 2){
                posMin = new Point(0, (mapSize.height / 2) + 1);
                posMax = new Point(mapSize.width / 2, mapSize.height);
            }else if((i++) % 4 == 3){
                posMin = new Point((mapSize.width / 2) + 1, 0);
                posMax = new Point(mapSize.width, mapSize.height / 2);
            }else{
                posMin = new Point(0, 0);
                posMax = new Point(mapSize.width, mapSize.height);
            }*/
            posMin = new Point(0, 0);
            posMax = new Point(mapSize.width, mapSize.height);
            position = new Point(ThreadLocalRandom.current().nextInt(posMin.x, posMax.x + 1),
                                 ThreadLocalRandom.current().nextInt(posMin.y, posMax.y + 1));
            castle = new Castle(position, player.getName() + " Basis");
            castle.setOwner(player);
            returnList.add(castle);
        }
        return returnList;
    }

    /**
     * Generates a list of {@link AVector2D}, where every {@link AVector2D} points to an castle
     * @param castle castle to generate vectors for
     * @param castles list of castles to generate vectors to
     * @return the generated {@link AVector2D} objects in a {@link List}
     */
    public static List<AVector2D> generateCastleVectors(Castle castle, Collection<Castle> castles){
        List<AVector2D> returnList = new LinkedList<>();
        for(Castle c : castles){
            if(c != castle && castle != null){
                returnList.add(new AVector2D(castle.getLocationOnMap(), c.getLocationOnMap()));
            }
        }
        return returnList;
    }

    /**
     * Scales the castle vector, that it is as long as required to intersect with the border
     * @param castle the Castle set the offset of the vector
     * @param vector the vector to scale
     * @param mapSize the size of the map
     * @return the scaled vector
     */
    public static AVector2D scaleCastleVectors(Castle castle, AVector2D vector, Dimension mapSize){
        APoint xIntersection, yIntersection;
        double xDistance, yDistance;
        APoint castlePos = new APoint(castle.getLocationOnMap());
        yIntersection = linesIntersectionPoint(castlePos, castlePos.add(vector),
                                                new APoint((vector.x < 0) ? 0 : mapSize.width, -1),
                                                new APoint((vector.x < 0) ? 0 : mapSize.width, 1));
        xIntersection = linesIntersectionPoint(castlePos, castlePos.add(vector),
                                                new APoint(-1, (vector.y < 0) ? 0 : mapSize.height),
                                                new APoint(1, (vector.y < 0) ? 0 : mapSize.height));
        if(xIntersection == null && yIntersection == null){
            return null;
        }else if(xIntersection == null){
            return new AVector2D(castlePos, yIntersection);
        }else if(yIntersection == null){
            return new AVector2D(castlePos, xIntersection);
        }
        xDistance = castlePos.distance(xIntersection);
        yDistance = castlePos.distance(yIntersection);
        return new AVector2D(castlePos, (xDistance < yDistance) ? xIntersection : yIntersection);
    }

    /**
     * Calculates the intersection point of 2 lines
     * @param A1 the start point of the first line
     * @param A2 the end point of the first line
     * @param B1 the start point of the second line
     * @param B2 the start point of the third line
     * @return The intersection point of the lines, or null if tey are parallel
     */
    public static APoint linesIntersectionPoint(Point A1, Point A2, Point B1, Point B2){
        double a1 = A2.y - A1.y;
        double b1 = A1.x - A2.x;
        double c1 = a1*(A1.x) + b1*(A1.y);

        double a2 = B2.y - B1.y;
        double b2 = B1.x - B2.x;
        double c2 = a2*(B1.x) + b2*(B1.y);

        double d = a1*b2 - a2*b1;
        if(d == 0){
            return null;
        }else{
            double x = (b2*c1 - b1*c2) / d;
            double y = (a1*c2 - a2*c1) / d;
            return new APoint((int)x, (int)y);
        }
    }
}
