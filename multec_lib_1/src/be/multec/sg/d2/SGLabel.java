package be.multec.sg.d2;

import java.awt.Color;
import java.awt.Rectangle;

import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;
import be.multec.languages.IMLStringUpdateHandler;
import be.multec.languages.MLString;
import be.multec.sg.SGApp;
import be.multec.sg.SGNode;
import be.multec.sg.styles.ILabelStyles;

/**
 * Draws a text-label.
 * 
 * <h2>Width and height</h2>
 * 
 * The width and height of a label (as obtained through the getWidth and getHeight accessors) depend
 * on the label mode.
 * <ul>
 * <li>When the label mode is <em>SIMPLE</em> then the width equals the width of the rendered text
 * plus the padding, while the height equals the height of the rendered text (the sum of the
 * text-ascent and the text-descent as given by the PGraphics object) plus the padding.</li>
 * <li>When the label mode is <em>BOX</em> then the width is the explicitly set width (or the
 * default width) while the height is the explicitly set height (or the default height).</li>
 * </ul>
 * 
 * @see SGLabel#getWidth()
 * @see SGLabel#getHeight()
 * 
 * @author Wouter Van den Broeck
 */
public class SGLabel extends SGNode implements IMLStringUpdateHandler {
	
	// *********************************************************************************************
	// Attributes:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * The image modes:
	 * 
	 * <DL>
	 * 
	 * <DT>SIMPLE</DT>
	 * <DD>The label is positioned with respect to the label's position.</DD>
	 * 
	 * <DT>BOX</DT>
	 * <DD>The label is positioned inside a box. The left-top corner of the box is placed on the
	 * label's position.</DD>
	 * 
	 * <DT>BOX_CENTER</DT>
	 * <DD>The label is positioned inside a box. The center of the box is placed on the label's
	 * position.</DD>
	 * 
	 * </DL>
	 */
	public enum SGLabelMode {
		SIMPLE, BOX, BOX_CENTER
	}
	
	/**
	 * The horizontal text align modes:
	 * 
	 * <DL>
	 * 
	 * <DT>LEFT</DT>
	 * <DD></DD>
	 * 
	 * <DT>CENTER</DT>
	 * <DD></DD>
	 * 
	 * <DT>RIGHT</DT>
	 * <DD></DD>
	 * 
	 * </DL>
	 */
	public enum HAlignMode {
		LEFT, CENTER, RIGHT
	}
	
	/**
	 * The horizontal text align modes:
	 * 
	 * <DL>
	 * 
	 * <DT>LEFT</DT>
	 * <DD></DD>
	 * 
	 * <DT>CENTER</DT>
	 * <DD></DD>
	 * 
	 * <DT>RIGHT</DT>
	 * <DD></DD>
	 * 
	 * </DL>
	 */
	public enum VAlignMode {
		TOP, CENTER, BOTTOM
	}
	
	// ---------------------------------------------------------------------------------------------
	
	public static Color DEFAULT_TEXT_COLOR = Color.BLACK;
	public static float DEFAULT_TEXT_SIZE = 18;
	public static float DEFAULT_TEXT_PADDING = 0;
	public static Color DEFAULT_BACKGROUND_COLOR = null;
	public static SGLabelMode DEFAULT_LABEL_MODE = SGLabelMode.SIMPLE;
	public static HAlignMode DEFAULT_HALIGN_MODE = HAlignMode.LEFT;
	public static VAlignMode DEAFULT_VALIGN_MODE = VAlignMode.BOTTOM;
	
	// ---------------------------------------------------------------------------------------------
	
	/* The text for this label. */
	private String label;
	
	/* The multi-language string when one is used. */
	private MLString mlString;
	
	/* The font to use for this label. */
	private PFont font;
	
	/* The font size to use for this label. */
	private float textSize;
	
	/* The explicitly set text leading. A value of -1 is ignored. */
	private float leading = -1;
	
	/* The color for this label. */
	private Color textColor;
	
	/* The color for the background. When this is -1 then no background is drawn. */
	private Color bgColor;
	
	/* Draw the background when this is true. */
	private boolean drawBackground = false;
	
