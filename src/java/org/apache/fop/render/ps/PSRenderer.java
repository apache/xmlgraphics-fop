/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

package org.apache.fop.render.ps;

// Java
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Source;

// FOP
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.fop.apps.FOPException;
import org.apache.fop.area.Area;
import org.apache.fop.area.BlockViewport;
import org.apache.fop.area.CTM;
import org.apache.fop.area.LineArea;
import org.apache.fop.area.OffDocumentExtensionAttachment;
import org.apache.fop.area.OffDocumentItem;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.RegionViewport;
import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.AbstractTextArea;
import org.apache.fop.area.inline.Character;
import org.apache.fop.area.inline.ForeignObject;
import org.apache.fop.area.inline.Image;
import org.apache.fop.area.inline.InlineParent;
import org.apache.fop.area.inline.Leader;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.extensions.ExtensionAttachment;
import org.apache.fop.fonts.FontSetup;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.image.EPSImage;
import org.apache.fop.image.FopImage;
import org.apache.fop.image.ImageFactory;
import org.apache.fop.image.XMLImage;
import org.apache.fop.render.Graphics2DAdapter;
import org.apache.fop.render.AbstractPathOrientedRenderer;
import org.apache.fop.render.RendererContext;
import org.apache.fop.render.ps.extensions.PSSetupCode;
import org.apache.fop.util.CharUtilities;

import org.w3c.dom.Document;

/**
 * Renderer that renders to PostScript.
 * <br>
 * This class currently generates PostScript Level 2 code. The only exception
 * is the FlateEncode filter which is a Level 3 feature. The filters in use
 * are hardcoded at the moment.
 * <br>
 * This class follows the Document Structuring Conventions (DSC) version 3.0.
 * If anyone modifies this renderer please make
 * sure to also follow the DSC to make it simpler to programmatically modify
 * the generated Postscript files (ex. extract pages etc.).
 * <br>
 * This renderer inserts FOP-specific comments into the PostScript stream which
 * may help certain users to do certain types of post-processing of the output.
 * These comments all start with "%FOP". 
 *
 * @author <a href="mailto:fop-dev@xmlgraphics.apache.org">Apache FOP Development Team</a>
 * @version $Id$
 */
public class PSRenderer extends AbstractPathOrientedRenderer {

    /** The MIME type for PostScript */
    public static final String MIME_TYPE = "application/postscript";

    /** The application producing the PostScript */
    private int currentPageNumber = 0;

    private boolean enableComments = true;
    private boolean autoRotateLandscape = false;

    /** The PostScript generator used to output the PostScript */
    protected PSGenerator gen;
    private boolean ioTrouble = false;

    private boolean inTextMode = false;
    private boolean firstPageSequenceReceived = false;

    /** Used to temporarily store PSSetupCode instance until they can be written. */
    private List setupCodeList;

    /** This is a map of PSResource instances of all fonts defined (key: font key) */
    private Map fontResources;
    
    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(Configuration)
     */
    public void configure(Configuration cfg) throws ConfigurationException {
        super.configure(cfg);
        this.autoRotateLandscape = cfg.getChild("auto-rotate-landscape").getValueAsBoolean(false);

        //Font configuration
        List cfgFonts = FontSetup.buildFontListFromConfiguration(cfg);
        if (this.fontList == null) {
            this.fontList = cfgFonts;
        } else {
            this.fontList.addAll(cfgFonts);
        }
    }

    /**
     * @see org.apache.fop.render.Renderer#setUserAgent(FOUserAgent)
     */
    public void setUserAgent(FOUserAgent agent) {
        super.setUserAgent(agent);
    }

    /** @see org.apache.fop.render.Renderer#getGraphics2DAdapter() */
    public Graphics2DAdapter getGraphics2DAdapter() {
        return new PSGraphics2DAdapter(this);
    }

    /**
     * Write out a command
     * @param cmd PostScript command
     */
    protected void writeln(String cmd) {
        try {
            gen.writeln(cmd);
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        }
    }

    /**
     * Central exception handler for I/O exceptions.
     * @param ioe IOException to handle
     */
    protected void handleIOTrouble(IOException ioe) {
        if (!ioTrouble) {
            log.error("Error while writing to target file", ioe);
            ioTrouble = true;
        }
    }

    /**
     * Write out a comment
     * @param comment Comment to write
     */
    protected void comment(String comment) {
        if (this.enableComments) {
            if (comment.startsWith("%")) {
                writeln(comment);
            } else {
                writeln("%" + comment);
            }
        }
    }

