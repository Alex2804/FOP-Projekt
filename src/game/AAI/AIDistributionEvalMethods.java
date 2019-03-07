package game.AAI;

import base.Edge;
import base.Graph;
import base.Node;
import game.Player;
import game.map.Castle;
import game.map.Kingdom;
import javafx.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

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
        List<Castle> availableCastles = Player.getCastles(allCastles, null);

        if(availableCastles.size() <= castleCount) // return all available castles if there aren't enough
            return availableCastles;

        List<Castle> bestCastles = new LinkedList<>();
        List<Castle> tempBestCastles;
        List<Castle> tempCastles = new LinkedList<>(allCastles);
        int tempCastleCount = castleCount;
        while(!tempCastles.isEmpty() && tempCastleCount > 0){
            tempBestCastles = canOwnKingdom(tempCastles, player, tempCastleCount);
            if(tempBestCastles != null && tempCastleCount - tempBestCastles.size() >=  0){
                bestCastles.addAll(tempBestCastles);
                tempCastles = AIMethods.getCastlesFromOtherKingdoms(tempCastles, tempBestCastles.get(0).getKingdom());
                tempCastleCount -= tempBestCastles.size();
            }else{
                break;
            }
        }

        if(bestCastles == null) {
            bestCastles = new LinkedList<>();
        }
        tempCastleCount = castleCount - bestCastles.size();
        if(tempCastleCount <= 0){
            return bestCastles;
        }

        List<List<Castle>> pairs = null;
        int pairCount = tempCastleCount;
        Map<Castle, Integer> castleRatingMap = new HashMap<>();
        while(tempCastleCount > 0 && pairCount > 0){
            if(pairCount <= tempCastleCount){
                pairs = AIMethods.getPossibleCastlePairs(castleGraph, player, pairCount--);

                if(pairs != null && !pairs.isEmpty()){
                    List<List<Pair<Castle, Integer>>> ratedPairs = new LinkedList<>();
                    List<Pair<Castle, Integer>> ratedPair;
                    Integer rating;
                    for(List<Castle> pair : pairs){
                        ratedPair = new LinkedList<>();
                        for(Castle castle : pair){
                            rating = castleRatingMap.get(castle);
                            if(rating == null){
                                rating = evaluateCastle(castleGraph, player, castle);
                                castleRatingMap.put(castle, rating);
                            }
                            ratedPair.add(new Pair<>(castle, rating));
                        }
                        ratedPairs.add(ratedPair);
                    }

                    while(!pairs.isEmpty() && pairCount < tempCastleCount){
                        List<Castle> bestPair = AIMethods.getBestPair(ratedPairs);
                        ratedPairs.remove(bestPair.stream().map(c -> new Pair<>(c, castleRatingMap.get(c))).collect(Collectors.toList()));
                        if(bestPair.size() <= tempCastleCount) {
                            bestCastles.addAll(bestPair);
                        }
                        tempCastleCount = castleCount - bestCastles.size();
                    }
                }
            }
        }

        // the following shouldn't be necessary but is for safety
        tempCastleCount = castleCount - bestCastles.size();
        if(tempCastleCount > 0){ // return a list with the best rated castles
            System.out.println("Semantic error in AIDistributionEvalMethods#getBestDistributionCastles !!!");

            for(Castle castle : availableCastles){
                castleRatingMap.put(castle, evaluateCastle(castleGraph, player, castle));
            }

            bestCastles.addAll(AIMethods.getBest(AIMethods.entrysToPairs(castleRatingMap.entrySet()), tempCastleCount));
            return bestCastles;
        }

        return (bestCastles.size() < castleCount) ? bestCastles : bestCastles.subList(0, castleCount);
    }

    /**
     * @param castles all castles
     * @param player the player which want to capture a kingdom
     * @param castleCount the castles which can be captured at this move
     * @return null if their is no kingdom which can be owned with the given castle count or a list with the castles,
     * which are necessary to own the kingdom. If a list is returned, the size is equal or smaller to the value of
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
        points += !AIMethods.hasOtherNeighbours(castleGraph, tempPlayers, castle) ? AIConstants.NO_ENEMY_NEIGHBOUR + AIConstants.SURROUNDED_BY_OWN_CASTLES : 0;
        if(points <= 0){
            tempPlayers.add(null);
            points += !AIMethods.hasOtherNeighbours(castleGraph, tempPlayers, castle) ? AIConstants.NO_ENEMY_NEIGHBOUR : 0;
        }
        if(castle.getKingdom() != null){
            points += isFirstCastleInKingdom(player, castle) ? AIConstants.FIRST_CASTLE_IN_KINGDOM : 0;
            points += AIKingdomEvalMethods.evaluateKingdom(castleGraph, castle.getKingdom());
        }


        return points;
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
