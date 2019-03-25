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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@link JPanel} to buy {@link ATroop}s for {@link game.goals.AClashOfArmiesGoal}
 * @author Alexander Muth
 */
public class ATroopBuyPanel extends ATroopMovePanel {
	private static final long serialVersionUID = -912260969922416623L;
	ASynchronizer synchronizer;
    int troopCount = 0;
    public static Color defaultColor = null;

    public ATroopBuyPanel(ASynchronizer synchronizer, ATroops troops, Window owner) {
        super(troops, owner);
        imageLabel.setIcon(new ImageIcon(Resources.scaleImage(troops.troop().image, AConstants.TROOP_IMAGE_BUY_PANEL_SIZE, AConstants.TROOP_IMAGE_BUY_PANEL_SIZE)));
        remove(healthPanel);
        this.synchronizer = synchronizer;

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
        Color color;
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
            con = synchronizer.getPoints(this, dif * troops.troop().price);
        }else if(dif < 0){
            synchronizer.putPoints(this, -(dif * troops.troop().price));
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

    /**
     * @param availableTroops the available troops
     * @param availablePoints the amount of available points
     * @param owner the owner of the dialog
     * @return The created {@link ATroopBuyDialog}
     */
    public static ATroopBuyDialog getTroopBuyDialog(ATroop[] availableTroops, int availablePoints, Window owner){
        return new ATroopBuyDialog(availableTroops, availablePoints, owner);
    }

    /**
     * Class to synchronize multiple {@link ATroopBuyPanel}s in an {@link ATroopBuyDialogPanel} and display the
     * available points
     */
    private static class ASynchronizer extends JLabel{
		private static final long serialVersionUID = 8948390271883358989L;
		public int availablePoints;
        public int points;
        public List<ATroopBuyPanel> buyPanels;

        public ASynchronizer(List<ATroopBuyPanel> buyPanels, int points){
            this.buyPanels = buyPanels;
            this.points = points;
            availablePoints = points;
            setFont(new Font("Arial", Font.BOLD, 14));
        }

        /**
         * This method is called when a {@link ATroopBuyPanel} requests points. It removes the requested points
         * if they are available
         * @param buyPanel the panel which requested the points
         * @param points the requested amount of points
         * @return if there are enough points available
         */
        public boolean getPoints(ATroopBuyPanel buyPanel, int points){
            if(points > availablePoints || !buyPanels.contains(buyPanel)){
                update();
                return false;
            }
            availablePoints -= points;
            update();
            return true;
        }
        /**
         * This method is called when a {@link ATroopBuyPanel} doesn't need points anymore
         * @param buyPanel the panel which would put the points back
         * @param points the amount of points to put back
         */
        public void putPoints(ATroopBuyPanel buyPanel, int points){
            if(!buyPanels.contains(buyPanel))
                return;
            availablePoints += points;
            update();
        }
        /**
         * Updates all {@link ATroopBuyPanel}s and the text of available points
         */
        public void update(){
            for(ATroopBuyPanel panel : buyPanels){
                panel.troops.setTroopCount(availablePoints / panel.troops.troop().price);
                panel.updateValues();
            }
            setText("Du hast noch " + availablePoints + " von " + points + " Punkten");
        }
    }

    /**
     * Class to display a {@link ATroopBuyDialogPanel}
     */
    public static class ATroopBuyDialog extends ATroopMoveDialog {
		private static final long serialVersionUID = 3051461916514783705L;
		public ATroopBuyDialog(ATroop[] availableTroops, int points, Window owner){
            super(Arrays.stream(availableTroops).map(t -> new ATroops(t, 0)).collect(Collectors.toList()), owner);
            troopBuyDialogPanel().syncronizer.availablePoints = troopBuyDialogPanel().syncronizer.points = points;
            troopBuyDialogPanel().syncronizer.update();
        }
        @Override
        protected void initPanel(List<ATroops> troops, Window owner) {
            contentPanel = new ATroopBuyDialogPanel(troops, 0, owner);
        }
        /**
         * @return the troop buy panel of this dialog
         */
        protected ATroopBuyDialogPanel troopBuyDialogPanel(){
            return (ATroopBuyDialogPanel)contentPanel;
        }
    }

    /**
     * Class to display multiple {@link ATroopBuyPanel}s.
     */
    public static class ATroopBuyDialogPanel extends ATroopMoveDialogPanel{
		private static final long serialVersionUID = 7916594190215345526L;
		ASynchronizer syncronizer;
        public ATroopBuyDialogPanel(List<ATroops> availableTroops, int points, Window owner){
            super(availableTroops, owner);
            add(syncronizer, BorderLayout.NORTH);
            syncronizer.availablePoints = syncronizer.points = points;
            syncronizer.update();
        }
        @Override
        protected JPanel getDialogPanel(List<ATroops> troops, Window owner) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
            syncronizer = new ASynchronizer(new LinkedList<>(), 0);
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
        @Override
        public List<ATroops> getResult(){
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
