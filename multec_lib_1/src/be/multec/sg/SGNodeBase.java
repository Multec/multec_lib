package be.multec.sg;

import java.io.File;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * The base-class for SGNode. This class provides a number of properties and methods that are
 * useful.
 * 
 * @author Wouter Van den Broeck
 */
public class SGNodeBase {
	
	// *********************************************************************************************
	// Initialization:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @param app The PApplet instance this object delegates to.
	 */
	public SGNodeBase(PApplet app) {
		if (app == null) {
			System.err.println("The PApplet arg is null in the" + " SGNodeBase constructor call.");
			throw new Error("The PApplet arg is null in the" + " SGNodeBase constructor call.");
		}
		this.pa = app;
	}
	
	/**
	 * To be called when this objects will be no longer used.
	 */
	public void dispose() {
		pa = null;
	}
	
	// *********************************************************************************************
	// Attributes:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * The number Pi.
	 */
	static final float PI = (float) Math.PI;
	
	/**
	 * Multiply an angle expressed in degrees with this constant in order to obtain that angle
	 * expressed in radians.
	 */
	static final float DEG_TO_RAD = PI / 180.0f;
	
	/**
	 * Multiply an angle expressed in radians with this constant in order to obtain that angle
	 * expressed in degrees.
	 */
	static final float RAD_TO_DEG = 180.0f / PI;
	
	// ---------------------------------------------------------------------------------------------
	
	/** The PApplet instance this object delegates to. */
	protected PApplet pa;
	
	// *********************************************************************************************
	// Methods:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @param path A path to a file.
	 * 
	 * @return True when a file exists on the given path.
	 */
	protected boolean fileExists(String path) {
		return new File(path).exists();
	}
	
	// *********************************************************************************************
	// Delegate methods:
	// ---------------------------------------------------------------------------------------------
	
	/** @see PApplet#println() */
	protected void println() {
		PApplet.println();
	}
	
	/** @see PApplet#println(String) */
	protected void println(String v) {
		PApplet.println(v);
	}
	
	/** @see PApplet#println(Boolean) */
	protected void println(Boolean v) {
		PApplet.println(v);
	}
	
	// ---------------------------------------------------------------------------------------------
	
	protected int round(float v) {
		return (int) Math.round(v);
	}
	
	protected int round(double v) {
		return (int) Math.round(v);
	}
	
	protected int ceil(float v) {
		return (int) Math.ceil(v);
	}
	
	protected int ceil(double v) {
		return (int) Math.ceil(v);
	}
	
	protected int floor(float v) {
		return (int) Math.floor(v);
	}
	
	protected int floor(double v) {
		return (int) Math.floor(v);
	}
	
	// *********************************************************************************************
	// Color methods - Based on PApplet#color():
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * Creates colors for storing in variables of the <b>color</b> datatype. The parameters are
	 * interpreted as RGB or HSB values depending on the current <b>colorMode()</b>. The default
	 * mode is RGB values from 0 to 255 and therefore, the function call <b>color(255, 204, 0)</b>
	 * will return a bright yellow color. More about how colors are stored can be found in the
	 * reference for the <a href="color_datatype.html">color</a> datatype.
	 * 
	 * (Based on PApplet#color())
	 * 
	 * @param gray A number specifying a value between white (255) and black (0).
	 * @see PApplet#colorMode(int)
	 * 
	 * @return A 0xAARRGGBB color.
	 */
	public final int color(int gray) {
		return pa.color(gray);
	}
	
	/**
	 * @param fgray number specifying value between white and black
	 * 
	 * @return A 0xAARRGGBB color.
	 */
	public final int color(float fgray) {
		return pa.color(fgray);
	}
	
	/**
	 * As of 0116 this also takes color(#FF8800, alpha)
	 * 
	 * @param alpha relative to current color range
	 * 
	 * @return A 0xAARRGGBB color.
	 */
	public final int color(int gray, int alpha) {
		return pa.color(gray, alpha);
	}
	
	/**
	 * @param fgray
	 * @param falpha
	 * 
	 * @return A 0xAARRGGBB color.
	 */
	public final int color(float fgray, float falpha) {
		return pa.color(fgray, falpha);
	}
	
	/**
	 * @param v1 red or hue values relative to the current color range
	 * @param v2 green or saturation values relative to the current color range
	 * @param v3 blue or brightness values relative to the current color range
	 * 
	 * @return A 0xAARRGGBB color.
	 */
	public final int color(int v1, int v2, int v3) {
		return pa.color(v1, v2, v3);
	}
	
	/**
	 * @param v1 red or hue values relative to the current color range
	 * @param v2 green or saturation values relative to the current color range
	 * @param v3 blue or brightness values relative to the current color range
	 * @param alpha
	 * 
	 * @return A 0xAARRGGBB color.
	 */
	public final int color(int v1, int v2, int v3, int alpha) {
		return pa.color(v1, v2, v3, alpha);
	}
	
	/**
	 * @param v1 red or hue values relative to the current color range
	 * @param v2 green or saturation values relative to the current color range
	 * @param v3 blue or brightness values relative to the current color range
	 * 
	 * @return A 0xAARRGGBB color.
	 */
	public final int color(float v1, float v2, float v3) {
		return pa.color(v1, v2, v3);
	}
	
	/**
	 * @param v1 red or hue values relative to the current color range
	 * @param v2 green or saturation values relative to the current color range
	 * @param v3 blue or brightness values relative to the current color range
	 * @param alpha
	 * 
	 * @return A 0xAARRGGBB color.
	 */
	public final int color(float v1, float v2, float v3, float alpha) {
		return pa.color(v1, v2, v3, alpha);
	}
	
	// *********************************************************************************************
	// Other convenience methods copied from PApplet:
	// ---------------------------------------------------------------------------------------------
	
	/** Maps the given angle in radians to the corresponding angle in degrees. */
	public final float degrees(float radians) {
		return radians * RAD_TO_DEG;
	}
	
	/** Maps the given angle in degrees to the corresponding angle in radians. */
	public final float radians(float degrees) {
		return degrees * DEG_TO_RAD;
	}
	
	/** @see PApplet#map(float, float, float, float, float) */
	public final float map(float value, float start1, float stop1, float start2, float stop2) {
		return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
	}
	
	// *********************************************************************************************
	// Delegates to methods in PApplet:
	// ---------------------------------------------------------------------------------------------
	
	/** @see PApplet#loadImage(String) */
	public PImage loadImage(String filename) {
		return pa.loadImage(filename, null);
	}
	
	/** @see PApplet#loadImage(String, String) */
	public PImage loadImage(String filename, String extension) {
		return pa.loadImage(filename, extension);
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/** @see PApplet#noise(float) */
	public float noise(float x) {
		return pa.noise(x, 0f, 0f);
	}
	
	/** @see PApplet#noise(float, float) */
	public float noise(float x, float y) {
		return pa.noise(x, y, 0f);
	}
	
	/** @see PApplet#noise(float, float, float) */
	public float noise(float x, float y, float z) {
		return pa.noise(x, y, z);
	}
	
}
