package org.apache.xml.fop.render.pdf;

// FOP
import org.apache.xml.fop.render.Renderer;
import org.apache.xml.fop.image.ImageArea;
import org.apache.xml.fop.image.FopImage;
import org.apache.xml.fop.layout.*;
import org.apache.xml.fop.svg.*;
import org.apache.xml.fop.pdf.*;

// Java
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

/**
 * Renderer that renders areas to PDF
 */
public class PDFRenderer implements Renderer {
	
    /** the PDF Document being created */
    protected PDFDocument pdfDoc;

    /** the /Resources object of the PDF document being created */
    protected PDFResources pdfResources;

    /** the current stream to add PDF commands to */
    PDFStream currentStream;

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

    /**
     * create the PDF renderer
     */
    public PDFRenderer() {
	this.pdfDoc = new PDFDocument();
    }

    /**
     * set the PDF document's producer
     *
     * @param producer string indicating application producing PDF
     */
    public void setProducer(String producer) {
	this.pdfDoc.setProducer(producer);
    }

    /**
     * render the areas into PDF
     *
     * @param areaTree the laid-out area tree
     * @param writer the PrintWriter to write the PDF with
     */
    public void render(AreaTree areaTree, PrintWriter writer) throws IOException {
	System.err.println("rendering areas to PDF");
	this.pdfResources = this.pdfDoc.getResources();
	Enumeration e = areaTree.getPages().elements();
	while (e.hasMoreElements()) {
	    this.renderPage((Page) e.nextElement());
	}
	System.err.println("writing out PDF");
	this.pdfDoc.output(writer);
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
	currentStream.add(r + " " + g + " " + b + " RG\n"
			  + (x1/1000f) + " " + (y1/1000f) + " m "
			  + (x2/1000f) + " " + (y2/1000f) + " l "
			  + (th/1000f) + " w S\n"
			  + "0 0 0 RG\n");
    }

    /**
     * add a rectangle to the current stream
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
	currentStream.add(r + " " + g + " " + b + " RG\n"
			  + (x/1000f) + " " + (y/1000f) + " "
			  + (w/1000f) + " " + (h/1000f) + " re S\n"
			  + "0 0 0 RG\n");
    }

    /**
     * add a filled rectangle to the current stream
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
	currentStream.add(fr + " " + fg + " " + fb + " rg\n"
			  + r + " " + g + " " + b + " RG\n"
			  + (x/1000f) + " " + (y/1000f) + " "
			  + (w/1000f) + " " + (h/1000f) + " re S\n"
			  + (x/1000f) + " " + (y/1000f) + " "
			  + (w/1000f) + " " + (h/1000f) + " re f\n"
			  + "0 0 0 RG 0 0 0 rg\n");
    }

    /**
     * render area container to PDF
     *
     * @param area the area container to render
     */
    public void renderAreaContainer(AreaContainer area) {

	/* move into position */
	currentStream.add("1 0 0 1 "
			  + (area.getXPosition()/1000f) + " "
			  + (area.getYPosition()/1000f) + " Tm\n");

	this.currentYPosition = area.getYPosition();
	this.currentAreaContainerXPosition = area.getXPosition();

	Enumeration e = area.getChildren().elements();
	while (e.hasMoreElements()) {
	    Box b = (Box) e.nextElement();
	    b.render(this);
	}
    }

    /**
     * render block area to PDF
     *
     * @param area the block area to render
     */
    public void renderBlockArea(BlockArea area) {
	int rx = this.currentAreaContainerXPosition
	    + area.getStartIndent();
	int ry = this.currentYPosition;
	int w = area.getContentWidth();
	int h = area.getHeight();
	Enumeration e = area.getChildren().elements();
	while (e.hasMoreElements()) {
	    Box b = (Box) e.nextElement();
	    b.render(this);
	}
    }

    /**
     * render display space to PDF
     *
     * @param space the display space to render
     */
    public void renderDisplaySpace(DisplaySpace space) {
	int d = space.getSize();
	this.currentYPosition -= d;
    }

