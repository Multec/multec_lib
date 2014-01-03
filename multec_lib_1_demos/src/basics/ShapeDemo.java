package basics;

import java.awt.Color;

import processing.core.PConstants;
import processing.core.PShape;
import processing.core.PVector;
import be.multec.sg.SGApp;
import be.multec.sg.SGMouseEventHandler;
import be.multec.sg.SGNode;
import be.multec.sg.SGWindow;
import be.multec.sg.d2.SGRect;
import be.multec.sg.d2.SGShape;
import be.multec.sg.d2.SGShape.Position;

/**
 * @author Wouter Van den Broeck
 */
public class ShapeDemo extends SGWindow {
	
	// *********************************************************************************************
	// main():
	// ---------------------------------------------------------------------------------------------
	
	public static void main(String[] args) {
		ShapeDemo app = new ShapeDemo();
		//app.setRenderer(PConstants.P2D); // Not OK
		app.open("Shape Demo", 50, 30, 900, 500, new Color(0xFFFFFF));
	}
	
	// *********************************************************************************************
	// Methods:
	// ---------------------------------------------------------------------------------------------

	/* @see processing.core.PApplet#setup() */
	@Override
	public void setup() {
		noLoop();
		// smooth();
		
		PShape svg = loadShape("Multec_logo_RGB.svg");
		// svg.noStroke();
		SGShape node;
		
		// SVG: basic use case
		node = new LogoShape(this, svg);
		addNode(node, 20, 20);
		
		node = new LogoShape(this, svg);
		node.scale(2);
		node.rotate(-HALF_PI);
		addNode(node, 20, 450);
		
		addNode(new SGRect(this, 90, 90, new Color(0xFFCC00)), 260, 20);
		node = new MouseShape1(this, "test_1_blue.svg");
		addNode(node, 260, 20);
		
		addNode(new SGRect(this, 90, 90, new Color(0xFFCC00)), 370, 20);
		node = new MouseShape1(this, "test_1_blue.svg", SGShape.Position.CENTER);
		addNode(node, 415, 65);
		
		addNode(new SGRect(this, 90, 90, new Color(0xFFCC00)), 480, 20);
		node = new MouseShape1(this, "test_1_blue.svg", SGShape.Position.SOURCE);
		addNode(node, 480, 20);
		
		addNode(new SGRect(this, 100, 100, new Color(0xFFCC00)), 250, 130);
		node = new MouseShape1(this, "test_1_green.svg");
		addNode(node, 250, 130);
		
		addNode(new SGRect(this, 100, 100, new Color(0xFFCC00)), 360, 130);
		node = new MouseShape1(this, "test_1_green.svg", Position.CENTER);
		addNode(node, 410, 180);
		
		addNode(new SGRect(this, 100, 100, new Color(0xFFCC00)), 470, 130);
		node = new MouseShape1(this, "test_1_green.svg", Position.SOURCE);
		addNode(node, 520, 180);
		
		addNode(new SGRect(this, 200, 200, new Color(0xFFCC00)), 590, 20);
		node = new MouseShape1(this, "test_1_blue.svg", SGShape.Position.CENTER);
		node.scale(2);
		addNode(node, 690, 120);
		
		addCreatedShapes();
	}
	
	// *********************************************************************************************
	// addCreatedShapes:
	// ---------------------------------------------------------------------------------------------
	
	private void addCreatedShapes() {
		BtnShape btn;
		PShape pShape;
		
		addNode(new SGRect(this, 200, 200, new Color(0xFFCC00)), 250, 250);
		
		pShape = createShape(TRIANGLE, 100, 0, 100, 100, 0, 100);
		btn = new BtnShape(this, pShape);
		addNode(btn, 250, 250);
		
		pShape = createShape(TRIANGLE, 0, 0, 100, 100, 0, 100);
		btn = new BtnShape(this, pShape);
		addNode(btn, 350, 250);
		
		pShape = createShape(TRIANGLE, 0, 0, 100, 0, 100, 100);
		btn = new BtnShape(this, pShape);
		addNode(btn, 250, 350);
		
		pShape = createShape(TRIANGLE, 0, 0, 100, 0, 0, 100);
		btn = new BtnShape(this, pShape);
		addNode(btn, 350, 350);
		
		pShape = createShape(QUAD, 50, 0, 100, 50, 50, 100, 0, 50);
		btn = new BtnShape(this, pShape);
		btn.outColor = new Color(0);
		btn.fill(btn.outColor);
		addNode(btn, 300, 300);
	}
	
