package gui.views;

import de.teast.aai.AAIConstants;
import game.Game;
import game.Player;
import game.map.Castle;
import game.players.ABasicAI;
import gui.GameWindow;
import gui.components.DicePanel;

import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

public class GameViewTraining extends GameView {
    public static class ATrainingThread extends Thread{
        public GameViewTraining view;
        public GameWindow gameWindow;
        public Game game;
        public int threadID;

        public ATrainingThread(GameWindow gameWindow, Game game, int threadID){
            this.gameWindow = gameWindow;
            this.game = game;
            this.threadID = threadID;
        }

        public GameViewTraining createView(){
            return new GameViewTraining(gameWindow, game);
        }

        @Override
        public void run() {
            if(view == null)
                view = createView();
            view.threadID = threadID;
            game.start(view);
        }
    }

    private DicePanel dices;
    private Game game;

    public int threadID = 0;

    GameViewTraining(GameWindow gameWindow, Game game) {
        super(gameWindow, game);
        this.game = game;
    }

    private int sidebarWidth() {
        return (int) Math.max(getWidth() * 0.15, 300);
    }

    private Dimension mapPanelSize() {
        int w = getWidth();
        int h = getHeight();
        return new Dimension(w - sidebarWidth() - 40, h - 20);
    }

    @Override
    public void onResize() {

    }

    @Override
    protected void onInit() {
        this.dices = new DicePanel(getWindow().getResources());
        this.dices.setBorder(new LineBorder(Color.BLACK));
        this.add(dices);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {

    }

    @Override
    public void onCastleChosen(Castle castle, Player player) {

    }

    @Override
    public void onNextTurn(Player currentPlayer, int troopsGot, boolean human) {

    }

    @Override
    public void onNewRound(int round) {

    }

    private static final boolean autoRestart = true;
    private int gameCount = 0;
    private int maxWins = 0;
    private int currentWins = 0;
    private static final int roundsPerConstants = 100;
    private AAIConstants currentConstants = null;
    private long start = 0;
    @Override
    public void onGameOver(Player winner) {
        ++gameCount;
        if(winner != null && winner.getClass() == ABasicAI.class)
            ++currentWins;
        if(autoRestart){
            Game newGame = new Game();
            Player newPlayer;
            for(Player player : game.getPlayers()){
                newPlayer = Player.createPlayer(player.getClass(), player.getName(), player.getColor());
                newGame.addPlayer(newPlayer);
            }
            newGame.setMapSize(game.getMapSize());
            newGame.setGoal(game.getGoal());
            newGame.start(this);
        }
    }

    @Override
    public void onGameStarted(Game game) {
        if(gameCount % roundsPerConstants == 0 || currentConstants == null){
            System.out.println("" +
                    "=================================================================\n" +
                    "Thread: " + threadID + "\n" +
                    "Played Games: " + gameCount + "\n" +
                    "Max-Wins: " + maxWins + "   ;   Current-Wins: " + currentWins + "\n" +
                    "Time Needed: " + (System.currentTimeMillis() - start) / 1000.0 + " seconds\n" +
                    "=================================================================");
            start = System.currentTimeMillis();
            if(maxWins < currentWins && currentConstants != null){
                maxWins = currentWins;
                try {
                    currentConstants.write("best"+threadID+".txt");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(currentConstants == null){
                currentConstants = new AAIConstants("best"+threadID+".txt");
            }else{
                currentConstants = new AAIConstants(true);
            }
            currentWins = 0;
        }
        for (Player player : game.getPlayers()){
            if(player.getClass() == ABasicAI.class){
                ((ABasicAI)player).constants = currentConstants;
            }
        }
    }

    @Override
    public void onConquer(Castle castle, Player player) {

    }

    @Override
    public void onUpdate() {

    }

    @Override
    public void onAddScore(Player player, int score) {

    }

    @Override
    public int[] onRoll(Player player, int dices, boolean fastForward) {
        try {
            return this.dices.generateRandom(dices, false);
        } catch(InterruptedException ex) {
            ex.printStackTrace();
            return new int[0];
        }
    }

    @Override
    public void onAttackStarted(Castle source, Castle target, int troopCount) {

    }

    @Override
    public void onAttackStopped() {

    }
}