/*
 * $Id: AWTRenderer.java,v 1.44 2003/03/07 09:46:31 jeremias Exp $
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
package org.apache.fop.render.awt;

/*
 * originally contributed by
 * Juergen Verwohlt: Juergen.Verwohlt@jCatalog.com,
 * Rainer Steinkuhle: Rainer.Steinkuhle@jCatalog.com,
 * Stanislav Gorkhover: Stanislav.Gorkhover@jCatalog.com
 */

// Java
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.RenderingHints;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Vector;

// FOP
import org.apache.fop.apps.Document;
import org.apache.fop.apps.InputHandler;
import org.apache.fop.apps.FOPException;
import org.apache.fop.area.Area;
import org.apache.fop.area.Page;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.RegionViewport;
import org.apache.fop.area.Trait;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.fo.FOTreeControl;
import org.apache.fop.fo.properties.BackgroundRepeat;
import org.apache.fop.fonts.Font;
import org.apache.fop.image.FopImage;
import org.apache.fop.image.ImageFactory;
import org.apache.fop.render.AbstractRenderer;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.viewer.PreviewDialog;
import org.apache.fop.viewer.Translator;

/**
 * This is FOP's AWT renderer.
 */
public class AWTRenderer extends AbstractRenderer implements Printable, Pageable {

    protected double scaleFactor = 100.0;
    protected int pageNumber = 0;
    protected Vector pageViewportList = new java.util.Vector();
    protected Vector pageList = new java.util.Vector();
    protected BufferedImage currentPageImage = null;

    /** Font configuration */
    protected Document fontInfo;

    /**
        The InputHandler associated with this Renderer.
        Sent to the PreviewDialog for document reloading.
    */
    protected InputHandler inputHandler;

    /**
     * The resource bundle used for AWT messages.
     */
    protected Translator translator = null;

    protected Map fontNames = new java.util.Hashtable();
    protected Map fontStyles = new java.util.Hashtable();
    protected Color saveColor = null;

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
     * The preview dialog frame used for display of the documents.
     * Also used as the AWT Component for FontSetup in generating
     * valid font measures.
     */
    protected PreviewDialog frame;

    public AWTRenderer(InputHandler handler) {
        inputHandler = handler;
        translator = new Translator();
        createPreviewDialog(inputHandler);
    }

    public AWTRenderer() {
        translator = new Translator();
        createPreviewDialog(null);
    }

    public Translator getTranslator() {
        return translator;
    }

