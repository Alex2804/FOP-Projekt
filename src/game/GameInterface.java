package game;

import de.teast.aextensions.ajoker.AJoker;
import de.teast.agui.ATroopBuyPanel;
import de.teast.agui.ATroopCountPanel;
import de.teast.agui.ATroopMovePanel;
import de.teast.atroops.ATroop;
import de.teast.atroops.ATroops;
import game.map.Castle;

import java.awt.*;
import java.util.List;

public interface GameInterface {

    void onAttackStopped();
    void onAttackStarted(Castle source, Castle target, int troopCount);
    void onCastleChosen(Castle castle, Player player);
    void onNextTurn(Player currentPlayer, int troopsGot, boolean human);
    void onNewRound(int round);
    void onGameOver(Player winner);
    void onGameStarted(Game game);
    void onConquer(Castle castle, Player player);
    void onUpdate();
    void onAddScore(Player player, int score);
    /**
     * Log text to show it the user
     * @param text the text as String
     */
    void onLogText(String text);
    /**
     * Log text to show it the user
     * @param text the text as String
     * @param playerFormat format the players
     */
    void onLogText(String text, Player... playerFormat);
    int[] onRoll(Player player, int dices, boolean fastForward);

    ATroopMovePanel.ATroopMoveDialog getTroopMoveDialog(List<ATroops> troops);
    ATroopBuyPanel.ATroopBuyDialog getTroopBuyDialog(ATroop[] availableTroops, int availablePoints);

    void addTroopCountPanel(ATroopCountPanel troopCountPanel);
    void replaceTroopCountPanel(ATroopCountPanel troopCountPanel);
    void removeTroopCountPanel();

    Window getGameWindow();

    void onUpdateJokerButton(boolean visible, AJoker nextJoker);
}
