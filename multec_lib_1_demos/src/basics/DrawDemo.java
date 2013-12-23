package basics;

import java.awt.Color;

import processing.core.PConstants;
import processing.core.PGraphics;
import be.multec.sg.SGApp;
import be.multec.sg.SGNode;
import be.multec.sg.SGWindow;

/**
 * @author Wouter Van den Broeck
 */
public class DrawDemo extends SGWindow {
	
	// *********************************************************************************************
	// main():
	// ---------------------------------------------------------------------------------------------
	
	public static void main(String[] args) {
		DrawDemo app = new DrawDemo();
		app.setRenderer(PConstants.P2D);
		app.open("Draw Demo", 50, 30, 1024, 960, new Color(0xFFFFFF));
	}
	
	// *********************************************************************************************
	// Methods:
	// ---------------------------------------------------------------------------------------------
	
	/* @see be.multec.sg.SGApp#setupSG() */
	@Override
	public void setupSG() {
		noLoop();
		// smooth();
		
		addNode(new DrawNode_1(this));
	}
	
	class DrawNode_1 extends SGNode {
		
		public DrawNode_1(SGApp app) {
			super(app);
		}
		
		/* @see be.multec.sg.SGNode#draw(processing.core.PGraphics) */
		@Override
		protected void draw(PGraphics g) {
			g.smooth();
			g.quality = 4;
			g.fill(0xFFFFCC00);
			g.rect(0, 0, 200, 200);
			g.fill(0);
			g.beginShape();
			g.vertex(100, 100);
			g.bezierVertex(0, 0, 200, 0, 100, 100);
			g.bezierVertex(200, 0, 200, 200, 100, 100);
			g.bezierVertex(200, 200, 0, 200, 100, 100);
			g.bezierVertex(0, 200, 0, 0, 100, 100);
			g.endShape();
		}
		
	}
	
}