    /**
     * render image area to PDF
     *
     * @param area the image area to render
     */
    public void renderImageArea(ImageArea area) {
	// adapted from contribution by BoBoGi
	int x = this.currentAreaContainerXPosition +
	    area.getXOffset();
	int y = this.currentYPosition;
	int w = area.getContentWidth();
	int h = area.getHeight();

	this.currentYPosition -= h*1000;

	FopImage img = area.getImage();

	int xObjectNum = this.pdfDoc.addImage(img);

	currentStream.add("ET\nq\n" + (img.getWidth()/1000f) + " 0 0 " +
			  (img.getHeight()/1000f) + " " + 
			  ((x + img.getX())/1000f) + " " + 
			  (((y - h) - img.getY())/1000f) + " cm\n" +
			  "/Im" + xObjectNum + " Do\nQ\nBT\n"); 
    }
    
    /**
     * render SVG area to PDF
     *
     * @param area the SVG area to render
     */
    public void renderSVGArea(SVGArea area) {
	int x = this.currentAreaContainerXPosition;
	int y = this.currentYPosition;
	int w = area.getContentWidth();
	int h = area.getHeight();
	this.currentYPosition -= h;
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
	}
    }

    /**
     * render inline area to PDF
     *
     * @param area inline area to render
     */
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
	    pdf = pdf.append("/" + name + " " + (size/1000) + " Tf\n");
	}

	if ((red != this.currentRed)
	    || (green != this.currentGreen)
	    || (blue != this.currentBlue)) {
	    this.currentRed = red;
	    this.currentGreen = green;
	    this.currentBlue = blue;
	    pdf = pdf.append(red + " " + green + " " + blue + " rg\n");
	}
		
	int rx = this.currentXPosition;
	int bl = this.currentYPosition;

	pdf = pdf.append("1 0 0 1 "
			 +(rx/1000f) + " " + (bl/1000f)
			 + " Tm (");

	String s = area.getText();
	int l = s.length();

	for (int i=0; i < l; i++) {
	    ch = s.charAt(i);
	    if (ch > 127) {
		pdf = pdf.append("\\");
		pdf = pdf.append(Integer.toOctalString((int)ch));
	    } else {
		switch (ch) {
		case '('  : pdf = pdf.append("\\("); break;
		case ')'  : pdf = pdf.append("\\)"); break;
		case '\\' : pdf = pdf.append("\\\\"); break;
		default   : pdf = pdf.append(ch); break;
		}
	    }
	}
	pdf = pdf.append(") Tj\n");

	currentStream.add(pdf.toString());

	this.currentXPosition += area.getContentWidth();
    }

    /**
     * render inline space to PDF
     *
     * @param space space to render
     */
    public void renderInlineSpace(InlineSpace space) {
	this.currentXPosition += space.getSize();
    }

    /**
     * render line area to PDF
     *
     * @param area area to render
     */
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
	    Box b = (Box) e.nextElement();
	    b.render(this);
	}

	this.currentYPosition = ry-h;
    }

    /**
     * render page into PDF
     *
     * @param page page to render
     */
    public void renderPage(Page page) {
	AreaContainer body, before, after;
      
	currentStream = this.pdfDoc.makeStream();
	body = page.getBody();
	before = page.getBefore();
	after = page.getAfter();

	this.currentFontName = "";
	this.currentFontSize = 0;

	currentStream.add("BT\n");
	renderAreaContainer(body);

	if (before != null) {
	    renderAreaContainer(before);
	}

	if (after != null) {
	    renderAreaContainer(after);
	}
	
	currentStream.add("ET\n");

	this.pdfDoc.makePage(this.pdfResources, currentStream,
			     page.getWidth()/1000, page.getHeight()/1000);
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
	
	addLine(rx, ry, rx+w, ry, th, r, g, b);
    }

    /**
     * set up the font info
     *
     * @param fontInfo font info to set up
     */
    public void setupFontInfo(FontInfo fontInfo) {
	FontSetup.setup(fontInfo);
	FontSetup.addToResources(this.pdfDoc, fontInfo);
    }
}
