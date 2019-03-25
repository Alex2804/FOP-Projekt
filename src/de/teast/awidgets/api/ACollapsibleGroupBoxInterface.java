package de.teast.awidgets.api;

import java.awt.Component;
import java.awt.Dimension;

/**
 * All methods of the API of ACollapsibleGroupBox
 * @author Alexander Muth
 */
public interface ACollapsibleGroupBoxInterface extends AComponent {
	/**
	 * sets the ipady of the content panel in the GridBagLayout to the current height (and with it the height of the content panel)
	 * @param height - new height of content panel
	 */
	public void updateContentHeight(int height);
	/**
	 * @return the preferred size of the content panel, including insets
	 */
	public Dimension getContentSize();
	
	/**
	 * starts the collapse animation with standard duration
	 */
	public void collapse();
	/**
	 * starts the collapse animation with the passed duration
	 * @param duration - animation duration
	 */
	public void collapse(int duration);
	/**
	 * starts the collapse animation with the standard duration no matter if the group box is already collapsed
	 */
	public void forceCollapse();
	/**
	 * starts the collapse animation with the passed duration no matter if the group box is already collapsed
	 * @param duration - animation duration
	 */
	public void forceCollapse(int duration);
	/**
	 * starts the expand animation with standard duration
	 */
	public void expand();
	/**
	 * starts the expand animation with passed duration
	 * @param duration - animation duration
	 */
	public void expand(int duration);
	/**
	 * starts the expand animation with standard duration no matter if the group box is already expanded
	 */
	public void forceExpand();
	/**
	 * starts the expand animation with the passed duration no matter if the group box is already expanded
	 * @param duration - animation duration
	 */
	public void forceExpand(int duration);
	/**
	 * changes the state of the panel and expand/collapse it depending on the current state
	 */
	public void changeState();
	
	/**
	 * adds the new component as content and removes the old
	 * If Component implements AComponent, the parent is set to the content panel of this ACollapsibleGroupBox
	 * @param component
	 */
	public void setContent(Component component);
	
	/**
	 * changes if the panel is active (color of the header)
	 * @param active
	 */
	public void setActive(boolean active);
	/**
	 * changes if the panel is expandable
	 * @param expandable
	 */
	public void setExpandable(boolean expandable);
	/**
	 * @return the current state
	 */
	public ACollapsibleGroupBoxState getCurrentState();
	/**
	 * @param listener - listener to add
	 */
	public void addACollapsibleGroupBoxListener(ACollapsibleGroupBoxListener listener);
	/**
	 * @param listener - listener to remove
	 * @return if the listener existed
	 */
	public boolean removeACollapsibleGroupBoxListener(ACollapsibleGroupBoxListener listener);
}
