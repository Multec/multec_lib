package basics;

import java.awt.Color;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import be.multec.sg.SGWindow;
import be.multec.sg.nodes.SGEllipse;
import be.multec.sg.nodes.SGNode;
import be.multec.sg.nodes.SGRect;
import be.multec.sg.nodes.controllers.NodeController;

/**
 * @author Wouter Van den Broeck
 */
public class FullscreenDemo extends SGWindow {
	
	// *********************************************************************************************
	// Attributes:
	// ---------------------------------------------------------------------------------------------
	
	public static Logger LOG = Logger.getLogger("FullscreenDemo");
	
	// *********************************************************************************************
	// main():
	// ---------------------------------------------------------------------------------------------
	
	public static void main(String[] args) {
		
		LOG.setLevel(Level.INFO);
		
		FileHandler fh = null;
		try {
			fh = new FileHandler("FullscreenDemo.log");
			LOG.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
			
			LOG.info("Launching FullscreenDemo");
		}
		catch (SecurityException e) {
			e.printStackTrace();
			return;
		}
		catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		final FullscreenDemo app = new FullscreenDemo();
		
		try {
			// open_1(app); // OK
			// open_2(app); // Not OK
			open_3(app); // OK
			// open_4(app); // Not OK, gets stuck
		}
		catch (Throwable e) {
			LOG.log(Level.SEVERE, "Uncaught exception in main thread. " + e, e);
			fh.flush();
			fh.close();
		}
		
	}
	
	private static void open_1(FullscreenDemo app) {
		app.openFullscreen(new Color(0xBE0000));
	}
	
	private static void open_2(FullscreenDemo app) {
		app.setFullscreenMode(FullscreenMode.FULLSCREEN_EXCLUSIVE);
		app.openFullscreen(new Color(0xBE0000));
	}
	
	private static void open_3(FullscreenDemo app) {
		app.setRenderer(P2D);
		app.openFullscreen(new Color(0xBE0000));
	}
	
	private static void open_4(FullscreenDemo app) {
		app.setFullscreenMode(FullscreenMode.FULLSCREEN_EXCLUSIVE);
		app.setRenderer(P2D);
		app.openFullscreen(new Color(0xBE0000));
	}
	
	// *********************************************************************************************
	// Methods:
	// ---------------------------------------------------------------------------------------------
	
	private int counter = 0;
	
	/* @see processing.core.PApplet#setup() */
	@Override
	public void setup() {
		frameRate(30);
		// noLoop();
		
		SGNode n1 = new SGNode(this);
		n1.setController(new NodeController() {
			@Override
			public void apply(SGNode node) {
				node.rotate(PI / 100);
				// if (counter++ == 30) throw new Error("TEST");
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
