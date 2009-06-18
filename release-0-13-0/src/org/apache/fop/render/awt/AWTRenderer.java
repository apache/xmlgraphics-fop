

package org.apache.fop.render.awt;

/*
  originally contributed by
  Juergen Verwohlt: Juergen.Verwohlt@af-software.de,
  Rainer Steinkuhle: Rainer.Steinkuhle@af-software.de,
  Stanislav Gorkhover: Stanislav.Gorkhover@af-software.de
 */

import org.apache.fop.layout.*;
import org.apache.fop.datatypes.*;
import org.apache.fop.image.*;
import org.apache.fop.svg.*;
import org.apache.fop.render.pdf.*;
import org.apache.fop.viewer.*;
import org.apache.fop.apps.*;
import org.apache.fop.render.Renderer;

import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.awt.font.*;
import java.util.*;
import java.io.*;
import java.beans.*;
import javax.swing.*;
import java.awt.print.*;
import java.awt.image.BufferedImage;


public class AWTRenderer implements Renderer, Printable, Pageable {

    protected int pageWidth    = 0;
    protected int pageHeight   = 0;
    protected double scaleFactor = 100.0;
    protected int pageNumber = 0;
    protected AreaTree tree;
    protected ProgressListener progressListener = null;
    protected Translator res = null;
    
    protected Hashtable fontNames = new Hashtable();
    protected Hashtable fontStyles = new Hashtable();
    protected Color saveColor;

    // Key - Font name, Value - java Font name.
    protected static Hashtable JAVA_FONT_NAMES;

    /**
     * Image Object and Graphics Object. The Graphics Object is the Graphics
     * object that is contained withing the Image Object.
     */
    private BufferedImage pageImage = null;
    private Graphics2D graphics = null;
    
    /**
     * The current (internal) font name
     */
    protected String currentFontName;

    /**
     * The current font size in millipoints
     */
    protected int currentFontSize;

    /**
     * The current colour's red, green and blue component
     */
    protected float currentRed = 0;
    protected float currentGreen = 0;
    protected float currentBlue = 0;

    /**
     * The current vertical position in millipoints from bottom
     */
    protected int currentYPosition = 0;

    /**
     * The current horizontal position in millipoints from left
     */
    protected int currentXPosition = 0;

    /**
     * The horizontal position of the current area container
     */
    private int currentAreaContainerXPosition = 0;

    static {
	JAVA_FONT_NAMES = new Hashtable();
	JAVA_FONT_NAMES.put("Times", "serif");
	JAVA_FONT_NAMES.put("Times-Roman", "serif");
	JAVA_FONT_NAMES.put("Courier", "monospaced");
	JAVA_FONT_NAMES.put("Helvetica", "sansserif");
	// JAVA_FONT_NAMES.put("Serif", "sansserif");
    }

    public AWTRenderer(Translator aRes) {
	res = aRes;
    }

    public int getPageNumber() {
	return pageNumber;
    }

    public void setPageNumber(int aValue) {
	pageNumber = aValue;
    }

    public void setScaleFactor(double newScaleFactor) {
	scaleFactor = newScaleFactor;
    }

    public double getScaleFactor() {
	return scaleFactor;
    }

    public BufferedImage getLastRenderedPage() {
	return pageImage;
    }

    /**
     * add a line to the current stream
     *
     * @param x1 the start x location in millipoints
     * @param y1 the start y location in millipoints
     * @param x2 the end x location in millipoints
     * @param y2 the end y location in millipoints
     * @param th the thickness in millipoints
     * @param r the red component
     * @param g the green component
     * @param b the blue component
     */
    protected void addLine(int x1, int y1, int x2, int y2, int th,
			   float r, float g, float b) {
	graphics.setColor(new Color (r,g,b));
	graphics.drawLine((int)(x1/1000f), pageHeight - (int)(y1/1000f),
			  (int)(x2/1000f), pageHeight - (int)(y2/1000f));
    }


    /**
     * draw a filled rectangle
     *
     * @param x the x position of left edge in millipoints
     * @param y the y position of top edge in millipoints
     * @param w the width in millipoints
     * @param h the height in millipoints
     * @param r the red component
     * @param g the green component
     * @param b the blue component
     */
    protected void addRect(int x, int y, int w, int h,
			   float r, float g, float b) {
	graphics.setColor(new Color (r,g,b));
	graphics.fill3DRect((int) (x/1000f), pageHeight - (int) (y/1000f),
			    (int) (w/1000f), -(int) (h/1000f),false);
    }

