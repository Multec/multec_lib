package basics;

import java.awt.Color;

import be.multec.sg.SGWindow;
import be.multec.sg.d2.SGLabel;

public class HelloWorld extends SGWindow {
	
	// *********************************************************************************************
	// Main method:
	// ---------------------------------------------------------------------------------------------
	
	public static void main(String[] args) {
		// Launch the HelloWorld program.
		new HelloWorld().open("Hello World", 50, 30, 300, 300, new Color(0xFFCC00));
	}
	
	// *********************************************************************************************
	// Attributes:
	// ---------------------------------------------------------------------------------------------
	
	SGLabel label;
	
	// *********************************************************************************************
	// Methods:
	// ---------------------------------------------------------------------------------------------
	
	/* @see be.multec.sg.SGApp#setupSG() */
	@Override
	public void setupSG() {
		label = new SGLabel(this, "Hello world");
		addNode(label, 50, 70);
		noLoop();
	}
	
}
