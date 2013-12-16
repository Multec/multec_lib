package be.multec.sg.modifiers;

import be.multec.sg.SGNode;


/**
 * Interface for modifier classes. Modifier classes can be added to a SGNode. Their apply method is
 * called in the update traversal.
 * 
 * @author Wouter Van den Broeck
 */
public interface IModifier {
	
	/**
	 * This method is called during the update traversal. Provide an implementation that applies the
	 * modification.
	 * 
	 * @param node The node on which this modifier should be applied.
	 */
	void apply(SGNode node);
	
}
