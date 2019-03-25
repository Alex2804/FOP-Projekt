package de.teast.awidgets;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.Timer;
import de.teast.awidgets.api.ACollapsibleGroupBoxInterface;
import de.teast.awidgets.api.ACollapsibleGroupBoxListener;
import de.teast.awidgets.api.ACollapsibleGroupBoxState;
import de.teast.awidgets.api.AComponent;

/**
 * This is a panel, which can get collapsed or extended. This class is derived from APanel and inherits its possibility to have a parent and update the parent-tree
 * In my opinion, this is a real drop-down menu :D
 * @author Alexander Muth
 */
public class ACollapsibleGroupBox extends APanel implements ACollapsibleGroupBoxInterface {
	private static final long serialVersionUID = -4570859684634176128L;
	
	private LinkedList<ACollapsibleGroupBoxListener> groupBoxListeners;
	
	private int collapseInterval, collapseDuration, expandInterval, expandDuration;
	private double animationDegree, animationCurrentHeight, animationTargetHeight, animationHeight;
	
	private GridBagLayout layout;
	private GridBagConstraints contentPanelConstraints;
	
	private ACollapsibleGroupBoxHeader header;
	private ACollapsibleGroupBoxContent contentPanel;
	private ACollapsibleGroupBoxMouseListener mouseListener;
	
	private Timer collapseTimer, expandTimer;
	ACollapsibleGroupBoxState currentState;

