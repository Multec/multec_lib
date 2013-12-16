package be.multec.sg;

import processing.core.PApplet;

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
	public SGStage(PApplet app) {
		super(app);
		this.name = "stage for " + app.getClass().getSimpleName();
		isStage = true;
		onAddedToSG();
	}
	
	// *********************************************************************************************
	// Methods:
	// ---------------------------------------------------------------------------------------------
	
	/* @see be.multec.sg.SGNode#setCached(boolean) */
	public void setCached(boolean cached) {
		throw new Error("Do not use the cache on the stage.");
	}
	
	/* @see be.multec.sg.SGNode#cache() */
	public void cache() {
		throw new Error("Do not use the cache on the stage.");
	}
	
	// *********************************************************************************************
	// Accessors:
	// ---------------------------------------------------------------------------------------------
	
}
