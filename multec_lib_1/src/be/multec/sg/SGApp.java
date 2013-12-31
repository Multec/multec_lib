package be.multec.sg;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PMatrix;
import processing.core.PMatrix2D;
import processing.core.PMatrix3D;
import processing.core.PVector;
import processing.event.MouseEvent;
import be.multec.sg.modifiers.IModifier;

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
	
	/** Enables the debug mode. Additional checks are performed when this mode is enabled. */
	public static boolean DEBUG_MODE = false;
	
	// ---------------------------------------------------------------------------------------------
	
	/** The name of this SGApp. Mainly for debugging purposes. */
	protected String name;
	
	// ---------------------------------------------------------------------------------------------
	
	/* The root-node of the scene-graph. */
	private SGStage stage;
	
	/* True when the update traversal is active. */
	boolean updateActive = false;
	
	/* True when the redraw traversal is active. */
	boolean drawActive = false;
	
	/* False as long as the PApplet.draw() method was not called for the first time. */
	private boolean setupComplete = false;
	
	/*
	 * The background color.
	 */
	private Color backgroundColor = null;
	
	// *********************************************************************************************
	// Constructors:
	// ---------------------------------------------------------------------------------------------
	
	/** Basic constructor. */
	public SGApp() {
		super();
		
		// Intialize the stage (the root of the scene-graph):
		stage = new SGStage(this);
		// registerMethod("keyEvent", this);
		
		name = getClassName();
	}
	
	/**
	 * @see processing.core.PApplet#dispose()
	 */
	@Override
	public void dispose() {
		stop();
		noLoop();
		unregisterMethod("mouseEvent", this);
		// unregisterMethod("keyEvent", this);
		if (stage != null) {
			stage.dispose(true);
			stage = null;
		}
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
	
	/** Trace the draw traversal when true. */
	public boolean traceDrawTrav = false;
	
	/**
	 * This method should be called by overriding methods.
	 * 
	 * @see processing.core.PApplet#draw()
	 */
	@Override
	public void draw() {
		// if (stage.updatePending || stage.redrawPending)
		// println("\n>> SGApp[" + name + "].draw() - " + stage.updatePending + ", "
		// + stage.redrawPending);
		
		// Complete setup when draw() is called for the first time:
		if (!setupComplete) {
			stageMouseMatrix = g.getMatrix();
			registerMethod("mouseEvent", this);
			setupComplete = true;
		}
		
		// Flag the mouse vector as dirty when the mouse moved since the previous frame:
		if (mouseX != pmouseX || mouseY != pmouseY) stageMouseVectorDirty = true;
		
		// Apply scheduled updates that are due:
		if (dueUpdates != null) {
			// println("# apply scheduled updates");
			DueUpdate[] updates;
			synchronized (dueUpdatesLock) {
				updates = dueUpdates;
				dueUpdates = null;
			}
			for (DueUpdate update : updates)
				update.apply();
		}
		
		// Trigger update traversal when needed:
		updateActive = true;
		if (stage.updatePending) {
			stage.update_sys();
		}
		updateActive = false;
		
		// draw traversal:
		if (drawActive) throw new Error("The redraw is already active [in " + name + "].");
		drawActive = true;
		if (DEBUG_MODE) stage.checkTree();
		if (traceDrawTrav) {
			println("+ DRAW - START TRAVERSAL for [" + name + "]");
			// stage.printTree();
		}
		if (stage.redrawPending) {
			if (backgroundColor != null) background(backgroundColor.getRGB());
			stage.draw_sys(this.g);
		}
		drawActive = false;
		if (traceDrawTrav) println("+ DRAW - END TRAVERSAL for [" + name + "]");
		applyEnqueuedRedraws();
		
		if (!stage.updatePending && !stage.redrawPending) noLoop();
	}
	
	// ---------------------------------------------------------------------------------------------
	// Functionality for enqueueing asynchronous redraw requests while the redraw traversal is
	// active.
	
	private Object redrawQueueLock = new Object();
	private ArrayList<SGNode> redrawQueue = new ArrayList<SGNode>();
	private ArrayList<SGNode> redrawQueueAlt = new ArrayList<SGNode>();
	
	public void enqueueRedraw(SGNode node) {
		synchronized (redrawQueueLock) {
			redrawQueue.add(node);
		}
	}
	
	public void applyEnqueuedRedraws() {
		synchronized (redrawQueueLock) {
			if (redrawQueue.size() == 0) return;
			if (SGNode.traceRedraw)
				println("* REDRAW - applying enqueued redraws for [" + name + "]");
			ArrayList<SGNode> queue = redrawQueue;
			redrawQueue = redrawQueueAlt;
			for (SGNode node : queue)
				node.redraw();
			queue.clear();
			redrawQueueAlt = queue;
		}
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	// Redraw scheduling:
	
	private Timer scheduledUpdateTimer = new Timer();
	
	private Object dueUpdatesLock = new Object();
	
	private DueUpdate[] dueUpdates;
	
	public void scheduleUpdate(int delay, final SGNode target, final IModifier modifier) {
		// println(">> SGApp.scheduleUpdate() - target: " + target.name);
		final DueUpdate dueUpdate = new DueUpdate(target, modifier);
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				// println(">> SGApp.scheduleUpdate >> task.run() " + target.name);
				synchronized (dueUpdatesLock) {
					if (dueUpdates == null) {
						dueUpdates = new DueUpdate[] { dueUpdate };
						stage.invalidateUpdate();
						loop();
					}
					else {
						DueUpdate[] na = new DueUpdate[dueUpdates.length + 1];
						System.arraycopy(dueUpdates, 0, na, 0, dueUpdates.length);
						na[dueUpdates.length] = dueUpdate;
						dueUpdates = na;
					}
				}
			}
		};
		scheduledUpdateTimer.schedule(task, delay);
	}
	
	private class DueUpdate {
		private SGNode target;
		private IModifier modifier;
		
		public DueUpdate(SGNode target, IModifier modifier) {
			this.target = target;
			this.modifier = modifier;
		}
		
		public void apply() {
			modifier.apply(target);
		}
	}
	
	// *********************************************************************************************
	// Mouse functionality:
	// ---------------------------------------------------------------------------------------------
	
	/* True when the stageMouseVector is no longer valid. */
	private boolean stageMouseVectorDirty = true;
	
	/*
	 * The mouse position as given by the PApplet, currently relative to the top-left corner of the
	 * PApplet panel.
	 */
	private PVector mouseVector = new PVector(0, 0);
	
	/*
	 * The mouse position in the stage.
	 */
	private PVector stageMouseVector = new PVector(0, 0);
	
	/*
	 * The base transformation matrix that applies to the stage. The mouseVector should by be
	 * multiplied by this matrix to obtain the stageMouseVector.
	 */
	private PMatrix stageMouseMatrix;
	
	/**
	 * @return The mouse position in the stage.
	 */
	public PVector getMouseVector() {
		boolean trace = false;
		if (trace) {
			println(">> SGApp.getMouseVector() - stageMouseVectorDirty: " + stageMouseVectorDirty);
		}
		if (stageMouseVectorDirty) {
			if (trace) {
				println(" - stageMouseMatrix:");
				printMatrix(stageMouseMatrix);
			}
			mouseVector.set(mouseX, mouseY);
			stageMouseMatrix.mult(mouseVector, stageMouseVector);
			if (trace) {
				println(" - mouseVector: " + mouseVector.x + ", " + mouseVector.y);
				println(" - stageMouseVector: " + stageMouseVector.x + ", " + stageMouseVector.y);
			}
			stageMouseVectorDirty = false;
		}
		return stageMouseVector;
	}
	
	/**
	 * Class for internal system mouse events.
	 */
	public class MouseSystemEvent {
		
		private SGApp app;
		
		public MouseEvent processingEvent;
		
		public boolean consumed = false;
		
		public MouseSystemEvent(SGApp app) {
			this.app = app;
		}
		
		public MouseSystemEvent reset(MouseEvent processingEvent) {
			this.processingEvent = processingEvent;
			consumed = false;
			return this;
		}
	}
	
	/* The mouseSysEvent singleton. */
	private MouseSystemEvent mouseSysEvent = new MouseSystemEvent(this);
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/**
	 * Handler of Processing mouse events.
	 * 
	 * @param event
	 */
	public void mouseEvent(MouseEvent pEvent) {
		int x = pEvent.getX();
		int y = pEvent.getY();
		int action = pEvent.getAction();
		
		if (!stage.wantsSysMouseEvents) return;
		
		if ((x != mouseVector.x) || (y != mouseVector.y)) stageMouseVectorDirty = true;
		
		mouseSysEvent.reset(pEvent);
		
		switch (action) {
			case MouseEvent.ENTER:
				// TODO: dispatch enter event
				break;
			case MouseEvent.EXIT:
				if (SGNode.currentOverNode != null && SGNode.currentOverNode.wantsSysMouseEvents)
					SGNode.currentOverNode.dispatchMouseOut();
				// TODO: dispatch exit event
				break;
			case MouseEvent.PRESS:
				// Manually set the mouseX & mouseY because this is not yet done when the mouse
				// touches the window while it was out of system focus (on Mac OS X at least).
				mouseX = x;
				mouseY = y;
				stage.mousePressed_sys(mouseSysEvent);
				break;
			case MouseEvent.RELEASE:
				// Manually set the mouseX & mouseY because this is not yet done when the mouse
				// touches the window while it was out of system focus (on Mac OS X at least).
				mouseX = x;
				mouseY = y;
				stage.mouseReleased_sys(mouseSysEvent);
				break;
			case MouseEvent.CLICK:
				// Manually set the mouseX & mouseY because this is not yet done when the mouse
				// touches the window while it was out of system focus (on Mac OS X at least).
				mouseX = x;
				mouseY = y;
				stage.mouseClicked_sys(mouseSysEvent);
				break;
			case MouseEvent.MOVE:
				// println(">> SGApp > MOVE - x, y: (" + x + ", " + y + "),  mouseX/Y: (" + mouseX
				// + ", " + mouseY + ")");
				stage.mouseMoved_sys(mouseSysEvent, false);
				break;
			case MouseEvent.DRAG:
				// println(">> SGApp > DRAG - x, y: (" + x + ", " + y + "),  mouseX/Y: (" + mouseX
				// + ", " + mouseY + ")");
				stage.mouseMoved_sys(mouseSysEvent, true);
				break;
		}
	}
	
	// *********************************************************************************************
	// Keyboard functionality:
	// ---------------------------------------------------------------------------------------------
	// Key event handlers:
	
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
	// public SGNode addNode(SGNode child, float x, float y, float z) {
	// return stage.addNode(child, x, y, z);
	// }
	
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
	
	/**
	 * TODO: Integrate nicely with Component.setBackground(Color) and PApplet.setBackground(int)
	 * 
	 * @return The currently set background color. This might be -1 when it was not set.
	 */
	public Color getBackgroundColor() {
		return backgroundColor;
	}
	
	/**
	 * Sets the background color of the window.
	 * 
	 * @param color the background color to use
	 */
	/* @see java.awt.Component#setBackground(java.awt.Color) */
	public void setBackground(Color color) {
		backgroundColor = color;
		redraw();
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
	public boolean isJAVA2D(PGraphics pg) {
		return pg.getClass().getName().equals(PConstants.JAVA2D);
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @return True if the renderer of this PApplet is P2D.
	 */
	public boolean isP2D() {
		return g.getClass().getName().equals(PConstants.P2D);
	}
	
	/**
	 * @return True if the renderer of the given PGraphics object is P2D.
	 */
	public boolean isP2D(PGraphics pg) {
		return pg.getClass().getName().equals(PConstants.P2D);
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @return True if the renderer of this PApplet is P3D.
	 */
	public boolean isP3D() {
		return g.getClass().getName().equals(PConstants.P3D);
	}
	
	/**
	 * @return True if the renderer of the given PGraphics object is P3D.
	 */
	public boolean isP3D(PGraphics pg) {
		return pg.getClass().getName().equals(PConstants.P3D);
	}
	
	// ---------------------------------------------------------------------------------------------
	
	public String getClassName() {
		return getClass().getSimpleName() + "[SGApp]";
	}
	
	protected void printMatrix(PMatrix matrix) {
		if (matrix.getClass() == PMatrix2D.class) ((PMatrix2D) matrix).print();
		else if (matrix.getClass() == PMatrix3D.class) ((PMatrix3D) matrix).print();
	}
	
	// *********************************************************************************************
	// Other methods:
	// ---------------------------------------------------------------------------------------------
	
	@Override
	public String toString() {
		return name;
	}
	
}
