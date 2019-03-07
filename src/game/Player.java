package game;

import base.Graph;
import base.Node;
import game.map.Castle;

import java.awt.Color;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

public abstract class Player {

    private final String name;
    private Color color;
    private int points;
    private int remainingTroops;

    protected boolean hasJoker;

    protected Player(String name, Color color) {
        this.name = name;
        this.points = 0;
        this.color = color;
        this.remainingTroops = 0;
        hasJoker = true;
    }

    public int getRemainingTroops() {
        return this.remainingTroops;
    }

    public static Player createPlayer(Class<?> playerType, String name, Color color) {
        if(!Player.class.isAssignableFrom(playerType))
            throw new IllegalArgumentException("Not a player class");

        try {
            Constructor<?> constructor = playerType.getConstructor(String.class, Color.class);
            return (Player) constructor.newInstance(name, color);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setColor(Color c) {
        this.color = c;
    }

    public Color getColor() {
        return this.color;
    }

    public String getName() {
        return this.name;
    }

    public int getPoints() {
        return points;
    }

    public void addPoints(int points) {
        this.points += points;
    }

    public void addTroops(int troops) {
        if(troops < 0)
            return;

        this.remainingTroops += troops;
    }

    public void removeTroops(int troops) {
        if(this.remainingTroops - troops < 0 || troops < 0)
            return;

        this.remainingTroops -= troops;
    }

    /**
     * @param game game object
     * @return the count of castles which belongs to this player
     */
    public int getNumRegions(Game game) {
        return this.getCastles(game).size();
    }
    /**
     * @param castles castles to check
     * @return the count of castles which belongs to this player
     */
    public int getNumRegions(List<Castle> castles) {
        return getCastles(castles).size();
    }

    /**
     * @param game the game object
     * @return a list with all castles which belongs to this player
     */
    public List<Castle> getCastles(Game game) {
        return getCastles(game.getMap().getCastles());
        //return game.getMap().getCastles().stream().filter(c -> c.getOwner() == this).collect(Collectors.toList());
    }
    /**
     * @param castles list of castles to check
     * @return a list with all castles which belongs to this player
     */
    public List<Castle> getCastles(List<Castle> castles){
        return getCastles(castles, this);
        //return castles.stream().filter(c -> c.getOwner() == this).collect(Collectors.toList()); //streams are slower than foreach loops
    }
    /**
     * The advantage of this method over the non static ({@link Player#getCastles(List)}) is, that also the castles for null (no owner) could be found
     * @param castles list of castles to check
     * @param player player who should be owner of the returned castles (could be null)
     * @return a list with all castles which belongs to this player
     */
    public static List<Castle> getCastles(List<Castle> castles, Player player){
        List<Castle> returnList = new LinkedList<>();
        for(Castle  castle : castles){
            if(castle.getOwner() == player){
                returnList.add(castle);
            }
        }
        return returnList;
    }

    /**
     * This function wrapps {@link #getCastleNodes(Graph, Player)}
     * @param castleGraph graph containing all nodes to check
     * @return a list with all nodes, containing castles which belongs to this player
     */
    public List<Node<Castle>> getCastleNodes(Graph<Castle> castleGraph){
        return getCastleNodes(castleGraph, this);
    }
    /**
     * this function wrapps {@link #getCastleNodes(List, Player)}
     * @param nodes list of nodes to check
     * @return a list with all nodes, containing castles which belongs to this player
     */
    public List<Node<Castle>> getCastleNodes(List<Node<Castle>> nodes){
        return getCastleNodes(nodes, this);
    }
    /**
     * This function wrapps {@link #getCastleNodes(List, Player)}
     * @param castleGraph graph containing all nodes to check
     * @param player player to check (could be null)
     * @return a list with all nodes, containing castles which belongs to the player
     */
    public static List<Node<Castle>> getCastleNodes(Graph<Castle> castleGraph, Player player){
        return getCastleNodes(castleGraph.getNodes(), player);
    }
    /**
     * if the passed player is null, this method return a list with all castles with no owner (owner == null)
     * @param nodes list of nodes to check
     * @param player player to check (could be null)
     * @return a list with all nodes, containing castles which belongs to this player
     */
    public static List<Node<Castle>> getCastleNodes(List<Node<Castle>> nodes, Player player){
        List<Node<Castle>> returnList = new LinkedList<>();
        for(Node<Castle> node : nodes){
            if(node.getValue().getOwner() == player){
                returnList.add(node);
            }
        }
        return returnList;
    }

    public void reset() {
        this.remainingTroops = 0;
        this.points = 0;
    }

    /**
     * uses the joker
     * @return if the joker was used
     */
    public boolean useJoker() {
        if(!hasJoker())
            return false;
        addTroops(GameConstants.JOKER_TROOP_COUNT);
        hasJoker = false;
        return true;
    }

    /**
     * @return if the player has a joker
     */
    public boolean hasJoker(){
        return hasJoker;
    }

    /**
     * @return acopy of the player object
     */
    public abstract Player copy();
}
