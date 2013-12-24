package be.multec.sg.d2;

import java.awt.Color;
import java.awt.Rectangle;

import processing.core.PGraphics;
import processing.core.PShape;
import be.multec.sg.SGApp;
import be.multec.sg.SGFigure;

/**
 * A node that draws a vector shape.
 * 
 * When a fill or a stroke was set on this node, then the shape's default styles are disabled, and
 * the set fill and stroke are used.
 * 
 * @author Wouter Van den Broeck
 */
public class SGShape extends SGFigure {
	
	// *********************************************************************************************
	// Attributes:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * <dl>
	 * 
	 * <dt>CORNER</dt>
	 * <dd>The shape is positioned such that the top-left corner of its bounding box is on the local
	 * origin. This is the default mode.</dd>
	 * 
	 * <dt>CENTER</dt>
	 * <dd>The shape is positioned such that the center of its bounding box is on the local origin.</dd>
	 * 
	 * <dt>SOURCE</dt>
	 * <dd>The shape is positioned according to the position in the source shape. The explicitly set
	 * with or height are ignored in this mode.</dd>
	 * 
	 * </dl>
	 */
	public enum Position {
		CORNER, CENTER, SOURCE
	};
	
	// ---------------------------------------------------------------------------------------------
	
	/* The shape object. */
	private PShape shape;
	
	/* The position mode. */
	private Position position = Position.CORNER;
	
	/* True when the shape changed. This is set with invalidateShape(). */
	private boolean shapeChanged = true;
	
	private boolean useNodeStyles = false;
	
	private Rectangle sourceBounds = new Rectangle();
	
	// *********************************************************************************************
	// Constructors:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @param app The scene-graph application object.
	 * @param shape The shape object to draw in this node.
	 */
	public SGShape(SGApp app, PShape shape) {
		super(app);
		checkShape(shape);
		this.shape = shape;
	}
	
