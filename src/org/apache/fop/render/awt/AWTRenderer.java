/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.render.awt;

/*
 * originally contributed by
 * Juergen Verwohlt: Juergen.Verwohlt@jCatalog.com,
 * Rainer Steinkuhle: Rainer.Steinkuhle@jCatalog.com,
 * Stanislav Gorkhover: Stanislav.Gorkhover@jCatalog.com
 */

import org.apache.fop.layout.*;
import org.apache.fop.layout.inline.*;
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.datatypes.*;
import org.apache.fop.image.*;
import org.apache.fop.svg.*;
import org.apache.fop.render.pdf.*;
import org.apache.fop.viewer.*;
import org.apache.fop.apps.*;

import org.w3c.dom.svg.*;
import org.w3c.dom.Document;

import org.apache.batik.bridge.*;
import org.apache.batik.swing.svg.*;
import org.apache.batik.swing.gvt.*;
import org.apache.batik.gvt.*;
import org.apache.batik.gvt.renderer.*;
import org.apache.batik.gvt.filter.*;
import org.apache.batik.gvt.event.*;

import java.awt.*;
import java.awt.Image;
import java.awt.image.*;
import java.awt.geom.*;
import java.awt.font.*;
import java.util.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.*;
import java.beans.*;
import javax.swing.*;
import java.awt.print.*;
import java.awt.image.BufferedImage;
import java.text.*;

import org.apache.fop.render.Renderer;

/**
  Modified by Mark Lillywhite mark-fop@inomial.com. Did lots of
  cleaning up and made the class implement the new Renderer
  interface. This class could also do with a general audit,
  and I suspect it's not swing-thread-safe either.
*/

public class AWTRenderer implements Renderer, Printable, Pageable {

    protected int pageWidth = 0;
    protected int pageHeight = 0;
    protected double scaleFactor = 100.0;
    protected int pageNumber = 0;
    protected Vector pageList = new Vector();
    protected ProgressListener progressListener = null;
    protected Translator res = null;

    protected Hashtable fontNames = new Hashtable();
    protected Hashtable fontStyles = new Hashtable();
    protected Color saveColor = null;

    protected IDReferences idReferences = null;

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
     * The parent component, used to set up the font.
     * This is needed as FontSetup needs a live AWT component
     * in order to generate valid font measures.
     */
    protected Component parent;

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

    /**
     * options
     */
    protected Hashtable options;

    /**
     * set up renderer options
     */
    public void setOptions(Hashtable options) {
        this.options = options;
    }

    public AWTRenderer(Translator aRes) {
        res = aRes;
    }

