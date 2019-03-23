package de.teast.agui;

import de.teast.atroops.ATroop;

import javax.swing.*;
import java.awt.*;

public class ATroopInfoPanel extends JPanel {

    public ATroopInfoPanel(ATroop troop){
        super(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = c.gridy = 0;
        c.insets = new Insets(10, 10, 0, 10);
        c.gridwidth = 2;
        JLabel tempLabel = new JLabel(troop.name, SwingConstants.CENTER);
        tempLabel.setFont(new Font("Arial", Font.BOLD, 20));
        add(tempLabel, c);
        c.gridy = 1;
        add(new JLabel(new ImageIcon(troop.image), SwingConstants.CENTER), c);
        c.gridy = 2;
        c.insets = new Insets(10, 0, 0, 0);
        tempLabel = new JLabel("Nahkampf:", SwingConstants.CENTER);
        tempLabel.setFont(new Font("Arial", Font.BOLD, 12));
        add(tempLabel, c);
        c.gridy = 4;
        tempLabel = new JLabel("Fernkampf:", SwingConstants.CENTER);
        tempLabel.setFont(new Font("Arial", Font.BOLD, 12));
        add(tempLabel, c);
        c.gridwidth = 1;
        c.insets = new Insets(0, 10, 0, 10);
        c.gridy = 3;
        c.gridx = 0;
        add(getValuePanel("Verteidigung: ", Integer.toString(troop.defenseShortRange)), c);
        c.gridy = 3;
        c.gridx = 1;
        add(getValuePanel("Angriff: ", Integer.toString(troop.attackShortRange)), c);
        c.gridy = 5;
        c.gridx = 0;
        add(getValuePanel("Verteidigung: ", Integer.toString(troop.defenseLongRange)), c);
        c.gridy = 5;
        c.gridx = 1;
        add(getValuePanel("Angriff: ", Integer.toString(troop.attackLongRange)), c);
        c.insets = new Insets(10, 0, 0, 0);
        c.gridwidth = 2;
        c.gridy = 6;
        c.gridx = 0;
        add(getValuePanel("Reichweite: ", Integer.toString(troop.longRangeRange)), c);
        c.insets = new Insets(0, 0, 0, 0);
        c.gridy = 7;
        c.gridx = 0;
        add(getValuePanel("Geschwindigkeit: ", Integer.toString(troop.speed)), c);
        c.insets = new Insets(0, 0, 10, 0);
        c.gridy = 8;
        c.gridx = 0;
        add(getValuePanel("Leben: ", Integer.toString(troop.life)), c);
    }
    private JPanel getValuePanel(String title, String value){
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = c.gridy = 0;
        c.weighty = 0;
        c.weightx = 1;
        panel.add(new JLabel(title), c);
        c.gridx = 1;
        panel.add(new JLabel(value), c);
        return panel;
    }

    public static void showTroopInfoDialog(ATroop troop){
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        showTroopInfoDialog(troop, (int)(dimension.getWidth() / 2), (int)(dimension.getHeight() / 2));
    }
    public static void showTroopInfoDialog(ATroop troop, int xCenter, int yCenter){
        ATroopInfoDialog dialog = new ATroopInfoDialog(troop, xCenter, yCenter);
    }

    public static class ATroopInfoDialog extends JFrame{
        ATroopInfoPanel infoPanel;
        public ATroopInfoDialog(ATroop troop, int xCenter, int yCenter){
            super();

            infoPanel = new ATroopInfoPanel(troop);
            add(infoPanel);

            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setTitle("Daten von " + troop.name);
            pack();
            setLocation(xCenter - getWidth() / 2, yCenter - getHeight() / 2);
            setVisible(true);
        }
    }
}
