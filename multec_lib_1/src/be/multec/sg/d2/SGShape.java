package be.multec.sg.d2;

import java.awt.Color;
import java.awt.Rectangle;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PShape;
import be.multec.sg.SGFigure;

/**
 * A node that draws a vector shape.
 * 
 * <h2>Notes</h2>
 * <ul>
 * <li>The sourceBounds should be defined according to the viewBox as it is defined in the
 * SVG-file./li>
 * </ul>
 * 
 * @author Wouter Van den Broeck
 */
public class SGShape extends SGFigure {
	
	// *********************************************************************************************
	// Attributes:
	// ---------------------------------------------------------------------------------------------
	
	/* The shape object. */
	private PShape shape;
	
	/* The explicitly set width. */
	// private float explicitWidth = 0;
	
	/* The explicitly set height. */
	// private float explicitHeight = 0;
	
	/* The target width of the drawn shape. */
	// protected float targetWidth = 0;
	
	/* The target height of the drawn shape. */
	// protected float targetHeight = 0;
	
	protected Rectangle sourceBounds;
	
	protected Rectangle targetBounds;
	
	/* True when the shape must be drawn with custom fill & stroke. */
	private boolean customStyle = false;
	
	/* True when the shape changed. This is set with invalidateShape(). */
	private boolean shapeChanged = true;
	
	// *********************************************************************************************
	// Constructors:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * A new PShape object is constructed when you use this constructor. Override the createShape
	 * method if you want to customize the construction of the PShape object. After the creation of
	 * the PShape object, the drawShape method is called to actually draw the content of the shape.
	 * 
	 * @see createShape
	 * @see drawShape
	 * 
	 * @param app The scene-graph application object.
	 */
	public SGShape(PApplet app) {
		super(app);
		
		if (isJAVA2D(app.g))
			throw new Error("It is currently not possible to create a PShape in the default"
					+ " JAVA2D renderer. You can load an SVG-file instead, or use P2D"
					+ " or P3D as renderer by means of the SGWindow.setRenderer() method. "
					+ "[in SGShape(PApplet) - " + name + "]");
		
		PShape shape = createShape();
		if (shape == null)
			throw new Error("The createShape() method implementation for the class "
					+ getClass().getName() + " did return null instead of a new PShape object. "
					+ "[in SGShape(PApplet) - " + name + "]");
		
		this.sourceBounds = new Rectangle(0, 0, 100, 100);
		this.targetBounds = new Rectangle(0, 0, 100, 100);
		drawShape(shape);
		checkShape(shape);
		this.shape = shape;
		updateSize();
	}
	
	/**
	 * @param app The scene-graph application object.
	 * @param shape The shape object to draw in this node.
	 * @param sourceBounds The bounds in the source shape from which to select the content to draw.
	 *            These bounds should be defined according to the viewBox as it is defined in the
	 *            shape (or SVG-file). The target bounds are equal to the source bounds.
	 */
	public SGShape(PApplet app, PShape shape, Rectangle sourceBounds) {
		super(app);
		checkShape(shape);
		this.sourceBounds = sourceBounds;
		this.targetBounds = (Rectangle) sourceBounds.clone();
		this.shape = shape;
		updateSize();
	}
	
	/**
	 * @param app The scene-graph application object.
	 * @param shape The shape object to draw in this node.
	 * @param sourceBounds The bounds in the source shape from which to select the content to draw.
	 *            The sourceBounds should be defined according to the viewBox as it is defined in
	 *            the shape (or SVG-file).
	 * @param targetBounds The bounds in which the shape content should be drawn. These bounds
	 *            should be defined according to the local origin in this node.
	 */
	public SGShape(PApplet app, PShape shape, Rectangle sourceBounds, Rectangle targetBounds) {
		super(app);
		checkShape(shape);
		this.sourceBounds = sourceBounds;
		this.targetBounds = targetBounds;
		this.shape = shape;
		updateSize();
	}
	
	/**
	 * Use this constructor to load a shape from an SVG-file.
	 * 
	 * @param app The scene-graph application object.
	 * @param path The path to the SVG to load.
	 * @param sourceBounds The bounds in the source shape from which to select the content to draw.
	 *            The sourceBounds should be defined according to the viewBox as it is defined in
	 *            the shape (or SVG-file).
	 * @param targetBounds The bounds in which the shape content should be drawn. These bounds
	 *            should be defined according to the local origin in this node.
	 */
	public SGShape(PApplet app, String path, Rectangle sourceBounds, Rectangle targetBounds) {
		super(app);
		name += "__" + path;
		shape = app.loadShape(path);
		this.sourceBounds = sourceBounds;
		this.targetBounds = targetBounds;
		checkShape(shape);
		updateSize();
	}
	