    /**
     * Make sure the cursor is in the right place.
     */
    protected void movetoCurrPosition() {
        moveTo(this.currentIPPosition, this.currentBPPosition);
    }

    /** @see org.apache.fop.render.AbstractPathOrientedRenderer#clip() */
    protected void clip() {
        writeln("clip newpath");
    }
    
    /**
     * Clip an area.
     * Write a clipping operation given coordinates in the current
     * transform.
     * @param x the x coordinate
     * @param y the y coordinate
     * @param width the width of the area
     * @param height the height of the area
     */
    protected void clipRect(float x, float y, float width, float height) {
        try {
            gen.defineRect(x, y, width, height);
            clip();
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        }
    }

    /** @see org.apache.fop.render.AbstractPathOrientedRenderer#moveTo(float, float) */
    protected void moveTo(float x, float y) {
        writeln(gen.formatDouble(x) + " " + gen.formatDouble(y) + " M");
    }
    
    /** @see org.apache.fop.render.AbstractPathOrientedRenderer#lineTo(float, float) */
    protected void lineTo(float x, float y) {
        writeln(gen.formatDouble(x) + " " + gen.formatDouble(y) + " lineto");
    }
    
    /** @see org.apache.fop.render.AbstractPathOrientedRenderer#closePath() */
    protected void closePath() {
        writeln("cp");
    }
    
    /** @see org.apache.fop.render.AbstractPathOrientedRenderer */
    protected void fillRect(float x, float y, float width, float height) {
        if (width != 0 && height != 0) {
            try {
                gen.defineRect(x, y, width, height);
                gen.writeln("fill");
            } catch (IOException ioe) {
                handleIOTrouble(ioe);
            }
        }
    }

    /** @see org.apache.fop.render.AbstractPathOrientedRenderer */
    protected void updateColor(ColorType col, boolean fill) {
        try {
            useColor(col);
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        }
    }

    /** @see org.apache.fop.render.AbstractPathOrientedRenderer */
    protected void drawImage(String url, Rectangle2D pos) {
        endTextObject();
        url = ImageFactory.getURL(url);
        ImageFactory fact = ImageFactory.getInstance();
        FopImage fopimage = fact.getImage(url, userAgent);
        if (fopimage == null) {
            return;
        }
        if (!fopimage.load(FopImage.DIMENSIONS)) {
            return;
        }
        float x = (float)pos.getX() / 1000f;
        x += currentIPPosition / 1000f;
        float y = (float)pos.getY() / 1000f;
        y += currentBPPosition / 1000f;
        float w = (float)pos.getWidth() / 1000f;
        float h = (float)pos.getHeight() / 1000f;
        try {
            String mime = fopimage.getMimeType();
            if ("text/xml".equals(mime)) {
                if (!fopimage.load(FopImage.ORIGINAL_DATA)) {
                    return;
                }
                Document doc = ((XMLImage) fopimage).getDocument();
                String ns = ((XMLImage) fopimage).getNameSpace();

                renderDocument(doc, ns, pos);
            } else if ("image/svg+xml".equals(mime)) {
                if (!fopimage.load(FopImage.ORIGINAL_DATA)) {
                    return;
                }
                Document doc = ((XMLImage) fopimage).getDocument();
                String ns = ((XMLImage) fopimage).getNameSpace();

                renderDocument(doc, ns, pos);
            } else if (fopimage instanceof EPSImage) {
                PSImageUtils.renderEPS((EPSImage)fopimage, x, y, w, h, gen);
            } else {
                PSImageUtils.renderBitmapImage(fopimage, x, y, w, h, gen);
            }
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        }
    }

    /**
     * Draw a line.
     *
     * @param startx the start x position
     * @param starty the start y position
     * @param endx the x end position
     * @param endy the y end position
     */
    private void drawLine(float startx, float starty, float endx, float endy) {
        writeln(gen.formatDouble(startx) + " " 
                + gen.formatDouble(starty) + " M " 
                + gen.formatDouble(endx) + " " 
                + gen.formatDouble(endy) + " lineto stroke newpath");
    }
    
    /** Saves the graphics state of the rendering engine. */
    public void saveGraphicsState() {
        endTextObject();
        try {
            //delegate
            gen.saveGraphicsState();
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        }
    }

    /** Restores the last graphics state of the rendering engine. */
    public void restoreGraphicsState() {
        try {
            //delegate
            gen.restoreGraphicsState();
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        }
    }

