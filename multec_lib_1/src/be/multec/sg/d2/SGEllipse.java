package be.multec.sg.d2;

import java.awt.Color;
import java.awt.Rectangle;

import processing.core.PApplet;
import processing.core.PGraphics;
import be.multec.sg.SGFigure;

/**
 * A node that draws an ellipse.
 * 
 * @author Wouter Van den Broeck
 */
public class SGEllipse extends SGFigure {
	
	// *********************************************************************************************
	// Attributes:
	// ---------------------------------------------------------------------------------------------
	
	/* The diameter along the x-axis */
	private float diamX;
	
	/* The diameter along the y-axis */
	private float diamY;
	
	/* The x-component of the center. */
	private float centerX = 0;
	
	/* The y-component of the center. */
	private float centerY = 0;
	
	// *********************************************************************************************
	// Constructors:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @param app The scene-graph application object.
	 * @param diamX The horizontal diameter of the ellipse.
	 * @param diamY The vertical diameter of the ellipse.
	 * @param fillColor The fill color.
	 */
	public SGEllipse(PApplet app, float diamX, float diamY, Color fillColor) {
		super(app, fillColor);
		this.diamX = diamX;
		this.diamY = diamY;
		filled = true;
	}
	
	/**
	 * @param app The scene-graph application object.
	 * @param diam The diameter of the circular ellipse.
	 * @param fillColor The fill color.
	 */
	public SGEllipse(PApplet app, float diam, Color fillColor) {
		super(app, fillColor);
		this.diamX = this.diamY = diam;
		filled = true;
	}
	
	/**
	 * @param app The scene-graph application object.
	 * @param diamX The horizontal diameter of the ellipse.
	 * @param diamY The vertical diameter of the ellipse.
	 * @param fillColor
	 * @param strokeColor
	 * @param strokeWeight
	 */
	public SGEllipse(PApplet app, float diamX, float diamY, Color fillColor, Color strokeColor,
			int strokeWeight)
	{
		super(app, fillColor, strokeColor, strokeWeight);
		this.diamX = diamX;
		this.diamY = diamY;
		filled = true;
		stroked = true;
	}
	
	/**
	 * @param app The scene-graph application object.
	 * @param diam The diameter of the circular ellipse.
	 * @param fillColor
	 * @param strokeColor
	 * @param strokeWeight
	 */
	public SGEllipse(PApplet app, float diam, Color fillColor, Color strokeColor, int strokeWeight)
	{
		super(app, fillColor, strokeColor, strokeWeight);
		this.diamX = this.diamY = diam;
		filled = true;
		stroked = true;
	}
	
	// *********************************************************************************************
	// Accessors:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @return the diamX
	 */
	public float getDiamX() {
		return diamX;
	}
	
	/**
	 * @param diamX the diamX to set
	 * @return This node, allow you to chain this method.
	 */
	public SGEllipse setDiamX(float diamX) {
		if (this.diamX == diamX) return this;
		this.diamX = diamX;
		invalidateBounds(true);
		invalidateContent();
		return this;
	}
	
	/**
	 * @return the diamY
	 */
	public float getDiamY() {
		return diamY;
	}
	
	/**
	 * @param diamY the diamY to set
	 * @return This node, allow you to chain this method.
	 */
	public SGEllipse setDiamY(float diamY) {
		if (this.diamY == diamY) return this;
		this.diamY = diamY;
		invalidateBounds(true);
		invalidateContent();
		return this;
	}
	
	/**
	 * Sets the diameter of the ellipse.
	 * 
	 * @param diamX
	 * @param diamY
	 * @return This node, allow you to chain this method.
	 */
	public SGEllipse setDiam(float diamX, float diamY) {
		if (this.diamX == diamX && this.diamY == diamY) return this;
		this.diamX = diamX;
		this.diamY = diamY;
		invalidateBounds(true);
		invalidateContent();
		return this;
	}
	
	/**
	 * Sets the diameter of the ellipse.
	 * 
	 * @param diam The uniform diameter.
	 * @return This node, allow you to chain this method.
	 */
	public SGEllipse setDiam(float diam) {
		if (this.diamX == diam && this.diamY == diam) return this;
		this.diamX = diam;
		this.diamY = diam;
		invalidateBounds(true);
		invalidateContent();
		return this;
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @return the centerX
	 */
	public float getCenterX() {
		return centerX;
	}
	
	/**
	 * @param centerX the centerX to set
	 * @return This node, allow you to chain this method.
	 */
	public SGEllipse setCenterX(float centerX) {
		if (this.centerX == centerX) return this;
		this.centerX = centerX;
		invalidateBounds(true);
		invalidateContent();
		return this;
	}
	
	/**
	 * @return the centerY
	 */
	public float getCenterY() {
		return centerY;
	}
	
	/**
	 * @param centerY the centerY to set
	 * @return This node, allow you to chain this method.
	 */
	public SGEllipse setCenterY(float centerY) {
		if (this.centerY == centerY) return this;
		this.centerY = centerY;
		invalidateBounds(true);
		invalidateContent();
		return this;
	}
	
	/**
	 * @param centerY the centerY to set
	 * @return This node, allow you to chain this method.
	 */
	public SGEllipse setCenter(float centerX, float centerY) {
		if (this.centerX == centerX && this.centerY == centerY) return this;
		this.centerX = centerX;
		this.centerY = centerY;
		invalidateBounds(true);
		invalidateContent();
		return this;
	}
	
	// *********************************************************************************************
	// SGApp system methods:
	// ---------------------------------------------------------------------------------------------
	
	/* @see be.multec.sg.d2.SG2DNode#draw(processing.core.PGraphics) */
	@Override
	protected void draw(PGraphics g) {
		g.ellipse(centerX, centerY, diamX, diamY);
	}
	
	/* @see be.multec.sg.SGNode#updateLocalBounds(java.awt.Rectangle) */
	@Override
	protected void updateLocalBounds(Rectangle bounds) {
		if (stroked) {
			bounds.x = (int) Math.floor(-(diamX + strokeWeight) / 2);
			bounds.y = (int) Math.floor(-(diamY + strokeWeight) / 2);
			bounds.width = (int) Math.ceil(diamX + strokeWeight);
			bounds.height = (int) Math.ceil(diamY + strokeWeight);
		}
		else {
			bounds.x = bounds.y = 0;
			bounds.width = (int) Math.ceil(diamX);
			bounds.height = (int) Math.ceil(diamY);
		}
	}
	
}
