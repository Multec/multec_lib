package basics;

import java.awt.Color;
import java.awt.Rectangle;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PShape;
import be.multec.sg.SGWindow;
import be.multec.sg.d2.SGShape;

/**
 * @author Wouter Van den Broeck
 */
public class ShapeDemo extends SGWindow {
	
	// *********************************************************************************************
	// main():
	// ---------------------------------------------------------------------------------------------
	
	public static void main(String[] args) {
		ShapeDemo app = new ShapeDemo();
		app.setRenderer(PConstants.P2D);
		app.open("Shape Demo", 50, 30, 900, 900, new Color(0xFFFFFF));
	}
	
	// *********************************************************************************************
	// Methods:
	// ---------------------------------------------------------------------------------------------
	
	/* @see be.multec.sg.SGApp#setupSG() */
	@Override
	public void setupSG() {
		noLoop();
		// smooth();
		
		PShape svg = loadShape("Multec_logo_RGB.svg");
		SGShape node;
		
		// SVG: basic use case
		node = new SGShape(this, svg, new Rectangle(-219, 445, 161, 53), new Rectangle(0, 0, 162, 54));
		addNode(node, 25, 25);
		
		// SVG: non-proportional resizing
		node = new SGShape(this, svg, new Rectangle(-219, 445, 161, 53), new Rectangle(0, 0, 100, 52));
		addNode(node, 25, 100);
		
		// SVG: proportional resizing - provide 0 for the unknown dimension
		node = new SGShape(this, svg, new Rectangle(-219, 445, 161, 53), new Rectangle(0, 0, 100, 0));
		addNode(node, 25, 175);
		
		// SVG: proportional resizing - provide 0 for the unknown dimension
		node = new SGShape(this, svg, new Rectangle(-219, 445, 161, 53), new Rectangle(0, 0, 0, 30));
		addNode(node, 25, 225);
		
		// SVG: load test SVG with content that extends out of the viewBox.
		node = new SGShape(this, "test_1_blue.svg", new Rectangle(-10, -10, 90, 90), new Rectangle(
				0, 0, 200, 200));
		addNode(node, 200, 25);
		
		// SVG: load test SVG with a viewBox centered around the origin.
		node = new SGShape(this, "test_1_green.svg", new Rectangle(-50, -50, 100, 100), new Rectangle(0,
				0, 200, 200));
		addNode(node, 200, 250);
		
		// SVG: load test SVG with a viewBox centered around the origin.
		node = new SGShape(this, "test_1_red.svg", new Rectangle(50, 0, 100, 100), new Rectangle(0,
				0, 200, 200));
		addNode(node, 200, 475);
		
		if (!node.isJAVA2D(this.g)) addCreatedShapes();
	}
	
	private void addCreatedShapes() {
		SGShape node;
		
		// Custom shape: constructing a special shape by overriding createShape
		node = new SGShape(this) {
			
			/* @see be.multec.sg.d2.SGShape#createShape() */
			@Override
			protected PShape createShape() {
				return app.createShape(TRIANGLE, 10, 10, 100, 100, 10, 50);
			}
			
		};
		addNode(node, 425, 25);
		
		// not operational in P2D
		// addNode(new SGShape(this, svg), 25, 25);
		// addNode(new SGShape(this, svg, 400, 25), 25, 125);
		// addNode(new SGShape(this, svg, 400, 0), 25, 170);
		
		// operational in P2D
		// sn = new SGShape(this, svg, 250, 0);
		// sn.disableStyle();
		// sn.fill(Color.BLACK);
		// addNode(sn, 200, 25);
		
		// not operational in JAVA2D (default renderer)
		node = new SGShape(this) {
			
			/* @see be.multec.sg.d2.SGShape#draw(processing.core.PGraphics) */
			@Override
			protected void draw(PGraphics g) {
				super.draw(g);
				
				// g.beginShape();
				// g.fill(0xBE0000);
				// g.vertex(20, 20);
				// g.vertex(80, 20);
				// g.vertex(20, 80);
				// //g.bezierVertex(80, 80, 80, 80, 20, 80);
				// g.endShape(CLOSE);
				
				// fill(new Color(0xBE0000));
				// beginShape();
				// vertex(20, 20);
				// vertex(80, 20);
				// vertex(20, 80);
				// //g.bezierVertex(80, 80, 80, 80, 20, 80);
				// endShape(CLOSE);
				
			}
			
		};
		// addNode(sn, 450, 25);
		
		// addNode(new RectShape(this), 550, 75);
		// addNode(new TShape(this), 650, 25);
	}
	
	class RectShape extends SGShape {
		
		/**
		 * @param app
		 */
		public RectShape(PApplet app) {
			super(app);
			
			// shape = app.createShape(RECT, 0, 0, 100, 100);
			// System.out.println("- shape: " + shape);
			// fill(new Color(0xBE0000));
			//
			// targetWidth = shape.getWidth();
			// targetHeight = shape.getHeight();
			
			// shape.beginShape();
			// shape.vertex(20, 20);
			// shape.vertex(80, 20);
			// shape.vertex(20, 80);
			// shape.endShape();
		}
		
		// @Override
		// protected void draw(PGraphics g) {
		// if (shape == null) {
		// System.err.println("The shape is not available in SGShape.draw.");
		// return;
		// }
		// System.out.println("- shape: " + shape);
		// System.out.println("- shape width/height: " + shape.getWidth() + " x " +
		// shape.getHeight());
		// System.out.println("- targetWidth/Height: " + targetWidth + " x " + targetHeight);
		//
		// g.shape(shape, 0, 0, targetWidth, targetHeight);
		// }
		
	}
	
	class TShape extends SGShape {
		
		/**
		 * @param app
		 */
		public TShape(PApplet app) {
			super(app);
			
			// shape = app.createShape(RECT, 0, 0, 100, 100);
			// System.out.println("- shape: " + shape);
			// fill(new Color(0xBE0000));
			//
			// targetWidth = shape.getWidth();
			// targetHeight = shape.getHeight();
			
			// shape.beginShape();
			// shape.vertex(20, 20);
			// shape.vertex(80, 20);
			// shape.vertex(20, 80);
			// shape.endShape();
		}
		
		// @Override
		// protected void draw(PGraphics g) {
		// if (shape == null) {
		// System.err.println("The shape is not available in SGShape.draw.");
		// return;
		// }
		// System.out.println("- shape: " + shape);
		// System.out.println("- shape width/height: " + shape.getWidth() + " x " +
		// shape.getHeight());
		// System.out.println("- targetWidth/Height: " + targetWidth + " x " + targetHeight);
		//
		// g.shape(shape, 0, 0, targetWidth, targetHeight);
		// }
		
	}
	
}
