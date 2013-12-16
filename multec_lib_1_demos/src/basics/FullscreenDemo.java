package basics;

import java.awt.Color;

import be.multec.sg.SGNode;
import be.multec.sg.SGWindow;
import be.multec.sg.d2.SGEllipse;
import be.multec.sg.d2.SGRect;
import be.multec.sg.modifiers.IModifier;

/**
 * @author Wouter Van den Broeck
 */
public class FullscreenDemo extends SGWindow {
	
	// *********************************************************************************************
	// main():
	// ---------------------------------------------------------------------------------------------
	
	public static void main(String[] args) {
		FullscreenDemo app = new FullscreenDemo();
		app.openFullscreen(new Color(0xBE0000));
		// app.open("OpenFullscreen", 50, 50, 1280, 960, 0xBE0000);
	}
	
	// *********************************************************************************************
	// Methods:
	// ---------------------------------------------------------------------------------------------
	
	/* @see be.multec.sg.SGApp#setupSG() */
	@Override
	public void setupSG() {
		frameRate(30);
		// noLoop();
		
		SGNode n1 = new SGNode(this);
		n1.addModifier(new IModifier() {
			@Override
			public void apply(SGNode node) {
				node.rotate(PI / 100);
			}
		});
		n1.addNode(new SGRect(this, 600, 600, new Color(0xFFCC00)), -300, -300);
		n1.addNode(new SGEllipse(this, 300, 300, new Color(0xBE0000)));
		for (int i = 0; i < 4; i++) {
			SGNode n2 = new SGEllipse(this, 30, 30, new Color(0xBE0000));
			SGNode n3 = new SGNode(this);
			n3.addNode(n2, 250, 250);
			n3.rotateTo(i * PI / 2);
			n1.addNode(n3);
		}
		n1.move(width / 2, height / 2);
		addNode(n1);
	}
	
}
