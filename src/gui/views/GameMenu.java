package gui.views;

import de.teast.AConstants;
import de.teast.aextensions.ajoker.AJoker;
import de.teast.aextensions.ajoker.ATroopJoker;
import game.*;
import game.map.MapSize;
import game.players.ABasicAI;
import game.players.BasicAI;
import gui.GameWindow;
import gui.View;
import gui.components.ColorChooserButton;
import gui.components.NumberChooser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;

public class GameMenu extends View {

    private JLabel lblTitle;
    private JLabel lblPlayerCount;
    private JLabel lblMapSize;
    private JLabel lblGoal;
    private JTextArea lblGoalDescription;

    private NumberChooser playerCount;
    private int supportedPlayerCount;
    private Class<?>[] supportedPlayerTypes;
    private JComboBox mapSize;
    private MapSize[] supportedMapSizes;
    private JComboBox goal;
    private JComponent[][] playerConfig;
    private JButton btnStart, btnBack;

    private JComboBox[] jokerComboBox;
    private AJoker[] supportedJoker;
    private Class<?>[] jokers;

    public static final boolean training = Game.training;
    public static final int threadCount = 1;

    // map size, type?
    // goal?

    public GameMenu(GameWindow gameWindow) {
        super(gameWindow);
    }

    @Override
    public void onResize() {
        updatePlayerCount();

        int offsetY = 25;
        int offsetX = 25;

        lblTitle.setLocation(offsetX, offsetY);
        offsetY += 50;

        int columnWidth = Math.max(300, (getWidth() - 75) / 2);

        // Column 1
        offsetX = (getWidth() - 2*columnWidth - 25) / 2 + (columnWidth - 350) / 2;
        lblPlayerCount.setLocation(offsetX, offsetY + 2);
        playerCount.setLocation(offsetX + lblPlayerCount.getWidth() + 10, offsetY);
        offsetY += 50;

        for(int i = 0; i < GameConstants.MAX_PLAYERS; i++) {
            int tempOffsetX = offsetX;
            for(JComponent c : playerConfig[i]) {
                c.setLocation(tempOffsetX, offsetY);
                tempOffsetX += c.getWidth() + 10;
                c.setEnabled(i < playerCount.getValue());
            }

            jokerComboBox[i].setLocation(tempOffsetX, offsetY);
            if(ABasicAI.class.isAssignableFrom(supportedPlayerTypes[((JComboBox)playerConfig[i][3]).getSelectedIndex()])) {
                jokerComboBox[i].setEnabled(false);
                jokerComboBox[i].setSelectedIndex(jokerComboBox[i].getItemCount() - 1);
            } else if(BasicAI.class.isAssignableFrom(supportedPlayerTypes[((JComboBox)playerConfig[i][3]).getSelectedIndex()])){
                jokerComboBox[i].setEnabled(false);
                jokerComboBox[i].setSelectedIndex(0);
            } else {
                jokerComboBox[i].setEnabled(i < playerCount.getValue());
            }

            offsetY += 40;
        }

        // Column 2
        //map
        offsetY = 125 - lblMapSize.getHeight();
        offsetX = (getWidth() - 2*columnWidth - 25) / 2 + columnWidth + 25 + (columnWidth - mapSize.getWidth()) / 2 + 50;
        lblMapSize.setLocation(offsetX, offsetY); offsetY += lblMapSize.getHeight();
        mapSize.setLocation(offsetX, offsetY); offsetY += mapSize.getHeight() + 10;
        //goal
        lblGoal.setLocation(offsetX, offsetY); offsetY += lblGoal.getHeight();
        goal.setLocation(offsetX, offsetY); offsetY += goal.getHeight();
        lblGoalDescription.setLocation(offsetX, offsetY);
        lblGoalDescription.setSize(goal.getWidth() + 25, getHeight() - offsetY - BUTTON_SIZE.height - 50);

        // Button bar
        offsetY = this.getHeight() - BUTTON_SIZE.height - 25;
        offsetX = (this.getWidth() - 2*BUTTON_SIZE.width - 25) / 2;
        btnBack.setLocation(offsetX, offsetY);
        btnStart.setLocation(offsetX + BUTTON_SIZE.width + 25, offsetY);
    }

