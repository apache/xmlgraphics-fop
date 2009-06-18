

package org.apache.fop.render.awt;

/*
  originally contributed by
  Juergen Verwohlt: Juergen.Verwohlt@jCatalog.com,
  Rainer Steinkuhle: Rainer.Steinkuhle@jCatalog.com,
  Stanislav Gorkhover: Stanislav.Gorkhover@jCatalog.com
 */

import org.apache.fop.layout.*;
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.datatypes.*;
import org.apache.fop.image.*;
import org.apache.fop.svg.*;
import org.apache.fop.dom.svg.*;
import org.apache.fop.dom.svg.SVGArea;
import org.apache.fop.render.pdf.*;
import org.apache.fop.viewer.*;
import org.apache.fop.apps.*;

import org.w3c.dom.svg.*;

import java.awt.*;
import java.awt.Image;
import java.awt.image.*;
import java.awt.geom.*;
import java.awt.font.*;
import java.util.*;
import java.net.URL;
import java.io.*;
import java.beans.*;
import javax.swing.*;
import java.awt.print.*;
import java.awt.image.BufferedImage;

import org.apache.fop.render.Renderer;

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
      // graphics.setColor(Color.red);
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
	// graphics.setColor(Color.green);
	graphics.drawRect((int) (x/1000f), pageHeight - (int) (y/1000f),
			    (int) (w/1000f), -(int) (h/1000f));
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
   * To configure before print.
   *
   * Choose pages
   * Zoom factor
   * Page format  / Landscape or Portrait
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
                e.printStackTrace();
		// This exception can't occur because we are not dealing with
		// any files.
	    }
	}
    }

    public void render(AreaTree areaTree, PrintWriter writer)
	throws IOException {
	tree = areaTree;
	render(areaTree, 0);
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

    // SG: Wollen wir Links abbilden?
    /*
    if (page.hasLinks()) {
      ....
    }
    */
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
	    MessageHandler.logln("area.getImage() is null");
      }

	try {
		byte[] map = img.getBitmaps();

		String path = img.getURL();

      ImageIcon icon = new ImageIcon(path);
      Image imgage = icon.getImage();

      graphics.drawImage(imgage, currentXPosition / 1000,
                         pageHeight - y / 1000,
                         img.getWidth() / 1000,
                         img.getHeight() / 1000,
                         null);

    currentYPosition -= h;
	} catch (FopImageException imgex) {
		// ?
		MessageHandler.logln("Error while loading image : " + imgex.getMessage());
	}
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

    Enumeration e = area.getChildren().elements();
	while (e.hasMoreElements()) {
            Object o = e.nextElement();
	    if(o instanceof GraphicImpl) {
	      renderElement(area, (GraphicImpl)o, x, y, null);
	     }
	}

    this.currentYPosition -= h;

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



  /**
   * Draws an image.
   * TODO: protect other image formats (JIMI)
   */
  public void renderImage(String href, float x, float y, float width, float height, Vector transform) {
    // What is with transformations?
    try {
      URL url = new URL(href);
      ImageIcon imageIcon = new ImageIcon(url);

      AffineTransform fullTransform = new AffineTransform();
      AffineTransform aTransform;

      transform = (transform == null) ? new Vector() : transform;
      for (int i = 0; i < transform.size(); i++) {
        org.w3c.dom.svg.SVGTransform t = (org.w3c.dom.svg.SVGTransform)transform.elementAt(i);
        SVGMatrix matrix = t.getMatrix();
        aTransform = new AffineTransform(matrix.getA(), matrix.getB(), matrix.getC(),
                                 matrix.getD(), matrix.getE(), matrix.getF());
        fullTransform.concatenate(aTransform);
      }

      BufferedImage bi = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_RGB);
      Graphics2D g2d = bi.createGraphics();
      BufferedImageOp bop = new AffineTransformOp(fullTransform,
                                AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
      g2d.drawImage(imageIcon.getImage(), 0, 0, (int) width, (int) height, imageIcon.getImageObserver());
      graphics.drawImage(bi, bop, (int) x, (int) y);
    } catch (Exception ex) {
      MessageHandler.errorln("AWTRenderer: renderImage(): " + ex.getMessage());
    }
  }



  public void renderElement(SVGArea svgarea, GraphicImpl area, int posx, int posy,
                            Vector parentTransforms)  {
		int x = posx;
		int y = posy;
		Hashtable style = area.oldgetStyle();
		DrawingInstruction di = createInstruction(area, style);

        Object o = null;
        Vector v = area.oldgetTransform();
        v = (v == null) ? new Vector() : v;
        Vector trans = new Vector(v);
        parentTransforms = (parentTransforms == null) ? new Vector() : parentTransforms;

        if (parentTransforms != null) {
          trans.addAll(0, parentTransforms);
        }

		float red = (float) graphics.getColor().getRed();
		float green = (float) graphics.getColor().getGreen();
		float blue = (float) graphics.getColor().getBlue();
        Color c = null;

        ColorType ct = null;
        try {
          o = style.get("fill");
          if (o != null && o instanceof ColorType) {
            ct = (ColorType) o;
            c = new Color((int) (ct.red() * 255f), (int) (ct.green() * 255f), (int) (ct.blue() * 255f));
          }
          o = style.get("stroke");
          if (c == null && o != null && o instanceof ColorType) {
            ct = (ColorType) o;
            c = new Color((int) (ct.red() * 255f), (int) (ct.green() * 255f), (int) (ct.blue() * 255f));
            }
        } catch (Exception ex) {
          MessageHandler.errorln("Can't set color: R G B : " + (int) (ct.red() * 255f) + " " + (int) (ct.green() * 255f) + " " + (int) (ct.blue() * 255f));
          c = Color.pink;
        }

        if (c == null) {
          c = new Color((int) red, (int) green, (int) blue);
        }
        Color oldColor = graphics.getColor();

		if (area instanceof SVGLineElement) {
			graphics.setColor(c);
			SVGLineElementImpl lg = (SVGLineElementImpl) area;

			float x1 = lg.getX1().getBaseVal().getValue() * 1000 + posx;
			float y1 = posy - lg.getY1().getBaseVal().getValue() * 1000 ;
			float x2 = lg.getX2().getBaseVal().getValue() * 1000 + posx;
			float y2 = posy - lg.getY2().getBaseVal().getValue() * 1000;
			// TODO:
            // The thickness of contour protect.
   			int th = 1;
   			o = style.get("stroke-width");
      	    if (o != null)
              th = (int)((SVGLengthImpl)o).getValue();
            Line2D.Double aLine = new Line2D.Double(x1 / 1000f, pageHeight - y1 / 1000f,
                                                    x2 / 1000f, pageHeight - y2 / 1000f);
            drawShape(transformShape(trans, aLine), di);
			graphics.setColor(oldColor);
  		} else if (area instanceof SVGRectElement) {
			graphics.setColor(c);
			SVGRectElement rg = (SVGRectElement)area;
			float rectx = rg.getX().getBaseVal().getValue() * 1000 + posx;
			float recty = posy - rg.getY().getBaseVal().getValue() * 1000;
			float rx = rg.getRx().getBaseVal().getValue() * 1000;
			float ry = rg.getRy().getBaseVal().getValue() * 1000;
			float rw = rg.getWidth().getBaseVal().getValue() * 1000;
			float rh = rg.getHeight().getBaseVal().getValue() * 1000;

            // TODO:
			// rx and ry are roundings.
            // RoundRectangle2D.Double
            Rectangle aRectangle = new Rectangle();
            aRectangle.setRect(rectx / 1000d, pageHeight - recty / 1000d, rw / 1000d, rh / 1000d);
            drawShape(transformShape(trans, aRectangle), di);
			graphics.setColor(oldColor);
		} else if (area instanceof SVGCircleElement) {
			graphics.setColor(c);
			SVGCircleElement cg = (SVGCircleElement)area;
			float cx = cg.getCx().getBaseVal().getValue() * 1000 + posx;
			float cy = posy - cg.getCy().getBaseVal().getValue() * 1000;
			float r = cg.getR().getBaseVal().getValue();
            Ellipse2D.Double anEllipse = new Ellipse2D.Double(cx / 1000d - r,
                                                              pageHeight - cy / 1000d - r,
                                                              r * 2d, r * 2d);
            drawShape(transformShape(trans, anEllipse), di);
			graphics.setColor(oldColor);
		} else if (area instanceof SVGEllipseElement) {
			graphics.setColor(c);
			SVGEllipseElement cg = (SVGEllipseElement)area;
			float cx = cg.getCx().getBaseVal().getValue() * 1000 + posx;
			float cy = posy - cg.getCy().getBaseVal().getValue() * 1000;
			float rx = cg.getRx().getBaseVal().getValue();
			float ry = cg.getRy().getBaseVal().getValue();
            Ellipse2D.Double anEllipse = new Ellipse2D.Double(cx / 1000d - rx,
                                                              pageHeight - cy / 1000d - ry,
                                                              rx * 2d, ry * 2d);
            drawShape(transformShape(trans, anEllipse), di);
			graphics.setColor(oldColor);
		}  else if (area instanceof SVGImageElementImpl) {
			SVGImageElementImpl ig = (SVGImageElementImpl)area;
			renderImage(ig.link, ig.x + posx / 1000f, pageHeight - (posy / 1000f - ig.y), ig.width, ig.height, trans);
		}  else if(area instanceof SVGUseElementImpl) {
			SVGUseElementImpl ug = (SVGUseElementImpl)area;
			String ref = ug.link;
			ref = ref.substring(1, ref.length());
			GraphicImpl graph = null;
//			graph = area.locateDef(ref);
			if(graph != null) {
				// probably not the best way to do this, should be able
				// to render without the style being set.
//				GraphicImpl parent = graph.getGraphicParent();
//				graph.setParent(area);
				// need to clip (if necessary) to the use area
				// the style of the linked element is as if is was
				// a direct descendant of the use element.

				renderElement(svgarea, graph, posx, posy, trans);
//				graph.setParent(parent);
			}
		}  else if (area instanceof SVGPolylineElementImpl) {
		   graphics.setColor(c);
           Vector points = ((SVGPolylineElementImpl)area).points;
           PathPoint p = null;
           Point2D.Double p1 = null;
           Point2D.Double p2 = null;
           if (points.size() > 0) {
             p = (PathPoint) points.elementAt(0);
             double xc = p.x * 1000f + posx;
             double yc = posy - p.y * 1000f;
             p1 = new Point2D.Double(xc / 1000f, pageHeight - yc / 1000f);

             int[] xarr = {(int) xc};
             int[] yarr = {(int) yc};
             graphics.drawPolyline(xarr, yarr, 1);
           }
           Line2D.Double aLine;
           for (int i = 1; i< points.size(); i++) {
             p = (PathPoint) points.elementAt(i);
             p2 = new Point2D.Double(p.x + posx / 1000f, pageHeight - (posy - p.y * 1000f) /  1000f);
             aLine = new Line2D.Double(p1, p2);
             graphics.draw(transformShape(trans, aLine));
             p1 = p2;
           }
		  graphics.setColor(oldColor);
		} else if (area instanceof SVGPolygonElementImpl) {
			graphics.setColor(c);
            java.awt.Polygon aPolygon = convertPolygon(((SVGPolygonElementImpl)area), posx, posy);
            drawShape(transformShape(trans, aPolygon), di);
			graphics.setColor(oldColor);
		}  else if (area instanceof SVGGElementImpl) {
			renderGArea(svgarea, (SVGGElementImpl)area, x, y, parentTransforms);
		} else if (area instanceof SVGPathElementImpl) {
          graphics.setColor(c);
          GeneralPath path = convertPath((SVGPathElementImpl) area, posx, posy);
          drawShape(transformShape(trans, path), di);
          graphics.setColor(oldColor);
		} else if (area instanceof SVGTextElementImpl) {
			MessageHandler.errorln("SVGTextElementImpl  is not implemented yet.");
			// renderText(svgarea, (SVGTextElementImpl)area, 0, 0, di);
		}  else if (area instanceof SVGArea) {
			// the x and y pos will be wrong!
			Enumeration e = ((SVGArea)area).getChildren().elements();
			while (e.hasMoreElements()) {
				Object el = e.nextElement();
				if(o instanceof GraphicImpl) {
                    renderElement((SVGArea)area, (GraphicImpl)el, x, y, parentTransforms);
				}
			}
		}

		// should be done with some cleanup code, so only
		// required values are reset.
  } // renderElement


  public void renderGArea(SVGArea svgarea, SVGGElementImpl area, int posx, int posy, Vector v) {


  Vector trans = null;
//    trans = new Vector(area.oldgetTransform());
//  trans.addAll(0, v);
/*		Enumeration e = area.getChildren().elements();
		while (e.hasMoreElements()) {
			Object o = e.nextElement();
			if(o instanceof GraphicImpl) {
				renderElement(svgarea, (GraphicImpl)o, posx, posy, trans);
			}
		}*/
  }
	public void renderGArea(SVGArea svgarea, SVGGElementImpl area, int posx, int posy)
	{
      renderGArea(svgarea, area, posx, posy, new Vector());
	}


 /**
  * Applies SVGTransform to the shape and gets the transformed shape.
  * The type of the new shape may be different to the original type.
  */
  public Shape transformShape(Vector trans, Shape shape) {
    if (trans == null || trans.size() == 0) {
      return shape;
    }

    AffineTransform at;
    for(int i = trans.size() - 1; i >= 0; i--) {
      org.w3c.dom.svg.SVGTransform t = (org.w3c.dom.svg.SVGTransform)trans.elementAt(i);
      SVGMatrix matrix = t.getMatrix();
      at = new AffineTransform(matrix.getA(), matrix.getB(), matrix.getC(),
                           matrix.getD(), matrix.getE(), matrix.getF());
      shape = at.createTransformedShape(shape);
    }
    return shape;
  }


  /**
   * Mapps a SVG-Polygon to a AWT-Polygon.
   */
  public java.awt.Polygon convertPolygon(SVGPolygonElementImpl svgpl, int x, int y) {
    java.awt.Polygon aPolygon = new java.awt.Polygon();
    Vector points = svgpl.points;

    PathPoint p;
    for (int i = 0; i < points.size(); i++) {
      p = (PathPoint) points.elementAt(i);
      aPolygon.addPoint((int) (x / 1000f + p.x), pageHeight - (int) (y / 1000f - p.y));
    }

    return aPolygon;
  }

  // TODO: other attributes of DrawingInstruction protect too.
  protected DrawingInstruction createInstruction(GraphicImpl area, Hashtable style) {
    DrawingInstruction di = new DrawingInstruction();
    Object sp;
    sp = style.get("fill");
    if(sp != null && !(sp instanceof String && sp.equals("none"))) {
      di.fill = true;
    }
    // ...
    return di;
  }

  // Draws a shape.
  // TODO: other attributes of DrawingInstruction protect too.
  protected void drawShape(Shape s, DrawingInstruction di) {
    if (di.fill) {
      graphics.fill(s);
    } else {
      graphics.draw(s);
    }
  }

  /**
   * Mapps a SVG-Path to a AWT-GeneralPath.
   */
  public GeneralPath convertPath(SVGPathElementImpl svgpath, float x, float y) {
    Vector points = svgpath.pathElements;
    GeneralPath path = new GeneralPath();

    float lastx = 0;
    float lasty = 0;
    SVGPathSegImpl pathmoveto = null;

    for(Enumeration e = points.elements(); e.hasMoreElements(); ) {
      SVGPathSegImpl pc = (SVGPathSegImpl)e.nextElement();
      float[] vals = pc.getValues();
      float lastcx = 0;
      float lastcy = 0;
      switch(pc.getPathSegType()) {
        case SVGPathSeg.PATHSEG_MOVETO_ABS:
          lastx = vals[0];
          lasty = vals[1];
          pathmoveto = pc;
          path.moveTo(lastx + x / 1000f, pageHeight - y / 1000f + lasty);
        break;
        case SVGPathSeg.PATHSEG_MOVETO_REL:
          if (pathmoveto == null) {
            lastx = vals[0];
            lasty = vals[1];
            path.moveTo(lastx + x / 1000f, pageHeight - y / 1000f + lasty);
            pathmoveto = pc;
          } else {
            lastx += vals[0];
            lasty += vals[1];
            path.lineTo(lastx + x / 1000f, pageHeight - y / 1000f + lasty);

          }
        break;
        case SVGPathSeg.PATHSEG_LINETO_ABS:
          lastx = vals[0];
          lasty = vals[1];
          path.lineTo(lastx + x / 1000f, pageHeight - y / 1000f + lasty);
        break;
        case SVGPathSeg.PATHSEG_LINETO_REL:
            lastx += vals[0];
            lasty += vals[1];
            path.lineTo(lastx + x / 1000f, pageHeight - y / 1000f + lasty);
        break;
        case SVGPathSeg.PATHSEG_LINETO_VERTICAL_ABS:
          lasty = vals[0];
          path.lineTo(lastx + x / 1000f, pageHeight - y / 1000f + lasty);
        break;
        case SVGPathSeg.PATHSEG_LINETO_VERTICAL_REL:
          lasty += vals[0];
          path.lineTo(lastx + x / 1000f, pageHeight - y / 1000f + lasty);
        break;
        case SVGPathSeg.PATHSEG_LINETO_HORIZONTAL_ABS:
          lastx = vals[0];
          path.lineTo(lastx + x / 1000f, pageHeight - y / 1000f + lasty);
        break;
        case SVGPathSeg.PATHSEG_LINETO_HORIZONTAL_REL:
          lastx += vals[0];
          path.lineTo(lastx + x / 1000f, pageHeight - y / 1000f + lasty);
        break;
        case SVGPathSeg.PATHSEG_CURVETO_CUBIC_ABS:
          lastx = vals[4];
          lasty = vals[5];
          lastcx = vals[2];
          lastcy = vals[3];
          path.curveTo(x / 1000f + vals[0], pageHeight - y / 1000f + vals[1],
                      x / 1000f + lastcx, pageHeight - y / 1000f + lastcy,
                      x / 1000f + lastx, pageHeight - y / 1000f + lasty);
        break;
        case SVGPathSeg.PATHSEG_CURVETO_CUBIC_REL:
          path.curveTo(x / 1000f + vals[0] + lastx, pageHeight - y / 1000f + vals[1] + lasty,
                       x / 1000f + lastx + vals[2], pageHeight - y / 1000f + lasty + vals[3],
                       x / 1000f + lastx + vals[4], pageHeight - y / 1000f + lasty + vals[5]);
          lastcx = vals[2] + lastx;
          lastcy = vals[3] + lasty;
          lastx += vals[4];
          lasty += vals[5];
        break;
        case SVGPathSeg.PATHSEG_CURVETO_CUBIC_SMOOTH_ABS:
          if (lastcx == 0)
            lastcx = lastx;
          if (lastcy == 0)
            lastcy = lasty;
          lastx = vals[2];
          lasty = vals[3];
          path.curveTo(x / 1000f + lastcx, pageHeight - y / 1000f + lastcy,
                       x / 1000f + vals[0], pageHeight - y / 1000f + vals[1],
                       x / 1000f + lastx, pageHeight - y / 1000f + lasty);
        break;
        case SVGPathSeg.PATHSEG_CURVETO_CUBIC_SMOOTH_REL:
          if (lastcx == 0)
            lastcx = lastx;
          if (lastcy == 0)
            lastcy = lasty;
          path.curveTo(x / 1000f + lastcx, pageHeight - y / 1000f + lastcy,
                       x / 1000f + lastx + vals[0], pageHeight - y / 1000f + lasty + vals[1],
                       x / 1000f + lastx + vals[2], pageHeight - y / 1000f + lasty + vals[3]);
          lastx += vals[2];
          lasty += vals[3];
        break;
        case SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_ABS:
          if (lastcx == 0)
            lastcx = lastx;
          if (lastcy == 0)
            lastcy = lasty;
          lastx = vals[0];
          lasty = vals[1];
          lastcx = 0;
          lastcy = 0;
          path.quadTo(x / 1000f + lastcx, pageHeight - y / 1000f + lastcy,
                      x / 1000f + lastx, pageHeight - y / 1000f + lasty);
        break;
        case SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_REL:
          if (lastcx == 0)
            lastcx = lastx;
          if (lastcy == 0)
            lastcy = lasty;

          path.quadTo(x / 1000f + lastcx , pageHeight - y / 1000f + lastcy,
                      x / 1000f + lastx + vals[0], pageHeight - y / 1000f + lasty + vals[1]);

          lastx += vals[0];
          lasty += vals[1];
          lastcx = 0;
          lastcy = 0;
        break;
        case SVGPathSeg.PATHSEG_ARC_ABS: {
          // Arc2D.Double arc = new Arc2D.Double();
          // arc.setAngles(current point, end point); ....
	    				double rx = vals[0];
	    				double ry = vals[1];
	    				double theta = vals[2];
	    				boolean largearcflag = (vals[3] == 1.0);
	    				boolean sweepflag = (vals[4] == 1.0);

						double cx = lastx;
						double cy = lasty;

      path.curveTo(x / 1000f + lastx, pageHeight - y / 1000f + lasty,
                   x / 1000f + vals[0], pageHeight - y / 1000f + vals[1],
                   x / 1000f + vals[5], pageHeight - y / 1000f + vals[6]);

		    			lastcx = 0; //??
		    			lastcy = 0; //??
		    			lastx = vals[5];
		    			lasty = vals[6];
         }
        break;
        case SVGPathSeg.PATHSEG_ARC_REL: {
	    				double rx = vals[0];
	    				double ry = vals[1];
	    				double theta = vals[2];
	    				boolean largearcflag = (vals[3] == 1.0);
	    				boolean sweepflag = (vals[4] == 1.0);

		path.curveTo(x / 1000f + lastx, pageHeight - y / 1000f + lasty,
                    x / 1000f + (vals[0] + lastx), pageHeight - y / 1000f + (vals[1] + lasty),
					x / 1000f + (vals[5] + lastx) , pageHeight - y / 1000f + (vals[6] + lasty));
		    			lastcx = 0; //??
		    			lastcy = 0; //??
		    			lastx += vals[5];
		    			lasty += vals[6];
         }
        break;
        case SVGPathSeg.PATHSEG_CLOSEPATH:
          path.closePath();
        break;


      } // switch
    } // for points.elements()

    return path;
  }  // convertPath
/*
		if(di == null) {
			currentStream.add("S\n");
		} else {
			if(di.fill) {
				if(di.stroke) {
					if(!di.nonzero)
						currentStream.add("B*\n");
					else
						currentStream.add("B\n");
				} else {
					if(!di.nonzero)
						currentStream.add("f*\n");
					else
						currentStream.add("f\n");
				}
			} else {
//				if(di.stroke)
					currentStream.add("S\n");
			}
		}
*/

	/*
	 * by pdfrenderer übernommen.
	 *
	 */
	class DrawingInstruction {
		boolean stroke = false;
		boolean nonzero = false; // non-zero fill rule "f*", "B*" operator
		boolean fill = false;
		int linecap = 0; // butt
		int linejoin = 0; // miter
		int miterwidth = 8;
	}

    public void renderForeignObjectArea(ForeignObjectArea area) {
      area.getObject().render(this);
    }

}



