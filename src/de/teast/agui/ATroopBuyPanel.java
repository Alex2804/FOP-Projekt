package de.teast.agui;

import de.teast.AConstants;
import de.teast.atroops.ATroop;
import de.teast.atroops.ATroops;
import gui.Resources;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ATroopBuyPanel extends ATroopMovePanel {
    ASyncronizer syncronizer;
    int troopCount = 0;
    public static Color defaultColor = null;

    public ATroopBuyPanel(ASyncronizer syncronizer, ATroops troops, Window owner) {
        super(troops, owner);
        imageLabel.setIcon(new ImageIcon(Resources.scaleImage(troops.troop().image, AConstants.TROOP_IMAGE_BUY_PANEL_SIZE, AConstants.TROOP_IMAGE_BUY_PANEL_SIZE)));
        remove(healthPanel);
        this.syncronizer = syncronizer;

        this.troopCountSpinner.addChangeListener(this);
        this.troopCountSlider.addChangeListener(this);

        imageLabel.setToolTipText("Preis: " + troops.troop().price + " (Klicken für mehr Infos!)");
    }

    @Override
    public void updateValues(){
        updateValues(troopCount + troops.troopCount());
    }
    @Override
    public void updateValues(int maxValue) {
        super.updateValues(maxValue);
        Color color = new Color(0, 0, 0, 0);
        if (maxValue <= 0){
            color = new Color(255, 0, 0, 50);
        }else{
            if(defaultColor == null){
                JPanel panel = new JPanel();
                defaultColor = panel.getBackground();
            }
            color = defaultColor;
        }
        setBackground(color);
        imageLabel.setBackground(getBackground());
        troopCountSlider.setOpaque(false);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        updateValues();
        int dif = 0;
        if(e.getSource() == troopCountSlider){
            dif = troopCountSlider.getValue() - (int)troopCountSpinner.getValue();
        }else if(e.getSource() == troopCountSpinner){
            dif = (int)troopCountSpinner.getValue() - troopCountSlider.getValue();
        }

        boolean con = false;
        troopCount += dif;
        if(dif > 0){
            con = syncronizer.getPoints(this, dif * troops.troop().price);
        }else if(dif < 0){
            syncronizer.putPoints(this, -(dif * troops.troop().price));
            con = true;
        }
        if(!con){
            troopCount -= dif;
            return;
        }

        if(e.getSource() == troopCountSlider){
            troopCountSpinner.setValue(troopCountSlider.getValue());
        }else if(e.getSource() == troopCountSpinner){
            troopCountSlider.setValue((int)(troopCountSpinner.getValue()));
        }
    }

    private static class ASyncronizer extends JLabel{
        public int availablePoints;
        public int points;
        public List<ATroopBuyPanel> buyPanels;

        public ASyncronizer(List<ATroopBuyPanel> buyPanels, int points){
            this.buyPanels = buyPanels;
            this.points = points;
            availablePoints = points;
            setFont(new Font("Arial", Font.BOLD, 14));
        }

        public boolean getPoints(ATroopBuyPanel buyPanel, int points){
            if(points > availablePoints || !buyPanels.contains(buyPanel)){
                update();
                return false;
            }
            availablePoints -= points;
            update();
            return true;
        }
        public void putPoints(ATroopBuyPanel buyPanel, int points){
            if(!buyPanels.contains(buyPanel))
                return;
            availablePoints += points;
            update();
        }
        public void update(){
            for(ATroopBuyPanel panel : buyPanels){
                panel.troops.setTroopCount(availablePoints / panel.troops.troop().price);
                panel.updateValues();
            }
            setText("Du hast noch " + availablePoints + " von " + points + " Punkten");
        }
    }

    public static ATroopBuyDialog getTroopBuyDialog(ATroop[] availableTroops, int availablePoints, Frame owner){
        return new ATroopBuyDialog(availableTroops, availablePoints, owner);
    }

    public static class ATroopBuyDialog extends ATroopMoveDialog {
        public ATroopBuyDialog(ATroop[] availableTroops, int points, Frame owner){
            super(Arrays.stream(availableTroops).map(t -> new ATroops(t, 0)).collect(Collectors.toList()), owner);
            troopBuyDialogPanel().syncronizer.availablePoints = troopBuyDialogPanel().syncronizer.points = points;
            troopBuyDialogPanel().syncronizer.update();
        }
        @Override
        protected void initPanel(List<ATroops> troops, Window owner) {
            contentPanel = new ATroopBuyDialogPanel(troops, 0, owner);
        }
        protected ATroopBuyDialogPanel troopBuyDialogPanel(){
            return (ATroopBuyDialogPanel)contentPanel;
        }
        public List<ATroops> getBuyed(){
            return troopBuyDialogPanel().getBuyed();
        }
    }

    public static class ATroopBuyDialogPanel extends ATroopMoveDialogPanel{
        ASyncronizer syncronizer;
        public ATroopBuyDialogPanel(ATroop[] availableTroops, int points, Window owner){
            this(Arrays.stream(availableTroops).map(t -> new ATroops(t, 0)).collect(Collectors.toList()), points, owner);
        }
        protected ATroopBuyDialogPanel(List<ATroops> availableTroops, int points, Window owner){
            super(availableTroops, owner);
            add(syncronizer, BorderLayout.NORTH);
            syncronizer.availablePoints = syncronizer.points = points;
            syncronizer.update();
        }
        @Override
        protected JPanel getDialogPanel(List<ATroops> troops, Window owner) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
            syncronizer = new ASyncronizer(new LinkedList<>(), 0);
            ATroopBuyPanel buyPanel;
            for(ATroops t : troops){
                buyPanel = new ATroopBuyPanel(syncronizer, t, owner);
                buyPanel.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Color.BLACK), new EmptyBorder(5, 5, 5, 5)));
                panel.add(buyPanel);
                syncronizer.buyPanels.add(buyPanel);
            }
            JPanel spacer = new JPanel(new BorderLayout());
            spacer.add(new JPanel(), BorderLayout.CENTER);
            spacer.setMinimumSize(new Dimension(0, 0));
            spacer.setPreferredSize(spacer.getMinimumSize());
            panel.add(spacer);
            return panel;
        }

        public List<ATroops> getBuyed(){
            List<ATroops> returnList = new LinkedList<>();
            for(ATroopBuyPanel panel : syncronizer.buyPanels){
                if(panel.troopCount > 0){
                    returnList.add(new ATroops(panel.troops.troop(), panel.troopCount));
                }
            }
            return returnList;
        }
    }
}
