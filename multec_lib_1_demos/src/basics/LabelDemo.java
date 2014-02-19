package basics;

import java.awt.Color;
import java.awt.Rectangle;

import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;
import be.multec.sg.SGApp;
import be.multec.sg.SGWindow;
import be.multec.sg.nodes.SGLabel;
import be.multec.sg.nodes.SGNode;
import be.multec.sg.nodes.SGLabel.HAlignMode;
import be.multec.sg.nodes.SGLabel.SGLabelMode;
import be.multec.sg.nodes.SGLabel.VAlignMode;
import be.multec.sg.styles.ILabelStyles;
import be.multec.sg.styles.SGLabelStyles;

/**
 * TODO: Test multi-line labels in a box.
 * 
 * @author Wouter Van den Broeck
 */
public class LabelDemo extends SGWindow {
	
	// *********************************************************************************************
	// main():
	// ---------------------------------------------------------------------------------------------
	
	public static void main(String[] args) {
		LabelDemo app = new LabelDemo();
		//app.setRenderer(PConstants.P2D); // OK
		app.open("LabelDemo", 50, 30, 750, 750, new Color(0xFFCC00));
	}
	
	// *********************************************************************************************
	// Components:
	// ---------------------------------------------------------------------------------------------
	
	private class CrossMark extends SGNode {
		
		public CrossMark(SGApp app) {
			super(app);
		}
		
		/* @see be.multec.sg.SGNode#draw() */
		@Override
		protected void draw(PGraphics g) {
			g.stroke(0x66AA0000);
			g.line(0, -9, 0, 10);
			g.line(-9, 0, 10, 0);
		}
		
		/* @see be.multec.sg.SGNode#updateLocalBounds(java.awt.Rectangle) */
		@Override
		protected void updateLocalBounds(Rectangle localBounds) {
			localBounds.setBounds(-9, -9, 20, 20);
		}
		
	}
	
	// *********************************************************************************************
	// Methods:
	// ---------------------------------------------------------------------------------------------
	