	/* The label mode to use when drawing the label. */
	private SGLabelMode labelMode;
	
	/* The horizontal text align mode to use when drawing the label. */
	private HAlignMode hAlignMode;
	
	/*
	 * The vertical text align mode to use when drawing the label. This mode is ignored when the
	 * label mode is <em>BOX</em>.
	 */
	private VAlignMode vAlignMode;
	
	/* The horizontal Processing align mode. */
	private int pHAlignMode = PConstants.LEFT;
	
	/*
	 * The vertical Processing align mode. This mode is ignored when the label mode is <em>BOX</em>.
	 */
	private int pVAlignMode = PConstants.TOP;
	
	/*
	 * The width of the label. This value is updated in the updateSize method. Check the <em>Width
	 * and height</em> section in the class documentation for more information.
	 */
	private float LblWidth = -1;
	
	/*
	 * The height of the label. This value is updated in the updateSize method. Check the <em>Width
	 * and height</em> section in the class documentation for more information.
	 */
	private float LblHeight = -1;
	
	/*
	 * True when the width and height are incorrect. Check the <em>Width and height</em> section in
	 * the class documentation for more information.
	 */
	private boolean sizeDirty = false;
	
	/*
	 * The explicitly set width. Check the <em>Width and height</em> section in the class
	 * documentation for more information.
	 */
	private float explicitWidth = 200;
	
	/*
	 * The explicitly set height. Check the <em>Width and height</em> section in the class
	 * documentation for more information.
	 */
	private float explicitHeight = 30;
	
	/*
	 * The padding to use. Check the <em>Width and height</em> section in the class documentation
	 * for more information.
	 */
	private float padding;
	
	/* A vertical offset to improve the alignment of the text. */
	private float vOffset = 0;
	
	/* The width of the text-box. Used when the label-mode is BOX. */
	private float textBoxWidth;
	
	/* The height of the text-box. Used when the label-mode is BOX. */
	private float textBoxHeight;
	
	/* The blend-mode to use when drawing this label. */
	private int blendMode = BLEND;
	
	// *********************************************************************************************
	// Constructors:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @param app The scene-graph application.
	 * @param label The label text.
	 */
	public SGLabel(SGApp app, String label) {
		super(app);
		this.name = "Label (" + label + ")";
		this.label = label;
		init(null, null, null, null);
	}
	
	/**
	 * As the width and height are explicitly given, the label mode is assumed to be <em>BOX</em>.
	 * 
	 * @param app The scene-graph application.
	 * @param label The label text.
	 * @param boxWidth The explicit width of this label. Check the <em>Width and height</em> section
	 *            in the class documentation for more information.
	 * @param boxHeight The explicit height of this label. Check the <em>Width and height</em>
	 *            section in the class documentation for more information.
	 * 
	 * @see SGLabel#setWidth(float)
	 */
	public SGLabel(SGApp app, String label, float boxWidth, float boxHeight) {
		super(app);
		this.name = "Label (" + label + ")";
		this.label = label;
		this.explicitWidth = boxWidth;
		this.explicitHeight = boxHeight;
		init(null, SGLabelMode.BOX, null, null);
	}
	
	/**
	 * @param app The scene-graph application.
	 * @param label The label text.
	 * @param style The style to use.
	 */
	public SGLabel(SGApp app, String label, ILabelStyles styles) {
		super(app);
		this.name = "Label (" + label + ")";
		this.label = label;
		init(styles, null, null, null);
	}
	
	/**
	 * As the width and height are explicitly given, the label mode is assumed to be <em>BOX</em>.
	 * 
	 * @param app The scene-graph application.
	 * @param label The label text.
	 * @param styles The style to use.
	 * @param boxWidth The explicit width of this label. Check the <em>Width and height</em> section
	 *            in the class documentation for more information.
	 * @param height The explicit height of this label. Check the <em>Width and height</em> section
	 *            in the class documentation for more information.
	 */
	public SGLabel(SGApp app, String label, ILabelStyles styles, float boxWidth, float height) {
		super(app);
		this.name = "Label (" + label + ")";
		this.label = label;
		this.explicitWidth = boxWidth;
		this.explicitHeight = height;
		init(styles, SGLabelMode.BOX, null, null);
	}
	
