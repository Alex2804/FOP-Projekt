package de.teast.autils;

import java.awt.*;

/**
 * Basic implementation of a 2D vector. None of the methods modifies the vector object itself.
 * @author Alexander Muth
 */
public class AVector2D {
    public double x, y;

    public AVector2D(double x, double y){
        this.x = x;
        this.y = y;
    }
    public AVector2D(Point source, Point destination){
        this.x = destination.x - source.x;
        this.y = destination.y - source.y;
    }

    /**
     * @param other the vector to add
     * @return the result of this vector addition
     */
    public AVector2D add(AVector2D other){
        return new AVector2D(x + other.x, y + other.y);
    }
    /**
     * @return a negation of this vector
     */
    public AVector2D negate(){
        return new AVector2D(-x, -y);
    }
    /**
     * @param scalar the scalar to multiply with
     * @return the result of this vector-scalar multiplication
     */
    public AVector2D mult(double scalar){
        return new AVector2D(x * scalar, y * scalar);
    }

    /**
     * @return the length of the vector
     */
    public double length(){
        return Math.sqrt((x*x)+(y*y));
    }
    /**
     * @param length the length to scale to
     * @return a to the given {@code length} scaled vector
     *
     * @see #length()
     */
    public AVector2D scale(double length){
        double l = length();
        if(l == length || length == 0)
            return clone();
        return new AVector2D((x/l)*length, (y/l)*length);
    }

    /**
     * @return a perpendicular vector to this (clockwise rotated)
     */
    public AVector2D getPerpendicular(){
        return new AVector2D(y, -x);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof AVector2D){
            AVector2D other = (AVector2D)obj;
            return x == other.x && y == other.y;
        }
        return super.equals(obj);
    }

    @Override
    public AVector2D clone() {
        return new AVector2D(x, y);
    }

    @Override
    public String toString() {
        return getClass() + "[x="+x+",y="+y+"]";
    }
}
