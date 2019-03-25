package de.teast.autils;

import java.awt.*;

/**
 * Basic implemenation of a 2D Point which is based of {@link Point} and adds basic mathematical functions for
 * {@link AVector2D}
 * @author Alexander Muth
 */
public class APoint extends Point {
	private static final long serialVersionUID = -6055528765131261491L;
	public APoint(int x, int y){
        super(x, y);
    }
    public APoint(Point point){
        super(point.x, point.y);
    }

    /**
     * Adds a vector to this point
     * @param vector the vector to add
     * @return the result of this addition
     */
    public APoint add(AVector2D vector){
        return new APoint(x + (int)vector.x, y + (int)vector.y);
    }
    /**
     * @param point the point to get the distance from
     * @return the distance from this point to {@code point}
     */
    public double distance(APoint point){
        return Math.sqrt(Math.pow(x - point.x, 2) + Math.pow(y - point.y, 2));
    }
}