    /**
     * draw a filled rectangle
     *
     * @param x the x position of left edge in millipoints
     * @param y the y position of top edge in millipoints
     * @param w the width in millipoints
     * @param h the height in millipoints
     * @param r the red component of edges
     * @param g the green component of edges
     * @param b the blue component of edges
     * @param fr the red component of the fill
     * @param fg the green component of the fill
     * @param fb the blue component of the fill
     */
    protected void addRect(int x, int y, int w, int h,
			   float r, float g, float b,
			   float fr, float fg, float fb) {
	graphics.setColor(new Color (r,g,b));
	graphics.fill3DRect((int) (x/1000f), pageHeight - (int) (y/1000f),
			    (int) (w/1000f), -(int) (h/1000f),true);
    }

    /**
     * Vor dem Druck einzustellen:
     *
     * Seite/Seiten wählen
     * Zoomfaktor
     * Seitenformat  / Quer- oder Hoch
     **/
    public void transform(Graphics2D g2d, double zoomPercent, double angle) {
	AffineTransform at = g2d.getTransform();
	at.rotate(angle);
	at.scale(zoomPercent/100.0, zoomPercent/100.0);
	g2d.setTransform(at);
    }

    protected void drawFrame() {

	int width  = pageWidth;
	int height = pageHeight;

	graphics.setColor(Color.white);
	graphics.fillRect(0, 0, width, height);
	graphics.setColor(Color.black);
	graphics.drawRect(-1, -1, width+2, height+2);
	graphics.drawLine(width+2, 0, width+2, height+2);
	graphics.drawLine(width+3, 1, width+3, height+3);

	graphics.drawLine(0, height+2, width+2, height+2);
	graphics.drawLine(1, height+3, width+3, height+3);
    }

    /**
     * Retrieve the number of pages in this document.
     *
     * @return the number of pages
     */
    public int getPageCount()
    {
	if (tree == null) {
	    return 0;
	}
	
	return tree.getPages().size();
    }

    public void render(int aPageNumber) {
	if (tree != null) {
	    try {
		render(tree, aPageNumber);
	    } catch (IOException e) {
		// This exception can't occur because we are not dealing with
		// any files.
	    }
	}
    }
	
    public void render(AreaTree areaTree, PrintWriter writer)
	throws IOException {
	tree = areaTree;
	render(areaTree,0);
    }
    
    public void render(AreaTree areaTree, int aPageNumber)
	throws IOException {
	tree = areaTree;
	Page page = (Page)areaTree.getPages().elementAt(aPageNumber);
	
	pageWidth  = (int)((float)page.getWidth() / 1000f);
	pageHeight = (int)((float)page.getHeight() / 1000f);

	
	pageImage = new BufferedImage((int)((pageWidth * (int)scaleFactor)/100),
				      (int)((pageHeight * (int)scaleFactor)/100),
				      BufferedImage.TYPE_INT_RGB);

	graphics = pageImage.createGraphics();
	
	transform(graphics, scaleFactor, 0);
	drawFrame();

	renderPage(page);
    }

    public void renderPage(Page page) {
	AreaContainer body, before, after;

	body = page.getBody();
	before = page.getBefore();
	after = page.getAfter();

	this.currentFontName = "";
	this.currentFontSize = 0;

	renderAreaContainer(body);

	if (before != null) {
	    renderAreaContainer(before);
	}

	if (after != null) {
	    renderAreaContainer(after);
	}
    }