	/**
	 * @param title - title of header bar
	 */
	public ACollapsibleGroupBox(String title) {
		super();
		init(title);
	}
	/**
	 * @param parent - parent
	 * @param title - title of header bar
	 */
	public ACollapsibleGroupBox(AComponent parent, String title) {
		super(parent);
		init(title);
	}
	/**
	 * @return this ACollapsibleGroupBox
	 */
	private ACollapsibleGroupBox getOuter() {
		return this;
	}
	/**
	 * Initializer for the class
	 * @param title - title of ACollapsibleGroupBox
	 */
	private void init(String title) {
		groupBoxListeners = new LinkedList<ACollapsibleGroupBoxListener>();
		
		currentState = ACollapsibleGroupBoxState.COLLAPSED;
		
		collapseInterval = 10;
		collapseDuration = 200;
		collapseTimer = new Timer(collapseInterval, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean finished = false;
				header.degree -= animationDegree;
				if(header.degree < 0)
					header.degree = 0;
				animationCurrentHeight -= animationHeight;
				if(animationCurrentHeight < 0){
					animationCurrentHeight = 0;
					collapseTimer.stop();
					currentState = ACollapsibleGroupBoxState.COLLAPSED;
					finished = true;
				}
				updateParent();
				if(finished) {
					ACollapsibleGroupBoxListener next;
					for (Iterator<ACollapsibleGroupBoxListener> iterator = groupBoxListeners.iterator(); iterator.hasNext();) {
						next = iterator.next();
						next.collapsed(getOuter());
					}
				}
			}
		});
		expandInterval = 10;
		expandDuration = 200;
		expandTimer = new Timer(expandInterval, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean finished = false;
				header.degree += animationDegree;
				if(header.degree > 90)
					header.degree = 90;
				animationCurrentHeight += animationHeight;
				if(animationCurrentHeight > animationTargetHeight) {
					animationCurrentHeight = animationTargetHeight;
					expandTimer.stop();
					currentState = ACollapsibleGroupBoxState.EXPANDED;
					finished = true;
				}
				updateParent();

				if(finished) {
					ACollapsibleGroupBoxListener next;
					for (Iterator<ACollapsibleGroupBoxListener> iterator = groupBoxListeners.iterator(); iterator.hasNext();) {
						next = iterator.next();
						next.expanded(getOuter());
					}
				}
			}
		});
		initUI(title);
	}
	/**
	 * Initializes the UI
	 * @param title - title of ACollapsibleGroupBox
	 */
	private void initUI(String title) {
		layout = new GridBagLayout();
		setLayout(layout);
		
		header = new ACollapsibleGroupBoxHeader(title, 18, new Insets(3, 5, 2, 0), new Insets(3, 3, 3, 2));
		header.setParent(this);
		contentPanel = new ACollapsibleGroupBoxContent(null, change(header.getBackground(), 20), new Insets(5, 15, 5, 5));
		contentPanel.setParent(this);
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.ipady = header.height;
		add(header, c);
		
		contentPanelConstraints = new GridBagConstraints();
		contentPanelConstraints.gridx = 0;
		contentPanelConstraints.gridy = 1;
		contentPanelConstraints.fill = GridBagConstraints.BOTH;
		contentPanelConstraints.anchor = GridBagConstraints.PAGE_START;
		contentPanelConstraints.ipady = 0;
		add(contentPanel, contentPanelConstraints);
		
		mouseListener = new ACollapsibleGroupBoxMouseListener();
		header.addMouseListener(mouseListener);
		contentPanel.addMouseListener(mouseListener);
		
		updateSize();
	}
	
	/**
	 * changes r, g and b values of the color
	 * @param color - color to change
	 * @param value - value for change
	 */
	private static Color change(Color color, int value) {
		return new Color(color.getRed() + value > 255 ? 255 : color.getRed() + value < 0 ? 0 : color.getRed() + value,
						 color.getRed() + value > 255 ? 255 : color.getRed() + value < 0 ? 0 : color.getRed() + value,
						 color.getRed() + value > 255 ? 255 : color.getRed() + value < 0 ? 0 : color.getRed() + value,
						 color.getAlpha());
	}
	
	@Override
	public void updateSize() {
		setPreferredSize(new Dimension(getPreferredSize().width, contentPanel.getHeight() + header.height));
		setMaximumSize(new Dimension(getMaximumSize().width, getPreferredSize().height));
		setSize(contentPanel.getPreferredSize());
		revalidate();
		repaint();
	}
	/**
	 * Updates the size with a given height for the content
	 * @param height - height of content
	 */
	private void updateSize(int height) {
		setPreferredSize(new Dimension(getPreferredSize().width, height + header.height));
		setMaximumSize(new Dimension(getMaximumSize().width, getPreferredSize().height));
		setSize(contentPanel.getPreferredSize());
		revalidate();
		repaint();
	}
	@Override
	public void updateParent() {
		if(currentState == ACollapsibleGroupBoxState.COLLAPSED) {
			animationCurrentHeight = 0;
		}else if(currentState == ACollapsibleGroupBoxState.EXPANDED) {
			animationCurrentHeight = getContentSize().height;
		}
		updateContentHeight((int) animationCurrentHeight);
		super.updateParent();
	}
	
	@Override
	public void updateContentHeight(int height) {
		contentPanelConstraints.ipady = height;
		layout.setConstraints(contentPanel, contentPanelConstraints);
		updateSize(height);
	}
	@Override
	public Dimension getContentSize() {
		return contentPanel.getPreferredSize();
	}

	@Override
	public void collapse() {
		collapse(collapseDuration);
	}
	@Override
	public void collapse(int duration) {
		if(currentState == ACollapsibleGroupBoxState.COLLAPSED)
			return;
		forceCollapse(duration);
	}
	@Override
	public void forceCollapse() {
		forceCollapse(collapseDuration);
	}
	@Override
	public void forceCollapse(int duration) {
		expandTimer.stop();
		if(duration <= 0) {
			currentState = ACollapsibleGroupBoxState.COLLAPSED;
			updateParent();
		}else {
			currentState = ACollapsibleGroupBoxState.COLLAPSING;
			animationDegree = (90 / 1000D) * collapseInterval * (1000D / duration);
			animationCurrentHeight = contentPanel.getHeight();
			animationHeight = ((contentPanel.getPreferredSize().height) / (1000D / collapseInterval)) * (1000D / duration);
			collapseTimer.start();
		}
	}
	
	@Override
	public void expand() {
		expand(expandDuration);
	}
	@Override
	public void expand(int duration) {
		if(currentState == ACollapsibleGroupBoxState.EXPANDED)
			return;
		forceExpand(duration);
	}
	@Override
	public void forceExpand() {
		forceExpand(expandDuration);
	}
	@Override
	public void forceExpand(int duration) {
		collapseTimer.stop();
		if(duration <= 0) {
			currentState = ACollapsibleGroupBoxState.EXPANDED;
			updateParent();
		}else {
			currentState = ACollapsibleGroupBoxState.EXPANDING;
			animationDegree = (90 / 1000D) * expandInterval * (1000D / duration);
			animationCurrentHeight = contentPanel.getHeight();
			animationTargetHeight = getContentSize().height;
			animationHeight = ((animationTargetHeight) / (1000D / expandInterval)) * (1000D / duration);
			expandTimer.start();
		}
	}
	
	@Override
	public void changeState() {
		if(currentState == ACollapsibleGroupBoxState.COLLAPSED || currentState == ACollapsibleGroupBoxState.COLLAPSING) {
			expand();
		}else if(currentState == ACollapsibleGroupBoxState.EXPANDED || currentState == ACollapsibleGroupBoxState.EXPANDING) {
			collapse();
		}
	}
	
	@Override
	public void addACollapsibleGroupBoxListener(ACollapsibleGroupBoxListener listener) {
		groupBoxListeners.add(listener);
	}
	@Override
	public boolean removeACollapsibleGroupBoxListener(ACollapsibleGroupBoxListener listener) {
		return groupBoxListeners.remove(listener);
	}
	
	@Override
	public void setContent(Component component) {
		contentPanel.setContent(component);
		updateParent();
	}
	/**
	 * Set the content of the header bar to the passed component and removes the old
	 * @param component - new content of header bar
	 */
	public void setHeaderContent(Component component) {
		header.setContent(component, new Insets(0, 0, 2, 0));
	}
	
	@Override
	public void setActive(boolean active) {
		header.active = active;
		repaint();
	}
	@Override
	public void setExpandable(boolean expandable) {
		header.expandable = expandable;
		if(!expandable)
			collapse(-1);
	}

	@Override
	public ACollapsibleGroupBoxState getCurrentState() {
		return currentState;
	}
	
	/**
	 * Mouse listener which handles mouse events at ACollapsibleGroupBox
	 * @author Alexander Muth
	 */
	private class ACollapsibleGroupBoxMouseListener implements MouseListener {
		@Override
		public void mouseClicked(MouseEvent e) {
			if(header.expandable)
				changeState();
		}
		@Override
		public void mousePressed(MouseEvent e) {}
		@Override
		public void mouseReleased(MouseEvent e) {}
		@Override
		public void mouseEntered(MouseEvent e) {
			if(header.expandable)
				setCursor(new Cursor(Cursor.HAND_CURSOR));
		}
		@Override
		public void mouseExited(MouseEvent e) {
			if(header.expandable)
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}
	/**
	 * The Header bar for ACollapsibleGroupBox, containing the title and an alternative extra widget
	 * @author Alexander Muth
	 */
	private class ACollapsibleGroupBoxHeader extends APanel {
		private static final long serialVersionUID = 4956177489732279119L;
		
		String title;
		int height;
		BasicStroke stroke;
		Insets fontInsets, arrowInsets;
		boolean active, expandable;
		Font font;
		FontMetrics metrics;
		Color fontColorActive, fontColorInactive;
		double degree;
		
		GridBagLayout layout;
		APanel padding;
		Component component;
		
		public ACollapsibleGroupBoxHeader(String title, int height, Insets fontInsets, Insets arrowInsets) {
			super();
			layout = new GridBagLayout();
			setLayout(layout);
			this.title = title;
			this.height = height;
			stroke = new BasicStroke(height / 10);
			this.fontInsets = fontInsets;
			this.arrowInsets = arrowInsets;
			active = expandable = true;
			degree = 0;
			
			font = new Font("Arial", Font.BOLD, height - fontInsets.bottom - fontInsets.top);
			metrics = getFontMetrics(font);
			
			fontColorActive = new Color(0, 0, 0, 255);
			fontColorInactive = new Color(fontColorActive.getRed(), fontColorActive.getGreen(), fontColorActive.getBlue(), 70);
			
			setBackground(new Color(210, 210, 210, 255));
			
			padding = new APanel();
			padding.setOpaque(false);
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = c.gridy = 0;
			c.weightx = 1;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.insets = new Insets(0, 0, 0, 0);
			add(padding, c);
			
			setPreferredSize(new Dimension(getPreferredSize().width, 0));
			setMaximumSize(new Dimension(getMaximumSize().width, 0));
			setMinimumSize(new Dimension(getMinimumSize().width, 0));
		}
		
		/**
		 * sets the content of the header bar
		 * @param component - content to add
		 * @param insets - insets of content
		 */
		public void setContent(Component component, Insets insets) {
			if(this.component != null)
				remove(this.component);
			if(component == null)
				return;
			this.component = component;
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 1;
			c.gridy = 0;
			c.fill = GridBagConstraints.BOTH;
			c.anchor = GridBagConstraints.FIRST_LINE_END;
			c.insets = insets;
			add(component, c);
			component.setBackground(getBackground());
			component.setMinimumSize(new Dimension(component.getMinimumSize().width, height));
			component.setPreferredSize(new Dimension(component.getPreferredSize().width, height));
			component.setMaximumSize(new Dimension(getMaximumSize().width, height));
			
			updateSize();
			updateParent();
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			
			int temp;
			g2d.setColor(active ? fontColorActive : fontColorInactive);
			g2d.setFont(font);
			temp = arrowInsets.left + arrowInsets.right + (height - fontInsets.top - fontInsets.bottom) + fontInsets.left;
			g2d.drawString(title, temp, (int)(height - metrics.getStringBounds(title, g2d).getHeight()) / 2 + metrics.getAscent());
			
			g2d.setStroke(stroke);
			temp = ((height - arrowInsets.top - arrowInsets.bottom) / 2);
			g2d.translate(arrowInsets.left + temp, arrowInsets.top + temp);
			g2d.rotate((degree) * (Math.PI/180));
			g2d.drawLine(-temp + arrowInsets.left, -temp + arrowInsets.top, temp - arrowInsets.right, 0);
			g2d.drawLine(-temp + arrowInsets.left, temp - arrowInsets.bottom, temp - arrowInsets.right, 0);
			
			g2d.rotate((-degree) * (Math.PI/180));
			g2d.translate(-arrowInsets.left - temp, -arrowInsets.top - temp);
		}
	}
	/**
	 * Panel which handels the content of the group box, with insets and preferred size
	 * @author Alexander Muth
	 */
	private class ACollapsibleGroupBoxContent extends APanel {
		private static final long serialVersionUID = 4400501240134488307L;
		
		Component component;
		Insets insets;
		
		public ACollapsibleGroupBoxContent(Component component, Color backgroundColor, Insets insets) {
			super();
			setLayout(new GridBagLayout());
			
			setContent(component);
			this.insets = insets;
			setBackground(backgroundColor);
			
			setMinimumSize(new Dimension(getMinimumSize().width, 0));
			setMaximumSize(new Dimension(getMaximumSize().width, 0));
			setPreferredSize(new Dimension(getPreferredSize().width, 0));
		}
		/**
		 * sets the content of the ACollapsibleGroupBox
		 * @param component - component to add
		 */
		public void setContent(Component component) {
			if(this.component != null)
				remove(this.component);
			if(component == null)
				return;
			if(component.getMouseListeners().length == 0)
				component.addMouseListener(new AEmptyMouseListener());
			this.component = component;
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = c.gridy = 0;
			c.weightx = c.weighty = 1;
			c.fill = GridBagConstraints.BOTH;
			c.insets = insets;
			add(component, c);
			
			updateSize();
			updateParent();
		}
		@Override
		public Dimension getPreferredSize() {
			if(component != null) {
				return new Dimension(insets.left + insets.right + component.getPreferredSize().width,
						 			 insets.top + insets.bottom + component.getPreferredSize().height);
			}
			return super.getPreferredSize();
		}
		/**
		 * Empty Mouse listener, which is necessary for the content panel. Without this listener a click at the content panel would change
		 * the state of the group box
		 * @author Alexander Muth
		 */
		private class AEmptyMouseListener implements MouseListener {
			@Override
			public void mouseClicked(MouseEvent e) {}
			@Override
			public void mousePressed(MouseEvent e) {}
			@Override
			public void mouseReleased(MouseEvent e) {}
			@Override
			public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseExited(MouseEvent e) {}
		}
	}	
}
