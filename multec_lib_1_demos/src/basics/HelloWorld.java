package basics;

import java.awt.Color;

import be.multec.sg.SGWindow;
import be.multec.sg.d2.SGLabel;

public class HelloWorld extends SGWindow {
	
	// *********************************************************************************************
	// Main method:
	// ---------------------------------------------------------------------------------------------
	
	public static void main(String[] args) {
		SGWindow.logToFile = true;
		
		// Launch the HelloWorld program.
		HelloWorld app = new HelloWorld();
		app.setRenderer(P2D);
		app.open("Hello World", 50, 30, 300, 300, new Color(0xFFCC00));
	}
	
	// *********************************************************************************************
	// Attributes:
	// ---------------------------------------------------------------------------------------------
	
	SGLabel label;
	
	// *********************************************************************************************
	// Methods:
	// ---------------------------------------------------------------------------------------------

	/* @see processing.core.PApplet#setup() */
	@Override
	public void setup() {
		label = new SGLabel(this, "Hello world");
		addNode(label, 50, 70);
	}

	/* @see be.multec.sg.SGWindow#windowClosed() */
	@Override
	protected void windowClosed() {
		System.exit(0);
	}
	
}