	/**
	 * @param app The scene-graph application.
	 * @param label The label text.
	 * @param labelMode The label mode to use for this label.
	 */
	public SGLabel(SGApp app, String label, SGLabelMode labelMode) {
		super(app);
		this.name = "Label (" + label + ")";
		this.label = label;
		init(null, labelMode, null, null);
	}
	
	/**
	 * @param app The scene-graph application.
	 * @param label The label text.
	 * @param labelMode The label mode to use for this label.
	 * @param width The explicit width of this label. Check the <em>Width and height</em> section in
	 *            the class documentation for more information.
	 * @param height The explicit height of this label. Check the <em>Width and height</em> section
	 *            in the class documentation for more information.
	 */
	public SGLabel(SGApp app, String label, SGLabelMode labelMode, float width, float height) {
		super(app);
		this.name = "Label (" + label + ")";
		this.label = label;
		this.explicitWidth = width;
		this.explicitHeight = height;
		init(null, labelMode, null, null);
	}
	
	/**
	 * @param app The scene-graph application.
	 * @param label The label text.
	 * @param styles The style to use.
	 * @param labelMode The label mode to use for this label.
	 */
	public SGLabel(SGApp app, String label, ILabelStyles styles, SGLabelMode labelMode) {
		super(app);
		this.name = "Label (" + label + ")";
		this.label = label;
		init(styles, labelMode, null, null);
	}
	
	/**
	 * @param app The scene-graph application.
	 * @param label The label text.
	 * @param styles The style to use.
	 * @param labelMode The label mode to use for this label.
	 * @param width The explicit width of this label. Check the <em>Width and height</em> section in
	 *            the class documentation for more information.
	 * @param height The explicit height of this label. Check the <em>Width and height</em> section
	 *            in the class documentation for more information.
	 */
	public SGLabel(SGApp app, String label, ILabelStyles styles, SGLabelMode labelMode,
			float width, float height)
	{
		super(app);
		this.name = "Label (" + label + ")";
		this.label = label;
		this.explicitWidth = width;
		this.explicitHeight = height;
		init(styles, labelMode, null, null);
	}
	
	/**
	 * @param app The scene-graph application.
	 * @param label The label text.
	 * @param labelMode The label mode to use for this label.
	 * @param hAlignMode The horizontal align mode to use for this label.
	 * @param vAlignMode The vertical align mode to use for this label.
	 */
	public SGLabel(SGApp app, String label, SGLabelMode labelMode, HAlignMode hAlignMode,
			VAlignMode vAlignMode)
	{
		super(app);
		this.name = "Label (" + label + ")";
		this.label = label;
		init(null, labelMode, hAlignMode, vAlignMode);
	}
	
	/**
	 * @param app The scene-graph application.
	 * @param label The label text.
	 * @param labelMode The label mode to use for this label.
	 * @param hAlignMode The horizontal align mode to use for this label.
	 * @param vAlignMode The vertical align mode to use for this label.
	 * @param width The explicit width of this label. Check the <em>Width and height</em> section in
	 *            the class documentation for more information.
	 * @param height The explicit height of this label. Check the <em>Width and height</em> section
	 *            in the class documentation for more information.
	 */
	public SGLabel(SGApp app, String label, SGLabelMode labelMode, HAlignMode hAlignMode,
			VAlignMode vAlignMode, float width, float height)
	{
		super(app);
		this.name = "Label (" + label + ")";
		this.label = label;
		this.explicitWidth = width;
		this.explicitHeight = height;
		init(null, labelMode, hAlignMode, vAlignMode);
	}
	
	/**
	 * @param app The scene-graph application.
	 * @param label The label text.
	 * @param styles The style to use.
	 * @param labelMode The label mode to use for this label.
	 * @param hAlignMode The horizontal align mode to use for this label.
	 * @param vAlignMode The vertical align mode to use for this label.
	 */
	public SGLabel(SGApp app, String label, ILabelStyles styles, SGLabelMode labelMode,
			HAlignMode hAlignMode, VAlignMode vAlignMode)
	{
		super(app);
		this.name = "Label (" + label + ")";
		this.label = label;
		init(styles, labelMode, hAlignMode, vAlignMode);
	}
	
