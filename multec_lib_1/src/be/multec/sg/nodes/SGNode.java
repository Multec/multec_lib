package be.multec.sg.nodes;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PMatrix;
import processing.core.PMatrix2D;
import processing.core.PMatrix3D;
import processing.core.PVector;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
import be.multec.sg.SGApp;
import be.multec.sg.eventHandlers.SGKeyEventHandler;
import be.multec.sg.eventHandlers.SGMouseEventHandler;
import be.multec.sg.nodes.controllers.INodeController;

/**
 * Base class for nodes in a scene-graph. Each node can act as a container of child-nodes.
 * 
 * TODO: Treat move/drag event separately.
 * 
 * @author Wouter Van den Broeck
 */
public class SGNode extends SGNodeBase implements PConstants {
	
	// *********************************************************************************************
	// Attributes:
	// ---------------------------------------------------------------------------------------------
	
	/** The name of this node. */
	public String name;
	
	/**
	 * True when this node is the stage, the root of the scene-graph, which is maintained in the
	 * SGApp object.
	 * 
	 * @see SGApp#getStage()
	 */
	protected boolean isStage = false;
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/**
	 * The scene-graph application this nodes is associated with, in particular the SGApp instance
	 * passed in the constructor.
	 */
	protected SGApp app;
	
	// ---------------------------------------------------------------------------------------------
	
	/* Counter used for constructing the name of a node for which no name is given. */
	private static int nodeCounter = 0;
	
	/* When false then this node is not drawn and does not respond to mouse or other events. */
	private boolean visible = true;
	
	/* The parent of this node, which might be null if this node has no parent. */
	private SGNode parent = null;
	
	/* True when this node is included in a scene-graph. */
	private boolean addedToSG = false;
	
	/* True when this node has been disposed. */
	protected boolean disposed = false;
	
	// *********************************************************************************************
	// Constructors:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * Basic constructor.
	 * 
	 * @param app The scene-graph application object.
	 */
	public SGNode(SGApp app) {
		super(app);
		nodeCounter++;
		this.app = app;
		name = makeName(nodeCounter);
	}
	
	/**
	 * Basic constructor.
	 * 
	 * @param app The scene-graph application object.
	 * @param name The name for this node.
	 */
	public SGNode(SGApp app, String name) {
		super(app);
		nodeCounter++;
		this.app = app;
		if (name == null) this.name = makeName(nodeCounter);
		else this.name = name;
	}
	
	/**
	 * Basic constructor.
	 * 
	 * @param app The scene-graph application object.
	 * @param explicitWidth The explicit width of this node.
	 * @param explicitHeight The explicit height of this node.
	 */
	public SGNode(SGApp app, float explicitWidth, float explicitHeight) {
		super(app);
		this.app = app;
		this.explicitWidth = explicitWidth;
		this.explicitHeight = explicitHeight;
		this.name = makeName(nodeCounter++);
	}
	