	/**
	 * @param app The scene-graph application object.
	 * @param shape The shape object to draw in this node.
	 * @param position The position of the drawn shape. @see setPosition.
	 */
	public SGShape(SGApp app, PShape shape, Position position) {
		super(app);
		checkShape(shape);
		this.shape = shape;
		this.position = position;
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * Use this constructor to load a shape from an SVG-file.
	 * 
	 * @param app The scene-graph application object.
	 * @param path The path to the SVG-file to load.
	 */
	public SGShape(SGApp app, String path) {
		super(app);
		name += "__" + path;
		shape = app.loadShape(path);
		checkShape(shape);
	}
	
	/**
	 * Use this constructor to load a shape from an SVG-file.
	 * 
	 * @param app The scene-graph application object.
	 * @param path The path to the SVG-file to load.
	 * @param position The position of the drawn shape. @see setPosition.
	 */
	public SGShape(SGApp app, String path, Position position) {
		super(app);
		name += "__" + path;
		shape = app.loadShape(path);
		checkShape(shape);
		this.position = position;
	}
	
	// *********************************************************************************************
	// Accessors:
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
		redraw();
		invalidateLocalBounds();
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/**
	 * @return The position. @see setPosition
	 */
	public Position getPosition() {
		return position;
	}
	
	/**
	 * The position can be one of:
	 * 
	 * <dl>
	 * <dt>Position.DEFAULT</dt>
	 * <dd>The shape is drawn normally with respect to the local coordinates. The default position.</dd>
	 * <dt>Position.CENTER</dt>
	 * <dd>The shape is drawn centered on the origin of the local coordinate system. Note that in
	 * this positioning mode, the shape is shifted left and up. The horizontal shift equals half of
	 * the shape's width, while the vertical shift equals half of the shape's height.</dd>
	 * 
	 * </dl>
	 * 
	 * @param position The position to set.
	 */
	public void setPosition(Position position) {
		if (this.position == position) return;
		this.position = position;
		invalidateLocalBounds();
		redraw();
	}
	
	// *********************************************************************************************
	// Styles:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * Use the node styles instead of the styles in the shape.
	 * 
	 * @param b When true then use the node styles.
	 */
	public void useNodeStyles() {
		if (useNodeStyles) return;
		useNodeStyles = true;
		invalidateLocalBounds();
		redraw();
	}
	
	/**
	 * @return True when the node styles are used instead of the shape styles.
	 */
	public boolean usingNodeStyles() {
		return useNodeStyles;
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/**
	 * Use the shape styles instead of the styles of the node.
	 * 
	 * @param b When true then use the node styles.
	 */
	public void useShapeStyles() {
		if (!useNodeStyles) return;
		useNodeStyles = false;
		invalidateLocalBounds();
		redraw();
	}
	
	/**
	 * @return True when the shape styles are used instead of the node styles.
	 */
	public boolean usingShapeStyles() {
		return useNodeStyles;
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/* @see be.multec.sg.SGFigure#noFill() */
	@Override
	public void noFill() {
		useNodeStyles();
		super.noFill();
	}
	
	/* @see be.multec.sg.SGFigure#fill(java.awt.Color) */
	@Override
	public void fill(Color color) {
		useNodeStyles();
		super.fill(color);
	}
	
	/* @see be.multec.sg.SGFigure#fill(int, int, int) */
	@Override
	public void fill(int r, int g, int b) {
		useNodeStyles();
		super.fill(r, g, b);
	}
	
	/* @see be.multec.sg.SGFigure#noStroke() */
	@Override
	public void noStroke() {
		useNodeStyles();
		super.noStroke();
	}
	
	/* @see be.multec.sg.SGFigure#stroke(java.awt.Color) */
	@Override
	public void stroke(Color color) {
		useNodeStyles();
		super.stroke(color);
	}
	
	/* @see be.multec.sg.SGFigure#stroke(java.awt.Color, float) */
	@Override
	public void stroke(Color color, float weight) {
		useNodeStyles();
		super.stroke(color, weight);
	}
	
	// *********************************************************************************************
	// draw implementation:
	// ---------------------------------------------------------------------------------------------
	
	/* @see be.multec.sg.d2.SG2DNode#draw(processing.core.PGraphics) */
	@Override
	protected void draw(PGraphics g) {
		
		if (useNodeStyles) shape.disableStyle();
		else shape.enableStyle();
		
		if (position == Position.CORNER) {
			g.pushMatrix();
			g.translate(-sourceBounds.x, -sourceBounds.y);
			shape.draw(g);
			g.popMatrix();
		}
		else if (position == Position.CENTER) {
			g.pushMatrix();
			g.translate(getLocalBounds().x - sourceBounds.x, getLocalBounds().y - sourceBounds.y);
			shape.draw(g);
			g.popMatrix();
		}
		else { // ORIGIN
			shape.draw(g);
		}
	}
	
	// *********************************************************************************************
	// updateLocalBounds implementation:
	// ---------------------------------------------------------------------------------------------
	
	/* @see be.multec.sg.SGNode#updateLocalBounds(java.awt.Rectangle) */
	@Override
	protected void updateLocalBounds(Rectangle bounds) {
		boolean trace = false;
		if (trace) println(">> SGShape[" + this.name + "].updateLocalBounds()");
		
		float[] xxyy = new float[] { Float.MAX_VALUE, Float.MIN_VALUE, Float.MAX_VALUE,
				Float.MIN_VALUE };
		updateBounds(shape, xxyy);
		
		sourceBounds.x = (int) Math.floor(xxyy[0]);
		sourceBounds.y = (int) Math.floor(xxyy[2]);
		sourceBounds.width = (int) Math.ceil(xxyy[1] - sourceBounds.x);
		sourceBounds.height = (int) Math.ceil(xxyy[3] - sourceBounds.y);
		
		if (position == Position.SOURCE) {
			bounds.setBounds(sourceBounds);
			return;
		}
		
		float bw = xxyy[1] - xxyy[0];
		float bh = xxyy[3] - xxyy[2];
		
		if (bw == 0 || bh == 0) {
			bounds.setBounds(0, 0, 0, 0);
			return;
		}
		
		float bx = 0, by = 0;
		if (position == Position.CENTER) {
			bx = -bw / 2;
			by = -bh / 2;
		}
		
		bounds.x = (int) Math.floor(bx);
		bounds.y = (int) Math.floor(by);
		bounds.width = (int) Math.ceil(bw + bx - bounds.x);
		bounds.height = (int) Math.ceil(bh + by - bounds.y);
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	private void updateBounds(PShape shape, float[] xxyy) {
		boolean trace = false;
		if (trace) println(">> SGShape[" + this.name + "].updateBounds()");
		
		if (shape.getMatrix() != null) { throw new Error(
				"Transformations in shapes are currently not supported."); }
		
		switch (shape.getFamily()) {
		
			case GROUP:
				if (trace) println(" # GROUP");
				PShape[] children = shape.getChildren();
				for (int i = 0; i < children.length; i++) {
					if (children[i] != null) updateBounds(children[i], xxyy);
				}
				break;
			
			case PShape.PRIMITIVE:
				if (trace) println(" # PRIMITIVE");
				updatePrimBounds(shape, xxyy);
				break;
			
			case PShape.PATH:
				if (trace) println(" # PATH");
				int vertexCount = shape.getVertexCount();
				float hsw = actualStrokeWeight(shape) / 2;
				
				if (trace) println(" - vertexCount: " + vertexCount);
				if (trace) println(" - hsw: " + hsw);
				
				for (int i = 0; i < vertexCount; i++) {
					// println("- i: " + i);
					// println("- shape.getVertexX(i): " + shape.getVertexX(i));
					// println("- shape.getVertexCode(i): " + shape.getVertexCode(i));
					// println("- shape.getStroke(i): " + shape.getStroke(i));
					// println("- shape.getStrokeWeight(i): " +
					// shape.getStrokeWeight(i));
					updateBounds(shape.getVertexX(i), shape.getVertexY(i), hsw, xxyy);
				}
				break;
			
			case PShape.GEOMETRY:
				System.err.println("GEOMETRY case in SGShape.updateBounds() is not implemented.");
				break;
			
			default:
				throw new Error("Unexpected shape family: '" + shape.getFamily() + "'.");
		}
	}
	
	private void updatePrimBounds(PShape shape, float[] xxyy) {
		boolean trace = false;
		if (trace) println(">> SGShape[" + this.name + "].updatePrimBounds()");
		
		float[] pp = shape.getParams();
		float hsw = actualStrokeWeight(shape) / 2;
		if (trace) println(" - hsw: " + hsw);
		
		switch (shape.getKind()) {
			case POINT:
				updateBounds(pp[0], pp[1], hsw, xxyy);
				break;
			
			case LINE:
				if (pp.length == 4) { // 2D
					updateBounds(pp[0], pp[1], hsw, xxyy);
					updateBounds(pp[2], pp[3], hsw, xxyy);
				}
				else System.err.println("3D not supported.");
				break;
			
			case TRIANGLE:
				updateBounds(pp[0], pp[1], hsw, xxyy);
				updateBounds(pp[2], pp[3], hsw, xxyy);
				updateBounds(pp[4], pp[5], hsw, xxyy);
				break;
			
			case QUAD:
				updateBounds(pp[0], pp[1], hsw, xxyy);
				updateBounds(pp[2], pp[3], hsw, xxyy);
				updateBounds(pp[4], pp[5], hsw, xxyy);
				updateBounds(pp[6], pp[7], hsw, xxyy);
				break;
			
			case RECT:
				updateBounds(pp[0], pp[1], hsw, xxyy);
				updateBounds(pp[0] + pp[2], pp[1] + pp[3], hsw, xxyy);
				break;
			
			case ELLIPSE:
				updateBounds(pp[0], pp[1], hsw, xxyy);
				updateBounds(pp[0] + pp[2], pp[1] + pp[3], hsw, xxyy);
				break;
			
			case ARC:
				float cx = pp[0];
				float cy = pp[1];
				float da = pp[5] - pp[4];
				float hw = pp[2] / 2; // half width
				float hh = pp[3] / 2; // half height
				if (da > TWO_PI) {
					updateBounds(cx - hw, cy - hh, hsw, xxyy);
					updateBounds(cx + hw, cy + hh, hsw, xxyy);
					break;
				}
				int k1 = ((int) Math.floor(pp[4] / HALF_PI)) % 4;
				int k2 = ((int) Math.floor(pp[5] / HALF_PI)) % 4;
				float x1 = (float) (cx + Math.cos(pp[4]) * hw);
				float y1 = (float) (cy + Math.sin(pp[4]) * hh);
				float x2 = (float) (cx + Math.cos(pp[5]) * hw);
				float y2 = (float) (cy + Math.sin(pp[5]) * hh);
				updateBounds(cx, cy, hsw, xxyy);
				updateBounds(x1, y1, hsw, xxyy);
				updateBounds(x2, y2, hsw, xxyy);
				if (k1 == 0) {
					if (k2 == 1) updateBounds(cx, cy + hh, hsw, xxyy);
					else if (k2 == 2) updateBounds(cx - hw, cy + hh, hsw, xxyy);
					else if (k2 == 3) {
						updateBounds(cx - hw, cy - hh, hsw, xxyy);
						updateBounds(cx - hw, cy + hh, hsw, xxyy);
					}
				}
				else if (k1 == 1) {
					if (k2 == 2) updateBounds(cx - hw, cy, hsw, xxyy);
					else if (k2 == 3) updateBounds(cx - hw, cy - hh, hsw, xxyy);
					else if (k2 == 0) {
						updateBounds(cx - hw, cy - hh, hsw, xxyy);
						updateBounds(cx + hw, cy - hh, hsw, xxyy);
					}
				}
				else if (k1 == 2) {
					if (k2 == 3) updateBounds(cx, cy - hh, hsw, xxyy);
					else if (k2 == 0) updateBounds(cx + hw, cy - hh, hsw, xxyy);
					else if (k2 == 1) {
						updateBounds(cx + hw, cy - hh, hsw, xxyy);
						updateBounds(cx + hw, cy + hh, hsw, xxyy);
					}
				}
				else { // k == 3
					if (k2 == 0) updateBounds(cx, cy + hh, hsw, xxyy);
					else if (k2 == 1) updateBounds(cx + hw, cy + hh, hsw, xxyy);
					else if (k2 == 2) {
						updateBounds(cx + hw, cy + hh, hsw, xxyy);
						updateBounds(cx - hw, cy + hh, hsw, xxyy);
					}
				}
				break;
			
			case BOX:
				System.err.println("BOX case in SGShape.updatePrimBounds() is not implemented.");
				break;
			
			case SPHERE:
				System.err.println("SPHERE case in SGShape.updatePrimBounds() is not implemented.");
				break;
		
		}
	}
	
	/**
	 * @param x
	 * @param y
	 * @param hsw Half of the strokeWeight
	 * @param xxyy
	 */
	private void updateBounds(float x, float y, float hsw, float[] xxyy) {
		if (x - hsw < xxyy[0]) xxyy[0] = x - hsw;
		if (x + hsw > xxyy[1]) xxyy[1] = x + hsw;
		if (y - hsw < xxyy[2]) xxyy[2] = y - hsw;
		if (y + hsw > xxyy[3]) xxyy[3] = y + hsw;
	}
	
	// *********************************************************************************************
	// contains implementation:
	// ---------------------------------------------------------------------------------------------
	
	private boolean traceContains = false;
	
	/* @see be.multec.sg.SGNode#mouseHitTest() */
	@Override
	protected boolean contains(float x, float y) {
		if (traceContains) println(" >> contains(" + x + ", " + y + ")");
		if (position == Position.CORNER) {
			if (localBoundsChanged) validateLocalBounds();
			return contains(shape, x + sourceBounds.x, y + sourceBounds.y);
		}
		else if (position == Position.CENTER) {
			if (localBoundsChanged) validateLocalBounds();
			return contains(shape, x - (getLocalBounds().x - sourceBounds.x), y
					- (getLocalBounds().y - sourceBounds.y));
		}
		return contains(shape, x, y);
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	private boolean contains(PShape shape, float x, float y) {
		if (traceContains) println(" >> contains_sys(" + x + ", " + y + ")");
		if (shape.getMatrix() != null) { throw new Error(
				"Transformations in shapes are currently not supported."); }
		switch (shape.getFamily()) {
		
			case GROUP:
				PShape[] children = shape.getChildren();
				for (int i = 0; i < children.length; i++) {
					if (children[i] != null && contains(children[i], x, y)) return true;
				}
				return false;
				
			case PShape.PRIMITIVE:
				return containsPrim(shape, x, y);
				
			case PShape.PATH:
				if (traceContains) println("   - PATH contains: " + shape.contains(x, y));
				return shape.contains(x, y);
				
			case PShape.GEOMETRY:
				System.err.println("GEOMETRY case in SGShape.contains() is not implemented.");
				return false;
				
			default:
				throw new Error("Unexpected shape family: '" + shape.getFamily() + "'.");
		}
	}
	
	private boolean containsPrim(PShape shape, float x, float y) {
		if (traceContains) println("  >> containsPrim(" + x + ", " + y + ")");
		float[] pp = shape.getParams();
		float hsw = actualStrokeWeight(shape) / 2;
		boolean b0, b1;
		
		switch (shape.getKind()) {
			case POINT:
				System.err.println("POINT case in SGShape.containsPrim() is not implemented.");
				break;
			
			case LINE:
				System.err.println("LINE case in SGShape.containsPrim() is not implemented.");
				if (pp.length == 4) { // 2D
				}
				else System.err.println("3D not supported.");
				break;
			
			case TRIANGLE:
				b0 = (x - pp[2]) * (pp[1] - pp[3]) - (pp[0] - pp[2]) * (y - pp[3]) < 0;
				b1 = (x - pp[4]) * (pp[3] - pp[5]) - (pp[2] - pp[4]) * (y - pp[5]) < 0;
				if (b0 != b1) return false;
				b1 = (x - pp[0]) * (pp[5] - pp[1]) - (pp[4] - pp[0]) * (y - pp[1]) < 0;
				return b0 == b1;
				
			case QUAD:
				// TODO: Untested code
				b0 = ((y - pp[1]) * (pp[2] - pp[0])) - ((x - pp[0]) * (pp[3] - pp[1])) <= 0;
				b1 = ((y - pp[3]) * (pp[4] - pp[2])) - ((x - pp[2]) * (pp[5] - pp[3])) <= 0;
				if (b0 != b1) return false;
				b1 = ((y - pp[5]) * (pp[6] - pp[4])) - ((x - pp[4]) * (pp[7] - pp[5])) <= 0;
				if (b0 != b1) return false;
				b1 = ((y - pp[7]) * (pp[0] - pp[6])) - ((x - pp[6]) * (pp[1] - pp[7])) <= 0;
				return b0 == b1;
				
			case RECT:
				// TODO: Untested code
				// pp[0] & pp[1]: x/y of top-left corner
				// pp[2] & pp[3]: width & height
				return (x >= pp[0]) && (y >= pp[1]) && (x < pp[0] + pp[2]) && (y < pp[1] + pp[3]);
				
			case ELLIPSE:
				// pp[0] & pp[1]: x/y of top-left corner
				// pp[2] & pp[3]: width & height
				float ta = (x - (pp[0] + pp[2] / 2)) / (pp[2] / 2);
				float tb = (y - (pp[1] + pp[3] / 2)) / (pp[3] / 2);
				if (traceContains) {
					println("   - ELLIPSE, ta: " + ta + ", tb: " + tb);
					println("   - pp[0-3]: " + pp[0] + ", " + pp[1] + ", " + pp[2] + ", " + pp[3]);
					println("   - contains: " + (ta * ta + tb * tb <= 1));
				}
				return ta * ta + tb * tb <= 1;
				
			case ARC:
				System.err.println("ARC case in SGShape.containsPrim() is not implemented.");
				break;
			
			case BOX:
				System.err.println("BOX case in SGShape.containsPrim() is not implemented.");
				break;
			
			case SPHERE:
				System.err.println("SPHERE case in SGShape.containsPrim() is not implemented.");
				break;
		
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
	
	private float actualStrokeWeight(PShape shape) {
		if (useNodeStyles) {
			if (stroked()) return strokeWeight();
			else return 0;
		}
		else {
			if (shape.stroked()) return shape.getStrokeWeight();
			else return 0;
		}
	}
}
