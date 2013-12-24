package be.multec.sg;

import java.awt.Color;

import processing.core.PApplet;
import processing.core.PGraphics;

/**
 * A SGFigure is an SGNode that has appearance properties such as a fill color, a stroke color, a
 * stroke weight
 * 
 * TODO: Consider allowing the user to specify that a figure inherits the fill or stroke from its
 * parent.
 * 
 * @author Wouter Van den Broeck
 */
abstract public class SGFigure extends SGNode {
	
	// *********************************************************************************************
	// Attributes:
	// ---------------------------------------------------------------------------------------------
	
	/* When true then this form should be drawn with a fill. @see setFilled */
	private boolean filled = false;
	
	/* When true then this form should be drawn with a stroke. @see setStroked */
	private boolean stroked = false;
	
	/* The fill color. */
	protected Color fillColor; // = new Color(0xFF999999, true);
	
	/* The stroke color. */
	protected Color strokeColor; // = new Color(0xFF000000, true);
	
	/* The stroke weight. */
	private float strokeWeight = 1;
	
	/* The blend-mode. @see setBlendMode */
	protected int blendMode = BLEND;
	
	// *********************************************************************************************
	// Constructors:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @param app The PApplet object.
	 */
	public SGFigure(SGApp app) {
		super(app);
	}
	
	/**
	 * @param app The PApplet object.
	 * @param fillColor The fill color.
	 */
	public SGFigure(SGApp app, Color fillColor) {
		super(app);
		this.fillColor = fillColor;
		filled = true;
	}
	
	/**
	 * @param app The PApplet object.
	 * @param fillColor The fill color.
	 * @param strokeColor The stroke color.
	 * @param strokeWeight The thickness of the stroke.
	 */
	public SGFigure(SGApp app, Color fillColor, Color strokeColor, float strokeWeight) {
		super(app);
		this.fillColor = fillColor;
		this.strokeColor = strokeColor;
		this.strokeWeight = strokeWeight;
		filled = true;
		stroked = true;
	}
	
	private void initSGFigure() {}
	
	// *********************************************************************************************
	// Fill methods and accessors:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @return True when the graphical content of this node will be drawn with a fill.
	 */
	public boolean filled() {
		return filled;
	}
	
	/**
	 * @return The fill color.
	 */
	public Color fillColor() {
		return fillColor;
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * Specifies that the graphical content of this node will be drawn without fill.
	 */
	public void noFill() {
		if (!filled) return;
		filled = false;
		redraw();
	}
	
	/**
	 * Specifies that the graphical content of this node will be drawn with the given fill color.
	 * 
	 * @param color The fill color.
	 */
	public void fill(Color color) {
		if (filled && (fillColor == color || fillColor.equals(color))) return;
		filled = true;
		fillColor = color;
		redraw("SGFigure.fill()");
	}
	
	public void fill(int r, int g, int b) {
		fill(new Color(r, g, b));
	}
	
	// *********************************************************************************************
	// Stroke methods and accessors:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @return True when the graphical content of this node will be drawn with a stroke.
	 */
	public boolean stroked() {
		return stroked;
	}
	
	/**
	 * @return the strokeColor
	 */
	public Color strokeColor() {
		return strokeColor;
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * Specifies that the graphical content of this node will be drawn without stroke.
	 */
	public void noStroke() {
		if (!stroked) return;
		stroked = false;
		invalidateLocalBounds();
		redraw();
	}
	
	/**
	 * Specifies that the graphical content of this node will be drawn with the given stroke color.
	 * 
	 * @param color The stroke color.
	 */
	public void stroke(Color color) {
		if (stroked && (strokeColor == color || strokeColor.equals(color))) return;
		if (!stroked) invalidateLocalBounds();
		stroked = true;
		strokeColor = color;
		redraw();
	}
	
	/**
	 * Specifies that the graphical content of this node will be drawn with the given stroke color
	 * and the given stroke weight.
	 * 
	 * @param color The stroke color.
	 * @param weight The stroke weight.
	 */
	public void stroke(Color color, float weight) {
		if (stroked && (strokeColor == color || strokeColor.equals(color))
				&& strokeWeight == weight) return;
		if (!stroked || strokeWeight != weight) invalidateLocalBounds();
		stroked = true;
		strokeColor = color;
		strokeWeight = weight;
		redraw();
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @return the strokeWeight
	 */
	public float strokeWeight() {
		return strokeWeight;
	}
	
	/**
	 * @param strokeWeight the strokeWeight to set
	 */
	public void strokeWeight(float weight) {
		if (strokeWeight == weight) return;
		strokeWeight = weight;
		if (stroked) {
			redraw();
			invalidateLocalBounds();
		}
	}
	
	// *********************************************************************************************
	// BlendMode methods and accessors:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @return The currently applicable Processing blend mode.
	 */
	public int blendMode() {
		return blendMode;
	}
	
	/**
	 * @param blendMode The Processing blend mode to use when drawing this label. Can be set to any
	 *            of the constants that can be set with the PApplet.blendMode() method.
	 * 
	 * @default PConstant#BLEND
	 * 
	 * @see PApplet#blendMode(int)
	 */
	public void blendMode(int blendMode) {
		if (this.blendMode == blendMode) return;
		this.blendMode = blendMode;
		redraw();
	}
	
	// *********************************************************************************************
	// SGNode methods:
	// ---------------------------------------------------------------------------------------------
	
	/* @see be.multec.sg.SGNode#draw_sys(processing.core.PGraphics) */
	@Override
	protected void draw_sys(PGraphics g) {
		// println(">> SG2DFigure.draw_sys() - this: " + this);
		
		if (filled) g.fill(fillColor.getRGB(), fillColor.getAlpha());
		else g.noFill();
		
		if (stroked) {
			g.stroke(strokeColor.getRGB(), strokeColor.getAlpha());
			g.strokeWeight(strokeWeight);
		}
		else g.noStroke();
		
		g.blendMode(blendMode);
		
		super.draw_sys(g);
	}
	
}
