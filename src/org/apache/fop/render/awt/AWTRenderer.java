

package org.apache.fop.render.awt;

/*
  originally contributed by
  Juergen Verwohlt: Juergen.Verwohlt@af-software.de,
  Rainer Steinkuhle: Rainer.Steinkuhle@af-software.de,
  Stanislav Gorkhover: Stanislav.Gorkhover@af-software.de
 */

 
import org.apache.fop.layout.*;
import org.apache.fop.image.*;
import org.apache.fop.svg.*;
import org.apache.fop.render.pdf.*;
import org.apache.fop.viewer.*;


import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.awt.font.*;
import java.util.*;
import java.io.*;
import javax.swing.*;


public class AWTRenderer implements org.apache.fop.render.Renderer {

  protected int pageWidth    = 0;
  protected int pageHeight   = 0;
  protected double scaleFactor = 100.0;
  protected int pageNumber = 0;

  protected Hashtable fontNames = new Hashtable();
  protected Hashtable fontStyles = new Hashtable();




  protected Graphics2D graphics = null;

  protected DocumentPanel documentPanel = null;


  /** the current (internal) font name */
  protected String currentFontName;

  /** the current font size in millipoints */
  protected int currentFontSize;

  /** the current colour's red component */
  protected float currentRed = 0;

  /** the current colour's green component */
  protected float currentGreen = 0;

  /** the current colour's blue component */
  protected float currentBlue = 0;

  /** the current vertical position in millipoints from bottom */
  protected int currentYPosition = 0;

  /** the current horizontal position in millipoints from left */
  protected int currentXPosition = 0;

  /** the horizontal position of the current area container */
  private int currentAreaContainerXPosition = 0;


  public AWTRenderer() {
  }

  public void setGraphics(Graphics2D g) {
    graphics = g;
    if (graphics != null) {
      graphics = g;
      graphics.setColor(Color.red);
    }
  }

  public int getPageNumber() {
    return pageNumber;
  }

  public void setPageNumber(int aValue) {
    pageNumber = aValue;
    if (documentPanel == null)
      return;
    documentPanel.updateSize(pageNumber, scaleFactor / 100.0);
  }

  public void setScaleFactor(double newScaleFactor) {
    scaleFactor = newScaleFactor;
    if (documentPanel == null)
      return;
    documentPanel.updateSize(pageNumber, scaleFactor / 100.0);
  }


  public double getScaleFactor() {
    return scaleFactor;
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


  public void render(AreaTree areaTree, PrintWriter writer) throws IOException {
    documentPanel.setAreaTree(areaTree);
    documentPanel.setPageCount(areaTree.getPages().size());
    documentPanel.updateSize(pageNumber, scaleFactor/100.0);
    render(areaTree, 0);
  }

  public void render(AreaTree areaTree, int aPageNumber) throws IOException {
    Page page = (Page)areaTree.getPages().elementAt(aPageNumber);

    pageWidth  = (int)((float)page.getWidth() / 1000f);
    pageHeight = (int)((float)page.getHeight() / 1000f);

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


	this.currentYPosition = area.getYPosition();
	this.currentAreaContainerXPosition = area.getXPosition();

	Enumeration e = area.getChildren().elements();
	while (e.hasMoreElements()) {
	    org.apache.fop.layout.Box b = (org.apache.fop.layout.Box) e.nextElement();
	    b.render(this);
	}
  }



  protected Rectangle2D getBounds(org.apache.fop.layout.Area a) {
    return new Rectangle2D.Double(currentAreaContainerXPosition,
                                  currentYPosition,
                                  a.getAllocationWidth(),
                                  a.getHeight());
  }

    public void renderBlockArea(BlockArea area) {
	int rx = this.currentAreaContainerXPosition
	    + area.getStartIndent();
	int ry = this.currentYPosition;
	int w = area.getContentWidth();
	int h = area.getHeight();
	Enumeration e = area.getChildren().elements();
	while (e.hasMoreElements()) {
	    org.apache.fop.layout.Box b = (org.apache.fop.layout.Box) e.nextElement();
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
    // path = "c:/any.gif";

    ImageIcon icon = new ImageIcon(path); 

    Image imgage = icon.getImage();

    graphics.drawImage(imgage, currentXPosition / 1000,
                       pageHeight - y / 1000,
                       img.getWidth() / 1000,
                       img.getHeight() / 1000,
                       null);

	currentYPosition -= h;


	/* int xObjectNum = this.pdfDoc.addImage(img);

	currentStream.add("ET\nq\n" + (img.getWidth()/1000f) + " 0 0 " +
			  (img.getHeight()/1000f) + " " +
			  ((x + img.getX())/1000f) + " " +
			  (((y - h) - img.getY())/1000f) + " cm\n" +
			  "/Im" + xObjectNum + " Do\nQ\nBT\n");
    */
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
    java.awt.Font f = new java.awt.Font(fontNames.get(name).toString(),
                                        ((Integer)fontStyles.get(name)).intValue(),
                                        (int)(size / 1000f));


    graphics.setColor(new Color(red, green, blue));

    graphics.setFont(f);
    graphics.drawString(s, rx / 1000f, (int)(pageHeight - bl / 1000f));
    graphics.setFont(oldFont);
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
	    org.apache.fop.layout.Box b = (org.apache.fop.layout.Box) e.nextElement();
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
  /*
	Enumeration e = area.getChildren().elements();
	while (e.hasMoreElements()) {
	    Object o = e.nextElement();
	    if (o instanceof RectGraphic) {
		int rx = ((RectGraphic)o).x;
		int ry = ((RectGraphic)o).y;
		int rw = ((RectGraphic)o).width;
		int rh = ((RectGraphic)o).height;
		addRect(x+rx,y-ry,rw,-rh,0,0,0);
	    } else if (o instanceof LineGraphic) {
		int x1 = ((LineGraphic)o).x1;
		int y1 = ((LineGraphic)o).y1;
		int x2 = ((LineGraphic)o).x2;
		int y2 = ((LineGraphic)o).y2;
		addLine(x+x1,y-y1,x+x2,y-y2,0,0,0,0);
	    } else if (o instanceof TextGraphic) {
		int tx = ((TextGraphic)o).x;
		int ty = ((TextGraphic)o).y;
		String s = ((TextGraphic)o).s;
		currentStream.add("1 0 0 1 "
				  + ((x+tx)/1000f) + " "
				  + ((y-ty)/1000f) + " Tm "
				  + "(" + s + ") Tj\n");
	    }
	} */
    }


  public void setProducer(String producer) {
    // this.pdfDoc.setProducer(producer);
  }


  public void setComponent(DocumentPanel comp) {
    documentPanel = comp;
  }

}





