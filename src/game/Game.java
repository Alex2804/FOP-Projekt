package game;

import java.util.*;

import game.goals.ACaptureTheFlagGoal;
import game.goals.AClashOfArmiesGoal;
import game.map.Castle;
import game.map.Kingdom;
import game.map.GameMap;
import game.map.MapSize;
import gui.AttackThread;
import gui.Resources;

public class Game {

    protected Goal goal;
    protected List<Player> players;
    protected boolean isOver;
    protected boolean hasStarted;
    protected int round;
    protected MapSize mapSize;
    protected GameMap gameMap;
    protected Queue<Player> playerQueue;
    protected Player startingPlayer;
    protected Player currentPlayer;
    protected GameInterface gameInterface;
    protected AttackThread attackThread;

    public static final boolean training = false;

    public Game() {
        this.isOver = false;
        this.hasStarted = false;
        this.mapSize = MapSize.MEDIUM;
        this.players = new LinkedList<>();
    }

    public void addPlayer(Player p) {
        if(players.contains(p))
            throw new IllegalArgumentException("Spieler wurde bereits hinzugefügt");

        this.players.add(p);
    }

    public void setGoal(Goal goal) {
        this.goal = goal;
        this.goal.setGame(this);
    }
    public Goal getGoal(){
        return goal;
    }

    public int getRound() {
        return round;
    }

    public void setMapSize(MapSize mapSize) {
        this.mapSize = mapSize;
    }
    public MapSize getMapSize(){
        return mapSize;
    }

