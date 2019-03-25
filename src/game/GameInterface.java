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

    /**
     * add the troop count panel in the sidebar
     * @param troopCountPanel new troop count panel
     */
    void addTroopCountPanel(ATroopCountPanel troopCountPanel);
    /**
     * replaces the troop count panel in the sidebar
     * @param troopCountPanel new troop count panel
     */
    void replaceTroopCountPanel(ATroopCountPanel troopCountPanel);
    /**
     * removes the troop count panel from the sidebar
     */
    void removeTroopCountPanel();

    /**
     * @return the Game window (Here it is always {@link gui.GameWindow})
     */
    Window getGameWindow();

    /**
     * updates the joker button and adds/removes it from the sidepanel
     * @param visible if the joker button should be visible
     * @param nextJoker the next joker to display
     */
    void onUpdateJokerButton(boolean visible, AJoker nextJoker);
}
