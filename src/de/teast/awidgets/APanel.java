package de.teast.awidgets;

import java.awt.Component;

import javax.swing.JPanel;

import de.teast.awidgets.api.AComponent;

/**
 * This is a panel (JPanel), including the parent system of AComponent
 * @author alexander
 */
public class APanel extends JPanel implements AComponent{
	private static final long serialVersionUID = -1849344928808625479L;
	
	private AComponent parent = null;
	
	/**
	 * standard constructor
	 */
	public APanel() {
		super();
	}
	/**
	 * @param parent
	 */
	public APanel(AComponent parent) {
		super();
		this.parent = parent;
	}

	@Override
	public void setParent(AComponent parent) {
		this.parent = parent;
	}

	@Override
	public AComponent parent() {
		return parent;
	}

	@Override
	public void updateSize() {
		revalidate();
		repaint();
	}
	
	@Override
	public void updateParent() {
		if(parent == null) {
			updateSize();
		}else {
			parent.updateParent();
		}
	}
	
	@Override
	public Component add(Component comp) {
		if(comp instanceof AComponent)
			((AComponent)comp).setParent(this);
		return super.add(comp);
	}
	@Override
	public void add(Component comp, Object constraints) {
		if(comp instanceof AComponent)
			((AComponent)comp).setParent(this);
		super.add(comp, constraints);
	}
}
