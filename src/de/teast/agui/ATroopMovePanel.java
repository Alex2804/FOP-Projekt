package de.teast.agui;

import de.teast.AConstants;
import de.teast.atroops.ATroop;
import de.teast.atroops.ATroops;
import gui.Resources;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.List;

/**
 * Panel which displays Troops which can be moved
 * @author Alexander Muth
 */
public class ATroopMovePanel extends JPanel implements ChangeListener {
	private static final long serialVersionUID = 4626949115047288268L;
	public JLabel imageLabel;
    public JSlider troopCountSlider;
    public JSpinner troopCountSpinner;
    public SpinnerNumberModel troopCountSpinnerModel;
    public AHealthPanel healthPanel;

    public ATroops troops;
    public Window owner;

    public ATroopMovePanel(ATroops troops, Window owner) {
        super();
        this.owner = owner;
        init(troops);
    }
    /**
     * Initializes the panel gui
     * @param troops the troops to display
     */
    private void init(ATroops troops){
        this.troops = troops;

        ATroop troop = troops.troop();
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        healthPanel = new AHealthPanel();
        healthPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        healthPanel.setMaximumSize(new Dimension(AHealthPanel.width, healthPanel.getMaximumSize().height));

        imageLabel = new JLabel(new ImageIcon(Resources.scaleImage(troop.image, AConstants.TROOP_IMAGE_MOVE_PANEL_SIZE, AConstants.TROOP_IMAGE_MOVE_PANEL_SIZE)));
        imageLabel.setToolTipText(troop.name + " (Klicken für mehr Infos!)");

        troopCountSlider = new JSlider(JSlider.HORIZONTAL, 0, troops.troopCount(), (troops.troopCount() > 0) ? 1 : 0);

        troopCountSpinnerModel = new SpinnerNumberModel(troopCountSlider.getValue(), troopCountSlider.getMinimum(), troopCountSlider.getMaximum(), 1);
        troopCountSpinner = new JSpinner(troopCountSpinnerModel);

        troopCountSlider.addChangeListener(this);
        troopCountSpinner.addChangeListener(this);

        imageLabel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ATroopInfoPanel.ATroopInfoDialog dialog = ATroopInfoPanel.getTroopInfoDialog(troops.troop(), owner);
                dialog.setVisible(true);
            }
            @Override
            public void mousePressed(MouseEvent e) {}
            @Override
            public void mouseReleased(MouseEvent e) {}
            @Override
            public void mouseEntered(MouseEvent e) {
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        add(healthPanel);
        add(imageLabel);
        troopCountSlider.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        add(troopCountSlider);
        troopCountSpinner.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        add(troopCountSpinner);

        updateValues();
    }

