package de.teast.atroops;

import gui.Resources;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;

public class ATroop {
    private static HashMap<String, BufferedImage> images = new HashMap<>();
    public BufferedImage image;
    public String name;
    public int defenseShortRange;
    public int defenseLongRange;
    public int attackShortRange;
    public int attackLongRange;
    public int longRangeRange;
    public int speed;
    public int life;

    public ATroop(String imagePath, String name, int defenseShortRange, int defenseLongRange, int attackShortRange,
                  int attackLongRange, int longRangeRange, int speed, int life){
        image = images.get(name);
        if(image == null) {
            try {
                image = Resources.getInstance().loadImage(imagePath);
                images.put(name, image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.name = name;
        this.defenseShortRange = defenseShortRange;
        this.defenseLongRange = defenseLongRange;
        this.attackShortRange = attackShortRange;
        this.attackLongRange = attackLongRange;
        this.longRangeRange = longRangeRange;
        this.speed = speed;
        this.life = life;
    }
}
