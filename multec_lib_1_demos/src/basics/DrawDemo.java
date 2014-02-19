package basics;

import java.awt.Color;

import processing.core.PGraphics;
import be.multec.sg.SGApp;
import be.multec.sg.SGWindow;
import be.multec.sg.nodes.SGNode;

/**
 * @author Wouter Van den Broeck
 */
public class DrawDemo extends SGWindow {
	
	// *********************************************************************************************
	// main():
	// ---------------------------------------------------------------------------------------------
	
	public static void main(String[] args) {
		DrawDemo app = new DrawDemo();
		// app.setRenderer(PConstants.P2D);
		app.open("Draw Demo", 50, 30, 960, 960, Color.BLACK);
	}
	
	// *********************************************************************************************
	// Methods:
	// ---------------------------------------------------------------------------------------------
	
	/* @see processing.core.PApplet#setup() */
	@Override
	public void setup() {
		noLoop();
		// smooth();
		
		addNode(new SGNode(this, width, height) {
			/* @see be.multec.sg.SGNode#draw(processing.core.PGraphics) */
			@Override
			protected void draw(PGraphics g) {
				g.fill(0xFFFFCC00);
				g.noStroke();
				g.rect(20, 20, width - 40, height - 40);
			}
		});
		
		addNode(new Flower(this), width / 2, height / 2);
	}
	
	class Flower extends SGNode {
		
		private int leafCount = 16;
		
		private int leafSize = 500;
		
		public Flower(SGApp app) {
			super(app);
		}
		
		/* @see be.multec.sg.SGNode#draw(processing.core.PGraphics) */
		@Override
		protected void draw(PGraphics g) {
			float angle = TWO_PI / leafCount;
			g.fill(0, 60);
			g.noStroke();
			for (int i = 0; i < leafCount; i++) {
				g.beginShape();
				g.vertex(0, 0);
				g.bezierVertex(-leafSize, -leafSize, +leafSize, -leafSize, 0, 0);
				g.endShape();
				g.rotate(angle);
			}
		}
		
	}
	
}
