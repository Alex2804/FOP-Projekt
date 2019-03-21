package gui.views;

import de.teast.aai.AAIConstantsWrapper;
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
import java.util.Arrays;

public class GameViewTraining extends GameView {
    private DicePanel dices;
    private Game game;

    public static boolean update = false;

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
    private static final int roundsPerConstants = 1000;
    private AAIConstantsWrapper currentConstants = null;
    @Override
    public void onGameOver(Player winner) {
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
        System.out.println("Played Games: " + ++gameCount);
        System.out.println("Max-Wins: " + maxWins + "   ;   Current-Wins: " + currentWins);
        if(gameCount % roundsPerConstants == 0 || currentConstants == null){
            if(maxWins < currentWins && currentConstants != null){
                maxWins = currentWins;
                System.out.println(Arrays.toString(currentConstants.save()));
                currentWins = 0;
                try {
                    currentConstants.write("best.txt");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(currentConstants == null){
                currentConstants = new AAIConstantsWrapper("best.txt");
            }else{
                currentConstants = new AAIConstantsWrapper(true);
            }
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