package tests;

import java.awt.Color;
import java.awt.Rectangle;

import processing.core.PConstants;
import processing.core.PGraphics;
import be.multec.sg.SGApp;
import be.multec.sg.SGFigure;
import be.multec.sg.SGNode;
import be.multec.sg.SGWindow;
import be.multec.sg.d2.SGEllipse;
import be.multec.sg.d2.SGRect;
import be.multec.sg.d2.SGShape;
import be.multec.sg.d2.SGShape.Position;
import be.multec.sg.modifiers.IModifier;

/**
 * A demo of the bounds functionality.
 * 
 * @author Wouter Van den Broeck
 */
public class BoundsDemo extends SGWindow {
	
	// *********************************************************************************************
	// Main method:
	// ---------------------------------------------------------------------------------------------
	
	public static void main(String[] args) {
		BoundsDemo app = new BoundsDemo();
		//app.setRenderer(PConstants.P2D);
		app.open("BoundsDemo", 50, 30, 1024, 960, new Color(0xFFFFFF));
	}
	
	// *********************************************************************************************
	// Attributes:
	// ---------------------------------------------------------------------------------------------
	
	// *********************************************************************************************
	// Methods:
	// ---------------------------------------------------------------------------------------------
	
	/* @see be.multec.sg.SGApp#setupSG() */
	@Override
	public void setupSG() {
		// noLoop();
		
		SGFigure n1, n2, n3;
		
		// addChild(new SGLine(this, 0, height / 2, width, height / 2, 0xDDDDDD, 1));
		
		addOutlined(new SGRect(this, 60, 100, new Color(0xFFCC00)), 20, 20);
		addOutlined(new SGEllipse(this, 60, 100, new Color(0xFFCC00)), 120, 70);
		
		addOutlined(new SGShape(this, "test_1_blue.svg"), 20, 150); //
		addOutlined(new SGShape(this, "test_1_blue.svg", Position.SOURCE), 130, 150);
		addOutlined(new SGShape(this, "test_1_blue.svg", Position.CENTER), 285, 195);
		
		n1 = new SGShape(this, "test_1_blue.svg");
		n1.fill(new Color(0xFFCC00));
		n1.noStroke();
		addOutlined(n1, 350, 150);
		
		n1 = new SGShape(this, "test_1_blue.svg");
		n1.stroke(new Color(0xFFCC00));
		n1.strokeWeight(10);
		n1.noFill();
		addOutlined(n1, 440, 150);
		
		addOutlined(new SGShape(this, "Multec_logo_RGB.svg"), 20, 330);
		
		n1 = new SGRect(this, 150, 20, new Color(0xFFCC00));
		n2 = new SGShape(this, "test_1_blue.svg");
		n3 = new SGShape(this, "Multec_logo_RGB.svg");
		n1.addNode(n2, 50, -20);
		n2.addNode(n3, -80, 30);
		addOutlined(n1, 230, 350);
		
		addOutlined(new N1(this), 100, 600);
		addOutlined(new N2(this), 250, 600);
		addOutlined(new N3(this), 390, 600);
		addOutlined(new N4(this), 530, 600);
		addOutlined(new N5(this), 700, 600);
	}
	
	private void addOutlined(SGNode node, float x, float y) {
		SGNode outlined = new Outlined(this, node);
		addNode(outlined, x, y);
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
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * A node that draws a slowly rotating rectangle.
	 */
	class N1 extends SGFigure {
		
		public N1(SGApp app) {
			super(app);
			
			SGNode n = new SGRect(app, 80, 100, new Color(0xFFCC00), new Color(0), 10);
			n.name = "N1_rect";
			n.move(-40, -50);
			addNode(n);
			addModifier(new IModifier() {
				@Override
				public void apply(SGNode node) {
					node.rotate(PI / 200);
				}
			});
		}
		
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * A node that draws a tilted rectangle with changing stroke weight.
	 */
	class N2 extends SGFigure {
		
		private SGFigure rect;
		private float delta = 0.1f;
		private int dir = 1;
		
		public N2(SGApp app) {
			super(app);
			
			rect = new SGRect(app, 60, 60, new Color(0xFFCC00), Color.BLACK, 1);
			rect.name = "N2_rect";
			rect.move(-40, -50);
			addNode(rect);
			rotate(PI / 4);
			
			addModifier(new IModifier() {
				@Override
				public void apply(SGNode node) {
					rect.strokeWeight(rect.strokeWeight() + dir * delta);
					if (rect.strokeWeight() >= 20) dir = -1;
					else if (rect.strokeWeight() <= 1) dir = 1;
				}
			});
		}
		
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * A node that draws a tilted rectangle with changing stroke weight.
	 */
	class N3 extends SGFigure {
		
		private SGFigure inner;
		private float delta = 1f;
		private int dir = 1;
		
		public N3(SGApp app) {
			super(app);
			
			SGFigure outer = new SGRect(app, 100, 40, new Color(0xFFCC00));
			outer.name = "N3_outer";
			addNode(outer, -50, -20);
			
			inner = new SGRect(app, 30, 30, Color.BLACK);
			inner.name = "N3_inner";
			inner.move(-15, -15);
			outer.addNode(inner, 35, 5);
			
			addModifier(new IModifier() {
				@Override
				public void apply(SGNode node) {
					inner.move(0, dir * delta);
					if (inner.getY() >= 55) dir = -1;
					else if (inner.getY() <= -45) dir = 1;
				}
			});
		}
		
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * A node that draws a tilted rectangle with changing stroke weight.
	 */
	class N4 extends SGFigure {
		
		public N4(SGApp app) {
			super(app);
			
			addNode(new SGEllipse(app, 60, 100, new Color(0xFFCC00), Color.BLACK, 10), 0, 0);
			
			addModifier(new IModifier() {
				@Override
				public void apply(SGNode node) {
					node.rotate(PI / 300);
				}
			});
		}
		
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * A node that draws a tilted rectangle with changing stroke weight.
	 */
	class N5 extends SGFigure {
		
		public N5(SGApp app) {
			super(app);
			
			Color c1 = new Color(0xFFCC00);
			Color c2 = Color.BLACK;
			
			addNode(new SGEllipse(app, 20, 40, c1, c2, 10), -40, -30);
			addNode(new SGEllipse(app, 20, 40, c1, c2, 10), 40, 30);
			addNode(new SGEllipse(app, 40, 20, c1), 30, -40);
			addNode(new SGEllipse(app, 40, 20, c1), -30, 40);
			
			addModifier(new IModifier() {
				@Override
				public void apply(SGNode node) {
					rotate(PI / 250);
				}
			});
		}
		
	}
	
	// *********************************************************************************************
	// Support methods:
	// ---------------------------------------------------------------------------------------------
	
	/* Returns a string that represents the data from the given rectangle. */
	private String rectStr(Rectangle r) {
		return "" + r.x + ", " + r.y + ", " + r.width + ", " + r.height;
	}
	
}
