package de.teast.agui;

import de.teast.AConstants;
import de.teast.atroops.ATroop;
import gui.Resources;

import javax.swing.*;
import java.awt.*;

/**
 * Panel that displays infos for {@link ATroop}
 * @author Alexander Muth
 */
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
        add(new JLabel(new ImageIcon(Resources.scaleImage(troop.image, AConstants.TROOP_IMAGE_INFO_SIZE, AConstants.TROOP_IMAGE_INFO_SIZE)), SwingConstants.CENTER), c);
        c.gridy = 2;
        add(getValuePanel("Kosten: ", troop.price+" Punkte", new Font("Arial", Font.BOLD, 14)), c);
        c.gridy = 3;
        c.insets = new Insets(10, 0, 0, 0);
        tempLabel = new JLabel("Nahkampf:", SwingConstants.CENTER);
        tempLabel.setFont(new Font("Arial", Font.BOLD, 12));
        add(tempLabel, c);
        c.gridy = 5;
        tempLabel = new JLabel("Fernkampf:", SwingConstants.CENTER);
        tempLabel.setFont(new Font("Arial", Font.BOLD, 12));
        add(tempLabel, c);

        c.gridwidth = 1;
        c.insets = new Insets(0, 10, 0, 10);
        c.gridy = 4;
        c.gridx = 0;
        add(getValuePanel("Verteidigung: ", Integer.toString(troop.defenseShortRange)), c);
        c.gridy = 4;
        c.gridx = 1;
        add(getValuePanel("Angriff: ", Integer.toString(troop.attackShortRange)), c);
        c.gridy = 6;
        c.gridx = 0;
        add(getValuePanel("Verteidigung: ", Integer.toString(troop.defenseLongRange)), c);
        c.gridy = 6;
        c.gridx = 1;
        add(getValuePanel("Angriff: ", Integer.toString(troop.attackLongRange)), c);
        c.insets = new Insets(10, 0, 0, 0);
        c.gridwidth = 2;
        c.gridy = 7;
        c.gridx = 0;
        add(getValuePanel("Reichweite: ", Integer.toString(troop.longRangeRange)), c);
        c.insets = new Insets(0, 0, 0, 0);
        c.gridy = 8;
        c.gridx = 0;
        add(getValuePanel("Geschwindigkeit: ", Integer.toString(troop.speed)), c);
        c.insets = new Insets(0, 0, 10, 0);
        c.gridy = 9;
        c.gridx = 0;
        add(getValuePanel("Leben: ", troop.life+"/"+troop.fullLife), c);
    }
    /**
     * Returns a {@link JPanel}, with 2 {@link JLabel}, one is the title and one the value |title value|
     * @param title the title text
     * @param value the value text
     * @return the created {@link JPanel}
     */
    private JPanel getValuePanel(String title, String value){
        JLabel temp = new JLabel();
        return getValuePanel(title, value, temp.getFont());
    }
    /**
     * Returns a {@link JPanel}, with 2 {@link JLabel}, one is the title and one the value |title value|
     * @param title the title text
     * @param value the value text
     * @param font the font for the labels
     * @return the created {@link JPanel}
     */
    private JPanel getValuePanel(String title, String value, Font font){
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = c.gridy = 0;
        c.weighty = 0;
        c.weightx = 1;
        JLabel label = new JLabel(title);
        label.setFont(font);
        panel.add(label, c);
        c.gridx = 1;
        label = new JLabel(value);
        label.setFont(font);
        panel.add(label, c);
        return panel;
    }

    /**
     * Creates a new {@link ATroopInfoDialog} and returns it
     * @param troop the troop to display
     * @param owner the owner of this dialog
     * @return the created dialog
     */
    public static ATroopInfoDialog getTroopInfoDialog(ATroop troop, Window owner){
        return new ATroopInfoDialog(troop, owner);
    }

    /**
     * {@link JDialog}, showing an {@link ATroopInfoPanel}
     */
    public static class ATroopInfoDialog extends JDialog{
        ATroopInfoPanel infoPanel;
        public ATroopInfoDialog(ATroop troop, Window owner){
            super(owner);

            infoPanel = new ATroopInfoPanel(troop);
            add(infoPanel);

            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setTitle("Daten von " + troop.name);
            pack();
            center(owner);
            setModal(true);
        }
        public void center(Window owner){
            setLocation((owner.getX() + (owner.getWidth()/  2)) - (getWidth() / 2),
                    (owner.getY() + (owner.getHeight() / 2)) - (getHeight() / 2));
        }
    }
}
