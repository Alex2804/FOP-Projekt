package de.teast;

import base.Edge;
import base.Graph;
import base.Node;
import com.sun.javafx.geom.Line2D;
import de.teast.autils.APoint;
import de.teast.autils.ATriplet;
import de.teast.autils.AVector2D;
import game.map.Castle;
import game.map.Kingdom;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class APath extends Graph<Castle> {
    Map<Castle, Node<Castle>> castleMap;
    List<ATriplet<Castle, Castle, List<Castle>>> stops;
    Kingdom kingdom = new Kingdom(6);

    public APath(List<Castle> castles){
        super();
        castleMap = new HashMap<>();
        for(Castle castle : castles){
            castleMap.put(castle, addNode(castle));
        }
        stops = new LinkedList<>();
    }

    public void generateStops(Castle source, Castle destination, int stopCount){
        generateStops(source, destination, stopCount, AConstants.AVOID_DISTANCE_SCALE_MULTIPLIER * 40);
    }
    public void generateStops(Castle source, Castle destination, int stopCount, double scale){
        if(!castleMap.containsKey(source)){
            castleMap.put(source, addNode(source));
        }
        if(!castleMap.containsKey(destination)){
            castleMap.put(destination, addNode(destination));
        }
        Set<Edge<Castle>> edges = new HashSet<>();
        for(List<Castle> stop : getStops(source, destination)){
            edges.addAll(getStopsEdges(stop));
        }
        edges = new HashSet<>(getEdges());

        List<Castle> newStops = new LinkedList<>();
        Node<Castle> newNode;
        Node<Castle> lastNode = castleMap.get(source);
        APoint pos = new APoint(source.getLocationOnMap());
        AVector2D difVec = new AVector2D((destination.getLocationOnMap().x - pos.x) / (stopCount + 1.0),
                                         (destination.getLocationOnMap().y - pos.y) / (stopCount + 1.0));
        AVector2D perpen = difVec.getPerpendicular().scale(AConstants.AVOID_DISTANCE_SCALE_MULTIPLIER * scale);
        AVector2D scaledPerpen;
        APoint stopPos;
        int tempI = 0;
        double posMult, minDist;
        List<APoint> positions = new LinkedList<>();
        while(true){
            positions.clear();
            scaledPerpen = perpen.mult(tempI);
            for(int i = 1; i <= stopCount; i++) {
                posMult = 1 - (Math.abs((stopCount / 2.0) - i) / AConstants.DISTANCE_MIDDLE_DIVISOR);
                posMult *= ThreadLocalRandom.current().nextDouble(AConstants.RANDOM_DISTANCE_SCALE_MULTIPLIER_MIN, AConstants.RANDOM_DISTANCE_SCALE_MULTIPLIER_MAX);
                stopPos = pos.add(difVec.mult(i)).add(scaledPerpen.mult(posMult));
                positions.add(stopPos);
            }
            minDist = minDistance(positions, edges);
            if(!intersects(positions, edges) && (minDist < 0 || minDist > AConstants.MIN_AVOID_DISTANCE)){
                break;
            }else if(tempI > 0){
                tempI = -tempI;
            }else{
                tempI = (-tempI) + 1;
            }
        }

        for(APoint position : positions){
            newNode = addNode(new Castle(new Point((Point)position), ""));
            newNode.getValue().setKingdom(kingdom);
            addEdge(lastNode, newNode);
            newStops.add(newNode.getValue());
            lastNode = newNode;
        }
        addEdge(lastNode, getNode(destination));

        stops.add(new ATriplet<>(source, destination, newStops));
    }

    public List<List<Castle>> getStops(Castle source, Castle destination){
        List<List<Castle>> returnList = new LinkedList<>();
        for(ATriplet<Castle, Castle, List<Castle>> triplet : stops){
            if((triplet.getFirst() == source && triplet.getSecond() == destination)
                    || (triplet.getSecond() == source && triplet.getFirst() == destination)){
                returnList.add(triplet.getThird());
            }
        }
        return returnList;
    }
    public List<Edge<Castle>> getStopsEdges(List<Castle> stops){
        Set<Edge<Castle>> edges = new HashSet<>();
        Node<Castle> node;
        for(Castle castle : stops){
            node = getNode(castle);
            if(node != null){
                edges.addAll(getEdges(node));
            }
        }
        return new LinkedList<>(edges);
    }

    public boolean intersects(List<APoint> points, Collection<Edge<Castle>> edges){
        if(points.isEmpty())
            return false;
        else if(points.size() == 1)
            return intersects(points.get(0), points.get(0), edges);
        Point lastPoint = null;
        for(Point point : points){
            if(lastPoint != null && intersects(lastPoint, point, edges)){
                return true;
            }
            lastPoint = point;
        }
        return false;
    }
    public boolean intersects(Point point1, Point point2, Collection<Edge<Castle>> edges){
        return intersects(point1.x, point1.y,point2.x, point2.y, edges);
    }
    public boolean intersects(int x1, int y1, int x2, int y2, Collection<Edge<Castle>> edges){
        for(Edge<Castle> edge : edges){
            if(intersects(x1, y1, x2, y2, edge)){
                return true;
            }
        }
        return false;
    }
    public boolean intersects(int x1, int y1, int x2, int y2, Edge<Castle> edge){
        Point posA = edge.getNodeA().getValue().getLocationOnMap();
        Point posB = edge.getNodeB().getValue().getLocationOnMap();
        return Line2D.linesIntersect(x1, y1, x2, y2, posA.x, posA.y, posB.x, posB.y);
    }

    public double minDistance(Collection<APoint> points, Collection<Edge<Castle>> edges){
        double minDistance = -1, distance;
        for(Point point : points){
            distance = minDistance(point, edges);
            if(minDistance < 0 || minDistance > distance){
                minDistance = distance;
            }
        }
        return minDistance;
    }
    public double minDistance(Point point, Collection<Edge<Castle>> edges){
        double minDistance = -1, distance;
        for (Edge<Castle> edge : edges){
            distance = distance(point, edge);
            if(minDistance < 0 || minDistance > distance){
                minDistance = distance;
            }
        }
        return minDistance;
    }
    public double distance(Point point, Edge<Castle> edge){
        Point posA = edge.getNodeA().getValue().getLocationOnMap();
        Point posB = edge.getNodeB().getValue().getLocationOnMap();
        return Line2D.ptSegDist(posA.x, posA.y, posB.x, posB.y, point.x, point.y);
    }
}
