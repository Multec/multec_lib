package be.multec.sg.d2;

import java.awt.Rectangle;
import java.lang.ref.SoftReference;
import java.util.HashMap;

import processing.core.PGraphics;
import processing.core.PImage;
import be.multec.sg.SGApp;
import be.multec.sg.SGNode;

/**
 * A node that draws a bitmap image.
 * 
 * This implementation optimizes the display of scaled and repeated images by performing the scaling
 * or repetition once given the source-image, yielding a target-image which is used to render the
 * bitmap image.
 * 
 * @author Wouter Van den Broeck
 */
public class SGImage extends SGNode {
	
	// *********************************************************************************************
	// Attributes:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * The image modes:
	 * 
	 * <DL>
	 * <DT>SIMPLE</DT>
	 * <DD>The image is drawn unscaled with its top-left corner at the origin (the x/y position) of
	 * this node. The explicitly given width and height are ignored. The width and the height of the
	 * image are taken as the width and height of this node.</DD>
	 * 
	 * <DT>CENTER</DT>
	 * <DD>The image is drawn unscaled with its center at the origin (the x/y position) of this
	 * node. The explicitly given width and height are ignored. The width and the height of the
	 * image are taken as the width and height of this node.</DD>
	 * 
	 * <DT>CROP</DT>
	 * <DD>The image is drawn unscaled with its top-left corner at the origin (the x/y position) of
	 * this node. When the explicitly given width or height are smaller than the respective width or
	 * height of the image then the image is cropped accordingly. The given width and the height are
	 * taken as the width and height of this node. When no explicit width and height are given then
	 * the SIMPLE behavior applies.</DD>
	 * 
	 * <DT>CENTER_CROP</DT>
	 * <DD>The image is drawn unscaled with its center at the origin (the x/y position) of this
	 * node. When the explicitly given width or height are smaller than the respective width or
	 * height of the image then the image is cropped according to the given width and height. The
	 * given width and the height are taken as the width and height of this node. When no explicit
	 * width and height are given then the CENTER behavior applies.</DD>
	 * 
	 * <DT>REPEAT</DT>
	 * <DD>The image is used to fill the area defined by the given width and height. This area is
	 * positioned with its top-left corner at the origin (the x/y position) of this node. The image
	 * is not scaled and is repeated or cropped where necessary to properly fill the given area. The
	 * given width and the height are taken as the width and height of this node. When no explicit
	 * width and height are given then the SIMPLE behavior applies.</DD>
	 * 
	 * <DT>SCALE_FIT</DT>
	 * <DD>The image is drawn with its top-left corner at the origin (the x/y position) of this
	 * node. It is scaled -disproportionately when necessary- to fill the explicitly given width and
	 * height. The given width and the height are taken as the width and height of this node. When
	 * no explicit width and height are given then the SIMPLE behavior applies.</DD>
	 * 
	 * <DT>CENTER_SCALE_FIT</DT>
	 * <DD>The image is drawn with its center at the origin (the x/y position) of this node. It is
	 * scaled -disproportionately when necessary- to fill the explicitly given width and height. The
	 * given width and the height are taken as the width and height of this node. When no explicit
	 * width and height are given then the CENTER behavior applies.</DD>
	 * 
	 * <DT>SCALE_ALL</DT>
	 * <DD>The image is drawn centered in the area defined by the given width and height. This area
	 * is positioned with its top-left corner at the origin (the x/y position) of this node. The
	 * image is scaled proportionately such that the image is fully visible and fills the area as
	 * much as possible. There will thus be equally distributed empty bands at the top/bottom edges
	 * or the left-side/right-side edges when the proportions of this area do not match those of the
	 * image. The given width and the height are taken as the width and height of this node. When no
	 * explicit width and height are given then the SIMPLE behavior applies.</DD>
	 * 
	 * <DT>CENTER_SCALE_ALL</DT>
	 * <DD>The image is drawn centered in the area defined by the given width and height. This area
	 * is positioned with its center at the origin (the x/y position) of this node. The image is
	 * scaled proportionately such that the image is fully visible and fills the area as much as
	 * possible. There will thus be equally distributed empty bands at the top/bottom edges or the
	 * left-side/right-side edges when the proportions of this area do not match those of the image.
	 * The given width and the height are taken as the width and height of this node. When no
	 * explicit width and height are given then the CENTRER behavior applies.</DD>
	 * 
	 * <DT>SCALE_CROP</DT>
	 * <DD>The image is drawn centered in the area defined by the given width and height. This area
	 * is positioned with its top-left corner at the origin (the x/y position) of this node. The
	 * image is scaled proportionately such that the area is fully filled. The image will be cropped
	 * equally at the top/bottom or the left-side/right-side when the proportions of this area do
	 * not match those of the image. The given width and the height are taken as the width and
	 * height of this node. When no explicit width and height are given then the SIMPLE behavior
	 * applies.</DD>
	 * 
	 * 
	 * <DT>CENTER_SCALE_CROP</DT>
	 * <DD>The image is drawn centered in the area defined by the given width and height. This area
	 * is positioned with its center at the origin (the x/y position) of this node. The image is
	 * scaled proportionately such that the area is fully filled. The image will be cropped equally
	 * at the top/bottom or the left-side/right-side when the proportions of this area do not match
	 * those of the image. The given width and the height are taken as the width and height of this
	 * node. When no explicit width and height are given then the CENTER behavior applies.</DD>
	 * </DL>
	 */
	public enum SGImageMode {
		SIMPLE, CENTER, CROP, CENTER_CROP, REPEAT, SCALE_FIT, CENTER_SCALE_FIT, SCALE_ALL, CENTER_SCALE_ALL, SCALE_CROP, CENTER_SCALE_CROP
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/* A cache of PImage objects, mapped by their path. */
	private static HashMap<String, SoftReference<PImage>> images = new HashMap<String, SoftReference<PImage>>();
	
