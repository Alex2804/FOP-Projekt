package game.AAI;

import base.Edge;
import base.Graph;
import base.Node;
import game.Player;
import game.map.Castle;
import game.map.Kingdom;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Alexander Muth
 * Evaluation Methods to choose castles during distribution period for an AI
 */
public class AIDistributionEvalMethods {
    /**
     * Get the best castles during distribution period
     * @param castleGraph the graph containing all castles
     * @param player the player to assign castles
     * @param castleCount the castles needed to distribute
     * @return a list with castles, which are the best for the player to choose. The size of the return list is equal
     * or smaller to the value of {@code castleCount}
     */
    public static List<Castle> getBestDistributionCastles(Graph<Castle> castleGraph, Player player, int castleCount){
        List<Castle> allCastles = castleGraph.getAllValues();
        List<Castle> availableCastles = AIMethods.getUnassignedCastles(allCastles);

        if(availableCastles.size() <= castleCount) // return all available castles if there aren't enough
            return availableCastles;

        List<Castle> bestCastles = canOwnKingdom(allCastles, player, castleCount);
        int tempCastleCount = bestCastles == null ? castleCount : (castleCount - bestCastles.size());
        if(tempCastleCount <= 0) { // if there is a kingdom which can get owned
            return bestCastles; // return the castles which are necessary to own the kingdom
        }else if(tempCastleCount < castleCount){
            List<Castle> tempBestCastles;
            List<Castle> tempCastles = AIMethods.getCastlesFromOtherKingdoms(allCastles, bestCastles.get(0).getKingdom());
            while(!tempCastles.isEmpty()){
                tempBestCastles = canOwnKingdom(tempCastles, player, tempCastleCount);
                if(tempBestCastles != null && tempCastleCount - tempBestCastles.size() >=  0){
                    bestCastles.addAll(tempBestCastles);
                    tempCastles = AIMethods.getCastlesFromOtherKingdoms(tempCastles, tempBestCastles.get(0).getKingdom());
                    tempCastleCount -= tempBestCastles.size();
                }else{
                    break;
                }
            }
        }

        List<List<Castle>> connectedCastles = AIMethods.getConnectedCastles(castleGraph, player);


        return null;
    }

    /**
     * @param castles all castles
     * @param player the player which want to capture a kingdom
     * @param castleCount the castles which can be captured at this move
     * @return null if their is no kingdom which can be owned with the given castle count or a list with the castles,
     * which are necessary to own the kingdom. If a list is returned, the size is equal to the value of
     * {@code castleCount}
     */
    public static List<Castle> canOwnKingdom(List<Castle> castles, Player player, int castleCount){
        Kingdom kingdom;
        List<Kingdom> passedKingdoms = new LinkedList<>();
        List<Castle> tempCastles;
        for(Castle castle : castles){
            kingdom = castle.getKingdom();
            if(kingdom != null && !passedKingdoms.contains(kingdom)){
                passedKingdoms.add(kingdom);

                tempCastles = new LinkedList<>();
                for(Castle kingdomCastle : kingdom.getCastles()){
                    if(kingdomCastle.getOwner() == null){
                        tempCastles.add(kingdomCastle);
                    }else if(kingdomCastle.getOwner() != player){
                        tempCastles = null;
                        break;
                    }
                }

                if(tempCastles != null && tempCastles.size() <= castleCount)
                    return tempCastles;
            }
        }

        return null;
    }

    /**
     * Evaluates a value for passed castle dependent on some factors
     * @param castleGraph the available castles
     * @param player the player to evaluate for
     * @param castle the castle to evaluate
     * @return the evaluated value for the castle object
     */
    public static int evaluateCastle(Graph<Castle> castleGraph, Player player, Castle castle){
        int points = 0;
        if(castle == null)
            return points;

        List<Player> tempPlayers = new ArrayList<>(2);
        tempPlayers.add(player);
        points += hasOtherNeighbours(castleGraph, tempPlayers, castle) ? AIConstants.NO_ENEMY_NEIGHBOUR + AIConstants.SURROUNDED_BY_OWN_CASTLES : 0;
        if(points <= 0){
            tempPlayers.add(null);
            points += hasOtherNeighbours(castleGraph, tempPlayers, castle) ? AIConstants.NO_ENEMY_NEIGHBOUR : 0;
        }
        if(castle.getKingdom() != null){
            points += isFirstCastleInKingdom(player, castle) ? AIConstants.FIRST_CASTLE_IN_KINGDOM : 0;
            points += AIKingdomEvalMethods.evaluateKingdom(castleGraph, castle.getKingdom());
        }


        return points;
    }

    /**
     * Returns if the castle has other neighbours than the given players
     * @param castleGraph graph containing all neighbours
     * @param players the players which should be the only neighbours
     * @param castle the castle to check the neighbours from
     * @return if the castle has other neighbours than the expected (owner of castles)
     */
    public static boolean hasOtherNeighbours(Graph<Castle> castleGraph, List<Player> players, Castle castle){
        Node<Castle> node = castleGraph.getNode(castle);
        if(node == null)
            return false;
        boolean temp;
        for(Edge<Castle> edge : castleGraph.getEdges(node)){
            temp = false;
            for(Player player : players){
                if(edge.getOtherNode(node).getValue().getOwner() == player)
                    temp = true;
            }
            if(!temp)
                return true;
        }
        return false;
    }

    /**
     * @param player the player to check if it has any castles in the kingdom
     * @param castle the castle to check it's kingdom
     * @return if the castle is the first castle of the player in it's kingdom (false if the castles kingdom is null)
     */
    public static boolean isFirstCastleInKingdom(Player player, Castle castle){
        if(castle.getKingdom() == null)
            return false;
        return !castle.getKingdom().getCastles().stream().anyMatch(c -> c.getOwner() == player);
    }
}
