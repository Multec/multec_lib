package be.multec.sg.eventHandlers;

import be.multec.sg.nodes.SGNode;
import processing.core.PVector;

/**
 * Base implementation for mouse-event handlers dispatched by an SGNode object.
 * 
 * @see SGNode#addMouseEventHandler(SGMouseEventHandler)
 * 
 * @author Wouter Van den Broeck
 */
public abstract class SGMouseEventHandler {
	
	// *********************************************************************************************
	// Methods:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * Called when the mouse was clicked while the cursor was over this node's shape.
	 * 
	 * @param node The node for which the mouse-event is dispatched.
	 * @param mousePosition The position of the mouse in the local coordinate system.
	 */
	public void mouseClicked(SGNode node, PVector mousePosition) {}
	
	/**
	 * Called when the mouse-button was pressed while the cursor was over this node's shape.
	 * 
	 * @param node The node for which the mouse-event is dispatched.
	 * @param mousePosition The position of the mouse in the local coordinate system.
	 */
	public void mousePressed(SGNode node, PVector mousePosition) {}
	
	/**
	 * Called when the mouse-button was released after it was pressed while the cursor was over this
	 * node's shape.
	 * 
	 * @param node The node for which the mouse-event is dispatched.
	 * @param mousePosition The position of the mouse in the local coordinate system.
	 */
	public void mouseReleased(SGNode node, PVector mousePosition) {}
	
	/**
	 * Called when the cursor moved onto this node's shape.
	 * 
	 * @param node The node for which the mouse-event is dispatched.
	 * @param mousePosition The position of the mouse in the local coordinate system.
	 * @param dragged True when the mouse-button was pressed while the mouse moved over the node.
	 */
	public void mouseOver(SGNode node, PVector mousePosition, boolean dragged) {}
	
	/**
	 * Called when the cursor moved out of this node's shape.
	 * 
	 * @param node The node for which the mouse-event is dispatched.
	 * @param mousePosition The position of the mouse in the local coordinate system.
	 * @param dragged True when the mouse-button was pressed while the mouse moved over the node.
	 */
	public void mouseOut(SGNode node, PVector mousePosition, boolean dragged) {}
	
	/**
	 * Called when the cursor moved over of this node's shape.
	 * 
	 * @param node The node for which the mouse-event is dispatched.
	 * @param mousePosition The position of the mouse in the local coordinate system.
	 * @param dragged True when the mouse-button was pressed while the mouse moved over the node.
	 */
	public void mouseMoved(SGNode node, PVector mousePosition, boolean dragged) {}
	
}
