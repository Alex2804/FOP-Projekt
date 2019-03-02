package game.AAI;

import game.Player;
import game.map.Castle;
import game.map.Kingdom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Alexander Muth
 * Evaluation Methods for an AI
 */
public class AIEvalMethods {
    /**
     * Evaluates a value for passed castle dependent on some factors
     * @param castles the available castles
     * @param player the player to evaluate for
     * @param castle the castle to evaluate
     * @return a wrapper with the evaluated points and the castle object
     */
    public static int evaluateCastle(List<Castle> castles, Player player, Castle castle){
        int points = 0;

        points += isLastCastleOfPlayer(castles, castle) ? AIConstants.OPPORTUNITY_ELEMINATE_PLAYER : 0;
        points += isBigThreat(castles, player, castle.getOwner()) ? AIConstants.BELONGS_BIG_THREAT : 0;
        if(castle.getKingdom() != null){
            points += isImportantKingdom(player, castle.getKingdom()) ? AIConstants.IMPORTANT_KINGDOM : 0;
            boolean closeToCapture = isCloseToCaptureKingdom(player, castle.getKingdom());
            points += closeToCapture ? AIConstants.CLOSE_TO_CAPTURE_KINGDOM : 0;
            points += isOwnedByOnePlayer(castle.getKingdom()) ? AIConstants.BREAK_UP_KINGDOM : 0;
            points += isLastCastleInKingdom(player, castle) ? AIConstants.LAST_CASTLE_IN_KINGDOM : 0;
        }

        return points;
    }

    /**
     * Checks if the castle is the last castle, which belongs to the owner in the list of castles
     * @param castles list of castles
     * @param castle castle to check owner
     * @return if the castle is the last castle of the owner
     */
    public static boolean isLastCastleOfPlayer(List<Castle> castles, Castle castle){
        return !castles.stream().anyMatch(c -> c.getOwner() == castle.getOwner() && c != castle);
    }

    /**
     * Checks if a Player is a big threat for another player
     * @param player
     * @param other player to check if it is a big threat
     * @return if the other player is a big threat for player
     */
    public static boolean isBigThreat(List<Castle> castles, Player player, Player other){
        return player.getCastles(castles).size() <= other.getCastles(castles).size();
    }

    /**
     * checks if a player is (one of) the strongest player in a kingdom
     * @param player player to check if is (one of) the strongest
     * @param kingdom kingdom to check if player is (one of) the strongest
     * @return if the player is (one of) the strongest players in the kingdom
     */
    public static boolean isImportantKingdom(Player player, Kingdom kingdom){
        kingdom.getCastles();
        Integer otherIndex;
        int playerPoints = 0;
        List<Integer> otherPoints = new ArrayList<>();
        Map<Player, Integer> indices = new HashMap<>();
        for(Castle castle : kingdom.getCastles()){
            if(castle.getOwner() == player){
                playerPoints += 1;
            }else{
                otherIndex = indices.get(castle.getOwner());
                if(otherIndex == null){
                    otherIndex = otherPoints.size();
                    indices.put(castle.getOwner(), otherIndex);
                    otherPoints.add(1);
                }else{
                    otherPoints.set(otherIndex, otherPoints.get(otherIndex) + 1);
                }
            }
        }

        for(int p : otherPoints){
            if(p > playerPoints)
                return false;
        }
        return true;
    }

    /**
     * Checks if a player is close to capture a kingdom
     * @param player player to check for
     * @param kingdom kingdom to check for
     * @return if the player is close to capture the kingdom
     */
    public static boolean isCloseToCaptureKingdom(Player player, Kingdom kingdom){
        double playerCastleCount =  kingdom.getCastles().stream().filter(c -> c.getOwner() != player).collect(Collectors.toList()).size();
        double percentage = playerCastleCount / kingdom.getCastles().size();
        return percentage >= 0.7; // more than 70% of the kingdom is owned by the player
    }

    /**
     * @param kingdom kingdom to check
     * @return if the kingdom is owned by one player
     */
    public static boolean isOwnedByOnePlayer(Kingdom kingdom){
        Player owner = null;
        for(Castle castle : kingdom.getCastles()){
            if(owner == null)
                owner = castle.getOwner();
            else if(owner != castle.getOwner())
                return false;
        }
        return true;
    }

    /**
     * Checks if the castle is the last castle in the kingdom, owned by another player than the passed one
     * @param player
     * @param castle
     * @return if the castle is the last one owned by another player
     */
    public static boolean isLastCastleInKingdom(Player player, Castle castle){
        return !castle.getKingdom().getCastles().stream().anyMatch(c -> c.getOwner() != player && c != castle);
    }
}
