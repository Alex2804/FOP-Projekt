package gui;

import game.Game;
import game.Player;
import game.map.Castle;

import java.util.LinkedList;
import java.util.List;

public class AttackThread extends Thread {

    private Castle attackerCastle, defenderCastle;
    private Player attacker, defender;
    private int troopAttackCount;
    private Game game;
    private boolean fastForward;
    private Player winner;
    private List<Player> doubleDices;

    public AttackThread(Game game, Castle attackerCastle, Castle defenderCastle, int troopAttackCount, boolean fastForward) {
        this.attackerCastle = attackerCastle;
        this.defenderCastle = defenderCastle;
        this.attacker = attackerCastle.getOwner();
        this.defender = defenderCastle.getOwner();
        this.winner = defender;
        this.troopAttackCount = troopAttackCount;
        this.game = game;
        this.fastForward = fastForward;
        this.doubleDices = new LinkedList<>();
    }
    public AttackThread(Game game, List<Player> doubleDices, Castle attackerCastle, Castle defenderCastle, int troopAttackCount, boolean fastForward) {
        this.attackerCastle = attackerCastle;
        this.defenderCastle = defenderCastle;
        this.attacker = attackerCastle.getOwner();
        this.defender = defenderCastle.getOwner();
        this.winner = defender;
        this.troopAttackCount = troopAttackCount;
        this.game = game;
        this.fastForward = fastForward;
        this.doubleDices = new LinkedList<>(doubleDices);
    }

    public void fastForward() {
        fastForward = true;
    }

    private void sleep(int ms) throws InterruptedException {
        long end = System.currentTimeMillis() + ms;
        while(System.currentTimeMillis() < end && !fastForward) {
            Thread.sleep(10);
        }
    }

    @Override
    public void run() {

        int attackUntil = Math.max(1, attackerCastle.getTroopCount() - troopAttackCount);
        Player useDoubleDices = null;

        try {
            sleep(1500);

            while(attackerCastle.getTroopCount() > attackUntil) {

                // Attacker dices: at maximum 3 and not more than actual troop count
                int attackerCount =  Math.min(troopAttackCount, Math.min(attackerCastle.getTroopCount() - 1, 3));
                if(doubleDices.contains(attacker))
                    attackerCount *= 2;
                int[] attackerDice = game.roll(attacker, attackerCount, fastForward);

                sleep(1500);

                // Defender dices: at maximum 2
                int defenderCount = Math.min(2, defenderCastle.getTroopCount());
                int[] defenderDice = game.roll(defender, defenderCount, fastForward);

                game.doAttack(attackerCastle, defenderCastle, attackerDice, defenderDice);
                if(defenderCastle.getOwner() == attacker || (game.isFlagEmpireGoal() && game.flagEmpireGoal().isFlagSet(defenderCastle) && defenderCastle.getTroopCount() <= 0)) {
                    winner = attacker;
                    break;
                }

                sleep(1500);
            }
        } catch(InterruptedException ex) {
            ex.printStackTrace();
            useDoubleDices = null;
        }

        game.stopAttack(useDoubleDices);
    }

    public Player getWinner() {
        return winner;
    }
}