    public void renderAreaContainer(AreaContainer area) {

	int saveY = this.currentYPosition;
	int saveX = this.currentAreaContainerXPosition;
	
	if (area.getPosition() ==
	    org.apache.fop.fo.properties.Position.ABSOLUTE) {
	    // Y position is computed assuming positive Y axis, adjust
	    //for negative postscript one
	    this.currentYPosition = area.getYPosition() -
		2 * area.getPaddingTop() -
		2 * area.borderWidthTop;
	    this.currentAreaContainerXPosition = area.getXPosition();
	} else if (area.getPosition() ==
		   org.apache.fop.fo.properties.Position.RELATIVE) {
	    this.currentYPosition -= area.getYPosition();
	    this.currentAreaContainerXPosition += area.getXPosition();
	} else if (area.getPosition() ==
		   org.apache.fop.fo.properties.Position.STATIC) {
	    this.currentYPosition -= area.getPaddingTop() + area.borderWidthTop;
	    this.currentAreaContainerXPosition += area.getPaddingLeft() +
		area.borderWidthLeft;
	}

	doFrame(area);

	Enumeration e = area.getChildren().elements();
	while (e.hasMoreElements()) {
	    org.apache.fop.layout.Box b =
		(org.apache.fop.layout.Box) e.nextElement();
	    b.render(this);
	}
	
	if (area.getPosition() !=
	    org.apache.fop.fo.properties.Position.STATIC) {
	    this.currentYPosition = saveY;
	    this.currentAreaContainerXPosition = saveX;
	} else {
	    this.currentYPosition -= area.getHeight();
	}
    }

    private void doFrame(org.apache.fop.layout.Area area) {
	int w, h;
	int rx = this.currentAreaContainerXPosition;
	w = area.getContentWidth();

	if (area instanceof BlockArea) {
	    rx += ((BlockArea)area).getStartIndent();
	}
	
	h = area.getContentHeight();
	int ry = this.currentYPosition;
	ColorType bg = area.getBackgroundColor();

	rx = rx - area.getPaddingLeft();
	ry = ry + area.getPaddingTop();
	w = w + area.getPaddingLeft() + area.getPaddingRight();
	h = h + area.getPaddingTop() + area.getPaddingBottom();

	// I'm not sure I should have to check for bg being null
	// but I do
	if ((bg != null) && (bg.alpha() == 0)) {
	    this.addRect(rx, ry, w, -h,
			 bg.red(), bg.green(), bg.blue(),
			 bg.red(), bg.green(), bg.blue());
	}

	rx = rx - area.borderWidthLeft;
	ry = ry + area.borderWidthTop;
	w = w + area.borderWidthLeft + area.borderWidthRight;
	h = h + area.borderWidthTop + area.borderWidthBottom;

	if (area.borderWidthTop != 0) {
	    addLine(rx, ry, rx + w, ry,
		    area.borderWidthTop,
		    area.borderColorTop.red(), area.borderColorTop.green(),
		    area.borderColorTop.blue());
	}

	if (area.borderWidthLeft != 0) {
	    addLine(rx, ry, rx, ry - h,
		    area.borderWidthLeft,
		    area.borderColorLeft.red(), area.borderColorLeft.green(),
		    area.borderColorLeft.blue());
	}

	if (area.borderWidthRight != 0) {
	    addLine(rx + w, ry, rx + w, ry - h,
		    area.borderWidthRight,
		    area.borderColorRight.red(), area.borderColorRight.green(),
		    area.borderColorRight.blue());
	}

	if (area.borderWidthBottom != 0) {
	    addLine(rx, ry - h, rx + w, ry - h,
		    area.borderWidthBottom,
		    area.borderColorBottom.red(), area.borderColorBottom.green(),
		    area.borderColorBottom.blue());
	}
    }



    protected Rectangle2D getBounds(org.apache.fop.layout.Area a) {
	return new Rectangle2D.Double(currentAreaContainerXPosition,
				      currentYPosition,
				      a.getAllocationWidth(),
				      a.getHeight());
    }

    public void renderBlockArea(BlockArea area) {
	doFrame(area);
	Enumeration e = area.getChildren().elements();
	while (e.hasMoreElements()) {
	    org.apache.fop.layout.Box b =
		(org.apache.fop.layout.Box) e.nextElement();
	    b.render(this);
	}
    }

