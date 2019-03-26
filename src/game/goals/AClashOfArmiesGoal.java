package game.goals;

import base.Edge;
import de.teast.AClustering;
import de.teast.AConstants;
import de.teast.APath;
import de.teast.ARangePathFinding;
import de.teast.aai.AAIMethods;
import de.teast.aextensions.ajoker.AJoker;
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
import game.map.MapSize;
import game.players.Human;
import javafx.util.Pair;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Alexander Muth
 */
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

    @Override
    public MapSize[] getSupportedMapSizes() {
        return new MapSize[]{MapSize.MEDIUM, MapSize.LARGE};
    }
    @Override
    public AJoker[] getSupportedJokers() {
        return AConstants.CLASH_OF_ARMIES_JOKERS;
    }
    @Override
    public Class<?>[] getSupportedPlayerTypes() {
        return new Class[]{Human.class};
    }
    @Override
    public int getMaxPlayerCount() {
        return 2;
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
            doBattle(attacker, destination);
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
        if(troops.troopCount() <= 0)
            return;
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
     * removes all troops from a castle (updates ownership)
     * @param castle the castle to remove the troops from
     */
    public void removeTroops(Castle castle){
        castleTroops.remove(castle);
        if(!isBase(castle))
            castle.setOwner(null);
    }
    /**
     * remove troops from a castle (updates ownership)
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
                next.removeTroops(troops.troopCount());
            }
            if(next.troopCount() <= 0){
                iterator.remove();
            }
        }
        if(castleTroops.isEmpty() && !isBase(castle)){
            castle.setOwner(null);
        }
    }
    /**
     * remove troops from a castle (updates ownership)
     * @param castle the castle to remove the troops from
     * @param troops the troops to remove
     */
    public void removeTroops(Castle castle, List<ATroops> troops){
        for(ATroops t : troops){
            removeTroops(castle, t);
        }
    }
    /**
     * Removes the old troops, gives the ownership to the {@code player} and adds {@code troops}
     * @param player the new owner or null to keep old
     * @param castle the castle to update
     * @param troops the new troops
     */
    public void updateTroops(Player player, Castle castle, List<ATroops> troops){
        Player oldOwner = castle.getOwner();
        removeTroops(castle);
        if(player != null && !troops.isEmpty()){
            addTroops(castle, troops);
            castle.setOwner(player);
        }else if(oldOwner != null && !troops.isEmpty()){
            addTroops(castle, troops);
            castle.setOwner(oldOwner);
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
     * Runs battles, the {@code attacker} attribute has the Castles which battles as keys and the troops which battles
     * as value. This method modifies the troops and ownerships
     * @param attacker the attacker
     * @param defender the defender
     * @return a {@link ATriplet}, containing the winner of the battle as first, the defender castle as second and the
     * remaining troops of the winner as third value
     */
    public ATriplet<Player, Castle, List<ATroops>> doBattle(Pair<Castle, List<ATroops>> attacker, Castle defender){
        Player attackerPlayer = attacker.getKey().getOwner();
        Player defenderPlayer = defender.getOwner();
        removeTroops(attacker.getKey(), attacker.getValue());
        doLongRangeAttack(attacker.getKey(), defender); // First let long range troops from attacker attack defender

        List<ATroops> defenderTroops = getTroops(defender);
        List<ATroops> attackerTroops = attacker.getValue().stream().map(ATroops::clone).collect(Collectors.toList());
        Pair<Integer, List<ATroops>> attackResult;
        while(!defenderTroops.isEmpty() && !attackerTroops.isEmpty()){
            attackResult = doAttack(attackerTroops, defenderTroops,
                            (ATroops t) -> t.troop().attackShortRange, (ATroops t) -> t.troop().defenseShortRange);
            defenderTroops = attackResult.getValue();
            madeDamage(attackerPlayer, attackResult.getKey());


            attackResult = doAttack(defenderTroops, attackerTroops,
                            (ATroops t) -> t.troop().attackShortRange, (ATroops t) -> t.troop().defenseShortRange);
            attackerTroops = attackResult.getValue();
            madeDamage(defenderPlayer, attackResult.getKey());
        }

        ATriplet<Player, Castle, List<ATroops>> returnTriplet;
        if(defenderTroops.isEmpty()){
            returnTriplet = new ATriplet<>(attackerPlayer, defender, attackerTroops);
        }else{
            returnTriplet = new ATriplet<>(defenderPlayer, defender, defenderTroops);
        }
        updateTroops(returnTriplet.getFirst(), returnTriplet.getSecond(), returnTriplet.getThird());
        return returnTriplet;
    }
    /**
     * This method runs one round of an battle (one attack)
     * @param attackerTroops the attacker troops
     * @param defenderTroops the defender troops
     * @param attackerAttackValue function to get the attack value
     * @param defenderDefendValue function to get the defend value
     * @return a {@link Pair} with the damage which was made as key and the remaining defender troops as value
     */
    public Pair<Integer, List<ATroops>> doAttack(List<ATroops> attackerTroops, List<ATroops> defenderTroops,
                                                 Function<ATroops, Integer> attackerAttackValue,
                                                 Function<ATroops, Integer> defenderDefendValue){
        if(attackerTroops.isEmpty())
            return new Pair<>(0, new LinkedList<>(defenderTroops));
        defenderTroops = defenderTroops.stream(). map(ATroops::clone).collect(Collectors.toList());
        defenderTroops.sort(Comparator.comparingInt((ATroops t) -> t.troop().life));
        defenderTroops.sort(Comparator.comparingInt((ATroops t) -> t.troop().attackShortRange));
        defenderTroops.sort(Comparator.comparingInt(defenderDefendValue::apply));
        if(defenderTroops.isEmpty())
            return new Pair<>(0, new LinkedList<>());

        attackerTroops.sort(Comparator.comparingInt(attackerAttackValue::apply));

        List<ATriplet<ATroops, Integer, String>> attackerTroopsCount = attackerTroops.stream(). //Triplet: troops, remaining troop count for attack, NOTHING
                map(t -> new ATriplet<>(t, t.troopCount(), "")).
                collect(Collectors.toList());

        List<ATroops> newDefenderTroops = new LinkedList<>();

        ListIterator<ATroops> defenderIterator;
        ListIterator<ATriplet<ATroops, Integer, String>> attackerIterator;
        ATriplet<ATroops, Integer, String> triplet;
        ATroops defender, newDefender;
        int troopCount, damageSum = 0, damage;
        int neededDamage, neededCount;
        defenderIterator = defenderTroops.listIterator();
        while(defenderIterator.hasNext() && !attackerTroopsCount.isEmpty()){
            defender = defenderIterator.next();
            neededDamage = defenderDefendValue.apply(defender) + defender.troop().life;

            for(int i=0; i<defender.troopCount(); i++){
                damage = 0;
                attackerIterator = attackerTroopsCount.listIterator();
                while(attackerIterator.hasNext()){
                    triplet = attackerIterator.next();
                    if(attackerAttackValue.apply(triplet.getFirst()) <= defenderDefendValue.apply(defender))
                        continue;
                    troopCount = (int)Math.ceil(((double)neededDamage) / attackerAttackValue.apply(triplet.getFirst()));

                    if(triplet.getSecond() <= troopCount){
                        damage += attackerAttackValue.apply(triplet.getFirst()) * triplet.getSecond();
                        attackerIterator.remove();
                    }else{
                        neededCount = (int)Math.ceil((double)neededDamage / attackerAttackValue.apply(triplet.getFirst()));
                        damage = neededDamage;
                        triplet.setSecond(triplet.getSecond() - neededCount);
                    }

                    if(damage >= neededDamage)
                        break;
                }
                damage -= defenderDefendValue.apply(defender);
                damage = Math.max(0, damage);
                damageSum += damage;
                defender.removeTroops(1);
                if(defender.troopCount() <= 0)
                    defenderIterator.remove();
                newDefender = new ATroops(defender.troop().clone(), 1);
                newDefender.troop().life -= damage;
                if(newDefender.troop().life > 0){
                    newDefenderTroops.add(newDefender);
                }
            }
        }

        Map<ATroop, ATroops> returnMap = new HashMap<>();
        ATroops defenderTroopsTemp;
        defenderTroops.addAll(newDefenderTroops);
        for(ATroops troops : defenderTroops){
            defenderTroopsTemp = returnMap.get(troops.troop());
            if(defenderTroopsTemp == null){
                returnMap.put(troops.troop(), troops);
            }else{
                defenderTroopsTemp.addTroop(troops.troopCount());
            }
        }
        return new Pair<>(damageSum, new LinkedList<>(returnMap.values()));
    }

    /**
     * Let the long range troops do their attacks for all possibilities
     * @param player the player to do all attacks for
     */
    public void doLongRangeAttacks(Player player){
        List<ATroops> troops;
        int biggestRange = 0;
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
                doLongRangeAttack(attackerCastle, targetCastle);
            }
        }
    }
    /**
     * This method do a long range attack with all troops from the attacker castle which has enough range to reach
     * the defender castle
     * @param attacker the attacker castle
     * @param target the target castle
     */
    private void doLongRangeAttack(Castle attacker, Castle target){
        int range = ARangePathFinding.getRange(getPath(), attacker, target);
        List<ATroops> attackerTroops = getTroops(attacker).stream().filter((ATroops t) -> t.troop().longRangeRange >= range).collect(Collectors.toList());

        Pair<Integer, List<ATroops>> result = doAttack(attackerTroops, getTroops(target),
                                                (ATroops t) -> t.troop().attackLongRange,
                                                (ATroops t) -> t.troop().defenseLongRange);
        madeDamage(attacker.getOwner(), result.getKey());
        updateTroops(null, target, result.getValue());
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
