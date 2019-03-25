package game.map;

import base.GraphAlgorithm;
import base.Node;
import base.Edge;
import base.Graph;
import game.Game;
import game.Player;
import game.goals.AFlagEmpireGoal;
import gui.components.MapPanel;

import java.util.List;

public class PathFinding extends GraphAlgorithm<Castle> {

    private MapPanel.Action action;
    private Player currentPlayer;

    public boolean clashOfArmiesGoal = false;
    public AFlagEmpireGoal flagEmpireGoal;

    public PathFinding(Graph<Castle> graph, Castle sourceCastle, MapPanel.Action action, Player currentPlayer) {
        super(graph, graph.getNode(sourceCastle));
        this.action = action;
        this.currentPlayer = currentPlayer;
    }

    @Override
    protected double getValue(Edge<Castle> edge) {
        Castle castleA = edge.getNodeA().getValue();
        Castle castleB = edge.getNodeB().getValue();
        return castleA.distance(castleB);
    }

    @Override
    protected boolean isPassable(Edge<Castle> edge) {
        if(clashOfArmiesGoal)
            return true;

        Castle castleA = edge.getNodeA().getValue();
        Castle castleB = edge.getNodeB().getValue();

        // One of the regions should belong to the current player
        if(flagEmpireGoal == null && castleA.getOwner() != currentPlayer && castleB.getOwner() != currentPlayer)
            return false;
        else if(flagEmpireGoal != null && castleA.getOwner() != currentPlayer && castleB.getOwner() != currentPlayer
                && !flagEmpireGoal.isFlagSet(castleA) && !flagEmpireGoal.isFlagSet(castleB)){
            return false;
        }

        if(action == MapPanel.Action.ATTACKING) {
            return castleA.getOwner() != null && castleB.getOwner() != null;
        } else if(flagEmpireGoal == null && action == MapPanel.Action.MOVING) {
            // One of the regions may be empty
            if(castleA.getOwner() == null || castleB.getOwner() == null)
                return true;
            // Else both regions should belong to the current player
            return castleA.getOwner() == castleB.getOwner() && castleA.getOwner() == currentPlayer;
        } else if(flagEmpireGoal != null && action == MapPanel.Action.MOVING){
            if((flagEmpireGoal.isFlagSet(castleA) && castleA.getTroopCount() <= 0 && castleB.getOwner() == currentPlayer)
                    || (flagEmpireGoal.isFlagSet(castleB) && castleB.getTroopCount() <= 0 && castleA.getOwner() == currentPlayer)){
                return true;
            }else if(castleA.getOwner() == null || castleB.getOwner() == null){
                return true;
            }
            return castleA.getOwner() == castleB.getOwner() && castleA.getOwner() == currentPlayer;
        }else {
            return false;
        }
    }


    @Override
    protected boolean isPassable(Node<Castle> node) {
        if(clashOfArmiesGoal) {
            return true;
        } else if(flagEmpireGoal != null){
            return (node.getValue().getOwner() == currentPlayer) || ((flagEmpireGoal.isFlagSet(node.getValue()) && node.getValue().getTroopCount() <= 0));
        }
        return node.getValue().getOwner() == currentPlayer;
    }

    public List<Edge<Castle>> getPath(Castle targetCastle) {
        if(action == MapPanel.Action.MOVING && flagEmpireGoal != null && targetCastle.getOwner() != sourceNode.getValue().getOwner()){
            return null;
        }
        return this.getPath(getGraph().getNode(targetCastle));
    }
}