	/* @see processing.core.PApplet#setup() */
	@Override
	public void setup() {
		noLoop();
		
		PFont TheSans_ExtraLight_16 = loadFont("TheSans_ExtraLight_16.vlw");
		PFont TheSans_ExtraLight_32 = loadFont("TheSans_ExtraLight_32.vlw");
		
		Color bgColor = new Color(0x77FFFFFF, true);
		
		ILabelStyles normalStyles = new SGLabelStyles();
		normalStyles.setTextSize(16);
		normalStyles.setFont(TheSans_ExtraLight_16);
		normalStyles.setBackgroundColor(bgColor);
		
		ILabelStyles largerStyles = new SGLabelStyles();
		largerStyles.setTextSize(32);
		largerStyles.setFont(TheSans_ExtraLight_32);
		largerStyles.setBackgroundColor(bgColor);
		
		ILabelStyles paddingStyles = new SGLabelStyles();
		paddingStyles.setTextSize(16);
		paddingStyles.setFont(TheSans_ExtraLight_16);
		paddingStyles.setBackgroundColor(bgColor);
		paddingStyles.setPadding(5);
		
		ILabelStyles headerStyles = new SGLabelStyles();
		headerStyles.setTextSize(24);
		headerStyles.setFont(TheSans_ExtraLight_32);
		
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		
		addNode(new SGLabel(this, "Label mode: SIMPLE - no padding", headerStyles), 20, 30);
		
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		
		addNode(new CrossMark(this), 20, 40);
		addNode(new SGLabel(this, "Label (left, top)", normalStyles, SGLabelMode.SIMPLE,
				HAlignMode.LEFT, VAlignMode.TOP), 20, 40);
		
		addNode(new CrossMark(this), 340, 40);
		addNode(new SGLabel(this, "Label (center, top)", normalStyles, SGLabelMode.SIMPLE,
				HAlignMode.CENTER, VAlignMode.TOP), 340, 40);
		
		addNode(new CrossMark(this), 660, 40);
		addNode(new SGLabel(this, "Label (right, top)", normalStyles, SGLabelMode.SIMPLE,
				HAlignMode.RIGHT, VAlignMode.TOP), 660, 40);
		
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		
		addNode(new CrossMark(this), 20, 70);
		addNode(new SGLabel(this, "Label (36p)", largerStyles, SGLabelMode.SIMPLE, HAlignMode.LEFT,
				VAlignMode.TOP), 20, 70);
		
		addNode(new CrossMark(this), 340, 70);
		addNode(new SGLabel(this, "Label (36p)", largerStyles, SGLabelMode.SIMPLE,
				HAlignMode.CENTER, VAlignMode.TOP), 340, 70);
		
		addNode(new CrossMark(this), 660, 70);
		addNode(new SGLabel(this, "Label (36p)", largerStyles, SGLabelMode.SIMPLE,
				HAlignMode.RIGHT, VAlignMode.TOP), 660, 70);
		
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		
		addNode(new CrossMark(this), 20, 129);
		addNode(new SGLabel(this, "Label (left, center)", normalStyles, SGLabelMode.SIMPLE,
				HAlignMode.LEFT, VAlignMode.CENTER), 20, 129);
		
		addNode(new CrossMark(this), 340, 129);
		addNode(new SGLabel(this, "Label (center, center)", normalStyles, SGLabelMode.SIMPLE,
				HAlignMode.CENTER, VAlignMode.CENTER), 340, 129);
		
		addNode(new CrossMark(this), 660, 129);
		addNode(new SGLabel(this, "Label (right, center)", normalStyles, SGLabelMode.SIMPLE,
				HAlignMode.RIGHT, VAlignMode.CENTER), 660, 129);
		
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		
		addNode(new CrossMark(this), 20, 170);
		addNode(new SGLabel(this, "Label (left, bottom)", normalStyles, SGLabelMode.SIMPLE,
				HAlignMode.LEFT, VAlignMode.BOTTOM), 20, 170);
		
		addNode(new CrossMark(this), 340, 170);
		addNode(new SGLabel(this, "Label (center, bottom)", normalStyles, SGLabelMode.SIMPLE,
				HAlignMode.CENTER, VAlignMode.BOTTOM), 340, 170);
		
		addNode(new CrossMark(this), 660, 170);
		addNode(new SGLabel(this, "Label (right, bottom)", normalStyles, SGLabelMode.SIMPLE,
				HAlignMode.RIGHT, VAlignMode.BOTTOM), 660, 170);
		
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		
		addNode(new CrossMark(this), 20, 220);
		addNode(new SGLabel(this, "Label (36p)", largerStyles, SGLabelMode.SIMPLE, HAlignMode.LEFT,
				VAlignMode.BOTTOM), 20, 220);
		
		addNode(new CrossMark(this), 340, 220);
		addNode(new SGLabel(this, "Label (36p)", largerStyles, SGLabelMode.SIMPLE,
				HAlignMode.CENTER, VAlignMode.BOTTOM), 340, 220);
		
		addNode(new CrossMark(this), 660, 220);
		addNode(new SGLabel(this, "Label (36p)", largerStyles, SGLabelMode.SIMPLE,
				HAlignMode.RIGHT, VAlignMode.BOTTOM), 660, 220);
		
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		
		addNode(new SGLabel(this, "Label mode: SIMPLE - padding: 5", headerStyles), 20, 260);
		
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		
		addNode(new CrossMark(this), 20, 270);
		addNode(new SGLabel(this, "Label (left, top)", paddingStyles, SGLabelMode.SIMPLE,
				HAlignMode.LEFT, VAlignMode.TOP), 20, 270);
		
		addNode(new CrossMark(this), 340, 270);
		addNode(new SGLabel(this, "Label (center, top)", paddingStyles, SGLabelMode.SIMPLE,
				HAlignMode.CENTER, VAlignMode.TOP), 340, 270);
		
		addNode(new CrossMark(this), 660, 270);
		addNode(new SGLabel(this, "Label (right, top)", paddingStyles, SGLabelMode.SIMPLE,
				HAlignMode.RIGHT, VAlignMode.TOP), 660, 270);
		
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		
		addNode(new CrossMark(this), 20, 325);
		addNode(new SGLabel(this, "Label (left, center)", paddingStyles, SGLabelMode.SIMPLE,
				HAlignMode.LEFT, VAlignMode.CENTER), 20, 325);
		
		addNode(new CrossMark(this), 340, 325);
		addNode(new SGLabel(this, "Label (center, center)", paddingStyles, SGLabelMode.SIMPLE,
				HAlignMode.CENTER, VAlignMode.CENTER), 340, 325);
		
		addNode(new CrossMark(this), 660, 325);
		addNode(new SGLabel(this, "Label (right, center)", paddingStyles, SGLabelMode.SIMPLE,
				HAlignMode.RIGHT, VAlignMode.CENTER), 660, 325);
		
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		
		addNode(new CrossMark(this), 20, 380);
		addNode(new SGLabel(this, "Label (left, bottom)", paddingStyles, SGLabelMode.SIMPLE,
				HAlignMode.LEFT, VAlignMode.BOTTOM), 20, 380);
		
		addNode(new CrossMark(this), 340, 380);
		addNode(new SGLabel(this, "Label (center, bottom)", paddingStyles, SGLabelMode.SIMPLE,
				HAlignMode.CENTER, VAlignMode.BOTTOM), 340, 380);
		
		addNode(new CrossMark(this), 660, 380);
		addNode(new SGLabel(this, "Label (right, bottom)", paddingStyles, SGLabelMode.SIMPLE,
				HAlignMode.RIGHT, VAlignMode.BOTTOM), 660, 380);
		
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		
		addNode(new SGLabel(this, "Label mode: BOX", headerStyles), 20, 420);
		
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		
		addNode(new CrossMark(this), 20, 430);
		addNode(new SGLabel(this, "Label (left, top)", normalStyles, SGLabelMode.BOX,
				HAlignMode.LEFT, VAlignMode.TOP, 200, 40), 20, 430);
		
		addNode(new CrossMark(this), 240, 430);
		addNode(new SGLabel(this, "Label (center, top)", normalStyles, SGLabelMode.BOX,
				HAlignMode.CENTER, VAlignMode.TOP, 200, 40), 240, 430);
		
		addNode(new CrossMark(this), 460, 430);
		addNode(new SGLabel(this, "Label (right, top)", normalStyles, SGLabelMode.BOX,
				HAlignMode.RIGHT, VAlignMode.TOP, 200, 40), 460, 430);
		
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		
		addNode(new CrossMark(this), 20, 480);
		addNode(new SGLabel(this, "Label (left, center)", normalStyles, SGLabelMode.BOX,
				HAlignMode.LEFT, VAlignMode.CENTER, 200, 40), 20, 480);
		
		addNode(new CrossMark(this), 240, 480);
		addNode(new SGLabel(this, "Label (center, center)", normalStyles, SGLabelMode.BOX,
				HAlignMode.CENTER, VAlignMode.CENTER, 200, 40), 240, 480);
		
		addNode(new CrossMark(this), 460, 480);
		addNode(new SGLabel(this, "Label (right, center)", normalStyles, SGLabelMode.BOX,
				HAlignMode.RIGHT, VAlignMode.CENTER, 200, 40), 460, 480);
		
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		
		addNode(new CrossMark(this), 20, 530);
		addNode(new SGLabel(this, "Label (left, bottom)", normalStyles, SGLabelMode.BOX,
				HAlignMode.LEFT, VAlignMode.BOTTOM, 200, 40), 20, 530);
		
		addNode(new CrossMark(this), 240, 530);
		addNode(new SGLabel(this, "Label (center, bottom)", normalStyles, SGLabelMode.BOX,
				HAlignMode.CENTER, VAlignMode.BOTTOM, 200, 40), 240, 530);
		
		addNode(new CrossMark(this), 460, 530);
		addNode(new SGLabel(this, "Label (right, bottom)", normalStyles, SGLabelMode.BOX,
				HAlignMode.RIGHT, VAlignMode.BOTTOM, 200, 40), 460, 530);
		
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		
		addNode(new SGLabel(this, "Label mode: BOX_CENTER", headerStyles), 20, 610);
		
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		
		addNode(new CrossMark(this), 120, 630);
		addNode(new SGLabel(this, "Label (left, bottom)", normalStyles, SGLabelMode.BOX_CENTER,
				HAlignMode.LEFT, VAlignMode.BOTTOM, 200, 40), 120, 630);
		
		addNode(new CrossMark(this), 340, 630);
		addNode(new SGLabel(this, "Label (center, bottom)", normalStyles, SGLabelMode.BOX_CENTER,
				HAlignMode.CENTER, VAlignMode.BOTTOM, 200, 40), 340, 630);
		
		addNode(new CrossMark(this), 560, 630);
		addNode(new SGLabel(this, "Label (right, bottom)", normalStyles, SGLabelMode.BOX_CENTER,
				HAlignMode.RIGHT, VAlignMode.BOTTOM, 200, 40), 560, 630);
		
	}
	
}
