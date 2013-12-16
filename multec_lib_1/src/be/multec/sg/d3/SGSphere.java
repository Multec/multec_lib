package be.multec.sg.d3;

import java.awt.Color;

import processing.core.PGraphics;
import be.multec.sg.SGApp;
import be.multec.sg.SGFigure;

/**
 * @author Wouter Van den Broeck
 */
public class SGSphere extends SGFigure {
	
	// *********************************************************************************************
	// Attributes:
	// ---------------------------------------------------------------------------------------------
	
	/** The radius of the sphere. */
	private float radius;
	
	/** The number of segments (minimum 3) used per full circle revolution. */
	private int detail = 30;
	
	// *********************************************************************************************
	// Constructors:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @param app The scene-graph application object.
	 * @param radius The radius of the sphere.
	 * @param fillColor The fill color.
	 */
	public SGSphere(SGApp app, float radius, Color fillColor) {
		super(app, fillColor);
		initSphere(radius);
	}
	
	/**
	 * @param app The scene-graph application object.
	 * @param radius The radius of the sphere.
	 * @param detail The number of segments (minimum 3) used per full circle revolution.
	 * @param fillColor The fill color.
	 */
	public SGSphere(SGApp app, float radius, int detail, Color fillColor) {
		super(app, fillColor);
		initSphere(radius);
		this.detail = detail;
	}
	
	/**
	 * @param app The scene-graph application object.
	 * @param radius The radius of the sphere.
	 * @param fillColor The fill color.
	 * @param strokeColor The stroke color.
	 * @param strokeWeight The thickness of the stroke.
	 */
	public SGSphere(SGApp app, float radius, Color fillColor, Color strokeColor, int strokeWeight) {
		super(app, fillColor, strokeColor, strokeWeight);
		initSphere(radius);
	}
	
	/**
	 * @param app The scene-graph application object.
	 * @param radius The radius of the sphere.
	 * @param detail The number of segments (minimum 3) used per full circle revolution.
	 * @param fillColor The fill color.
	 * @param strokeColor The stroke color.
	 * @param strokeWeight The thickness of the stroke.
	 */
	public SGSphere(SGApp app, float radius, int detail, Color fillColor, Color strokeColor,
			int strokeWeight)
	{
		super(app, fillColor, strokeColor, strokeWeight);
		initSphere(radius);
		this.detail = detail;
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/* Common initialization. */
	private void initSphere(float radius) {
		is3D = true;
		this.radius = radius;
	}
	
	// *********************************************************************************************
	// Methods:
	// ---------------------------------------------------------------------------------------------
	
	/* @see be.multec.sg.SGNode#draw(processing.core.PGraphics) */
	@Override
	public void draw(PGraphics g) {
		g.sphereDetail(detail);
		g.sphere(radius);
	}

	// *********************************************************************************************
	// Accessors:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @return The radius of the sphere.
	 */
	public float getRadius() {
		return radius;
	}
	
	/**
	 * @param radius The radius of the sphere.
	 */
	public void setRadius(float radius) {
		this.radius = radius;
		invalidateContent();
	}
	
	/**
	 * @return The number of segments (minimum 3) used per full circle revolution.
	 */
	public int getDetail() {
		return detail;
	}
	
	/**
	 * @param detail The number of segments (minimum 3) used per full circle revolution.
	 */
	public void setDetail(int detail) {
		this.detail = detail;
		invalidateContent();
	}
	
}