	// *********************************************************************************************
	// MouseShape:
	// ---------------------------------------------------------------------------------------------
	
	private class MouseShape1 extends SGShape {
		
		public MouseShape1(SGApp app, PShape shape) {
			super(app, shape);
			init();
		}
		
		public MouseShape1(SGApp app, PShape shape, Position position) {
			super(app, shape, position);
			init();
		}
		
		public MouseShape1(SGApp app, String path, Position position) {
			super(app, path, position);
			init();
		}
		
		public MouseShape1(SGApp app, String path) {
			super(app, path);
			init();
		}
		
		private void init() {
			addMouseEventHandler(new SGMouseEventHandler() {
				
				/* @see be.multec.sg.SGMouseEventHandler#mouseOver(be.multec.sg.SGNode) */
				@Override
				protected void mouseOver(SGNode node, PVector mousePosition, boolean dragged) {
					// System.out.println("OVER");
					SGShape shape = (SGShape) node;
					shape.fill(new Color(0xBE0000));
					shape.stroke(new Color(0x9B0000));
					shape.strokeWeight(20);
				}
				
				/* @see be.multec.sg.SGMouseEventHandler#mouseOut(be.multec.sg.SGNode) */
				@Override
				protected void mouseOut(SGNode node, PVector mousePosition, boolean dragged) {
					// System.out.println("OUT");
					// ((SGFigure) node).fill(new Color(0));
					((SGShape) node).useShapeStyles();
				}
				
			});
		}
		
	}
	
	// ---------------------------------------------------------------------------------------------
	
	private class LogoShape extends SGShape {
		
		public LogoShape(SGApp app, PShape shape) {
			super(app, shape);
			init();
		}
		
		private void init() {
			addMouseEventHandler(new SGMouseEventHandler() {
				
				/* @see be.multec.sg.SGMouseEventHandler#mouseOver(be.multec.sg.SGNode) */
				@Override
				protected void mouseOver(SGNode node, PVector mousePosition, boolean dragged) {
					// System.out.println("OVER");
					((SGShape) node).fill(new Color(0));
				}
				
				/* @see be.multec.sg.SGMouseEventHandler#mouseOut(be.multec.sg.SGNode) */
				@Override
				protected void mouseOut(SGNode node, PVector mousePosition, boolean dragged) {
					// System.out.println("OUT");
					((SGShape) node).useShapeStyles();
				}
				
			});
		}
		
	}
	
	// ---------------------------------------------------------------------------------------------
	
	private class BtnShape extends SGShape {
		
		public Color overColor = new Color(0xBE0000);
		public Color outColor = new Color(0x554400);
		
		public BtnShape(SGApp app, PShape shape) {
			super(app, shape);
			useNodeStyles();
			fill(outColor);
			addMouseEventHandler(new SGMouseEventHandler() {
				
				/* @see be.multec.sg.SGMouseEventHandler#mouseOver(be.multec.sg.SGNode) */
				@Override
				protected void mouseOver(SGNode node, PVector mousePosition, boolean dragged) {
					// System.out.println("OVER");
					((SGShape) node).fill(overColor);
				}
				
				/* @see be.multec.sg.SGMouseEventHandler#mouseOut(be.multec.sg.SGNode) */
				@Override
				protected void mouseOut(SGNode node, PVector mousePosition, boolean dragged) {
					// System.out.println("OUT");
					((SGShape) node).fill(outColor);
				}
				
			});
		}
		
	}
	
}
