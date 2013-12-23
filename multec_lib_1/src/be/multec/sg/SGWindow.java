package be.multec.sg;

import java.awt.Color;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.CopyOnWriteArraySet;

import processing.core.PConstants;

/**
 * Base class for windowed scene-graph applications.
 * 
 * <h2>Application setup</h2>
 * 
 * To create a single-window application, create a class that extends this base-class. Override the
 * <code>setupSG</code> method in this new class. The implementation of this method should
 * initialize the scene-graph by adding one or more scene-graph-nodes to the stage. See the
 * documentation of the <code>SGApp</code> class for more details.
 * 
 * To start the application and open the window, create an new object of your custom class, and call
 * one of the <code>open</code> or <code>openFullscreen</code> methods.
 * 
 * The following is an example of a minimal single-application class:
 * 
 * <pre>
 * {@code
 * import be.multec.sg.SGWindow;
 * import be.multec.sg.SGLabel;
 * 
 * public class HelloWorld extends SGWindow {
 *   
 *   public static void main(String[] args) {
 *     new HelloWorld().open("Hello World", 50, 30, 300, 300, 0xFFCC00);
 *   }
 *   
 *   @Override
 *   public void setupSG() {
 *     addChild(new SGLabel(this, "Hello world"), 50, 70);
 *   }
 * }
 * }
 * </pre>
 * 
 * The <code>draw</code> method in this base class will draw the background if it is set. If you
 * want to draw stuff before calling the <code>super.draw()</code> implementation then the
 * backgroundColor should be null.
 * 
 * @author Wouter Van den Broeck
 */
public class SGWindow extends SGApp {
	
	// *********************************************************************************************
	// Attributes:
	// ---------------------------------------------------------------------------------------------
	
	/** The default horizontal position of the window. */
	public static int defaultWindowX = 50;
	
	/** The default vertical position of the window. */
	public static int defaultWindowY = 30;
	
	// ---------------------------------------------------------------------------------------------
	// application state:
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/* The title of this application. This title is also shown in the header of the window. */
	private String title;
	
	/* The underlying Java AWT Frame object, which is the window in which the app is opened. */
	private Frame frame;
	
	/*
	 * The width of the window content. In the default implementation the scene-graph will occupy
	 * the complete window content.
	 */
	private int contentWidth;
	
	/*
	 * The height of the window content. In the default implementation the scene-graph will occupy
	 * the complete window content.
	 */
	private int contentHeight;
	
	/* The x-position of the stage (which holds the scene-graph content). */
	private int stageX = 0;
	
	/* The y-position of the stage (which holds the scene-graph content). */
	private int stageY = 0;
	
	private boolean resizable = false;
	
	/*
	 * The background color.
	 */
	private Color backgroundColor = null;
	
	private boolean undecorated = false;
	
	/* The renderer. @default PConstants#JAVA2D */
	private String renderer = JAVA2D;
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	// application state:
	
	// True when the application was started.
	private boolean started = false;
	
	// True when the application window is being closed.
	private boolean closing = false;
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	// system properties:
	
	/*
	 * True when the frame should be disposed when the app is disposed. This value is false when the
	 * shutdown-hook set in the constructor is triggered, in which case the frame is already
	 * disposed or is about to be disposed.
	 */
	private boolean disposeFrame = false;
	
	// *********************************************************************************************
	// Constructors:
	// ---------------------------------------------------------------------------------------------
	
