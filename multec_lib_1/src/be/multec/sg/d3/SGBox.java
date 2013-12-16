package be.multec.sg.d3;

import java.awt.Color;

import processing.core.PGraphics;
import be.multec.sg.SGApp;
import be.multec.sg.SGFigure;

/**
 * A node that includes a 3D-box in a 3D-graph.
 * 
 * @author Wouter Van den Broeck
 */
public class SGBox extends SGFigure {
	
	// *********************************************************************************************
	// Attributes:
	// ---------------------------------------------------------------------------------------------
	
	private float width;
	private float height;
	private float depth;
	
	// *********************************************************************************************
	// Constructors:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @param app The scene-graph application object.
	 * @param width
	 * @param height
	 * @param depth
	 * @param fillColor The fill color.
	 */
	public SGBox(SGApp app, float width, float height, float depth, Color fillColor) {
		super(app, fillColor);
		is3D = true;
		this.width = width;
		this.height = height;
		this.depth = depth;
	}
	
	/**
	 * @param app The scene-graph application object.
	 * @param width
	 * @param height
	 * @param depth
	 * @param fillColor The fill color.
	 * @param strokeColor The stroke color.
	 * @param strokeWeight The thickness of the stroke.
	 */
	public SGBox(SGApp app, float width, float height, float depth, Color fillColor,
			Color strokeColor, float strokeWeight)
	{
		super(app, fillColor, strokeColor, strokeWeight);
		is3D = true;
		this.width = width;
		this.height = height;
		this.depth = depth;
	}
	
	// *********************************************************************************************
	// Methods:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * Sets the size of the box.
	 * 
	 * @param width
	 * @param height
	 * @param depth
	 * @return The box object.
	 */
	public SGBox setSize(float width, float height, float depth) {
		this.width = width;
		this.height = height;
		this.depth = depth;
		invalidateContent();
		return this;
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/* @see be.multec.sg.SGNode#draw(processing.core.PGraphics) */
	@Override
	public void draw(PGraphics g) {
		g.box(width, height, depth);
	}
	
	// *********************************************************************************************
	// Accessors:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @return the width
	 */
	public float getWidth_TODO() {
		return width;
	}
	
	/**
	 * @param width the width to set
	 */
	public void setWidth(float width) {
		this.width = width;
		invalidateContent();
	}
	
	/**
	 * @return the height
	 */
	public float getHeight_TODO() {
		return height;
	}
	
	/**
	 * @param height the height to set
	 */
	public void setHeight(float height) {
		this.height = height;
		invalidateContent();
	}
	
	/**
	 * @return the depth
	 */
	public float getDepth() {
		return depth;
	}
	
	/**
	 * @param depth the depth to set
	 */
	public void setDepth(float depth) {
		this.depth = depth;
		invalidateContent();
	}
	
}
