package de.teast.agui;

import de.teast.atroops.ATroop;
import de.teast.atroops.ATroops;
import javafx.util.Pair;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ATroopMovePanel extends JPanel implements ChangeListener {
    public JLabel imageLabel;
    public JSlider troopCountSlider;
    public JSpinner troopCountSpinner;
    public SpinnerNumberModel troopCountSpinnerModel;

    public ATroops troops;
    public JFrame frame = null;

    public ATroopMovePanel(ATroops troops, JFrame frame) {
        super();
        this.frame = frame;
        init(troops);
    }
    public ATroopMovePanel(ATroops troops) {
        super();
        init(troops);
    }

    private void init(ATroops troops){
        this.troops = troops;

        ATroop troop = troops.troop();
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        imageLabel = new JLabel(new ImageIcon(troop.image));

        troopCountSlider = new JSlider(JSlider.HORIZONTAL, 0, troops.troopCount(), (troops.troopCount() > 0) ? 1 : 0);

        troopCountSpinnerModel = new SpinnerNumberModel(troopCountSlider.getValue(), troopCountSlider.getMinimum(), troopCountSlider.getMaximum(), 1);
        troopCountSpinner = new JSpinner(troopCountSpinnerModel);

        troopCountSlider.addChangeListener(this);
        troopCountSpinner.addChangeListener(this);

        imageLabel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(frame == null){
                    ATroopInfoPanel.showTroopInfoDialog(troops.troop());
                }else{
                    ATroopInfoPanel.showTroopInfoDialog(troops.troop(), frame.getX() + frame.getWidth() / 2, frame.getY() + frame.getHeight() / 2);
                }
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

        add(imageLabel);
        add(troopCountSlider);
        add(troopCountSpinner);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if(e.getSource() == troopCountSlider){
            troopCountSpinner.setValue(troopCountSlider.getValue());
        }else if(e.getSource() == troopCountSpinner){
            troopCountSlider.setValue((int)(troopCountSpinner.getValue()));
        }
    }

    public static ATroopMoveDialog getTroopMoveDialog(List<ATroops> troops){
        return new ATroopMoveDialog(troops);
    }

    public static class ATroopMoveDialog extends JFrame{
        List<ATroopMovePanel> movePanels;
        public ATroopMoveDialog(List<ATroops> troops){
            super();
            setLayout(new BorderLayout());
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
            movePanels = new LinkedList<>();
            ATroopMovePanel movePanel;
            for(ATroops t : troops){
                movePanel = new ATroopMovePanel(t, this);
                panel.add(movePanel);
                movePanels.add(movePanel);
            }
            add(panel, BorderLayout.NORTH);
            add(new JPanel(), BorderLayout.CENTER);

            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            pack();
            setVisible(true);
        }
    }

    public static void main(String[] args) {
        ATroop troop = new ATroop("castle1.png", "Test", 0, 1, 2, 3, 4, 5, 6);
        ATroops troops = new ATroops(troop, 15);
        getTroopMoveDialog(Collections.singletonList(troops));
        ATroopInfoPanel.showTroopInfoDialog(troop);
    }
}
