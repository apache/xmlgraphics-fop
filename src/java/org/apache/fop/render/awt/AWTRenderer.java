/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.render.awt;

/*
 * originally contributed by
 * Juergen Verwohlt: Juergen.Verwohlt@jCatalog.com,
 * Rainer Steinkuhle: Rainer.Steinkuhle@jCatalog.com,
 * Stanislav Gorkhover: Stanislav.Gorkhover@jCatalog.com
 */

// Java
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Vector;

import org.apache.fop.fonts.FontInfo;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.InputHandler;
import org.apache.fop.area.Area;
import org.apache.fop.area.Page;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.RegionViewport;
import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.fo.FOTreeControl;
import org.apache.fop.image.FopImage;
import org.apache.fop.image.ImageFactory;
import org.apache.fop.render.AbstractRenderer;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.render.awt.FontMetricsMapper;
import org.apache.fop.render.awt.viewer.PreviewDialog;
import org.apache.fop.render.awt.viewer.Translator;

/**
 * This is FOP's AWT renderer.
 */
public class AWTRenderer extends AbstractRenderer implements Printable, Pageable {

    protected double scaleFactor = 100.0;
    protected int pageNumber = 0;
    private int pageWidth = 0;
    private int pageHeight = 0;
    private Vector pageViewportList = new java.util.Vector();
    private Vector pageList = new java.util.Vector();
    private Vector bufferedImageList = new java.util.Vector();
    private BufferedImage currentPageImage = null;
    
    /** Font configuration */
    protected FontInfo fontInfo;

    /**
        The InputHandler associated with this Renderer.
        Sent to the PreviewDialog for document reloading.
    */
    private InputHandler inputHandler;

    /**
     * The resource bundle used for AWT messages.
     */
    protected Translator translator = null;

    private Map fontNames = new java.util.Hashtable();
    private Map fontStyles = new java.util.Hashtable();
    private Color saveColor = null;

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

    /**
     * @see org.apache.fop.render.Renderer
     */
    public boolean supportsOutOfOrder() {
        return false;
    }

    public Translator getTranslator() {
        return translator;
    }

    public void setupFontInfo(FontInfo inFontInfo) {
        // create a temp Image to test font metrics on
        fontInfo = inFontInfo;
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
        bufferedImageList.removeAllElements();
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
        bufferedImageList.add(getPageImage(pageViewport));
    }

    public BufferedImage getBufferedPageImage(int pageNum) throws FOPException {
        return (BufferedImage) bufferedImageList.get(pageNum);
    }

    /** Generates a desired page from the renderer's page viewport vector.
     * @param pageNum the 0-based page number to generate
     *  @return the <code>java.awt.image.BufferedImage</code> corresponding to the page
     *  @throws FOPException in case of an out-of-range page number requested
    */
    public BufferedImage getPageImage(PageViewport pageViewport) throws FOPException {
        Page page = pageViewport.getPage();

        Rectangle2D bounds = pageViewport.getViewArea();
        pageWidth = (int) Math.round(bounds.getWidth() / 1000f );
        pageHeight = (int) Math.round(bounds.getHeight() / 1000f );
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

        renderPageAreas(page);
        return currentPageImage;
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
        pageWidth = (int) Math.round(bounds.getWidth() / 1000f );
        pageHeight = (int) Math.round(bounds.getHeight() / 1000f );
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
        Rectangle2D viewArea = region.getViewArea();

        int startX = (int) Math.round((viewArea.getX() / 1000f)
            * (scaleFactor / 100f));
        int startY = (int) Math.round((viewArea.getY() / 1000f)
            * (scaleFactor / 100f));
        // for rounding to work correctly, need to take into account
        // fractional portion of X and Y.
        int width = (int) Math.round(((viewArea.getX() + viewArea.getWidth()) / 1000f)
            * (scaleFactor / 100f)) - startX;
        int height = (int) Math.round(((viewArea.getY() + viewArea.getHeight()) / 1000f)
            * (scaleFactor / 100f)) - startY;

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
                if (fopimage != null && fopimage.load(FopImage.DIMENSIONS, userAgent.getLogger())) {
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
            int borderWidth = (int) Math.round((bps.width / 1000f) * (scaleFactor / 100f));
            graphics.setColor(bps.color.getAWTColor());
            graphics.fillRect(startx, starty, width, borderWidth);
        }
        bps = (BorderProps) block.getTrait(Trait.BORDER_AFTER);
        if (bps != null) {
            int borderWidth = (int) Math.round((bps.width / 1000f) * (scaleFactor / 100f));
            int sy = starty + height;
            graphics.setColor(bps.color.getAWTColor());
            graphics.fillRect(startx, starty + height - borderWidth, 
                width, borderWidth);
        }
        bps = (BorderProps) block.getTrait(Trait.BORDER_START);
        if (bps != null) {
            int borderWidth = (int) Math.round((bps.width / 1000f) * (scaleFactor / 100f));
            graphics.setColor(bps.color.getAWTColor());
            graphics.fillRect(startx, starty, borderWidth, height);
        }
        bps = (BorderProps) block.getTrait(Trait.BORDER_END);
        if (bps != null) {
            int borderWidth = (int) Math.round((bps.width / 1000f) * (scaleFactor / 100f));
            int sx = startx + width;
            graphics.setColor(bps.color.getAWTColor());
            graphics.fillRect(startx + width - borderWidth, starty, 
                borderWidth, height);
        }
        
    }
    
    /**
     * @see org.apache.fop.render.Renderer#renderText(TextArea)
     */
    public void renderText(TextArea text) {
        System.out.println("In render text: " + text.getTextArea());

        Graphics2D graphics = currentPageImage.createGraphics();
        String fontName = (String) text.getTrait(Trait.FONT_NAME);
        int size = ((Integer) text.getTrait(Trait.FONT_SIZE)).intValue();
//      Typeface f = (Typeface) fontInfo.getFonts().get(fontName);
        ColorType ct = (ColorType) text.getTrait(Trait.COLOR);

        FontMetricsMapper mapper = (FontMetricsMapper) 
            fontInfo.getMetricsFor(fontName);
        if (mapper == null) {
            mapper = new FontMetricsMapper("MonoSpaced", java.awt.Font.PLAIN,
                graphics);
        }

//      graphics.setColor(ct.getAWTColor());
//      graphics.setFont(mapper.getFont(size));
        graphics.setColor(java.awt.Color.black);
        graphics.setFont(new java.awt.Font("monospaced", java.awt.Font.PLAIN,
            10));
        
        int rx = currentBlockIPPosition;
        int bl = currentBPPosition + text.getOffset();

        int newx = (int) (rx + 500) / 1000;
        int newy = (int) (pageHeight - (bl + 500) / 1000);
                
        String s = text.getTextArea();
//      graphics.drawString(s, newx, newy);
        graphics.drawString(s, 220, 200);

        // TODO: render text decorations
        currentBlockIPPosition += text.getWidth();
    }
}
