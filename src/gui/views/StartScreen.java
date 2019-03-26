package gui.views;

import gui.GameWindow;
import gui.View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

public class StartScreen extends View {

    private JButton btnStart, btnStats, btnInfo, btnQuit, btnCredits;
    private JLabel lblTitle;

    public StartScreen(GameWindow gameWindow) {
        super(gameWindow);
    }

    @Override
    public void onResize() {
        int width = getWidth();
        int height = getHeight();
        int labelHeight = 40;

        int offsetY = (height - 4 * (BUTTON_SIZE.height + 15) - labelHeight) / 3;

        lblTitle.setSize(width, labelHeight);
        lblTitle.setLocation(0, offsetY);

        offsetY += labelHeight + 30;

        int offsetX = (width - BUTTON_SIZE.width) / 2;
        JButton[] buttons = { btnStart, btnStats, btnInfo, btnQuit, btnCredits };
        for (JButton button : buttons) {
            button.setLocation(offsetX, offsetY);
            offsetY += BUTTON_SIZE.height + 15;
        }
    }

    @Override
    protected void onInit() {
        this.lblTitle = createLabel("Game of Castles", 25);
        this.lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        this.lblTitle.setFont(View.createCelticFont(25));
        this.btnStart = createButton("Start");
        this.btnStats = createButton("Punkte");
        this.btnInfo = createButton("Info");
        this.btnQuit = createButton("Beenden");
        this.btnCredits = createButton("Credits");

        getWindow().setSize(750, 450);
        getWindow().setMinimumSize(new Dimension(600, 400));
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if(actionEvent.getSource() == btnQuit) {
            getWindow().dispose();
        } else if(actionEvent.getSource() == btnStart) {
            getWindow().setView(new GameMenu(getWindow()));
        } else if(actionEvent.getSource() == btnInfo) {
            getWindow().setView(new InfoView(getWindow()));
        } else if(actionEvent.getSource() == btnStats) {
            getWindow().setView(new HighscoreView(getWindow()));
        } else if(actionEvent.getSource() == btnCredits){
            new ACreditsDialog(getWindow());
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        BufferedImage soldiers[] = getWindow().getResources().getSoldiers();
        int width = 200;
        int height = (int) (((double)width / soldiers[0].getWidth()) * soldiers[0].getHeight());

        g.drawImage(soldiers[0], 25, 100, width, height, null);
        g.drawImage(soldiers[1], getWidth() - 25 - width, 100, width, height, null);
    }

    public class ACreditsDialog extends JDialog{
        public ACreditsDialog(GameWindow owner){
            super(owner);

            JPanel outerPanel = new JPanel(new BorderLayout());
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
            panel.add(new JLabel("Horse Icon by: Freepik (https://www.freepik.com/) from https://www.flaticon.com/"));
            panel.add(new JLabel("Sword Icon by: Freepik (https://www.freepik.com/) from https://www.flaticon.com/"));
            panel.add(new JLabel("Spear Icon by: Freepik (https://www.freepik.com/) from https://www.flaticon.com/"));
            panel.add(new JLabel("Bow Icon by: Those Icons (https://www.flaticon.com/authors/those-icons) from https://www.flaticon.com/"));
            panel.add(new JLabel("Crossbow Icon by: Smashicons (https://www.flaticon.com/authors/smashicons) from https://www.flaticon.com/"));
            outerPanel.add(panel, BorderLayout.CENTER);
            getContentPane().add(outerPanel);

            setSize(owner.getWidth() - 100, owner.getHeight() - 100);
            center(owner);
            setModal(true);
            setVisible(true);
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        }
        public void center(Window owner){
            setLocation((owner.getX() + (owner.getWidth()/  2)) - (getWidth() / 2),
                    (owner.getY() + (owner.getHeight() / 2)) - (getHeight() / 2));
        }
    }
}