    /**
     * Concats the transformation matrix.
     * @param a A part
     * @param b B part
     * @param c C part
     * @param d D part
     * @param e E part
     * @param f F part
     */
    protected void concatMatrix(double a, double b,
                                double c, double d,
                                double e, double f) {
        try {
            gen.concatMatrix(a, b, c, d, e, f);
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        }
    }

    /**
     * Concats the transformations matrix.
     * @param matrix Matrix to use
     */
    protected void concatMatrix(double[] matrix) {
        try {
            gen.concatMatrix(matrix);
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        }
    }

    /**
     * Changes the currently used font.
     * @param name name of the font
     * @param size font size
     */
    public void useFont(String name, int size) {
        try {
            gen.useFont(name, size / 1000f);
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        }
    }

    private void useColor(ColorType col) throws IOException {
        gen.useRGBColor(toColor(col));
    }

    /** @see org.apache.fop.render.AbstractPathOrientedRenderer#drawBackAndBorders(
     * Area, float, float, float, float) */
    protected void drawBackAndBorders(Area area, float startx, float starty,
            float width, float height) {
        if (area.hasTrait(Trait.BACKGROUND)
                || area.hasTrait(Trait.BORDER_BEFORE)
                || area.hasTrait(Trait.BORDER_AFTER)
                || area.hasTrait(Trait.BORDER_START)
                || area.hasTrait(Trait.BORDER_END)) {
            comment("%FOPBeginBackgroundAndBorder: " 
                    + startx + " " + starty + " " + width + " " + height);
            super.drawBackAndBorders(area, startx, starty, width, height);
            comment("%FOPEndBackgroundAndBorder"); 
        }
    }
    
    /** @see org.apache.fop.render.AbstractPathOrientedRenderer */
    protected void drawBorderLine(float x1, float y1, float x2, float y2, 
            boolean horz, boolean startOrBefore, int style, ColorType col) {
        try {
            float w = x2 - x1;
            float h = y2 - y1;
            if ((w < 0) || (h < 0)) {
                log.error("Negative extent received. Border won't be painted.");
                return;
            }
            switch (style) {
                case Constants.EN_DASHED: 
                    useColor(col);
                    if (horz) {
                        float unit = Math.abs(2 * h);
                        int rep = (int)(w / unit);
                        if (rep % 2 == 0) {
                            rep++;
                        }
                        unit = w / rep;
                        gen.useDash("[" + unit + "] 0");
                        gen.useLineCap(0);
                        gen.useLineWidth(h);
                        float ym = y1 + (h / 2);
                        drawLine(x1, ym, x2, ym);
                    } else {
                        float unit = Math.abs(2 * w);
                        int rep = (int)(h / unit);
                        if (rep % 2 == 0) {
                            rep++;
                        }
                        unit = h / rep;
                        gen.useDash("[" + unit + "] 0");
                        gen.useLineCap(0);
                        gen.useLineWidth(w);
                        float xm = x1 + (w / 2);
                        drawLine(xm, y1, xm, y2);
                    }
                    break;
                case Constants.EN_DOTTED:
                    useColor(col);
                    gen.useLineCap(1); //Rounded!
                    if (horz) {
                        float unit = Math.abs(2 * h);
                        int rep = (int)(w / unit);
                        if (rep % 2 == 0) {
                            rep++;
                        }
                        unit = w / rep;
                        gen.useDash("[0 " + unit + "] 0");
                        gen.useLineWidth(h);
                        float ym = y1 + (h / 2);
                        drawLine(x1, ym, x2, ym);
                    } else {
                        float unit = Math.abs(2 * w);
                        int rep = (int)(h / unit);
                        if (rep % 2 == 0) {
                            rep++;
                        }
                        unit = h / rep;
                        gen.useDash("[0 " + unit + "] 0");
                        gen.useLineWidth(w);
                        float xm = x1 + (w / 2);
                        drawLine(xm, y1, xm, y2);
                    }
                    break;
                case Constants.EN_DOUBLE:
                    useColor(col);
                    gen.useDash(null);
                    if (horz) {
                        float h3 = h / 3;
                        gen.useLineWidth(h3);
                        float ym1 = y1 + (h3 / 2);
                        float ym2 = ym1 + h3 + h3;
                        drawLine(x1, ym1, x2, ym1);
                        drawLine(x1, ym2, x2, ym2);
                    } else {
                        float w3 = w / 3;
                        gen.useLineWidth(w3);
                        float xm1 = x1 + (w3 / 2);
                        float xm2 = xm1 + w3 + w3;
                        drawLine(xm1, y1, xm1, y2);
                        drawLine(xm2, y1, xm2, y2);
                    }
                    break;
                case Constants.EN_GROOVE:
                case Constants.EN_RIDGE:
                    float colFactor = (style == EN_GROOVE ? 0.4f : -0.4f);
                    gen.useDash(null);
                    Color c = toColor(col);
                    if (horz) {
                        Color uppercol = lightenColor(c, -colFactor);
                        Color lowercol = lightenColor(c, colFactor);
                        float h3 = h / 3;
                        gen.useLineWidth(h3);
                        float ym1 = y1 + (h3 / 2);
                        gen.useRGBColor(uppercol);
                        drawLine(x1, ym1, x2, ym1);
                        gen.useRGBColor(c);
                        drawLine(x1, ym1 + h3, x2, ym1 + h3);
                        gen.useRGBColor(lowercol);
                        drawLine(x1, ym1 + h3 + h3, x2, ym1 + h3 + h3);
                    } else {
                        Color leftcol = lightenColor(c, -colFactor);
                        Color rightcol = lightenColor(c, colFactor);
                        float w3 = w / 3;
                        gen.useLineWidth(w3);
                        float xm1 = x1 + (w3 / 2);
                        gen.useRGBColor(leftcol);
                        drawLine(xm1, y1, xm1, y2);
                        gen.useRGBColor(c);
                        drawLine(xm1 + w3, y1, xm1 + w3, y2);
                        gen.useRGBColor(rightcol);
                        drawLine(xm1 + w3 + w3, y1, xm1 + w3 + w3, y2);
                    }
                    break;
                case Constants.EN_INSET:
                case Constants.EN_OUTSET:
                    colFactor = (style == EN_OUTSET ? 0.4f : -0.4f);
                    gen.useDash(null);
                    c = toColor(col);
                    if (horz) {
                        c = lightenColor(c, (startOrBefore ? 1 : -1) * colFactor);
                        gen.useLineWidth(h);
                        float ym1 = y1 + (h / 2);
                        gen.useRGBColor(c);
                        drawLine(x1, ym1, x2, ym1);
                    } else {
                        c = lightenColor(c, (startOrBefore ? 1 : -1) * colFactor);
                        gen.useLineWidth(w);
                        float xm1 = x1 + (w / 2);
                        gen.useRGBColor(c);
                        drawLine(xm1, y1, xm1, y2);
                    }
                    break;
                case Constants.EN_HIDDEN:
                    break;
                default:
                    useColor(col);
                    gen.useDash(null);
                    gen.useLineCap(0);
                    if (horz) {
                        gen.useLineWidth(h);
                        float ym = y1 + (h / 2);
                        drawLine(x1, ym, x2, ym);
                    } else {
                        gen.useLineWidth(w);
                        float xm = x1 + (w / 2);
                        drawLine(xm, y1, xm, y2);
                    }
            }
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        }
    }
    