    public void setupFontInfo(FontInfo fontInfo) {
	FontSetup.setup(fontInfo);
	Hashtable hash = fontInfo.getFonts();
	org.apache.fop.render.pdf.Font f;
	String name;
	Object key;
	int fontStyle;

	for (Enumeration e = hash.keys(); e.hasMoreElements();) {
	    fontStyle = java.awt.Font.PLAIN;
	    key = e.nextElement();
	    f = (org.apache.fop.render.pdf.Font)hash.get(key);
	    name = f.fontName();

	    if (name.toUpperCase().indexOf("BOLD") > 0) {
		fontStyle += java.awt.Font.BOLD;
	    }
	    if (name.toUpperCase().indexOf("ITALIC") > 0 ||
		name.toUpperCase().indexOf("OBLIQUE") > 0) {
		fontStyle += java.awt.Font.ITALIC;
	    }
      
	    int hyphenIndex = name.indexOf("-");
      
	    hyphenIndex = (hyphenIndex < 0) ? name.length() : hyphenIndex;
	    fontNames.put(key, name.substring(0, hyphenIndex));
	    fontStyles.put(key, new Integer(fontStyle));
	}
    
    }
    
    public void renderDisplaySpace(DisplaySpace space) {
	int d = space.getSize();
	this.currentYPosition -= d;
    }
    
    
    public void renderImageArea(ImageArea area) {
	int x = this.currentAreaContainerXPosition +
	    area.getXOffset();
	int y = this.currentYPosition;
	int w = area.getContentWidth();
	int h = area.getHeight();
	
	FopImage img = area.getImage();
	
	if (img == null) {
	    System.out.println("area.getImage() is null");
	}
	
	int[] map = img.getimagemap();
	
	String path = img.gethref();
	
	ImageIcon icon = new ImageIcon(path);
	Image imgage = icon.getImage();
	
	graphics.drawImage(imgage, currentXPosition / 1000,
			   pageHeight - y / 1000,
			   img.getWidth() / 1000,
			   img.getHeight() / 1000,
			   null);
	
	currentYPosition -= h;
    }

    public void renderInlineArea(InlineArea area) {
	char ch;
	StringBuffer pdf = new StringBuffer();
	
	String name = area.getFontState().getFontName();
	int size = area.getFontState().getFontSize();
	
	float red = area.getRed();
	float green = area.getGreen();
	float blue = area.getBlue();
	
	if ((!name.equals(this.currentFontName))
	    || (size != this.currentFontSize)) {
	    this.currentFontName = name;
	    this.currentFontSize = size;
	}
	
	if ((red != this.currentRed)
	    || (green != this.currentGreen)
	    || (blue != this.currentBlue)) {
	    this.currentRed = red;
	    this.currentGreen = green;
	    this.currentBlue = blue;
	}

	int rx = this.currentXPosition;
	int bl = this.currentYPosition;


	String s = area.getText();
	Color oldColor = graphics.getColor();
	java.awt.Font oldFont = graphics.getFont();
	String aFontName = fontNames.get(name).toString();

	aFontName = getJavaFontName(aFontName);

	java.awt.Font f =
	    new java.awt.Font(aFontName,
			      ((Integer)fontStyles.get(name)).intValue(),
			      (int)(size / 1000f));

	graphics.setColor(new Color(red, green, blue));
		
	/*
	  Die KLasse TextLayout nimmt für die Ausgabe eigenen Schriftsatz,
	  der i.R. breiter ist. Deshalb wird bis diese Tatsache sich geklärt/
	  geregelt hat weniger schöne Ausgabe über Graphics benutzt.
	*/

	// Fonts in bold still have trouble displaying!
	FontRenderContext newContext = new FontRenderContext(null, true, true);
	TextLayout layout = new TextLayout(s, f, newContext);
	graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
				  RenderingHints.VALUE_RENDER_QUALITY);
	layout.draw(graphics, rx / 1000f, (int)(pageHeight - bl / 1000f));