	// ---------------------------------------------------------------------------------------------
	
	/* The original image set by the user. */
	private PImage sourceImg;
	
	/* The image that is actually shown. */
	private PImage targetImg;
	
	/* The image-mode to use when drawing this label. */
	private SGImageMode imageMode = SGImageMode.SIMPLE;
	
	/* The blend-mode to use when drawing this label. */
	private int blendMode = BLEND;
	
	/* The effective width of this node. */
	private int imgWidth = 0;
	
	/* The effective height of this node. */
	private int imgHeight = 0;
	
	/* The explicitly set width. */
	private int explicitWidth = 0;
	
	/* The explicitly set height. */
	private int explicitHeight = 0;
	
	/*
	 * The x-coordinate of the center of this node with respect to the origin (the x/y position) of
	 * this node.
	 */
	private float centerX = 0;
	
	/*
	 * The y-coordinate of the center of this node with respect to the origin (the x/y position) of
	 * this node.
	 */
	private float centerY = 0;
	
	/* True when a tint should be applied when drawing the image. */
	private boolean applyTint = false;
	
	/* The tint to apply when <em>applyTint</em> is true. */
	private int tintRGB = 0xFFFFFF;
	
	/* The tint-alpha to apply when <em>applyTint</em> is true. */
	private float tintAlpha = 0xFF;
	
	/*
	 * Memoised parameters used when drawing the bitmap. These values are updated when needed in the
	 * updateImageParams() method.
	 */
	private int targetX;
	private int targetY;
	private int targetW;
	private int targetH;
	private int sourceX1;
	private int sourceY1;
	private int sourceX2;
	private int sourceY2;
	private int pImageMode;
	
	// *********************************************************************************************
	// Constructors:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @param app The scene-graph application object.
	 * @param path The path of the image file.
	 */
	public SGImage(SGApp app, String path) {
		super(app);
		initImage(path);
	}
	
	/**
	 * @param app The scene-graph application object.
	 * @param path The path of the image file.
	 * @param imageMode The image-mode to use.
	 */
	public SGImage(SGApp app, String path, SGImageMode imageMode) {
		super(app);
		this.imageMode = imageMode;
		initImage(path);
	}
	
	/**
	 * @param app The scene-graph application object.
	 * @param path The path of the image file.
	 * @param width The explicit width of this node. This value is ignored under some image-modes.
	 * @param height The explicit height of this node. This value is ignored under some image-modes.
	 */
	public SGImage(SGApp app, String path, int width, int height) {
		super(app);
		explicitWidth = width;
		explicitHeight = height;
		initImage(path);
	}
	
	/**
	 * @param app The scene-graph application object.
	 * @param path The path of the image file.
	 * @param imageMode The image-mode to use.
	 * @param width The explicit width of this node. This value is ignored under some image-modes.
	 * @param height The explicit height of this node. This value is ignored under some image-modes.
	 */
	public SGImage(SGApp app, String path, SGImageMode imageMode, int width, int height) {
		super(app);
		this.imageMode = imageMode;
		explicitWidth = width;
		explicitHeight = height;
		initImage(path);
	}
	
