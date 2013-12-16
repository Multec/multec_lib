package be.multec.sg;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;

/**
 * Base class for scene-graph applets. These specialized Processing-applets provide a scene-graph
 * for managing a visual composition. The root-node of the scene-graph is referred to as the
 * <strong>stage</strong>.
 * 
 * <h2>Usage</h2>
 * 
 * To create a scene-graph applet, create a class that extends this base-class. Override the
 * <code>setupSG</code> method in this new class. The implementation of this method should
 * initialize the scene-graph by adding one or more scene-graph-nodes to the stage.
 * 
 * Do not add nodes in the scene-graph in the constructor. Some node-implementations require the
 * <code>PGraphics</code> object. This object is not yet available in the constructor. Add the
 * initial nodes in the <code>setup()</code> method.
 * 
 * There is typically no need to implement the <code>draw()</code> method. If you choose to do so,
 * do not forget the call the <code>super.draw()</code> implementation.
 * 
 * @author Wouter Van den Broeck
 */
public class SGApp extends PApplet {
	
	// *********************************************************************************************
	// Attributes:
	// ---------------------------------------------------------------------------------------------
	
	/* The root-node of the scene-graph. */
	private SGStage stage;
	
	/* True when the update traversal is active. */
	private boolean updateActive = false;
	
	// *********************************************************************************************
	// Constructors:
	// ---------------------------------------------------------------------------------------------
	
	/** Basic constructor. */
	public SGApp() {
		super();
		
		// Intialize the stage (the root of the scene-graph):
		stage = new SGStage(this);
	}
	
	/**
	 * @see processing.core.PApplet#dispose()
	 */
	@Override
	public void dispose() {
		stop();
		noLoop();
		stage.dispose(true);
		stage = null;
		super.dispose();
	}
	
	// *********************************************************************************************
	// Essential methods:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * Implement this method to initialize the scene-graph.
	 */
	protected void setupSG() {}
	
	// *********************************************************************************************
	// PApplet methods:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * This method should be called by overriding methods.
	 * 
	 * @see processing.core.PApplet#setup()
	 */
	@Override
	public void setup() {
		smooth();
		setupSG();
	}
	
	/**
	 * This method should be called by overriding methods.
	 * 
	 * @see processing.core.PApplet#draw()
	 */
	@Override
	public void draw() {
		// System.out.println("\n>> SGApp.draw()");
		if (stage.updatePending()) {
			updateActive = true;
			stage.update_sys();
			updateActive = false;
		}
		if (stage.redrawPending()) {
			preDraw();
			stage.draw_sys(this.g);
		}
	}
	
	/**
	 * This method is called right before the scene-graph is redrawn. You can override it to perform
	 * specific actions.
	 */
	protected void preDraw() {}
	
	// ---------------------------------------------------------------------------------------------
	// Mouse event handlers:
	// ---------------------------------------------------------------------------------------------
	
	@Override
	public void mouseClicked(MouseEvent e) {
		super.mouseClicked(e);
		if (stage.wantsSysMouseEvents) stage.mouseClicked_sys();
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		super.mousePressed(e);
		if (stage.wantsSysMouseEvents) stage.mousePressed_sys();
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		super.mouseReleased(e);
		if (stage.wantsSysMouseEvents) stage.mouseReleased_sys();
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		super.mouseMoved(e);
		if (stage.wantsSysMouseEvents) stage.mouseMoved_sys();
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		super.mouseDragged(e);
		if (stage.wantsSysMouseEvents) stage.mouseDragged_sys();
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	// TODO
	
	// @Override
	// public void mouseEntered(MouseEvent e) {
	// super.mouseEntered(e);
	// }
	
	// @Override
	// public void mouseExited(MouseEvent e) {
	// super.mouseExited(e);
	// }
	
	// @Override
	// public void mouseWheelMoved(MouseWheelEvent e) {
	// super.mouseWheelMoved(e);
	// }
	
	@Override
	public void mouseWheel(processing.event.MouseEvent event) {
		super.mouseWheel(event);
		// if (_stage.mouseEventsEnabled) {
		// _stage.mouseWheel_sys();
		// }
	}
	
	// ---------------------------------------------------------------------------------------------
	// Key event handlers:
	// ---------------------------------------------------------------------------------------------
	
	@Override
	public void keyTyped(KeyEvent e) {
		super.keyTyped(e);
		if (stage.wantsSysKeyEvents) stage.keyTyped_sys();
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		super.keyPressed(e);
		if (stage.wantsSysKeyEvents) stage.keyPressed_sys();
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		super.keyReleased(e);
		if (stage.wantsSysKeyEvents) stage.keyReleased_sys();
	}
	
	// *********************************************************************************************
	// Stage delegate methods:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * Adds a child-node in the stage of this app.
	 * 
	 * @param child The node to add in this container.
	 * @throws RuntimeException when the given child is already in the scene-graph
	 */
	public SGNode addNode(SGNode child) {
		return stage.addNode(child);
	}
	
	/**
	 * Adds a child-node in the stage of this app.
	 * 
	 * @param child The child-node to add in this container.
	 * @param x The x-position to use for the child-node.
	 * @param y The y-position to use for the child-node.
	 * @throws RuntimeException when the given child is already in the scene-graph
	 */
	public SGNode addNode(SGNode child, float x, float y) {
		return stage.addNode(child, x, y);
	}
	
	/**
	 * Adds a child-node in the stage of this app.
	 * 
	 * @param child The child-node to add in this container.
	 * @param x The x-position to use for the child-node.
	 * @param y The y-position to use for the child-node.
	 * @param y The z-position to use for the child-node.
	 * @throws RuntimeException when the given child is already in the scene-graph
	 */
	public SGNode addNode(SGNode child, float x, float y, float z) {
		return stage.addNode(child, x, y, z);
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/** Removes the given node from the stage of this app. */
	public void removeNode(SGNode child) {
		stage.removeNode(child);
	}
	
	/** Removes the indexed node from the stage of this app. */
	public void removeNode(int index) {
		stage.removeNode(index);
	}
	
	/** Removes all child-nodes from this container. */
	public void removeAllNodes() {
		stage.removeAllNodes();
	}
	
	// *********************************************************************************************
	// Accessors:
	// ---------------------------------------------------------------------------------------------
	
	/** @return The stage, which is the root-nood of the scene-graph. */
	public SGStage getStage() {
		return stage;
	}
	
	/**
	 * @return True when the update traversal is active.
	 */
	public boolean isUpdateActive() {
		return updateActive;
	}
	
	// *********************************************************************************************
	// Other methods:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @return True if the renderer of this PApplet is JAVA2D.
	 */
	public boolean isJAVA2D() {
		return g.getClass().getName().equals(PConstants.JAVA2D);
	}
	
	/**
	 * @return True if the renderer of the given PGraphics object is JAVA2D.
	 */
	public boolean isJAVA2D(PGraphics g) {
		return g.getClass().getName().equals(PConstants.JAVA2D);
	}
	
	public String getClassName() {
		return getClass().getSimpleName() + "(SGApp)";
	}
	
}
