package be.multec.sg;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import be.multec.sg.modifiers.IModifier;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PMatrix2D;

/**
 * Base class for nodes in a scene-graph. Each node can act as a container of child-nodes.
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
	protected PApplet app;
	
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
	
	/* True when this node represents 3D content. */
	protected boolean is3D = false;
	
	// *********************************************************************************************
	// Constructors:
	// ---------------------------------------------------------------------------------------------

	/**
	 * Basic constructor.
	 * 
	 * @param app The scene-graph application object.
	 */
	public SGNode(PApplet app) {
		super(app);
		nodeCounter++;
		this.app = app;
		if (name == null) name = makeName(nodeCounter);
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/** Override to give the objects a custom name. */
	protected String makeName(int nodeCounter) {
		return getClass().getName() + "_" + nodeCounter;
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
		
		applyTranslate = applyRotate = applyRotateX = applyRotateY = applyRotateZ = false;
		applyScale = applyTransformation = false;
		
		bounds = localBounds = transfBounds = null;
		boundsDirty = localBoundsDirty = transfBoundsDirty = maintainBounds = false;
		maintainBoundsCnt = 0;
		
		updatePending = redrawPending = false;
		
		clearCache();
		cached = cacheContentDirty = cacheSizeDirty = false;
		
		inverseTMatrix = null;
		inverseTMatrixDirty = maintainInverseTMatrix = false;
		localTMatrix = null;
		localTMatrixDirty = false;
		
		mouseEventsEnabled = mouseHandlerAdded = dispatchMouseEvents = false;
		forwardSysMouseEvents = wantsSysMouseEvents = false;
		mouseHandlers.clear();
		mouseIsOver = mouseWasPressed = mouseXDirty = mouseYDirty = false;
		
		disposed = true;
		
		super.dispose();
	}
	
	@Override
	protected void finalize() throws Throwable {
		if (!disposed) dispose();
		super.finalize();
	}
	
	// *********************************************************************************************
	// Basic accessors:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @return The scene-graph application.
	 */
	public PApplet getApp() {
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
	
	/**
	 * @return True when this node represents 2D content.
	 */
	public boolean is2D() {
		return !is3D;
	}
	
	/**
	 * @return True when this node represents 3D content.
	 */
	public boolean is3D() {
		return is3D;
	}
	
	// *********************************************************************************************
	// Visibility:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @return True when this node is drawn and dispatches mouse or other events.
	 */
	public boolean isVisible() {
		return visible;
	}
	
	/**
	 * @param visible True when this node should be drawn, or false when this node should not be
	 *            drawn and should not dispatch mouse or other events.
	 */
	public void setVisible(boolean visible) {
		if (this.visible == visible) return;
		this.visible = visible;
		invalidateContent();
		if (visible) invalidateBounds(false);
		if (parent != null) parent.invalidateBounds(false);
	}
	
	/** Set the visibility of this node to true. */
	public void show() {
		if (!visible) setVisible(true);
	}
	
	/** Set the visibility of this node to false. */
	public void hide() {
		if (visible) setVisible(false);
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
	
	/* True when a rotation needs to be applied this node in a 2D-scene-graph. */
	private boolean applyRotate = false;
	
	/* True when a rotation around the x-axis needs to be applied this node in a 3D-scene-graph. */
	private boolean applyRotateX = false;
	
	/* True when a rotation around the y-axis needs to be applied this node in a 3D-scene-graph. */
	private boolean applyRotateY = false;
	
	/* True when a rotation around the z-axis needs to be applied this node in a 3D-scene-graph. */
	private boolean applyRotateZ = false;
	
	/* True when a rotation around the x-axis needs to be applied this node in a 3D-scene-graph. */
	private boolean applyScale = false;
	
	/* True when some transformation needs to be applied before drawing this node. */
	private boolean applyTransformation = false;
	
	// ---------------------------------------------------------------------------------------------
	
	/* Helper method. */
	private void updateApplyTranslate() {
		applyTranslate = x != 0 || y != 0 || z != 0;
		applyTransformation = applyTranslate || applyRotate || applyScale;
	}
	
	/* Helper method. */
	private void updateApplyRotate2D() {
		applyRotate = rotation != 0;
		applyTransformation = applyTranslate || applyRotate || applyScale;
	}
	
	/* Helper method. */
	private void updateApplyRotate3D() {
		if (!app.g.is3D())
			throw new Error("SGNode.updateApplyRotate3D() should be used in a 3D context.");
		applyRotate = rotationX != 0 || rotationY != 0 || rotationZ != 0;
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
	
	/**
	 * The position in the z-axis of the origin of the coordinate system of this node with respect
	 * to the coordinate system of its parent. This property is only relevant in a 3D-scene-graph.
	 */
	private float z = 0;
	
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
		invalidateTMatrix();
		invalidateContent();
		invalidateBounds(true);
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
		invalidateTMatrix();
		invalidateContent();
		invalidateBounds(true);
	}
	
	/**
	 * Get the z-position of the origin of this node. Only available in a 3D-scene-graph.
	 */
	public float getZ() {
		if (!app.g.is3D()) throw new Error("SGNode.getZ() should be used in a 3D context.");
		return z;
	}
	
	/**
	 * Set the z-position of the origin of this node. Only available in a 3D-scene-graph.
	 * 
	 * @param z the z to set
	 */
	public void setZ(float z) {
		if (this.z == z) return;
		if (!app.g.is3D()) throw new Error("SGNode.setZ(float) should be used in a 3D context.");
		this.z = z;
		updateApplyTranslate();
		// invalidateTransformationMatrix(); // TODO: for now not enabled in 3D
		invalidateContent();
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
		invalidateTMatrix();
		invalidateContent();
		invalidateBounds(true);
		return this;
	}
	
	/**
	 * Sets the 3D-position of this object to the given coordinate. Only available in a
	 * 3D-scene-graph.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return This object.
	 */
	public SGNode moveTo(float x, float y, float z) {
		if (this.x == x && this.y == y && this.z == z) return this;
		if (!app.g.is3D())
			throw new Error("SGNode.moveTo(float, float, float) should be used in a 3D context.");
		this.x = x;
		this.y = y;
		this.z = z;
		updateApplyTranslate();
		// invalidateTransformationMatrix(); // for now not enabled in 3D
		invalidateContent();
		invalidateBounds(true);
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
		invalidateTMatrix();
		invalidateContent();
		invalidateBounds(true);
		return this;
	}
	
	/**
	 * Updates the position of this object by adding the given coordinate-vector to the current
	 * position. Only available in a 3D-scene-graph.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return This object.
	 */
	public SGNode move(float x, float y, float z) {
		if (x == 0 && y == 0 && z == 0) return this;
		if (!app.g.is3D())
			throw new Error("SGNode.move(float, float, float) should be used in a 3D context.");
		this.x += x;
		this.y += y;
		this.z += z;
		updateApplyTranslate();
		// invalidateTransformationMatrix(); // TODO: for now not enabled in 3D
		invalidateContent();
		return this;
	}
	
	// *********************************************************************************************
	// Rotation in 2D-scene-graphs:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * The angle around the z-axis in radians that determines the rotation of the coordinate system
	 * of this node with respect to the coordinate system of its parent. This property is only
	 * relevant in 2D-scene-graphs.
	 */
	private float rotation = 0;
	
	/**
	 * @return The rotation in radians (around the z-axis). Only available in a 2D-scene-graph.
	 */
	public float getRotation() {
		// if (app.g.is3D()) throw new Error("SGNode.getRotation() used in a" +
		// " 3D context - node: "
		// + this.toString());
		// else return rotation;
		return rotation;
	}
	
	/**
	 * Sets the rotation of this node to given angle.
	 * 
	 * @param rotation The rotation in radians (around the z-axis). Only available in a
	 *            2D-scene-graph.
	 */
	public void rotateTo(float angle) {
		if (this.rotation == angle) return;
		this.rotation = angle;
		updateApplyRotate2D();
		invalidateTMatrix();
		invalidateContent();
		invalidateBounds(true);
		
	}
	
	/**
	 * Adds the given angle to the current rotation of this node.
	 * 
	 * @param rotation The rotation in radians to add to the current rotation (around the z-axis).
	 *            Only available in a 2D-scene-graph.
	 */
	public void rotate(float angle) {
		if (angle == 0) return;
		this.rotation += angle;
		updateApplyRotate2D();
		invalidateTMatrix();
		invalidateContent();
		invalidateBounds(true);
	}
	
	// *********************************************************************************************
	// Rotation in 3D-scene-graphs:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * The angle around the x-axis in radians that determines the rotation of the coordinate system
	 * of this node with respect to the coordinate system of its parent. This property is only
	 * relevant in a 3D-scene-graph.
	 */
	private float rotationX = 0;
	
	/**
	 * The angle around the y-axis in radians that determines the rotation of the coordinate system
	 * of this node with respect to the coordinate system of its parent. This property is only
	 * relevant in a 3D-scene-graph.
	 */
	private float rotationY = 0;
	
	/**
	 * The angle around the z-axis in radians that determines the rotation of the coordinate system
	 * of this node with respect to the coordinate system of its parent. This property is only
	 * relevant in a 3D-scene-graph.
	 */
	private float rotationZ = 0;
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/**
	 * @return The rotation in radians around the x-axis. Only available in 3D-graphs.
	 */
	public float getRotationX() {
		if (!app.g.is3D())
			throw new Error("SGNode.getRotationX() should be used in a 3D context.");
		return rotationX;
	}
	
	/**
	 * Sets the rotation in radians around the x-axis. Only available in 3D-graphs.
	 * 
	 * @param rotation the rotation in radians to set
	 */
	public void setRotationX(float rotation) {
		if (!app.g.is3D())
			throw new Error("SGNode.setRotationX() should be used in a 3D context.");
		if (this.rotationX == rotation) return;
		this.rotationX = rotation;
		updateApplyRotate3D();
		// invalidateTransformationMatrix(); // for now not enabled in 3D
		invalidateContent();
	}
	
	/**
	 * Adds an angle in radians to the current x-rotation. Only available in 3D-graphs.
	 * 
	 * @param rotation the angle in radians to add to the current rotation around the x-axis
	 */
	public void addRotationX(float rotation) {
		if (!app.g.is3D())
			throw new Error("SGNode.addRotationX() should be used in a 3D context.");
		setRotationX(rotationX + rotation);
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/**
	 * @return The rotation in radians around the y-axis. Only available in 3D-graphs.
	 */
	public float getRotationY() {
		if (!app.g.is3D())
			throw new Error("SGNode.getRotationY() should be used in a 3D context.");
		return rotationY;
	}
	
	/**
	 * Sets the rotation in radians around the y-axis. Only available in 3D-graphs.
	 * 
	 * @param rotation the rotation to set
	 */
	public void setRotationY(float rotation) {
		if (!app.g.is3D())
			throw new Error("SGNode.setRotationY() should be used in a 3D context.");
		if (this.rotationY == rotation) return;
		this.rotationY = rotation;
		updateApplyRotate3D();
		// invalidateTransformationMatrix(); // for now not enabled in 3D
		invalidateContent();
	}
	
	/**
	 * Adds an angle in radians to the current y-rotation. Only available in 3D-graphs.
	 * 
	 * @param rotation the angle in radians to add to the current rotation around the y-axis
	 */
	public void addRotationY(float rotation) {
		if (!app.g.is3D())
			throw new Error("SGNode.addRotationY() should be used in a 3D context.");
		setRotationY(rotationY + rotation);
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/**
	 * @return The rotation in radians around the z-axis. Only available in 3D-graphs.
	 */
	public float getRotationZ() {
		if (!app.g.is3D())
			throw new Error("SGNode.getRotationZ() should be used in a 3D context.");
		return rotationZ;
	}
	
	/**
	 * Sets the rotation in radians around the z-axis. Only available in 3D-graphs.
	 * 
	 * @param rotation the rotation to set
	 */
	public void setRotationZ(float rotation) {
		if (!app.g.is3D())
			throw new Error("SGNode.setRotationZ() should be used in a 3D context.");
		if (this.rotationZ == rotation) return;
		this.rotationZ = rotation;
		updateApplyRotate3D();
		// invalidateTransformationMatrix(); // for now not enabled in 3D
		invalidateContent();
	}
	
	/**
	 * Adds an angle in radians to the current z-rotation. Only available in 3D-graphs.
	 * 
	 * @param rotation the angle in radians to add to the current rotation around the z-axis
	 */
	public void addRotationZ(float rotation) {
		if (!app.g.is3D())
			throw new Error("SGNode.addRotationZ() should be used in a 3D context.");
		setRotationZ(rotationZ + rotation);
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
		// System.out.println(">> SGNode[" + this + "].scale()");
		if (this.scale == scale) return;
		
		this.scale = scale;
		applyScale = scale != 0;
		applyTransformation = applyTranslate || applyRotate || applyScale;
		invalidateTMatrix();
		invalidateContent();
		invalidateBounds(true);
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
	protected boolean hasChildren = false;
	
	/** The list of child nodes in this node. */
	protected final CopyOnWriteArrayList<SGNode> children = new CopyOnWriteArrayList<SGNode>();
	
	/**
	 * The list of child nodes in this node to which system-mouse-events should be forwarded.
	 * 
	 * Note that forwardSysMouseEvents is true when there is one or more nodes in this collection.
	 */
	protected final CopyOnWriteArrayList<SGNode> mouseChildren = new CopyOnWriteArrayList<SGNode>();
	
	/**
	 * The list of child nodes in this node to which system-key-events should be forwarded.
	 * 
	 * Note that forwardSysMouseEvents is true when there is one or more nodes in this collection.
	 */
	protected final CopyOnWriteArrayList<SGNode> keyChildren = new CopyOnWriteArrayList<SGNode>();
	
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
		// System.out.println(">> SGNode[" + this.name + "].addChild()");
		if (child.addedToSG) {
			throw new Error("SGNode.addChild(SGNode) was" + " called (on " + this.toString()
					+ ") with a child that" + " is already part of the scene-graph - container: .");
			
		}
		else if (children.add(child)) {
			hasChildren = true;
			child.parent = this;
			if (addedToSG) child.onAddedToSG();
			if (child.wantsSysMouseEvents) forwardMouseEventTo(child);
			if (child.wantsSysKeyEvents) forwardKeyEventTo(child);
			if (child.updatePending && !updatePending) invalidateState();
			invalidateContent(); // always request redraw
			if (maintainBounds) {
				child.maintainBounds();
				invalidateBounds(false);
			}
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
	
	/**
	 * Adds a child-node in this container.
	 * 
	 * @param node The child-node to add in this container.
	 * @param x The x-position to use for the child-node.
	 * @param y The y-position to use for the child-node.
	 * @param y The z-position to use for the child-node.
	 * @throws RuntimeException when the given child is already in the scene-graph
	 */
	public SGNode addNode(SGNode node, float x, float y, float z) {
		if (!app.g.is3D()) return addNode(node, x, y);
		addNode(node);
		node.moveTo(x, y, z);
		return node;
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
			invalidateContent();
			if (maintainBounds) {
				child.unmaintainBounds();
				invalidateBounds(false);
			}
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
			invalidateContent();
			if (maintainBounds) {
				child.unmaintainBounds();
				invalidateBounds(false);
			}
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
			if (maintainBounds) child.unmaintainBounds();
			if (child.wantsSysMouseEvents) unforwardMouseEventsTo(child);
			if (child.wantsSysKeyEvents) unforwardKeyEventsTo(child);
		}
		children.clear();
		if (maintainBounds) invalidateBounds(false);
		hasChildren = false;
		invalidateContent();
	}
	
	// *********************************************************************************************
	// Bounds:
	// ---------------------------------------------------------------------------------------------
	
	/*
	 * The bounds are maintained when this property is true.
	 * 
	 * Do not modify this property. Use the maintainBounds and unmaintainBounds methods.
	 * 
	 * @see maintainBounds
	 * 
	 * @see unmaintainBounds
	 */
	private boolean maintainBounds = false;
	
	/*
	 * The bounds are maintained when this property larger than 0. For each call of the
	 * maintainBounds method this value is increased. It is decreased for each call of the
	 * unmaintainBounds method. This approach is necessary because there might be more than one
	 * independent reason for maintaining the bounds. When one of the these reasons no longer
	 * applies, the other might still apply.
	 * 
	 * Do not modify this property. Use the maintainBounds and unmaintainBounds methods.
	 * 
	 * @see maintainBounds
	 * 
	 * @see unmaintainBounds
	 */
	private int maintainBoundsCnt = 0;
	
	/**
	 * Call this method when the bounds for this node need to be maintained.
	 */
	public void maintainBounds() {
		if (maintainBoundsCnt == 0) {
			maintainBounds = localBoundsDirty = boundsDirty = true;
			for (SGNode child : children)
				child.maintainBounds();
			invalidateState();
		}
		maintainBoundsCnt++;
	}
	
	/**
	 * Call this method when the bounds for this node no longer need to be maintained.
	 */
	public void unmaintainBounds() {
		if (maintainBoundsCnt == 0) throw new Error("Too many unmaintainBounds calls.");
		maintainBoundsCnt--;
		if (maintainBoundsCnt == 0) {
			maintainBounds = localBoundsDirty = boundsDirty = false;
			for (SGNode child : children)
				child.unmaintainBounds();
		}
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/*
	 * The bounds of the graphical content of this node and all of its child nodes. The
	 * transformations that apply to this nodes are not considered in these bounds. This value is
	 * not valid when the boundsDirty property is true.
	 * 
	 * @see boundsDirty
	 */
	private Rectangle bounds;
	
	/*
	 * True when the bounds property is not valid and needs to be updated.
	 * 
	 * @see bounds
	 */
	private boolean boundsDirty = false;
	
	// ---------------------------------------------------------------------------------------------
	
	/*
	 * The bounds of the graphical content of this node without that of its child nodes. The
	 * transformations that apply to this nodes are not considered in these bounds. This value is
	 * not valid when the localBoundsDirty property is true.
	 * 
	 * @see localBoundsDirty
	 */
	private Rectangle localBounds;
	
	/*
	 * True when the localBounds property is not valid and needs to be updated.
	 * 
	 * @see localBounds
	 */
	private boolean localBoundsDirty = false;
	
	// ---------------------------------------------------------------------------------------------
	
	/*
	 * The bounds of this node with the transformations for this node applied to it. This value is
	 * not valid when the transformedBoundsDirty property is true.
	 * 
	 * @see transfBoundsDirty
	 */
	private Rectangle transfBounds;
	
	/*
	 * True when the transformedBounds property is not valid and needs to be updated. This is the
	 * case when either the bounds or the transformations for this node were modified.
	 * 
	 * @see transformedBounds
	 */
	private boolean transfBoundsDirty = false;
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @return The bounds of this node with the transformations for this node applied to it. This
	 *         value is not valid when the transformedBoundsDirty property is true.
	 * 
	 * @see transformedBoundsDirty
	 */
	final public Rectangle getBounds() {
		if (!maintainBounds) { throw new Error(
				"The bounds for this node are not maintained. Use the maintainBounds() method."); }
		if (transfBoundsDirty) { throw new Error("The bounds are dirty."); }
		return transfBounds;
	}
	
	/**
	 * @return The width of the (transformed) graphics content of this this node and its child
	 *         nodes.
	 */
	final public float getWidth() {
		if (!maintainBounds) { throw new Error(
				"The bounds for this node are not maintained. Use the maintainBounds() method."); }
		if (transfBoundsDirty) { throw new Error("The bounds are dirty."); }
		return transfBounds.width;
	}
	
	/**
	 * @return The height of the (transformed) graphicsrt content of this this node and its child
	 *         nodes.
	 */
	final public float getHeight() {
		if (!maintainBounds) { throw new Error(
				"The bounds for this node are not maintained. Use the maintainBounds() method."); }
		if (transfBoundsDirty) { throw new Error("The bounds are dirty."); }
		return transfBounds.height;
	}
	
	/**
	 * @return The bounds of the graphical content of this node and all of its child nodes. The
	 *         transformations that apply to this nodes are not considered in these bounds. This
	 *         value is not valid when the boundsDirty property is true.
	 * 
	 * @see boundsDirty
	 */
	final public Rectangle getUntransformedBounds() {
		if (!maintainBounds) { throw new Error(
				"The bounds for this node are not maintained. Use the maintainBounds() method."); }
		if (boundsDirty) { throw new Error("The bounds are dirty."); }
		return bounds;
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * Call this method from the implementation of concrete nodes with graphical content whenever
	 * this content changed such that the bounds of that content changed as well.
	 * 
	 * @param local Should be true when the local bounds should be invalidated too.
	 */
	protected void invalidateBounds(boolean local) {
		if (!maintainBounds || is3D) return;
		// System.out.println(">> SGNode[" + name + "].invalidateBounds() - " + boundsDirty
		// + " - current bounds: " + rectStr(bounds));
		if (local && !localBoundsDirty) localBoundsDirty = true;
		if (!boundsDirty) {
			// Set the boundsDirty property to true for this node and request an update.
			// During the update-phase of the next update-loop, the bounds will be updated by
			// calling the updateLocalBounds method, which is called from the updateBounds method.
			boundsDirty = true;
			invalidateState();
		}
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * Implement this method for each node that has graphical content. Set the bounds -x, y, width &
	 * height- in the given localBounds rectangle.
	 * 
	 * @param localBounds The rectangle in which to set the local bounds.
	 */
	protected void updateLocalBounds(Rectangle localBounds) {}
	
	/*
	 * Updates the bounds. This method is called from the during the return-phase of the depth-first
	 * update-traversal of the node tree. The local bounds will be updated when needed by calling
	 * the updateLocalBounds method. The boundsDirty property of the parent of this node is set to
	 * true when the local bounds are found to have been effectively modified. The bounds of the
	 * parent will, as a consequence, be updated when the update-traversal returns to the parent.
	 */
	private void updateBounds() {
		boolean trace = false;
		if (!maintainBounds) return;
		if (bounds == null) bounds = new Rectangle();
		if (trace)
			System.out.println(">> SGNode[" + name
					+ "].updateBounds() - current transformedBounds: " + rectStr(bounds));
		
		// Handle invisible case:
		if (!visible) {
			if (parent != null && !parent.boundsDirty) parent.boundsDirty = true;
			return;
		}
		
		// Update the bounds of the graphical content drawn by this node (ignoring the children):
		if (localBoundsDirty) {
			if (localBounds == null) localBounds = new Rectangle();
			updateLocalBounds(localBounds);
			localBoundsDirty = false;
		}
		
		// Remember the current bounds to check for effective changes:
		int px = bounds.x;
		int py = bounds.y;
		int pw = bounds.width;
		int ph = bounds.height;
		
		if (localBounds != null) {
			bounds.x = localBounds.x;
			bounds.y = localBounds.y;
			bounds.width = localBounds.width;
			bounds.height = localBounds.height;
			if (trace) System.out.println("   local : " + rectStr(localBounds));
		}
		else bounds.x = bounds.y = bounds.width = bounds.height = 0;
		
		for (SGNode child : children) {
			if (trace)
				System.out.println("   adding: " + rectStr(child.transfBounds) + "  ("
						+ rectStr(child.bounds) + ")");
			if (child.visible) bounds.add(child.transfBounds);
		}
		
		if (trace) System.out.println("   result: " + rectStr(bounds));
		
		// Check if the (untransformed) bounds changed:
		if (px != bounds.x || py != bounds.y || pw != bounds.width || ph != bounds.height) {
			if (cached) cacheSizeDirty = true;
			if (parent != null && !parent.boundsDirty) parent.boundsDirty = true;
			transfBoundsDirty = true;
		}
		
		if (transfBoundsDirty) updateTransformedBounds();
		
		boundsDirty = false;
	}
	
	/* Updates the transformed bounds. */
	private void updateTransformedBounds() {
		boolean trace = false;
		if (trace)
			System.out.println("* [" + this.name + "].updateTransformedBounds() - "
					+ applyTranslate + ", " + applyRotate + ", " + applyScale + " -- "
					+ rectStr(bounds));
		
		if (transfBounds == null) transfBounds = new Rectangle();
		
		// check if the transformed bounds changed:
		int px = transfBounds.x;
		int py = transfBounds.y;
		int pw = transfBounds.width;
		int ph = transfBounds.height;
		
		if (applyRotate || applyScale) {
			float x1 = localTMatrix.multX(bounds.x, bounds.y);
			float y1 = localTMatrix.multY(bounds.x, bounds.y);
			float x2 = localTMatrix.multX(bounds.x + bounds.width, bounds.y);
			float y2 = localTMatrix.multY(bounds.x + bounds.width, bounds.y);
			float x3 = localTMatrix.multX(bounds.x + bounds.width, bounds.y + bounds.height);
			float y3 = localTMatrix.multY(bounds.x + bounds.width, bounds.y + bounds.height);
			float x4 = localTMatrix.multX(bounds.x, bounds.y + bounds.height);
			float y4 = localTMatrix.multY(bounds.x, bounds.y + bounds.height);
			transfBounds.x = (int) Math.floor(Math.min(Math.min(x1, x2), Math.min(x3, x4)));
			transfBounds.y = (int) Math.floor(Math.min(Math.min(y1, y2), Math.min(y3, y4)));
			transfBounds.width = (int) Math.ceil(Math.max(Math.max(x1, x2), Math.max(x3, x4)))
					- transfBounds.x;
			transfBounds.height = (int) Math.ceil(Math.max(Math.max(y1, y2), Math.max(y3, y4)))
					- transfBounds.y;
		}
		else if (applyTranslate) {
			transfBounds.x = (int) Math.floor(bounds.x + x);
			transfBounds.y = (int) Math.floor(bounds.y + y);
			transfBounds.width = bounds.width;
			transfBounds.height = bounds.height;
		}
		else {
			transfBounds.x = bounds.x;
			transfBounds.y = bounds.y;
			transfBounds.width = bounds.width;
			transfBounds.height = bounds.height;
		}
		
		if (px != bounds.x || py != bounds.y || pw != bounds.width || ph != bounds.height) {
			if (parent != null && !parent.boundsDirty) parent.boundsDirty = true;
		}
		
		transfBoundsDirty = false;
		if (trace) System.out.println("   result: " + rectStr(transfBounds));
	}
	
	// *********************************************************************************************
	// Update functionality:
	// ---------------------------------------------------------------------------------------------
	
	/* True when this node needs to be updated. */
	private boolean updatePending = true;
	
	// ---------------------------------------------------------------------------------------------
	
	/** The list of child nodes in this node. */
	protected CopyOnWriteArrayList<IModifier> modifiers;
	
	/**
	 * Add a modifier for this node.
	 * 
	 * @param modifier
	 */
	public void addModifier(IModifier modifier) {
		if (modifiers == null) modifiers = new CopyOnWriteArrayList<IModifier>();
		modifiers.add(modifier);
		if (modifiers.size() == 1) invalidateState();
	}
	
	/**
	 * Remove a modifier from this node.
	 * 
	 * @param modifier
	 */
	public void removeModifier(IModifier modifier) {
		modifiers.remove(modifier);
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
	final protected void invalidateState() {
		if (updatePending) return;
		updatePending = true;
		if (isStage) {
			if (app != null) app.redraw();
		}
		else if (parent != null && !parent.updatePending) parent.invalidateState();
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * A system method that calls the update() method for this node and for its child-nodes. Do not
	 * override this method in custom node-classes. Override the update() method instead.
	 * 
	 * @see SGNode#update()
	 */
	final protected void update_sys() {
		if (disposed || !updatePending) return;
		boolean trace = false;
		updatePending = false;
		if (trace) {
			System.out.println(">> SGNode[" + this.name + "].update_sys() - localTMatrixDirty: "
					+ localTMatrixDirty);
			if (maintainBounds) {
				System.out.println(" + maintainBounds: - boundsDirty: " + boundsDirty
						+ ", transfBoundsDirty: " + transfBoundsDirty);
			}
		}
		
		// update local transformation matrix:
		if (maintainBounds && localTMatrixDirty) {
			localTMatrix.reset();
			if (applyTranslate) localTMatrix.translate(x, y);
			if (applyRotate) localTMatrix.rotate(rotation);
			if (applyScale) localTMatrix.scale(scale, scale, scale);
			transfBoundsDirty = true;
			localTMatrixDirty = false;
		}
		
		// traverse the children, except for cached nodes:
		if (hasChildren) {
			for (SGNode child : children) {
				if (child.visible && child.updatePending) child.update_sys();
			}
		}
		
		// apply the modifiers:
		if (modifiers != null && modifiers.size() > 0) {
			for (IModifier modifier : modifiers) {
				modifier.apply(this);
			}
			if (modifiers.size() > 0) invalidateState();
		}
		
		// update the bounds when needed:
		if (maintainBounds) {
			if (boundsDirty) updateBounds();
			else if (transfBoundsDirty) updateTransformedBounds();
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
	final boolean redrawPending() {
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
	
	/**
	 * Trigger a redraw of this node.
	 * 
	 * Use the invalidateContentFromUpdate method instead of this method when you want to request a
	 * redraw of this node from within the update loop.
	 * 
	 * @see invalidateContentFromUpdate
	 */
	final public void invalidateContent() {
		if (disposed || redrawPending) return;
		// System.out.println(">> SGNode[" + name + "].invalidateContent()");
		redrawPending = true;
		
		if (!visible) return;
		if (cached) cacheContentDirty = true;
		if (parent != null) parent.invalidateContent();
		else if (isStage && app != null) app.redraw();
	}
	
	/**
	 * Use this method instead of invalidateContent when you want to request a redraw of this node
	 * from within the update loop.
	 * 
	 * @see invalidateContent
	 */
	final public void invalidateContentFromUpdate() {
		if (disposed || redrawPending) return;
		redrawPending = true;
		
		if (!visible) return;
		if (cached) cacheContentDirty = true;
		if (parent != null) parent.invalidateContent();
		// do not call app.redraw() in this version
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * A system function that applies the transformations, calls the draw() method for this node and
	 * for its child-nodes. Do not override this method in custom node-classes. Override the draw()
	 * method instead.
	 * 
	 * @see SGNode#draw(processing.core.PGraphics)
	 */
	protected void draw_sys(PGraphics g) {
		if (!visible || disposed) return;
		// System.out.println(">> SGNode[" + this + "].draw_sys()");
		
		if (cached) {
			if (bounds.width == 0 || bounds.height == 0) { return; }
			if (cache == null) {
				cacheContentDirty = true;
				initCache();
			}
			else if (cacheSizeDirty) {
				cacheContentDirty = true;
				resizeCache();
				cacheSizeDirty = false;
			}
			
			if (cacheContentDirty) {
				cache.beginDraw();
				cache.resetMatrix();
				cache.translate(-bounds.x, -bounds.y);
				cache.clear();
				draw(cache);
				if (hasChildren) {
					for (SGNode child : children) {
						if (child.visible) child.draw_sys(cache);
					}
				}
				cache.resetMatrix();
				cache.endDraw();
			}
			
			applyTransformation(g);
			g.copy(cache, 0, 0, bounds.width, bounds.height, bounds.x, bounds.y, bounds.width,
					bounds.height);
		}
		else {
			applyTransformation(g);
			draw(g); // call the draw() method on this node:
			
			// forward the draw_sys() call to each child:
			if (hasChildren) {
				for (SGNode child : children) {
					if (child.visible) child.draw_sys(g);
				}
			}
		}
		
		if (applyTransformation) g.popMatrix();
		inverseTMatrixDirty = false;
		redrawPending = false;
	}
	
	protected void applyTransformation(PGraphics g) {
		if (applyTransformation) {
			g.pushMatrix();
			if (is3D) { // 3D node
				if (applyTranslate) g.translate(x, y, z);
				if (applyRotate) {
					if (rotationX != 0) g.rotateX(rotationX);
					if (rotationY != 0) g.rotateY(rotationY);
					if (rotationZ != 0) g.rotateZ(rotationZ);
				}
			}
			else { // 2D-scene-graph
					// if (applyTranslate) System.out.println(" translate(" + x + ", " + y + ")");
				if (applyTranslate) g.translate(x, y);
				if (applyRotate) g.rotate(rotation);
			}
			if (applyScale) g.scale(scale);
		}
		
		// update the locally stored transformation matrix:
		if (inverseTMatrixDirty && g.is2D()) {
			// System.out.println("* transformMatrixDirty [" + this + "]");
			// app.printMatrix();
			
			// update the inverse transformation matrix:
			inverseTMatrix = (PMatrix2D) g.getMatrix();
			inverseTMatrix.invert();
		}
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
		if (app.g.is3D()) { throw new Error("Caching is currently not supported for 3D content."); }
		if (this.cached == cached) return;
		this.cached = cached;
		if (cached) {
			cacheContentDirty = true;
			maintainBounds();
		}
		else {
			clearCache();
			unmaintainBounds();
		}
	}
	
	/**
	 * Convenience method that enables the cache.
	 */
	public void cache() {
		setCached(true);
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * Initialized the cache.
	 */
	private void initCache() {
		if (cache != null) clearCache();
		cache = app.createGraphics(bounds.width, bounds.height);
	}
	
	private void resizeCache() {
		clearCache();
		cache = app.createGraphics(bounds.width, bounds.height);
	}
	
	private void clearCache() {
		cache = null; // There is no proper dispose() method on PImage.
		cacheContentDirty = false;
		cacheSizeDirty = false;
	}
	
	// *********************************************************************************************
	// Transformation matrix functionality:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * The locally maintained inverse of the transformation-matrix. It is updated each time the
	 * translation, rotation of scale was modified and this node is redrawn.
	 * 
	 * This matrix is used to map coordinates -such as mouse coordinates- from the coordinate system
	 * valid inside this node and the top-level application coordinate system. This matrix is
	 * currently only maintained when this node dispatches mouse-events.
	 */
	protected PMatrix2D inverseTMatrix = new PMatrix2D();
	
	/**
	 * True when the translation, rotation of scale was modified and as a consequence the locally
	 * maintained transformation-matrix is no longer valid.
	 */
	protected boolean inverseTMatrixDirty = false;
	
	/** True when the inverse of the transformation matrix should be maintained for this node. */
	protected boolean maintainInverseTMatrix = false;
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/**
	 * The transformation-matrix in which only the transformations specified on this node are
	 * represented. It is updated each time the translation, rotation of scale was modified and this
	 * node is redrawn.
	 * 
	 * This matrix is used to map the bounds of child-nodes to their parent nodes.
	 */
	protected PMatrix2D localTMatrix = new PMatrix2D();
	
	/* True when the localTMatrix is not valid. */
	private boolean localTMatrixDirty = true;
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/**
	 * Call this method when maintainTransformMatrix is true and some aspect of the transformation
	 * of this node has changed. It flags the locally stored inverse of the transformation matrix as
	 * dirty, so that it is updated during the next draw-traversal.
	 */
	private void invalidateTMatrix() {
		if (maintainInverseTMatrix) {
			inverseTMatrixDirty = true;
			mouseXDirty = true;
			mouseYDirty = true;
			if (hasChildren) for (SGNode child : children) {
				if (child.maintainInverseTMatrix) child.invalidateTMatrix();
				child.invalidateContent();
			}
		}
		if (maintainBounds) localTMatrixDirty = true;
	}
	
	/**
	 * Debug method that prints the transformation matrix.
	 */
	public void printTransformMatrix() {
		System.out.println("- transformMatrix: " + inverseTMatrix.m00 + ", " + inverseTMatrix.m01
				+ ", " + inverseTMatrix.m02 + " / " + inverseTMatrix.m10 + ", "
				+ inverseTMatrix.m11 + ", " + inverseTMatrix.m12);
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @param globalX The x-component of the global coordinate that should be mapped to a local
	 *            coordinate.
	 * @param globalY The y-component of the global coordinate that should be mapped to a local
	 *            coordinate.
	 * @return The x-component of the local coordinate to which the given global coordinate maps.
	 */
	public float getLocalX(float globalX, float globalY) {
		return inverseTMatrix.multX(globalX, globalY);
	}
	
	/**
	 * @param globalX The x-component of the global coordinate that should be mapped to a local
	 *            coordinate.
	 * @param globalY The y-component of the global coordinate that should be mapped to a local
	 *            coordinate.
	 * @return The y-component of the local coordinate to which the given global coordinate maps.
	 */
	public float getLocalY(float globalX, float globalY) {
		return inverseTMatrix.multY(globalX, globalY);
	}
	
	/**
	 * @param globalX The x-component of the global coordinate that should be mapped to a local
	 *            coordinate.
	 * @param globalY The y-component of the global coordinate that should be mapped to a local
	 *            coordinate.
	 * @return The local coordinate to which the given global coordinate maps.
	 */
	public Point2D.Float getLocalCoord(float globalX, float globalY) {
		return new Point2D.Float(inverseTMatrix.multY(globalX, globalY), inverseTMatrix.multY(
				globalX, globalY));
	}
	
	/**
	 * @param globalCoord The global coordinate that should be mapped to a local coordinate.
	 * @return The local coordinate to which the given global coordinate maps.
	 */
	public Point2D.Float getLocalCoord(Point2D.Float globalCoord) {
		return new Point2D.Float(inverseTMatrix.multY(globalCoord.x, globalCoord.y),
				inverseTMatrix.multY(globalCoord.x, globalCoord.y));
	}
	
	// *********************************************************************************************
	// Mouse functionality:
	// ---------------------------------------------------------------------------------------------
	
	/*
	 * True when the mouse-events have been directly enabled by calling the enableMouseEvents()
	 * method. Do not modify this property.
	 */
	private boolean mouseEventsEnabled = false;
	
	/*
	 * True when at least one mouse-event-handler has been registered on this node using
	 * addMouseEventHandler(). Do not modify this property.
	 */
	private boolean mouseHandlerAdded = false;
	
	/*
	 * True when this node needs to dispatch mouse-events, i.e. when either mouseEventsEnabled or
	 * mouseHandlerAdded is true. Do not modify this property.
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
	boolean wantsSysMouseEvents = false;
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/* The collection of registered mouse-event-handlers. */
	private CopyOnWriteArraySet<SGMouseEventHandler> mouseHandlers = new CopyOnWriteArraySet<SGMouseEventHandler>();
	
	/*
	 * True when the mouse-cursor is currently over this node as determined by the mouseHitTest()
	 * implementation.
	 */
	private boolean mouseIsOver = false;
	
	/* True when the mouse was pressed while the cursor was over this node. */
	private boolean mouseWasPressed = false;
	
	/*
	 * Memoises the x-position of the mouse-cursor. This value becomes invalid when the mouse moves.
	 * Therefore only access this property through the getMouseX() method, which updates this value
	 * when needed.
	 */
	private int mouseX;
	
	/*
	 * Memoises the y-position of the mouse-cursor. This value becomes invalid when the mouse moves.
	 * Therefore only access this property through the getMouseY() method, which updates this value
	 * when needed.
	 */
	private int mouseY;
	
	/* True when the mouseX value is no longer valid. */
	private boolean mouseXDirty = false;
	
	/* True when the mouseY value is no longer valid. */
	private boolean mouseYDirty = false;
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/**
	 * Call this method if the mouse-event-handlers on this node need to be called. Note that
	 * mouse-events will only be dispatched for classes that properly implement the mouseHitTest()
	 * method.
	 * 
	 * @see SGNode#mouseHitTest()
	 * @see SGNode#disableMouseEvents()
	 */
	public final void enableMouseEvents() {
		mouseEventsEnabled = true;
		updateMouseFlags();
	}
	
	/**
	 * Call this method if the mouse-event-handlers on this node no longer need to be called.
	 * 
	 * @see SGNode#enableMouseEvents()
	 */
	public final void disableMouseEvents() {
		mouseEventsEnabled = false;
		updateMouseFlags();
	}
	
	/**
	 * This method needs to be implemented in node-classes that should dispatch mouse-events.
	 * 
	 * @return True when the mouse is over this node.
	 * 
	 * @see SGNode#enableMouseEvents()
	 * @see SGNode#addMouseEventHandler(SGMouseEventHandler)
	 */
	protected boolean mouseHitTest() {
		if (!visible) return false;
		if (children.size() == 0) {
			throw new Error("The mouseHitTest() method is not implemented for "
					+ getClass().getName() + ".");
		}
		else {
			for (SGNode child : children)
				if (child.mouseHitTest()) return true;
		}
		return false;
	}
	
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
			mouseHandlerAdded = true;
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
			mouseHandlerAdded = false;
			updateMouseFlags();
		}
	}
	
	private void updateMouseFlags() {
		dispatchMouseEvents = mouseEventsEnabled || mouseHandlerAdded;
		
		if (mouseEventsEnabled || mouseHandlerAdded || forwardSysMouseEvents) {
			wantsSysMouseEvents = true;
			maintainInverseTMatrix = true;
		}
		else {
			wantsSysMouseEvents = false;
			maintainInverseTMatrix = false;
			mouseIsOver = false;
			mouseWasPressed = false;
			mouseXDirty = true;
			mouseYDirty = true;
		}
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	// Mouse-event dispatch helper methods.
	
	private void dispatchMouseClicked() {
		mouseClicked();
		for (SGMouseEventHandler handler : mouseHandlers)
			handler.mouseClicked(this);
	}
	
	private void dispatchMouseOver() {
		mouseOver();
		for (SGMouseEventHandler handler : mouseHandlers)
			handler.mouseOver(this);
	}
	
	private void dispatchMouseOut() {
		mouseOut();
		for (SGMouseEventHandler handler : mouseHandlers)
			handler.mouseOut(this);
	}
	
	private void dispatchMouseMoved() {
		mouseMoved();
		for (SGMouseEventHandler handler : mouseHandlers)
			handler.mouseMoved(this);
	}
	
	private void dispatchMousePressed() {
		mousePressed();
		for (SGMouseEventHandler handler : mouseHandlers)
			handler.mousePressed(this);
	}
	
	private void dispatchMouseReleased() {
		mouseReleased();
		for (SGMouseEventHandler handler : mouseHandlers)
			handler.mouseReleased(this);
	}
	
	private void dispatchMouseDragged() {
		mouseDragged();
		for (SGMouseEventHandler handler : mouseHandlers)
			handler.mouseDragged(this);
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * This method is called when the mouse was clicked while the cursor was over this node as
	 * determined by the implementation of the mouseHitTest() method. Override this method to handle
	 * this mouse-event. Alternatively a mouse-event handler can be registered.
	 * 
	 * @see SGNode#mouseHitTest()
	 * @see SGNode#addMouseEventHandler(SGMouseEventHandler)
	 */
	protected void mouseClicked() {}
	
	/**
	 * This method is called when the cursor moved onto this node as determined by the
	 * implementation of the mouseHitTest() method. Override this method to handle this mouse-event.
	 * Alternatively a mouse-event handler can be registered.
	 * 
	 * @see SGNode#mouseHitTest()
	 * @see SGNode#addMouseEventHandler(SGMouseEventHandler)
	 */
	protected void mouseOver() {}
	
	/**
	 * This method is called when the cursor moved out of this node as determined by the
	 * implementation of the mouseHitTest() method. Override this method to handle this mouse-event.
	 * Alternatively a mouse-event handler can be registered.
	 * 
	 * @see SGNode#mouseHitTest()
	 * @see SGNode#addMouseEventHandler(SGMouseEventHandler)
	 */
	protected void mouseOut() {}
	
	/**
	 * This method is called when the cursor moved over of this node as determined by the
	 * implementation of the mouseHitTest() method. Override this method to handle this mouse-event.
	 * Alternatively a mouse-event handler can be registered.
	 * 
	 * @see SGNode#mouseHitTest()
	 * @see SGNode#addMouseEventHandler(SGMouseEventHandler)
	 */
	protected void mouseMoved() {}
	
	/**
	 * This method is called when the mouse-button was pressed while the cursor was over this nod as
	 * determined by the implementation of the mouseHitTest() method. Override this method to handle
	 * this mouse-event. Alternatively a mouse-event handler can be registered.
	 * 
	 * @see SGNode#mouseHitTest()
	 * @see SGNode#addMouseEventHandler(SGMouseEventHandler)
	 */
	protected void mousePressed() {}
	
	/**
	 * This method is called when the mouse-button was released after it was pressed while the
	 * cursor was over this node as determined by the implementation of the mouseHitTest() method.
	 * Override this method to handle this mouse-event. Alternatively a mouse-event handler can be
	 * registered.
	 * 
	 * @see SGNode#mouseHitTest()
	 * @see SGNode#addMouseEventHandler(SGMouseEventHandler)
	 */
	protected void mouseReleased() {}
	
	/**
	 * TODO
	 */
	protected void mouseDragged() {}
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @return the horizontal position of the mouse-cursor
	 */
	public int getMouseX() {
		if (mouseXDirty) {
			mouseX = (int) Math.floor(getLocalX(app.mouseX, app.mouseY));
			mouseXDirty = false;
		}
		return mouseX;
	}
	
	/**
	 * @return the vertical position of the mouse-cursor
	 */
	public int getMouseY() {
		if (mouseYDirty) {
			mouseY = (int) Math.floor(getLocalY(app.mouseX, app.mouseY));
			mouseYDirty = false;
		}
		return mouseY;
	}
	
	// ---------------------------------------------------------------------------------------------
	// system mouse methods:
	
	/* System method. */
	void mouseClicked_sys() {
		if (dispatchMouseEvents && mouseHitTest()) dispatchMouseClicked();
		if (forwardSysMouseEvents) {
			for (SGNode child : mouseChildren)
				if (child.visible) child.mouseClicked_sys();
		}
	}
	
	/* System method. */
	void mousePressed_sys() {
		if (dispatchMouseEvents && mouseHitTest()) {
			mouseWasPressed = true;
			dispatchMousePressed();
		}
		if (forwardSysMouseEvents) {
			for (SGNode child : mouseChildren) {
				if (child.visible) child.mousePressed_sys();
			}
		}
	}
	
	/* System method. */
	void mouseReleased_sys() {
		if (dispatchMouseEvents && mouseWasPressed) {
			mouseWasPressed = false;
			dispatchMouseReleased();
		}
		if (forwardSysMouseEvents) {
			for (SGNode child : mouseChildren) {
				if (child.visible) child.mouseReleased_sys();
			}
		}
	}
	
	/* System method. */
	void mouseMoved_sys() {
		// println(">> " + this + ".mouseMoved_sys() - mouseXDirty = true");
		if (dispatchMouseEvents) {
			mouseXDirty = true;
			mouseYDirty = true;
			if (mouseHitTest()) {
				if (!mouseIsOver) {
					mouseIsOver = true;
					dispatchMouseOver();
				}
				else dispatchMouseMoved();
			}
			else if (mouseIsOver) {
				mouseIsOver = false;
				dispatchMouseOut();
			}
		}
		if (forwardSysMouseEvents) {
			for (SGNode child : mouseChildren) {
				if (child.visible) child.mouseMoved_sys();
			}
		}
	}
	
	/* System method. */
	void mouseDragged_sys() {
		// println(">> SGNode.mouseDragged_sys()");
		if (dispatchMouseEvents) {
			mouseXDirty = true;
			mouseYDirty = true;
			if (mouseHitTest()) {
				if (!mouseIsOver) {
					mouseIsOver = true;
					dispatchMouseOver();
				}
				else dispatchMouseDragged();
			}
			else if (mouseIsOver) {
				mouseIsOver = false;
				dispatchMouseOut();
			}
		}
		if (forwardSysMouseEvents) {
			for (SGNode child : mouseChildren) {
				if (child.visible) child.mouseDragged_sys();
			}
		}
	}
	
	/* System method. */
	void mouseWheel_sys() {
		// TODO
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * Call this method for each child of this container that wants to receive system-mouse-events.
	 * 
	 * @param node The child-node that wants to receive system-mouse-events.
	 */
	protected void forwardMouseEventTo(SGNode child) {
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
	 * True when the key-events have been directly enabled by calling the enableKeyEvents() method.
	 * Do not modify this property.
	 */
	private boolean keyEventsEnabled = false;
	
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
	boolean wantsSysKeyEvents = false;
	
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
		dispatchKeyEvents = keyEventsEnabled || keyHandlerAdded;
		wantsSysKeyEvents = keyEventsEnabled || keyHandlerAdded || forwardSysKeyEvents;
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/**
	 * Call this method if the key-event-handlers on this node need to be called.
	 * 
	 * @see SGNode#disableKeyEvents()
	 */
	public final void enableKeyEvents() {
		keyEventsEnabled = true;
		updateKeyFlags();
	}
	
	/**
	 * Call this method if the key-event-handlers on this node no longer need to be called.
	 * 
	 * @see SGNode#enableKeyEvents()
	 */
	public final void disableKeyEvents() {
		keyEventsEnabled = false;
		updateKeyFlags();
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	// Key-event dispatch helper methods.
	
	private void dispatchKeyTyped() {
		keyTyped(app.key, app.keyCode);
		for (SGKeyEventHandler handler : keyHandlers)
			handler.keyTyped(this, app.key, app.keyCode);
	}
	
	private void dispatchKeyPressed() {
		keyPressed(app.key, app.keyCode);
		for (SGKeyEventHandler handler : keyHandlers)
			handler.keyPressed(this, app.key, app.keyCode);
	}
	
	private void dispatchKeyReleased() {
		keyReleased(app.key, app.keyCode);
		for (SGKeyEventHandler handler : keyHandlers)
			handler.keyReleased(this, app.key, app.keyCode);
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * Called once every time a key is pressed, but action keys such as Ctrl, Shift, and Alt are
	 * ignored. Override this method to handle this key-event. Alternatively a key-event handler can
	 * be registered.
	 * 
	 * Because of how operating systems handle key repeats, holding down a key will cause multiple
	 * calls to keyTyped(), the rate is set by the operating system and how each computer is
	 * configured.
	 * 
	 * @param key The key that was pressed.
	 * @param keyCode See PApplet.keyCode.
	 * 
	 * @see SGNode#addKeyEventHandler(SGKeyEventHandler)
	 * @see PApplet#keyTyped()
	 * @see PApplet#key
	 * @see PApplet#keyCode
	 */
	protected void keyTyped(char key, int keyCode) {}
	
	/**
	 * Called once every time a key is pressed. The key that was pressed is stored in the key
	 * variable. Override this method to handle this key-event. Alternatively a key-event handler
	 * can be registered.
	 * 
	 * Because of how operating systems handle key repeats, holding down a key may cause multiple
	 * calls to keyPressed() (and keyReleased() as well). The rate of repeat is set by the operating
	 * system and how each computer is configured.
	 * 
	 * @param key The key that was pressed.
	 * @param keyCode See PApplet.keyCode.
	 * 
	 * @see SGNode#addKeyEventHandler(SGKeyEventHandler)
	 * @see PApplet#keyPressed()
	 * @see PApplet#key
	 * @see PApplet#keyCode
	 */
	protected void keyPressed(char key, int keyCode) {}
	
	/**
	 * Called once every time a key is released. The key that was released will be stored in the key
	 * variable. Override this method to handle this key-event. Alternatively a key-event handler
	 * can be registered.
	 * 
	 * @param key The key that was pressed.
	 * @param keyCode See PApplet.keyCode.
	 * 
	 * @see SGNode#addKeyEventHandler(SGKeyEventHandler)
	 * @see PApplet#keyReleased()
	 * @see PApplet#key
	 * @see PApplet#keyCode
	 */
	protected void keyReleased(char key, int keyCode) {}
	
	// ---------------------------------------------------------------------------------------------
	// system key methods:
	
	/* System method. */
	void keyTyped_sys() {
		if (dispatchKeyEvents) dispatchKeyTyped();
		if (forwardSysKeyEvents) {
			for (SGNode child : keyChildren)
				if (child.visible) child.keyTyped_sys();
		}
	}
	
	/* System method. */
	void keyPressed_sys() {
		if (dispatchKeyEvents) dispatchKeyPressed();
		if (forwardSysKeyEvents) {
			for (SGNode child : keyChildren)
				if (child.visible) child.keyPressed_sys();
		}
	}
	
	/* System method. */
	void keyReleased_sys() {
		if (dispatchKeyEvents) dispatchKeyReleased();
		if (forwardSysKeyEvents) {
			for (SGNode child : keyChildren)
				if (child.visible) child.keyReleased_sys();
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
	
	/* Returns a string that represents the data from the given rectangle. */
	private String rectStr(Rectangle r) {
		return "" + r.x + ", " + r.y + ", " + r.width + ", " + r.height;
	}
	
}