	/**
	 * @param app The scene-graph application object.
	 * @param img The image
	 */
	public SGImage(SGApp app, PImage img) {
		super(app);
		this.sourceImg = img;
		updateImageParams();
	}
	
	/**
	 * @param app The scene-graph application object.
	 * @param img The image
	 * @param imageMode The image-mode to use.
	 */
	public SGImage(SGApp app, PImage img, SGImageMode imageMode) {
		super(app);
		this.sourceImg = img;
		this.imageMode = imageMode;
		updateImageParams();
	}
	
	/**
	 * @param app The scene-graph application object.
	 * @param img The image
	 * @param width The explicit width of this node. This value is ignored under some image-modes.
	 * @param height The explicit height of this node. This value is ignored under some image-modes.
	 */
	public SGImage(SGApp app, PImage img, int width, int height) {
		super(app);
		this.sourceImg = img;
		explicitWidth = width;
		explicitHeight = height;
		updateImageParams();
	}
	
	/**
	 * @param app The scene-graph application object.
	 * @param img The image
	 * @param imageMode The image-mode to use.
	 * @param width The explicit width of this node. This value is ignored under some image-modes.
	 * @param height The explicit height of this node. This value is ignored under some image-modes.
	 */
	public SGImage(SGApp app, PImage img, SGImageMode imageMode, int width, int height) {
		super(app);
		this.sourceImg = img;
		this.imageMode = imageMode;
		explicitWidth = width;
		explicitHeight = height;
		updateImageParams();
	}
	
	// ---------------------------------------------------------------------------------------------
	
	private void initImage(String path) {
		if (images.containsKey(path)) {
			sourceImg = images.get(path).get();
			if (sourceImg == null) {
				// This happens when the soft-referenced PImage was deleted by the garbage
				// collector.
				images.remove(path);
			}
		}
		if (sourceImg == null) {
			sourceImg = app.loadImage(path);
			images.put(path, new SoftReference<PImage>(sourceImg));
		}
		updateImageParams();
	}
	
	// ---------------------------------------------------------------------------------------------
	
	@Override
	public void dispose(boolean traverse) {
		sourceImg = null;
		targetImg = null;
		
		super.dispose(traverse);
	}
	
	// *********************************************************************************************
	// Accessors:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @param explicitWidth The explicitly set width of this node. The effective width depends on
	 *            the image-mode.
	 */
	public void setWidth(int explicitWidth) {
		if (this.explicitWidth == explicitWidth) return;
		this.explicitWidth = explicitWidth;
		updateImageParams();
		redraw();
	}
	
	/**
	 * @param explicitHeight The explicitly set height of this node. The effective height depends on
	 *            the image-mode.
	 */
	public void setHeight(int explicitHeight) {
		if (this.explicitHeight == explicitHeight) return;
		this.explicitHeight = explicitHeight;
		updateImageParams();
		redraw();
	}
	
	/**
	 * @return The current image-mode.
	 */
	public SGImageMode getImageMode() {
		return imageMode;
	}
	
	/**
	 * @param imageMode The image-mode to use.
	 * @return This SGLabel object, so that this method can be chained.
	 */
	public SGImage setImageMode(SGImageMode imageMode) {
		if (this.imageMode == imageMode) return this;
		imageMode = imageMode;
		updateImageParams();
		redraw();
		return this;
	}
	
	/**
	 * @return The image.
	 */
	public PImage getImage() {
		return sourceImg;
	}
	
	/**
	 * @param image The image to use.
	 * @return This SGLabel object, so that this method can be chained.
	 */
	public SGImage setImage(PImage image) {
		if (this.sourceImg == image) return this;
		this.sourceImg = image;
		updateImageParams();
		redraw();
		return this;
	}
	
	public SGImage noTint() {
		if (!applyTint) return this;
		applyTint = false;
		redraw();
		return this;
	}
	
