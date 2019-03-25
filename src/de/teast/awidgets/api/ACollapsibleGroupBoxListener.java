package de.teast.awidgets.api;

/**
 * Listener for ACollapsibleGroupBox state changes
 * @author Alexander Muth
 */
public interface ACollapsibleGroupBoxListener {
	/**
	 * gets called when the collapsible group box was collapsed
	 */
	public void collapsed(ACollapsibleGroupBoxInterface groupBox);
	/**
	 * gets called when the collapsible group box was expanded
	 */
	public void expanded(ACollapsibleGroupBoxInterface groupBox);
}