    /**
     * @see org.apache.fop.render.Renderer#startRenderer(OutputStream)
     */
    public void startRenderer(OutputStream outputStream)
                throws IOException {
        log.debug("rendering areas to PostScript");

        //Setup for PostScript generation
        this.gen = new PSGenerator(outputStream) {
            /** Need to subclass PSGenerator to have better URI resolution */
            public Source resolveURI(String uri) {
                return userAgent.resolveURI(uri);
            }
        };
        this.currentPageNumber = 0;

        //PostScript Header
        writeln(DSCConstants.PS_ADOBE_30);
        gen.writeDSCComment(DSCConstants.CREATOR, new String[] {userAgent.getProducer()});
        gen.writeDSCComment(DSCConstants.CREATION_DATE, new Object[] {new java.util.Date()});
        gen.writeDSCComment(DSCConstants.LANGUAGE_LEVEL, new Integer(gen.getPSLevel()));
        gen.writeDSCComment(DSCConstants.PAGES, new Object[] {PSGenerator.ATEND});
        gen.writeDSCComment(DSCConstants.DOCUMENT_SUPPLIED_RESOURCES, 
                new Object[] {PSGenerator.ATEND});
        gen.writeDSCComment(DSCConstants.END_COMMENTS);

        //Defaults
        gen.writeDSCComment(DSCConstants.BEGIN_DEFAULTS);
        gen.writeDSCComment(DSCConstants.END_DEFAULTS);

        //Prolog and Setup written right before the first page-sequence, see startPageSequence()
    }