	public SGImage setTint(int rgb, float alpha) {
		if (tintRGB == rgb && tintAlpha == alpha) return this;
		tintRGB = rgb;
		tintAlpha = alpha;
		applyTint = true;
		redraw();
		return this;
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/**
	 * @return The currently applicable Processing blend mode.
	 */
	public int getBlendMode() {
		return blendMode;
	}
	
	/**
	 * @param blendMode The Processing blend mode to use when drawing this image.
	 * @return This SGImage object, so that this method can be chained.
	 */
	public SGImage setBlendMode(int blendMode) {
		if (this.blendMode == blendMode) return this;
		this.blendMode = blendMode;
		redraw();
		return this;
	}
	
	// *********************************************************************************************
	// SGNode Methods:
	// ---------------------------------------------------------------------------------------------
	
	/* @see be.multec.sg.d2.SG2DNode#draw(processing.core.PGraphics) */
	@Override
	protected void draw(PGraphics g) {
		if (targetImg == null) {
			System.err.println("The image is not available.");
			return;
		}
		
		g.blendMode(blendMode);
		
		if (applyTint) g.tint(tintRGB, tintAlpha);
		else g.noTint();
		
		g.imageMode(pImageMode);
		
		// println("# SGImage.draw() - imageMode: " + imageMode.toString() + " pImageMode: "
		// + pImageMode);
		// println(" - source (x1, y1, x2, y2): " + sourceX1 + ", " + sourceY1 + ", " + sourceX2
		// + ", " + sourceY2);
		// println(" - target (x, y, w, h): " + targetX + ", " + targetY + ", " + targetW + ", "
		// + targetH);
		// println(" - targetImg (w, h): " + targetImg.width + ", " + targetImg.height);
		
		g.image(targetImg, targetX, targetY, targetW, targetH, sourceX1, sourceY1, sourceX2,
				sourceY2);
	}
	
	/* @see be.multec.sg.SGNode#updateLocalBounds(java.awt.Rectangle) */
	@Override
	protected void updateLocalBounds(Rectangle bounds) {
		switch (imageMode) {
			case SIMPLE:
			case CROP:
			case REPEAT:
			case SCALE_FIT:
			case SCALE_ALL:
			case SCALE_CROP:
				bounds.x = targetX;
				bounds.y = targetY;
				break;
			
			default:
				bounds.x = -targetW / 2;
				bounds.y = -targetH / 2;
		}
		bounds.width = targetW;
		bounds.height = targetH;
	}
	
	/* @see be.multec.sg.SGNode#mouseHitTest() */
	@Override
	protected boolean contains(float x, float y) {
		if (imgWidth == 0 || imgHeight == 0) return false;
		return x >= 0 && x < imgWidth && y >= 0 && y < imgHeight;
	}
	
	// *********************************************************************************************
	// Local support methods:
	// ---------------------------------------------------------------------------------------------
	
	private void updateImageParams() {
		
		// update the width & height values:
		switch (imageMode) {
			case SIMPLE:
			case CENTER:
				if (sourceImg != null) {
					imgWidth = sourceImg.width;
					imgHeight = sourceImg.height;
				}
				else {
					imgWidth = explicitWidth;
					imgHeight = explicitHeight;
				}
				break;
			
			default:
				if (explicitWidth > 0) imgWidth = explicitWidth;
				else if (sourceImg != null) imgWidth = sourceImg.width;
				else imgWidth = 0;
				
				if (explicitHeight > 0) imgHeight = explicitHeight;
				else if (sourceImg != null) imgHeight = sourceImg.height;
				else imgHeight = 0;
				
				break;
		}
		
		// set pImageMode, centerX & centerY:
		switch (imageMode) {
			case SIMPLE:
			case CROP:
			case REPEAT:
			case SCALE_FIT:
			case SCALE_ALL:
			case SCALE_CROP:
				pImageMode = CORNER;
				centerX = imgWidth / 2;
				centerY = imgHeight / 2;
				break;
			
			default:
				pImageMode = CENTER;
				centerX = 0;
				centerY = 0;
		}
		
		// Set targetX, targetY, targetW, targetH, sourceX1, sourceY1, sourceX2 & sourceY2. There
		// values are used in the app.image() instruction in draw().
		targetX = 0;
		targetY = 0;
		sourceX1 = 0;
		sourceY1 = 0;
		targetW = sourceX2 = imgWidth;
		targetH = sourceY2 = imgHeight;
		float scaleX, scaleY;
		switch (imageMode) {
			case SIMPLE:
			case CENTER:
				targetImg = sourceImg;
				break;
			
			case CROP:
				targetImg = sourceImg;
				if (targetImg != null) {
					targetW = sourceX2 = Math.min(imgWidth, targetImg.width);
					targetH = sourceY2 = Math.min(imgHeight, targetImg.height);
				}
				break;
			
			case CENTER_CROP:
				targetImg = sourceImg;
				if (targetImg != null) {
					if (imgWidth < targetImg.width) {
						sourceX1 = (targetImg.width - imgWidth) / 2;
						sourceX2 = sourceX1 + imgWidth;
					}
					else targetW = sourceX2 = targetImg.width;
					if (imgHeight < targetImg.height) {
						sourceY1 = (targetImg.height - imgHeight) / 2;
						sourceY2 = sourceY1 + imgHeight;
					}
					else targetH = sourceY2 = targetImg.height;
				}
				break;
			
			case REPEAT:
				// Requires multiple draw instructions in draw() which do not use the predefined
				// values.
				targetImg = app.createImage(imgWidth, imgHeight, ARGB);
				int imgW = sourceImg.width;
				int imgH = sourceImg.height;
				int cntX = imgWidth / imgW;
				int restX = imgWidth % imgW;
				int cntY = imgHeight / imgH;
				int restY = imgHeight % imgH;
				int iX = 0;
				int iY = 0;
				for (iX = 0; iX < cntX; iX++) {
					for (iY = 0; iY < cntY; iY++)
						targetImg.copy(sourceImg, 0, 0, imgW, imgH, iX * imgW, iY * imgH, imgW,
								imgH);
				}
				if (restX > 0) {
					for (iY = 0; iY < cntY; iY++)
						targetImg.copy(sourceImg, 0, 0, restX, imgH, iX * imgW, iY * imgH, restX,
								imgH);
				}
				if (restY > 0) {
					for (iX = 0; iX < cntX; iX++)
						targetImg.copy(sourceImg, 0, 0, imgW, restY, iX * imgW, iY * imgH, imgW,
								restY);
				}
				if (restX > 0 && restY > 0) {
					targetImg.copy(sourceImg, 0, 0, restX, restY, iX * imgW, iY * imgH, restX,
							restY);
				}
				break;
			
			case SCALE_FIT:
			case CENTER_SCALE_FIT:
				targetImg = app.createImage(imgWidth, imgHeight, ARGB);
				targetImg.copy(sourceImg, 0, 0, sourceImg.width, sourceImg.height, 0, 0, imgWidth,
						imgHeight);
				break;
			
			case SCALE_ALL:
				scaleX = imgWidth / sourceImg.width;
				scaleY = imgHeight / sourceImg.height;
				if (scaleX < scaleY) {
					targetH = sourceY2 = (int) Math.round(sourceImg.height * scaleX);
					targetY = (int) (imgHeight - targetH) / 2;
				}
				else if (scaleX > scaleY) {
					targetW = sourceX2 = (int) Math.round(sourceImg.width * scaleY);
					targetX = (int) (imgWidth - targetW) / 2;
				}
				targetImg = app.createImage(targetW, targetH, ARGB);
				targetImg.copy(sourceImg, 0, 0, sourceImg.width, sourceImg.height, 0, 0, targetW,
						targetH);
				break;
			
			case CENTER_SCALE_ALL:
				scaleX = imgWidth / sourceImg.width;
				scaleY = imgHeight / sourceImg.height;
				if (scaleX < scaleY) {
					targetH = sourceY2 = (int) Math.round(sourceImg.height * scaleX);
				}
				else if (scaleX > scaleY) {
					targetW = sourceX2 = (int) Math.round(sourceImg.width * scaleY);
				}
				targetImg = app.createImage(targetW, targetH, ARGB);
				targetImg.copy(sourceImg, 0, 0, sourceImg.width, sourceImg.height, 0, 0, targetW,
						targetH);
				break;
			
			case SCALE_CROP:
			case CENTER_SCALE_CROP:
				targetImg = app.createImage(imgWidth, imgHeight, ARGB);
				scaleX = imgWidth / sourceImg.width;
				scaleY = imgHeight / sourceImg.height;
				if (scaleX == scaleY) {
					targetImg.copy(sourceImg, 0, 0, sourceImg.width, sourceImg.height, 0, 0,
							imgWidth, imgHeight);
				}
				else if (scaleX < scaleY) { // scaleY > scaleX
					int cw = (int) Math.round(imgWidth / scaleY);
					int cx = (sourceImg.width - cw) / 2;
					targetImg.copy(sourceImg, cx, 0, cw, sourceImg.height, 0, 0, imgWidth,
							imgHeight);
				}
				else {
					int ch = (int) Math.round(imgHeight / scaleX);
					int cy = (sourceImg.height - ch) / 2;
					targetImg
							.copy(sourceImg, 0, cy, sourceImg.width, ch, 0, 0, imgWidth, imgHeight);
				}
				break;
		}
	}
	
}
