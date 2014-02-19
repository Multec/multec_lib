package be.multec.sg;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
import be.multec.sg.eventHandlers.SGWindowEventHandler;

/**
 * Base class for windowed scene-graph applications.
 * 
 * <h2>Application setup</h2>
 * 
 * To create a single-window application, create a class that extends this base-class. Override the
 * <code>setup</code> method in this new class. The implementation of this method should initialize
 * the scene-graph by adding one or more scene-graph-nodes to the stage. See the documentation of
 * the <code>SGApp</code> class for more details.
 * 
 * To start the application and open the window, create an new object of your custom class, and call
 * one of the <code>open</code> or <code>openFullscreen</code> methods.
 * 
 * <h3>Window configuration</h3>
 * 
 * Certain configurations of the window need to be set before opening the window. These are listed
 * below.
 * 
 * <dl>
 * 
 * <dt>The Processing renderer</dt>
 * <dd>See setRenderer(String).</dd>
 * 
 * <dt>The window decoration</dt>
 * <dd>See setUndecorated(boolean).</dd>
 * 
 * <dt>The fullscreen-mode</dt>
 * <dd>See setFullscreenMode(FullscreenMode).</dd>
 * 
 * <dt>Window resizability</dt>
 * <dd>See setResizable(boolean).</dd>
 * 
 * </dl>
 * 
 * <h3>Application setup</h3>
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
 *     addNode(new SGLabel(this, "Hello world"), 50, 70);
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
	
	/** The default position of the window. */
	public static Point DEFAULT_FRAME_POSITION = new Point(50, 30);
	
	// ---------------------------------------------------------------------------------------------
	// Logging
	
	public static boolean logToFile = false;
	
	public static final Logger logger = Logger.getLogger("SGWindow");
	
	private FileHandler loggerFH = null;
	
	// ---------------------------------------------------------------------------------------------
	// Application state:
	
	/* The title of this application. This title is also shown in the header of the window. */
	private String title;
	
	/* The underlying Java AWT Frame object, which is the window in which the app is opened. */
	private Frame frame;
	
	/* True when the application was started. */
	private boolean started = false;
	
	/**
	 * @return True when the application was started.
	 */
	public boolean started() {
		return started;
	}
	
	/* True when the window was opened in exclusive fullscreen mode. */
	private boolean exclusiveFullscreenActive = false;
	
	/* The display, set when the window was opened in exclusive fullscreen mode. */
	private GraphicsDevice exclusiveFullscreenDisplay = null;
	
	// *********************************************************************************************
	// Properties to set before opening the windowed application:
	// ---------------------------------------------------------------------------------------------
	
	/* The renderer. @default PConstants#JAVA2D */
	private String renderer = JAVA2D;
	
	/**
	 * Use this method before opening the window, i.e. before calling the open() or openFullscreen()
	 * method. The default renderer in Processing 2 is JAVA2D, a pure software renderer. You can set
	 * one of the following alternative render contexts:
	 * <ul>
	 * <li>PConstants.JAVA2D: software renderer (the default)</li>
	 * <li>PConstants.P2D: hardware accelerated 2D (see http://processing.org/tutorials/p3d/)</li>
	 * </ul>
	 * 
	 * <strong>Notes</strong>
	 * <ul>
	 * <li>The P3D renderer cannot be used because 3D content is currently not supported in this
	 * framework.</li>
	 * <li>It is currently not possible to have two fullscreen windows when using the P2D renderer.</li>
	 * </ul>
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
	
	// ---------------------------------------------------------------------------------------------
	
	/* True when this window should not be decorated. */
	private boolean undecorated = false;
	
	/**
	 * Specify whether the window should be decorated. This setting is ignored when opening a
	 * fullscreen window. Fullscreen windows are always undecorated.
	 * 
	 * @param undecorated True when this window should not be decorated.
	 */
	public void setUndecorated(boolean undecorated) {
		this.undecorated = undecorated;
	}
	
	/**
	 * @return True when this window will not be decorated.
	 */
	public boolean isUndecorated() {
		return undecorated;
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/* True when this window is to be resizable. */
	private boolean resizable = false;
	
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
	 * Indicates whether this frame is resizable by the user. By default, all frames are initially
	 * resizable.
	 * 
	 * @return true if the user can resize this frame; false otherwise.
	 */
	public boolean isResizable() {
		return resizable;
	}
	
	// ---------------------------------------------------------------------------------------------
	
	public static enum FullscreenMode {
		
		/**
		 * Fullscreen mode in which regular windows without decoration are used.
		 * 
		 * @default
		 */
		FULLSCREEN_WINDOWED,
		
		/**
		 * Fullscreen mode in which the windowing system is suspended so that drawing can be done
		 * directly to the screen. This mode is not available on all systems.
		 * 
		 * This mode cannot be used when multiple fullscreen windows on multiple displays are
		 * needed.
		 * 
		 * For more information, see
		 * http://docs.oracle.com/javase/tutorial/extra/fullscreen/exclusivemode.html
		 */
		FULLSCREEN_EXCLUSIVE
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	private static FullscreenMode fullscreenMode = FullscreenMode.FULLSCREEN_WINDOWED;
	
	/**
	 * @param mode The fullscreenMode to use, either FullscreenMode.FULLSCREEN_WINDOWED or
	 *            FullscreenMode.FULLSCREEN_EXCLUSIVE.
	 */
	public static void setFullscreenMode(FullscreenMode mode) {
		if (fullscreenMode == mode) return;
		fullscreenMode = mode;
	}
	
	/**
	 * @return The currently set fullscreen mode.
	 */
	public static FullscreenMode getFullscreenMode() {
		return fullscreenMode;
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * When true then the application is terminated when the window closes.
	 */
	public boolean exitOnClose = true;
	
	// *********************************************************************************************
	// Constructors and closing:
	// ---------------------------------------------------------------------------------------------
	
	/** Basic constructor. */
	public SGWindow() {
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
				// close();
			}
		});
		
		final SGWindow app = this;
		
		// Runtime.getRuntime().addShutdownHook(new Thread() {
		// @Override
		// public void run() {
		// System.out.println(">> ShutdownHook triggered");
		// }
		// });
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/** Close the application. */
	public final void close() {
		// println("SGWindow.close()");
		dispose();
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/* Disposal synchronization lock. */
	private final Object disposeLock = new Object();
	
	/* True when the disposal process is already running. */
	private boolean disposing = false;
	
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
		// System.err.println("SGWindow.dispose()");
		
		synchronized (disposeLock) {
			if (disposing) return;
			disposing = true;
		}
		
		super.dispose();
		
		if (exclusiveFullscreenActive) {
			try {
				exclusiveFullscreenDisplay.setFullScreenWindow(null);
			}
			catch (Throwable e) {
				logger.log(Level.SEVERE, "Failed to stop the exclusive fullscreen mode." + e, e);
			}
		}
		
		if (frame != null) {
			try {
				frame.dispose();
			}
			catch (Throwable e) {
				System.err.println("> s5 error" + e);
			}
		}
		
		if (loggerFH != null) {
			try {
				loggerFH.flush();
				loggerFH.close();
			}
			catch (Throwable e) {
				System.err.println("Failed to flush or close the log file." + e);
				e.printStackTrace(System.err);
			}
		}
	}
	
	// *********************************************************************************************
	// Methods to open a regular window:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * Opens the application in a normal window on the primary display.
	 * 
	 * @param title The title to show in the window header.
	 * @param contentWidth The width of the window content.
	 * @param contentHeight The height of the windows content.
	 * @param bgColor the background color of the window
	 */
	public void open(String title, int contentWidth, int contentHeight, Color bgColor) {
		open_sys(getDefaultDisplay(), contentWidth, contentHeight, DEFAULT_FRAME_POSITION, title,
				bgColor);
	}
	
	/**
	 * Opens the application in a normal window on the primary display.
	 * 
	 * @param title The title to show in the window header.
	 * @param frameX The horizontal position of the top-left corner corner of the window relative to
	 *            the top-left corner of the display.
	 * @param frameY The vertical position of the top-left corner corner of the window relative to
	 *            the top-left corner of the display.
	 * @param contentWidth The width of the window content.
	 * @param contentHeight The height of the windows content.
	 * @param bgColor the background color of the window
	 */
	public void open(String title, int frameX, int frameY, int contentWidth, int contentHeight,
			Color bgColor)
	{
		open_sys(getDefaultDisplay(), contentWidth, contentHeight, new Point(frameX, frameY),
				title, bgColor);
	}
	
	/**
	 * Opens the application in a normal window on the display with the given index.
	 * 
	 * @param title The title to show in the window header.
	 * @param contentWidth The width of the window content.
	 * @param contentHeight The height of the windows content.
	 * @param bgColor the background color of the window
	 * @param displayIndex The index of the display, the primary display has index 0, the second
	 *            display when present has index 1, etc.
	 */
	public void open(String title, int contentWidth, int contentHeight, int displayIndex,
			Color bgColor)
	{
		open_sys(getDisplay(displayIndex), contentWidth, contentHeight, DEFAULT_FRAME_POSITION,
				title, bgColor);
	}
	
	/**
	 * Opens the application in a normal window on the display with the given index.
	 * 
	 * @param title The title to show in the window header.
	 * @param frameX The horizontal position of the top-left corner corner of the window relative to
	 *            the top-left corner of the display.
	 * @param frameY The vertical position of the top-left corner corner of the window relative to
	 *            the top-left corner of the display.
	 * @param contentWidth The width of the window content.
	 * @param contentHeight The height of the windows content.
	 * @param displayIndex The index of the display, the primary display has index 0, the second
	 *            display when present has index 1, etc.
	 * @param bgColor the background color of the window
	 */
	public void open(String title, int frameX, int frameY, int contentWidth, int contentHeight,
			int displayIndex, Color bgColor)
	{
		open_sys(getDisplay(displayIndex), contentWidth, contentHeight, new Point(frameX, frameY),
				title, bgColor);
	}
	
	/**
	 * Opens the application in a normal window on the given display.
	 * 
	 * @param title The title to show in the window header.
	 * @param contentWidth The width of the window content.
	 * @param contentHeight The height of the windows content.
	 * @param display the representation of the display device on which to show the window
	 * @param bgColor the background color of the window
	 */
	public void open(String title, int contentWidth, int contentHeight, GraphicsDevice display,
			Color bgColor)
	{
		open_sys(display, contentWidth, contentHeight, DEFAULT_FRAME_POSITION, title, bgColor);
	}
	
	/**
	 * Opens the application in a normal window on the given display.
	 * 
	 * @param title The title to show in the window header.
	 * @param frameX The horizontal position of the top-left corner corner of the window relative to
	 *            the top-left corner of the display.
	 * @param frameY The vertical position of the top-left corner corner of the window relative to
	 *            the top-left corner of the display.
	 * @param contentWidth The width of the window content.
	 * @param contentHeight The height of the windows content.
	 * @param display the representation of the display device on which to show the window
	 * @param bgColor the background color of the window
	 */
	public void open(String title, int frameX, int frameY, int contentWidth, int contentHeight,
			GraphicsDevice display, Color bgColor)
	{
		open_sys(display, contentWidth, contentHeight, new Point(frameX, frameY), title, bgColor);
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	private void open_sys(GraphicsDevice display, int contentWidth, int contentHeight,
			Point framePosition, String title, Color bgColor)
	{
		started = true;
		setBackground(bgColor);
		
		// Initialize the frame:
		GraphicsConfiguration gc = display.getDefaultConfiguration();
		Rectangle displayBounds = gc.getBounds();
		initFrame(gc);
		frame.setUndecorated(undecorated);
		frame.setResizable(resizable);
		frame.setTitle(title); // if (!undecorated)
		frame.setLocation(displayBounds.x + framePosition.x, displayBounds.y + framePosition.y);
		
		// Allow developers to perform extra actions on the frame.
		frameInitialized(frame);
		
		// Initialize the Processing (PApplet) component:
		setSize(contentWidth, contentHeight);
		setPreferredSize(new Dimension(contentWidth, contentHeight));
		
		// Initialize the Processing PApplet component:
		init();
		
		// Add the Processing component in the frame and open the frame.
		frame.add(this);
		frame.pack();
		frame.setVisible(true);
	}
	
	// *********************************************************************************************
	// Fullscreen window:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * Opens the application in a fullscreen window on the given display. The content will be
	 * centered in the display.
	 * 
	 * @param display the display device on which to show the window
	 * @param contentWidth the size of the Processing content
	 * @param contentHeight the size of the Processing content
	 * @param bgColor the background color of the window
	 */
	/**
	 * @param display
	 * @param bgColor
	 */
	public void openFullscreen(GraphicsDevice display, int contentWidth, int contentHeight,
			Color bgColor)
	{
		openFullscreen_sys(display, contentWidth, contentHeight, bgColor);
	}
	
	/**
	 * @param displayIndex the index of the display, the primary display has index 0, the second
	 *            display when present has index 1, etc.
	 * @param contentWidth the size of the Processing content
	 * @param contentHeight the size of the Processing content
	 * @param bgColor the background color of the window
	 */
	public void openFullscreen(int displayIndex, int contentWidth, int contentHeight, Color bgColor)
	{
		openFullscreen_sys(getDisplay(displayIndex), contentWidth, contentHeight, bgColor);
	}
	
	/**
	 * Opens the application in a fullscreen window on the given display. The content width and
	 * height will be set to the width and the height of the display.
	 * 
	 * @param display the display device on which to show the window
	 * @param bgColor the background color of the window
	 */
	public void openFullscreen(GraphicsDevice display, Color bgColor) {
		Rectangle frameBounds = display.getDefaultConfiguration().getBounds();
		openFullscreen_sys(getDefaultDisplay(), frameBounds.width, frameBounds.height, bgColor);
	}
	
	/**
	 * Opens the application in a fullscreen window on the display with the given index. The content
	 * width and height will be set to the width and the height of the display.
	 * 
	 * @param displayIndex the index of the display, the primary display has index 0, the second
	 *            display when present has index 1, etc.
	 * @param bgColor the background color of the window
	 */
	public void openFullscreen(int displayIndex, Color bgColor) {
		GraphicsDevice display = getDisplay(displayIndex);
		Rectangle frameBounds = display.getDefaultConfiguration().getBounds();
		openFullscreen_sys(getDefaultDisplay(), frameBounds.width, frameBounds.height, bgColor);
	}
	
	/**
	 * Opens the application in a fullscreen window on the primary display. The content will be
	 * centered in the display.
	 * 
	 * @param contentWidth the size of the Processing content
	 * @param contentHeight the size of the Processing content
	 * @param bgColor the background color of the window
	 */
	public void openFullscreen(int contentWidth, int contentHeight, Color bgColor) {
		openFullscreen_sys(getDefaultDisplay(), contentWidth, contentHeight, bgColor);
	}
	
	/**
	 * Opens the application in a fullscreen window on the primary display. The content width and
	 * height will be set to the width and the height of the display.
	 * 
	 * @param bgColor the background color of the window
	 */
	public void openFullscreen(Color bgColor) {
		GraphicsDevice display = getDefaultDisplay();
		Rectangle frameBounds = display.getDefaultConfiguration().getBounds();
		openFullscreen_sys(getDefaultDisplay(), frameBounds.width, frameBounds.height, bgColor);
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	private void openFullscreen_sys(GraphicsDevice display, int contentWidth, int contentHeight,
			Color bgColor)
	{
		started = true;
		setBackground(bgColor);
		
		if (fullscreenMode == FullscreenMode.FULLSCREEN_EXCLUSIVE
				&& !display.isFullScreenSupported())
		{
			logger.warning("The exclusive fullscreen mode is not supported. Using windowed"
					+ " fullscreen mode instead.");
			fullscreenMode = FullscreenMode.FULLSCREEN_WINDOWED;
		}
		
		// Set the stage position when the size of the content does not correspond with the size
		// of the frame:
		Rectangle frameBounds = display.getDefaultConfiguration().getBounds();
		if (frameBounds.width != contentWidth || frameBounds.height != contentHeight) {
			getStage().moveTo((frameBounds.width - contentWidth) / 2,
					(frameBounds.height - contentHeight) / 2);
		}
		
		// Initialize the frame:
		initFrame(display.getDefaultConfiguration());
		frame.setUndecorated(true);
		frame.setResizable(false);
		frame.setLocation(frameBounds.x, frameBounds.y);
		
		// Allow developers to perform extra actions on the frame.
		frameInitialized(frame);
		dispatchFrameInitialized();
		
		// Initialize the Processing (PApplet) component:
		setSize(contentWidth, contentHeight);
		setPreferredSize(new Dimension(contentWidth, contentHeight));
		
		// Initialize the Processing PApplet component:
		init();
		
		// Add the Processing component in the frame and open the frame.
		frame.add(this);
		
		// Open the frame:
		if (fullscreenMode == FullscreenMode.FULLSCREEN_WINDOWED) {
			frame.pack();
			frame.setBounds(display.getDefaultConfiguration().getBounds());
			frame.setVisible(true);
		}
		else if (fullscreenMode == FullscreenMode.FULLSCREEN_EXCLUSIVE) {
			exclusiveFullscreenActive = true;
			exclusiveFullscreenDisplay = display;
			display.setFullScreenWindow(frame);
		}
	}
	
	/**
	 * A Processing mouse event handler. This handler is installed in debugging mode. It closes the
	 * application when the user clicks in the top-left corner of a full-screen window.
	 * 
	 * @see be.multec.sg.SGApp#mouseEvent(processing.event.MouseEvent)
	 */
	@Override
	public void mouseEvent(MouseEvent event) {
		if (DEBUG_MODE && event.getAction() == MouseEvent.CLICK && event.getX() < 30
				&& event.getY() < 30) close();
		else super.mouseEvent(event);
	}
	
	/**
	 * Handler of Processing key events.
	 * 
	 * @param event
	 */
	@Override
	public void keyEvent(KeyEvent event) {
		if (DEBUG_MODE && event.getAction() == KeyEvent.TYPE && event.getKey() == 'q'
				&& (event.isControlDown() || event.isMetaDown() || event.isAltDown()))
		{
			// System.err.println("! Q typed");
			close();
		}
		else super.keyEvent(event);
	}
	
	// *********************************************************************************************
	// Shared screen functionality:
	// ---------------------------------------------------------------------------------------------
	
	/** @return The underlying Java AWT Frame object. */
	public Frame getFrame() {
		return frame;
	}
	
	public static void printDisplaysInfo() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		System.out.println("\nDisplays info:");
		int displayCnt = ge.getScreenDevices().length;
		System.out.println("- There are " + displayCnt + " displays.");
		for (int i = 0; i < displayCnt; i++) {
			GraphicsDevice display = ge.getScreenDevices()[i];
			GraphicsConfiguration gc = display.getDefaultConfiguration();
			System.out.println("- Display " + i + ":");
			printBounds("  - bounds: ", gc.getBounds());
			System.out.println("  - isFullScreenSupported: " + display.isFullScreenSupported());
		}
		System.out.println("- PApplet.useQuartz: " + PApplet.useQuartz);
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	private void initFrame(GraphicsConfiguration gc) {
		final SGWindow app = this; // reference to the app for use in the event handlers
		frame = new Frame(gc);
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
				// println(">> " + app.getClassName() + " -> windowClosing()");
				boolean close = app.windowClosing();
				app.dispatchWindowClosing();
				if (close) app.dispose();
			}
			
			@Override
			public void windowClosed(WindowEvent e) {
				// println(">> " + app.getClassName() + " -> windowClosed()");
				app.dispatchWindowClosed();
				app.handlers.clear();
				app.windowClosed();
				if (exitOnClose) System.exit(0);
			}
		});
	}
	
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
	
	private GraphicsDevice getDefaultDisplay() {
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
	}
	
	// *********************************************************************************************
	// Window accessors and delegate methods:
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
	
	/* @see processing.core.PApplet#sketchRenderer() */
	@Override
	public String sketchRenderer() {
		return renderer;
	}
	
	// *********************************************************************************************
	// Other methods:
	// ---------------------------------------------------------------------------------------------
	
	@Override
	public String toString() {
		return "SGWindow '" + title + "'";
	}
	
}