	// *********************************************************************************************
	// Accessors:
	// ---------------------------------------------------------------------------------------------
	
	public SGShape disableStyle() {
		customStyle = true;
		invalidateContent();
		return this;
	}
	
	public SGShape enableStyle() {
		customStyle = false;
		invalidateContent();
		return this;
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @return the shape
	 */
	public PShape getShape() {
		return shape;
	}
	
	/**
	 * @param shape the shape to set
	 */
	public void setShape(PShape shape) {
		checkShape(shape);
		this.shape = shape;
		updateSize();
		invalidateContent();
		invalidateBounds(true);
	}
	
	// *********************************************************************************************
	// SGShape Methods:
	// ---------------------------------------------------------------------------------------------
	
/**
	 * Override this method if you want to construct a custom PShape object.
	 * 
	 * <h4>Example:</h4>
	 * 
	 * {@code
	 * @Override
	 * protected PShape createShape() {
	 *   return app.createShape(TRIANGLE, 0, 0, 100, 100, 0, 50);
	 * }}
	 * 
	 * @return The new PShape object.
	 * 
	 */
	protected PShape createShape() {
		return app.createShape();
	}
	
	/**
	 * Implement this method to draw a custom shape.
	 * 
	 * Use the appropriate methods of the given shape object when doing so.
	 * 
	 * <h4>Example:</h4>
	 * 
	 * {@code
	 * shape.beginShape();
	 * shape.fill(0xFFBE0000);
	 * shape.noStroke();
	 * shape.vertex(60, 20);
	 * shape.vertex(100, 60);
	 * shape.vertex(60, 100);
	 * shape.vertex(20, 60);
	 * shape.endShape(CLOSE);}
	 * 
	 */
	protected void drawShape(PShape shape) {
		/* implement to draw the shape */
	}
	
	/**
	 * Call this method when you changed the shape in this node.
	 */
	public void shapeModified() {
		updateSize();
		invalidateContent();
	}
	
	// *********************************************************************************************
	// SGNode Methods:
	// ---------------------------------------------------------------------------------------------
	
	/* @see be.multec.sg.d2.SG2DNode#draw(processing.core.PGraphics) */
	@Override
	protected void draw(PGraphics g) {
		float scaleW = 1f * targetBounds.width / sourceBounds.width;
		float scaleH = 1f * targetBounds.height / sourceBounds.height;
		if (customStyle) shape.disableStyle();
		//else shape.enableStyle();
		if (isJAVA2D(g)) {
			shape.resetMatrix();
			shape.scale(scaleW, scaleH);
			shape.translate(-sourceBounds.x, -sourceBounds.y);
			shape.draw(g);
			shape.resetMatrix();
		}
		else {
			PGraphics pg = app.createGraphics(targetBounds.width, targetBounds.height, JAVA2D);
			pg.smooth();
			shape.resetMatrix();
			shape.scale(scaleW, scaleH);
			shape.translate(-sourceBounds.x, -sourceBounds.y);
			shape.draw(pg);
			shape.resetMatrix();
			g.image(pg, targetBounds.x, targetBounds.y);
		}
		
		// g.fill(0x44FF0000);
		// g.noStroke();
		// g.rect(targetBounds.x, targetBounds.y, targetBounds.width, targetBounds.height);
	}
	
	/* @see be.multec.sg.SGNode#updateLocalBounds(java.awt.Rectangle) */
	@Override
	protected void updateLocalBounds(Rectangle bounds) {
		bounds.x = targetBounds.x;
		bounds.y = targetBounds.y;
		bounds.width = targetBounds.width;
		bounds.height = targetBounds.height;
	}
	
	/* @see be.multec.sg.SGNode#mouseHitTest() */
	@Override
	protected boolean mouseHitTest() {
		if (targetBounds.width == 0 || targetBounds.height == 0) return false;
		float tx = getMouseX();
		if (tx >= 0 && tx < targetBounds.width) {
			float ty = getMouseY();
			return ty >= 0 && ty < targetBounds.height;
		}
		return false;
	}
	
	// *********************************************************************************************
	// Other methods:
	// ---------------------------------------------------------------------------------------------
	
	private void checkShape(PShape shape) {
		if (shape == null) { throw new Error("The shape is null"); }
		if (app.g.is2D() && shape.is3D()) { throw new Error("A 3D-shape was used in a 2D context."); }
	}
	
	protected void updateSize() {
		if (targetBounds.width == 0)
			targetBounds.width = sourceBounds.width * targetBounds.height / sourceBounds.height;
		if (targetBounds.height == 0)
			targetBounds.height = sourceBounds.height * targetBounds.width / sourceBounds.width;
	}
	
}