    /**
     * @see org.apache.fop.render.Renderer#stopRenderer()
     */
    public void stopRenderer() throws IOException {
        //Notify resource usage for font which are not supplied
        Map fonts = fontInfo.getUsedFonts();
        Iterator e = fonts.keySet().iterator();
        while (e.hasNext()) {
            String key = (String)e.next();
            //Typeface font = (Typeface)fonts.get(key);
            PSResource res = (PSResource)this.fontResources.get(key);
            boolean supplied = gen.isResourceSupplied(res);
            if (!supplied) {
                gen.notifyResourceUsage(res, true);
            }
        }
        
        //Write trailer
        gen.writeDSCComment(DSCConstants.TRAILER);
        gen.writeDSCComment(DSCConstants.PAGES, new Integer(this.currentPageNumber));
        gen.writeResources(false);
        gen.writeDSCComment(DSCConstants.EOF);
        gen.flush();
    }

    /** @see org.apache.fop.render.Renderer */
    public void processOffDocumentItem(OffDocumentItem oDI) {
        log.debug("Handling OffDocumentItem: " + oDI.getName());
        if (oDI instanceof OffDocumentExtensionAttachment) {
            ExtensionAttachment attachment = ((OffDocumentExtensionAttachment)oDI).getAttachment();
            if (PSSetupCode.CATEGORY.equals(attachment.getCategory())) {
                PSSetupCode setupCode = (PSSetupCode)attachment;
                if (setupCodeList == null) {
                    setupCodeList = new java.util.ArrayList();
                }
                setupCodeList.add(setupCode);
            }
        }
        super.processOffDocumentItem(oDI);
    }
    
    /** @see org.apache.fop.render.Renderer#startPageSequence(org.apache.fop.area.LineArea) */
    public void startPageSequence(LineArea seqTitle) {
        super.startPageSequence(seqTitle);
        if (!firstPageSequenceReceived) {
            //Do this only once, as soon as we have all the content for the Setup section!
            try {
                //Prolog
                gen.writeDSCComment(DSCConstants.BEGIN_PROLOG);
                PSProcSets.writeFOPStdProcSet(gen);
                PSProcSets.writeFOPEPSProcSet(gen);
                gen.writeDSCComment(DSCConstants.END_PROLOG);

                //Setup
                gen.writeDSCComment(DSCConstants.BEGIN_SETUP);
                writeSetupCodeList(setupCodeList, "SetupCode");
                this.fontResources = PSFontUtils.writeFontDict(gen, fontInfo);
                gen.writeln("FOPFonts begin");
                gen.writeDSCComment(DSCConstants.END_SETUP);
            } catch (IOException ioe) {
                handleIOTrouble(ioe);
            }
            
            firstPageSequenceReceived = true;
        }
    }
    
