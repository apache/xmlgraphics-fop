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

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.File;
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

import org.apache.xmlgraphics.ps.DSCConstants;
import org.apache.xmlgraphics.ps.PSGenerator;
import org.apache.xmlgraphics.ps.PSPageDeviceDictionary;
import org.apache.xmlgraphics.ps.PSProcSets;
import org.apache.xmlgraphics.ps.PSResource;
import org.apache.xmlgraphics.ps.dsc.DSCException;
import org.apache.xmlgraphics.ps.dsc.ResourceTracker;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentBoundingBox;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentHiResBoundingBox;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.fonts.LazyFont;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.render.intermediate.AbstractBinaryWritingIFDocumentHandler;
import org.apache.fop.render.intermediate.IFDocumentHandlerConfigurator;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.IFPainter;
import org.apache.fop.render.ps.extensions.PSExtensionAttachment;
import org.apache.fop.render.ps.extensions.PSSetPageDevice;

/**
 * {@code IFDocumentHandler} implementation that produces PostScript.
 */
public class PSDocumentHandler extends AbstractBinaryWritingIFDocumentHandler {

    /** logging instance */
    private static Log log = LogFactory.getLog(PSDocumentHandler.class);

    /**
     * Utility class which enables all sorts of features that are not directly connected to the
     * normal rendering process.
     */
    protected PSRenderingUtil psUtil;

    /** The PostScript generator used to output the PostScript */
    protected PSGenerator gen;

    /** the temporary file in case of two-pass processing */
    private File tempFile;

    private int currentPageNumber = 0;

    /** Is used to determine the document's bounding box */
    private Rectangle2D documentBoundingBox;

    /** Used to temporarily store PSSetupCode instance until they can be written. */
    private List setupCodeList;

    /** This is a map of PSResource instances of all fonts defined (key: font key) */
    private Map fontResources;
    /** This is a map of PSResource instances of all forms (key: uri) */
    private Map formResources;

    /** encapsulation of dictionary used in setpagedevice instruction **/
    private PSPageDeviceDictionary pageDeviceDictionary;

    /** This is a collection holding all document header comments */
    private Collection headerComments;

    /** This is a collection holding all document footer comments */
    private Collection footerComments;

    /**
     * Default constructor.
     */
    public PSDocumentHandler() {
    }

    /** {@inheritDoc} */
    public boolean supportsPagesOutOfOrder() {
        return false;
    }

    /** {@inheritDoc} */
    public String getMimeType() {
        return MimeConstants.MIME_POSTSCRIPT;
    }

    /** {@inheritDoc} */
    public void setUserAgent(FOUserAgent ua) {
        super.setUserAgent(ua);
        this.psUtil = new PSRenderingUtil(ua);
    }

    /** {@inheritDoc} */
    public IFDocumentHandlerConfigurator getConfigurator() {
        return new PSRendererConfigurator(getUserAgent());
    }

    PSRenderingUtil getPSUtil() {
        return this.psUtil;
    }