	/**
	 * @param app The scene-graph application.
	 * @param label The label text.
	 * @param styles The style to use.
	 * @param labelMode The label mode to use for this label.
	 * @param hAlignMode The horizontal align mode to use for this label.
	 * @param vAlignMode The vertical align mode to use for this label.
	 * @param width The explicit width of this label. Check the <em>Width and height</em> section in
	 *            the class documentation for more information.
	 * @param height The explicit height of this label. Check the <em>Width and height</em> section
	 *            in the class documentation for more information.
	 */
	public SGLabel(SGApp app, String label, ILabelStyles styles, SGLabelMode labelMode,
			HAlignMode hAlignMode, VAlignMode vAlignMode, float width, float height)
	{
		super(app);
		this.name = "Label (" + label + ")";
		this.label = label;
		this.explicitWidth = width;
		this.explicitHeight = height;
		init(styles, labelMode, hAlignMode, vAlignMode);
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	// Constructors with multi-language strings:
	
	public SGLabel(SGApp app, MLString mlString) {
		super(app);
		this.label = mlString.toString();
		this.name = "Label (" + label + ")";
		this.mlString = mlString;
		mlString.addUpdateHandler(this);
		init(null, null, null, null);
	}
	
	public SGLabel(SGApp app, MLString mlString, ILabelStyles styles) {
		super(app);
		this.label = mlString.toString();
		this.name = "Label (" + label + ")";
		this.mlString = mlString;
		mlString.addUpdateHandler(this);
		init(styles, null, null, null);
	}
	
	public SGLabel(SGApp app, MLString mlString, SGLabelMode labelMode) {
		super(app);
		this.label = mlString.toString();
		this.name = "Label (" + label + ")";
		this.mlString = mlString;
		mlString.addUpdateHandler(this);
		init(null, labelMode, null, null);
	}
	
	public SGLabel(SGApp app, MLString mlString, HAlignMode hAlignMode, VAlignMode vAlignMode) {
		super(app);
		this.label = mlString.toString();
		this.name = "Label (" + label + ")";
		this.mlString = mlString;
		mlString.addUpdateHandler(this);
		init(null, labelMode, hAlignMode, vAlignMode);
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * This method should only be called from the constructors.
	 * 
	 * @param styles Pass null to use the default styles.
	 * @param labelMode Pass null to use the default mode.
	 * @param hAlignMode Pass null to use the default mode.
	 * @param vAlignMode Pass null to use the default mode.
	 */
	private void init(ILabelStyles styles, SGLabelMode labelMode, HAlignMode hAlignMode,
			VAlignMode vAlignMode)
	{
		if (styles == null) {
			this.textColor = DEFAULT_TEXT_COLOR;
			this.textSize = DEFAULT_TEXT_SIZE;
			this.font = pa.createFont("sans serif", textSize);
			this.padding = DEFAULT_TEXT_PADDING;
			this.bgColor = DEFAULT_BACKGROUND_COLOR;
			if (labelMode == null) labelMode = DEFAULT_LABEL_MODE;
			if (hAlignMode == null) hAlignMode = DEFAULT_HALIGN_MODE;
			if (vAlignMode == null) vAlignMode = DEAFULT_VALIGN_MODE;
		}
		else {
			this.textColor = styles.getTextColor();
			this.textSize = styles.getTextSize();
			this.font = styles.getFont();
			if (this.font == null) this.font = pa.createFont("sans serif", textSize);
			this.padding = styles.getPadding();
			this.bgColor = styles.getBackgroundColor();
			if (labelMode == null) labelMode = styles.getLabelMode();
			if (hAlignMode == null) hAlignMode = styles.getHAlignMode();
			if (vAlignMode == null) vAlignMode = styles.getVAlignMode();
		}
		this.drawBackground = this.bgColor != null;
		
		updateModes(labelMode, hAlignMode, vAlignMode);
	}
	
	/**
	 * @param labelMode Pass null to use the default mode.
	 * @param hAlignMode Pass null to use the default mode.
	 * @param vAlignMode Pass null to use the default mode.
	 * 
	 * @return True when the node needs to be invalidated.
	 */
	private boolean updateModes(SGLabelMode labelMode, HAlignMode hAlignMode, VAlignMode vAlignMode)
	{
		// Use the default labelMode when the argument is null:
		if (labelMode == null) labelMode = SGLabelMode.SIMPLE; // set default
			
		// Use the default hAlignMode when the argument is null:
		if (hAlignMode == null) { // set default
			switch (labelMode) {
			
				case SIMPLE:
					hAlignMode = HAlignMode.LEFT;
					break;
				
				case BOX:
					hAlignMode = HAlignMode.CENTER;
					break;
				
				case BOX_CENTER:
					hAlignMode = HAlignMode.CENTER;
					break;
				
				default:
					throw new Error("Unexpected label mode '" + labelMode
							+ "' in SGLabel.updateModes().");
			}
		}
		
		// Use the default vAlignMode when the argument is null:
		if (vAlignMode == null) { // set default
			switch (labelMode) {
			
				case SIMPLE:
					vAlignMode = VAlignMode.BOTTOM;
					break;
				
				case BOX:
					vAlignMode = VAlignMode.CENTER;
					break;
				
				case BOX_CENTER:
					vAlignMode = VAlignMode.CENTER;
					break;
				
				default:
					throw new Error("Unexpected label mode '" + labelMode
							+ "' in SGLabel.updateModes().");
			}
		}
		
		// Set to true when requestRedraw() needs to be called:
		boolean requestRedraw = false;
		
		if (this.labelMode != labelMode) {
			this.labelMode = labelMode;
			requestRedraw = true;
			updateSizes();
			// switch (labelMode) {
			//
			// case SIMPLE:
			// break;
			//
			// case BOX:
			// break;
			//
			// case BOX_CENTER:
			// break;
			//
			// default:
			// throw new Error("Unexpected label mode '" + labelMode
			// + "' in SGLabel.updateModes().");
			// }
		}
		
		if (this.hAlignMode != hAlignMode) {
			this.hAlignMode = hAlignMode;
			requestRedraw = true;
			switch (hAlignMode) {
				case LEFT:
					pHAlignMode = PConstants.LEFT;
					break;
				
				case CENTER:
					pHAlignMode = PConstants.CENTER;
					break;
				
				case RIGHT:
					pHAlignMode = PConstants.RIGHT;
					break;
				
				default:
					throw new Error("Unexpected hAlignMode '" + hAlignMode
							+ "' in SGLabel.updateModes().");
			}
		}
		
		if (this.vAlignMode != vAlignMode) {
			this.vAlignMode = vAlignMode;
			requestRedraw = true;
			switch (vAlignMode) {
				case TOP:
					pVAlignMode = PConstants.TOP;
					break;
				
				case CENTER:
					pVAlignMode = PConstants.CENTER;
					break;
				
				case BOTTOM:
					pVAlignMode = PConstants.BOTTOM;
					break;
				
				default:
					throw new Error("Unexpected vAlignMode '" + hAlignMode
							+ "' in SGLabel.updateModes().");
			}
		}
		
		return requestRedraw;
	}
	
	private void updateSizes() {
		boolean requestRedraw = false;
		if (app.g == null) {
			sizeDirty = true;
			System.err.println("The correct width and height could not be calculated "
					+ "because the PGraphics object is not yet available. This could mean "
					+ "that this label is constructed in the constructor of a scene-graph "
					+ "app. Construct the initial nodes in the setup() method instead. "
					+ "The width and height will be calculated when the label is drawn.");
			return;
		}
		
		app.textFont(font, textSize);
		float textHeight = app.textAscent() + app.textDescent();
		vOffset = -(textHeight - textSize) * .5f;
		// println("- ascent: " + app.textAscent() + " - descent: " + app.textDescent()
		// + " - vOffset: " + vOffset);
		switch (labelMode) {
		
			case SIMPLE:
				LblWidth = app.textWidth(label) + 2 * padding;
				LblHeight = textHeight + 2 * padding;
				break;
			
			case BOX:
			case BOX_CENTER:
				LblWidth = explicitWidth;
				textBoxWidth = LblWidth - 2 * padding;
				float minimalHeight = textHeight + 2 * padding;
				if (explicitHeight < minimalHeight) {
					System.err.println("The explicitly set height in '" + this.toString()
							+ "' is not sufficiently high for rendering the text. "
							+ "The height is reset from " + explicitHeight + " to " + minimalHeight
							+ ".");
					explicitHeight = minimalHeight;
				}
				LblHeight = explicitHeight;
				textBoxHeight = LblHeight - 2 * padding;
				break;
			
			default:
				throw new Error("Unexpected label mode '" + labelMode
						+ "' in SGLabel.updateSizes().");
		}
		sizeDirty = false;
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/* @see be.multec.sg.SGNode#dispose(boolean) */
	@Override
	public void dispose(boolean traverse) {
		label = null;
		if (mlString != null) {
			mlString.removeUpdateHandler(this);
			mlString = null;
		}
		font = null;
		
		super.dispose(traverse);
	}
	
	// *********************************************************************************************
	// Accessors:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @return The label-string.
	 */
	public String getLabel() {
		return label;
	}
	
	/**
	 * Sets the label string.
	 * 
	 * @param label
	 */
	public void setLabel(String label) {
		if (this.label == label) return;
		name = label;
		if (mlString != null) {
			mlString.removeUpdateHandler(this);
			mlString = null;
		}
		invalidateLabel();
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	public PFont getFont() {
		return font;
	}
	
	public void setFont(PFont font) {
		if (this.font == font) return;
		this.font = font;
		invalidateLabel();
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	public float getTextSize() {
		return textSize;
	}
	
	public void setTextSize(float fontSize) {
		if (this.textSize == fontSize) return;
		this.textSize = fontSize;
		invalidateLabel();
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/**
	 * @return The text color.
	 */
	public Color getTextColor() {
		return textColor;
	}
	
	/**
	 * Set the text color.
	 * 
	 * @param color The text color.
	 */
	public void setTextColor(Color color) {
		if (this.textColor.equals(color)) return;
		this.textColor = color;
		redraw();
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/**
	 * The current label mode used when drawing this label.
	 * 
	 * @return the labelMode
	 */
	public SGLabelMode getLabelMode() {
		return labelMode;
	}
	
	/**
	 * Sets the label mode to use when drawing this label.
	 * 
	 * @param labelMode the labelMode to set
	 */
	public void setLabelMode(SGLabelMode labelMode) {
		if (updateModes(labelMode, hAlignMode, vAlignMode)) invalidateLabel();
	}
	
	public void setLabelMode(SGLabelMode labelMode, HAlignMode hAlignMode, VAlignMode vAlignMode) {
		if (updateModes(labelMode, hAlignMode, vAlignMode)) invalidateLabel();
	}
	
	/**
	 * The current horizontal text align mode used when drawing the label.
	 * 
	 * @return the horizontal align mode
	 */
	public HAlignMode getHAlignMode() {
		return hAlignMode;
	}
	
	/**
	 * Sets the horizontal text align mode to use when drawing the label.
	 * 
	 * @param hAlignMode the horizontalAlignMode to set
	 */
	public void setHAlignMode(HAlignMode hAlignMode) {
		if (updateModes(labelMode, hAlignMode, vAlignMode)) invalidateLabel();
	}
	
	/**
	 * The current vertical text align mode used when drawing the label. This mode is ignored when
	 * the label mode is <em>BOX</em>.
	 * 
	 * @return the vertical align mode
	 */
	public VAlignMode getVAlignMode() {
		return vAlignMode;
	}
	
	/**
	 * Sets the vertical text align mode to use when drawing the label. This mode is ignored when
	 * the label mode is <em>BOX</em>.
	 * 
	 * @param vAlignMode the verticalAlignMode to set
	 */
	public void setVAlignMode(VAlignMode vAlignMode) {
		if (updateModes(labelMode, hAlignMode, vAlignMode)) invalidateLabel();
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/**
	 * @return The width of the label. Check the <em>Width and height</em> section in the class
	 *         documentation for more information.
	 */
	public float getWidth_TODO() {
		return LblWidth;
	}
	
	/**
	 * @param explicitWidth the explicitWidth to set
	 */
	public void setWidth(float explicitWidth) {
		if (this.explicitWidth == explicitWidth) return;
		this.explicitWidth = LblWidth;
		updateSizes();
		invalidateLabel();
	}
	
	/**
	 * @return The height of the label. Check the <em>Width and height</em> section in the class
	 *         documentation for more information.
	 */
	public float getHeight_TODO() {
		return LblHeight;
	}
	
	/**
	 * @param explicitHeight the explicitHeight to set
	 */
	public void setHeight(float explicitHeight) {
		if (this.explicitHeight == explicitHeight) return;
		this.explicitHeight = LblHeight;
		updateSizes();
		invalidateLabel();
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/**
	 * @return The boxPadding used when the label mode is <em>BOX</em>.
	 */
	public float getPadding() {
		return padding;
	}
	
	/**
	 * Sets the boxPadding used when the label mode is <em>BOX</em>.
	 * 
	 * @param padding the padding to set
	 * 
	 * @return This SGLabel object, so that this method can be chained.
	 */
	public SGLabel setPadding(float padding) {
		if (this.padding == padding) return this;
		this.padding = padding;
		updateSizes();
		invalidateLabel();
		return this;
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/**
	 * @return The background color. No background is drawn when this value is null.
	 */
	public Color getBackgroundColor() {
		return bgColor;
	}
	
	/**
	 * Draws the label background with the given color.
	 * 
	 * @param backgroundColor The color of the background. No background is drawn when this value is
	 *            null.
	 * @return This SGLabel object, so that this method can be chained.
	 */
	public SGLabel setBackground(Color backgroundColor) {
		if (drawBackground && this.bgColor.equals(backgroundColor)) return this;
		this.bgColor = backgroundColor;
		drawBackground = true;
		redraw();
		return this;
	}
	
	/** Stops drawing the background. */
	public void clearBackground() {
		drawBackground = false;
		bgColor = null;
		redraw();
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/**
	 * @return The currently applicable Processing blend mode.
	 */
	public int getBlendMode() {
		return blendMode;
	}
	
	/**
	 * @param blendMode The Processing blend mode to use when drawing this label.
	 * @return This SGLabel object, so that this method can be chained.
	 */
	public SGLabel setBlendMode(int blendMode) {
		if (this.blendMode == blendMode) return this;
		this.blendMode = blendMode;
		redraw();
		return this;
	}
	
	// *********************************************************************************************
	// SGApp methods:
	// ---------------------------------------------------------------------------------------------
	
	/* @see be.multec.sg.d2.SG2DNode#draw(processing.core.PGraphics) */
	@Override
	protected void draw(PGraphics g) {
		if (disposed) return;
		if (label == null) throw new Error("The label string is null (in SGLabel.draw).");
		
		// if (font == null)
		// throw new Error("The font is not set in the SGLabel '" + this.toString() + "'.");
		
		if (sizeDirty) updateSizes();
		
		if (drawBackground) {
			g.fill(bgColor.getRGB(), bgColor.getAlpha());
			g.noStroke();
			g.rectMode(PConstants.CORNER);
			
			float x = 0;
			float y = 0;
			switch (labelMode) {
			
				case SIMPLE:
					if (hAlignMode == HAlignMode.CENTER) x = Math.round(-LblWidth * .5f);
					else if (hAlignMode == HAlignMode.RIGHT) x = Math.round(-LblWidth);
					
					if (vAlignMode == VAlignMode.CENTER) y = Math.round(-LblHeight * .5f);
					else if (vAlignMode == VAlignMode.BOTTOM) y = Math.round(-LblHeight);
					break;
				
				case BOX:
					x = 0;
					y = 0;
					break;
				
				case BOX_CENTER:
					x = Math.round(-LblWidth * .5);
					y = Math.round(-LblHeight * .5);
					break;
				
				default:
					throw new Error("Unexpected label mode '" + labelMode + "' in SGLabel.draw().");
			}
			
			g.rect(x, y, LblWidth, LblHeight);
			// println("- draw background (" + width + " x " + height + ")");
		}
		
		g.fill(textColor.getRGB(), textColor.getAlpha());
		g.textFont(font);
		g.textSize(textSize);
		if (leading > -1) g.textLeading(leading);
		g.textAlign(pHAlignMode, pVAlignMode);
		
		// Write the text:
		switch (labelMode) {
		
			case SIMPLE:
				if (padding == 0) g.text(label, 0, vOffset);
				else {
					float x = 0;
					if (hAlignMode == HAlignMode.LEFT) x = padding;
					else if (hAlignMode == HAlignMode.RIGHT) x = -padding;
					
					float y = 0;
					if (vAlignMode == VAlignMode.TOP) y = padding;
					else if (vAlignMode == VAlignMode.BOTTOM) y = -padding;
					
					g.text(label, x, y + vOffset);
				}
				break;
			
			case BOX:
				g.text(label, padding, padding + vOffset, textBoxWidth, textBoxHeight);
				break;
			
			case BOX_CENTER:
				g.text(label, Math.round(-LblWidth * .5f) + padding, Math.round(-LblHeight * .5f)
						+ padding + vOffset, textBoxWidth, textBoxHeight);
				break;
			
			default:
				throw new Error("Unexpected label mode '" + labelMode + "' in SGLabel.draw().");
		}
	}
	
	/* @see be.multec.sg.SGNode#updateLocalBounds(java.awt.Rectangle) */
	@Override
	protected void updateLocalBounds(Rectangle bounds) {
		
		bounds.y = 0;
		switch (labelMode) {
		
			case SIMPLE:
				if (hAlignMode == HAlignMode.LEFT) bounds.x = 0;
				else if (hAlignMode == HAlignMode.CENTER) bounds.x = Math.round(-LblWidth * .5f);
				else if (hAlignMode == HAlignMode.RIGHT) bounds.x = Math.round(-LblWidth);
				
				if (vAlignMode == VAlignMode.TOP) bounds.y = 0;
				else if (vAlignMode == VAlignMode.CENTER) bounds.y = Math.round(-LblHeight * .5f);
				else if (vAlignMode == VAlignMode.BOTTOM) bounds.y = Math.round(-LblHeight);
				
				bounds.width = (int) Math.ceil(LblWidth);
				bounds.height = (int) Math.ceil(LblHeight);
				break;
			
			case BOX:
				bounds.x = 0;
				bounds.y = 0;
				bounds.width = (int) Math.ceil(textBoxWidth);
				bounds.height = (int) Math.ceil(textBoxHeight);
				break;
			
			case BOX_CENTER:
				bounds.x = Math.round(-LblWidth * .5f);
				bounds.y = Math.round(-LblHeight * .5f);
				bounds.width = (int) Math.ceil(textBoxWidth);
				bounds.height = (int) Math.ceil(textBoxHeight);
				break;
			
			default:
				throw new Error("Unexpected label mode '" + labelMode
						+ "' in SGLabel.updateLocalBounds().");
		}
	}
	
	/* @see be.multec.sg.SGNode#mouseHitTest() */
	@Override
	protected boolean contains(float x, float y) {
		if (LblWidth == 0 || LblHeight == 0) return false;
		return x >= 0 && x < LblWidth && y >= 0 && y < LblHeight;
	}
	
	// *********************************************************************************************
	// MLString methods:
	// ---------------------------------------------------------------------------------------------
	
	/* @see be.multec.sg.utils.IMLStringUpdateHandler#stringUpdated(be.multec.sg.utils.MLString) */
	@Override
	public void stringUpdated(MLString mlString) {
		label = mlString.toString();
		invalidateLabel();
	}
	
	// *********************************************************************************************
	// SGApp system methods:
	// ---------------------------------------------------------------------------------------------
	
	private void invalidateLabel() {
		invalidateLocalBounds();
		redraw();
	}
	
}