    @Override
    protected void onInit() {

        // Title
        lblTitle = createLabel("Neues Spiel starten", 25, true);

        // Player Count
        lblPlayerCount = createLabel("Anzahl Spieler:", 16);
        playerCount = new NumberChooser(2, GameConstants.MAX_PLAYERS, 2);
        playerCount.setSize(125, 25);
        playerCount.addValueListener((oldValue, newValue) -> onResize());
        add(playerCount);

        // Player rows:
        // [Number] [Color] [Name] [Human/AI] (Team?)
        Vector<String> playerTypes = new Vector<>();
        for(Class<?> c : GameConstants.PLAYER_TYPES)
            playerTypes.add(c.getSimpleName());

        jokers = new Class[GameConstants.MAX_PLAYERS];
        jokerComboBox = new JComboBox[GameConstants.MAX_PLAYERS];
        for(int i=0; i < jokerComboBox.length; i++){
            jokerComboBox[i] = new JComboBox<>();
            jokerComboBox[i].addActionListener(new AJokerListener(i));
            jokerComboBox[i].setSize(150, 25);
            add(jokerComboBox[i]);
        }

        playerConfig = new JComponent[GameConstants.MAX_PLAYERS][];
        for(int i = 0; i < GameConstants.MAX_PLAYERS; i++) {
            playerConfig[i] = new JComponent[] {
                createLabel(String.format("%d.", i + 1),16),
                new ColorChooserButton(GameConstants.PLAYER_COLORS[i]),
                new JTextField(String.format("Spieler %d", i + 1)),
                new JComboBox<>(),
            };

            playerConfig[i][1].setSize(25, 25);
            playerConfig[i][2].setSize(130, 25);
            playerConfig[i][3].setSize(90, 25);
            ((JComboBox)playerConfig[i][3]).addActionListener((a) -> onResize());

            for(JComponent c : playerConfig[i])
                add(c);
        }

        // GameMap config
        lblMapSize = createLabel("Kartengröße", 16);
        mapSize = createCombobox(MapSize.getMapSizes(), MapSize.MEDIUM.ordinal());

        // Goals
        Vector<String> goalNames = new Vector<>();
        for(Goal goal : GameConstants.GAME_GOALS)
            goalNames.add(goal.getName());

        lblGoal = createLabel("Mission", 16);
        lblGoalDescription = createTextArea(GameConstants.GAME_GOALS[0].getDescription(), true);

        goal = createCombobox(goalNames, 0);
        goal.addItemListener(itemEvent -> {
            int i = goal.getSelectedIndex();
            if(i < 0 || i >= GameConstants.GAME_GOALS.length) {
                lblGoalDescription.setText("");
            } else {
                lblGoalDescription.setText(GameConstants.GAME_GOALS[i].getDescription());
                updateJokers();
                updateMapSize();
                updatePlayerTypes();
                updatePlayerCount();
                onResize();
            }
        });
        updateJokers();
        updateMapSize();
        updatePlayerTypes();
        updatePlayerCount();

        // Buttons
        btnBack = createButton("Zurück");
        btnStart = createButton("Starten");

        getWindow().setSize(750, 450);
        getWindow().setMinimumSize(new Dimension(750, 450));
    }

    public void updatePlayerCount(){
        supportedPlayerCount = GameConstants.GAME_GOALS[goal.getSelectedIndex()].getMaxPlayerCount();
        while(playerCount.getValue() > supportedPlayerCount){
            playerCount.decrement();
        }
    }

    public void updateJokers(){
        Vector<String> jokerNames = new Vector<>();
        supportedJoker = GameConstants.GAME_GOALS[goal.getSelectedIndex()].getSupportedJokers();
        for(AJoker joker : supportedJoker)
            jokerNames.add(joker.getJokerName());

        for (int i=0; i<jokerComboBox.length; i++) {
            ActionListener actionListener = jokerComboBox[i].getActionListeners()[0];
            jokerComboBox[i].removeActionListener(actionListener);
            jokerComboBox[i].removeAllItems();
            if (supportedJoker != null && supportedJoker.length > 0) {
                for (String name : jokerNames)
                    jokerComboBox[i].addItem(name);
                jokerComboBox[i].setToolTipText(supportedJoker[jokerComboBox[i].getSelectedIndex()].getJokerDescription());
                jokers[i] = supportedJoker[jokerComboBox[i].getSelectedIndex()].getClass();
            }else{
                jokerComboBox[i].setToolTipText(null);
                jokers[i] = null;
            }
            jokerComboBox[i].addActionListener(actionListener);
        }
    }

    public void updateMapSize(){
        mapSize.removeAllItems();
        supportedMapSizes = GameConstants.GAME_GOALS[goal.getSelectedIndex()].getSupportedMapSizes();
        for(MapSize size : supportedMapSizes){
            mapSize.addItem(size.toString());
        }
    }

    public void updatePlayerTypes(){
        JComboBox comboBox;
        ActionListener actionListener;
        supportedPlayerTypes = GameConstants.GAME_GOALS[goal.getSelectedIndex()].getSupportedPlayerTypes();
        for(int i=0; i<playerConfig.length; i++){
            comboBox = (JComboBox) playerConfig[i][3];
            actionListener = comboBox.getActionListeners()[0];
            comboBox.removeActionListener(actionListener);
            comboBox.removeAllItems();
            for(Class<?> playerType : supportedPlayerTypes){
                comboBox.addItem(playerType.getSimpleName());
            }
            comboBox.addActionListener(actionListener);
        }
    }

