package tests;

import java.awt.Color;
import java.awt.Rectangle;

import processing.core.PGraphics;
import be.multec.sg.SGApp;
import be.multec.sg.SGWindow;
import be.multec.sg.nodes.SGImage;
import be.multec.sg.nodes.SGImage.SGImageMode;
import be.multec.sg.nodes.SGNode;
import be.multec.sg.nodes.SGRect;
import be.multec.sg.nodes.controllers.NodeController;

/**
 * @author Wouter Van den Broeck
 */
public class BoundedImageDemo extends SGWindow {
	
	// *********************************************************************************************
	// main():
	// ---------------------------------------------------------------------------------------------
	
	public static void main(String[] args) {
		new BoundedImageDemo().open("ImageDemo", 50, 30, 1050, 408, new Color(0xffcc00));
	}
	
	// *********************************************************************************************
	// Methods:
	// ---------------------------------------------------------------------------------------------
	
	/* @see processing.core.PApplet#setup() */
	@Override
	public void setup() {
		noLoop();
		
		Color markerColor = new Color(0xffffff);
		String imagePath = "data/image_demo_stripes.png";
		
		addNode(new SGRect(this, 60, 60, markerColor), 30, 30);
		addBounded(new SGImage(this, imagePath, SGImageMode.SIMPLE), 35, 35);
		
		addNode(new SGRect(this, 40, 40, markerColor), 150, 40);
		addBounded(new SGImage(this, imagePath, SGImageMode.CROP, 30, 30), 155, 45);
		
		// different repeat cases:
		addNode(new SGRect(this, 290, 290, markerColor), 225, 5);
		addBounded(new SGImage(this, imagePath, SGImageMode.REPEAT, 280, 280), 230, 10);
		addNode(new SGRect(this, 290, 30, markerColor), 225, 305);
		addBounded(new SGImage(this, imagePath, SGImageMode.REPEAT, 280, 20), 230, 310);
		addNode(new SGRect(this, 30, 290, markerColor), 525, 5);
		addBounded(new SGImage(this, imagePath, SGImageMode.REPEAT, 20, 280), 530, 10);
		addNode(new SGRect(this, 30, 30, markerColor), 525, 305);
		addBounded(new SGImage(this, imagePath, SGImageMode.REPEAT, 20, 20), 530, 310);
		
		addNode(new SGRect(this, 60, 60, markerColor), 30, 140);
		addBounded(new SGImage(this, imagePath, SGImageMode.CENTER), 60, 170);
		
		addNode(new SGRect(this, 40, 40, markerColor), 150, 150);
		addBounded(new SGImage(this, imagePath, SGImageMode.CENTER_CROP, 30, 30), 170, 170);
		
		addNode(new SGRect(this, 210, 110, markerColor), 595, 5);
		addBounded(new SGImage(this, imagePath, SGImageMode.SCALE_FIT, 200, 100), 600, 10);
		
		addNode(new SGRect(this, 210, 110, markerColor), 595, 125);
		addBounded(new SGImage(this, imagePath, SGImageMode.SCALE_ALL, 200, 100), 600, 130);
		
		addNode(new SGRect(this, 210, 110, markerColor), 595, 245);
		addBounded(new SGImage(this, imagePath, SGImageMode.SCALE_CROP, 200, 100), 600, 250);
		
		addNode(new SGRect(this, 210, 110, markerColor), 815, 5);
		addBounded(new SGImage(this, imagePath, SGImageMode.CENTER_SCALE_FIT, 200, 100), 920, 60);
		
		addNode(new SGRect(this, 210, 110, markerColor), 815, 125);
		addBounded(new SGImage(this, imagePath, SGImageMode.CENTER_SCALE_ALL, 200, 100), 920, 180);
		
		addNode(new SGRect(this, 210, 110, markerColor), 815, 245);
		addBounded(new SGImage(this, imagePath, SGImageMode.CENTER_SCALE_CROP, 200, 100), 920, 300);
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
			setController(new NodeController() {
				@Override
				public void apply(SGNode node) {
					node.redraw();
				}
			});
		}
		
		/* @see be.multec.sg.nodes.SGRect#draw(processing.core.PGraphics) */
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