	graphics.setColor(oldColor);
       	this.currentXPosition += area.getContentWidth();
    }

    public void renderInlineSpace(InlineSpace space) {
	this.currentXPosition += space.getSize();
    }

    public void renderLineArea(LineArea area) {
	int rx = this.currentAreaContainerXPosition
	    + area.getStartIndent();
	int ry = this.currentYPosition;
	int w = area.getContentWidth();
	int h = area.getHeight();

	this.currentYPosition -= area.getPlacementOffset();
	this.currentXPosition = rx;

	int bl = this.currentYPosition;

	Enumeration e = area.getChildren().elements();
	while (e.hasMoreElements()) {
	    org.apache.fop.layout.Box b =
		(org.apache.fop.layout.Box) e.nextElement();
	    b.render(this);
	}

	this.currentYPosition = ry-h;
    }

    /**
     * render rule area into PDF
     *
     * @param area area to render
     */
    public void renderRuleArea(RuleArea area) {
	int rx = this.currentAreaContainerXPosition
	    + area.getStartIndent();
	int ry = this.currentYPosition;
	int w = area.getContentWidth();
	int h = area.getHeight();
	int th = area.getRuleThickness();
	float r = area.getRed();
	float g = area.getGreen();
	float b = area.getBlue();
	Color oldColor = graphics.getColor();

	graphics.setColor(new Color(r, g, b));

	graphics.fillRect((int)(rx / 1000f), (int)(pageHeight - ry / 1000f),
			  (int)(w / 1000f), (int)(th / 1000f));
	graphics.setColor(oldColor);

    }

    public void renderSVGArea(SVGArea area) {
	int x = this.currentAreaContainerXPosition;
	int y = this.currentYPosition;
	int w = area.getContentWidth();
	int h = area.getHeight();
	this.currentYPosition -= h;
	
//  	Enumeration e = area.getChildren().elements();
//  	while (e.hasMoreElements()) {
//  	    Object o = e.nextElement();
//  	    if (o instanceof RectGraphic) {
//  		int rx = ((RectGraphic)o).x;
//  		int ry = ((RectGraphic)o).y;
//  		int rw = ((RectGraphic)o).width;
//  		int rh = ((RectGraphic)o).height;
//  		addRect(x+rx,y-ry,rw,-rh,0,0,0);
//  	    } else if (o instanceof LineGraphic) {
//  		int x1 = ((LineGraphic)o).x1;
//  		int y1 = ((LineGraphic)o).y1;
//  		int x2 = ((LineGraphic)o).x2;
//  		int y2 = ((LineGraphic)o).y2;
//  		addLine(x+x1,y-y1,x+x2,y-y2,0,0,0,0);
//  	    } else if (o instanceof TextGraphic) {
//  		int tx = ((TextGraphic)o).x;
//  		int ty = ((TextGraphic)o).y;
//  		String s = ((TextGraphic)o).s;
//  		currentStream.add("1 0 0 1 "
//  				  + ((x+tx)/1000f) + " "
//  				  + ((y-ty)/1000f) + " Tm "
//  				  + "(" + s + ") Tj\n");
//  	    }
//  	}
    }

    protected String getJavaFontName(String aName) {
	if (aName == null)
	    return null;

	Object o = JAVA_FONT_NAMES.get(aName);

	return (o == null) ? aName : o.toString();
    }

    public void setProducer(String producer) {
	// defined in Renderer Interface
    }

    public int print(Graphics g, PageFormat pageFormat, int pageIndex)
	throws PrinterException {
	if (pageIndex >= tree.getPages().size())
	    return NO_SUCH_PAGE;

	Graphics2D oldGraphics = graphics;
	int oldPageNumber = pageNumber;

	graphics = (Graphics2D)g;
	Page aPage = (Page)tree.getPages().elementAt(pageIndex);
	renderPage(aPage);
	graphics = oldGraphics;

	return PAGE_EXISTS;
    }

    public int getNumberOfPages() {
	return tree.getPages().size();
    }

    public PageFormat getPageFormat(int pageIndex)
	throws IndexOutOfBoundsException {
	if (pageIndex >= tree.getPages().size())
	    return null;
	
	Page page = (Page)tree.getPages().elementAt(pageIndex);
	PageFormat pageFormat = new PageFormat();
	Paper paper = new Paper();
	paper.setImageableArea(0, 0,
			       page.getWidth() / 1000d, page.getHeight() / 1000d);
	paper.setSize(page.getWidth() / 1000d, page.getHeight() / 1000d);
	pageFormat.setPaper(paper);
	
	return pageFormat;
    }

    public Printable getPrintable(int pageIndex)
	throws IndexOutOfBoundsException {
	return this;
    }

    public void setProgressListener(ProgressListener l) {
	progressListener = l;
    }

    public static Color colorType2Color(ColorType ct) {
	if (ct == null) {
	    return null;
	}
	return new Color(ct.red(), ct.green(), ct.blue());
    }

}

