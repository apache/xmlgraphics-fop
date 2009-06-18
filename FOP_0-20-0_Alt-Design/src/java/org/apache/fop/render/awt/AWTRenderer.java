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
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.io.IOException;
import java.util.Map;
import org.apache.fop.apps.FOPException;
import org.apache.fop.area.PageViewport;
import org.apache.fop.render.AbstractRenderer;
import org.apache.fop.render.FontData;
import org.apache.fop.render.awt.viewer.PreviewDialog;
import org.apache.fop.render.awt.viewer.Translator;

/**
 * This is FOP's AWT renderer.
 * In alt-design, this renders Graphics2D objects.
 */
public class AWTRenderer
extends AbstractRenderer
implements Runnable, Printable, Pageable {

    protected double scaleFactor = 100.0;
    protected int pageNumber = 0;
    private BufferedImage currentPageImage = null;
    private GraphicsEnvironment gEnv =
        GraphicsEnvironment.getLocalGraphicsEnvironment();
    
    /**
     * The resource bundle used for AWT messages.
     */
    protected Translator translator = null;

    private Map fontNames = new java.util.Hashtable();
    private Map fontStyles = new java.util.Hashtable();
    private Color saveColor = null;

    protected Fonts fontData = null;
    public FontData getFontData() {
        return fontData;
    }
    /**
     * The preview dialog frame used for display of the documents.
     * Also used as the AWT Component for FontSetup in generating
     * valid font measures.
     */
    protected PreviewDialog frame;

    public AWTRenderer() {
        fontData = new Fonts();
        translator = new Translator();
        //createPreviewDialog();
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

//    public void setupFontInfo(FOTreeControl foTreeControl) {
//        // create a temp Image to test font metrics on
//        fontInfo = (Document) foTreeControl;
//        BufferedImage fontImage =
//            new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
//        FontSetup.setup(fontInfo, fontImage.createGraphics());
//    }

    public void setScaleFactor(double newScaleFactor) {
        scaleFactor = newScaleFactor;
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    private boolean finished = false;

    public synchronized void finish() {
        if (finished) return;
        finished = true;
        notifyAll();
    }

    private synchronized void finishing() {
        while (! finished) {
            try {
                wait();
            } catch (InterruptedException e) {
                // We can go now
                return;
            }
        }
    }

    public void run() {
        // Start the renderer thread.
        finishing();
    }


    // Printable Interface
    public PageFormat getPageFormat(int pos) {
        return null;
    }

    public Printable getPrintable(int pos) {
        return null;
    }

    public int getNumberOfPages() {
        return 1;
    }

    /**
     * Dummy function to keep PreviewDialog happy while W.I.P.
     * @param num
     */
    public void setPageNumber(int num) {
        return;
    }

    public int print(Graphics g, PageFormat format, int pos) {
        return 0;
    }

    private PreviewDialog createPreviewDialog() {
        frame = new PreviewDialog(this);
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

        /** Generates a desired page from the renderer's page viewport vector.
     * @param pageNum the 0-based page number to generate
     *  @return the <code>java.awt.image.BufferedImage</code> corresponding to the page
     *  @throws FOPException in case of an out-of-range page number requested
    */
    public BufferedImage getPageImage(int pageNum) throws FOPException {
        if (pageNum != 1) {
            throw new FOPException("out-of-range page number (" + pageNum
                + ") requested; only 1 page available.");
        }
//        PageViewport pageViewport = (PageViewport) pageViewportList.get(pageNum);
//        Page page = (Page) pageList.get(pageNum);
//
//        Rectangle2D bounds = pageViewport.getViewArea();
//        pageWidth = (int) Math.round(bounds.getWidth() / 1000f );
//        pageHeight = (int) Math.round(bounds.getHeight() / 1000f );
///*
//        System.out.println("(Page) X, Y, Width, Height: " + bounds.getX()
//            + " " + bounds.getY()
//            + " " + bounds.getWidth()
//            + " " + bounds.getHeight());
//*/
//        currentPageImage =
//            new BufferedImage((int)((pageWidth * (int)scaleFactor) / 100),
//                              (int)((pageHeight * (int)scaleFactor) / 100),
//                              BufferedImage.TYPE_INT_RGB);
//
//        Graphics2D graphics = currentPageImage.createGraphics();
//        graphics.setRenderingHint (RenderingHints.KEY_FRACTIONALMETRICS,
//                                   RenderingHints.VALUE_FRACTIONALMETRICS_ON);
//
//        // transform page based on scale factor supplied
//        AffineTransform at = graphics.getTransform();
//        at.scale(scaleFactor / 100.0, scaleFactor / 100.0);
//        graphics.setTransform(at);
//
//        // draw page frame
//        graphics.setColor(Color.white);
//        graphics.fillRect(0, 0, pageWidth, pageHeight);
//        graphics.setColor(Color.black);
//        graphics.drawRect(-1, -1, pageWidth + 2, pageHeight + 2);
//        graphics.drawLine(pageWidth + 2, 0, pageWidth + 2, pageHeight + 2);
//        graphics.drawLine(pageWidth + 3, 1, pageWidth + 3, pageHeight + 3);
//        graphics.drawLine(0, pageHeight + 2, pageWidth + 2, pageHeight + 2);
//        graphics.drawLine(1, pageHeight + 3, pageWidth + 3, pageHeight + 3);
//
//        renderPageAreas(page);
        return currentPageImage;
    }
    
    public void renderPage(PageViewport page)
    throws IOException, FOPException {
    }

}
