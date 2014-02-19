package be.multec.sg.styles;

import java.awt.Color;

import processing.core.PFont;
import be.multec.sg.nodes.SGLabel.HAlignMode;
import be.multec.sg.nodes.SGLabel.SGLabelMode;
import be.multec.sg.nodes.SGLabel.VAlignMode;

/**
 * Interface for classes that provide all the styles for SGLabel nodes.
 * 
 * @see SGLabelStyles
 * 
 * @author Wouter Van den Broeck
 */
public interface ILabelStyles {
	
	Color getTextColor();
	
	void setTextColor(Color color);
	
	PFont getFont();
	
	void setFont(PFont font);
	
	float getTextSize();
	
	void setTextSize(float size);
	
	float getPadding();
	
	void setPadding(float padding);
	
	Color getBackgroundColor();
	
	void setBackgroundColor(Color color);
	
	SGLabelMode getLabelMode();
	
	void setLabelMode(SGLabelMode mode);
	
	HAlignMode getHAlignMode();
	
	void setHAlignMode(HAlignMode mode);
	
	VAlignMode getVAlignMode();
	
	void setVAlignMode(VAlignMode mode);
	
	void setAlignModes(HAlignMode ham, VAlignMode vam);
	
}
