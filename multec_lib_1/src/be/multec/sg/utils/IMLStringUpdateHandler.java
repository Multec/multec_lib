package be.multec.sg.utils;

/**
 * A handler of MLString update events.
 * 
 * @author Wouter Van den Broeck
 */
public interface IMLStringUpdateHandler {
	
	/**
	 * Called by the MLString object to which this handler was set as the update-handler, when the
	 * string was updated.
	 * 
	 * @param mlString The MLString object that was updated.
	 */
	void stringUpdated(MLString mlString);
	
}
