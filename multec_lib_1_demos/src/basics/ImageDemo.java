package basics;

import java.awt.Color;

import processing.core.PConstants;

import be.multec.sg.SGWindow;
import be.multec.sg.nodes.SGImage;
import be.multec.sg.nodes.SGRect;
import be.multec.sg.nodes.SGImage.SGImageMode;

/**
 * @author Wouter Van den Broeck
 */
public class ImageDemo extends SGWindow {
	
	// *********************************************************************************************
	// main():
	// ---------------------------------------------------------------------------------------------
	
	public static void main(String[] args) {
		ImageDemo app = new ImageDemo();
		//app.setRenderer(PConstants.P2D); // OK
		app.open("ImageDemo", 50, 30, 1050, 408, new Color(0xffcc00));
	}
	
	// *********************************************************************************************
	// Methods:
	// ---------------------------------------------------------------------------------------------

	/* @see processing.core.PApplet#setup() */
	@Override
	public void setup() {
		noLoop();
		
		Color markerColor = new Color(0xffffff);
		String imagePath = "data/image_demo_stripes.png";
		
		addNode(new SGRect(this, 60, 60, markerColor), 30, 30);
		addNode(new SGImage(this, imagePath, SGImageMode.SIMPLE), 35, 35);
		
		addNode(new SGRect(this, 40, 40, markerColor), 150, 40);
		addNode(new SGImage(this, imagePath, SGImageMode.CROP, 30, 30), 155, 45);
		
		// different repeat cases:
		addNode(new SGRect(this, 290, 290, markerColor), 225, 5);
		addNode(new SGImage(this, imagePath, SGImageMode.REPEAT, 280, 280), 230, 10);
		addNode(new SGRect(this, 290, 30, markerColor), 225, 305);
		addNode(new SGImage(this, imagePath, SGImageMode.REPEAT, 280, 20), 230, 310);
		addNode(new SGRect(this, 30, 290, markerColor), 525, 5);
		addNode(new SGImage(this, imagePath, SGImageMode.REPEAT, 20, 280), 530, 10);
		addNode(new SGRect(this, 30, 30, markerColor), 525, 305);
		addNode(new SGImage(this, imagePath, SGImageMode.REPEAT, 20, 20), 530, 310);
		
		addNode(new SGRect(this, 60, 60, markerColor), 30, 140);
		addNode(new SGImage(this, imagePath, SGImageMode.CENTER), 60, 170);
		
		addNode(new SGRect(this, 40, 40, markerColor), 150, 150);
		addNode(new SGImage(this, imagePath, SGImageMode.CENTER_CROP, 30, 30), 170, 170);
		
		addNode(new SGRect(this, 210, 110, markerColor), 595, 5);
		addNode(new SGImage(this, imagePath, SGImageMode.SCALE_FIT, 200, 100), 600, 10);
		
		addNode(new SGRect(this, 210, 110, markerColor), 595, 125);
		addNode(new SGImage(this, imagePath, SGImageMode.SCALE_ALL, 200, 100), 600, 130);
		
		addNode(new SGRect(this, 210, 110, markerColor), 595, 245);
		addNode(new SGImage(this, imagePath, SGImageMode.SCALE_CROP, 200, 100), 600, 250);
		
		addNode(new SGRect(this, 210, 110, markerColor), 815, 5);
		addNode(new SGImage(this, imagePath, SGImageMode.CENTER_SCALE_FIT, 200, 100), 920, 60);
		
		addNode(new SGRect(this, 210, 110, markerColor), 815, 125);
		addNode(new SGImage(this, imagePath, SGImageMode.CENTER_SCALE_ALL, 200, 100), 920, 180);
		
		addNode(new SGRect(this, 210, 110, markerColor), 815, 245);
		addNode(new SGImage(this, imagePath, SGImageMode.CENTER_SCALE_CROP, 200, 100), 920, 300);
	}
	
}
