package be.multec.sg;

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
	 */
	protected void mouseClicked(SGNode node) {}
	
	/**
	 * Called when the cursor moved onto this node's shape.
	 *
	 * @param node The node for which the mouse-event is dispatched.
	 */
	protected void mouseOver(SGNode node) {}
	
	/**
	 * Called when the cursor moved out of this node's shape.
	 *
	 * @param node The node for which the mouse-event is dispatched.
	 */
	protected void mouseOut(SGNode node) {}
	
	/**
	 * Called when the cursor moved over of this node's shape.
	 *
	 * @param node The node for which the mouse-event is dispatched.
	 */
	protected void mouseMoved(SGNode node) {}
	
	/**
	 * Called when the mouse-button was pressed while the cursor was over this node's shape.
	 *
	 * @param node The node for which the mouse-event is dispatched.
	 */
	protected void mousePressed(SGNode node) {}
	
	/**
	 * Called when the mouse-button was released after it was pressed while the cursor was over this
	 * node's shape.
	 *
	 * @param node The node for which the mouse-event is dispatched.
	 */
	protected void mouseReleased(SGNode node) {}

	/**
	 * TODO
	 * 
	 * @param node
	 */
	protected void mouseDragged(SGNode node) {}
	
}
