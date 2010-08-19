/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Source;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.util.ImageUtil;
import org.apache.xmlgraphics.ps.DSCConstants;
import org.apache.xmlgraphics.ps.PSDictionary;
import org.apache.xmlgraphics.ps.PSDictionaryFormatException;
import org.apache.xmlgraphics.ps.PSGenerator;
import org.apache.xmlgraphics.ps.PSPageDeviceDictionary;
import org.apache.xmlgraphics.ps.PSProcSets;
import org.apache.xmlgraphics.ps.PSResource;
import org.apache.xmlgraphics.ps.PSState;
import org.apache.xmlgraphics.ps.dsc.DSCException;
import org.apache.xmlgraphics.ps.dsc.ResourceTracker;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentBoundingBox;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentHiResBoundingBox;

import org.apache.fop.ResourceEventProducer;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.area.Area;
import org.apache.fop.area.BlockViewport;
import org.apache.fop.area.CTM;
import org.apache.fop.area.OffDocumentExtensionAttachment;
import org.apache.fop.area.OffDocumentItem;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.RegionViewport;
import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.AbstractTextArea;
import org.apache.fop.area.inline.Image;
import org.apache.fop.area.inline.InlineParent;
import org.apache.fop.area.inline.Leader;
import org.apache.fop.area.inline.SpaceArea;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.area.inline.WordArea;
import org.apache.fop.datatypes.URISpecification;
import org.apache.fop.fo.extensions.ExtensionAttachment;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.LazyFont;
import org.apache.fop.fonts.SingleByteFont;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.render.AbstractPathOrientedRenderer;
import org.apache.fop.render.Graphics2DAdapter;
import org.apache.fop.render.ImageAdapter;
import org.apache.fop.render.ImageHandler;
import org.apache.fop.render.ImageHandlerRegistry;
import org.apache.fop.render.RendererContext;
import org.apache.fop.render.RendererEventProducer;
import org.apache.fop.render.ps.extensions.PSCommentAfter;
import org.apache.fop.render.ps.extensions.PSCommentBefore;
import org.apache.fop.render.ps.extensions.PSExtensionAttachment;
import org.apache.fop.render.ps.extensions.PSSetPageDevice;
import org.apache.fop.render.ps.extensions.PSSetupCode;
import org.apache.fop.traits.RuleStyle;
import org.apache.fop.util.CharUtilities;

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
public class PSRenderer extends AbstractPathOrientedRenderer
            implements ImageAdapter, PSSupportedFlavors, PSConfigurationConstants {

    /** logging instance */
    private static Log log = LogFactory.getLog(PSRenderer.class);

    /** The MIME type for PostScript */
    public static final String MIME_TYPE = "application/postscript";

    /** The application producing the PostScript */
    private int currentPageNumber = 0;

    /** the OutputStream the PS file is written to */
    private OutputStream outputStream;
    /** the temporary file in case of two-pass processing */
    private File tempFile;

    /** The PostScript generator used to output the PostScript */
    protected PSGenerator gen;
    private boolean ioTrouble = false;

    private boolean inTextMode = false;

    /** Used to temporarily store PSSetupCode instance until they can be written. */
    private List setupCodeList;

    /** This is a map of PSResource instances of all fonts defined (key: font key) */
    private Map fontResources;
    /** This is a map of PSResource instances of all forms (key: uri) */
    private Map formResources;

    /** encapsulation of dictionary used in setpagedevice instruction **/
    private PSPageDeviceDictionary pageDeviceDictionary;

    /**
     * Utility class which enables all sorts of features that are not directly connected to the
     * normal rendering process.
     */
    protected PSRenderingUtil psUtil;
    private PSBorderPainter borderPainter;

    /** Is used to determine the document's bounding box */
    private Rectangle2D documentBoundingBox;

    /** This is a collection holding all document header comments */
    private Collection headerComments;

    /** This is a collection holding all document footer comments */
    private Collection footerComments;

    /** {@inheritDoc} */
    public void setUserAgent(FOUserAgent agent) {
        super.setUserAgent(agent);
        this.psUtil = new PSRenderingUtil(getUserAgent());
    }

    PSRenderingUtil getPSUtil() {
        return this.psUtil;
    }

    /**
     * Sets the landscape mode for this renderer.
     * @param value false will normally generate a "pseudo-portrait" page, true will rotate
     *              a "wider-than-long" page by 90 degrees.
     */
    public void setAutoRotateLandscape(boolean value) {
        getPSUtil().setAutoRotateLandscape(value);
    }

    /** @return true if the renderer is configured to rotate landscape pages */
    public boolean isAutoRotateLandscape() {
        return getPSUtil().isAutoRotateLandscape();
    }

    /**
     * Sets the PostScript language level that the renderer should produce.
     * @param level the language level (currently allowed: 2 or 3)
     */
    public void setLanguageLevel(int level) {
        getPSUtil().setLanguageLevel(level);
    }

    /**
     * Return the PostScript language level that the renderer produces.
     * @return the language level
     */
    public int getLanguageLevel() {
        return getPSUtil().getLanguageLevel();
    }

    /**
     * Sets the resource optimization mode. If set to true, the renderer does two passes to
     * only embed the necessary resources in the PostScript file. This is slower, but produces
     * smaller files.
     * @param value true to enable the resource optimization
     */
    public void setOptimizeResources(boolean value) {
        getPSUtil().setOptimizeResources(value);
    }

    /** @return true if the renderer does two passes to optimize PostScript resources */
    public boolean isOptimizeResources() {
        return getPSUtil().isOptimizeResources();
    }

    /** {@inheritDoc} */
    public Graphics2DAdapter getGraphics2DAdapter() {
        return new PSGraphics2DAdapter(this);
    }

    /** {@inheritDoc} */
    public ImageAdapter getImageAdapter() {
        return this;
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
            RendererEventProducer eventProducer = RendererEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.ioError(this, ioe);
            ioTrouble = true;
        }
    }

    /**
     * Write out a comment
     * @param comment Comment to write
     */
    protected void comment(String comment) {
        try {
            if (comment.startsWith("%")) {
                gen.commentln(comment);
                writeln(comment);
            } else {
                gen.commentln("%" + comment);
            }
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        }
    }

    /**
     * Make sure the cursor is in the right place.
     */
    protected void movetoCurrPosition() {
        moveTo(this.currentIPPosition, this.currentBPPosition);
    }

    /** {@inheritDoc} */
    protected void clip() {
        writeln("clip newpath");
    }

    /** {@inheritDoc} */
    protected void clipRect(float x, float y, float width, float height) {
        try {
            gen.defineRect(x, y, width, height);
            clip();
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        }
    }

    /** {@inheritDoc} */
    protected void moveTo(float x, float y) {
        writeln(gen.formatDouble(x) + " " + gen.formatDouble(y) + " M");
    }

    /**
     * Moves the current point by (x, y) relative to the current position,
     * omitting any connecting line segment.
     * @param x x coordinate
     * @param y y coordinate
     */
    protected void rmoveTo(float x, float y) {
        writeln(gen.formatDouble(x) + " " + gen.formatDouble(y) + " RM");
    }

    /** {@inheritDoc} */
    protected void lineTo(float x, float y) {
        writeln(gen.formatDouble(x) + " " + gen.formatDouble(y) + " lineto");
    }

    /** {@inheritDoc} */
    protected void closePath() {
        writeln("cp");
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    protected void updateColor(Color col, boolean fill) {
        try {
            useColor(col);
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        }
    }

    /** {@inheritDoc} */
    protected void drawImage(String uri, Rectangle2D pos, Map foreignAttributes) {
        endTextObject();
        int x = currentIPPosition + (int)Math.round(pos.getX());
        int y = currentBPPosition + (int)Math.round(pos.getY());
        uri = URISpecification.getURL(uri);
        if (log.isDebugEnabled()) {
            log.debug("Handling image: " + uri);
        }
        int width = (int)pos.getWidth();
        int height = (int)pos.getHeight();
        Rectangle targetRect = new Rectangle(x, y, width, height);

        ImageManager manager = getUserAgent().getFactory().getImageManager();
        ImageInfo info = null;
        try {
            ImageSessionContext sessionContext = getUserAgent().getImageSessionContext();
            info = manager.getImageInfo(uri, sessionContext);

            PSRenderingContext renderingContext = new PSRenderingContext(
                    getUserAgent(), gen, getFontInfo());

            if (!isOptimizeResources()
                    || PSImageUtils.isImageInlined(info, renderingContext)) {
                if (log.isDebugEnabled()) {
                    log.debug("Image " + info + " is inlined");
                }

                //Determine supported flavors
                ImageFlavor[] flavors;
                ImageHandlerRegistry imageHandlerRegistry
                    = userAgent.getFactory().getImageHandlerRegistry();
                flavors = imageHandlerRegistry.getSupportedFlavors(renderingContext);

                //Only now fully load/prepare the image
                Map hints = ImageUtil.getDefaultHints(sessionContext);
                org.apache.xmlgraphics.image.loader.Image img = manager.getImage(
                        info, flavors, hints, sessionContext);

                //Get handler for image
                ImageHandler basicHandler = imageHandlerRegistry.getHandler(renderingContext, img);

                //...and embed as inline image
                basicHandler.handleImage(renderingContext, img, targetRect);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Image " + info + " is embedded as a form later");
                }
                //Don't load image at this time, just put a form placeholder in the stream
                PSResource form = getFormForImage(info.getOriginalURI());
                PSImageUtils.drawForm(form, info, targetRect, gen);
            }

        } catch (ImageException ie) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.imageError(this, (info != null ? info.toString() : uri), ie, null);
        } catch (FileNotFoundException fe) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.imageNotFound(this, (info != null ? info.toString() : uri), fe, null);
        } catch (IOException ioe) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.imageIOError(this, (info != null ? info.toString() : uri), ioe, null);
        }
    }

    /**
     * Returns a PSResource instance representing a image as a PostScript form.
     * @param uri the image URI
     * @return a PSResource instance
     */
    protected PSResource getFormForImage(String uri) {
        if (uri == null || "".equals(uri)) {
            throw new IllegalArgumentException("uri must not be empty or null");
        }
        if (this.formResources == null) {
            this.formResources = new java.util.HashMap();
        }
        PSResource form = (PSResource)this.formResources.get(uri);
        if (form == null) {
            form = new PSImageFormResource(this.formResources.size() + 1, uri);
            this.formResources.put(uri, form);
        }
        return form;
    }

    /** {@inheritDoc} */
    public void paintImage(RenderedImage image, RendererContext context,
            int x, int y, int width, int height) throws IOException {
        float fx = x / 1000f;
        x += currentIPPosition / 1000f;
        float fy = y / 1000f;
        y += currentBPPosition / 1000f;
        float fw = width / 1000f;
        float fh = height / 1000f;
        PSImageUtils.renderBitmapImage(image, fx, fy, fw, fh, gen);
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
            endTextObject();
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

    /** {@inheritDoc} */
    protected void concatenateTransformationMatrix(AffineTransform at) {
        try {
            gen.concatMatrix(at);
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        }
    }

    private String getPostScriptNameForFontKey(String key) {
        int pos = key.indexOf('_');
        String postFix = null;
        if (pos > 0) {
            postFix = key.substring(pos);
            key = key.substring(0, pos);
        }
        Map fonts = fontInfo.getFonts();
        Typeface tf = (Typeface)fonts.get(key);
        if (tf instanceof LazyFont) {
            tf = ((LazyFont)tf).getRealFont();
        }
        if (tf == null) {
            throw new IllegalStateException("Font not available: " + key);
        }
        if (postFix == null) {
            return tf.getFontName();
        } else {
            return tf.getFontName() + postFix;
        }
    }

    /**
     * Returns the PSResource for the given font key.
     * @param key the font key ("F*")
     * @return the matching PSResource
     */
    protected PSResource getPSResourceForFontKey(String key) {
        PSResource res = null;
        if (this.fontResources != null) {
            res = (PSResource)this.fontResources.get(key);
        } else {
            this.fontResources = new java.util.HashMap();
        }
        if (res == null) {
            res = new PSResource(PSResource.TYPE_FONT, getPostScriptNameForFontKey(key));
            this.fontResources.put(key, res);
        }
        return res;
    }

    /**
     * Changes the currently used font.
     * @param key key of the font ("F*")
     * @param size font size
     */
    protected void useFont(String key, int size) {
        try {
            PSResource res = getPSResourceForFontKey(key);
            gen.useFont("/" + res.getName(), size / 1000f);
            gen.getResourceTracker().notifyResourceUsageOnPage(res);
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        }
    }

    private void useColor(Color col) throws IOException {
        gen.useColor(col);
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    protected void drawBorderLine                               // CSOK: ParameterNumber
        (float x1, float y1, float x2, float y2,
         boolean horz, boolean startOrBefore, int style, Color col) {
        try {
            PSBorderPainter.drawBorderLine(gen, x1, y1, x2, y2, horz, startOrBefore, style, col);
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        }
    }

    /** {@inheritDoc} */
    public void startRenderer(OutputStream outputStream)
                throws IOException {
        log.debug("Rendering areas to PostScript...");

        this.outputStream = outputStream;
        OutputStream out;
        if (isOptimizeResources()) {
            this.tempFile = File.createTempFile("fop", null);
            out = new java.io.FileOutputStream(this.tempFile);
            out = new java.io.BufferedOutputStream(out);
        } else {
            out = this.outputStream;
        }

        //Setup for PostScript generation
        this.gen = new PSGenerator(out) {
            /** Need to subclass PSGenerator to have better URI resolution */
            public Source resolveURI(String uri) {
                return userAgent.resolveURI(uri);
            }
        };
        this.gen.setPSLevel(getLanguageLevel());
        this.borderPainter = new PSBorderPainter(this.gen);
        this.currentPageNumber = 0;

        //Initial default page device dictionary settings
        this.pageDeviceDictionary = new PSPageDeviceDictionary();
        pageDeviceDictionary.setFlushOnRetrieval(!getPSUtil().isDSCComplianceEnabled());
        pageDeviceDictionary.put("/ImagingBBox", "null");
    }

    private void writeHeader() throws IOException {
        //PostScript Header
        writeln(DSCConstants.PS_ADOBE_30);
        gen.writeDSCComment(DSCConstants.CREATOR, new String[] {userAgent.getProducer()});
        gen.writeDSCComment(DSCConstants.CREATION_DATE, new Object[] {new java.util.Date()});
        gen.writeDSCComment(DSCConstants.LANGUAGE_LEVEL, new Integer(gen.getPSLevel()));
        gen.writeDSCComment(DSCConstants.PAGES, new Object[] {DSCConstants.ATEND});
        gen.writeDSCComment(DSCConstants.BBOX, DSCConstants.ATEND);
        gen.writeDSCComment(DSCConstants.HIRES_BBOX, DSCConstants.ATEND);
        this.documentBoundingBox = new Rectangle2D.Double();
        gen.writeDSCComment(DSCConstants.DOCUMENT_SUPPLIED_RESOURCES,
                new Object[] {DSCConstants.ATEND});
        if (headerComments != null) {
            for (Iterator iter = headerComments.iterator(); iter.hasNext();) {
                PSExtensionAttachment comment = (PSExtensionAttachment)iter.next();
                gen.writeln("%" + comment.getContent());
            }
        }
        gen.writeDSCComment(DSCConstants.END_COMMENTS);

        //Defaults
        gen.writeDSCComment(DSCConstants.BEGIN_DEFAULTS);
        gen.writeDSCComment(DSCConstants.END_DEFAULTS);

        //Prolog and Setup written right before the first page-sequence, see startPageSequence()
        //Do this only once, as soon as we have all the content for the Setup section!
        //Prolog
        gen.writeDSCComment(DSCConstants.BEGIN_PROLOG);
        PSProcSets.writeStdProcSet(gen);
        PSProcSets.writeEPSProcSet(gen);
        gen.writeDSCComment(DSCConstants.END_PROLOG);

        //Setup
        gen.writeDSCComment(DSCConstants.BEGIN_SETUP);
        PSRenderingUtil.writeSetupCodeList(gen, setupCodeList, "SetupCode");
        if (!isOptimizeResources()) {
            this.fontResources = PSFontUtils.writeFontDict(gen, fontInfo);
        } else {
            gen.commentln("%FOPFontSetup"); //Place-holder, will be replaced in the second pass
        }
        gen.writeDSCComment(DSCConstants.END_SETUP);
    }

    /** {@inheritDoc} */
    public void stopRenderer() throws IOException {
        //Write trailer
        gen.writeDSCComment(DSCConstants.TRAILER);
        if (footerComments != null) {
            for (Iterator iter = footerComments.iterator(); iter.hasNext();) {
                PSExtensionAttachment comment = (PSExtensionAttachment)iter.next();
                gen.commentln("%" + comment.getContent());
            }
            footerComments.clear();
        }
        gen.writeDSCComment(DSCConstants.PAGES, new Integer(this.currentPageNumber));
        new DSCCommentBoundingBox(this.documentBoundingBox).generate(gen);
        new DSCCommentHiResBoundingBox(this.documentBoundingBox).generate(gen);
        gen.getResourceTracker().writeResources(false, gen);
        gen.writeDSCComment(DSCConstants.EOF);
        gen.flush();
        log.debug("Rendering to PostScript complete.");
        if (isOptimizeResources()) {
            IOUtils.closeQuietly(gen.getOutputStream());
            rewritePostScriptFile();
        }
        if (footerComments != null) {
            headerComments.clear();
        }
        if (pageDeviceDictionary != null) {
            pageDeviceDictionary.clear();
        }
        this.borderPainter = null;
        this.gen = null;
    }

    /**
     * Used for two-pass production. This will rewrite the PostScript file from the temporary
     * file while adding all needed resources.
     * @throws IOException In case of an I/O error.
     */
    private void rewritePostScriptFile() throws IOException {
        log.debug("Processing PostScript resources...");
        long startTime = System.currentTimeMillis();
        ResourceTracker resTracker = gen.getResourceTracker();
        InputStream in = new java.io.FileInputStream(this.tempFile);
        in = new java.io.BufferedInputStream(in);
        try {
            try {
                ResourceHandler handler = new ResourceHandler(this.userAgent, this.fontInfo,
                        resTracker, this.formResources);
                handler.process(in, this.outputStream,
                        this.currentPageNumber, this.documentBoundingBox);
                this.outputStream.flush();
            } catch (DSCException e) {
                throw new RuntimeException(e.getMessage());
            }
        } finally {
            IOUtils.closeQuietly(in);
            if (!this.tempFile.delete()) {
                this.tempFile.deleteOnExit();
                log.warn("Could not delete temporary file: " + this.tempFile);
            }
        }
        if (log.isDebugEnabled()) {
            long duration = System.currentTimeMillis() - startTime;
            log.debug("Resource Processing complete in " + duration + " ms.");
        }
    }

    /** {@inheritDoc} */
    public void processOffDocumentItem(OffDocumentItem oDI) {
        if (log.isDebugEnabled()) {
            log.debug("Handling OffDocumentItem: " + oDI.getName());
        }
        if (oDI instanceof OffDocumentExtensionAttachment) {
            ExtensionAttachment attachment = ((OffDocumentExtensionAttachment)oDI).getAttachment();
            if (attachment != null) {
                if (PSExtensionAttachment.CATEGORY.equals(attachment.getCategory())) {
                    if (attachment instanceof PSSetupCode) {
                        if (setupCodeList == null) {
                            setupCodeList = new java.util.ArrayList();
                        }
                        if (!setupCodeList.contains(attachment)) {
                            setupCodeList.add(attachment);
                        }
                    } else if (attachment instanceof PSSetPageDevice) {
                        /**
                         * Extract all PSSetPageDevice instances from the
                         * attachment list on the s-p-m and add all dictionary
                         * entries to our internal representation of the the
                         * page device dictionary.
                         */
                        PSSetPageDevice setPageDevice = (PSSetPageDevice)attachment;
                        String content = setPageDevice.getContent();
                        if (content != null) {
                            try {
                                this.pageDeviceDictionary.putAll(PSDictionary.valueOf(content));
                            } catch (PSDictionaryFormatException e) {
                                PSEventProducer eventProducer = PSEventProducer.Provider.get(
                                        getUserAgent().getEventBroadcaster());
                                eventProducer.postscriptDictionaryParseError(this, content, e);
                            }
                        }
                    } else if (attachment instanceof PSCommentBefore) {
                        if (headerComments == null) {
                            headerComments = new java.util.ArrayList();
                        }
                        headerComments.add(attachment);
                    } else if (attachment instanceof PSCommentAfter) {
                        if (footerComments == null) {
                            footerComments = new java.util.ArrayList();
                        }
                        footerComments.add(attachment);
                    }
                }
            }
        }
        super.processOffDocumentItem(oDI);
    }

    /** {@inheritDoc} */
    public void renderPage(PageViewport page)
            throws IOException, FOPException {
        log.debug("renderPage(): " + page);

        if (this.currentPageNumber == 0) {
            writeHeader();
        }

        this.currentPageNumber++;

        gen.getResourceTracker().notifyStartNewPage();
        gen.getResourceTracker().notifyResourceUsageOnPage(PSProcSets.STD_PROCSET);
        gen.writeDSCComment(DSCConstants.PAGE, new Object[]
                {page.getPageNumberString(),
                 new Integer(this.currentPageNumber)});

        double pageWidth = page.getViewArea().width / 1000f;
        double pageHeight = page.getViewArea().height / 1000f;
        boolean rotate = false;
        List pageSizes = new java.util.ArrayList();
        if (getPSUtil().isAutoRotateLandscape() && (pageHeight < pageWidth)) {
            rotate = true;
            pageSizes.add(new Long(Math.round(pageHeight)));
            pageSizes.add(new Long(Math.round(pageWidth)));
        } else {
            pageSizes.add(new Long(Math.round(pageWidth)));
            pageSizes.add(new Long(Math.round(pageHeight)));
        }
        pageDeviceDictionary.put("/PageSize", pageSizes);

        if (page.hasExtensionAttachments()) {
            for (Iterator iter = page.getExtensionAttachments().iterator();
                iter.hasNext();) {
                ExtensionAttachment attachment = (ExtensionAttachment) iter.next();
                if (attachment instanceof PSSetPageDevice) {
                    /**
                     * Extract all PSSetPageDevice instances from the
                     * attachment list on the s-p-m and add all
                     * dictionary entries to our internal representation
                     * of the the page device dictionary.
                     */
                    PSSetPageDevice setPageDevice = (PSSetPageDevice)attachment;
                    String content = setPageDevice.getContent();
                    if (content != null) {
                        try {
                            pageDeviceDictionary.putAll(PSDictionary.valueOf(content));
                        } catch (PSDictionaryFormatException e) {
                            PSEventProducer eventProducer = PSEventProducer.Provider.get(
                                    getUserAgent().getEventBroadcaster());
                            eventProducer.postscriptDictionaryParseError(this, content, e);
                        }
                    }
                }
            }
        }

        try {
            if (setupCodeList != null) {
                PSRenderingUtil.writeEnclosedExtensionAttachments(gen, setupCodeList);
                setupCodeList.clear();
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        final Integer zero = new Integer(0);
        Rectangle2D pageBoundingBox = new Rectangle2D.Double();
        if (rotate) {
            pageBoundingBox.setRect(0, 0, pageHeight, pageWidth);
            gen.writeDSCComment(DSCConstants.PAGE_BBOX, new Object[] {
                    zero, zero, new Long(Math.round(pageHeight)),
                    new Long(Math.round(pageWidth)) });
            gen.writeDSCComment(DSCConstants.PAGE_HIRES_BBOX, new Object[] {
                    zero, zero, new Double(pageHeight),
                    new Double(pageWidth) });
            gen.writeDSCComment(DSCConstants.PAGE_ORIENTATION, "Landscape");
        } else {
            pageBoundingBox.setRect(0, 0, pageWidth, pageHeight);
            gen.writeDSCComment(DSCConstants.PAGE_BBOX, new Object[] {
                    zero, zero, new Long(Math.round(pageWidth)),
                    new Long(Math.round(pageHeight)) });
            gen.writeDSCComment(DSCConstants.PAGE_HIRES_BBOX, new Object[] {
                    zero, zero, new Double(pageWidth),
                    new Double(pageHeight) });
            if (getPSUtil().isAutoRotateLandscape()) {
                gen.writeDSCComment(DSCConstants.PAGE_ORIENTATION,
                        "Portrait");
            }
        }
        this.documentBoundingBox.add(pageBoundingBox);
        gen.writeDSCComment(DSCConstants.PAGE_RESOURCES,
                new Object[] {DSCConstants.ATEND});

        gen.commentln("%FOPSimplePageMaster: " + page.getSimplePageMasterName());

        gen.writeDSCComment(DSCConstants.BEGIN_PAGE_SETUP);

        if (page.hasExtensionAttachments()) {
            List extensionAttachments = page.getExtensionAttachments();
            for (int i = 0; i < extensionAttachments.size(); i++) {
                Object attObj = extensionAttachments.get(i);
                if (attObj instanceof PSExtensionAttachment) {
                    PSExtensionAttachment attachment = (PSExtensionAttachment)attObj;
                    if (attachment instanceof PSCommentBefore) {
                        gen.commentln("%" + attachment.getContent());
                    } else if (attachment instanceof PSSetupCode) {
                        gen.writeln(attachment.getContent());
                    }
                }
            }
        }

        // Write any unwritten changes to page device dictionary
        if (!pageDeviceDictionary.isEmpty()) {
            String content = pageDeviceDictionary.getContent();
            if (getPSUtil().isSafeSetPageDevice()) {
                content += " SSPD";
            } else {
                content += " setpagedevice";
            }
            PSRenderingUtil.writeEnclosedExtensionAttachment(gen, new PSSetPageDevice(content));
        }

        if (rotate) {
            gen.writeln(Math.round(pageHeight) + " 0 translate");
            gen.writeln("90 rotate");
        }
        concatMatrix(1, 0, 0, -1, 0, pageHeight);

        gen.writeDSCComment(DSCConstants.END_PAGE_SETUP);

        //Process page
        super.renderPage(page);

        //Show page
        gen.showPage();
        gen.writeDSCComment(DSCConstants.PAGE_TRAILER);
        if (page.hasExtensionAttachments()) {
            List extensionAttachments = page.getExtensionAttachments();
            for (int i = 0; i < extensionAttachments.size(); i++) {
                Object attObj = extensionAttachments.get(i);
                if (attObj instanceof PSExtensionAttachment) {
                    PSExtensionAttachment attachment = (PSExtensionAttachment)attObj;
                    if (attachment instanceof PSCommentAfter) {
                        gen.commentln("%" + attachment.getContent());
                    }
                }
            }
        }
        gen.getResourceTracker().writeResources(true, gen);
    }

    /** {@inheritDoc} */
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
            inTextMode = false; //set before restoreGraphicsState() to avoid recursion
            writeln("ET");
            restoreGraphicsState();
        }
    }

    /** {@inheritDoc} */
    public void renderText(TextArea area) {
        renderInlineAreaBackAndBorders(area);
        String fontkey = getInternalFontNameForArea(area);
        int fontsize = area.getTraitAsInteger(Trait.FONT_SIZE);

        // This assumes that *all* CIDFonts use a /ToUnicode mapping
        Typeface tf = (Typeface) fontInfo.getFonts().get(fontkey);

        //Determine position
        int rx = currentIPPosition + area.getBorderAndPaddingWidthStart();
        int bl = currentBPPosition + area.getBlockProgressionOffset() + area.getBaselineOffset();

        Color ct = (Color)area.getTrait(Trait.COLOR);
        if (ct != null) {
            try {
                useColor(ct);
            } catch (IOException ioe) {
                handleIOTrouble(ioe);
            }
        }

        beginTextObject();
        writeln("1 0 0 -1 " + gen.formatDouble(rx / 1000f)
                + " " + gen.formatDouble(bl / 1000f) + " Tm");

        super.renderText(area); //Updates IPD

        renderTextDecoration(tf, fontsize, area, bl, rx);
    }

    /** {@inheritDoc} */
    protected void renderWord(WordArea word) {
        renderText((TextArea)word.getParentArea(), word.getWord(), word.getLetterAdjustArray());
        super.renderWord(word);
    }

    /** {@inheritDoc} */
    protected void renderSpace(SpaceArea space) {
        AbstractTextArea textArea = (AbstractTextArea)space.getParentArea();
        String s = space.getSpace();
        char sp = s.charAt(0);
        Font font = getFontFromArea(textArea);

        int tws = (space.isAdjustable()
                ? ((TextArea) space.getParentArea()).getTextWordSpaceAdjust()
                        + 2 * textArea.getTextLetterSpaceAdjust()
                : 0);

        rmoveTo((font.getCharWidth(sp) + tws) / 1000f, 0);
        super.renderSpace(space);
    }

    private Typeface getTypeface(String fontName) {
        Typeface tf = (Typeface)fontInfo.getFonts().get(fontName);
        if (tf instanceof LazyFont) {
            tf = ((LazyFont)tf).getRealFont();
        }
        return tf;
    }

    private void renderText(AbstractTextArea area, String text, int[] letterAdjust) {
        String fontkey = getInternalFontNameForArea(area);
        int fontSize = area.getTraitAsInteger(Trait.FONT_SIZE);
        Font font = getFontFromArea(area);
        Typeface tf = getTypeface(font.getFontName());
        SingleByteFont singleByteFont = null;
        if (tf instanceof SingleByteFont) {
            singleByteFont = (SingleByteFont)tf;
        }

        int textLen = text.length();
        if (singleByteFont != null && singleByteFont.hasAdditionalEncodings()) {
            int start = 0;
            int currentEncoding = -1;
            for (int i = 0; i < textLen; i++) {
                char c = text.charAt(i);
                char mapped = tf.mapChar(c);
                int encoding = mapped / 256;
                if (currentEncoding != encoding) {
                    if (i > 0) {
                        writeText(area, text, start, i - start, letterAdjust, fontSize, tf);
                    }
                    if (encoding == 0) {
                        useFont(fontkey, fontSize);
                    } else {
                        useFont(fontkey + "_" + Integer.toString(encoding), fontSize);
                    }
                    currentEncoding = encoding;
                    start = i;
                }
            }
            writeText(area, text, start, textLen - start, letterAdjust, fontSize, tf);
        } else {
            useFont(fontkey, fontSize);
            writeText(area, text, 0, textLen, letterAdjust, fontSize, tf);
        }
    }

    private void writeText(AbstractTextArea area, String text, int start, int len,
            int[] letterAdjust, int fontsize, Typeface tf) {
        int end = start + len;
        int initialSize = text.length();
        initialSize += initialSize / 2;
        StringBuffer sb = new StringBuffer(initialSize);
        if (letterAdjust == null
                && area.getTextLetterSpaceAdjust() == 0
                && area.getTextWordSpaceAdjust() == 0) {
            sb.append("(");
            for (int i = start; i < end; i++) {
                final char c = text.charAt(i);
                final char mapped = (char)(tf.mapChar(c) % 256);
                PSGenerator.escapeChar(mapped, sb);
            }
            sb.append(") t");
        } else {
            sb.append("(");
            int[] offsets = new int[len];
            for (int i = start; i < end; i++) {
                final char c = text.charAt(i);
                final char mapped = tf.mapChar(c);
                char codepoint = (char)(mapped % 256);
                int wordSpace;

                if (CharUtilities.isAdjustableSpace(mapped)) {
                    wordSpace = area.getTextWordSpaceAdjust();
                } else {
                    wordSpace = 0;
                }
                int cw = tf.getWidth(mapped, fontsize) / 1000;
                int ladj = (letterAdjust != null && i < end - 1 ? letterAdjust[i + 1] : 0);
                int tls = (i < end - 1 ? area.getTextLetterSpaceAdjust() : 0);
                offsets[i - start] = cw + ladj + tls + wordSpace;
                PSGenerator.escapeChar(codepoint, sb);
            }
            sb.append(")" + PSGenerator.LF + "[");
            for (int i = 0; i < len; i++) {
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
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    protected void endVParea() {
        restoreGraphicsState();
    }

    /** {@inheritDoc} */
    protected void renderBlockViewport(BlockViewport bv, List children) {
        comment("%FOPBeginBlockViewport: " + bv.toString());
        super.renderBlockViewport(bv, children);
        comment("%FOPEndBlockViewport");
    }

    /** {@inheritDoc} */
    protected void renderInlineParent(InlineParent ip) {
        super.renderInlineParent(ip);
    }

    /** {@inheritDoc} */
    public void renderLeader(Leader area) {
        renderInlineAreaBackAndBorders(area);

        int style = area.getRuleStyle();
        int ruleThickness = area.getRuleThickness();
        int startx = currentIPPosition + area.getBorderAndPaddingWidthStart();
        int starty = currentBPPosition + area.getBlockProgressionOffset() + (ruleThickness / 2);
        int endx = currentIPPosition
                        + area.getBorderAndPaddingWidthStart()
                        + area.getIPD();
        Color col = (Color)area.getTrait(Trait.COLOR);

        endTextObject();
        try {
            borderPainter.drawLine(new Point(startx, starty), new Point(endx, starty),
                    ruleThickness, col, RuleStyle.valueOf(style));
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        }

        super.renderLeader(area);
    }

    /**
     * {@inheritDoc}
     */
    public void renderImage(Image image, Rectangle2D pos) {
        drawImage(image.getURL(), pos, image.getForeignAttributes());
    }

    /**
     * {@inheritDoc}
     */
    protected RendererContext createRendererContext(int x, int y, int width, int height,
            Map foreignAttributes) {
        RendererContext context = super.createRendererContext(
                x, y, width, height, foreignAttributes);
        context.setProperty(PSRendererContextConstants.PS_GENERATOR, this.gen);
        context.setProperty(PSRendererContextConstants.PS_FONT_INFO, fontInfo);
        return context;
    }

    /** {@inheritDoc} */
    public String getMimeType() {
        return MIME_TYPE;
    }

    /**
     * Sets whether or not the safe set page device macro should be used
     * (as opposed to directly invoking setpagedevice) when setting the
     * postscript page device.
     *
     * This option is a useful option when you want to guard against the possibility
     * of invalid/unsupported postscript key/values being placed in the page device.
     *
     * @param safeSetPageDevice setting to false and the renderer will make a
     * standard "setpagedevice" call, setting to true will make a safe set page
     * device macro call (default is false).
     */
    public void setSafeSetPageDevice(boolean safeSetPageDevice) {
        getPSUtil().setSafeSetPageDevice(safeSetPageDevice);
    }

    /**
     * Sets whether or not PostScript Document Structuring Conventions (dsc) compliance are
     * enforced.
     * <p>
     * It can cause problems (unwanted PostScript subsystem initgraphics/erasepage calls)
     * on some printers when the pagedevice is set.  If this causes problems on a
     * particular implementation then use this setting with a 'false' value to try and
     * minimize the number of setpagedevice calls in the postscript document output.
     * <p>
     * Set this value to false if you experience unwanted blank pages in your
     * postscript output.
     * @param dscCompliant boolean value (default is true)
     */
    public void setDSCCompliant(boolean dscCompliant) {
        getPSUtil().setDSCComplianceEnabled(dscCompliant);
    }

}