    /** {@inheritDoc} */
    public void startDocument() throws IFException {
        try {
            if (getUserAgent() == null) {
                throw new IllegalStateException(
                        "User agent must be set before starting PostScript generation");
            }
            if (this.outputStream == null) {
                throw new IllegalStateException("OutputStream hasn't been set through setResult()");
            }
            OutputStream out;
            if (psUtil.isOptimizeResources()) {
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
                    return getUserAgent().resolveURI(uri);
                }
            };
            this.gen.setPSLevel(psUtil.getLanguageLevel());
            this.currentPageNumber = 0;
            this.documentBoundingBox = new Rectangle2D.Double();

            //Initial default page device dictionary settings
            this.pageDeviceDictionary = new PSPageDeviceDictionary();
            pageDeviceDictionary.setFlushOnRetrieval(!psUtil.isDSCComplianceEnabled());
            pageDeviceDictionary.put("/ImagingBBox", "null");
        } catch (IOException e) {
            throw new IFException("I/O error in startDocument()", e);
        }
    }

    private void writeHeader() throws IOException {
        //PostScript Header
        gen.writeln(DSCConstants.PS_ADOBE_30);
        gen.writeDSCComment(DSCConstants.CREATOR, new String[] {getUserAgent().getProducer()});
        gen.writeDSCComment(DSCConstants.CREATION_DATE, new Object[] {new java.util.Date()});
        gen.writeDSCComment(DSCConstants.LANGUAGE_LEVEL, new Integer(gen.getPSLevel()));
        gen.writeDSCComment(DSCConstants.PAGES, new Object[] {DSCConstants.ATEND});
        gen.writeDSCComment(DSCConstants.BBOX, DSCConstants.ATEND);
        gen.writeDSCComment(DSCConstants.HIRES_BBOX, DSCConstants.ATEND);
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
        if (!psUtil.isOptimizeResources()) {
            this.fontResources = PSFontUtils.writeFontDict(gen, fontInfo);
        } else {
            gen.commentln("%FOPFontSetup"); //Place-holder, will be replaced in the second pass
        }
        gen.writeDSCComment(DSCConstants.END_SETUP);
    }

    /** {@inheritDoc} */
    public void endDocumentHeader() throws IFException {
        try {
            writeHeader();
        } catch (IOException ioe) {
            throw new IFException("I/O error writing the PostScript header", ioe);
        }
    }

    /** {@inheritDoc} */
    public void endDocument() throws IFException {
        try {
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
            if (psUtil.isOptimizeResources()) {
                IOUtils.closeQuietly(gen.getOutputStream());
                rewritePostScriptFile();
            }
            if (footerComments != null) {
                headerComments.clear();
            }
            if (pageDeviceDictionary != null) {
                pageDeviceDictionary.clear();
            }
        } catch (IOException ioe) {
            throw new IFException("I/O error in endDocument()", ioe);
        }
        super.endDocument();
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
                ResourceHandler handler = new ResourceHandler(getUserAgent(), this.fontInfo,
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
    public void startPageSequence(String id) throws IFException {
        //nop
    }

    /** {@inheritDoc} */
    public void endPageSequence() throws IFException {
        //nop
    }

    /** {@inheritDoc} */
    public void startPage(int index, String name, String pageMasterName, Dimension size)
                throws IFException {
        try {
            if (this.currentPageNumber == 0) {
                //writeHeader();
            }

            this.currentPageNumber++;

            gen.getResourceTracker().notifyStartNewPage();
            gen.getResourceTracker().notifyResourceUsageOnPage(PSProcSets.STD_PROCSET);
            gen.writeDSCComment(DSCConstants.PAGE, new Object[]
                    {name,
                     new Integer(this.currentPageNumber)});

            double pageWidth = size.width / 1000.0;
            double pageHeight = size.height / 1000.0;
            boolean rotate = false;
            List pageSizes = new java.util.ArrayList();
            if (this.psUtil.isAutoRotateLandscape() && (pageHeight < pageWidth)) {
                rotate = true;
                pageSizes.add(new Long(Math.round(pageHeight)));
                pageSizes.add(new Long(Math.round(pageWidth)));
            } else {
                pageSizes.add(new Long(Math.round(pageWidth)));
                pageSizes.add(new Long(Math.round(pageHeight)));
            }
            pageDeviceDictionary.put("/PageSize", pageSizes);

            //TODO Handle extension attachments for the page!!!!!!!
            /*
            if (page.hasExtensionAttachments()) {
                for (Iterator iter = page.getExtensionAttachments().iterator();
                    iter.hasNext();) {
                    ExtensionAttachment attachment = (ExtensionAttachment) iter.next();
                    if (attachment instanceof PSSetPageDevice) {*/
                        /**
                         * Extract all PSSetPageDevice instances from the
                         * attachment list on the s-p-m and add all
                         * dictionary entries to our internal representation
                         * of the the page device dictionary.
                         *//*
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
            }*/

            if (setupCodeList != null) {
                PSRenderingUtil.writeEnclosedExtensionAttachments(gen, setupCodeList);
                setupCodeList.clear();
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
                if (psUtil.isAutoRotateLandscape()) {
                    gen.writeDSCComment(DSCConstants.PAGE_ORIENTATION,
                            "Portrait");
                }
            }
            this.documentBoundingBox.add(pageBoundingBox);
            gen.writeDSCComment(DSCConstants.PAGE_RESOURCES,
                    new Object[] {DSCConstants.ATEND});

            gen.commentln("%FOPSimplePageMaster: " + pageMasterName);

            gen.writeDSCComment(DSCConstants.BEGIN_PAGE_SETUP);

            //TODO Handle extension attachments for the page!!!!!!!
            /*
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
            }*/

            // Write any unwritten changes to page device dictionary
            if (!pageDeviceDictionary.isEmpty()) {
                String content = pageDeviceDictionary.getContent();
                if (psUtil.isSafeSetPageDevice()) {
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
            gen.concatMatrix(1, 0, 0, -1, 0, pageHeight);

            gen.writeDSCComment(DSCConstants.END_PAGE_SETUP);
        } catch (IOException ioe) {
            throw new IFException("I/O error in startPage()", ioe);
        }
    }

    /** {@inheritDoc} */
    public IFPainter startPageContent() throws IFException {
        return new PSPainter(this);
    }

    /** {@inheritDoc} */
    public void endPageContent() throws IFException {
        try {
            //Show page
            gen.writeln("showpage");
        } catch (IOException ioe) {
            throw new IFException("I/O error in endPageContent()", ioe);
        }
    }

    /** {@inheritDoc} */
    public void endPage() throws IFException {
        try {
            gen.writeDSCComment(DSCConstants.PAGE_TRAILER);

            //TODO Handle extension attachments for the page!!!!!!!
            /*
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
            }*/
            gen.getResourceTracker().writeResources(true, gen);
        } catch (IOException ioe) {
            throw new IFException("I/O error in endPage()", ioe);
        }
    }

    /** {@inheritDoc} */
    public void handleExtensionObject(Object extension) throws IFException {
        log.debug("Don't know how to handle extension object. Ignoring: "
                + extension + " (" + extension.getClass().getName() + ")");
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

}