	/** Basic constructor. */
	public SGWindow() {
		super();
		
		final SGWindow app = this;
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				// println(">> " + app.getClassName() + " shutdownHook - closing: " + closing);
				if (app.closing) return;
				try {
					disposeFrame = false;
					// app.windowClosed();
					// app.dispatchWindowClosed();
					app.dispose();
				}
				catch (Exception exception) {
					System.err.println("! ERROR in shutdownHook for " + app.getClassName() + ": "
							+ exception.getMessage());
				}
			}
		});
	}
	
	/**
	 * Do not call this method. If you want to to close the window, call the <code>close()</code>
	 * method. Override this method to dispose of elements when the window is closed, but don't
	 * forget to call <code>super.dispose()</code>.
	 * 
	 * @see close()
	 * @see processing.core.PApplet#dispose()
	 */
	@Override
	public void dispose() {
		synchronized (this) {
			// println(">> " + getClassName() + ".dispose() - closing: " + closing
			// + ", disposeFrame: " + disposeFrame);
			if (closing) return;
			closing = true;
		}
		super.dispose();
		if (disposeFrame) frame.dispose();
	}
	
	/** Close the application. */
	public final void close() {
		dispose();
	}
	
	// *********************************************************************************************
	// Setters to use before opening the application:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * Use this method before opening the window, i.e. before calling the open() or openFullscreen()
	 * method. The default renderer in Processing 2 is JAVA2D, a pure software renderer. You can set
	 * one of the following alternative render contexts:
	 * <ul>
	 * <li>PConstants.JAVA2D: software renderer (the default)</li>
	 * <li>PConstants.P2D: hardware accelerated 2D (see http://processing.org/tutorials/p3d/)</li>
	 * </ul>
	 * 
	 * The P3D renderer cannot be used because 3D content is currently not supported in this
	 * framework.
	 * 
	 * @param renderer the renderer to set.
	 * 
	 * @see PConstants#JAVA2D
	 * @see PConstants#P2D
	 */
	public void setRenderer(String renderer) {
		if (started) throw new Error("Set the renderer before opening the application window.");
		if (renderer == PConstants.JAVA2D || renderer == PConstants.P2D) this.renderer = renderer;
		else throw new Error("Invalid renderer '" + renderer + "'. Must be either JAVA2D or P2D.");
	}
	
	/**
	 * @return The renderer set before opening the window.
	 */
	public String getRenderer() {
		return renderer;
	}
	
	// *********************************************************************************************
	// Methods to open a regular window:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * Opens the application in a normal window on the primary display.
	 * 
	 * @param title The title to show in the window header.
	 * @param contentW The width of the window content.
	 * @param contentH The height of the windows content.
	 * @param bgColor the background color of the window
	 */
	public void open(String title, int contentW, int contentH, Color bgColor) {
		started = true;
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice display = ge.getScreenDevices()[0];
		GraphicsConfiguration gc = display.getDefaultConfiguration();
		this.contentWidth = contentW;
		this.contentHeight = contentH;
		setBackground(bgColor);
		openFrame(gc, defaultWindowX, defaultWindowY, title);
	}
	
	/**
	 * Opens the application in a normal window on the primary display.
	 * 
	 * @param title The title to show in the window header.
	 * @param x The horizontal position of the window relative to the origin of the default display.
	 * @param y The vertical position of the window relative to the origin of the default display.
	 * @param contentW The width of the window content.
	 * @param contentH The height of the windows content.
	 * @param bgColor the background color of the window
	 */
	public void open(String title, int x, int y, int contentW, int contentH, Color bgColor) {
		started = true;
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice display = ge.getScreenDevices()[0];
		GraphicsConfiguration gc = display.getDefaultConfiguration();
		this.contentWidth = contentW;
		this.contentHeight = contentH;
		setBackground(bgColor);
		openFrame(gc, x, y, title);
	}
	
	/**
	 * Opens the application in a normal window on the display with the given index.
	 * 
	 * @param title The title to show in the window header.
	 * @param contentW The width of the window content.
	 * @param contentH The height of the windows content.
	 * @param bgColor the background color of the window
	 * @param displayIndex The index of the display, the primary display has index 0, the second
	 *            display when present has index 1, etc.
	 */
	public void open(String title, int contentW, int contentH, int displayIndex, Color bgColor) {
		started = true;
		GraphicsDevice display = getDisplay(displayIndex);
		GraphicsConfiguration gc = display.getDefaultConfiguration();
		this.contentWidth = contentW;
		this.contentHeight = contentH;
		setBackground(bgColor);
		openFrame(gc, defaultWindowX, defaultWindowY, title);
	}
	
	/**
	 * Opens the application in a normal window on the display with the given index.
	 * 
	 * @param title The title to show in the window header.
	 * @param x The horizontal position of the window relative to the origin of the default display.
	 * @param y The vertical position of the window relative to the origin of the default display.
	 * @param contentW The width of the window content.
	 * @param contentH The height of the windows content.
	 * @param displayIndex The index of the display, the primary display has index 0, the second
	 *            display when present has index 1, etc.
	 * @param bgColor the background color of the window
	 */
	public void open(String title, int x, int y, int contentW, int contentH, int displayIndex,
			Color bgColor)
	{
		started = true;
		GraphicsDevice display = getDisplay(displayIndex);
		GraphicsConfiguration gc = display.getDefaultConfiguration();
		this.contentWidth = contentW;
		this.contentHeight = contentH;
		setBackground(bgColor);
		openFrame(gc, x, y, title);
	}
	
	/**
	 * Opens the application in a normal window on the given display.
	 * 
	 * @param title The title to show in the window header.
	 * @param contentW The width of the window content.
	 * @param contentH The height of the windows content.
	 * @param display the representation of the display device on which to show the window
	 * @param bgColor the background color of the window
	 */
	public void open(String title, int contentW, int contentH, GraphicsDevice display, Color bgColor)
	{
		started = true;
		GraphicsConfiguration gc = display.getDefaultConfiguration();
		this.contentWidth = contentW;
		this.contentHeight = contentH;
		setBackground(bgColor);
		openFrame(gc, defaultWindowX, defaultWindowY, title);
	}
	
	/**
	 * Opens the application in a normal window on the given display.
	 * 
	 * @param title The title to show in the window header.
	 * @param x The horizontal position of the window relative to the origin of the default display.
	 * @param y The vertical position of the window relative to the origin of the default display.
	 * @param contentW The width of the window content.
	 * @param contentH The height of the windows content.
	 * @param display the representation of the display device on which to show the window
	 * @param bgColor the background color of the window
	 */
	public void open(String title, int x, int y, int contentW, int contentH,
			GraphicsDevice display, Color bgColor)
	{
		started = true;
		GraphicsConfiguration gc = display.getDefaultConfiguration();
		this.contentWidth = contentW;
		this.contentHeight = contentH;
		setBackground(bgColor);
		openFrame(gc, x, y, title);
	}
	
	// *********************************************************************************************
	// Methods to open a fullscreen window:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * Opens the application in a fullscreen window on the primary display. The content width and
	 * height will be set to the width and the height of the display.
	 * 
	 * @param bgColor the background color of the window
	 */
	public void openFullscreen(Color bgColor) {
		started = true;
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice display = ge.getScreenDevices()[0];
		GraphicsConfiguration gc = display.getDefaultConfiguration();
		initFullscreenStage(gc);
		setBackground(bgColor);
		openFullscreenFrame(gc);
	}
	
	/**
	 * Opens the application in a fullscreen window on the display with the given index. The content
	 * width and height will be set to the width and the height of the display.
	 * 
	 * @param bgColor the background color of the window
	 * @param displayIndex the index of the display, the primary display has index 0, the second
	 *            display when present has index 1, etc.
	 */
	public void openFullscreen(Color bgColor, int displayIndex) {
		started = true;
		GraphicsDevice display = getDisplay(displayIndex);
		GraphicsConfiguration gc = display.getDefaultConfiguration();
		initFullscreenStage(gc);
		setBackground(bgColor);
		openFullscreenFrame(gc);
	}
	
	/**
	 * Opens the application in a fullscreen window on the given display. The content width and
	 * height will be set to the width and the height of the display.
	 * 
	 * @param bgColor the background color of the window
	 * @param display the representation of the display device on which to show the window
	 */
	public void openFullscreen(Color bgColor, GraphicsDevice display) {
		started = true;
		GraphicsConfiguration gc = display.getDefaultConfiguration();
		initFullscreenStage(gc);
		setBackground(bgColor);
		openFullscreenFrame(gc);
	}
	
	/**
	 * Opens the application in a fullscreen window on the primary display. The content will be
	 * centered in the display.
	 * 
	 * @param contentW the width of the window content
	 * @param contentH the height of the windows content
	 * @param bgColor the background color of the window
	 */
	public void openFullscreen(int contentW, int contentH, Color bgColor) {
		started = true;
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice display = ge.getScreenDevices()[0];
		GraphicsConfiguration gc = display.getDefaultConfiguration();
		initFullscreenStage(gc);
		setBackground(bgColor);
		openFullscreenFrame(gc);
	}
	
	/**
	 * Opens the application in a fullscreen window on the display with the given index. The content
	 * will be centered in the display.
	 * 
	 * @param contentW the width of the window content
	 * @param contentH the height of the windows content
	 * @param bgColor the background color of the window
	 * @param displayIndex the index of the display, the primary display has index 0, the second
	 *            display when present has index 1, etc.
	 */
	public void openFullscreen(int contentW, int contentH, Color bgColor, int displayIndex) {
		started = true;
		GraphicsDevice display = getDisplay(displayIndex);
		GraphicsConfiguration gc = display.getDefaultConfiguration();
		initStage(gc, contentW, contentH);
		setBackground(bgColor);
		openFullscreenFrame(gc);
		
	}
	
	/**
	 * Opens the application in a fullscreen window on the given display. The content will be
	 * centered in the display.
	 * 
	 * @param contentW the width of the window content
	 * @param contentH the height of the windows content
	 * @param bgColor the background color of the window
	 * @param display the representation of the display device on which to show the window
	 */
	public void openFullscreen(int contentW, int contentH, Color bgColor, GraphicsDevice display) {
		started = true;
		GraphicsConfiguration gc = display.getDefaultConfiguration();
		initStage(gc, contentW, contentH);
		setBackground(bgColor);
		openFullscreenFrame(gc);
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * Adds the app (which inherits from PApplet) in the frame. This method is called after the
	 * window was opened. In this default implementation the app will occupy the available space.
	 * This method can be overridden to embed the scene-graph in a composition with other
	 * AWT-components.
	 */
	protected void addSceneGraph(Frame frame, SGWindow app) {
		frame.add(app);
	}
	
	// *********************************************************************************************
	// Private support methods for opening the window:
	// ---------------------------------------------------------------------------------------------
	
	private void initStage(GraphicsConfiguration gc, int contentWidth, int contentHeight) {
		contentWidth = contentWidth;
		contentHeight = contentHeight;
		if (gc.getBounds().width != contentWidth)
			stageX = (gc.getBounds().width - contentWidth) / 2;
		if (gc.getBounds().height != contentHeight)
			stageY = (gc.getBounds().height - contentHeight) / 2;
		getStage().moveTo(stageX, stageY);
	}
	
	private void initFullscreenStage(GraphicsConfiguration gc) {
		contentWidth = gc.getBounds().width;
		contentHeight = gc.getBounds().height;
	}
	
	private void openFullscreenFrame(GraphicsConfiguration gc) {
		initFrame(gc);
		frame.setUndecorated(true);
		frame.setResizable(false);
		frameInitialized(frame);
		gc.getDevice().setFullScreenWindow(frame);
		
		addSceneGraph(frame, this);
		
		init();
	}
	
	private void openFrame(GraphicsConfiguration gc, int x, int y, String title) {
		initFrame(gc);
		frame.setUndecorated(undecorated);
		if (!undecorated) frame.setTitle(title);
		frame.setResizable(false);
		frame.setBounds(x, y, contentWidth, contentHeight);
		frameInitialized(frame);
		frame.pack();
		frame.setVisible(true);
		frame.setBounds(x, y, contentWidth, contentHeight + frame.getInsets().top);
		
		addSceneGraph(frame, this);
		
		init();
		
		smooth(8);
	}
	
	private void initFrame(GraphicsConfiguration gc) {
		final SGWindow app = this; // reference to the app for use in the event handlers
		frame = new Frame(gc);
		disposeFrame = true;
		frame.addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowOpened(WindowEvent e) {
				// println(">> " + app.getClassName() + " -> windowOpened()");
				app.windowOpened();
				app.dispatchWindowOpened();
			}
			
			@Override
			public void windowActivated(WindowEvent event) {
				// println(">> " + app.getClassName() + " -> windowActivated()");
				app.windowActivated();
				app.dispatchWindowActivated();
			}
			
			@Override
			public void windowDeactivated(WindowEvent e) {
				// println(">> " + app.getClassName() + " -> windowDeactivated()");
				app.windowDeactivated();
				app.dispatchWindowDeactivated();
			}
			
			@Override
			public void windowIconified(WindowEvent e) {
				// println(">> " + app.getClassName() + " -> windowIconified()");
				app.windowIconified();
				app.dispatchWindowIconified();
			}
			
			@Override
			public void windowDeiconified(WindowEvent e) {
				// println(">> " + app.getClassName() + " -> windowDeiconified()");
				app.windowDeiconified();
				app.dispatchWindowDeiconified();
			}
			
			@Override
			public void windowClosing(WindowEvent event) {
				// println(">> " + app.getClassName() + " -> windowClosing() - closing: " +
				// closing);
				boolean close = app.windowClosing();
				app.dispatchWindowClosing();
				if (close) app.dispose();
			}
			
			@Override
			public void windowClosed(WindowEvent e) {
				// println(">> " + app.getClassName() + " -> windowClosed()");
				app.windowClosed();
				app.dispatchWindowClosed();
				app.handlers.clear();
			}
		});
	}
	
	// *********************************************************************************************
	// Window delegate methods:
	// ---------------------------------------------------------------------------------------------
	// TODO: consider implementing additional Window delegate methods
	
	/**
	 * Gets the title of the window. The title is displayed in the window border.
	 * 
	 * @return the title of this window, an empty string ("") if this window doesn't have a title,
	 *         or null if the window is not available.
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * Sets the title for the window to the specified string.
	 * 
	 * @param title the title to be displayed in the window border. A null value is treated as an
	 *            empty string, "".
	 */
	public void setTitle(String title) {
		this.title = title;
		if (frame != null) frame.setTitle(title);
	}
	
	/**
	 * Indicates whether this frame is resizable by the user. By default, all frames are initially
	 * resizable.
	 * 
	 * @return true if the user can resize this frame; false otherwise.
	 */
	public boolean isResizable() {
		return resizable;
	}
	
	/**
	 * Sets whether this frame is resizable by the user.
	 * 
	 * @param resizable true if this frame is resizable; false otherwise.
	 */
	public void setResizable(boolean resizable) {
		if (this.resizable == resizable) return;
		this.resizable = resizable;
		if (frame != null) frame.setResizable(resizable);
	}
	
	/**
	 * Set this value before opening the window. Setting this value to true can be used to mimic a
	 * fullscreen window while opening a regular window.
	 * 
	 * @param undecorated
	 * 
	 * @see Frame#setUndecorated(boolean)
	 */
	public void setUndecorated(boolean undecorated) {
		if (this.undecorated == undecorated) return;
		this.undecorated = undecorated;
		if (frame != null) frame.setUndecorated(undecorated);
	}
	
	// *********************************************************************************************
	// Private support methods:
	// ---------------------------------------------------------------------------------------------
	
	/* Checks if the given display index is correct. */
	private GraphicsDevice getDisplay(int displayIndex) {
		if (displayIndex < 0)
			throw new RuntimeException("The given displayIndex " + displayIndex
					+ " should be at least 0.");
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice devices[] = ge.getScreenDevices();
		if (displayIndex >= devices.length)
			throw new RuntimeException("The given displayIndex " + displayIndex
					+ " can be at most " + (devices.length - 1));
		return devices[displayIndex];
	}
	
	// *********************************************************************************************
	// Window event-handling:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * Called when the underlying Java AWT Frame object has been initialized but before it will be
	 * made visible. This method can be overridden in order to apply frame configurations that must
	 * be applied before the frame is made visible.
	 * 
	 * @param frame the underlying Java AWT Frame object
	 */
	protected void frameInitialized(Frame frame) {}
	
	/**
	 * Called the first time a window is made visible, before the application is started, i.e.
	 * before the setup() method is called. This method can be overridden.
	 */
	protected void windowOpened() {}
	
	/**
	 * Called when the window becomes the active window.
	 */
	protected void windowActivated() {}
	
	/**
	 * Called when the window is no longer the active window.
	 */
	protected void windowDeactivated() {}
	
	/**
	 * Called when the window is changed from a normal to a minimized state. For many platforms, a
	 * minimized window is displayed as the icon specified in the window iconImage property.
	 * 
	 * TODO iconImage?
	 */
	protected void windowIconified() {}
	
	/**
	 * Called when the window is changed from a minimized to a normal state.
	 */
	protected void windowDeiconified() {}
	
	/**
	 * Called when the user attempts to close the window from the window close button or from the
	 * window system menu.
	 * 
	 * @return true if the window can be effectively closed; or false when the user's attempt should
	 *         be ignored.
	 */
	protected boolean windowClosing() {
		return true;
	}
	
	/**
	 * Called when the window was closed.
	 */
	protected void windowClosed() {}
	
	// *********************************************************************************************
	// Application event-handling:
	// ---------------------------------------------------------------------------------------------
	
	private CopyOnWriteArraySet<SGWindowEventHandler> handlers = new CopyOnWriteArraySet<SGWindowEventHandler>();
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/**
	 * Adds an event handler.
	 * 
	 * @param handler The handler to add.
	 */
	public void addAppEventHandler(SGWindowEventHandler handler) {
		handlers.add(handler);
	}
	
	/**
	 * Removes an event handler.
	 * 
	 * @param handler The handler to remove.
	 */
	public void removeAppEventHandler(SGWindowEventHandler handler) {
		handlers.remove(handler);
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	private void dispatchFrameInitialized() {
		for (SGWindowEventHandler handler : handlers)
			handler.frameInitialized(frame);
	}
	
	private void dispatchWindowOpened() {
		for (SGWindowEventHandler handler : handlers)
			handler.windowOpened(this);
	}
	
	private void dispatchWindowActivated() {
		for (SGWindowEventHandler handler : handlers)
			handler.windowActivated(this);
	}
	
	private void dispatchWindowDeactivated() {
		for (SGWindowEventHandler handler : handlers)
			handler.windowDeactivated(this);
	}
	
	private void dispatchWindowIconified() {
		for (SGWindowEventHandler handler : handlers)
			handler.windowIconified(this);
	}
	
	private void dispatchWindowDeiconified() {
		for (SGWindowEventHandler handler : handlers)
			handler.windowDeiconified(this);
	}
	
	private void dispatchWindowClosing() {
		for (SGWindowEventHandler handler : handlers)
			handler.windowClosing(this);
	}
	
	private void dispatchWindowClosed() {
		for (SGWindowEventHandler handler : handlers)
			handler.windowClosed(this);
	}
	
	// *********************************************************************************************
	// PApplet methods:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * This method should be called by overriding methods.
	 * 
	 * @see processing.core.SGApp#setup()
	 */
	@Override
	public void setup() {
		if (closing) return;
		size(contentWidth, contentHeight, renderer);
		super.setup();
	}
	
	/**
	 * This method should be called by overriding methods.
	 * 
	 * @see be.multec.sg.SGApp#draw()
	 */
	@Override
	public void draw() {
		if (closing) return;
		super.draw();
	}
	
	/**
	 * Called by the Processing event system right before draw is called.
	 */
	public void pre() {
		background(backgroundColor.getRGB(), backgroundColor.getAlpha());
	}
	
	// ---------------------------------------------------------------------------------------------
	// Key event handlers:
	
	@Override
	public void keyTyped(KeyEvent e) {
		if (closing) return;
		super.keyTyped(e);
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		if (closing) return;
		super.keyPressed(e);
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		if (closing) return;
		super.keyReleased(e);
	}
	
	// *********************************************************************************************
	// Accessors:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @return True when the application was started.
	 */
	public boolean started() {
		return started;
	}
	
	/** @return The underlying Java AWT Frame object. */
	public Frame getFrame() {
		return frame;
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
		if (color != null) {
			registerMethod("pre", this);
		}
		else {
			unregisterMethod("pre", this);
		}
		redraw();
	}
	
	/**
	 * @return the frameContentWidth
	 */
	public int getContentWidth() {
		return contentWidth;
	}
	
	/**
	 * @return the frameContentHeight
	 */
	public int getContentHeight() {
		return contentHeight;
	}
	
	/**
	 * @return the stageX
	 */
	public int getStageX() {
		return stageX;
	}
	
	/**
	 * @param stageX the stageX to set
	 */
	public void setStageX(int stageX) {
		this.stageX = stageX;
	}
	
	// *********************************************************************************************
	// Other methods:
	// ---------------------------------------------------------------------------------------------
	
	@Override
	public String toString() {
		return "SGWindow '" + title + "'";
	}
	
}
