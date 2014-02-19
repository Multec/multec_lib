package be.multec.sg.nodes;

import java.awt.Rectangle;

import be.multec.sg.SGApp;

/**
 * @author Wouter Van den Broeck
 */
public class SGStage extends SGNode {
	
	// *********************************************************************************************
	// Attributes:
	// ---------------------------------------------------------------------------------------------
	
	// *********************************************************************************************
	// Constructors:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @param app
	 */
	public SGStage(SGApp app) {
		super(app);
		this.name = "stage for " + app.getClass().getSimpleName();
		isStage = true;
		onAddedToSG();
	}
	
	// *********************************************************************************************
	// SGApp Methods:
	// ---------------------------------------------------------------------------------------------
	
	/* @see be.multec.sg.SGNode#setCached(boolean) */
	public void setCached(boolean cached) {
		throw new Error("Do not use the cache on the stage.");
	}
	
	/* @see be.multec.sg.SGNode#cache() */
	public void cache() {
		throw new Error("Do not use the cache on the stage.");
	}
	
	/* @see be.multec.sg.SGNode#updateLocalBounds(java.awt.Rectangle) */
	@Override
	protected void updateLocalBounds(Rectangle localBounds) { /* no content */}
	
	// *********************************************************************************************
	// Processing Methods:
	// ---------------------------------------------------------------------------------------------
	
	// *********************************************************************************************
	// Accessors:
	// ---------------------------------------------------------------------------------------------
	
}
