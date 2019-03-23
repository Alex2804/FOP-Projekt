package de.teast.autils;

import java.awt.*;

public class AVector2D {
    public double x, y;

    public AVector2D(double x, double y){
        this.x = x;
        this.y = y;
    }
    public double length(){
        return Math.sqrt((x*x)+(y*y));
    }

    public AVector2D negate(){
        return new AVector2D(-x, -y);
    }

    public AVector2D getPerpendicular(){
        return new AVector2D(y, -x);
    }

    public AVector2D add(AVector2D other){
        return new AVector2D(x + other.x, y + other.y);
    }
    public AVector2D mult(double scalar){
        return new AVector2D(x * scalar, y * scalar);
    }

    public AVector2D scale(double length){
        double l = length();
        if(l == length)
            return clone();
        return new AVector2D((x/l)*length, (y/l)*length);
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
}
