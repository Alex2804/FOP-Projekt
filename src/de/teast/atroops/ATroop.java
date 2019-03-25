package de.teast.atroops;

import gui.Resources;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;

/**
 * This class represents a troop for {@link game.goals.AClashOfArmiesGoal}
 * @author Alexander Muth
 */
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
    public int fullLife;
    public int life;
    public int price;

    private ATroop(BufferedImage image, String name, int defenseShortRange, int defenseLongRange, int attackShortRange,
                   int attackLongRange, int longRangeRange, int speed, int fullLife, int life, int price){
        this.image = image;
        this.name = name;
        this.defenseShortRange = defenseShortRange;
        this.defenseLongRange = defenseLongRange;
        this.attackShortRange = attackShortRange;
        this.attackLongRange = attackLongRange;
        this.longRangeRange = longRangeRange;
        this.speed = speed;
        this.fullLife = fullLife;
        this.life = life;
        this.price = price;
    }
    public ATroop(String imagePath, String name, int defenseShortRange, int defenseLongRange, int attackShortRange,
                  int attackLongRange, int longRangeRange, int speed, int life, int price){
        this(null, name, defenseShortRange, defenseLongRange, attackShortRange, attackLongRange, longRangeRange, speed, life, life, price);
        image = images.get(name);
        if(image == null) {
            try {
                image = Resources.getInstance().loadImage(imagePath);
                images.put(name, image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        return getClass()+":"+name+"["+life+"/"+fullLife+"]";
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ATroop){
            return obj.toString().equals(toString());
        }
        return super.equals(obj);
    }

    @Override
    protected ATroop clone() {
        return new ATroop(image, name, defenseShortRange, defenseLongRange, attackShortRange, attackLongRange, longRangeRange, speed, fullLife, life, price);
    }
}