    /**
     * Updates the slider and spinner max values
     * @see #updateValues()
     */
    public void updateValues(){
        updateValues(troops.troopCount());
    }
    /**
     * Updates the slider and spinner max values
     * @param maxValue the maxValue
     */
    public void updateValues(int maxValue){
        troopCountSlider.setMaximum(maxValue);
        troopCountSpinnerModel.setMaximum(maxValue);
        if(troopCountSlider.getValue() > maxValue){
            troopCountSlider.setValue(maxValue);
        }
        if((int)(troopCountSpinner.getValue()) > maxValue){
            troopCountSpinner.setValue(maxValue);
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        updateValues();
        if(e.getSource() == troopCountSlider){
            troopCountSpinner.setValue(troopCountSlider.getValue());
        }else if(e.getSource() == troopCountSpinner){
            troopCountSlider.setValue((int)(troopCountSpinner.getValue()));
        }
    }

    /**
     * @param troops the troops to display
     * @param owner the owner of the dialog
     * @return the new created dialog
     */
    public static ATroopMoveDialog getTroopMoveDialog(List<ATroops> troops, Window owner){
        return new ATroopMoveDialog(troops, owner);
    }

    /**
     * Panel which displays the health of a troop
     */
    public class AHealthPanel extends JPanel{
		private static final long serialVersionUID = 3074606997746745982L;
		public static final int width = 20;
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            ATroop troop = troops.troop();
            double percentage = ((double)troop.life) / troop.fullLife;
            Color color = new Color((int)((1-percentage)*255), (int)(percentage*255), 0);
            g.setColor(color);
            int x = getBorder().getBorderInsets(this).left;
            int y = getBorder().getBorderInsets(this).top;
            int height = (int)(percentage * getHeight()) - y - getBorder().getBorderInsets(this).bottom;
            int width = getWidth() - (x + getBorder().getBorderInsets(this).right);
            g.fillRect(x, y+getHeight()-height, width, height);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, x+width-1, y+(getHeight()-y-getBorder().getBorderInsets(this).bottom-1));
            setToolTipText("Gesundheit: " + troops.troop().life+"/"+troops.troop().fullLife);
        }
    }

    /**
     * Dialog which displays {@link ATroopMoveDialogPanel} and ok and cancel buttons
     */
    public static class ATroopMoveDialog extends JDialog{
		private static final long serialVersionUID = -6567922416045463880L;
		public JButton okButton, cancelButton;
        protected ATroopMoveDialogPanel contentPanel;
        public ATroopMoveDialog(List<ATroops> troops, Window owner){
            super(owner, "Truppen verschieben");

            initPanel(troops, owner);
            getContentPane().add(contentPanel);

            okButton = new JButton("Ok");
            cancelButton = new JButton("Abbrechen");
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout());
            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);
            getContentPane().add(buttonPanel, BorderLayout.PAGE_END);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            pack();
            center();
            setModal(true);
        }
        /**
         * initializes the contentPanel
         * @param troops the troops to display
         * @param owner the owner of the dialog
         */
        protected void initPanel(List<ATroops> troops, Window owner){
            contentPanel = new ATroopMoveDialogPanel(troops, owner);
        }

        /**
         * Centers the dialog inside the owner
         */
        public void center(){
            setLocation((getOwner().getX() + (getOwner().getWidth()/  2)) - (getWidth() / 2),
                        (getOwner().getY() + (getOwner().getHeight() / 2)) - (getHeight() / 2));
        }

        /**
         * Adds an {@link ActionListener} to the {@code ok} button
         * @param listener the listener
         */
        public void addOkListener(ActionListener listener){
            okButton.addActionListener(listener);
        }
        /**
         * Adds an {@link ActionListener} to the {@code cancel} button
         * @param listener the listener
         */
        public void addCancelListener(ActionListener listener){
            cancelButton.addActionListener(listener);
        }
        /**
         * Adds an {@link ActionListener} to the {@code ok} and {@code cancel} button
         * @param listener the listener
         */
        public void addButtonListener(ActionListener listener){
            addOkListener(listener);
            addCancelListener(listener);
        }

        /**
         * @return the resulting {@link ATroops} of this dialog
         */
        public List<ATroops> getResult(){
            return contentPanel.getResult();
        }
    }

    /**
     * Panel which displays multiple {@link #ATroopMovePanel(List, Window)}
     */
    public static class ATroopMoveDialogPanel extends JPanel{
		private static final long serialVersionUID = -3020259341867619444L;
		List<ATroopMovePanel> movePanels;
        public ATroopMoveDialogPanel(List<ATroops> troops, Window owner){
            super();
            setLayout(new BorderLayout());
            JScrollPane scrollPane = new JScrollPane(getDialogPanel(troops, owner));
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            add(scrollPane, BorderLayout.CENTER);
        }
        /**
         * Creates the dialog content panel
         * @param troops the troops to display
         * @param owner the owner of this dialog
         * @return the content panel
         */
        protected JPanel getDialogPanel(List<ATroops> troops, Window owner){
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
            movePanels = new LinkedList<>();
            ATroopMovePanel movePanel;
            for(ATroops t : troops){
                movePanel = new ATroopMovePanel(t, owner);
                movePanel.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Color.BLACK), new EmptyBorder(5, 5, 5, 5)));
                panel.add(movePanel);
                movePanels.add(movePanel);
            }
            JPanel spacer = new JPanel(new BorderLayout());
            spacer.add(new JPanel(), BorderLayout.CENTER);
            spacer.setMinimumSize(new Dimension(0, 0));
            spacer.setPreferredSize(getMinimumSize());
            panel.add(spacer);
            return panel;
        }

        /**
         * @return the resulting {@link ATroops} of the this panel
         */
        public List<ATroops> getResult(){
            List<ATroops> returnList = new LinkedList<>();
            for(ATroopMovePanel movePanel : movePanels){
                if(movePanel.troopCountSlider.getValue() > 0){
                    returnList.add(new ATroops(movePanel.troops.troop(), movePanel.troopCountSlider.getValue()));
                }
            }
            return returnList;
        }
    }
}
