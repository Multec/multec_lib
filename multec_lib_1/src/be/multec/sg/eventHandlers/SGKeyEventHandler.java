package be.multec.sg.eventHandlers;

import be.multec.sg.nodes.SGNode;
import processing.event.KeyEvent;

/**
 * Base implementation for key-event handlers dispatched by an SGNode object.
 * 
 * @see SGNode#addKeyEventHandler(SGKeyEventHandler)
 * 
 * @author Wouter Van den Broeck
 */
public abstract class SGKeyEventHandler {
	
	// *********************************************************************************************
	// Methods:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * Called once every time a key is pressed, but action keys such as Ctrl, Shift, and Alt are
	 * ignored.
	 * 
	 * Because of how operating systems handle key repeats, holding down a key will cause multiple
	 * calls to keyTyped(), the rate is set by the operating system and how each computer is
	 * configured.
	 * 
	 * @param node The node for which the key-event is dispatched.
	 * @param event The Processing key event.
	 */
	public void keyTyped(SGNode node, KeyEvent event) {}
	
	/**
	 * Called once every time a key is pressed.
	 * 
	 * Because of how operating systems handle key repeats, holding down a key may cause multiple
	 * calls to keyPressed() (and keyReleased() as well). The rate of repeat is set by the operating
	 * system and how each computer is configured.
	 * 
	 * @param node The node for which the key-event is dispatched.
	 * @param event The Processing key event.
	 */
	public void keyPressed(SGNode node, KeyEvent event) {}
	
	/**
	 * Called once every time a key is released. The key that was released will be stored in the key
	 * variable.
	 * 
	 * @param node The node for which the key-event is dispatched.
	 * @param event The Processing key event.
	 */
	public void keyReleased(SGNode node, KeyEvent event) {}
	
}