    public void setupFontInfo(FOTreeControl foTreeControl) {
        // create a temp Image to test font metrics on
        fontInfo = (Document) foTreeControl;
        BufferedImage fontImage =
            new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        FontSetup.setup(fontInfo, fontImage.createGraphics());
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

    public void startRenderer(OutputStream out)
    throws IOException {
        // empty pageViewportList, in case of a reload from PreviewDialog
        pageViewportList.removeAllElements();
        pageList.removeAllElements();
        System.out.println("\nRegion Types: 0-Before/Top, 1-Start/Left, 2-Body, 3-End/Right, 4-After/Bottom");
    }

    public void stopRenderer()
    throws IOException {
        frame.setStatus(translator.getString("Status.Show"));
        frame.showPage();
    }

    // Printable Interface
    public PageFormat getPageFormat(int pos) {
        return null;
    }

    public Printable getPrintable(int pos) {
        return null;
    }

    public int getNumberOfPages() {
        return pageViewportList.size();
    }

    public int print(Graphics g, PageFormat format, int pos) {
        return 0;
    }

    private PreviewDialog createPreviewDialog(InputHandler handler) {
        frame = new PreviewDialog(this, handler);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent we) {
                System.exit(0);
            }
        });

        //Centers the window
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
        frame.setLocation((screenSize.width - frameSize.width) / 2,
                          (screenSize.height - frameSize.height) / 2);
        frame.setVisible(true);
        frame.setStatus(translator.getString("Status.Build.FO.tree"));
        return frame;
    }

    /** This method override only stores the PageViewport in a vector.
      * No actual rendering performed -- this is done by getPageImage(pageNum) instead.
      * @param pageViewport the <code>PageViewport</code> object supplied by the Area Tree
      * @see org.apache.fop.render.Renderer
    */
    public void renderPage(PageViewport pageViewport)  throws IOException, FOPException {
        pageViewportList.add(pageViewport);
        pageList.add(pageViewport.getPage().clone());
    }

    /** Generates a desired page from the renderer's page viewport vector.
     * @param pageNum the 0-based page number to generate
     *  @return the <code>java.awt.image.BufferedImage</code> corresponding to the page
     *  @throws FOPException in case of an out-of-range page number requested
    */
    public BufferedImage getPageImage(int pageNum) throws FOPException {
        if (pageNum < 0 || pageNum >= pageViewportList.size()) {
            throw new FOPException("out-of-range page number (" + pageNum
                + ") requested; only " + pageViewportList.size()
                + " page(s) available.");
        }
        PageViewport pageViewport = (PageViewport) pageViewportList.get(pageNum);
        Page page = (Page) pageList.get(pageNum);

        Rectangle2D bounds = pageViewport.getViewArea();
        int pageWidth = (int)((float) bounds.getWidth() / 1000f + .5);
        int pageHeight = (int)((float) bounds.getHeight() / 1000f + .5);
/*
        System.out.println("(Page) X, Y, Width, Height: " + bounds.getX()
            + " " + bounds.getY()
            + " " + bounds.getWidth()
            + " " + bounds.getHeight());
*/
        currentPageImage =
            new BufferedImage((int)((pageWidth * (int)scaleFactor) / 100),
                              (int)((pageHeight * (int)scaleFactor) / 100),
                              BufferedImage.TYPE_INT_RGB);

        Graphics2D graphics = currentPageImage.createGraphics();
        graphics.setRenderingHint (RenderingHints.KEY_FRACTIONALMETRICS,
                                   RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        // transform page based on scale factor supplied
        AffineTransform at = graphics.getTransform();
        at.scale(scaleFactor / 100.0, scaleFactor / 100.0);
        graphics.setTransform(at);

        // draw page frame
        graphics.setColor(Color.white);
        graphics.fillRect(0, 0, pageWidth, pageHeight);
        graphics.setColor(Color.black);
        graphics.drawRect(-1, -1, pageWidth + 2, pageHeight + 2);
        graphics.drawLine(pageWidth + 2, 0, pageWidth + 2, pageHeight + 2);
        graphics.drawLine(pageWidth + 3, 1, pageWidth + 3, pageHeight + 3);
        graphics.drawLine(0, pageHeight + 2, pageWidth + 2, pageHeight + 2);
        graphics.drawLine(1, pageHeight + 3, pageWidth + 3, pageHeight + 3);

        currentFontName = "";
        currentFontSize = 0;
        renderPageAreas(page);
        return currentPageImage;
    }

    /**
     * Handle the traits for a region
     * This is used to draw the traits for the given page region.
     * (See Sect. 6.4.1.2 of XSL-FO spec.)
     * @param region the RegionViewport whose region is to be drawn
     */
    protected void handleRegionTraits(RegionViewport region) {
        currentFontName = "";
        currentFontSize = 0;
        Rectangle2D viewArea = region.getViewArea();

        int startX = (int)(((float) viewArea.getX() / 1000f + .5)
            * (scaleFactor / 100f));
        int startY = (int)(((float) viewArea.getY() / 1000f + .5)
            * (scaleFactor / 100f));
        int width = (int)(((float) viewArea.getWidth() / 1000f + .5)
            * (scaleFactor / 100f));
        int height = (int)(((float) viewArea.getHeight() / 1000f + .5)
            * (scaleFactor / 100f));

        if (region.getRegion() != null) {
            System.out.print("\nRegion type = " + region.getRegion().getRegionClass());
        }
        System.out.println("  X, Width, Y, Height: " + startX
            + " " + width
            + " " + startY
            + " " + height
            );

        drawBackAndBorders(region, startX, startY, width, height);
    }

    /**
     * Draw the background and borders.
     * This draws the background and border traits for an area given
     * the position.
     *
     * @param block the area to get the traits from
     * @param startx the start x position
     * @param starty the start y position
     * @param width the width of the area
     * @param height the height of the area
     */
    protected void drawBackAndBorders(Area block,
                    int startx, int starty,
                    int width, int height) {

        // draw background then border
        Graphics2D graphics = currentPageImage.createGraphics();
        Trait.Background back;
        back = (Trait.Background) block.getTrait(Trait.BACKGROUND);
        if (back != null) {

            if (back.getColor() != null) {
                graphics.setColor(back.getColor().getAWTColor());
                graphics.fillRect(startx, starty, width, height);
            }
            if (back.getURL() != null) {  // TODO: implement
                ImageFactory fact = ImageFactory.getInstance();
                FopImage fopimage = fact.getImage(back.getURL(), userAgent);
                if (fopimage != null && fopimage.load(FopImage.DIMENSIONS, userAgent)) {
                    if (back.getRepeat() == BackgroundRepeat.REPEAT) {
                        // create a pattern for the image
                    } else {
                        // place once
                        Rectangle2D pos;
                        pos = new Rectangle2D.Float((startx + back.getHoriz()) * 1000,
                                                    (starty + back.getVertical()) * 1000,
                                                    fopimage.getWidth() * 1000,
                                                    fopimage.getHeight() * 1000);
//                      putImage(back.getURL(), pos);
                    }
                }
            }
        }

        BorderProps bps = (BorderProps) block.getTrait(Trait.BORDER_BEFORE);
        if (bps != null) {
            int borderWidth = (int) ((bps.width / 1000f) * (scaleFactor / 100f));
            System.out.println("Before (color/width) " + bps.color.getAWTColor().toString() + " " + bps.width);
            graphics.setColor(bps.color.getAWTColor());
            // drawLine(x1, y1, x2, y2);
            System.out.println("Draw from (" + startx + "," + (starty + borderWidth/2) + 
                ") to (" + (startx+width) + "," + (starty + borderWidth/ 2) + ")");
            graphics.drawLine(startx, starty + borderWidth / 2, startx + width, 
                starty + borderWidth / 2);
        }
        bps = (BorderProps) block.getTrait(Trait.BORDER_START);
        if (bps != null) {
            int borderWidth = (int) ((bps.width / 1000f) * (scaleFactor / 100f));
            System.out.println("Start (color/width) " + bps.color.getAWTColor().toString() + " " + bps.width);
            graphics.setColor(bps.color.getAWTColor());
            System.out.println("Draw from (" + (startx + borderWidth / 2) + "," + starty + 
                ") to (" + (startx + borderWidth / 2) + "," + (starty + height) + ")");
            graphics.drawLine(startx + borderWidth / 2, starty, startx + borderWidth / 2, 
                starty + height);
        }
        bps = (BorderProps) block.getTrait(Trait.BORDER_AFTER);
        if (bps != null) {
            int borderWidth = (int) ((bps.width / 1000f) * (scaleFactor / 100f));
            System.out.println("After (color/width) " + bps.color.getAWTColor().toString() + " " + bps.width);
            int sy = starty + height;
            graphics.setColor(bps.color.getAWTColor());
            System.out.println("Draw from (" + startx + "," + (sy - borderWidth / 2) + 
                ") to (" + (startx+width) + "," + (sy - borderWidth / 2) + ")");
            graphics.drawLine(startx, sy - borderWidth / 2, startx + width,
                sy - borderWidth / 2);
        }
        bps = (BorderProps) block.getTrait(Trait.BORDER_END);
        if (bps != null) {
            int borderWidth = (int) ((bps.width / 1000f) * (scaleFactor / 100f));
            System.out.println("End (color/width) " + bps.color.getAWTColor().toString() + " " + bps.width);
            int sx = startx + width;
            graphics.setColor(bps.color.getAWTColor());
            System.out.println("Draw from (" + (sx - borderWidth / 2) + "," + starty + 
                ") to (" + (sx - borderWidth / 2) + "," + (starty + height) + ")");
            graphics.drawLine(sx - borderWidth / 2, starty, sx - borderWidth / 2, 
                starty + height);
        }
    }
}
