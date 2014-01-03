package tests;

import java.awt.Color;
import java.awt.Rectangle;

import be.multec.sg.SGApp;
import be.multec.sg.SGNode;
import be.multec.sg.SGWindow;
import be.multec.sg.d2.SGRect;

/**
 * A demo of the caching functionality.
 * 
 * TODO: This functionality is currently not fully operational.
 * 
 * @author Wouter Van den Broeck
 */
public class CacheDemo extends SGWindow {
	
	// *********************************************************************************************
	// Main method:
	// ---------------------------------------------------------------------------------------------
	
	public static void main(String[] args) {
		new CacheDemo().open("Cache Demo", 50, 30, 800, 600, new Color(0xFFFFFF));
	}
	
	// *********************************************************************************************
	// Attributes:
	// ---------------------------------------------------------------------------------------------
	
	// *********************************************************************************************
	// Methods:
	// ---------------------------------------------------------------------------------------------

	/* @see processing.core.PApplet#setup() */
	@Override
	public void setup() {
		// noLoop();
		// addChild(new SGLine(this, 0, height / 2, width, height / 2, 0xDDDDDD, 1));
		
		SGNode node;
		
		node = new Node_A(this, "normal");
		node.cache();
		addNode(node, 100, 150);
	}
	
	// *********************************************************************************************
	// Classes:
	// ---------------------------------------------------------------------------------------------
	
	class Node_A extends SGNode {
		
		public Node_A(SGApp app, String ext) {
			super(app);
			
			SGNode node = new SGRect(app, 80, 100, new Color(0xFFCC00), Color.BLACK, 10);
			node.name = "N1_rect_" + ext;
			node.move(-40, -50);
			addNode(node);
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