	/**
	 * Basic constructor.
	 * 
	 * @param app The scene-graph application object.
	 * @param explicitWidth The explicit width of this node.
	 * @param explicitHeight The explicit height of this node.
	 * @param name The name for this node.
	 */
	public SGNode(SGApp app, float explicitWidth, float explicitHeight, String name) {
		super(app);
		this.app = app;
		this.explicitWidth = explicitWidth;
		this.explicitHeight = explicitHeight;
		this.name = name;
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/** Override to give the objects a custom name. */
	protected String makeName(int nodeCounter) {
		return getClass().getSimpleName() + "_" + nodeCounter;
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * Dispose this node after which it can no longer be used in a scene-graph. This operation
	 * should properly dispose of all resources that will not reclaimed by the garbage collector.
	 * Also null all references to other objects in order to facilitate the garbage collection.
	 * 
	 * When this node is contained by another node, then it will be removed from its parent node.
	 * 
	 * @param recursive True when the child-nodes of this nodes need to be disposed recursively.
	 */
	public void dispose(boolean traverse) {
		// println(">> " + this + "(SGNode).dispose() - addedToSG:" + addedToSG);
		if (disposed) return;
		
		if (hasChildren) {
			if (traverse) {
				Iterator<SGNode> iter = children.iterator();
				removeAllNodes();
				while (iter.hasNext())
					iter.next().dispose(true);
			}
			else removeAllNodes();
		}
		
		app = null;
		parent = null;
		isStage = visible = false;
		
		applyTranslate = applyRotate = false;
		applyScale = applyTransformation = false;
		
		localBounds = null;
		localBoundsChanged = compositeBoundsChanged = false;
		
		updatePending = redrawPending = false;
		
		clearCache();
		cached = cacheContentDirty = cacheSizeDirty = false;
		
		inverseTMatrix = null;
		inverseTMatrixDirty = false;
		localTMatrix = null;
		localTMatrixDirty = false;
		
		dispatchMouseEvents = forwardSysMouseEvents = wantsSysMouseEvents = false;
		mouseHandlers.clear();
		mouseWasPressed = false;
		
		disposed = true;
		
		super.dispose();
	}
	
	@Override
	protected void finalize() throws Throwable {
		if (!disposed) dispose();
		super.finalize();
	}
	
	// *********************************************************************************************
	// Explicit width & height:
	// ---------------------------------------------------------------------------------------------
	
	protected float explicitWidth = 0;
	protected float explicitHeight = 0;
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/**
	 * @return the explicitly set width
	 */
	public float explicitWidth() {
		return explicitWidth;
	}
	
	/**
	 * @param explicitWidth the explicit width to set
	 */
	public void explicitWidth(float explicitWidth) {
		if (this.explicitWidth == explicitWidth) return;
		this.explicitWidth = explicitWidth;
		invalidateLocalBounds();
	}
	
	/**
	 * @return True if the width was explicitly set.
	 */
	public boolean explicitWidthSet() {
		return explicitWidth != 0;
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/**
	 * @return the explicitly set height
	 */
	public float explicitHeight() {
		return explicitHeight;
	}
	
	/**
	 * @param explicitHeight the explicit height to set
	 */
	public void explicitHeight(float explicitHeight) {
		if (this.explicitHeight == explicitHeight) return;
		this.explicitHeight = explicitHeight;
		invalidateLocalBounds();
	}
	
	/**
	 * @return True if the height was explicitly set.
	 */
	public boolean explicitHeightSet() {
		return explicitHeight != 0;
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/**
	 * @param explicitWidth the explicit width to set
	 * @param explicitHeight the explicit height to set
	 */
	public void explicitSize(float explicitWidth, float explicitHeight) {
		if (this.explicitWidth == explicitWidth && this.explicitHeight == explicitHeight) return;
		this.explicitWidth = explicitWidth;
		this.explicitHeight = explicitHeight;
		invalidateLocalBounds();
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	public float width() {
		if (explicitWidth > 0) return explicitWidth;
		else return getLocalBounds().width;
	}
	
	public float height() {
		if (explicitHeight > 0) return explicitHeight;
		else return getLocalBounds().height;
	}
	
	public int widthInt() {
		return ceil(width());
	}
	
	public int heightInt() {
		return ceil(height());
	}
	
	// *********************************************************************************************
	// Basic accessors:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @return The scene-graph application.
	 */
	public SGApp getApp() {
		return app;
	}
	
	/**
	 * @return the parent of this node, which might be null if this node has no parent
	 */
	public SGNode getParent() {
		return parent;
	}
	
	/**
	 * @return true when this node is the stage, the root of the scene-graph node tree.
	 */
	public boolean isStage() {
		return isStage;
	}
	
	/**
	 * @return true when this node was disposed
	 */
	public boolean isDisposed() {
		return disposed;
	}
	
	// *********************************************************************************************
	// Visibility:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @return True when this node is drawn and dispatches mouse or other events.
	 */
	public boolean visible() {
		return visible;
	}
	
	/**
	 * @param visible True when this node should be drawn, or false when this node should not be
	 *            drawn and should not dispatch mouse or other events.
	 */
	public void visible(boolean visible) {
		if (this.visible == visible) return;
		this.visible = visible;
		if (visible) redraw("SGNode.setVisible(true) [" + this + "]");
		else if (parent != null) parent.redraw("SGNode.setVisible(false) [" + this + "]");
		invalidateCompositeBounds();
	}
	
	/** Set the visibility of this node to true. */
	public void show() {
		if (!visible) visible(true);
	}
	
	/** Set the visibility of this node to false. */
	public void hide() {
		if (visible) visible(false);
	}
	
	// *********************************************************************************************
	// Styles:
	// ---------------------------------------------------------------------------------------------
	
	// /* Holds the style values for this node. */
	// protected SGStyle style;
	
	// *********************************************************************************************
	// Transformation:
	// ---------------------------------------------------------------------------------------------
	
	/* True when a translation needs to be applied before drawing this node. */
	private boolean applyTranslate = false;
	
	/* True when a rotation needs to be applied before drawing this node. */
	private boolean applyRotate = false;
	
	/* True when a scaling needs to be applied before drawing this node. */
	private boolean applyScale = false;
	
	/* True when some transformation needs to be applied before drawing this node. */
	private boolean applyTransformation = false;
	
	// ---------------------------------------------------------------------------------------------
	
	/* Helper method. */
	private void updateApplyTranslate() {
		applyTranslate = x != 0 || y != 0;
		applyTransformation = applyTranslate || applyRotate || applyScale;
	}
	
	/* Helper method. */
	private void updateApplyRotate2D() {
		applyRotate = rotation != 0;
		applyTransformation = applyTranslate || applyRotate || applyScale;
	}
	
	// *********************************************************************************************
	// Position:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * The position in the x-axis of the origin of the coordinate system of this node with respect
	 * to the coordinate system of its parent.
	 */
	private float x = 0;
	
	/**
	 * The position in the y-axis of the origin of the coordinate system of this node with respect
	 * to the coordinate system of its parent.
	 */
	private float y = 0;
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/**
	 * Get the horizontal position of the origin of this node.
	 */
	public float getX() {
		return x;
	}
	
	/**
	 * Set the horizontal position of the origin of this node.
	 */
	public void setX(float x) {
		if (this.x == x) return;
		this.x = x;
		updateApplyTranslate();
		invalidateTransformation();
		invalidateCompositeBounds();
	}
	
	/**
	 * Get the vertical position of the origin of this node.
	 */
	public float getY() {
		return y;
	}
	
	/**
	 * Set the vertical position of the origin of this node.
	 * 
	 * @param y the y to set
	 */
	public void setY(float y) {
		if (this.y == y) return;
		this.y = y;
		updateApplyTranslate();
		invalidateTransformation();
		invalidateCompositeBounds();
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/**
	 * Sets the position of this object to the given coordinate.
	 * 
	 * @param x
	 * @param y
	 * @return This object.
	 */
	public SGNode moveTo(float x, float y) {
		if (this.x == x && this.y == y) return this;
		this.x = x;
		this.y = y;
		updateApplyTranslate();
		invalidateTransformation();
		invalidateCompositeBounds();
		return this;
	}
	
	/**
	 * Sets the position of this object to the given coordinate.
	 * 
	 * @param point
	 * @return This object.
	 */
	public SGNode moveTo(Point point) {
		moveTo(point.x, point.y);
		return this;
	}
	
	/**
	 * Updates the position of this object by adding the given coordinate-vector to the current
	 * position.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return This object.
	 */
	public SGNode move(float x, float y) {
		if (x == 0 && y == 0) return this;
		this.x += x;
		this.y += y;
		updateApplyTranslate();
		invalidateTransformation();
		invalidateCompositeBounds();
		return this;
	}
	
	// *********************************************************************************************
	// Rotation in 2D-scene-graphs:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * The angle around the z-axis in radians that determines the rotation of the coordinate system
	 * of this node with respect to the coordinate system of its parent.
	 */
	private float rotation = 0;
	
	/**
	 * @return The rotation in radians (around the z-axis).
	 */
	public float getRotation() {
		return rotation;
	}
	
	/**
	 * Sets the rotation of this node to given angle.
	 * 
	 * @param rotation The rotation in radians (around the z-axis).
	 */
	public void rotateTo(float angle) {
		if (this.rotation == angle) return;
		this.rotation = angle;
		updateApplyRotate2D();
		invalidateTransformation();
		invalidateCompositeBounds();
	}
	
	/**
	 * Adds the given angle to the current rotation of this node.
	 * 
	 * @param rotation The rotation in radians to add to the current rotation (around the z-axis).
	 */
	public void rotate(float angle) {
		if (angle == 0) return;
		this.rotation += angle;
		updateApplyRotate2D();
		invalidateTransformation();
		invalidateCompositeBounds();
	}
	
	// *********************************************************************************************
	// Scale:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * The scale of the coordinate system of this node with respect to the coordinate system of its
	 * parent, expressed as a fraction.
	 */
	private float scale = 1;
	
	/**
	 * @return the scale
	 */
	public float getScale() {
		return scale;
	}
	
	/**
	 * @param scale the scale to set
	 */
	public void scale(float scale) {
		// println(">> SGNode[" + this + "].scale()");
		if (this.scale == scale) return;
		
		this.scale = scale;
		applyScale = scale != 0;
		applyTransformation = applyTranslate || applyRotate || applyScale;
		invalidateTransformation();
		invalidateCompositeBounds();
	}
	
	// *********************************************************************************************
	// Scene-graph methods:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * This system method is called when this node is added to the scene-graph directly or as a
	 * child-node of a container that was added to the scene-graph.
	 */
	protected void onAddedToSG() {
		// println(">> " + this + "(SGNode).onAddedToSG() - addedToSG:" + addedToSG);
		if (addedToSG) throw new Error("SGNode.onAddedToSG() was" + " called but the node ("
				+ this.toString() + ") is already in the scene-graph.");
		else {
			addedToSG = true;
			if (hasChildren) for (SGNode child : children)
				child.onAddedToSG();
		}
	}
	
	/**
	 * This system method is called when this node is removed to the scene-graph directly or as a
	 * child-node of a container that was added to the scene-graph.
	 */
	protected void onRemovedFromSG() {
		// println(">> " + this + "(SGNode).onRemovedFromSG() - addedToSG:" + addedToSG);
		if (addedToSG) {
			addedToSG = false;
			if (hasChildren) for (SGNode child : children)
				child.onRemovedFromSG();
		}
		else throw new Error("SGNode.onRemovedFromSG() was" + " called but the node ("
				+ this.toString() + ") is not in the scene-graph.");
	}
	
	// *********************************************************************************************
	// Container functionality:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * True when this node contains one or more child-nodes. Do not directly modify this property.
	 */
	private boolean hasChildren = false;
	
	/** The list of child nodes in this node. */
	private final CopyOnWriteArrayList<SGNode> children = new CopyOnWriteArrayList<SGNode>();
	
	/**
	 * The list of child nodes in this node to which system-mouse-events should be forwarded.
	 * 
	 * Note that forwardSysMouseEvents is true when there is one or more nodes in this collection.
	 */
	private final CopyOnWriteArrayList<SGNode> mouseChildren = new CopyOnWriteArrayList<SGNode>();
	
	/**
	 * The list of child nodes in this node to which system-key-events should be forwarded.
	 * 
	 * Note that forwardSysMouseEvents is true when there is one or more nodes in this collection.
	 */
	private final CopyOnWriteArrayList<SGNode> keyChildren = new CopyOnWriteArrayList<SGNode>();
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @return true when this node is a container that contains child-nodes
	 */
	public boolean hasChildren() {
		return hasChildren;
	}
	
	/** Get the list of child nodes in this container. */
	public CopyOnWriteArrayList<SGNode> getChildren() {
		return children;
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * Adds a child-node in this container.
	 * 
	 * @param child The node to add in this container.
	 * 
	 * @throws Error when the given child is already in the scene-graph
	 */
	public SGNode addNode(SGNode child) {
		// println(">> SGNode[" + this.name + "].addNode()");
		if (child == this) { throw new Error("Trying to add a node to itself for node: " + this); }
		if (child.addedToSG)
			throw new Error("SGNode.addNode(SGNode) was" + " called (on " + this
					+ ") with a child that" + " is already part of the scene-graph - container: .");
		
		if (children.contains(child)) { throw new Error("The child (" + child
				+ ") is already in the children list (of " + this + ") [in SGNode.addNode]"); }
		if (children.add(child)) {
			hasChildren = true;
			child.parent = this;
			if (addedToSG) child.onAddedToSG();
			if (child.wantsSysMouseEvents) forwardMouseEventsTo(child);
			if (child.wantsSysKeyEvents) forwardKeyEventTo(child);
			if (child.updatePending && !updatePending) invalidateNode();
			redraw("SGNode.addNode(SGNode) [" + this + "]"); // always request redraw
			if (!localCompositeBoundsChanged) invalidateLocalCompositeBounds();
		}
		return child;
	}
	
	/**
	 * Adds a child-node in this container.
	 * 
	 * @param child The child-node to add in this container.
	 * @param x The x-position to use for the child-node.
	 * @param y The y-position to use for the child-node.
	 * 
	 * @throws Error when the given child is already in the scene-graph
	 */
	public SGNode addNode(SGNode child, float x, float y) {
		addNode(child);
		child.moveTo(x, y);
		return child;
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/** Remove the given node from this container. */
	public void removeNode(SGNode child) {
		if (children.remove(child)) {
			if (children.size() == 0) hasChildren = false;
			if (addedToSG) child.onRemovedFromSG();
			child.parent = null;
			if (child.wantsSysMouseEvents) unforwardMouseEventsTo(child);
			if (child.wantsSysKeyEvents) unforwardKeyEventsTo(child);
			redraw("SGNode.removeNode(SGNode) [" + this + "]");
			if (!localCompositeBoundsChanged) invalidateLocalCompositeBounds();
		}
		else throw new Error("SGNode.removeChild(SGNode) was"
				+ " called with a child that is not contained by the" + " container ("
				+ this.toString() + ").");
	}
	
	/** Remove the indexed node from this container. */
	public void removeNode(int index) {
		try {
			SGNode child = children.remove(index);
			if (addedToSG) child.onRemovedFromSG();
			child.parent = null;
			if (child.wantsSysMouseEvents) unforwardMouseEventsTo(child);
			if (child.wantsSysKeyEvents) unforwardKeyEventsTo(child);
			if (children.size() == 0) hasChildren = false;
			redraw("SGNode.removeNode(int) [" + this + "]");
			if (!localCompositeBoundsChanged) invalidateLocalCompositeBounds();
		}
		catch (IndexOutOfBoundsException exc) {
			throw new Error("SGNode.removeChild(int) was"
					+ " called with an out of bounds index on the" + " container ("
					+ this.toString() + ").");
		}
	}
	
	/** Removes all child-nodes from this container. */
	public void removeAllNodes() {
		for (SGNode child : children) {
			if (addedToSG) child.onRemovedFromSG();
			child.parent = null;
			if (child.wantsSysMouseEvents) unforwardMouseEventsTo(child);
			if (child.wantsSysKeyEvents) unforwardKeyEventsTo(child);
		}
		children.clear();
		hasChildren = false;
		redraw("SGNode.removeAllNodes() [" + this + "]");
		if (!localCompositeBoundsChanged) invalidateLocalCompositeBounds();
	}
	
	// *********************************************************************************************
	// Bounds:
	// ---------------------------------------------------------------------------------------------
	
	/***
	 * The bounds serve two functions: 1) accelerating the contains-detection, and 2) the caching of
	 * the graphical content of a node.
	 * 
	 * There are three types of bounds:
	 * <dl>
	 * <dt>The local-bounds:</dt>
	 * <dd>Concerns only the visual content of this node in the local coordinate system of this
	 * node.</dd>
	 * <dt>The local-composite-bounds:</dt>
	 * <dd>Concerns the local visual content of this node and its children in the local coordinate
	 * system of this node.</dd>
	 * <dt>The composite-bounds:</dt>
	 * <dd>Concerns the visual content of this node and its children in the local coordinate system
	 * of the parent of this node.</dd>
	 * </ul>
	 * 
	 * When the local-bounds, then the local-composite-bounds might change as well. And when the
	 * local-composite-bounds changes, then the composite-bounds might change as well. However, when
	 * the transitions for a node change, then the composite-bounds change but the local bounds
	 * remain unchanged. Also, when a child-node was added or removed then the
	 * local-composite-bounds might change, but the local-bounds remain unchanged. We can thus avoid
	 * unnecessary but potentially costly updates by treating these three bounds separately.
	 * 
	 * The local-bounds are always kept up-to-date. The local-composite-bounds and composite-bounds
	 * on the other hand, are only calculated on demand, and memoized until they become invalid.
	 * 
	 * For both the acceleration of the contains-detection and the caching for a particular node, we
	 * need the local bounds of that node and the local bounds of its children.
	 */
	
	// ---------------------------------------------------------------------------------------------
	// Local bounds:
	
	/* @see SGNode#getLocalBounds() */
	private Rectangle localBounds = new Rectangle();
	
	/* True when the localBounds property is not valid and needs to be updated. */
	protected boolean localBoundsChanged = true;
	
	/**
	 * @return The bounds of the graphical content of this node (not including its children). The
	 *         transformations set for this nodes are not applied on these bounds. This value is not
	 *         valid when the localBoundsChanged property is true.
	 * 
	 * @see localBoundsChanged
	 */
	final public Rectangle getLocalBounds() {
		if (localBoundsChanged) validateLocalBounds();
		return localBounds;
	}
	
	/**
	 * Immediately validates local bounds. [Experimental]
	 */
	protected void validateLocalBounds() {
		// println(">> SGNode[" + this.name + "].validateLocalBounds()");
		updateLocalBounds(localBounds);
		localBoundsChanged = false;
	}
	
	/**
	 * Implement this method for each node that has graphical content. Set the bounds (x, y, width &
	 * height) in the given rectangle.
	 * 
	 * @param localBounds The rectangle in which to set the local bounds.
	 */
	protected void updateLocalBounds(Rectangle localBounds) {
		if (explicitWidth != 0 && explicitHeight != 0) {
			localBounds.setBounds(0, 0, ceil(explicitWidth), ceil(explicitHeight));
		}
		else if (!hasChildren)
			System.err.println("The method updateLocalBounds() is not implemented for " + this.name
					+ ".");
	}
	
	/*
	 * Sets the localBoundsDirty property to true for this node and request an update. During the
	 * update-phase of the next update-loop, the local bounds will be updated by the
	 * updateLocalBounds method, which is called from the updateBounds method.
	 */
	final public void invalidateLocalBounds() {
		if (localBoundsChanged) return;
		// println(">> SGNode[" + this.name + "].invalidateLocalBounds()");
		localBoundsChanged = true;
		if (!localCompositeBoundsChanged) invalidateLocalCompositeBounds();
		if (!updatePending) invalidateNode();
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/* True when the local composite bounds were changed. */
	private boolean localCompositeBoundsChanged = true;
	
	/* Memoized bounds. */
	private Rectangle localCompositeBounds;
	
	/* True when the memoized localCompositeBounds is invalid. */
	private boolean localCompositeBoundsDirty = true;
	
	/**
	 * Call this method when the the composite bounds of a child of this node changed.
	 */
	final public void invalidateLocalCompositeBounds() {
		if (localCompositeBoundsChanged) return;
		localCompositeBoundsChanged = true;
		localCompositeBoundsDirty = true;
		if (!compositeBoundsChanged) invalidateCompositeBounds();
		if (!updatePending) invalidateNode();
	}
	
	/**
	 * This value is not valid when the transformedBoundsDirty property is true.
	 * 
	 * @return The bounds of this node and its children. The transformations for this node are *not*
	 *         applied.
	 * 
	 * @see transformedBoundsDirty
	 */
	final public Rectangle getLocalCompositeBounds() {
		boolean trace = false;
		if (trace) println(">> SGNode[" + name + "].getLocalCompositeBounds()");
		if (localCompositeBoundsDirty) {
			if (trace) println(" - localCompositeBoundsDirty = true");
			if (localCompositeBounds == null) localCompositeBounds = new Rectangle();
			if (!visible) {
				localCompositeBounds.setBounds(0, 0, 0, 0);
				System.err.println("Avoid calling getLocalCompositeBounds() on invisible nodes. ["
						+ this + "]");
			}
			else {
				localCompositeBounds.setBounds(getLocalBounds());
				if (trace) println(" - local : " + rectStr(localCompositeBounds));
				
				for (SGNode child : children) {
					if (!child.visible) continue;
					Rectangle childBounds = child.getCompositeBounds();
					if (trace) println(" - adding: " + rectStr(childBounds));
					addBounds(localCompositeBounds, childBounds);
				}
			}
			localCompositeBoundsDirty = false;
			if (trace) println(" < result: " + rectStr(localCompositeBounds));
		}
		return localCompositeBounds;
	}
	
	/*
	 * Add the source to the target, avoiding the error that occurs
	 */
	private void addBounds(Rectangle target, Rectangle source) {
		if (target.isEmpty()) {
			target.setBounds(source);
		}
		else if (!source.isEmpty()) {
			target.add(source);
		}
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/*
	 * True when the compositeBounds property is not valid and needs to be updated. This is the case
	 * when either the bounds or the transformations for this node were modified.
	 * 
	 * @see transformedBounds
	 */
	private boolean compositeBoundsChanged = true;
	
	/**
	 * Call this method from the implementation of concrete nodes with graphical content whenever
	 * this content changed such that the bounds of that content changed as well.
	 * 
	 * Note that the composite bound is changed but the local bounds remain untouched, when the
	 * transformations set on this node are modified.
	 * 
	 * @param local Should be true when the local bounds should be invalidated too.
	 */
	protected void invalidateCompositeBounds() {
		// println(">> SGNode[" + name + "].invalidateCompositeBounds()");
		if (compositeBoundsChanged) return;
		
		compositeBoundsChanged = true;
		compositeBoundsDirty = true;
		if (parent != null && !parent.localCompositeBoundsChanged) {
			parent.invalidateLocalCompositeBounds();
		}
		
		if (!updatePending) invalidateNode();
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/* Memoized bounds. */
	private Rectangle compositeBounds;
	
	/* True when the memoized compositeBounds is invalid. */
	private boolean compositeBoundsDirty = true;
	
	/**
	 * @return The bounds of this node and its children. The transformations for this node are
	 *         applied to these bounds.
	 */
	final public Rectangle getCompositeBounds() {
		boolean trace = false;
		if (trace) println("* [" + this.name + "].getCompositeBounds()");
		if (trace) println(" - compositeBoundsDirty = " + compositeBoundsDirty);
		if (compositeBoundsDirty) {
			if (compositeBounds == null) compositeBounds = new Rectangle();
			if (!visible) {
				localCompositeBounds.setBounds(0, 0, 0, 0);
				System.err.println("Avoid calling getCompositeBounds() on invisible nodes. ["
						+ this + "]");
			}
			else {
				Rectangle b = compositeBounds;
				b.setBounds(getLocalCompositeBounds());
				if (applyRotate || applyScale) {
					if (trace) println(" - applyRotate || applyScale [" + this + "]");
					if (localTMatrixDirty) throw new Error("localTMatrix is dirty [" + this + "]");
					float x1 = localTMatrix.multX(b.x, b.y);
					float y1 = localTMatrix.multY(b.x, b.y);
					float x2 = localTMatrix.multX(b.x + b.width, b.y);
					float y2 = localTMatrix.multY(b.x + b.width, b.y);
					float x3 = localTMatrix.multX(b.x + b.width, b.y + b.height);
					float y3 = localTMatrix.multY(b.x + b.width, b.y + b.height);
					float x4 = localTMatrix.multX(b.x, b.y + b.height);
					float y4 = localTMatrix.multY(b.x, b.y + b.height);
					b.x = floor(Math.min(Math.min(x1, x2), Math.min(x3, x4)));
					b.y = floor(Math.min(Math.min(y1, y2), Math.min(y3, y4)));
					b.width = ceil(Math.max(Math.max(x1, x2), Math.max(x3, x4))) - b.x;
					b.height = ceil(Math.max(Math.max(y1, y2), Math.max(y3, y4))) - b.y;
				}
				else if (applyTranslate) {
					if (trace) println(" - applyTranslate only [" + this + "]");
					b.x = floor(b.x + x);
					b.y = floor(b.y + y);
				}
			}
			compositeBoundsDirty = false;
			if (trace) println(" < result: " + rectStr(compositeBounds));
		}
		return compositeBounds;
	}
	
	// *********************************************************************************************
	// Update functionality:
	// ---------------------------------------------------------------------------------------------
	
	/* True when this node needs to be updated. */
	private boolean updatePending = true;
	
	// ---------------------------------------------------------------------------------------------
	
	/** The list of child nodes in this node. */
	private CopyOnWriteArrayList<INodeController> modifiers;
	
	/**
	 * Add a controller for this node.
	 * 
	 * @param controller
	 */
	public void addController(INodeController controller) {
		if (modifiers == null) modifiers = new CopyOnWriteArrayList<INodeController>();
		modifiers.add(controller);
		if (modifiers.size() == 1 && !updatePending) invalidateNode();
	}
	
	/**
	 * Remove a controller from this node.
	 * 
	 * @param modifier
	 */
	public void removeController(INodeController controller) {
		modifiers.remove(controller);
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @return True when this node needs to be updated in the next update-loop.
	 */
	final public boolean updatePending() {
		return updatePending;
	}
	
	/**
	 * Call this method when this node needs to be updated.
	 */
	public final void invalidateNode() {
		// if (updatePending) return;
		updatePending = true;
		if (isStage) {
			if (app != null) app.loop();
		}
		else if (parent != null && !parent.updatePending) parent.invalidateNode();
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * A system method that calls the update() method for this node and for its child-nodes. Do not
	 * override this method in custom node-classes. Override the update() method instead.
	 * 
	 * @see SGNode#update()
	 */
	final public void updateNode() {
		boolean trace = false;
		if (disposed || !updatePending) return;
		updatePending = false;
		if (trace) {
			println(">> SGNode[" + this.name + "].update_sys()");
			println(" - localTMatrixDirty: " + localTMatrixDirty);
			println(" - localBoundsChanged: " + localBoundsChanged);
			println(" - localCompositeBoundsChanged: " + localCompositeBoundsChanged);
			println(" - compositeBoundsChanged: " + compositeBoundsChanged);
		}
		
		// apply the modifiers:
		if (modifiers != null && modifiers.size() > 0) {
			for (INodeController modifier : modifiers) {
				modifier.apply(this);
			}
			if (modifiers.size() > 0 && !updatePending) invalidateNode();
		}
		
		// update local transformation matrix:
		if (localTMatrixDirty) {
			if (trace) println(" * localTMatrixDirty! [" + this.name + "]");
			localTMatrix.reset();
			if (applyTranslate) localTMatrix.translate(x, y);
			if (applyRotate) localTMatrix.rotate(rotation);
			if (applyScale) localTMatrix.scale(scale);
			localTMatrixDirty = false;
		}
		
		// traverse the children, except for cached nodes:
		if (hasChildren) {
			for (SGNode child : children) {
				if (child.visible && child.updatePending) child.updateNode();
			}
		}
		
		// Update the local-bounds:
		if (localBoundsChanged) validateLocalBounds();
		
		if (localCompositeBoundsChanged) {
			if (trace) println(" * localCompositeBoundsChanged! [" + this.name + "]");
			localCompositeBoundsChanged = false;
			if (cached) cacheSizeDirty = true;
		}
		
		if (compositeBoundsChanged) {
			if (trace) println(" * compositeBoundsChanged! [" + this.name + "]");
			compositeBoundsChanged = false;
		}
	}
	
	// *********************************************************************************************
	// Draw functionality:
	// ---------------------------------------------------------------------------------------------
	
	/* True when a redraw was requested. */
	private boolean redrawPending = true;
	
	/**
	 * @return True when this node needs to be redrawn in the next update-loop.
	 */
	final public boolean redrawPending() {
		return redrawPending;
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * Implement this method for nodes that have graphical content. The implementation should draw
	 * the graphical content of this node on the PGraphics object that is passed as an argument.
	 * 
	 * This method is called in a traversal starting with the stage, the root of the scene-graph.
	 * This method is called after the update() method.
	 * 
	 * @param g The PGraphics canvas on which to draw.
	 * 
	 * @see SGNode#update()
	 */
	protected void draw(PGraphics g) {};
	
	// ---------------------------------------------------------------------------------------------
	
	public static boolean traceRedraw = false;
	
	/**
	 * Request a redraw of this node.
	 * 
	 * Use the invalidateContentFromUpdate method instead of this method when you want to request a
	 * redraw of this node from within the update loop.
	 * 
	 * @see invalidateContentFromUpdate
	 */
	public void redraw(String caller) {
		if (app.drawActive()) {
			if (traceRedraw) println("* REDRAW ENQUEUED for [" + name + "] from [" + caller + "]");
			app.enqueueRedraw(this);
			return;
		}
		
		if (disposed || redrawPending) {
			if (traceRedraw) println("* REDRAW IGNORED for [" + name + "] from [" + caller + "]");
			return;
		}
		
		redrawPending = true;
		if (traceRedraw) println("* REDRAW SCHEDULED for [" + name + "] from [" + caller + "]");
		
		if (!visible) return;
		if (cached) cacheContentDirty = true;
		if (parent != null) parent.redraw(caller);
		else if (isStage && app != null) {
			if (traceRedraw)
				println("* REDRAW - loop() CALLED from [" + name + "] from [" + caller + "]");
			app.loop();
		}
		if (SGApp.DEBUG_MODE) checkTree();
	}
	
	final public void redraw() {
		redraw("--");
	}
	
	// ---------------------------------------------------------------------------------------------
	
	public boolean drawBounds = false;
	
	public void drawBounds() {
		if (this.drawBounds) return;
		this.drawBounds = true;
		redraw();
	}
	
	public void drawBounds(boolean drawBounds) {
		if (this.drawBounds == drawBounds) return;
		this.drawBounds = drawBounds;
		redraw();
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * A system function that applies the transformations, calls the draw() method for this node and
	 * for its child-nodes. Do not override this method in custom node-classes. Override the draw()
	 * method instead. This method should only be called from SGApp.
	 * 
	 * @see SGNode#draw(processing.core.PGraphics)
	 */
	public void drawNode(PGraphics g) {
		boolean trace = false;
		if (app.updateActive()) throw new Error();
		if (!visible || disposed) return;
		if (trace) println(">> SGNode[" + this + "].draw_sys()");
		redrawPending = false;
		
		if (cached) {
			if (trace) println(" * cached! [" + this + "]");
			if (cachedBounds == null) cachedBounds = new Rectangle();
			if (cache == null) {
				cacheContentDirty = true;
				cachedBounds.setBounds(getLocalCompositeBounds());
				cache = app.createGraphics(cachedBounds.width, cachedBounds.height);
			}
			else if (cacheSizeDirty) {
				cacheContentDirty = true;
				cachedBounds.setBounds(getLocalCompositeBounds());
				cache = app.createGraphics(cachedBounds.width, cachedBounds.height);
				cacheSizeDirty = false;
			}
			
			if (cacheContentDirty) {
				if (trace) println(" * cacheContentDirty! [" + this + "]");
				cache.beginDraw();
				cache.resetMatrix();
				cache.translate(-cachedBounds.x, -cachedBounds.y);
				cache.clear();
				draw(cache);
				if (hasChildren) {
					for (SGNode child : children) {
						if (child.visible) child.drawNode(cache);
					}
				}
				cache.resetMatrix();
				cache.endDraw();
			}
			
			applyTransformation(g);
			g.copy(cache, 0, 0, cachedBounds.width, cachedBounds.height, cachedBounds.x,
					cachedBounds.y, cachedBounds.width, cachedBounds.height);
		}
		else {
			applyTransformation(g);
			draw(g); // call the draw() method on this node:
			
			if (drawBounds) {
				Rectangle bounds = getLocalCompositeBounds();
				g.noFill();
				g.stroke(0x99990000);
				g.strokeWeight(1);
				g.rect(bounds.x, bounds.y, bounds.width, bounds.height);
			}
			
			// forward the draw_sys() call to each child:
			if (hasChildren) {
				for (SGNode child : children) {
					if (child.visible) child.drawNode(g);
				}
			}
		}
		
		if (applyTransformation) g.popMatrix();
		inverseTMatrixDirty = false;
	}
	
	protected void applyTransformation(PGraphics g) {
		boolean trace = false;
		if (trace) {
			println(">> SGNode[" + this + "].applyTransformation()");
			println(" - matrix:");
			printMatrix(g.getMatrix());
		}
		if (applyTransformation) {
			g.pushMatrix();
			if (applyTranslate) g.translate(x, y);
			if (applyRotate) g.rotate(rotation);
			if (applyScale) g.scale(scale);
		}
		
		// update the locally stored transformation matrix:
		if (inverseTMatrixDirty) {
			if (trace) println("* transformMatrixDirty [" + this + "]");
			
			// update the inverse transformation matrix:
			inverseTMatrix = g.getMatrix();
			inverseTMatrix.invert();
			if (trace) printInverseTMatrix();
		}
	}
	
	// *********************************************************************************************
	// Transformation matrix functionality:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * The locally maintained inverse of the transformation-matrix. It is updated each time the
	 * translation, rotation of scale was modified and this node is redrawn.
	 * 
	 * This matrix is used to map coordinates -such as mouse coordinates- from the top-level
	 * application coordinate system to the the coordinate system valid inside this node.
	 * 
	 * This matrix is actually updated in the draw-traversal. The value is invalid as long as
	 * inverseTMatrixDirty is true;
	 */
	private PMatrix inverseTMatrix = new PMatrix2D();
	
	/**
	 * True when the value of inverseTMatrix is no longer valid.
	 */
	private boolean inverseTMatrixDirty = false;
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/**
	 * The transformation-matrix in which only the transformations specified on this node are
	 * represented. It is updated each time the translation, rotation of scale was modified and this
	 * node is redrawn.
	 * 
	 * This matrix is used to map the bounds of child-nodes to their parent nodes.
	 */
	private PMatrix2D localTMatrix = new PMatrix2D();
	
	/* True when the localTMatrix is not valid. */
	private boolean localTMatrixDirty = true;
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/**
	 * Call this method when some aspect of the transformation of this node has changed. It flags
	 * the locally stored inverse of the transformation matrix as dirty, so that it is updated
	 * during the next draw-traversal.
	 */
	private void invalidateTransformation() {
		inverseTMatrixDirty = true;
		localTMatrixDirty = true;
		if (hasChildren) for (SGNode child : children) {
			child.invalidateTransformation();
		}
		redraw("SGNode.invalidateTransformation() [" + this + "]");
		// Do not invalidate the composite-bounds here. Doing so would also invalidate the
		// composite-bounds in the children of this node, which is not necesssary.
	}
	
	protected void printInverseTMatrix() {
		printMatrix(inverseTMatrix);
	}
	
	protected void printMatrix(PMatrix matrix) {
		if (matrix.getClass() == PMatrix2D.class) ((PMatrix2D) matrix).print();
		else if (matrix.getClass() == PMatrix3D.class) ((PMatrix3D) matrix).print();
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @param globalX The x-component of the global coordinate that should be mapped to a local
	 *            coordinate.
	 * @param globalY The y-component of the global coordinate that should be mapped to a local
	 *            coordinate.
	 * 
	 * @return The local coordinate to which the given global coordinate maps.
	 */
	public PVector globalToLocal(float globalX, float globalY) {
		return inverseTMatrix.mult(new PVector(globalX, globalY), null);
	}
	
	/**
	 * @param globalCoord The global coordinate that should be mapped to a local coordinate.
	 * 
	 * @return The local coordinate to which the given global coordinate maps.
	 */
	public PVector globalToLocal(PVector globalCoord) {
		return inverseTMatrix.mult(globalCoord, null);
	}
	
	// *********************************************************************************************
	// Cache:
	// ---------------------------------------------------------------------------------------------
	
	/* The cache. */
	PGraphics cache;
	
	/* True when the (graphics) content of this node is cached in a bitmap image. */
	private boolean cached = false;
	
	/* True when the cache content is dirty. */
	private boolean cacheContentDirty = true;
	
	/* True when the cache size is dirty. */
	private boolean cacheSizeDirty = false;
	
	/* The cached surface bounds. */
	private Rectangle cachedBounds;
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @return True when the (graphics) content of this node is cached in a bitmap image.
	 */
	public boolean isCached() {
		return cached;
	}
	
	/**
	 * @param cached True when the (graphics) content of this node should be cached in a bitmap
	 *            image
	 * 
	 * @default false
	 */
	public void setCached(boolean cached) {
		if (this.cached == cached) return;
		this.cached = cached;
		if (cached) cacheContentDirty = true;
		else clearCache();
	}
	
	/**
	 * Convenience method that enables the cache.
	 */
	public void cache() {
		setCached(true);
	}
	
	// ---------------------------------------------------------------------------------------------
	
	private void clearCache() {
		cache = null; // There is no proper dispose() method on PImage.
		cachedBounds = null;
		cacheContentDirty = false;
		cacheSizeDirty = false;
	}
	
	// *********************************************************************************************
	// Contains functionality:
	// ---------------------------------------------------------------------------------------------
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/**
	 * This method needs to be implemented in all nodes that draw interactive content.
	 * 
	 * @return True when the given point is contained by the graphical content of this node.
	 * 
	 * @see SGNode#enableMouseEventMethods()
	 * @see SGNode#addMouseEventHandler(SGMouseEventHandler)
	 */
	protected boolean contains(float x, float y) {
		return false;
	}
	
	/**
	 * @return True when the given point is contained by the graphical content of this node.
	 * 
	 * @see SGNode#enableMouseEventMethods()
	 * @see SGNode#addMouseEventHandler(SGMouseEventHandler)
	 */
	private boolean contains_sys(float x, float y) {
		if (!visible) return false;
		Rectangle bounds = getLocalCompositeBounds();
		return (getLocalBounds().contains(x, y) && contains(x, y))
				|| getLocalCompositeBounds().contains(x, y);
	}
	
	/**
	 * @return True when the given point is contained by the graphical content of this node.
	 * 
	 * @see SGNode#enableMouseEventMethods()
	 * @see SGNode#addMouseEventHandler(SGMouseEventHandler)
	 */
	private boolean contains_sys(PVector point) {
		return contains_sys(point.x, point.y);
	}
	
	/**
	 * @return True when the mouse is contained by the graphical content of this node.
	 * 
	 * @see SGNode#enableMouseEventMethods()
	 * @see SGNode#addMouseEventHandler(SGMouseEventHandler)
	 */
	private boolean containsMouse() {
		return contains_sys(getMousePosition());
	}
	
	// *********************************************************************************************
	// Mouse functionality:
	// ---------------------------------------------------------------------------------------------
	
	/* The node that is currently under the mouse cursor. */
	private static SGNode currentOverNode;
	
	/**
	 * @return The node that is currently under the mouse cursor.
	 */
	public static SGNode getCurrentOverNode() {
		return currentOverNode;
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/*
	 * True when at least one mouse-event-handler has been registered on this node using
	 * addMouseEventHandler(). Do not modify this property.
	 */
	private boolean dispatchMouseEvents = false;
	
	/*
	 * True when the system-mouse-events should be forwarded to one or more child-node that needs to
	 * dispatch mouse-events or forward the system-mouse-events itself. Do not modify this property.
	 */
	private boolean forwardSysMouseEvents = false;
	
	/*
	 * True when this node should receive system-mouse-events. Do not modify this property.
	 */
	private boolean wantsSysMouseEvents = false;
	
	/**
	 * @return True when this node should receive mouse-events from the system to dispatch further.
	 */
	public boolean wantsSysMouseEvents() {
		return wantsSysMouseEvents;
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/* The collection of registered mouse-event-handlers. */
	private CopyOnWriteArraySet<SGMouseEventHandler> mouseHandlers = new CopyOnWriteArraySet<SGMouseEventHandler>();
	
	/* True when the mouse was pressed while the cursor was over this node. */
	private boolean mouseWasPressed = false;
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/**
	 * Add a mouse-event handler. Note that mouse-events will only be dispatched for classes that
	 * properly implement the mouseHitTest() method.
	 * 
	 * @param handler The handler to add.
	 * 
	 * @see SGNode#removeMouseEventHandler(SGMouseEventHandler)
	 * @see SGNode#mouseHitTest()
	 */
	public void addMouseEventHandler(SGMouseEventHandler handler) {
		if (mouseHandlers.add(handler) && mouseHandlers.size() == 1) {
			dispatchMouseEvents = true;
			updateMouseFlags();
		}
	}
	
	/**
	 * Remove a mouse-event handler.
	 * 
	 * @param handler The handler to remove.
	 * 
	 * @see SGNode#addMouseEventHandler(SGMouseEventHandler)
	 */
	public void removeMouseEventHandler(SGMouseEventHandler handler) {
		if (mouseHandlers.remove(handler) && mouseHandlers.size() == 0) {
			dispatchMouseEvents = false;
			updateMouseFlags();
			if (currentOverNode == this) currentOverNode = null;
			mouseWasPressed = false;
		}
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	private void updateMouseFlags() {
		if (dispatchMouseEvents || forwardSysMouseEvents) {
			if (!wantsSysMouseEvents) {
				wantsSysMouseEvents = true;
				if (parent != null) parent.forwardMouseEventsTo(this);
			}
		}
		else {
			if (wantsSysMouseEvents) {
				wantsSysMouseEvents = false;
				if (parent != null) parent.unforwardMouseEventsTo(this);
			}
		}
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	// Mouse-event dispatch helper methods.
	
	private void dispatchMouseClicked(PVector mousePosition) {
		for (SGMouseEventHandler handler : mouseHandlers)
			handler.mouseClicked(this, mousePosition);
	}
	
	private void dispatchMousePressed(PVector mousePosition) {
		for (SGMouseEventHandler handler : mouseHandlers)
			handler.mousePressed(this, mousePosition);
	}
	
	private void dispatchMouseReleased(PVector mousePosition) {
		for (SGMouseEventHandler handler : mouseHandlers)
			handler.mouseReleased(this, mousePosition);
	}
	
	private void dispatchMouseOver(PVector mousePosition, boolean dragged) {
		for (SGMouseEventHandler handler : mouseHandlers)
			handler.mouseOver(this, mousePosition, dragged);
	}
	
	private void dispatchMouseOut(PVector mousePosition, boolean dragged) {
		for (SGMouseEventHandler handler : mouseHandlers)
			handler.mouseOut(this, mousePosition, dragged);
	}
	
	/**
	 * System method that dispatches a mouse-out event to the handlers of this node. This method
	 * should only be called from SGApp.
	 */
	public void dispatchMouseOut() {
		for (SGMouseEventHandler handler : mouseHandlers)
			handler.mouseOut(this, getMousePosition(), false);
	}
	
	private void dispatchMouseMoved(PVector mousePosition, boolean dragged) {
		for (SGMouseEventHandler handler : mouseHandlers)
			handler.mouseMoved(this, mousePosition, dragged);
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/* Vector to be reused in getMousePosition. */
	private PVector tempMouseVector = new PVector();
	
	/**
	 * @return The position of the mouse as local coordinates.
	 */
	public PVector getMousePosition() {
		return inverseTMatrix.mult(app.getMouseVector(), tempMouseVector);
	}
	
	// ---------------------------------------------------------------------------------------------
	// system mouse methods:
	
	static boolean traceMClicked = false;
	
	/**
	 * System method. This method should only be called from SGApp.
	 */
	public void processMouseClicked(MouseEvent event) {
		boolean trace = false;
		String tm = null;
		if (traceMClicked) {
			PVector mp = getMousePosition();
			tm = ">> mouseClicked_sys() on " + this + " (" + mp.x + ", " + mp.y + ")";
			if (!visible) println(tm + " - invisible [" + this + "]");
		}
		if (!visible) return;
		if (dispatchMouseEvents && containsMouse()) {
			if (traceMClicked) println(tm + " - dispatched [" + this + "]");
			event.consumed = true;
			dispatchMouseClicked(getMousePosition());
			return;
		}
		if (forwardSysMouseEvents && containsMouse()) {
			if (traceMClicked) println(tm + " - forwarded [" + this + "]");
			for (int i = mouseChildren.size() - 1; i >= 0; i--) {
				SGNode child = mouseChildren.get(i);
				if (!child.visible) continue;
				child.processMouseClicked(event);
				if (event.consumed) return;
			}
		}
	}
	
	/**
	 * System method. This method should only be called from SGApp.
	 */
	public void processMousePressed(MouseEvent event) {
		if (!visible) return;
		if (dispatchMouseEvents && containsMouse()) {
			mouseWasPressed = true;
			event.consumed = true;
			dispatchMousePressed(getMousePosition());
			return;
		}
		if (forwardSysMouseEvents && containsMouse()) {
			for (int i = mouseChildren.size() - 1; i >= 0; i--) {
				SGNode child = mouseChildren.get(i);
				if (!child.visible) continue;
				child.processMousePressed(event);
				if (event.consumed) return;
			}
		}
	}
	
	/**
	 * System method. This method should only be called from SGApp.
	 */
	public void processMouseReleased(MouseEvent event) {
		if (!visible) return;
		if (dispatchMouseEvents && containsMouse()) {
			mouseWasPressed = false;
			event.consumed = true;
			dispatchMouseReleased(getMousePosition());
			return;
		}
		if (forwardSysMouseEvents && containsMouse()) {
			for (int i = mouseChildren.size() - 1; i >= 0; i--) {
				SGNode child = mouseChildren.get(i);
				if (!child.visible) continue;
				child.processMouseReleased(event);
				if (event.consumed) return;
			}
		}
	}
	
	private boolean traceMMove = false;
	
	/**
	 * System method. This method should only be called from SGApp.
	 */
	public void processMouseMoved(MouseEvent event, boolean dragged) {
		if (!visible) return;
		if (traceMMove) println(">> " + this + ".mouseMoved_sys() - dragged: " + dragged);
		if (dispatchMouseEvents) {
			PVector mousePos = getMousePosition();
			if (contains_sys(mousePos)) {
				event.consumed = true;
				if (currentOverNode == this) dispatchMouseMoved(mousePos, dragged);
				else {
					if (currentOverNode != null && currentOverNode.wantsSysMouseEvents)
						currentOverNode.dispatchMouseOut();
					currentOverNode = this;
					dispatchMouseOver(mousePos, dragged);
				}
			}
			else if (currentOverNode == this) {
				currentOverNode = null;
				dispatchMouseOut(mousePos, dragged);
			}
		}
		else if (forwardSysMouseEvents) {
			for (int i = mouseChildren.size() - 1; i >= 0; i--) {
				SGNode child = mouseChildren.get(i);
				if (!child.visible) continue;
				child.processMouseMoved(event, dragged);
				if (event.consumed) return;
			}
		}
	}
	
	/**
	 * System method. This method should only be called from SGApp.
	 */
	void processMouseWheel(MouseEvent event) {
		// TODO
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * Call this method for each child of this container that wants to receive system-mouse-events.
	 * 
	 * @param node The child-node that wants to receive system-mouse-events.
	 */
	protected void forwardMouseEventsTo(SGNode child) {
		if (!mouseChildren.contains(child)) {
			mouseChildren.add(child);
			if (mouseChildren.size() == 1) {
				forwardSysMouseEvents = true;
				updateMouseFlags();
			}
		}
	}
	
	/**
	 * Call this method for each child of this container that no longer wants to receive system-
	 * mouse-events.
	 * 
	 * @param node The child-node that no longer wants to receive system-mouse-events.
	 */
	protected void unforwardMouseEventsTo(SGNode child) {
		if (mouseChildren.remove(child) && mouseChildren.size() == 0) {
			forwardSysMouseEvents = false;
			updateMouseFlags();
		}
	}
	
	// *********************************************************************************************
	// Key functionality:
	// ---------------------------------------------------------------------------------------------
	
	/*
	 * True when at least one key-event-handler has been registered on this node using
	 * addKeyEventHandler(). Do not modify this property.
	 */
	private boolean keyHandlerAdded = false;
	
	/*
	 * True when this node needs to dispatch key-events, i.e. when either keyEventsEnabled or
	 * keyHandlerAdded is true. Do not modify this property.
	 */
	private boolean dispatchKeyEvents = false;
	
	/*
	 * True when the system-key-events should be forwarded to one or more child-node that needs to
	 * dispatch key-events or forward the system-key-events itself. Do not modify this property.
	 */
	private boolean forwardSysKeyEvents = false;
	
	/*
	 * True when this node should receive system-key-events. Do not modify this property.
	 */
	private boolean wantsSysKeyEvents = false;
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/**
	 * @return True when this node should receive system-key-events. Do not modify this property.
	 */
	public boolean wantsSysKeyEvents() {
		return wantsSysKeyEvents;
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/* The collection of registered key-event-handlers. */
	private CopyOnWriteArraySet<SGKeyEventHandler> keyHandlers = new CopyOnWriteArraySet<SGKeyEventHandler>();
	
	/**
	 * Add a key-event handler.
	 * 
	 * @param handler The handler to add.
	 * 
	 * @see SGNode#removeKeyEventHandler(SGMouseEventHandler)
	 */
	public void addKeyEventHandler(SGKeyEventHandler handler) {
		if (keyHandlers.add(handler) && keyHandlers.size() == 1) {
			keyHandlerAdded = true;
			updateKeyFlags();
		}
	}
	
	/**
	 * Remove a key-event handler.
	 * 
	 * @param handler The handler to remove.
	 * 
	 * @see SGNode#addKeyEventHandler(SGKeyEventHandler)
	 */
	public void removeKeyEventHandler(SGKeyEventHandler handler) {
		if (keyHandlers.remove(handler) && keyHandlers.size() == 0) {
			keyHandlerAdded = false;
			updateKeyFlags();
		}
	}
	
	private void updateKeyFlags() {
		dispatchKeyEvents = keyHandlerAdded;
		wantsSysKeyEvents = keyHandlerAdded || forwardSysKeyEvents;
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	// Key-event dispatch helper methods.
	
	private void dispatchKeyTyped(KeyEvent event) {
		for (SGKeyEventHandler handler : keyHandlers)
			handler.keyTyped(this, event);
	}
	
	private void dispatchKeyPressed(KeyEvent event) {
		for (SGKeyEventHandler handler : keyHandlers)
			handler.keyPressed(this, event);
	}
	
	private void dispatchKeyReleased(KeyEvent event) {
		for (SGKeyEventHandler handler : keyHandlers)
			handler.keyReleased(this, event);
	}
	
	// ---------------------------------------------------------------------------------------------
	// system key methods:
	
	/**
	 * System method. This method should only be called from SGApp.
	 */
	public void processKeyTyped(KeyEvent event) {
		if (dispatchKeyEvents) dispatchKeyTyped(event);
		if (forwardSysKeyEvents) {
			for (SGNode child : keyChildren)
				if (child.visible) child.processKeyTyped(event);
		}
	}
	
	/**
	 * System method. This method should only be called from SGApp.
	 */
	public void processKeyPressed(KeyEvent event) {
		if (dispatchKeyEvents) dispatchKeyPressed(event);
		if (forwardSysKeyEvents) {
			for (SGNode child : keyChildren)
				if (child.visible) child.processKeyPressed(event);
		}
	}
	
	/**
	 * System method. This method should only be called from SGApp.
	 */
	public void processKeyReleased(KeyEvent event) {
		if (dispatchKeyEvents) dispatchKeyReleased(event);
		if (forwardSysKeyEvents) {
			for (SGNode child : keyChildren)
				if (child.visible) child.processKeyReleased(event);
		}
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * Call this method for each child of this container that wants to receive system-key-events.
	 * 
	 * @param node The child-node that wants to receive system-key-events.
	 */
	protected void forwardKeyEventTo(SGNode child) {
		if (!keyChildren.contains(child)) {
			keyChildren.add(child);
			if (keyChildren.size() == 1) {
				forwardSysKeyEvents = true;
				updateKeyFlags();
			}
		}
	}
	
	/**
	 * Call this method for each child of this container that no longer wants to receive system-
	 * key-events.
	 * 
	 * @param node The child-node that no longer wants to receive system-key-events.
	 */
	protected void unforwardKeyEventsTo(SGNode child) {
		if (keyChildren.remove(child) && keyChildren.size() == 0) {
			forwardSysKeyEvents = false;
			updateKeyFlags();
		}
	}
	
	// *********************************************************************************************
	// Other methods:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @return True if the renderer of the given PGraphics object is JAVA2D.
	 */
	public boolean isJAVA2D(PGraphics g) {
		return g.getClass().getName().equals(PConstants.JAVA2D);
	}
	
	/* @see java.lang.Object#toString() */
	@Override
	public String toString() {
		return name;
	}
	
	// *********************************************************************************************
	// Testing:
	// ---------------------------------------------------------------------------------------------
	
	private class CheckTreeState {
		public boolean stagePendingError = false;
		public boolean childEqParentError = false;
	}
	
	public void checkTree() {
		boolean stageRedrawPending = app.getStage().redrawPending();
		CheckTreeState state = new CheckTreeState();
		if (!stageRedrawPending) checkTreeRec(app.getStage(), state);
		if (state.stagePendingError) {
			printTree();
			throw new Error("Found error in SGNode.checkTree. See System.err for more details. ["
					+ this + "]");
		}
	}
	
	public void checkTreeRec(SGNode node, CheckTreeState state) {
		if (node.redrawPending) {
			if (!state.stagePendingError) {
				System.err.println("The stage redraw is not pending,"
						+ " but the redraw of these nodes is pending: [" + this + "]");
				state.stagePendingError = true;
			}
			System.err.println(" - " + node.name);
		}
		for (SGNode child : node.children) {
			if (child == node) {
				state.childEqParentError = true;
				printTree();
				throw new Error("Unexpected, child == node - node: " + node + ", child: " + child);
			}
			checkTreeRec(child, state);
		}
	}
	
	// ---------------------------------------------------------------------------------------------
	
	public void printTree() {
		println("\n# TREE");
		printTree(app.getStage(), "");
	}
	
	private void printTree(SGNode node, String indent) {
		String line = indent + "- " + node;
		if (node.redrawPending) line += " - redrawPending";
		println(line);
		indent += "  ";
		for (SGNode child : node.children) {
			if (child == node) {
				println(indent + "! " + child + " - SAME AS PARENT");
				return;
			}
			printTree(child, indent);
		}
	}
	
}