    /**
     * Sets parent component which is  used to set up the font.
     * This is needed as FontSetup needs a live AWT component
     * in order to generate valid font measures.
     * @param parent the live AWT component reference
     */
    public void setComponent(Component parent) {
        this.parent = parent;
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

    // corrected 7/13/01 aml,rlc to properly handle thickness
    //
    protected void addLine(int x1, int y1, int x2, int y2, int th, float r,
                           float g, float b) {
        graphics.setColor(new Color(r, g, b));
        int x = x1;
        int y = y1;
        int height, width;
        if (x1 == x2)    // vertical line
        {
            height = y2 - y1;
            if (height > 0)    // y coordinates are reversed between fo and AWT
            {
                height = -height;
                y = y2;
            }
            width = th;
            if (width < 0) {
                width = -width;
                x -= width;
            }
        } else           // horizontal line
        {
            width = x2 - x1;
            if (width < 0) {
                width = -width;
                x = x2;
            }
            height = th;
            if (height > 0)    // y coordinates are reversed between fo and AWT
            {
                height = -height;
                y -= height;
            }
        }
        addRect(x, y, width, height, false);

        // // graphics.setColor(Color.red);
        // graphics.drawLine((int)(x1 / 1000f),
        // pageHeight - (int)(y1 / 1000f), (int)(x2 / 1000f),
        // pageHeight - (int)(y2 / 1000f));
    }


    /**
     * draw a rectangle
     *
     * @param x the x position of left edge in millipoints
     * @param y the y position of top edge in millipoints
     * @param w the width in millipoints
     * @param h the height in millipoints
     * @param r the red component
     * @param g the green component
     * @param b the blue component
     */

    // changed by aml/rlc to use helper function that
    // corrects for integer roundoff, and to remove 3D effect


    protected void addRect(int x, int y, int w, int h, float r, float g,
                           float b) {
        graphics.setColor(new Color(r, g, b));
        // graphics.setColor(Color.green);
        addRect(x, y, w, h, true);
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

    // changed by aml/rlc to use helper function that
    // corrects for integer roundoff
    protected void addRect(int x, int y, int w, int h, float r, float g,
                           float b, float fr, float fg, float fb) {
        graphics.setColor(new Color(r, g, b));
        addRect(x, y, w, h, true);
        graphics.setColor(new Color(fr, fg, fb));
        addRect(x, y, w, h, false);
    }

    /**
     * draw a filled rectangle in the current color
     *
     * @param x the x position of left edge in millipoints
     * @param y the y position of top edge in millipoints
     * @param w the width in millipoints
     * @param h the height in millipoints
     * @param drawAsOutline true for draw, false for fill
     */

    // helper function by aml/rlc to correct integer roundoff problems
    //
    protected void addRect(int x, int y, int w, int h,
                           boolean drawAsOutline) {
        int startx = (x + 500) / 1000;
        int starty = pageHeight - ((y + 500) / 1000);
        int endx = (x + w + 500) / 1000;
        int endy = pageHeight - ((y + h + 500) / 1000);
        if (drawAsOutline)
            graphics.drawRect(startx, starty, endx - startx, endy - starty);
        else
            graphics.fillRect(startx, starty, endx - startx, endy - starty);
    }

    /**
     * To configure before print.
     *
     * Choose pages
     * Zoom factor
     * Page format  / Landscape or Portrait
     */
    public void transform(Graphics2D g2d, double zoomPercent, double angle) {
        AffineTransform at = g2d.getTransform();
        at.rotate(angle);
        at.scale(zoomPercent / 100.0, zoomPercent / 100.0);
        g2d.setTransform(at);
    }

    protected void drawFrame() {

        int width = pageWidth;
        int height = pageHeight;

        graphics.setColor(Color.white);
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(Color.black);
        graphics.drawRect(-1, -1, width + 2, height + 2);
        graphics.drawLine(width + 2, 0, width + 2, height + 2);
        graphics.drawLine(width + 3, 1, width + 3, height + 3);

        graphics.drawLine(0, height + 2, width + 2, height + 2);
        graphics.drawLine(1, height + 3, width + 3, height + 3);
    }

    /**
     * Retrieve the number of pages in this document.
     *
     * @return the number of pages
     */
    public int getPageCount() {
        return pageList.size();
    }

    public void removePage(int page) {
        pageList.removeElementAt(page);
    }

    public void render(int aPageNumber) {
        if(aPageNumber >= pageList.size())
            return;

        try {
            render((Page) pageList.elementAt(aPageNumber));
        } catch(IOException e) {
            e.printStackTrace();
            // This exception can't occur because we are not dealing with
            // any files
        }

    }

    public void render(Page page, OutputStream stream)
    throws IOException {
        pageList.addElement(page);
    }

    public void render(Page page)
    throws IOException {
        idReferences = page.getIDReferences();

        pageWidth = (int)((float)page.getWidth() / 1000f + .5);
        pageHeight = (int)((float)page.getHeight() / 1000f + .5);


        pageImage =
            new BufferedImage((int)((pageWidth * (int)scaleFactor) / 100),
                              (int)((pageHeight * (int)scaleFactor) / 100),
                              BufferedImage.TYPE_INT_RGB);

        graphics = pageImage.createGraphics();

        transform(graphics, scaleFactor, 0);
        drawFrame();

        renderPage(page);
    }

    public void renderPage(Page page) {
        BodyAreaContainer body;
        AreaContainer before, after;

        body = page.getBody();
        before = page.getBefore();
        after = page.getAfter();

        this.currentFontName = "";
        this.currentFontSize = 0;

        renderBodyAreaContainer(body);

        if (before != null) {
            renderAreaContainer(before);
        }

        if (after != null) {
            renderAreaContainer(after);
        }

        // SG: Wollen wir Links abbilden?
        /*
         * if (page.hasLinks()) {
         * ....
         * }
         */
    }

    public void renderAreaContainer(AreaContainer area) {

        int saveY = this.currentYPosition;
        int saveX = this.currentAreaContainerXPosition;

        if (area.getPosition()
                == org.apache.fop.fo.properties.Position.ABSOLUTE) {
            // Y position is computed assuming positive Y axis, adjust
            // for negative postscript one
            this.currentYPosition = area.getYPosition()
                                    - 2 * area.getPaddingTop()
                                    - 2 * area.getBorderTopWidth();
            this.currentAreaContainerXPosition = area.getXPosition();
        } else if (area.getPosition()
                   == org.apache.fop.fo.properties.Position.RELATIVE) {
            this.currentYPosition -= area.getYPosition();
            this.currentAreaContainerXPosition += area.getXPosition();
        } else if (area.getPosition()
                   == org.apache.fop.fo.properties.Position.STATIC) {
            this.currentYPosition -= area.getPaddingTop()
                                     + area.getBorderTopWidth();
            this.currentAreaContainerXPosition += area.getPaddingLeft()
                                                  + area.getBorderLeftWidth();
        }

        doFrame(area);

        Enumeration e = area.getChildren().elements();
        while (e.hasMoreElements()) {
            org.apache.fop.layout.Box b =
                (org.apache.fop.layout.Box)e.nextElement();
            b.render(this);
        }

        if (area.getPosition()
                != org.apache.fop.fo.properties.Position.STATIC) {
            this.currentYPosition = saveY;
            this.currentAreaContainerXPosition = saveX;
        } else {
            this.currentYPosition -= area.getHeight();
        }
    }

    // empty for now
    public void renderBodyAreaContainer(BodyAreaContainer area) {
        renderAreaContainer(area.getBeforeFloatReferenceArea());
        renderAreaContainer(area.getFootnoteReferenceArea());

        // main reference area
        Enumeration e = area.getMainReferenceArea().getChildren().elements();
        while (e.hasMoreElements()) {
            org.apache.fop.layout.Box b =
                (org.apache.fop.layout.Box)e.nextElement();
            b.render(this);    // span areas
        }

    }

    // empty for now
    public void renderSpanArea(SpanArea area) {
        Enumeration e = area.getChildren().elements();
        while (e.hasMoreElements()) {
            org.apache.fop.layout.Box b =
                (org.apache.fop.layout.Box)e.nextElement();
            b.render(this);    // column areas
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
            this.addRect(rx, ry, w, -h, bg.red(), bg.green(), bg.blue(),
                         bg.red(), bg.green(), bg.blue());
        }

        rx = rx - area.getBorderLeftWidth();
        ry = ry + area.getBorderTopWidth();
        w = w + area.getBorderLeftWidth() + area.getBorderRightWidth();
        h = h + area.getBorderTopWidth() + area.getBorderBottomWidth();

        BorderAndPadding bp = area.getBorderAndPadding();
        ColorType borderColor;

        if (area.getBorderTopWidth() != 0) {
            borderColor = bp.getBorderColor(BorderAndPadding.TOP);
            // addLine(rx, ry, rx + w, ry, area.getBorderTopWidth(),   // corrected aml/rlc
            addLine(rx, ry, rx + w, ry, -area.getBorderTopWidth(),
                    borderColor.red(), borderColor.green(),
                    borderColor.blue());
        }

        if (area.getBorderLeftWidth() != 0) {
            borderColor = bp.getBorderColor(BorderAndPadding.LEFT);
            addLine(rx, ry, rx, ry - h, area.getBorderLeftWidth(),
                    borderColor.red(), borderColor.green(),
                    borderColor.blue());
        }

        if (area.getBorderRightWidth() != 0) {
            borderColor = bp.getBorderColor(BorderAndPadding.RIGHT);
            addLine(rx + w, ry, rx + w, ry - h,
                    // area.getBorderRightWidth(), borderColor.red(), // corrected aml/rlc
                    -area.getBorderRightWidth(), borderColor.red(),
                    borderColor.green(),
                    borderColor.blue());
        }

        if (area.getBorderBottomWidth() != 0) {
            borderColor = bp.getBorderColor(BorderAndPadding.BOTTOM);
            addLine(rx, ry - h, rx + w, ry - h, area.getBorderBottomWidth(),
                    borderColor.red(), borderColor.green(),
                    borderColor.blue());
        }
    }



    protected Rectangle2D getBounds(org.apache.fop.layout.Area a) {
        return new Rectangle2D.Double(currentAreaContainerXPosition,
                                      currentYPosition,
                                      a.getAllocationWidth(), a.getHeight());
    }

    /*
     * public void renderBlockArea(BlockArea area) {
     * doFrame(area);
     * Enumeration e = area.getChildren().elements();
     * while (e.hasMoreElements()) {
     * org.apache.fop.layout.Box b =
     * (org.apache.fop.layout.Box) e.nextElement();
     * b.render(this);
     * }
     * }
     */
    public void renderBlockArea(BlockArea area) {
        this.currentYPosition -= (area.getPaddingTop()
                                  + area.getBorderTopWidth());
        doFrame(area);
        Enumeration e = area.getChildren().elements();
        while (e.hasMoreElements()) {
            org.apache.fop.layout.Box b =
                (org.apache.fop.layout.Box)e.nextElement();
            b.render(this);
        }
        this.currentYPosition -= (area.getPaddingBottom()
                                  + area.getBorderBottomWidth());
    }


    public void setupFontInfo(FontInfo fontInfo) {
        // create a temp Image to test font metrics on
        BufferedImage fontImage =
            new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        FontSetup.setup(fontInfo, fontImage.createGraphics());
    }

    public void renderDisplaySpace(DisplaySpace space) {
        int d = space.getSize();
        this.currentYPosition -= d;
    }


    // correct integer roundoff    (aml/rlc)

    public void renderImageArea(ImageArea area) {

        int x = currentAreaContainerXPosition + area.getXOffset();

        int y = currentYPosition;
        int w = area.getContentWidth();
        int h = area.getHeight();

        FopImage img = area.getImage();

        if (img == null) {
            MessageHandler.logln("Error while loading image : area.getImage() is null");

            // correct integer roundoff
            // graphics.drawRect(x / 1000, pageHeight - y / 1000,
            // w / 1000, h / 1000);
            addRect(x, y, w, h, true);    // use helper function


            java.awt.Font f = graphics.getFont();
            java.awt.Font smallFont = new java.awt.Font(f.getFontName(),
                                      f.getStyle(), 8);

            graphics.setFont(smallFont);

            // correct integer roundoff   // aml/rlc
            // graphics.drawString("area.getImage() is null", x / 1000,
            // pageHeight - y / 1000);

            graphics.drawString("area.getImage() is null", (x + 500) / 1000,
                                pageHeight - (y + 500) / 1000);


            graphics.setFont(f);
        } else {
            if (img instanceof SVGImage) {
                try {
                    SVGDocument svg = ((SVGImage)img).getSVGDocument();
                    renderSVGDocument(svg, (int)x, (int)y);
                } catch (FopImageException e) {}

            } else {

                String urlString = img.getURL();
                try {
                    URL url = new URL(urlString);

                    ImageIcon icon = new ImageIcon(url);
                    Image image = icon.getImage();

                    // correct integer roundoff      aml/rlc
                    // graphics.drawImage(image, x / 1000,
                    // pageHeight - y / 1000, w / 1000, h / 1000,
                    // null);

                    int startx = (x + 500) / 1000;
                    int starty = pageHeight - ((y + 500) / 1000);
                    int endx = (x + w + 500) / 1000;
                    int endy = pageHeight - ((y + h + 500) / 1000);

                    // reverse start and end y because h is positive
                    graphics.drawImage(image, startx, starty, endx - startx,
                                       starty - endy, null);

                } catch (MalformedURLException mue) {
                    // cannot normally occur because, if URL is wrong, constructing FopImage
                    // will already have failed earlier on
                }

            }
        }

        currentYPosition -= h;
    }

    public void renderWordArea(WordArea area) {
        char ch;
        StringBuffer pdf = new StringBuffer();

        String name = area.getFontState().getFontName();
        int size = area.getFontState().getFontSize();
        boolean underlined = area.getUnderlined();

        float red = area.getRed();
        float green = area.getGreen();
        float blue = area.getBlue();

        FontMetricsMapper mapper;
        try {
            mapper =
                (FontMetricsMapper)area.getFontState().getFontInfo().getMetricsFor(name);
        } catch (FOPException iox) {
            mapper = new FontMetricsMapper("MonoSpaced", java.awt.Font.PLAIN,
                                           graphics);
        }

        if ((!name.equals(this.currentFontName))
                || (size != this.currentFontSize)) {
            this.currentFontName = name;
            this.currentFontSize = size;
        }

        if ((red != this.currentRed) || (green != this.currentGreen)
                || (blue != this.currentBlue)) {
            this.currentRed = red;
            this.currentGreen = green;
            this.currentBlue = blue;
        }

        int rx = this.currentXPosition;
        int bl = this.currentYPosition;


        String s;    // = area.getText();
        if (area.getPageNumberID()
                != null) {    // this text is a page number, so resolve it
            s = idReferences.getPageNumber(area.getPageNumberID());
            if (s == null) {
                s = "";
            }
        } else {
            s = area.getText();
        }

        Color oldColor = graphics.getColor();
        java.awt.Font oldFont = graphics.getFont();
        java.awt.Font f = mapper.getFont(size);

        if (saveColor != null) {
            if (saveColor.getRed() != red || saveColor.getGreen() != green
                    || saveColor.getBlue() != blue) {
                saveColor = new Color(red, green, blue);
            }
        } else {
            saveColor = new Color(red, green, blue);
        }
        graphics.setColor(saveColor);

        AttributedString ats = new AttributedString(s);
        ats.addAttribute(TextAttribute.FONT, f);
        if (underlined) {
            ats.addAttribute(TextAttribute.UNDERLINE,
                             TextAttribute.UNDERLINE_ON);
        }
        AttributedCharacterIterator iter = ats.getIterator();

        // correct integer roundoff
        // graphics.drawString(iter, rx / 1000f,
        // (int)(pageHeight - bl / 1000f));

        graphics.drawString(iter, (rx + 500) / 1000,
                            (int)(pageHeight - (bl + 500) / 1000));

        graphics.setColor(oldColor);
        this.currentXPosition += area.getContentWidth();
    }

    public void renderInlineSpace(InlineSpace space) {
        this.currentXPosition += space.getSize();
    }

    public void renderLineArea(LineArea area) {

        int rx = this.currentAreaContainerXPosition + area.getStartIndent();
        int ry = this.currentYPosition;
        int w = area.getContentWidth();
        int h = area.getHeight();

        this.currentYPosition -= area.getPlacementOffset();
        this.currentXPosition = rx;

        int bl = this.currentYPosition;

        Enumeration e = area.getChildren().elements();
        while (e.hasMoreElements()) {
            org.apache.fop.layout.Box b =
                (org.apache.fop.layout.Box)e.nextElement();
            if (b instanceof InlineArea) {
                InlineArea ia = (InlineArea)b;
                this.currentYPosition = ry - ia.getYOffset();
            } else {
                this.currentYPosition = ry - area.getPlacementOffset();
            }
            b.render(this);
        }

        this.currentYPosition = ry - h;
    }

    /**
     * render leader area into AWT
     *
     * @param area area to render
     */

    // call to addRect corrected by aml/rlc

    public void renderLeaderArea(LeaderArea area) {

        int rx = this.currentXPosition;
        int ry = this.currentYPosition;
        int w = area.getLeaderLength();
        int h = area.getHeight();
        int th = area.getRuleThickness();
        int st = area.getRuleStyle();    // not used at the moment
        float r = area.getRed();
        float g = area.getGreen();
        float b = area.getBlue();
        Color oldColor = graphics.getColor();

        graphics.setColor(new Color(r, g, b));

        // use helper function to correct integer roundoff   - aml/rlc
        // graphics.fillRect((int)(rx / 1000f),
        // (int)(pageHeight - ry / 1000f), (int)(w / 1000f),
        // (int)(th / 1000f));

        addRect(rx, ry, w, -th, false);    // NB addRect expects negative height

        graphics.setColor(oldColor);
        this.currentXPosition += area.getContentWidth();
    }

    public void renderSVGArea(SVGArea area) {

        int x = this.currentXPosition;
        int y = this.currentYPosition;
        int w = area.getContentWidth();
        int h = area.getHeight();

        Document doc = area.getSVGDocument();
        renderSVGDocument(doc, x, y);
        this.currentXPosition += area.getContentWidth();
    }

    protected void renderSVGDocument(Document doc, int x, int y) {
        UserAgent userAgent = new MUserAgent(new AffineTransform());

        GVTBuilder builder = new GVTBuilder();
        GraphicsNodeRenderContext rc = getRenderContext();
        BridgeContext ctx = new BridgeContext(userAgent, rc);
        GraphicsNode root;

        // correct integer roundoff     aml/rlc
        // graphics.translate(x / 1000f, pageHeight - y / 1000f);
        graphics.translate((x + 500) / 1000, pageHeight - (y + 500) / 1000);

        graphics.setRenderingHints(rc.getRenderingHints());
        try {
            root = builder.build(ctx, doc);
            root.paint(graphics, rc);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // correct integer roundoff     aml/rlc
        // graphics.translate(-x / 1000f, y / 1000f - pageHeight);
        graphics.translate(-(x + 500) / 1000, (y + 500) / 1000 - pageHeight);

    }

    public GraphicsNodeRenderContext getRenderContext() {
        GraphicsNodeRenderContext nodeRenderContext = null;
        if (nodeRenderContext == null) {
            RenderingHints hints = new RenderingHints(null);
            hints.put(RenderingHints.KEY_ANTIALIASING,
                      RenderingHints.VALUE_ANTIALIAS_ON);

            hints.put(RenderingHints.KEY_INTERPOLATION,
                      RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            FontRenderContext fontRenderContext =
                new FontRenderContext(new AffineTransform(), true, true);

            TextPainter textPainter = new StrokingTextPainter();

            GraphicsNodeRableFactory gnrFactory =
                new ConcreteGraphicsNodeRableFactory();

            nodeRenderContext =
                new GraphicsNodeRenderContext(new AffineTransform(), null,
                                              hints, fontRenderContext,
                                              textPainter, gnrFactory);
        }

        return nodeRenderContext;
    }


    public void setProducer(String producer) {
        // defined in Renderer Interface
    }


    public int print(Graphics g, PageFormat pageFormat,
                     int pageIndex) throws PrinterException {
        if (pageIndex >= pageList.size())
            return NO_SUCH_PAGE;

        Graphics2D oldGraphics = graphics;
        int oldPageNumber = pageNumber;

        graphics = (Graphics2D)g;
        Page aPage = (Page)pageList.elementAt(pageIndex);
        renderPage(aPage);
        graphics = oldGraphics;

        return PAGE_EXISTS;
    }

    public int getNumberOfPages() {
        return pageList.size();
    }

    public PageFormat getPageFormat(int pageIndex)
    throws IndexOutOfBoundsException {
        if (pageIndex >= pageList.size())
            return null;

        Page page = (Page)pageList.elementAt(pageIndex);
        PageFormat pageFormat = new PageFormat();
        Paper paper = new Paper();

        double width = page.getWidth();
        double height = page.getHeight();

        // if the width is greater than the height assume lanscape mode
        // and swap the width and height values in the paper format
        if (width > height) {
            paper.setImageableArea(0, 0, height / 1000d, width / 1000d);
            paper.setSize(height / 1000d, width / 1000d);
            pageFormat.setOrientation(PageFormat.LANDSCAPE);
        } else {
            paper.setImageableArea(0, 0, width / 1000d, height / 1000d);
            paper.setSize(width / 1000d, height / 1000d);
            pageFormat.setOrientation(PageFormat.PORTRAIT);
        }
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
    /*
     * public void renderImage(String href, float x, float y, float width,
     * float height, Vector transform) {
     * // What is with transformations?
     * try {
     * URL url = new URL(href);
     * ImageIcon imageIcon = new ImageIcon(url);
     * AffineTransform fullTransform = new AffineTransform();
     * AffineTransform aTransform;
     * transform = (transform == null) ? new Vector() : transform;
     * for (int i = 0; i < transform.size(); i++) {
     * org.w3c.dom.svg.SVGTransform t =
     * (org.w3c.dom.svg.SVGTransform)
     * transform.elementAt(i);
     * SVGMatrix matrix = t.getMatrix();
     * aTransform = new AffineTransform(matrix.getA(),
     * matrix.getB(), matrix.getC(), matrix.getD(),
     * matrix.getE(), matrix.getF());
     * fullTransform.concatenate(aTransform);
     * }
     * BufferedImage bi = new BufferedImage((int) width, (int) height,
     * BufferedImage.TYPE_INT_RGB);
     * Graphics2D g2d = bi.createGraphics();
     * BufferedImageOp bop = new AffineTransformOp(fullTransform,
     * AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
     * g2d.drawImage(imageIcon.getImage(), 0, 0, (int) width,
     * (int) height, imageIcon.getImageObserver());
     * graphics.drawImage(bi, bop, (int) x, (int) y);
     * } catch (Exception ex) {
     * MessageHandler.errorln("AWTRenderer: renderImage(): " +
     * ex.getMessage());
     * }
     * }
     */

    public void renderForeignObjectArea(ForeignObjectArea area) {
        area.getObject().render(this);
    }


    protected class MUserAgent implements UserAgent {
        AffineTransform currentTransform = null;

        /**
         * Creates a new SVGUserAgent.
         */
        protected MUserAgent(AffineTransform at) {
            currentTransform = at;
        }

        /**
         * Displays an error message.
         */
        public void displayError(String message) {
            System.err.println(message);
        }

        /**
         * Displays an error resulting from the specified Exception.
         */
        public void displayError(Exception ex) {
            ex.printStackTrace(System.err);
        }

        /**
         * Displays a message in the User Agent interface.
         * The given message is typically displayed in a status bar.
         */
        public void displayMessage(String message) {
            System.out.println(message);
        }

        /**
         * Returns a customized the pixel to mm factor.
         */
        public float getPixelToMM() {
            return 0.264583333333333333333f;    // 72 dpi
        }

        /**
         * Returns the language settings.
         */
        public String getLanguages() {
            return "en";    // userLanguages;
        }

        /**
         * Returns the user stylesheet uri.
         * @return null if no user style sheet was specified.
         */
        public String getUserStyleSheetURI() {
            return null;    // userStyleSheetURI;
        }

        /**
         * Returns the class name of the XML parser.
         */
        public String getXMLParserClassName() {
            return Driver.getParserClassName();
        }

        /**
         * Opens a link in a new component.
         * @param doc The current document.
         * @param uri The document URI.
         */
        public void openLink(SVGAElement elt) {
            // application.openLink(uri);
        }


        public Point getClientAreaLocationOnScreen() {
            return new Point(0, 0);
        }

        public void setSVGCursor(java.awt.Cursor cursor) {}


        public AffineTransform getTransform() {
            return currentTransform;
        }

        public Dimension2D getViewportSize() {
            return new Dimension(100, 100);
        }

        public EventDispatcher getEventDispatcher() {
            return null;
        }

        public boolean supportExtension(String str) {
            return false;
        }

        public boolean hasFeature(String str) {
            return false;
        }

        public void registerExtension(BridgeExtension be) {}


    }

    public void startRenderer(OutputStream outputStream)
    throws IOException {}


    public void stopRenderer(OutputStream outputStream)
    throws IOException {
        render(0);
    }

}
