package gui.components;

import base.Edge;
import de.teast.AConstants;
import game.AI;
import game.Game;
import game.Player;
import game.map.Castle;
import game.map.GameMap;
import game.map.PathFinding;
import game.players.Human;
import gui.Resources;
import gui.View;
import gui.views.GameView;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.List;

public class MapPanel extends JScrollPane {

    public enum Action {
        NONE,
        MOVING,
        ATTACKING
    }

    private static final int CASTLE_SIZE = 50;
    private static final int ICON_SIZE = 20;
    private final GameView gameView;

    private ImagePanel imagePanel;
    private GameMap map;
    private Point mousePos, oldView;
    private Castle selectedCastle;
    private boolean showConnections;
    private Resources resources;
    private Game game;
    private Action currentAction;
    private PathFinding pathFinding;
    private List<Edge<Castle>> highlightedEdges;
    private Castle targetCastle;

    public MapPanel(GameView gameView, Resources resources) {
        super();
        this.gameView = gameView;
        this.setBorder(new LineBorder(Color.BLACK));
        this.setViewportView(this.imagePanel = new ImagePanel());
        this.addMouseListener(onMouseInput);
        this.addMouseMotionListener(onMouseInput);
        this.showConnections = false;
        this.setAutoscrolls(true);
        this.resources = resources;
        this.currentAction = Action.NONE;

        this.getActionMap().put("Escape", new AbstractAction("Escape") {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if(currentAction != Action.NONE) {
                    currentAction = Action.NONE;
                    targetCastle = null;
                    highlightedEdges = null;
                    repaint();
                } else if(selectedCastle != null) {
                    selectedCastle = null;
                    repaint();
                }
            }
        });
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Escape");
    }

    private Castle getRegion(Point p) {
        if (map == null)
            return null;

        for (Castle castle : map.getCastles()) {
            Point location = castle.getLocationOnMap();
            Rectangle rect = new Rectangle(location.x, location.y, CASTLE_SIZE, CASTLE_SIZE);
            if (rect.contains(p))
                return castle;
        }

        return null;
    }

    private boolean canPerformAction() {
        if(game.getCurrentPlayer() instanceof AI)
            return false;

        if(game.isOver())
            return false;

        return game.getAttackThread() == null;
    }

    private MouseAdapter onMouseInput = new MouseAdapter() {

        @Override
        public void mousePressed(MouseEvent mouseEvent) {
            super.mousePressed(mouseEvent);
            oldView = getViewport().getViewPosition();
            mousePos = mouseEvent.getPoint();
        }

        @Override
        public void mouseReleased(MouseEvent mouseEvent) {
            super.mousePressed(mouseEvent);

            if(getCursor().getType() == Cursor.MOVE_CURSOR)
                setCursor(Cursor.getDefaultCursor());
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            super.mouseDragged(e);

            if (oldView != null && mousePos != null && !mousePos.equals(e.getPoint())) {
                JViewport vp = getViewport();


                if (getWidth() < imagePanel.getWidth() || getHeight() < imagePanel.getHeight()) {
                    int newX = (int) Math.max(0, oldView.getX() - (e.getX() - mousePos.getX()));
                    int newY = (int) Math.max(0, oldView.getY() - (e.getY() - mousePos.getY()));

                    // don't update view position, if you cannot move in this direction (e.g. vp.w >= img.w)
                    if(getWidth() >= imagePanel.getWidth())
                        newX = vp.getViewPosition().x;

                    if(getHeight() >= imagePanel.getHeight())
                        newY = vp.getViewPosition().y;

                    if(currentAction == Action.ATTACKING ||  currentAction == Action.MOVING)
                        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                    else
                        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));

                    Point newPoint = new Point(newX, newY);
                    vp.setViewPosition(newPoint);
                }
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                Point mousePos = cursorToMapLocation(e.getPoint());
                Player currentPlayer = game.getCurrentPlayer();
                boolean selectNew = true;
                Action lastAction = currentAction;

                if (selectedCastle != null && canPerformAction()) {
                    Point castlePos = selectedCastle.getLocationOnMap();

                    if(game.isClashOfArmiesGoal()){
                        if(selectedCastle.getOwner() == game.getCurrentPlayer()){
                            Rectangle iconPlus  = getBoundsPlusIcon(castlePos);
                            Rectangle iconArrow  = getBoundsArrowIcon(castlePos);
                            selectNew = false;

                            if(iconPlus.contains(mousePos) && game.clashOfArmiesGoal().isBase(selectedCastle)) {
                                if(game.clashOfArmiesGoal().hasEnoughPointsToBuy(selectedCastle.getOwner())){
                                    game.clashOfArmiesGoal().addTroops(selectedCastle);
                                }
                            } else if (iconArrow.contains(mousePos)) {
                                if(!game.clashOfArmiesGoal().getTroops(selectedCastle).isEmpty()) {
                                    currentAction = (currentAction == Action.MOVING ? Action.NONE : Action.MOVING);
                                }
                            } else {
                                selectNew = true;
                            }
                        }
                    }else if(canChooseCastle()) {
                        Rectangle iconCheck = getBoundsIconCheck(castlePos);
                        if (iconCheck.contains(mousePos)) {
                            game.chooseCastle(selectedCastle, currentPlayer);
                            gameView.updateStats();
                            setCursor(Cursor.getDefaultCursor());
                        }
                    }else if(game.isCaptureTheFlagGoal() && canChooseForFlag()){
                        Rectangle iconFlag = getBoundsIconFlag(castlePos);
                        if(iconFlag.contains(mousePos)){
                            game.captureTheFlagGoal().chooseCastleForFlag(selectedCastle.getOwner(), selectedCastle);
                            setCursor(Cursor.getDefaultCursor());
                        }
                    }else if(selectedCastle.getOwner() == currentPlayer && game.getRound() > 1) {
                        Rectangle iconPlus  = getBoundsPlusIcon(castlePos);
                        Rectangle iconArrow  = getBoundsArrowIcon(castlePos);
                        Rectangle iconSwords  = getBoundsSwordsIcon(castlePos);
                        Rectangle iconFlag = getBoundsIconFlag(castlePos);
                        selectNew = false;

                        if(iconPlus.contains(mousePos)) {
                            if(currentPlayer.getRemainingTroops() > 0) {
                                game.addTroops(currentPlayer, selectedCastle, 1);
                                gameView.updateStats();
                            }
                        } else if (iconArrow.contains(mousePos)) {
                            if(selectedCastle.getTroopCount() > 1) {
                                currentAction = (currentAction == Action.MOVING ? Action.NONE : Action.MOVING);
                            }
                        } else if (iconSwords.contains(mousePos)) {
                            if(canAttack()) {
                                currentAction = (currentAction == Action.ATTACKING ? Action.NONE : Action.ATTACKING);
                            }
                        } else if (iconFlag.contains(mousePos)){
                            if(game.isFlagEmpireGoal() && canChooseForFlag()){
                                game.flagEmpireGoal().setFlag(selectedCastle, selectedCastle.getOwner());
                                setToolTipText("Auf dieser Flagge wurde bereits eine Flagge platziert.");
                                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                            }
                        } else {
                            selectNew = true;
                        }
                    }

                    if(currentAction != Action.NONE) {
                        if(lastAction != currentAction) {
                            pathFinding = new PathFinding(game.getMap().getGraph(), selectedCastle, currentAction, currentPlayer);
                            pathFinding.clashOfArmiesGoal = game.isClashOfArmiesGoal();
                            pathFinding.flagEmpireGoal = game.flagEmpireGoal();
                            pathFinding.run();
                        }

                        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                    }
                }

                if(selectNew) {
                    Castle nextCastle = getRegion(mousePos);
                    if(nextCastle == null || nextCastle == selectedCastle || currentAction == Action.NONE) {
                        currentAction = Action.NONE;
                        if(game.isClashOfArmiesGoal() && selectedCastle != nextCastle){
                            game.clashOfArmiesGoal().castleSelected(nextCastle);
                        }
                        selectedCastle = nextCastle;
                        setCursor(Cursor.getDefaultCursor());
                    }else if(game.isClashOfArmiesGoal() && currentAction == Action.MOVING && game.clashOfArmiesGoal().tryMove(selectedCastle, nextCastle, pathFinding.getPath(nextCastle)) != null) {
                        game.clashOfArmiesGoal().move(selectedCastle, nextCastle, pathFinding.getPath(nextCastle));
                        currentAction = Action.NONE;
                        highlightedEdges = null;
                        targetCastle = null;
                        setCursor(Cursor.getDefaultCursor());
                    }else if(!game.isClashOfArmiesGoal() && currentAction == Action.MOVING && pathFinding.getPath(nextCastle) != null) {
                        NumberDialog nd = new NumberDialog("Wie viele Truppen möchtest du verschieben?", 1, selectedCastle.getTroopCount() - 1, 1);
                        if(nd.showDialog(MapPanel.this)) {
                            game.moveTroops(selectedCastle, nextCastle, nd.getValue());
                            //selectedCastle.moveTroops(nextCastle, nd.getValue());
                            currentAction = Action.NONE;
                            selectedCastle = null;
                            highlightedEdges = null;
                            targetCastle = null;
                            setCursor(Cursor.getDefaultCursor());
                            gameView.updateStats();
                        }
                    } else if(!game.isClashOfArmiesGoal() && currentAction == Action.ATTACKING && pathFinding.getPath(nextCastle) != null && nextCastle.getOwner() != selectedCastle.getOwner()
                                && (!game.isFlagEmpireGoal() || nextCastle.getTroopCount() > 0)) {
                        NumberDialog nd = new NumberDialog("Mit wie vielen Truppen möchtest du angreifen?", 1, selectedCastle.getTroopCount(), selectedCastle.getTroopCount()  - 1);
                        if(nd.showDialog(MapPanel.this)) {
                            game.startAttack(selectedCastle, nextCastle, nd.getValue());
                            currentAction = Action.NONE;
                        }
                    } else {
                        setToolTipText(null);
                        currentAction = Action.NONE;
                        selectedCastle = nextCastle;
                        if(game.isClashOfArmiesGoal()){
                            game.clashOfArmiesGoal().castleSelected(selectedCastle);
                        }
                        setCursor(Cursor.getDefaultCursor());
                    }
                }

                repaint();
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            Point mousePos = cursorToMapLocation(e.getPoint());

            if (selectedCastle != null && getCursor().getType() != Cursor.MOVE_CURSOR) {
                Point castlePos = selectedCastle.getLocationOnMap();

                if(game.isClashOfArmiesGoal()){
                    if(selectedCastle.getOwner() == game.getCurrentPlayer()) {
                        if(!game.clashOfArmiesGoal().getTroops(selectedCastle).isEmpty()) {
                            Rectangle iconArrow = getBoundsArrowIcon(castlePos);
                            if (iconArrow.contains(mousePos)) {
                                setToolTipText("Truppen bewegen");
                                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                                return;
                            }
                        }
                        if(game.clashOfArmiesGoal().isBase(selectedCastle)){
                            Rectangle iconPlus  = getBoundsPlusIcon(castlePos);
                            if(iconPlus.contains(mousePos)){
                                setToolTipText("Truppen kaufen");
                                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                                return;
                            }
                        }
                    }
                } else if (canChooseCastle()) {
                    Rectangle iconCheck = getBoundsIconCheck(castlePos);
                    if (iconCheck.contains(mousePos)) {
                        setToolTipText("Diese Burg besetzen");
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        return;
                    }
                }else if(game.isCaptureTheFlagGoal() && canChooseForFlag()){
                    Rectangle iconFlag = getBoundsIconFlag(castlePos);
                    if(iconFlag.contains(mousePos)){
                        setToolTipText("Die Flagge auf dieser Burg platzieren");
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        return;
                    }
                } else if(selectedCastle.getOwner() == game.getCurrentPlayer() && game.getRound() > 1) {
                    Rectangle iconPlus  = getBoundsPlusIcon(castlePos);
                    Rectangle iconArrow  = getBoundsArrowIcon(castlePos);
                    Rectangle iconSwords  = getBoundsSwordsIcon(castlePos);
                    Rectangle bounds[] = { iconPlus, iconArrow, iconSwords };
                    String tooltips[] = { "Truppen hinzufügen", "Truppen bewegen", "Burg angreifen" };

                    if(game.isFlagEmpireGoal()  && getBoundsIconFlag(castlePos).contains(mousePos)){
                        if(canChooseForFlag()){
                            setToolTipText("Eine Flagge auf dieser Burg für " + AConstants.FLAG_POINTS + " Punkte platzieren");
                            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        }else{
                            setToolTipText("Auf dieser Burg wurde bereits eine Flagge platziert.");
                            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                        }
                        return;
                    }
                    for(int i = 0; i < bounds.length; i++) {
                        if (bounds[i].contains(mousePos)) {
                            setToolTipText(tooltips[i]);
                            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                            return;
                        }
                    }
                }

                if(currentAction == Action.MOVING || currentAction == Action.ATTACKING) {
                    String toolTipText = null;
                    setToolTipText(null);
                    targetCastle = getRegion(mousePos);
                    if(game.isClashOfArmiesGoal() && targetCastle != null){
                        highlightedEdges = game.clashOfArmiesGoal().tryMove(selectedCastle, targetCastle, pathFinding.getPath(targetCastle));
                        repaint();
                    }else if(targetCastle != null) {
                        if(currentAction != Action.ATTACKING || (targetCastle.getOwner() != selectedCastle.getOwner() && (!game.isFlagEmpireGoal() || (!game.flagEmpireGoal().isFlagSet(targetCastle)) || targetCastle.getTroopCount() > 0))) {
                            highlightedEdges = pathFinding.getPath(targetCastle);
                            repaint();
                        } else {
                            targetCastle = null;
                        }
                    } else if(highlightedEdges != null) {
                        highlightedEdges = null;
                        targetCastle = null;
                        repaint();
                    }

                    setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                    setToolTipText(toolTipText);
                    return;
                }


                setCursor(Cursor.getDefaultCursor());
                setToolTipText(null);
            }
        }
    };

    private boolean canChooseCastle() {
        if (selectedCastle == null)
            return false;

        return game.getCurrentPlayer() instanceof Human &&
                game.getCurrentPlayer().getRemainingTroops() > 0 &&
                game.getRound() == 1 &&
                selectedCastle.getOwner() == null;
    }
    private boolean canChooseForFlag(){
        if(game.isCaptureTheFlagGoal()) {
            return selectedCastle != null
                    && game.getCurrentPlayer() == selectedCastle.getOwner()
                    && !game.captureTheFlagGoal().hasChosen(selectedCastle.getOwner())
                    && !canChooseCastle()
                    && game.getRound() == 1;
        }else if(game.isFlagEmpireGoal()){
            return selectedCastle != null
                    && game.getCurrentPlayer() == selectedCastle.getOwner()
                    && !game.flagEmpireGoal().isFlagSet(selectedCastle)
                    && game.getRound() > 1
                    && selectedCastle.getOwner().getPoints() >= AConstants.FLAG_POINTS;
        }else{
            return false;
        }
    }

    private boolean canAttack() {
        if(selectedCastle == null)
            return false;

        return game.getCurrentPlayer() instanceof Human &&
               selectedCastle.getOwner() == game.getCurrentPlayer() &&
               selectedCastle.getTroopCount() > 1;
    }

    /**
     * @param castlePos position of the castle
     * @return the bounds of the flag icon
     */
    private Rectangle getBoundsIconFlag(Point castlePos){
        if(game.isCaptureTheFlagGoal()) {
            return getBoundsIconCheck(castlePos);
        } else {
            Rectangle swordsIcon = getBoundsSwordsIcon(castlePos);
            swordsIcon.x += ICON_SIZE + 2;
            return swordsIcon;
        }
    }

    private Rectangle getBoundsIconCheck(Point castlePos) {
        int x = (CASTLE_SIZE + 10 - ICON_SIZE) / 2 + castlePos.x - 5;
        int y = (castlePos.y - 5 - ICON_SIZE);

        return new Rectangle(x, y, ICON_SIZE, ICON_SIZE);
    }

    private Rectangle getBoundsPlusIcon(Point castlePos) {
        int totalWidth = 3 * (ICON_SIZE + 2);
        int x = castlePos.x - 5 + (CASTLE_SIZE + 10 - totalWidth) / 2;
        int y = castlePos.y - 6 - ICON_SIZE;
        return new Rectangle(x, y, ICON_SIZE, ICON_SIZE);
    }

    private Rectangle getBoundsArrowIcon(Point castlePos) {
        Rectangle plusIcon = getBoundsPlusIcon(castlePos);
        plusIcon.x += ICON_SIZE + 2;
        return plusIcon;
    }

    private Rectangle getBoundsSwordsIcon(Point castlePos) {
        Rectangle arrowIcon = getBoundsArrowIcon(castlePos);
        arrowIcon.x += ICON_SIZE + 2;
        return arrowIcon;
    }

    public void showConnections(boolean showConnections) {
        this.showConnections = showConnections;
        repaint();
    }

    // (0|0) -> (0 + offsetX|0 + offsetY)
    private Point translate(Point p) {
        int offsetX = 0;
        int offsetY = 0;

        if (getSize().getWidth() > map.getBackgroundImage().getWidth())
            offsetX = (int) ((getSize().getWidth() - map.getBackgroundImage().getWidth()) / 2);

        if (getSize().getHeight() > map.getBackgroundImage().getHeight())
            offsetY = (int) ((getSize().getHeight() - map.getBackgroundImage().getHeight()) / 2);

        return new Point(p.x + offsetX, p.y + offsetY);
    }

    private Point cursorToMapLocation(Point p) {
        int offsetX = 0;
        int offsetY = 0;

        if (getSize().getWidth() > map.getBackgroundImage().getWidth())
            offsetX = (int) ((getSize().getWidth() - map.getBackgroundImage().getWidth()) / 2);

        if (getSize().getHeight() > map.getBackgroundImage().getHeight())
            offsetY = (int) ((getSize().getHeight() - map.getBackgroundImage().getHeight()) / 2);

        JViewport jp = this.getViewport();
        return new Point(p.x - offsetX + jp.getViewPosition().x, p.y - offsetY + jp.getViewPosition().y);
    }

    public void setGame(Game game) {
        this.game = game;
        this.map = game.getMap();
        this.imagePanel.setSize(map.getSize());
        this.repaint();
    }

    class ImagePanel extends JPanel {

        @Override
        public Dimension getPreferredSize() {
            return map != null ? map.getSize() : new Dimension();
        }

        @Override
        public void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g;
            Point offset = translate(new Point(0, 0));

            if (map != null) {
                g.drawImage(map.getBackgroundImage(), offset.x, offset.y, null);

                if (showConnections) {
                    for (Edge<Castle> edge : map.getEdges()) {
                        Point p1 = translate(edge.getNodeA().getValue().getLocationOnMap());
                        Point p2 = translate(edge.getNodeB().getValue().getLocationOnMap());

                        if(highlightedEdges != null && highlightedEdges.contains(edge)) {
                            g2.setStroke(new BasicStroke(3));
                            g.setColor(Color.RED);
                        } else {
                            g2.setStroke(new BasicStroke(1));
                            g.setColor(Color.WHITE);
                        }

                        g2.draw(new Line2D.Float(p1.x + CASTLE_SIZE / 2.0f, p1.y + CASTLE_SIZE / 2.0f, p2.x + CASTLE_SIZE / 2.0f, p2.y + CASTLE_SIZE / 2.0f));
                        g2.setStroke(new BasicStroke(1));
                    }
                }

                for (Castle region : map.getCastles()) {
                    Color color = region.getOwner() == null ? Color.WHITE : region.getOwner().getColor();
                    Point location = translate(region.getLocationOnMap());
                    BufferedImage castle = resources.getCastle(color, region.getType());
                    g.drawImage(castle, location.x, location.y, null);

                    // Draw troop count
                    if(region.getTroopCount() > 0) {
                        BufferedImage unitIcon = resources.getUnitIcon();
                        String str = String.valueOf(region.getTroopCount());
                        Dimension strDimensions = View.calculateTextSize(str, g.getFont());
                        Font troopCountFont = new Font(g.getFont().getName(), Font.BOLD, 15);
                        FontMetrics fm = g.getFontMetrics(troopCountFont);

                        int totalWidth = strDimensions.width + 2 + unitIcon.getWidth();
                        int textX = location.x + (castle.getWidth() - totalWidth) / 2;
                        int textY = location.y + castle.getHeight();

                        g.setColor(Color.WHITE);
                        g.fillRoundRect(textX - 2, textY - 2, totalWidth + 4, unitIcon.getHeight() + 4, 5, 5);
                        g.setColor(Color.BLACK);
                        g.setFont(troopCountFont);
                        g.drawString(str, textX, textY + fm.getAscent());
                        g.drawImage(unitIcon, textX + 2 + strDimensions.width, textY, null);
                    }
                }

                // Draw overlay icon if highlighted
                if(currentAction != Action.NONE && targetCastle != null && highlightedEdges != null && canPerformAction()) {
                    BufferedImage icon = (currentAction == Action.ATTACKING ? resources.getSwordsIcon() : resources.getArrowIcon());
                    Point targetLocation = translate(targetCastle.getLocationOnMap());
                    int x = targetLocation.x + (CASTLE_SIZE - ICON_SIZE) / 2;
                    int y = targetLocation.y + (CASTLE_SIZE - ICON_SIZE) / 2;
                    g.drawImage(icon, x, y, ICON_SIZE, ICON_SIZE, null);
                }

                // HUD
                if (selectedCastle != null) {

                    Point location = translate(selectedCastle.getLocationOnMap());
                    g.setColor(selectedCastle.getOwner() == null ? Color.WHITE : selectedCastle.getOwner().getColor());
                    g.drawRect(location.x - 5, location.y - 5, CASTLE_SIZE + 10, CASTLE_SIZE + 10);

                    if(canPerformAction()) {
                        if (!game.isClashOfArmiesGoal() && canChooseCastle()) {
                            BufferedImage icon = resources.getCheckIcon();
                            Rectangle bounds = getBoundsIconCheck(location);
                            g.drawImage(icon, bounds.x, bounds.y, ICON_SIZE, ICON_SIZE, null);
                        }else if (game.isCaptureTheFlagGoal() && canChooseForFlag()){
                            drawFlag(g, getBoundsIconFlag(selectedCastle.getLocationOnMap()), selectedCastle.getOwner().getColor(), Color.BLACK);
                        }else if(game.isClashOfArmiesGoal() && selectedCastle.getOwner() == game.getCurrentPlayer()){
                            boolean canMove = !game.clashOfArmiesGoal().getTroops(selectedCastle).isEmpty();

                            BufferedImage plusIcon = game.clashOfArmiesGoal().hasEnoughPointsToBuy(game.getCurrentPlayer()) ? resources.getPlusIcon() : resources.getPlusIconDeactivated();
                            BufferedImage arrowIcon = resources.getArrowIcon();

                            int iconsX = location.x - 5 + (CASTLE_SIZE + 10 - (3 * (ICON_SIZE + 2))) / 2;
                            int iconsY = location.y - 6 - ICON_SIZE;

                            BufferedImage[] icons = {plusIcon, arrowIcon};
                            boolean isBase = game.clashOfArmiesGoal().isBase(selectedCastle);
                            for (int i = 0; i < icons.length; i++) {
                                if((i==0 && isBase) || (i == 1 && canMove)) {
                                    g.drawImage(icons[i], iconsX + (ICON_SIZE + 2) * i, iconsY, ICON_SIZE, ICON_SIZE, null);
                                }
                            }
                        }else if (!game.isClashOfArmiesGoal() && selectedCastle.getOwner() == game.getCurrentPlayer() && game.getRound() > 1) {
                            boolean hasTroops = game.getCurrentPlayer().getRemainingTroops() > 0;
                            boolean canMove = selectedCastle.getTroopCount() > 1;

                            BufferedImage plusIcon = hasTroops ? resources.getPlusIcon() : resources.getPlusIconDeactivated();
                            BufferedImage swordsIcon = resources.getSwordsIcon();
                            BufferedImage arrowIcon = canMove ? resources.getArrowIcon() : resources.getArrowIconDeactivated();

                            int iconsX = location.x - 5 + (CASTLE_SIZE + 10 - (3 * (ICON_SIZE + 2))) / 2;
                            int iconsY = location.y - 6 - ICON_SIZE;

                            BufferedImage icons[] = {plusIcon, arrowIcon, swordsIcon};
                            for (int i = 0; i < icons.length; i++) {
                                g.drawImage(icons[i], iconsX + (ICON_SIZE + 2) * i, iconsY, ICON_SIZE, ICON_SIZE, null);
                            }

                            if(game.isFlagEmpireGoal() && canChooseForFlag()){
                                drawFlag(g, new Rectangle(iconsX + (ICON_SIZE + 2) * icons.length, iconsY, ICON_SIZE, ICON_SIZE), Color.white, Color.BLACK);
                            }else if(game.isFlagEmpireGoal() && game.flagEmpireGoal().isFlagSet(selectedCastle)){
                                drawFlag(g, new Rectangle(iconsX + (ICON_SIZE + 2) * icons.length, iconsY, ICON_SIZE, ICON_SIZE), selectedCastle.getOwner().getColor(), Color.BLACK);
                            }
                        }else if(game.isFlagEmpireGoal() && selectedCastle.getOwner() != null && selectedCastle.getOwner() != game.getCurrentPlayer() && game.flagEmpireGoal().isFlagSet(selectedCastle)){
                            drawFlag(g, getBoundsIconCheck(location), selectedCastle.getOwner().getColor(), Color.BLACK);
                        }
                    }
                }
            }
        }
    }
    private void drawFlag(Graphics g, Rectangle bounds, Color fillColor, Color borderColor){
        Color tempColor = g.getColor();
        int[] xPoints = {bounds.x, bounds.x, bounds.x + bounds.width};
        int[] yPoints = {bounds.y, bounds.y + bounds.height, bounds.y + bounds.height / 2};
        g.setColor(fillColor);
        g.fillPolygon(xPoints, yPoints, 3);
        g.setColor(borderColor);
        g.drawPolygon(xPoints, yPoints, 3);
        g.setColor(tempColor);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if((selectedCastle != null && !game.isClashOfArmiesGoal())
                || (selectedCastle != null && game.isClashOfArmiesGoal() && game.clashOfArmiesGoal().isBase(selectedCastle))) {

            String titleText;
            if(currentAction == Action.NONE) {
                StringBuilder text = new StringBuilder();
                text.append(selectedCastle.getName());
                if (selectedCastle.getOwner() != null)
                    text.append(" - Besitzer: ").append(selectedCastle.getOwner().getName());
                if (selectedCastle.getTroopCount() > 0)
                    text.append(" - Truppen: ").append(selectedCastle.getTroopCount());

                titleText = text.toString();
            } else if(currentAction == Action.MOVING) {
                titleText = "Truppen verschieben";
            } else if(currentAction == Action.ATTACKING) {
                titleText = "Eine Burg angreifen";
            } else {
                return;
            }

            Font font = View.createFont(20);
            g.setFont(font);
            Dimension titleSize = View.calculateTextSize(titleText, font);
            titleSize.width += 6;
            titleSize.height += 3;

            Point textPos = (new Point((MapPanel.this.getWidth() - titleSize.width) / 2, -5));
            g.setColor(Color.WHITE);
            g.fillRect(textPos.x, textPos.y , titleSize.width, titleSize.height);
            g.setColor(Color.BLACK);
            g.drawString(titleText, textPos.x + 3, textPos.y + titleSize.height - 5);
        }
    }

    public void clearSelection() {
        this.selectedCastle = null;
        currentAction = Action.NONE;
        repaint();
        if(game.isClashOfArmiesGoal())
            game.clashOfArmiesGoal().castleSelected(selectedCastle);
    }

    public void reset() {
        currentAction = MapPanel.Action.NONE;
        selectedCastle = null;
        highlightedEdges = null;
        targetCastle = null;
        setCursor(Cursor.getDefaultCursor());
        repaint();
        if(game.isClashOfArmiesGoal())
            game.clashOfArmiesGoal().castleSelected(selectedCastle);
    }
}
