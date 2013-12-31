package be.multec.sg.d2;

import java.awt.Color;
import java.awt.Rectangle;

import processing.core.PGraphics;
import be.multec.sg.SGApp;
import be.multec.sg.SGFigure;

/**
 * @author Wouter Van den Broeck
 */
public class SGRect extends SGFigure {
	
	// *********************************************************************************************
	// Attributes:
	// ---------------------------------------------------------------------------------------------
	
	/* The width of this node. */
	private float rectWidth = 0;
	
	/* The height of this node. */
	private float rectHeight = 0;
	
	/*
	 * The x-coordinate of the center of this node with respect to the origin (the x/y position) of
	 * this node.
	 */
	private float centerX = 0;
	
	/*
	 * The y-coordinate of the center of this node with respect to the origin (the x/y position) of
	 * this node.
	 */
	private float centerY = 0;
	
	// *********************************************************************************************
	// Constructors:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @param app The scene-graph application object.
	 * @param width
	 * @param height
	 */
	public SGRect(SGApp app, float width, float height) {
		super(app);
		initRect(width, height);
	}
	
	/**
	 * @param app The scene-graph application object.
	 * @param width
	 * @param height
	 * @param fillColor
	 */
	public SGRect(SGApp app, float width, float height, Color fillColor) {
		super(app, fillColor);
		initRect(width, height);
	}
	
	/**
	 * @param app The scene-graph application object.
	 * @param width
	 * @param height
	 * @param fillColor
	 * @param strokeColor
	 * @param strokeWeight
	 */
	public SGRect(SGApp app, float width, float height, Color fillColor, Color strokeColor,
			int strokeWeight)
	{
		super(app, fillColor, strokeColor, strokeWeight);
		initRect(width, height);
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/* Common initialization. */
	private void initRect(float width, float height) {
		is3D = false;
		this.rectWidth = width;
		this.rectHeight = height;
		centerX = width / 2;
		centerY = height / 2;
	}
	
	/* @see be.multec.sg.SGNode#dispose(boolean) */
	@Override
	public void dispose(boolean traverse) {
		rectWidth = rectHeight = centerX = centerY = 0;
		super.dispose(traverse);
	}
	
	// *********************************************************************************************
	// Accessors:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @param width the width to set
	 */
	public void setWidth(float width) {
		if (this.rectWidth == width) return;
		this.rectWidth = width;
		centerX = width / 2;
		invalidateLocalBounds();
		redraw("SGRect.setWidth(" + width + ") [" + this + "]");
	}
	
	/**
	 * @param height the height to set
	 */
	public void setHeight(float height) {
		if (this.rectHeight == height) return;
		this.rectHeight = height;
		centerY = height / 2;
		invalidateLocalBounds();
		redraw("SGRect.setHeight(" + height + ") [" + this + "]");
	}
	
	/**
	 * @return the rectWidth
	 */
	public float getRectWidth() {
		return rectWidth;
	}
	
	/**
	 * @return the rectHeight
	 */
	public float getRectHeight() {
		return rectHeight;
	}
	
	/**
	 * Sets the size of the rectangle.
	 * 
	 * @param width
	 * @param height
	 * @return The box object.
	 */
	public SGRect setSize(float width, float height) {
		if (this.rectWidth == width && this.rectHeight == height) return this;
		this.rectWidth = width;
		this.rectHeight = height;
		centerX = width / 2;
		centerY = height / 2;
		invalidateLocalBounds();
		redraw("SGRect.setSize() [" + this + "]");
		return this;
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @return The x-coordinate of the center of this node with respect to the origin (the x/y
	 *         position) of this node.
	 */
	public float getCenterX() {
		return centerX;
	}
	
	/**
	 * @return The y-coordinate of the center of this node with respect to the origin (the x/y
	 *         position) of this node.
	 */
	public float getCenterY() {
		return centerY;
	}
	
	// *********************************************************************************************
	// Methods:
	// ---------------------------------------------------------------------------------------------
	
	// public void fill(String path) {
	// drawFillImage = false;
	// fillImage = app.loadImage(path);
	// }
	
	// public void fill(PImage image) {
	// drawFillImage = false;
	// fillImage = image;
	// }
	
	// ---------------------------------------------------------------------------------------------
	
	/* @see be.multec.sg.d2.SG2DNode#draw(processing.core.PGraphics) */
	@Override
	protected void draw(PGraphics g) {
		// println(">> SGRect[" + this + "].draw()");
		g.rectMode(CORNER);
		g.rect(0, 0, rectWidth, rectHeight);
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/* @see be.multec.sg.SGNode#mouseHitTest() */
	@Override
	protected boolean contains(float x, float y) {
		return x >= 0 && x < rectWidth && y >= 0 && y < rectHeight;
	}
	
	// *********************************************************************************************
	// SGApp system methods:
	// ---------------------------------------------------------------------------------------------
	
	/* @see be.multec.sg.SGNode#updateLocalBounds(java.awt.Rectangle) */
	@Override
	protected void updateLocalBounds(Rectangle bounds) {
		if (stroked()) {
			bounds.x = bounds.y = (int) Math.floor(-strokeWeight() / 2);
			bounds.width = (int) Math.ceil(rectWidth + strokeWeight());
			bounds.height = (int) Math.ceil(rectHeight + strokeWeight());
		}
		else {
			bounds.x = bounds.y = 0;
			bounds.width = (int) Math.ceil(rectWidth);
			bounds.height = (int) Math.ceil(rectHeight);
		}
	}
	
}