    /**
     * @return an independent copy of this game object
     */
    public Game copy(){
        Game copy = new Game();
        for(Player player : players){
            copy.addPlayer(Player.createPlayer(player.getClass(), player.getName(), player.getColor()));
        }
        copy.setMapSize(getMapSize());
        try {
            Goal goal = this.goal.getClass().getConstructor().newInstance();
            copy.setGoal(goal);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return copy;
    }

    private void generateMap() {

        int mapSizeMultiplier = this.mapSize.ordinal() + 1;
        int playerCount = players.size();
        int numRegions = playerCount * GameConstants.CASTLES_NUMBER_MULTIPLIER * mapSizeMultiplier;
        double tileMultiplier = 1.0 + (mapSizeMultiplier * 0.3);

        // We set up space for 2 times the region count
        int numTiles = (int) Math.ceil(numRegions * tileMultiplier);

        // Our map should be 3:2
        int width = (int) Math.ceil(0.6 * numTiles);
        int height = (int) Math.ceil(0.4 * numTiles);

        int continents = Math.max(3, playerCount + this.mapSize.ordinal());

        this.gameMap = GameMap.generateRandomMap(this, width, height, 40, numRegions, continents);
    }

    public void start(GameInterface gameInterface) {

        if(hasStarted)
            throw new IllegalArgumentException("Spiel wurde bereits gestartet");

        if(players.size() < 2)
            throw new IllegalArgumentException("Nicht genug Spieler");

        if(goal == null)
            throw new IllegalArgumentException("Kein Spielziel gesetzt");

        this.generateMap();

        // Create random player order
        startingPlayer = null;
        this.gameInterface = gameInterface;
        List<Player> tempList = new ArrayList<>(players);
        playerQueue = new ArrayDeque<>();
        while(!tempList.isEmpty()) {
            Player player = tempList.remove((int) (Math.random() * tempList.size()));
            player.reset();
            playerQueue.add(player);
        }

        startingPlayer = playerQueue.peek();
        hasStarted = true;
        isOver = false;
        round = 0;

        gameInterface.onGameStarted(this);
        nextTurn();
    }

    public AttackThread startAttack(Castle source, Castle target, int troopCount) {
        return startAttack(source, target, troopCount, false);
    }
    public AttackThread startAttack(Castle source, Castle target, int troopCount, boolean fastForward) {
        if(attackThread != null)
            return attackThread;

        if(source.getOwner() == target.getOwner() || troopCount < 1)
            return null;

        attackThread = new AttackThread(this, source, target, troopCount, fastForward);
        attackThread.start();
        gameInterface.onAttackStarted(source, target, troopCount);
        return attackThread;
    }

    public void doAttack(Castle attackerCastle, Castle defenderCastle, int[] rollAttacker, int[] rollDefender) {

        Integer[] rollAttackerSorted = Arrays.stream(rollAttacker).boxed().sorted(Comparator.reverseOrder()).toArray(Integer[]::new);
        Integer[] rollDefenderSorted = Arrays.stream(rollDefender).boxed().sorted(Comparator.reverseOrder()).toArray(Integer[]::new);

        Player attacker = attackerCastle.getOwner();
        Player defender = defenderCastle.getOwner();

        for(int i = 0; i < Math.min(rollAttacker.length, rollDefender.length); i++) {
            if(rollAttackerSorted[i] > rollDefenderSorted[i]) {
                defenderCastle.removeTroops(1);
                if(defenderCastle.getTroopCount() == 0) {
                    attackerCastle.removeTroops(1);
                    defenderCastle.setOwner(attacker);
                    defenderCastle.addTroops(1);
                    gameInterface.onConquer(defenderCastle, attacker);
                    if(isCaptureTheFlagGoal())
                        captureTheFlagGoal().conquered(defenderCastle, attacker);
                    addScore(attacker, 50);
                    break;
                } else {
                    addScore(attacker, 20);
                }
            } else {
                attackerCastle.removeTroops(1);
                addScore(defender, 30);
            }
        }

        gameInterface.onUpdate();
    }

    public void moveTroops(Castle source, Castle destination, int troopCount) {
        if(troopCount >= source.getTroopCount() || (destination.getOwner() != null && source.getOwner() != destination.getOwner()))
            return;

        if(destination.getOwner() == null){
            source.getOwner().addTroops(1);
            source.removeTroops(1);
            chooseCastle(destination, source.getOwner(), false);
            --troopCount;
        }
        source.moveTroops(destination, troopCount);
        gameInterface.onUpdate();
    }

    public void stopAttack() {
        this.attackThread = null;
        this.gameInterface.onAttackStopped();
    }

    public int[] roll(Player player, int dices, boolean fastForward) {
        return gameInterface.onRoll(player, dices, fastForward);
    }

    public boolean allCastlesChosen() {
        return gameMap.getCastles().stream().noneMatch(c -> c.getOwner() == null);
    }

    public AttackThread getAttackThread() {
        return this.attackThread;
    }

    public void chooseCastle(Castle castle, Player player) {
        chooseCastle(castle, player, true);
    }
    private void chooseCastle(Castle castle, Player player, boolean doNextTurn){
        if(castle.getOwner() != null || player.getRemainingTroops() == 0)
            return;

        gameInterface.onCastleChosen(castle, player);
        player.removeTroops(1);
        castle.setOwner(currentPlayer);
        castle.addTroops(1);
        addScore(player, 5);

        if(doNextTurn && (player.getRemainingTroops() == 0 || allCastlesChosen())) {
            player.removeTroops(player.getRemainingTroops());
            nextTurn();
        }
    }

    public void addTroops(Player player, Castle castle, int count) {
        if(count < 1 || castle.getOwner() != player)
            return;

        count = Math.min(count, player.getRemainingTroops());
        castle.addTroops(count);
        player.removeTroops(count);
    }

    public void addScore(Player player, int score) {
        player.addPoints(score);
        gameInterface.onAddScore(player, score);
    }

    public void endGame() {
        isOver = true;
        Player winner = goal.getWinner();

        if(winner != null)
            addScore(goal.getWinner(), 150);

        Resources resources = Resources.getInstance();
        if(!training) {
            for (Player player : players) {
                resources.addScoreEntry(new ScoreEntry(player, goal));
            }
        }

        gameInterface.onGameOver(winner);
    }

    public void nextTurn() {

        if(goal.isCompleted()) {
            endGame();
            return;
        }

        // Choose next player
        Player nextPlayer;
        do {
            nextPlayer = playerQueue.remove();

            // if player has already lost, remove him from queue
            if(goal.hasLost(nextPlayer)) {
                if(startingPlayer == nextPlayer) {
                    startingPlayer = playerQueue.peek();
                }
                nextPlayer = null;
            }
        } while(nextPlayer == null && !playerQueue.isEmpty());

        if(nextPlayer == null) {
            isOver = true;
            gameInterface.onGameOver(goal.getWinner());
            return;
        }

        currentPlayer = nextPlayer;
        if(round == 0 || (round == 1 && allCastlesChosen() && allFlagsChosen()) || (round > 1 && currentPlayer == startingPlayer)) {
            round++;
            gameInterface.onNewRound(round);
        }

        int numRegions = currentPlayer.getNumRegions(this);

        int addTroops;
        if(round == 1)
            if(isCaptureTheFlagGoal() && allCastlesChosen()){
                addTroops = 0;
            } else {
                addTroops = GameConstants.CASTLES_AT_BEGINNING;
            }
        else {
            addTroops = Math.max(3, numRegions / GameConstants.TROOPS_PER_ROUND_DIVISOR);
            addScore(currentPlayer, addTroops * 5);

            for(Kingdom kingdom : gameMap.getKingdoms()) {
                if(kingdom.getOwner() == currentPlayer) {
                    addScore(currentPlayer, 10);
                    addTroops++;
                }
            }
        }

        currentPlayer.addTroops(addTroops);
        boolean isAI = (currentPlayer instanceof AI);
        gameInterface.onNextTurn(currentPlayer, addTroops, !isAI);
        if(isAI) {
            ((AI)currentPlayer).doNextTurn(this);
        }

        playerQueue.add(currentPlayer);
    }

    /**
     * @return if all flags are chosen and always true if the goal is not an instance of ACaptureTheFlagGoal
     */
    public boolean allFlagsChosen(){
        if(isCaptureTheFlagGoal() && round == 1){
            return captureTheFlagGoal().getFlagsChoosen().size() == players.size();
        }else {
            return true;
        }
    }
    /**
     * @return A ACaptureTheFlagGoal object if the goal is an instance of this or null
     */
    public ACaptureTheFlagGoal captureTheFlagGoal(){
        if(isCaptureTheFlagGoal())
            return (ACaptureTheFlagGoal)goal;
        else
            return null;
    }
    /**
     * @return if the goal is an instance of ACaptureTheFlagGoal
     */
    public boolean isCaptureTheFlagGoal(){
        return goal instanceof ACaptureTheFlagGoal;
    }

    /**
     * @return A AClashOfArmiesGoal object if the goal is an instance of this or null
     */
    public AClashOfArmiesGoal clashOfArmiesGoal(){
        if(isClashOfArmiesGoal())
            return (AClashOfArmiesGoal) goal;
        else
            return null;
    }
    /**
     * @return if the goal is an instance of AClashOfArmiesGoal
     */
    public boolean isClashOfArmiesGoal(){
        return goal instanceof AClashOfArmiesGoal;
    }

    public Player getCurrentPlayer() {
        return this.currentPlayer;
    }

    public List<Player> getPlayers() {
        return this.players;
    }

    public GameMap getMap() {
        return this.gameMap;
    }

    public boolean isOver() {
        return this.isOver;
    }

    public GameInterface getGameInterface(){
        return gameInterface;
    }
}
