package basics;

import java.awt.Color;

import be.multec.sg.SGApp;
import be.multec.sg.SGWindow;
import be.multec.sg.nodes.SGEllipse;
import be.multec.sg.nodes.SGFigure;
import be.multec.sg.nodes.SGNode;
import be.multec.sg.nodes.controllers.INodeController;
import be.multec.sg.nodes.controllers.NodeController;

public class AnimationDemo01 extends SGWindow {
	
	// *********************************************************************************************
	// Main method:
	// ---------------------------------------------------------------------------------------------
	
	public static void main(String[] args) {
		AnimationDemo01 app = new AnimationDemo01();
		app.setRenderer(P2D); // default is JAVA2D
		app.open("Animation Demo 1", 50, 30, 800, 600, new Color(0xFFCC00));
	}
	
	// *********************************************************************************************
	// Attributes:
	// ---------------------------------------------------------------------------------------------
	
	private Color fgColor = new Color(0x66BE0000, true);
	
	private float diam = 200;
	
	private float offset = 250;
	
	private float scale = .6f;
	
	private float animAngle = PI / 1000;
	
	// *********************************************************************************************
	// Methods:
	// ---------------------------------------------------------------------------------------------
	
	/* @see processing.core.PApplet#setup() */
	@Override
	public void setup() {
		// noLoop();
		
		SGNode rootAnchor = new SGNode(this);
		addNode(rootAnchor, width / 2, height / 2);
		rootAnchor.addNode(new Branch(this, 8, 3));
	}
	
	// ---------------------------------------------------------------------------------------------
	
	class Branch extends SGFigure {
		
		public Branch(SGApp app, int nBranches, int depth) {
			super(app);
			
			SGEllipse ce = new SGEllipse(app, diam, diam, fgColor);
			addNode(ce, 0, 0);
			
			ce.setController(new NodeController() {
				@Override
				public void apply(SGNode node) {
					rotate(animAngle);
				}
			});
			
			if (depth > 0) {
				int nextDepth = depth - 1;
				for (int i = 0; i < nBranches; i++) {
					float angle = i * TWO_PI / nBranches;
					Branch branch = new Branch(app, nBranches, nextDepth);
					branch.move(cos(angle) * offset, sin(angle) * offset);
					branch.rotate(angle);
					branch.scale(scale);
					ce.addNode(branch);
				}
			}
		}
		
	}
	
}
