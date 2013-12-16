package be.multec.sg.utils;

import processing.core.PApplet;
import processing.data.XML;

/**
 * A collection of utilities.
 * 
 * @author Wouter Van den Broeck
 */
public class SGUtils {
	
	// *********************************************************************************************
	// Miscellaneous:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * Loads and returns the viewbox data from an SVG-file
	 * 
	 * @param path The path to the SVG-file.
	 * 
	 * @return
	 * 
	 * Source http://forum.processing.org/two/discussion/436/svg-pshape-wrong-position/p1
	 */
	float[] getViewBox(PApplet app, String path) {
		float[] viewBox = { 0, 0, 0, 0 };
	 
		XML xml = app.loadXML(path);
		String viewBoxStr = xml.getString("viewBox");
		//println(viewBoxStr);
		if (viewBoxStr != null) {
			//viewBox = app.float(app.split(viewBoxStr, ' ')); // TODO: fix
		}
		return viewBox;
	}
}