    /**
     * Formats and writes a List of PSSetupCode instances to the output stream.
     * @param setupCodeList a List of PSSetupCode instances
     * @param type the type of code section
     */
    private void writeSetupCodeList(List setupCodeList, String type) throws IOException {
        if (setupCodeList != null) {
            Iterator i = setupCodeList.iterator();
            while (i.hasNext()) {
                PSSetupCode setupCode = (PSSetupCode)i.next();
                gen.commentln("%FOPBegin" + type + ": (" 
                        + (setupCode.getName() != null ? setupCode.getName() : "") 
                        + ")");
                LineNumberReader reader = new LineNumberReader(
                        new java.io.StringReader(setupCode.getContent()));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.length() > 0) {
                        gen.writeln(line.trim());
                    }
                }
                gen.commentln("%FOPEnd" + type);
                i.remove();
            }
        }
    }

    /**
     * @see org.apache.fop.render.Renderer#renderPage(PageViewport)
     */
    public void renderPage(PageViewport page)
            throws IOException, FOPException {
        log.debug("renderPage(): " + page);

        this.currentPageNumber++;
        gen.notifyStartNewPage();
        gen.notifyResourceUsage(PSProcSets.STD_PROCSET, false);
        gen.writeDSCComment(DSCConstants.PAGE, new Object[]
                {page.getPageNumberString(),
                 new Integer(this.currentPageNumber)});
        final Integer zero = new Integer(0);
        final long pagewidth = Math.round(page.getViewArea().getWidth());
        final long pageheight = Math.round(page.getViewArea().getHeight());
        final double pspagewidth = pagewidth / 1000f;
        final double pspageheight = pageheight / 1000f;
        boolean rotate = false;
        if (this.autoRotateLandscape && (pageheight < pagewidth)) {
            rotate = true;
            gen.writeDSCComment(DSCConstants.PAGE_BBOX, new Object[]
                    {zero,
                     zero,
                     new Long(Math.round(pspageheight)),
                     new Long(Math.round(pspagewidth))});
            gen.writeDSCComment(DSCConstants.PAGE_HIRES_BBOX, new Object[]
                    {zero,
                     zero,
                     new Double(pspageheight),
                     new Double(pspagewidth)});
            gen.writeDSCComment(DSCConstants.PAGE_ORIENTATION, "Landscape");
        } else {
            gen.writeDSCComment(DSCConstants.PAGE_BBOX, new Object[]
                    {zero,
                     zero,
                     new Long(Math.round(pspagewidth)),
                     new Long(Math.round(pspageheight))});
            gen.writeDSCComment(DSCConstants.PAGE_HIRES_BBOX, new Object[]
                    {zero,
                     zero,
                     new Double(pspagewidth),
                     new Double(pspageheight)});
            if (this.autoRotateLandscape) {
                gen.writeDSCComment(DSCConstants.PAGE_ORIENTATION, "Portrait");
            }
        }
        gen.writeDSCComment(DSCConstants.PAGE_RESOURCES, 
                new Object[] {PSGenerator.ATEND});
        gen.commentln("%FOPSimplePageMaster: " + page.getSPM().getMasterName());
        gen.writeDSCComment(DSCConstants.BEGIN_PAGE_SETUP);
        
        //Handle PSSetupCode instances on simple-page-master
        if (page.getSPM().getExtensionAttachments().size() > 0) {
            List list = new java.util.ArrayList();
            //Extract all PSSetupCode instances from the attachment list on the s-p-m
            Iterator i = page.getSPM().getExtensionAttachments().iterator();
            while (i.hasNext()) {
                ExtensionAttachment attachment = (ExtensionAttachment)i.next();
                if (PSSetupCode.CATEGORY.equals(attachment.getCategory())) {
                    list.add(attachment);
                }
            }
            writeSetupCodeList(list, "PageSetupCode");
        }
        
        if (rotate) {
            gen.writeln(Math.round(pspageheight) + " 0 translate");
            gen.writeln("90 rotate");
        }
        gen.writeln("<<");
        gen.writeln("/PageSize [" 
                + Math.round(pspagewidth) + " " 
                + Math.round(pspageheight) + "]");
        gen.writeln("/ImagingBBox null");
        gen.writeln(">> setpagedevice");
        concatMatrix(1, 0, 0, -1, 0, pageheight / 1000f);

        gen.writeDSCComment(DSCConstants.END_PAGE_SETUP);

        //Process page
        super.renderPage(page);

        writeln("showpage");
        gen.writeDSCComment(DSCConstants.PAGE_TRAILER);
        gen.writeResources(true);
        gen.writeDSCComment(DSCConstants.END_PAGE);
    }

    /** @see org.apache.fop.render.AbstractRenderer */
    protected void renderRegionViewport(RegionViewport port) {
        if (port != null) {
            comment("%FOPBeginRegionViewport: " + port.getRegionReference().getRegionName());
            super.renderRegionViewport(port);
            comment("%FOPEndRegionViewport");
        }
    }
    
    /** Indicates the beginning of a text object. */
    protected void beginTextObject() {
        if (!inTextMode) {
            saveGraphicsState();
            writeln("BT");
            inTextMode = true;
        }
    }

    /** Indicates the end of a text object. */
    protected void endTextObject() {
        if (inTextMode) {
            writeln("ET");
            restoreGraphicsState();
            inTextMode = false;
        }
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderCharacter(Character)
     */
    public void renderCharacter(Character ch) {
        String text = ch.getChar();
        renderText(ch, text);
        super.renderCharacter(ch); //Updates IPD
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderText(TextArea)
     */
    public void renderText(TextArea area) {
        String text = area.getText();
        renderText(area, text);
        super.renderText(area); //Updates IPD
    }
    
    private void renderText(AbstractTextArea area, String text) {
        renderInlineAreaBackAndBorders(area);
        String fontname = (String)area.getTrait(Trait.FONT_NAME);
        int fontsize = area.getTraitAsInteger(Trait.FONT_SIZE);

        // This assumes that *all* CIDFonts use a /ToUnicode mapping
        Typeface tf = (Typeface) fontInfo.getFonts().get(fontname);

        //Determine position
        int rx = currentIPPosition + area.getBorderAndPaddingWidthStart();
        int bl = currentBPPosition + area.getOffset() + area.getBaselineOffset();

        useFont(fontname, fontsize);
        ColorType ct = (ColorType)area.getTrait(Trait.COLOR);
        if (ct != null) {
            try {
                useColor(ct);
            } catch (IOException ioe) {
                handleIOTrouble(ioe);
            }
        }
        
        boolean kerningAvailable = false;
        Map kerning = tf.getKerningInfo();
        if (kerning != null && !kerning.isEmpty()) {
            //kerningAvailable = true;
            //TODO Fix me when kerning is supported by the layout engine
            log.warn("Kerning info is available, but kerning is not yet implemented for"
                    + " the PS renderer and not currently supported by the layout engine.");
        }
        
        beginTextObject();
        writeln("1 0 0 -1 " + gen.formatDouble(rx / 1000f) 
                + " " + gen.formatDouble(bl / 1000f) + " Tm");

        int initialSize = text.length();
        initialSize += initialSize / 2;
        StringBuffer sb = new StringBuffer(initialSize);
        int textLen = text.length();
        if (area.getTextLetterSpaceAdjust() == 0 && area.getTextWordSpaceAdjust() == 0) {
            sb.append("(");
            for (int i = 0; i < textLen; i++) {
                final char c = text.charAt(i);
                final char mapped = tf.mapChar(c);
                PSGenerator.escapeChar(mapped, sb);
            }
            sb.append(") t");
        } else {
            sb.append("(");
            int[] offsets = new int[textLen];
            for (int i = 0; i < textLen; i++) {
                final char c = text.charAt(i);
                final char mapped = tf.mapChar(c);
                int wordSpace;
                //TODO Synchronize word space behaviour with TextLayoutManager
                //Check the other renderers, too!
                if (CharUtilities.isAnySpace(mapped)
                        && mapped != CharUtilities.ZERO_WIDTH_SPACE
                        && mapped != CharUtilities.ZERO_WIDTH_NOBREAK_SPACE) {
                    wordSpace = area.getTextWordSpaceAdjust();
                } else {
                    wordSpace = 0;
                }
                int cw = tf.getWidth(mapped, fontsize) / 1000;
                offsets[i] = cw + area.getTextLetterSpaceAdjust() + wordSpace;
                PSGenerator.escapeChar(mapped, sb);
            }
            sb.append(")" + PSGenerator.LF + "[");
            for (int i = 0; i < textLen; i++) {
                if (i > 0) {
                    if (i % 8 == 0) {
                        sb.append(PSGenerator.LF);
                    } else {
                        sb.append(" ");
                    }
                }
                sb.append(gen.formatDouble(offsets[i] / 1000f));
            }
            sb.append("]" + PSGenerator.LF + "xshow");
        }
        writeln(sb.toString());

        renderTextDecoration(tf, fontsize, area, bl, rx);
    }

    /** @see org.apache.fop.render.AbstractPathOrientedRenderer#breakOutOfStateStack() */
    protected List breakOutOfStateStack() {
        try {
            List breakOutList = new java.util.ArrayList();
            PSState state;
            while (true) {
                if (breakOutList.size() == 0) {
                    endTextObject();
                    comment("------ break out!");
                }
                state = gen.getCurrentState();
                if (!gen.restoreGraphicsState()) {
                    break;
                }
                breakOutList.add(0, state); //Insert because of stack-popping
            }
            return breakOutList;
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
            return null;
        }
    }
    
    /** @see org.apache.fop.render.AbstractPathOrientedRenderer */
    protected void restoreStateStackAfterBreakOut(List breakOutList) {
        try {
            comment("------ restoring context after break-out...");
            PSState state;
            Iterator i = breakOutList.iterator();
            while (i.hasNext()) {
                state = (PSState)i.next();
                saveGraphicsState();
                state.reestablish(gen);
            }
            comment("------ done.");
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        }
    }
    
    /**
     * @see org.apache.fop.render.AbstractRenderer#startVParea(CTM, Rectangle2D)
     */
    protected void startVParea(CTM ctm, Rectangle2D clippingRect) {
        saveGraphicsState();
        if (clippingRect != null) {
            clipRect((float)clippingRect.getX() / 1000f, 
                    (float)clippingRect.getY() / 1000f, 
                    (float)clippingRect.getWidth() / 1000f, 
                    (float)clippingRect.getHeight() / 1000f);
        }
        // multiply with current CTM
        final double[] matrix = ctm.toArray();
        matrix[4] /= 1000f;
        matrix[5] /= 1000f;
        concatMatrix(matrix);
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#endVParea()
     */
    protected void endVParea() {
        endTextObject();
        restoreGraphicsState();
    }

    /** @see org.apache.fop.render.AbstractRenderer */
    protected void renderBlockViewport(BlockViewport bv, List children) {
        comment("%FOPBeginBlockViewport: " + bv.toString());
        super.renderBlockViewport(bv, children);
        comment("%FOPEndBlockViewport");
    }
    
    /** @see org.apache.fop.render.AbstractRenderer */
    protected void renderInlineParent(InlineParent ip) {
        super.renderInlineParent(ip);
    }
    
    /**
     * @see org.apache.fop.render.AbstractRenderer#renderLeader(org.apache.fop.area.inline.Leader)
     */
    public void renderLeader(Leader area) {
        renderInlineAreaBackAndBorders(area);

        endTextObject();
        saveGraphicsState();
        int style = area.getRuleStyle();
        float startx = (currentIPPosition + area.getBorderAndPaddingWidthStart()) / 1000f;
        float starty = (currentBPPosition + area.getOffset()) / 1000f;
        float endx = (currentIPPosition + area.getBorderAndPaddingWidthStart() 
                        + area.getIPD()) / 1000f;
        float ruleThickness = area.getRuleThickness() / 1000f;
        ColorType col = (ColorType)area.getTrait(Trait.COLOR);

        try {
            switch (style) {
                case EN_SOLID:
                case EN_DASHED:
                case EN_DOUBLE:
                    drawBorderLine(startx, starty, endx, starty + ruleThickness, 
                            true, true, style, col);
                    break;
                case EN_DOTTED:
                    clipRect(startx, starty, endx - startx, ruleThickness);
                    //This displaces the dots to the right by half a dot's width
                    //TODO There's room for improvement here
                    gen.concatMatrix(1, 0, 0, 1, ruleThickness / 2, 0);
                    drawBorderLine(startx, starty, endx, starty + ruleThickness, 
                            true, true, style, col);
                    break;
                case EN_GROOVE:
                case EN_RIDGE:
                    float half = area.getRuleThickness() / 2000f;
    
                    gen.useRGBColor(lightenColor(toColor(col), 0.6f));
                    moveTo(startx, starty);
                    lineTo(endx, starty);
                    lineTo(endx, starty + 2 * half);
                    lineTo(startx, starty + 2 * half);
                    closePath();
                    gen.writeln(" fill newpath");
                    gen.useRGBColor(toColor(col));
                    if (style == EN_GROOVE) {
                        moveTo(startx, starty);
                        lineTo(endx, starty);
                        lineTo(endx, starty + half);
                        lineTo(startx + half, starty + half);
                        lineTo(startx, starty + 2 * half);
                    } else {
                        moveTo(endx, starty);
                        lineTo(endx, starty + 2 * half);
                        lineTo(startx, starty + 2 * half);
                        lineTo(startx, starty + half);
                        lineTo(endx - half, starty + half);
                    }
                    closePath();
                    gen.writeln(" fill newpath");
                    break;
                default:
                    throw new UnsupportedOperationException("rule style not supported");
            }
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        }

        restoreGraphicsState();
        super.renderLeader(area);
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderImage(Image, Rectangle2D)
     */
    public void renderImage(Image image, Rectangle2D pos) {
        drawImage(image.getURL(), pos);
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderForeignObject(ForeignObject, Rectangle2D)
     */
    public void renderForeignObject(ForeignObject fo, Rectangle2D pos) {
        Document doc = fo.getDocument();
        String ns = fo.getNameSpace();
        renderDocument(doc, ns, pos);
    }

    /**
     * Renders an XML document (SVG for example).
     * @param doc DOM Document containing the XML document to be rendered
     * @param ns Namespace for the XML document
     * @param pos Position for the generated graphic/image
     */
    public void renderDocument(Document doc, String ns, Rectangle2D pos) {
        endTextObject();
        RendererContext context;
        context = new RendererContext(this, MIME_TYPE);
        context.setUserAgent(userAgent);

        context.setProperty(PSRendererContextConstants.PS_GENERATOR, this.gen);
        context.setProperty(PSRendererContextConstants.PS_FONT_INFO, fontInfo);
        context.setProperty(PSRendererContextConstants.WIDTH,
                            new Integer((int) pos.getWidth()));
        context.setProperty(PSRendererContextConstants.HEIGHT,
                            new Integer((int) pos.getHeight()));
        context.setProperty(PSRendererContextConstants.XPOS,
                            new Integer(currentIPPosition + (int) pos.getX()));
        context.setProperty(PSRendererContextConstants.YPOS,
                            new Integer(currentBPPosition + (int) pos.getY()));
        
        renderXML(context, doc, ns);
    }

    /** @see org.apache.fop.render.AbstractRenderer */
    public String getMimeType() {
        return MIME_TYPE;
    }

}
