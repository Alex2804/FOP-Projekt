package game.players;

import java.awt.Color;

import game.Player;

public class Human extends Player {
    public Human(String name, Color color) {
        super(name, color);
    }

    @Override
    public Human copy() {
        Human human = new Human(getName(), new Color(getColor().getRGB()));
        human.addPoints(getPoints());
        human.addTroops(getRemainingTroops());
        human.hasJoker = hasJoker;
        return human;
    }
}
