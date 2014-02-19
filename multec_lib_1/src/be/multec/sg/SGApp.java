package be.multec.sg;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PMatrix;
import processing.core.PMatrix2D;
import processing.core.PMatrix3D;
import processing.core.PVector;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
import be.multec.sg.nodes.SGNode;
import be.multec.sg.nodes.SGStage;
import be.multec.sg.nodes.controllers.INodeController;

/**
 * Base class for scene-graph applets. These specialized Processing-applets provide a scene-graph
 * for managing a visual composition. The root-node of the scene-graph is referred to as the
 * <strong>stage</strong>.
 * 
 * <h2>Usage</h2>
 * 
 * To create a scene-graph applet, create a class that extends this base-class. Implement the
 * <code>setup</code>, in it initialize the scene-graph by adding one or more scene-graph-nodes to
 * the stage.
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
	// Logging
	
	private static boolean logToFile = false;
	
	/**
	 * The logger will log to file.
	 */
	public static void logToFile() {
		logToFile = true;
	}
	
	public static final Logger logger = Logger.getLogger("SGApp");
	
	/* The file-handler used when logging to file is enabled. */
	private FileHandler loggerFH = null;
	
	// ---------------------------------------------------------------------------------------------
	
	/** The name of this SGApp. Mainly for debugging purposes. */
	protected String name;
	
	// ---------------------------------------------------------------------------------------------
	
	/* The root-node of the scene-graph. */
	private SGStage stage;
	
	/* True when the update traversal is active. */
	private boolean updateActive = false;
	
	/**
	 * @return True when the update traversal is active.
	 */
	public boolean updateActive() {
		return updateActive;
	}
	
	/* True when the redraw traversal is active. */
	private boolean drawActive = false;
	
	/**
	 * @return True when the draw traversal is active.
	 */
	public boolean drawActive() {
		return drawActive;
	}
	
	/* False as long as the PApplet.draw() method was not called for the first time. */
	private boolean setupComplete = false;
	
	/* The background color. */
	private Color backgroundColor = null;
	
	// *********************************************************************************************
	// Constructors:
	// ---------------------------------------------------------------------------------------------
	
	/** Basic constructor. */
	public SGApp() {
		super();
		
		if (logToFile) {
			try {
				loggerFH = new FileHandler("SGWindow.log");
				logger.addHandler(loggerFH);
				SimpleFormatter formatter = new SimpleFormatter();
				loggerFH.setFormatter(formatter);
			}
			catch (SecurityException e) {
				System.err.println("Could not setup file handler for logger in SGWindow.");
				e.printStackTrace(System.err);
			}
			catch (IOException e) {
				System.err.println("Could not setup file handler for logger in SGWindow.");
				e.printStackTrace(System.err);
			}
		}
		
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				logger.log(Level.SEVERE,
						"Uncaught exception in thread '" + t.getName() + "'. " + e, e);
				loggerFH.flush();
			}
		});
		
		// Intialize the stage (the root of the scene-graph):
		stage = new SGStage(this);
		
		name = getClassName();
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @see processing.core.PApplet#dispose()
	 */
	@Override
	public void dispose() {
		stop(); // stop the PApplet animation loop
		
		try {
			unregisterMethod("mouseEvent", this);
			unregisterMethod("keyEvent", this);
		}
		catch (Exception e) {
			// ignore
		}
		
		if (stage != null) {
			stage.dispose(true);
			stage = null;
		}
		
		if (loggerFH != null) {
			try {
				logger.removeHandler(loggerFH);
				loggerFH.flush();
				loggerFH.close();
			}
			catch (Throwable e) {
				System.err.println("Failed to flush or close the log file." + e);
				e.printStackTrace(System.err);
			}
		}
		
		super.dispose();
	}
	
	// *********************************************************************************************
	// PApplet methods:
	// ---------------------------------------------------------------------------------------------
	
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
			registerMethod("keyEvent", this);
			setupComplete = true;
		}
		
		if (stage == null) return; // do not continue when the stage is not yet ready, or gone.
		
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
		if (stage.updatePending()) {
			stage.updateNode();
		}
		updateActive = false;
		
		// draw traversal:
		if (drawActive) throw new Error("The redraw is already active [in " + name + "].");
		drawActive = true;
		if (DEBUG_MODE) stage.checkTree();
		if (traceDrawTrav) {
		// println("+ DRAW - START TRAVERSAL for [" + name + "]");
			// stage.printTree();
		}
		if (stage.redrawPending()) {
			if (backgroundColor != null) background(backgroundColor.getRGB());
			stage.drawNode(this.g);
		}
		drawActive = false;
		if (traceDrawTrav) println("+ DRAW - END TRAVERSAL for [" + name + "]");
		applyEnqueuedRedraws();
		
		if (!stage.updatePending() && !stage.redrawPending()) noLoop();
	}
	
	// ---------------------------------------------------------------------------------------------
	// Functionality for enqueueing redraw requests that were issued while the redraw traversal is
	// active. This can happen when a redraw request was issued in draw-code (which is probably not
	// a good idea). This can also happen when a redraw request was issued by another thread.
	
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
	
	// *********************************************************************************************
	// Delayed controller application:
	// ---------------------------------------------------------------------------------------------
	
	/* Timer for scheduled controllers. */
	private Timer scheduledUpdateTimer = new Timer();
	
	/*
	 * Synchronization lock used when adding DueUpdate objects that are ready to be applied in the
	 * dueUpdates collection, and removing them when they are applied.
	 */
	private Object dueUpdatesLock = new Object();
	
	/* Collection of controllers scheduled for delayed application, wrapped in a DueUpdate objects. */
	private DueUpdate[] dueUpdates;
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * Utility method for registering a node controller that needs to be applied after a certain
	 * delay.
	 * 
	 * TODO: rename to delayController
	 * 
	 * @param delay The delay in milliseconds.
	 * @param target The node on which the controller needs to be applied.
	 * @param controller The controller that needs to be applied on the given node after the given
	 *            delay.
	 */
	public void scheduleUpdate(int delay, final SGNode target, final INodeController controller) {
		// println(">> SGApp.scheduleUpdate() - target: " + target.name);
		final DueUpdate dueUpdate = new DueUpdate(target, controller);
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				// println(">> SGApp.scheduleUpdate >> task.run() " + target.name);
				synchronized (dueUpdatesLock) {
					if (dueUpdates == null) {
						dueUpdates = new DueUpdate[] { dueUpdate };
						stage.invalidateNode();
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
	
	/**
	 * A system class that wraps a controller scheduled for delayed application and the node on
	 * which it needs to be applied.
	 */
	private class DueUpdate {
		private SGNode target;
		private INodeController controller;
		
		public DueUpdate(SGNode target, INodeController controller) {
			this.target = target;
			this.controller = controller;
		}
		
		public void apply() {
			controller.apply(target);
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
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/**
	 * Handler of Processing mouse events.
	 * 
	 * @param event
	 */
	public void mouseEvent(MouseEvent event) {
		if (!stage.wantsSysMouseEvents()) return;
		
		int x = event.getX();
		int y = event.getY();
		int action = event.getAction();
		
		if ((x != mouseVector.x) || (y != mouseVector.y)) stageMouseVectorDirty = true;
		
		switch (action) {
			case MouseEvent.ENTER:
				// TODO: dispatch enter event
				break;
			case MouseEvent.EXIT:
				if (SGNode.getCurrentOverNode() != null
						&& SGNode.getCurrentOverNode().wantsSysMouseEvents())
					SGNode.getCurrentOverNode().dispatchMouseOut();
				// TODO: dispatch exit event
				break;
			case MouseEvent.PRESS:
				// Manually set the mouseX & mouseY because this is not yet done when the mouse
				// touches the window while it was out of system focus (on Mac OS X at least).
				mouseX = x;
				mouseY = y;
				stage.processMousePressed(event);
				break;
			case MouseEvent.RELEASE:
				// Manually set the mouseX & mouseY because this is not yet done when the mouse
				// touches the window while it was out of system focus (on Mac OS X at least).
				mouseX = x;
				mouseY = y;
				stage.processMouseReleased(event);
				break;
			case MouseEvent.CLICK:
				// Manually set the mouseX & mouseY because this is not yet done when the mouse
				// touches the window while it was out of system focus (on Mac OS X at least).
				mouseX = x;
				mouseY = y;
				stage.processMouseClicked(event);
				break;
			case MouseEvent.MOVE:
				// println(">> SGApp > MOVE - x, y: (" + x + ", " + y + "),  mouseX/Y: (" + mouseX
				// + ", " + mouseY + ")");
				stage.processMouseMoved(event, false);
				break;
			case MouseEvent.DRAG:
				// println(">> SGApp > DRAG - x, y: (" + x + ", " + y + "),  mouseX/Y: (" + mouseX
				// + ", " + mouseY + ")");
				stage.processMouseMoved(event, true);
				break;
		}
	}
	
	// *********************************************************************************************
	// Keyboard functionality:
	// ---------------------------------------------------------------------------------------------
	// Key event handlers:
	
	/**
	 * Handler of Processing key events.
	 * 
	 * @param event
	 */
	public void keyEvent(KeyEvent event) {
		if (!stage.wantsSysKeyEvents()) return;
		
		switch (event.getAction()) {
			case KeyEvent.PRESS:
				stage.processKeyTyped(event);
				break;
			case KeyEvent.RELEASE:
				stage.processKeyPressed(event);
				break;
			case KeyEvent.TYPE:
				stage.processKeyReleased(event);
				break;
		}
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
		stage.redraw("SGApp.setBackground()");
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
	
	// *********************************************************************************************
	// Debug utilities:
	// ---------------------------------------------------------------------------------------------
	
	protected void printMatrix(PMatrix matrix) {
		if (matrix.getClass() == PMatrix2D.class) ((PMatrix2D) matrix).print();
		else if (matrix.getClass() == PMatrix3D.class) ((PMatrix3D) matrix).print();
	}
	
	public static void printBounds(Rectangle bounds) {
		printBounds("- bounds: ", bounds);
	}
	
	public static void printBounds(String prefix, Rectangle bounds) {
		System.out.println(prefix + bounds.x + ", " + bounds.y + ", " + bounds.width + ", "
				+ bounds.height);
	}
	
	// *********************************************************************************************
	// Other methods:
	// ---------------------------------------------------------------------------------------------
	
	public String getClassName() {
		return getClass().getSimpleName() + "[SGApp]";
	}
	
	@Override
	public String toString() {
		return name;
	}
	
}
