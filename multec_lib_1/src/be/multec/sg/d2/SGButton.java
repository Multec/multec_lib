package be.multec.sg.d2;

import java.awt.Color;

import processing.core.PGraphics;
import processing.core.PImage;
import be.multec.sg.SGApp;

/**
 * A node that includes a button. Mouse events can be captured by overriding the appropriate methods
 * declared in SGNode (mouseOver, mouseClicked, etc.), or by adding an event-handler, @see
 * SGNode#addEventHandler(SGMouseEventHandler).
 * 
 * Note: The implementation of this class is not stable yet.
 * 
 * @author Wouter Van den Broeck
 */
public class SGButton extends SGRect {
	
	// *********************************************************************************************
	// Attributes:
	// ---------------------------------------------------------------------------------------------
	
	private Color overColor;
	
	private Color outColor;
	
	private boolean showIcon = false;
	
	private PImage icon;
	
	// *********************************************************************************************
	// Constructors:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @param app
	 * @param width
	 * @param height
	 * @param outColor
	 * @param overColor
	 */
	public SGButton(SGApp app, float width, float height, Color outColor, Color overColor) {
		super(app, width, height, outColor);
		
		this.outColor = outColor;
		this.overColor = overColor;
		enableMouseEvents();
	}
	
	/**
	 * @param app
	 * @param width
	 * @param height
	 * @param outColor
	 * @param overColor
	 * @param image
	 */
	public SGButton(SGApp app, float width, float height, Color outColor, Color overColor,
			PImage image)
	{
		super(app, width, height, outColor);
		
		this.outColor = outColor;
		this.overColor = overColor;
		this.icon = image;
		showIcon = true;
		enableMouseEvents();
	}
	
	/**
	 * @param app
	 * @param width
	 * @param height
	 * @param outColor
	 * @param overColor
	 * @param path The path to the image.
	 */
	public SGButton(SGApp app, float width, float height, Color outColor, Color overColor,
			String path)
	{
		super(app, width, height, outColor);
		
		this.outColor = outColor;
		this.overColor = overColor;
		this.icon = app.loadImage(path);
		showIcon = true;
		enableMouseEvents();
	}
	
	// *********************************************************************************************
	// Methods:
	// ---------------------------------------------------------------------------------------------
	
	public SGButton setIcon(String path) {
		this.icon = app.loadImage(path);
		showIcon = true;
		invalidateContent();
		return this;
	}
	
	public SGButton setIcon(PImage icon) {
		if (this.icon != icon) {
			this.icon = icon;
			showIcon = true;
			invalidateContent();
		}
		return this;
	}
	
	// ---------------------------------------------------------------------------------------------
	
	@Override
	protected void mouseOver() {
		fill(overColor);
	}
	
	@Override
	protected void mouseOut() {
		fill(outColor);
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/* @see be.multec.sg.d2.SGRect#draw(processing.core.PGraphics) */
	@Override
	protected void draw(PGraphics g) {
		super.draw(g);
		if (showIcon) {
			g.imageMode(CENTER);
			g.image(icon, getCenterX(), getCenterY());
		}
	}
	
}
