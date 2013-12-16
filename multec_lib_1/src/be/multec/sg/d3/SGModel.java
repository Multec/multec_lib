package be.multec.sg.d3;

import processing.core.PGraphics;
import processing.core.PShape;
import be.multec.sg.SGApp;
import be.multec.sg.SGNode;

/**
 * A node that includes an SVG-file, a 3D-OBJ-file, or some other content represented by a PShape
 * object.
 * 
 * @author Wouter Van den Broeck
 */
public class SGModel extends SGNode {
	
	// *********************************************************************************************
	// Attributes:
	// ---------------------------------------------------------------------------------------------
	
	private PShape obj;
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @return the obj
	 */
	public PShape getObj() {
		return obj;
	}
	
	/**
	 * @param obj the obj to set
	 */
	public void setObj(PShape obj) {
		this.obj = obj;
		invalidateContent();
	}
	
	// *********************************************************************************************
	// Constructors:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @param obj The PShape object.
	 */
	public SGModel(SGApp app, PShape obj) {
		super(app);
		is3D = true;
		this.obj = obj;
	}
	
	// *********************************************************************************************
	// Methods:
	// ---------------------------------------------------------------------------------------------
	
	/* @see be.multec.sg.SGNode#draw(processing.core.PGraphics) */
	@Override
	protected void draw(PGraphics g) {
		g.shape(obj);
	}
	
}
