package be.multec.sg.styles;

import java.awt.Color;

import processing.core.PFont;
import be.multec.sg.d2.SGLabel;
import be.multec.sg.d2.SGLabel.HAlignMode;
import be.multec.sg.d2.SGLabel.SGLabelMode;
import be.multec.sg.d2.SGLabel.VAlignMode;

/**
 * Base implementation of the ILabelStyle interface.
 * 
 * @see ILabelStyles
 * 
 * @author Wouter Van den Broeck
 */
public class SGLabelStyles implements ILabelStyles {
	
	// *********************************************************************************************
	// Attributes:
	// ---------------------------------------------------------------------------------------------
	
	private Color color = SGLabel.DEFAULT_TEXT_COLOR;
	private PFont font;
	private float size = SGLabel.DEFAULT_TEXT_SIZE;
	private float padding = SGLabel.DEFAULT_TEXT_PADDING;
	private Color bgColor = SGLabel.DEFAULT_BACKGROUND_COLOR;
	private SGLabelMode labelMode = SGLabel.DEFAULT_LABEL_MODE;
	private HAlignMode hAlignMode = SGLabel.DEFAULT_HALIGN_MODE;
	private VAlignMode vAlignMode = SGLabel.DEAFULT_VALIGN_MODE;
	
	// *********************************************************************************************
	// Constructors:
	// ---------------------------------------------------------------------------------------------
	
	public SGLabelStyles() {}
	
	// *********************************************************************************************
	// Accessors:
	// ---------------------------------------------------------------------------------------------
	
	/* @see be.multec.sg.styles.ILabelStyle#getTextColor() */
	@Override
	public Color getTextColor() {
		return color;
	}
	
	/* @see be.multec.sg.styles.ILabelStyle#setTextColor(java.awt.Color) */
	@Override
	public void setTextColor(Color color) {
		this.color = color;
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/* @see be.multec.sg.styles.ILabelStyle#getFont() */
	@Override
	public PFont getFont() {
		return font;
	}
	
	/* @see be.multec.sg.styles.ILabelStyle#setFont(processing.core.PFont) */
	@Override
	public void setFont(PFont font) {
		this.font = font;
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/* @see be.multec.sg.styles.ILabelStyle#getTextSize() */
	@Override
	public float getTextSize() {
		return size;
	}
	
	/* @see be.multec.sg.styles.ILabelStyle#setTextSize(float) */
	@Override
	public void setTextSize(float size) {
		this.size = size;
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/* @see be.multec.sg.styles.ILabelStyle#getPadding() */
	@Override
	public float getPadding() {
		return padding;
	}
	
	/* @see be.multec.sg.styles.ILabelStyle#setPadding(float) */
	@Override
	public void setPadding(float padding) {
		this.padding = padding;
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/* @see be.multec.sg.styles.ILabelStyle#getBackgroundColor() */
	@Override
	public Color getBackgroundColor() {
		return bgColor;
	}
	
	/* @see be.multec.sg.styles.ILabelStyle#setBackgroundColor(java.awt.Color) */
	@Override
	public void setBackgroundColor(Color color) {
		this.bgColor = color;
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/* @see be.multec.sg.styles.ILabelStyle#getLabelMode() */
	@Override
	public SGLabelMode getLabelMode() {
		return labelMode;
	}
	
	/* @see be.multec.sg.styles.ILabelStyle#setLabelMode(be.multec.sg.d2.SGLabel.SGLabelMode) */
	@Override
	public void setLabelMode(SGLabelMode mode) {
		this.labelMode = mode;
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/* @see be.multec.sg.styles.ILabelStyle#getHAlignMode() */
	@Override
	public HAlignMode getHAlignMode() {
		return hAlignMode;
	}
	
	/* @see be.multec.sg.styles.ILabelStyle#setHAlignMode(be.multec.sg.d2.SGLabel.HAlignMode) */
	@Override
	public void setHAlignMode(HAlignMode mode) {
		this.hAlignMode = mode;
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/* @see be.multec.sg.styles.ILabelStyle#getVAlignMode() */
	@Override
	public VAlignMode getVAlignMode() {
		return vAlignMode;
	}
	
	/* @see be.multec.sg.styles.ILabelStyle#setVAlignMode(be.multec.sg.d2.SGLabel.VAlignMode) */
	@Override
	public void setVAlignMode(VAlignMode mode) {
		this.vAlignMode = mode;
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/*
	 * @see be.multec.sg.styles.ILabelStyle#setAlignModes(be.multec.sg.d2.SGLabel.HAlignMode,
	 * be.multec.sg.d2.SGLabel.VAlignMode)
	 */
	@Override
	public void setAlignModes(HAlignMode ham, VAlignMode vam) {
		this.hAlignMode = ham;
		this.vAlignMode = vam;
	}
	
	// *********************************************************************************************
	// Methods:
	// ---------------------------------------------------------------------------------------------
	
}
