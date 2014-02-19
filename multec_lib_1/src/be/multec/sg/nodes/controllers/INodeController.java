package be.multec.sg.nodes.controllers;

import be.multec.sg.nodes.SGNode;

/**
 * Interface for modifier classes. Modifier classes can be added to a SGNode. Their apply method is
 * called in the update traversal.
 * 
 * @author Wouter Van den Broeck
 */
public interface INodeController {
	
	/**
	 * This method is called during the update traversal.
	 * 
	 * @param node The node on which this controller should be applied.
	 */
	void apply(SGNode node);
	
}
