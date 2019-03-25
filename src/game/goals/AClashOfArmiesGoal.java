package game.goals;

import base.Edge;
import de.teast.AClustering;
import de.teast.AConstants;
import de.teast.APath;
import de.teast.ARangePathFinding;
import de.teast.aai.AAIMethods;
import de.teast.agui.ATroopBuyPanel;
import de.teast.agui.ATroopCountPanel;
import de.teast.agui.ATroopMovePanel;
import de.teast.atroops.ATroop;
import de.teast.atroops.ATroops;
import de.teast.autils.ATriplet;
import game.Goal;
import game.Player;
import game.map.Castle;
import game.map.Kingdom;
import javafx.util.Pair;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class AClashOfArmiesGoal extends Goal {
    public ATroopCountPanel troopCountPanel = null;
    private Map<Castle, List<ATroops>> castleTroops = new HashMap<>();
    public static ATroop[] availableTroops = AConstants.TROOPS;
    ATroopBuyPanel.ATroopBuyDialog buyDialog = null;
    ATroopMovePanel.ATroopMoveDialog moveDialog = null;

    public AClashOfArmiesGoal(){
        super("Clash of Armies", "Der Spieler, welcher bis zuletzt seine Burg halten kann gewinnt");
    }

    @Override
    public boolean isCompleted() {
        return this.getWinner() != null;
    }
    @Override
    public boolean isCompleted(List<Castle> castles) {
        return this.getWinner(castles) != null;
    }

    @Override
    public Player getWinner() {
        return getWinner(getGame().getMap().getCastles());
    }
    @Override
    public Player getWinner(List<Castle> castles) {
        Player player = null;
        for(Castle castle : castles){
            if(castle.getOwner() != null && player == null){
                player = castle.getOwner();
            }else if(castle.getOwner() != null && player != castle.getOwner()){
                return null;
            }
        }
        return player;
    }

    @Override
    public boolean hasLost(Player player) {
        return player.getNumRegions(getGame()) == 0;
    }

    @Override
    public boolean hasLost(Player player, List<Castle> castles, int round) {
        return player.getNumRegions(castles) == 0;
    }

    /**
     * @return the graph as {@link APath}
     */
    public APath getPath(){
        return (APath)getGame().getMap().getGraph();
    }
    /**
     * Generates a path between source and destination
     * @param source the source castle
     * @param destination the destination castle
     */
    public void generatePath(Castle source, Castle destination){
        int stopCount = ThreadLocalRandom.current().nextInt((int)(AConstants.MIN_STOP_COUNT * (AConstants.STOP_COUNT_PLAYER_MULTIPLIER * getGame().getPlayers().size())),
                                                            (int)((AConstants.MAX_STOP_COUNT + 1) * (AConstants.STOP_COUNT_PLAYER_MULTIPLIER * getGame().getPlayers().size())));
        getPath().generateStops(source, destination, stopCount,getGame().getMap().getScale());
    }
    /**
     * generate all paths between the castles
     */
    public void generatePaths(){
        List<Pair<Castle, Castle>> pairs = new LinkedList<>();
        int i=0;
        List<Castle> bases = getBases();
        for(Castle castle : bases){
            for(ListIterator<Castle> iterator = bases.listIterator(++i); iterator.hasNext();){
                pairs.add(new Pair<>(castle, iterator.next()));
            }
        }

        int pathCount;
        List<ATriplet<Castle, Castle, Integer>> pairCount = new LinkedList<>();
        for(Pair<Castle, Castle> pair : pairs){
            pathCount = ThreadLocalRandom.current().nextInt(AConstants.MIN_PATH_COUNT_PER_CASTLES,
                                                            AConstants.MAX_PATH_COUNT_PER_CASTLES + 1);
            pairCount.add(new ATriplet<>(pair.getKey(), pair.getValue(), pathCount));
        }

        while(!pairCount.isEmpty()){
            ATriplet<Castle, Castle, Integer> next;
            for(ListIterator<ATriplet<Castle, Castle, Integer>> iterator = pairCount.listIterator(); iterator.hasNext();){
                next = iterator.next();
                if(next.getThird() <= 0){
                    iterator.remove();
                    continue;
                }
                generatePath(next.getFirst(), next.getSecond());
                next.setThird(next.getThird() - 1);
            }
        }
    }

    /**
     * @param castle the castle to check
     * @return if {@code castle} is a base
     */
    public boolean isBase(Castle castle){
        return getPath().getCastleMap().containsKey(castle);
    }
    /**
     * adds {@code castle} to the bases
     * @param castle the castle to add
     */
    public void addBase(Castle castle){
        getPath().addCastle(castle);
    }
    /**
     * @return all bases
     */
    public List<Castle> getBases(){
        return new LinkedList<>(getPath().getCastleMap().keySet());
    }
    /**
     * @param player the player to get all bases for
     * @return all bases of {@code player}
     */
    public List<Castle> getBases(Player player){
        List<Castle> returnList = new LinkedList<>();
        for(Castle base : getBases()){
            if(base.getOwner() == player){
                returnList.add(base);
            }
        }
        return returnList;
    }
    /**
     * generates the bases in the Graph (APath)
     */
    public void generateBases(){
        List<Kingdom> kingdomList = new LinkedList<>();
        Kingdom kingdom;
        int i=0;
        for(Castle base : AClustering.generateBases(getGame().getPlayers(), getGame().getMap().getSize())){
            if(base.getKingdom() != null) {
                kingdom = base.getKingdom();
            }else {
                kingdom = new Kingdom((i++) % 5, base.getLocationOnMap().x, base.getLocationOnMap().y);
                base.setKingdom(kingdom);
            }
            kingdomList.add(kingdom);
            addBase(base);
        }
        getGame().getMap().setKingdoms(kingdomList);
    }
    /**
     * @param castle the castle to get the troops for
     * @return a {@link List} of {@link ATroops} positioned at the castle
     */
    public List<ATroops> getTroops(Castle castle){
        List<ATroops> returnList = castleTroops.get(castle);
        return (returnList == null) ? new LinkedList<>() : returnList;
    }
    /**
     * @param source the source castle
     * @param destination the destination castle
     * @param path the path of edges
     * @return a list of edges is the move is valid or null if not
     */
    public List<Edge<Castle>> tryMove(Castle source, Castle destination, List<Edge<Castle>> path){
        List<ATroops> movable = getMovableTroops(source, destination, path);
        if(movable != null && !movable.isEmpty())
            return path;
        return null;
    }

    /**
     * @param source the source castle
     * @param destination the destination castle
     * @param path the path of edges
     * @return a list of troops, which have enough speed to reach the destination with one move
     */
    private List<ATroops> getMovableTroops(Castle source, Castle destination, List<Edge<Castle>> path){
        List<ATroops> returnList = new LinkedList<>();
        if(path == null || path.isEmpty())
            return returnList;

        Castle castleA, castleB;
        for(Edge<Castle> edge : path){
            castleA = edge.getNodeA().getValue();
            castleB = edge.getNodeB().getValue();
            if(castleA != source && castleA != destination && castleA.getOwner() != source.getOwner() && castleA.getOwner() != null){
                return null;
            }else if((castleB != source && castleB != destination && castleB.getOwner() != source.getOwner() && castleB.getOwner() != null)){
                return null;
            }
        }

        int requiredSpeed = path.size();
        for(ATroops troops : getTroops(source)){
            if(troops.troopCount() > 0 && troops.troop().speed >= requiredSpeed){
                returnList.add(troops);
            }
        }
        return returnList;
    }
    /**
     * Opens an {@link de.teast.agui.ATroopMovePanel.ATroopMoveDialog} to choose the troops to move
     * @param source the source castle
     * @param destination the destination castle
     * @param path the path of edges
     */
    public void move(Castle source, Castle destination, List<Edge<Castle>> path){
        if(moveDialog != null && buyDialog.isDisplayable())
            moveDialog.dispose();
        moveDialog = ATroopMovePanel.getTroopMoveDialog(getMovableTroops(source, destination, path), getGame().getGameInterface().getGameWindow());
        moveDialog.addButtonListener(new AMoveListener(moveDialog, source, destination));
        if(destination.getOwner() != null && destination.getOwner() != source.getOwner())
            moveDialog.setTitle("Truppen für Angriff wählen");
        moveDialog.setVisible(true);
    }
    /**
     * Moves troops and attacks if necessary
     * @param source the source castle
     * @param destination the destination castle
     * @param troops the troops to move
     */
    private void doMoves(Castle source, Castle destination, List<ATroops> troops){
        if(destination.getOwner() != null && destination.getOwner() != source.getOwner()){ // Attack
            Pair<Castle, List<ATroops>> attacker = new Pair<>(source, troops);
            Pair<Castle, List<ATroops>> defender = new Pair<>(destination, getTroops(destination));
            ATriplet<Player, Castle, List<ATroops>> winner = battle(attacker, defender);
            if(winner.getFirst() == source.getOwner()){ // attacker wins
                removeTroops(source, troops);
                removeTroops(destination);
                addTroops(destination, winner.getThird());
            }else{ // defender wins
                removeTroops(source);
                removeTroops(destination, troops);
                addTroops(destination, winner.getThird());
            }
            destination.setOwner(winner.getFirst());
        }else{ // Move
            destination.setOwner(source.getOwner());
            removeTroops(source, troops);
            addTroops(destination, troops);
        }
        getGame().nextTurn();
        getGame().getGameInterface().onUpdate();
        updateSelectedCastle();
    }

    /**
     * Opens an {@link de.teast.agui.ATroopBuyPanel.ATroopBuyDialog}
     * @param base the base to buy troops for
     */
    public void addTroops(Castle base){
        if(buyDialog != null && buyDialog.isDisplayable())
            buyDialog.dispose();
        buyDialog = ATroopBuyPanel.getTroopBuyDialog(AConstants.TROOPS, base.getOwner().getPoints(), getGame().getGameInterface().getGameWindow());
        buyDialog.addButtonListener(new ABuyListener(buyDialog, base));
        buyDialog.setVisible(true);
    }
    /**
     * add troops to a castle
     * @param castle the castle to add troops
     * @param troops the troops to add
     */
    public void addTroops(Castle castle, ATroops troops){
        List<ATroops> castleTroops = getTroops(castle);
        if(castleTroops.isEmpty())
            this.castleTroops.put(castle, new LinkedList<>(Collections.singletonList(troops)));
        for(ATroops t : castleTroops){
            if(t.troop().equals(troops.troop())){
                t.addTroop(troops.troopCount());
                return;
            }
        }
        castleTroops.add(troops);
    }
    /**
     * add troops to a castle
     * @param castle the castle to add troops
     * @param troops the troops to add
     */
    public void addTroops(Castle castle, List<ATroops> troops){
        for(ATroops t : troops){
            addTroops(castle, t);
        }
    }
    /**
     * remove all troops from a castle
     * @param castle the castle to remove the troops from
     */
    public void removeTroops(Castle castle){
        castleTroops.remove(castle);
    }
    /**
     * remove troops from a castle
     * @param castle the castle to remove the troops from
     * @param troops the troops to remove
     */
    public void removeTroops(Castle castle, ATroops troops){
        if(!castleTroops.containsKey(castle))
            return;
        List<ATroops> castleTroops = getTroops(castle);
        ListIterator<ATroops> iterator = castleTroops.listIterator();
        ATroops next;
        while(iterator.hasNext()){
            next = iterator.next();
            if(troops.troop().equals(next.troop())){
                if(next.removeTroops(troops.troopCount()) <= 0){
                    iterator.remove();
                }
            }
        }
        if(castleTroops.isEmpty() && !isBase(castle)){
            castle.setOwner(null);
            this.castleTroops.remove(castle);
        }
    }
    /**
     * remove troops from a castle
     * @param castle the castle to remove the troops from
     * @param troops the troops to remove
     */
    public void removeTroops(Castle castle, List<ATroops> troops){
        for(ATroops t : troops){
            removeTroops(castle, t);
        }
    }

    /**
     * Buys troops
     * @param base the base to place the bought troops
     * @param troops the troops to buy
     */
    private void doBuy(Castle base, List<ATroops> troops){
        if(troops == null || troops.isEmpty())
            return;
        int price = getPrice(troops);
        if(price <= base.getOwner().getPoints()  && isBase(base)){
            base.getOwner().addPoints(-price);
            StringBuilder text = new StringBuilder("%PLAYER% hat");
            StringBuilder temp = new StringBuilder();
            for(ATroops t : troops){
                text.append(temp);
                temp.delete(0, temp.length());
                temp.append(" ").append(t.troopCount()).append(" ").append(t.troop().name).append(",");
                addTroops(base, t);
            }
            if(troops.size() > 1)
                text.deleteCharAt(text.length()-1).append(" und");
            text.append(temp.deleteCharAt(temp.length()-1)).append(" auf \"")
                    .append(base.getName()).append("\" für ").append(price).append(" Punkte angeheuert!");
            getGame().getGameInterface().onLogText(text.toString(), base.getOwner());
            getGame().getGameInterface().onUpdate();
            updateSelectedCastle();
        }
    }
    /**
     * @param player the player to check
     * @return if the player has enough points to buy any available troop
     */
    public boolean hasEnoughPointsToBuy(Player player){
        for(ATroop troop : availableTroops){
            if(player.getPoints() >= troop.price){
                return true;
            }
        }
        return false;
    }
    /**
     * @param troops the troops to get the price for
     * @return the price of all the troops
     */
    public int getPrice(List<ATroops> troops){
        int sum = 0;
        for(ATroops t : troops){
            sum += t.troopCount() * t.troop().price;
        }
        return sum;
    }

    /**
     * Changes the troopCountPanel in the gui if an castle gets selected
     * @param castle the selected castle
     */
    public void castleSelected(Castle castle){
        if(castle == null) {
            getGame().getGameInterface().removeTroopCountPanel();
            troopCountPanel = null;
        } else {
            List<ATroops> troops = getTroops(castle);
            troopCountPanel = new ATroopCountPanel(getGame().getGameInterface().getGameWindow(), troops, castle);
            if(troops.isEmpty()){
                getGame().getGameInterface().removeTroopCountPanel();
            }else {
                getGame().getGameInterface().replaceTroopCountPanel(troopCountPanel);
            }
        }
    }
    /**
     * updates the displayed troops of the selected castle
     */
    public void updateSelectedCastle(){
        if(troopCountPanel != null){
            castleSelected(troopCountPanel.castle);
        }else{
            castleSelected(null);
        }
    }

    /**
     * Runs battles, the {@code attacker} and {@code defender} attributes has the Castles which battles as keys and the
     * troops which battles as values;
     * @param attacker the attacker
     * @param defender the defender
     * @return a {@link ATriplet}, containing the winner of the battle as first, the castle as second and the remaining
     * troops of the winner as third value
     */
    public ATriplet<Player, Castle, List<ATroops>> battle(Pair<Castle, List<ATroops>> attacker, Pair<Castle, List<ATroops>> defender){
        return new ATriplet<>(attacker.getKey().getOwner(), attacker.getKey(), attacker.getValue());
    }

    /**
     * Let the long range troops do their attacks for all possibilities
     * @param player the player to do all attacks for
     */
    public void doLongRangeAttacks(Player player){
        List<ATroops> troops;
        int range, biggestRange = 0;
        for(Castle attackerCastle : player.getCastles(getGame().getMap().getCastles())){
            troops = getTroops(attackerCastle);
            for(ATroops t : troops){
                if(t.troopCount() > 0){
                    biggestRange = Math.max(t.troop().longRangeRange, biggestRange);
                }
            }
            if(biggestRange <= 0)
                continue;
            for(Castle targetCastle : getEnemyCastlesInRange(attackerCastle, biggestRange)){
                range = ARangePathFinding.getRange(getPath(), attackerCastle, targetCastle);
                doLongRangeAttack(attackerCastle, targetCastle, range);
            }
        }
    }
    /**
     * This method do a long range attack
     * @param attacker the attacker castle
     * @param target the target castle
     * @param range the range from the attacker to the target castle
     */
    private void doLongRangeAttack(Castle attacker, Castle target, int range){
        List<ATriplet<ATroops, Integer, String>> attackerTroops = new LinkedList<>();
        for(ATroops t : getTroops(attacker)){
            if(t.troop().longRangeRange >= range){
                attackerTroops.add(new ATriplet<>(t, t.troopCount(), ""));
            }
        }
        attackerTroops.sort(Comparator.comparingInt((ATriplet<ATroops, Integer, String> t) -> t.getFirst().troop().attackLongRange));
        List<ATroops> defenderTroops = getTroops(target);
        defenderTroops.sort(Comparator.comparingInt((ATroops t) -> t.troop().defenseLongRange));
        defenderTroops.sort(Comparator.comparingInt((ATroops t) -> t.troop().life));

        List<ATroops> newDefenderTroops = new LinkedList<>();

        ListIterator<ATroops> defenderIterator;
        ATroops defender, newDefender;
        int troopCount;
        ATroop troop;
        int damage;
        for(ATriplet<ATroops, Integer, String> triplet : attackerTroops){
            troop = triplet.getFirst().troop();
            defenderIterator = defenderTroops.listIterator();
            while(defenderIterator.hasNext() && triplet.getSecond() > 0){
                defender = defenderIterator.next();
                if(troop.attackLongRange >= defender.troop().defenseLongRange) {
                    troopCount = (int)Math.ceil(((double)(defender.troop().defenseLongRange + defender.troop().life)) / troop.attackLongRange);
                    for(int i=0; i<defender.troopCount(); i++){
                        if(triplet.getSecond() >= troopCount){
                            triplet.setSecond(triplet.getSecond() - troopCount);
                            damage = defender.troop().life;
                            defender.removeTroops(1);
                        }else{
                            defender.removeTroops(1);
                            newDefender = defender.clone();
                            damage = (troop.attackLongRange * triplet.getSecond()) - defender.troop().defenseLongRange;
                            newDefender.troop().life -= damage;
                            newDefender.setTroopCount(1);
                            triplet.setSecond(0);
                            if(newDefender.troop().life > 0)
                                newDefenderTroops.add(newDefender);
                        }
                        madeDamage(attacker.getOwner(), damage);
                    }
                    if(defender.troopCount() <= 0)
                        defenderIterator.remove();
                }else{
                    break;
                }
            }
        }

        addTroops(target, newDefenderTroops);
        if(defenderTroops.isEmpty() && newDefenderTroops.isEmpty()){
            target.setOwner(null);
        }
    }

    /**
     * Add points if a player has made damage
     * @param player the player who has made damage
     * @param damage the damage the player has made
     */
    public void madeDamage(Player player, int damage){
        if(damage <= 0)
            return;
        player.addPoints(damage);
        getGame().getGameInterface().onUpdate();
    }

    /**
     * @param castle the castle from where te range starts
     * @param range the range enemy castles must be in
     * @return all enemy castles which are in range
     */
    public List<Castle> getEnemyCastlesInRange(Castle castle, int range){
        if(castle.getOwner() == null)
            return null;
        List<Castle> returnList = new LinkedList<>();
        for(Castle c : getCastlesInRange(castle, range)){
            if(c.getOwner() != castle.getOwner() && c.getOwner() != null){
                returnList.add(c);
            }
        }
        return returnList;
    }
    /**
     * @param castle the castle from where te range starts
     * @param range the range castles must be in
     * @return all castles which are in range
     */
    public Set<Castle> getCastlesInRange(Castle castle, int range){
        if(range <= 0)
            return new HashSet<>();
        Set<Castle> castleSet = getCastlesInRangeHelper(castle, range, new HashSet<>());
        castleSet.remove(castle);
        return castleSet;
    }
    /**
     * helper method for {@link #getCastlesInRange(Castle, int)}
     * @param currentCastle the current castle
     * @param rangeLeft the range which is left
     * @param castleSet the passed castles
     * @return the passed castles
     *
     * @see #getCastlesInRange(Castle, int)
     */
    private Set<Castle> getCastlesInRangeHelper(Castle currentCastle, int rangeLeft, Set<Castle> castleSet){
        if(rangeLeft == 0) {
            castleSet.add(currentCastle);
        }else if(rangeLeft > 0) {
            castleSet.add(currentCastle);
            for(Castle neighbour : AAIMethods.getAllNeighbours(getGame().getMap().getGraph(), currentCastle)){
                if(!castleSet.contains(neighbour)){
                    getCastlesInRangeHelper(neighbour, rangeLeft-1, castleSet);
                }
            }
        }
        return castleSet;
    }

    /**
     * Listener for {@link de.teast.agui.ATroopMovePanel.ATroopMoveDialog}
     */
    private class AMoveListener implements ActionListener {
        ATroopMovePanel.ATroopMoveDialog moveDialog;
        Castle source, destination;
        public AMoveListener(ATroopMovePanel.ATroopMoveDialog moveDialog, Castle source, Castle destination){
            this.moveDialog = moveDialog;
            this.source = source;
            this.destination = destination;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == moveDialog.cancelButton){
                moveDialog.dispose();
            }else if(e.getSource() == moveDialog.okButton){
                doMoves(source, destination, moveDialog.getResult());
                moveDialog.dispose();
            }
        }
    }
    /**
     * Listener for {@link de.teast.agui.ATroopBuyPanel.ATroopBuyDialog}
     */
    private class ABuyListener implements ActionListener {
        ATroopBuyPanel.ATroopBuyDialog buyDialog;
        Castle base;
        public ABuyListener(ATroopBuyPanel.ATroopBuyDialog buyDialog, Castle base){
            this.buyDialog = buyDialog;
            this.base = base;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == buyDialog.cancelButton){
                buyDialog.dispose();
            }else if(e.getSource() == buyDialog.okButton){
                doBuy(base, buyDialog.getResult());
                buyDialog.dispose();
            }
        }
    }
}
