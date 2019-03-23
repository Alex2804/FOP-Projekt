package de.teast.autils;

import java.awt.*;

public class APoint extends Point {
    public APoint(int x, int y){
        super(x, y);
    }
    public APoint(Point point){
        super(point.x, point.y);
    }

    public APoint add(AVector2D vector){
        return new APoint(x + (int)vector.x, y + (int)vector.y);
    }
}