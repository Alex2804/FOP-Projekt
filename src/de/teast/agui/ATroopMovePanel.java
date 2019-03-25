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

public class ATroopMovePanel extends JPanel implements ChangeListener {
    public JLabel imageLabel;
    public JSlider troopCountSlider;
    public JSpinner troopCountSpinner;
    public SpinnerNumberModel troopCountSpinnerModel;
    public AHealthPanel healthPanel;

    public ATroops troops;
    public Window owner = null;

    public ATroopMovePanel(ATroops troops, Window owner) {
        super();
        this.owner = owner;
        init(troops);
    }

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
                ATroopInfoPanel.showTroopInfoDialog(troops.troop(), owner);
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

    public void updateValues(){
        updateValues(troops.troopCount());
    }
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

    public class AHealthPanel extends JPanel{
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
            setToolTipText("Gesundheit: " + Integer.toString(troops.troop().life)+"/"+Integer.toString(troops.troop().fullLife));
        }
    }

    public static ATroopMoveDialog getTroopMoveDialog(List<ATroops> troops, Window owner){
        return new ATroopMoveDialog(troops, owner);
    }

    public static class ATroopMoveDialog extends JDialog{
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
        protected void initPanel(List<ATroops> troops, Window owner){
            contentPanel = new ATroopMoveDialogPanel(troops, owner);
        }
        public void center(){
            setLocation((getOwner().getX() + (getOwner().getWidth()/  2)) - (getWidth() / 2),
                        (getOwner().getY() + (getOwner().getHeight() / 2)) - (getHeight() / 2));
        }

        public void addOkListener(ActionListener listener){
            okButton.addActionListener(listener);
        }
        public void addCancelListener(ActionListener listener){
            cancelButton.addActionListener(listener);
        }
        public void addButtonListener(ActionListener listener){
            addOkListener(listener);
            addCancelListener(listener);
        }

        public List<ATroops> getMoved(){
            return contentPanel.getMoved();
        }
    }

    public static class ATroopMoveDialogPanel extends JPanel{
        List<ATroopMovePanel> movePanels;
        public ATroopMoveDialogPanel(List<ATroops> troops, Window owner){
            super();
            setLayout(new BorderLayout());
            JScrollPane scrollPane = new JScrollPane(getDialogPanel(troops, owner));
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            add(scrollPane, BorderLayout.CENTER);
        }
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

        public List<ATroops> getMoved(){
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
