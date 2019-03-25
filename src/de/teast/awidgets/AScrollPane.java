package de.teast.awidgets;

import java.awt.Component;

import javax.swing.JScrollPane;

import de.teast.awidgets.api.AComponent;

/**
 * this is a scroll pane (JScrollPane), implementing the parent system of AComponent
 * @author Alexander Muth
 */
public class AScrollPane extends JScrollPane implements AComponent {
	private static final long serialVersionUID = -8199480767097368749L;
	
	private AComponent parent;
	
	/**
	 * standard constructor
	 */
	public AScrollPane() {
		super();
		updateParent();
	}
	/**
	 * @param view - component to add
	 */
	public AScrollPane(Component view) {
		super(view);
		if(view instanceof AComponent)
			((AComponent) view).setParent(this);
		updateParent();
	}
	/**
	 * @param vsbPolicy - vertical scroll bar policy
	 * @param hsbPolicy - horizontal scroll bar policy
	 */
	public AScrollPane(int vsbPolicy, int hsbPolicy) {
		super(vsbPolicy, hsbPolicy);
		updateParent();
	}
	/**
	 * @param view - component to add
	 * @param vsbPolicy - vertical scroll bar policy
	 * @param hsbPolicy - horizontal scroll bar policy
	 */
	public AScrollPane(Component view, int vsbPolicy, int hsbPolicy) {
		super(view, vsbPolicy, hsbPolicy);
		if(view instanceof AComponent)
			((AComponent) view).setParent(this);
		updateParent();
	}
	/**
	 * @param parent
	 * @param view - component to add
	 */
	public AScrollPane(AComponent parent, Component view) {
		super(view);
		if(view instanceof AComponent)
			((AComponent) view).setParent(this);
		this.parent = parent;
		updateParent();
	}
	/**
	 * @param parent
	 * @param view - component to add
	 * @param vsbPolicy - vertical scroll bar policy
	 * @param hsbPolicy - horizontal scroll bar policy
	 */
	public AScrollPane(AComponent parent, Component view, int vsbPolicy, int hsbPolicy) {
		super(view, vsbPolicy, hsbPolicy);
		if(view instanceof AComponent)
			((AComponent) view).setParent(this);
		this.parent = parent;
		updateParent();
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
	
}