    private class AJokerListener implements ActionListener{
        int index;
        public AJokerListener(int index){
            super();
            this.index = index;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            AJoker[] jokersTemp = AConstants.GAME_JOKERS.get(GameConstants.GAME_GOALS[goal.getSelectedIndex()].getClass());
            JComboBox<String> jokerBox = jokerComboBox[index];
            jokers[index] = jokersTemp[jokerBox.getSelectedIndex()].getClass();
            jokerBox.setToolTipText(jokersTemp[jokerBox.getSelectedIndex()].getJokerDescription());
        }
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if(actionEvent.getSource() == btnBack)
            getWindow().setView(new StartScreen(getWindow()));
        else if(actionEvent.getSource() == btnStart) {

            try {
                // Check Inputs
                int playerCount = this.playerCount.getValue();
                int mapSize = this.mapSize.getSelectedIndex();
                int goalIndex = this.goal.getSelectedIndex();

                // Should never happen
                if (playerCount < 2 || playerCount > GameConstants.MAX_PLAYERS) {
                    showErrorMessage("Bitte geben Sie eine gültige Spielerzahl an.", "Ungültige Eingaben");
                    return;
                }

                // Should never happen
                if (mapSize < 0 || mapSize >= supportedMapSizes.length) {
                    showErrorMessage("Bitte geben Sie eine gültige Kartengröße an.", "Ungültige Eingaben");
                    return;
                }

                // Should never happen
                if (goalIndex < 0 || goalIndex >= GameConstants.GAME_GOALS.length) {
                    showErrorMessage("Bitte geben Sie ein gültiges Spielziel an.", "Ungültige Eingaben");
                    return;
                }

                // Create Players
                Game game = new Game();
                for (int i = 0; i < playerCount; i++) {
                    String name = ((JTextField) playerConfig[i][2]).getText().replaceAll(";", "").trim();
                    if (name.isEmpty()) {
                        showErrorMessage(String.format("Bitte geben Sie einen gültigen Namen für Spieler %d an.", i + 1), "Ungültige Eingaben");
                        return;
                    }

                    Color color = ((ColorChooserButton) playerConfig[i][1]).getSelectedColor();
                    int playerType = ((JComboBox) playerConfig[i][3]).getSelectedIndex();

                    if (playerType < 0 || playerType >= supportedPlayerTypes.length) {
                        showErrorMessage(String.format("Bitte geben Sie einen gültigen Spielertyp für Spieler %d an.", i + 1), "Ungültige Eingaben");
                        return;
                    }

                    Player player = Player.createPlayer(supportedPlayerTypes[playerType], name, color);
                    if (player == null) {
                        showErrorMessage(String.format("Fehler beim Erstellen von Spieler %d", i + 1), "Unbekannter Fehler");
                        return;
                    }

                    try {
                        if(jokers[i] != null){
                            if(player instanceof ABasicAI && supportedMapSizes[mapSize] == MapSize.SMALL){
                                AJoker joker = new ATroopJoker(game, player);
                                player.addJoker(joker);
                            }else {
                                AJoker joker = (AJoker) jokers[i].getConstructor(Game.class, Player.class).newInstance(game, player);
                                player.addJoker(joker);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    game.addPlayer(player);
                }

                Goal goal = GameConstants.GAME_GOALS[goalIndex];
                GameView gameView;
                game.setGoal(goal);
                game.setMapSize(supportedMapSizes[mapSize]);
                if(game.isClashOfArmiesGoal() && playerCount > 2){
                    showInfoMessage("ClashOfArmies kann nur zu zweit gespielt werden!\n" +
                            "Wähle einen anderen Spielmodus oder entferne " + ((playerCount - 2 > 1) ? playerCount - 2 + " Spieler" : "einen Spieler"), "Zu viele Spieler");
                    return;
                }
                if(!training) {
                    gameView = new GameView(getWindow(), game);
                }
                if(!training) {
                    game.start(gameView);
                    getWindow().setView(gameView);
                }else{
                    List<GameViewTraining.ATrainingThread> threads = new LinkedList<>();
                    for(int i=0; i<threadCount; i++){
                        GameViewTraining.ATrainingThread trainingThread = new GameViewTraining.ATrainingThread(getWindow(), game.copy(), i);
                        threads.add(trainingThread);
                        trainingThread.start();
                    }
                    while(!threads.isEmpty() && threads.get(0).view == null){

                    }
                    getWindow().setView(threads.get(0).view);
                }
            } catch(IllegalArgumentException ex) {
                ex.printStackTrace();
                showErrorMessage("Fehler beim Erstellen des Spiels: " + ex.getMessage(), "Interner Fehler");
            }
        }
    }
}
