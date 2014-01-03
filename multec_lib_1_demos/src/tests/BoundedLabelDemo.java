package tests;

import java.awt.Color;
import java.awt.Rectangle;

import processing.core.PGraphics;
import be.multec.sg.SGApp;
import be.multec.sg.SGNode;
import be.multec.sg.SGWindow;
import be.multec.sg.d2.SGLabel;
import be.multec.sg.d2.SGLabel.HAlignMode;
import be.multec.sg.d2.SGLabel.SGLabelMode;
import be.multec.sg.d2.SGLabel.VAlignMode;
import be.multec.sg.modifiers.IModifier;
import be.multec.sg.styles.ILabelStyles;
import be.multec.sg.styles.SGLabelStyles;

/**
 * TODO: Test multi-line labels in a box.
 * 
 * @author Wouter Van den Broeck
 */
public class BoundedLabelDemo extends SGWindow {
	
	// *********************************************************************************************
	// main():
	// ---------------------------------------------------------------------------------------------
	
	public static void main(String[] args) {
		BoundedLabelDemo app = new BoundedLabelDemo();
		app.open("Bounded Labels Demo", 50, 30, 750, 750, new Color(0xFFCC00));
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
		
		Color bgColor = new Color(0x77FFFFFF, true);
		
		ILabelStyles normalStyles = new SGLabelStyles();
		normalStyles.setTextSize(18);
		normalStyles.setFont(loadFont("TheSans-Light-36.vlw"));
		normalStyles.setBackgroundColor(bgColor);
		
		ILabelStyles largerStyles = new SGLabelStyles();
		largerStyles.setTextSize(36);
		largerStyles.setFont(loadFont("TheSans-Light-36.vlw"));
		largerStyles.setBackgroundColor(bgColor);
		
		ILabelStyles paddingStyles = new SGLabelStyles();
		paddingStyles.setTextSize(18);
		paddingStyles.setFont(loadFont("TheSans-Light-36.vlw"));
		paddingStyles.setBackgroundColor(bgColor);
		paddingStyles.setPadding(5);
		
		ILabelStyles headerStyles = new SGLabelStyles();
		headerStyles.setTextSize(22);
		headerStyles.setFont(loadFont("TheSans-Light-36.vlw"));
		
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		
		addNode(new SGLabel(this, "Label mode: SIMPLE - no padding", headerStyles), 20, 30);
		
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		
		addNode(new CrossMark(this), 20, 40);
		addBounded(new SGLabel(this, "Label (left, top)", normalStyles, SGLabelMode.SIMPLE,
				HAlignMode.LEFT, VAlignMode.TOP).setBackground(bgColor), 20, 40);
		
		addNode(new CrossMark(this), 340, 40);
		addBounded(new SGLabel(this, "Label (center, top)", normalStyles, SGLabelMode.SIMPLE,
				HAlignMode.CENTER, VAlignMode.TOP).setBackground(bgColor), 340, 40);
		
		addNode(new CrossMark(this), 660, 40);
		addBounded(new SGLabel(this, "Label (right, top)", normalStyles, SGLabelMode.SIMPLE,
				HAlignMode.RIGHT, VAlignMode.TOP).setBackground(bgColor), 660, 40);
		
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		
		addNode(new CrossMark(this), 20, 70);
		addBounded(new SGLabel(this, "Label (36p)", largerStyles, SGLabelMode.SIMPLE,
				HAlignMode.LEFT, VAlignMode.TOP).setBackground(bgColor), 20, 70);
		
		addNode(new CrossMark(this), 340, 70);
		addBounded(new SGLabel(this, "Label (36p)", largerStyles, SGLabelMode.SIMPLE,
				HAlignMode.CENTER, VAlignMode.TOP).setBackground(bgColor), 340, 70);
		
		addNode(new CrossMark(this), 660, 70);
		addBounded(new SGLabel(this, "Label (36p)", largerStyles, SGLabelMode.SIMPLE,
				HAlignMode.RIGHT, VAlignMode.TOP).setBackground(bgColor), 660, 70);
		
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		
		addNode(new CrossMark(this), 20, 129);
		addBounded(new SGLabel(this, "Label (left, center)", normalStyles, SGLabelMode.SIMPLE,
				HAlignMode.LEFT, VAlignMode.CENTER).setBackground(bgColor), 20, 129);
		
		addNode(new CrossMark(this), 340, 129);
		addBounded(new SGLabel(this, "Label (center, center)", normalStyles, SGLabelMode.SIMPLE,
				HAlignMode.CENTER, VAlignMode.CENTER).setBackground(bgColor), 340, 129);
		
		addNode(new CrossMark(this), 660, 129);
		addBounded(new SGLabel(this, "Label (right, center)", normalStyles, SGLabelMode.SIMPLE,
				HAlignMode.RIGHT, VAlignMode.CENTER).setBackground(bgColor), 660, 129);
		
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		
		addNode(new CrossMark(this), 20, 170);
		addBounded(new SGLabel(this, "Label (left, bottom)", normalStyles, SGLabelMode.SIMPLE,
				HAlignMode.LEFT, VAlignMode.BOTTOM).setBackground(bgColor), 20, 170);
		
		addNode(new CrossMark(this), 340, 170);
		addBounded(new SGLabel(this, "Label (center, bottom)", normalStyles, SGLabelMode.SIMPLE,
				HAlignMode.CENTER, VAlignMode.BOTTOM).setBackground(bgColor), 340, 170);
		
		addNode(new CrossMark(this), 660, 170);
		addBounded(new SGLabel(this, "Label (right, bottom)", normalStyles, SGLabelMode.SIMPLE,
				HAlignMode.RIGHT, VAlignMode.BOTTOM).setBackground(bgColor), 660, 170);
		
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		
		addNode(new CrossMark(this), 20, 220);
		addBounded(new SGLabel(this, "Label (36p)", largerStyles, SGLabelMode.SIMPLE,
				HAlignMode.LEFT, VAlignMode.BOTTOM).setBackground(bgColor), 20, 220);
		
		addNode(new CrossMark(this), 340, 220);
		addBounded(new SGLabel(this, "Label (36p)", largerStyles, SGLabelMode.SIMPLE,
				HAlignMode.CENTER, VAlignMode.BOTTOM).setBackground(bgColor), 340, 220);
		
		addNode(new CrossMark(this), 660, 220);
		addBounded(new SGLabel(this, "Label (36p)", largerStyles, SGLabelMode.SIMPLE,
				HAlignMode.RIGHT, VAlignMode.BOTTOM).setBackground(bgColor), 660, 220);
		
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		
		addNode(new SGLabel(this, "Label mode: SIMPLE - padding: 5", headerStyles), 20, 260);
		
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		
		addNode(new CrossMark(this), 20, 270);
		addBounded(new SGLabel(this, "Label (left, top)", paddingStyles, SGLabelMode.SIMPLE,
				HAlignMode.LEFT, VAlignMode.TOP).setBackground(bgColor), 20, 270);
		
		addNode(new CrossMark(this), 340, 270);
		addBounded(new SGLabel(this, "Label (center, top)", paddingStyles, SGLabelMode.SIMPLE,
				HAlignMode.CENTER, VAlignMode.TOP).setBackground(bgColor), 340, 270);
		
		addNode(new CrossMark(this), 660, 270);
		addBounded(new SGLabel(this, "Label (right, top)", paddingStyles, SGLabelMode.SIMPLE,
				HAlignMode.RIGHT, VAlignMode.TOP).setBackground(bgColor), 660, 270);
		
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		
		addNode(new CrossMark(this), 20, 325);
		addBounded(new SGLabel(this, "Label (left, center)", paddingStyles, SGLabelMode.SIMPLE,
				HAlignMode.LEFT, VAlignMode.CENTER).setBackground(bgColor), 20, 325);
		
		addNode(new CrossMark(this), 340, 325);
		addBounded(new SGLabel(this, "Label (center, center)", paddingStyles, SGLabelMode.SIMPLE,
				HAlignMode.CENTER, VAlignMode.CENTER).setBackground(bgColor), 340, 325);
		
		addNode(new CrossMark(this), 660, 325);
		addBounded(new SGLabel(this, "Label (right, center)", paddingStyles, SGLabelMode.SIMPLE,
				HAlignMode.RIGHT, VAlignMode.CENTER).setBackground(bgColor), 660, 325);
		
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		
		addNode(new CrossMark(this), 20, 380);
		addBounded(new SGLabel(this, "Label (left, bottom)", paddingStyles, SGLabelMode.SIMPLE,
				HAlignMode.LEFT, VAlignMode.BOTTOM).setBackground(bgColor), 20, 380);
		
		addNode(new CrossMark(this), 340, 380);
		addBounded(new SGLabel(this, "Label (center, bottom)", paddingStyles, SGLabelMode.SIMPLE,
				HAlignMode.CENTER, VAlignMode.BOTTOM).setBackground(bgColor), 340, 380);
		
		addNode(new CrossMark(this), 660, 380);
		addBounded(new SGLabel(this, "Label (right, bottom)", paddingStyles, SGLabelMode.SIMPLE,
				HAlignMode.RIGHT, VAlignMode.BOTTOM).setBackground(bgColor), 660, 380);
		
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		
		addNode(new SGLabel(this, "Label mode: BOX", headerStyles), 20, 420);
		
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		
		addNode(new CrossMark(this), 20, 430);
		addBounded(new SGLabel(this, "Label (left, top)", normalStyles, SGLabelMode.BOX,
				HAlignMode.LEFT, VAlignMode.TOP, 200, 40).setBackground(bgColor), 20, 430);
		
		addNode(new CrossMark(this), 240, 430);
		addBounded(new SGLabel(this, "Label (center, top)", normalStyles, SGLabelMode.BOX,
				HAlignMode.CENTER, VAlignMode.TOP, 200, 40).setBackground(bgColor), 240, 430);
		
		addNode(new CrossMark(this), 460, 430);
		addBounded(new SGLabel(this, "Label (right, top)", normalStyles, SGLabelMode.BOX,
				HAlignMode.RIGHT, VAlignMode.TOP, 200, 40).setBackground(bgColor), 460, 430);
		
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		
		addNode(new CrossMark(this), 20, 480);
		addBounded(new SGLabel(this, "Label (left, center)", normalStyles, SGLabelMode.BOX,
				HAlignMode.LEFT, VAlignMode.CENTER, 200, 40).setBackground(bgColor), 20, 480);
		
		addNode(new CrossMark(this), 240, 480);
		addBounded(new SGLabel(this, "Label (center, center)", normalStyles, SGLabelMode.BOX,
				HAlignMode.CENTER, VAlignMode.CENTER, 200, 40).setBackground(bgColor), 240, 480);
		
		addNode(new CrossMark(this), 460, 480);
		addBounded(new SGLabel(this, "Label (right, center)", normalStyles, SGLabelMode.BOX,
				HAlignMode.RIGHT, VAlignMode.CENTER, 200, 40).setBackground(bgColor), 460, 480);
		
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		
		addNode(new CrossMark(this), 20, 530);
		addBounded(new SGLabel(this, "Label (left, bottom)", normalStyles, SGLabelMode.BOX,
				HAlignMode.LEFT, VAlignMode.BOTTOM, 200, 40).setBackground(bgColor), 20, 530);
		
		addNode(new CrossMark(this), 240, 530);
		addBounded(new SGLabel(this, "Label (center, bottom)", normalStyles, SGLabelMode.BOX,
				HAlignMode.CENTER, VAlignMode.BOTTOM, 200, 40).setBackground(bgColor), 240, 530);
		
		addNode(new CrossMark(this), 460, 530);
		addBounded(new SGLabel(this, "Label (right, bottom)", normalStyles, SGLabelMode.BOX,
				HAlignMode.RIGHT, VAlignMode.BOTTOM, 200, 40).setBackground(bgColor), 460, 530);
		
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		
		addNode(new SGLabel(this, "Label mode: BOX_CENTER", headerStyles), 20, 610);
		
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		
		addNode(new CrossMark(this), 120, 630);
		addBounded(new SGLabel(this, "Label (left, bottom)", normalStyles, SGLabelMode.BOX_CENTER,
				HAlignMode.LEFT, VAlignMode.BOTTOM, 200, 40).setBackground(bgColor), 120, 630);
		
		addNode(new CrossMark(this), 340, 630);
		addBounded(
				new SGLabel(this, "Label (center, bottom)", normalStyles, SGLabelMode.BOX_CENTER,
						HAlignMode.CENTER, VAlignMode.BOTTOM, 200, 40).setBackground(bgColor), 340,
				630);
		
		addNode(new CrossMark(this), 560, 630);
		addBounded(new SGLabel(this, "Label (right, bottom)", normalStyles, SGLabelMode.BOX_CENTER,
				HAlignMode.RIGHT, VAlignMode.BOTTOM, 200, 40).setBackground(bgColor), 560, 630);
		
	}
	
	private void addBounded(SGNode node, float x, float y) {
		addNode(new Outlined(this, node), x, y);
	}
	
	// *********************************************************************************************
	// Classes:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * A node that draws a rect around its bounds.
	 */
	class Outlined extends SGNode {
		
		public Outlined(SGApp app, SGNode node) {
			super(app);
			addNode(node);
			addModifier(new IModifier() {
				@Override
				public void apply(SGNode node) {
					node.redraw();
				}
			});
		}
		
		/* @see be.multec.sg.d2.SGRect#draw(processing.core.PGraphics) */
		@Override
		protected void draw(PGraphics g) {
			Rectangle bounds = getCompositeBounds();
			// System.out.println("- bounds: " + rectStr(bounds));
			g.noFill();
			g.stroke(0xFFBE0000);
			g.strokeWeight(1);
			g.rect(bounds.x - getX() - 1, bounds.y - getY() - 1, bounds.width + 1,
					bounds.height + 1);
		}
		
	}
	
}
