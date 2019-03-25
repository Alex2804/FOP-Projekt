package de.teast.agui;

import de.teast.AConstants;
import de.teast.atroops.ATroops;
import game.map.Castle;
import gui.Resources;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

/**
 * Class to Display multiple {@link ATroops} objects stationed at a castle for {@link game.goals.AClashOfArmiesGoal}
 * @author Alexander Muth
 */
public class ATroopCountPanel extends JScrollPane {
    public static final int VERTICAL_SPACING = 10;
    public Castle castle;

    public ATroopCountPanel(Window owner, List<ATroops> troops, Castle castle){
        super(JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.castle = castle;
        // Add troop panels
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        ATroopCountTroopPanel troopPanel;
        for(ATroops t : troops){
            troopPanel = new ATroopCountTroopPanel(owner, t);
            troopPanel.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Color.BLACK), new EmptyBorder(VERTICAL_SPACING/2, 5, VERTICAL_SPACING/2, 5)));
            panel.add(troopPanel);
        }

        setViewportView(panel);
    }

    /**
     * @return the height required by this widget
     */
    public int height(){
        if(horizontalScrollBar.isVisible())
            return getPreferredSize().height + horizontalScrollBar.getHeight();
        else
            return getPreferredSize().height;
    }

    /**
     * Class to display one {@link ATroops} object (with its count)
     */
    private static class ATroopCountTroopPanel extends ATroopMovePanel{
        public ATroopCountTroopPanel(Window owner, ATroops troops){
            super(troops, owner);
            imageLabel.setIcon(new ImageIcon(Resources.scaleImage(troops.troop().image, AConstants.TROOP_IMAGE_TROOPS_PANEL_SIZE, AConstants.TROOP_IMAGE_TROOPS_PANEL_SIZE)));
            remove(troopCountSlider);
            remove(troopCountSpinner);
            JLabel countLabel = new JLabel(Integer.toString(troops.troopCount()));
            countLabel.setFont(new Font("Arial", Font.BOLD, (int)(AConstants.TROOP_IMAGE_TROOPS_PANEL_SIZE * 0.8)));
            countLabel.setAlignmentY(JLabel.CENTER_ALIGNMENT);
            add(countLabel);
        }
    }
}
