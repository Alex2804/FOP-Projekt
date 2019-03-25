package de.teast.awidgets.api;

import java.awt.Component;
import java.awt.Dimension;

/**
 * this interface ensures, that there is a parent system. With this parent system, a widget should be able to update the
 * whole parent tree, and revalidates all necessary components, if there are dynamic changes in the layout (like ACollapsibleGroupBox which is
 * the reason for this implementation because swing is bullshit...)
 * Inspired by Qt but in not very flexible or smart and for other purposes. (I love Qt <3)
 * @author Alexander Muth
 */
public interface AComponent{
	/**
	 * Sets the parent of AComponent to the passed component
	 * @param parent - new parent
	 */
	public void setParent(AComponent parent);
	/**
	 * @return the parent of the widget
	 */
	public AComponent parent();
	/**
	 * Updates the size of AComponent depending on its content
	 */
	public void updateSize();
	/**
	 * Updates the parent or itself if there is no parent
	 */
	public void updateParent();
	/**
	 * adds the component to the actual layout and sets it parent to this
	 * @param comp - component to add
	 * @return the added component
	 */
	public Component add(Component comp);
	/**
	 * adds the component to the actual layout, with the passed constraints and sets it parent to this
	 * @param comp - component to add
	 * @param constraints - constraints for the layout
	 */
	public void add(Component comp, Object constraints);
	
	/**
	 * @return the current size
	 */
	public Dimension getSize();
}
