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
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Source;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.java2d.Dimension2DDouble;
import org.apache.xmlgraphics.ps.DSCConstants;
import org.apache.xmlgraphics.ps.PSDictionary;
import org.apache.xmlgraphics.ps.PSDictionaryFormatException;
import org.apache.xmlgraphics.ps.PSGenerator;
import org.apache.xmlgraphics.ps.PSPageDeviceDictionary;
import org.apache.xmlgraphics.ps.PSProcSets;
import org.apache.xmlgraphics.ps.PSResource;
import org.apache.xmlgraphics.ps.dsc.DSCException;
import org.apache.xmlgraphics.ps.dsc.ResourceTracker;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentBoundingBox;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentHiResBoundingBox;

import org.apache.fop.apps.MimeConstants;
import org.apache.fop.render.intermediate.AbstractBinaryWritingIFDocumentHandler;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFDocumentHandlerConfigurator;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.IFPainter;
import org.apache.fop.render.ps.PSRendererConfig.PSRendererConfigParser;
import org.apache.fop.render.ps.extensions.PSCommentAfter;
import org.apache.fop.render.ps.extensions.PSCommentBefore;
import org.apache.fop.render.ps.extensions.PSPageTrailerCodeBefore;
import org.apache.fop.render.ps.extensions.PSSetPageDevice;
import org.apache.fop.render.ps.extensions.PSSetupCode;


/**
 * {@link IFDocumentHandler} implementation that produces PostScript.
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
    private PageDefinition currentPageDefinition;

    /** Is used to determine the document's bounding box */
    private Rectangle2D documentBoundingBox;

    /** Used to temporarily store PSSetupCode instance until they can be written. */
    private List setupCodeList;

    /** This is a cache of PSResource instances of all fonts defined */
    private FontResourceCache fontResources;
    /** This is a map of PSResource instances of all forms (key: uri) */
    private Map formResources;

    /** encapsulation of dictionary used in setpagedevice instruction **/
    private PSPageDeviceDictionary pageDeviceDictionary;

    /** This is a collection holding all document header comments */
    private Collection[] comments = new Collection[4];
    private static final int COMMENT_DOCUMENT_HEADER = 0;
    private static final int COMMENT_DOCUMENT_TRAILER = 1;
    private static final int COMMENT_PAGE_TRAILER = 2;
    private static final int PAGE_TRAILER_CODE_BEFORE = 3;

    /**
     * Default constructor.
     */
    public PSDocumentHandler(IFContext context) {
        super(context);
        this.psUtil = new PSRenderingUtil(context.getUserAgent());
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
    public IFDocumentHandlerConfigurator getConfigurator() {
        return new PSRendererConfigurator(getUserAgent(), new PSRendererConfigParser());
    }

    PSRenderingUtil getPSUtil() {
        return this.psUtil;
    }

    /** {@inheritDoc} */
    public void startDocument() throws IFException {
        super.startDocument();
        this.fontResources = new FontResourceCache(getFontInfo());
        try {
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
        writeExtensions(COMMENT_DOCUMENT_HEADER);
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
        FOPProcSet.INSTANCE.writeTo(gen);
        gen.writeDSCComment(DSCConstants.END_PROLOG);

        //Setup
        gen.writeDSCComment(DSCConstants.BEGIN_SETUP);
        PSRenderingUtil.writeSetupCodeList(gen, setupCodeList, "SetupCode");
        if (!psUtil.isOptimizeResources()) {
            this.fontResources.addAll(PSFontUtils.writeFontDict(gen, fontInfo));
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
            writeExtensions(COMMENT_DOCUMENT_TRAILER);
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
            this.currentPageDefinition = new PageDefinition(
                    new Dimension2DDouble(pageWidth, pageHeight), rotate);

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
        } catch (IOException ioe) {
            throw new IFException("I/O error in startPage()", ioe);
        }
    }

    /** {@inheritDoc} */
    public void startPageHeader() throws IFException {
        super.startPageHeader();

        try {
            gen.writeDSCComment(DSCConstants.BEGIN_PAGE_SETUP);
        } catch (IOException ioe) {
            throw new IFException("I/O error in startPageHeader()", ioe);
        }
    }

    /** {@inheritDoc} */
    public void endPageHeader() throws IFException {
        try {
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

            double pageHeight = this.currentPageDefinition.dimensions.getHeight();
            if (this.currentPageDefinition.rotate) {
                gen.writeln(gen.formatDouble(pageHeight) + " 0 translate");
                gen.writeln("90 rotate");
            }
            gen.concatMatrix(1, 0, 0, -1, 0, pageHeight);

            gen.writeDSCComment(DSCConstants.END_PAGE_SETUP);
        } catch (IOException ioe) {
            throw new IFException("I/O error in endPageHeader()", ioe);
        }

        super.endPageHeader();
    }

    private void writeExtensions(int which) throws IOException {
        Collection extensions = comments[which];
        if (extensions != null) {
            PSRenderingUtil.writeEnclosedExtensionAttachments(gen, extensions);
            extensions.clear();
        }
    }

    /** {@inheritDoc} */
    public IFPainter startPageContent() throws IFException {
        return new PSPainter(this);
    }

    /** {@inheritDoc} */
    public void endPageContent() throws IFException {
        try {
            gen.showPage();
        } catch (IOException ioe) {
            throw new IFException("I/O error in endPageContent()", ioe);
        }
    }

    /** {@inheritDoc} */
    public void startPageTrailer() throws IFException {
        try {
            writeExtensions(PAGE_TRAILER_CODE_BEFORE);
            super.startPageTrailer();
            gen.writeDSCComment(DSCConstants.PAGE_TRAILER);
        } catch (IOException ioe) {
            throw new IFException("I/O error in startPageTrailer()", ioe);
        }
    }

    /** {@inheritDoc} */
    public void endPageTrailer() throws IFException {
        try {
            writeExtensions(COMMENT_PAGE_TRAILER);
        } catch (IOException ioe) {
            throw new IFException("I/O error in endPageTrailer()", ioe);
        }
        super.endPageTrailer();
    }

    /** {@inheritDoc} */
    public void endPage() throws IFException {
        try {
            gen.getResourceTracker().writeResources(true, gen);
        } catch (IOException ioe) {
            throw new IFException("I/O error in endPage()", ioe);
        }

        this.currentPageDefinition = null;
    }

    private boolean inPage() {
        return this.currentPageDefinition != null;
    }

    /** {@inheritDoc} */
    public void handleExtensionObject(Object extension) throws IFException {
        try {
            if (extension instanceof PSSetupCode) {
                if (inPage()) {
                    PSRenderingUtil.writeEnclosedExtensionAttachment(gen, (PSSetupCode)extension);
                } else {
                    //A special collection for setup code as it's put in a different place
                    //than the "before comments".
                    if (setupCodeList == null) {
                        setupCodeList = new java.util.ArrayList();
                    }
                    if (!setupCodeList.contains(extension)) {
                        setupCodeList.add(extension);
                    }
                }
            } else if (extension instanceof PSSetPageDevice) {
                /**
                 * Extract all PSSetPageDevice instances from the
                 * attachment list on the s-p-m and add all dictionary
                 * entries to our internal representation of the the
                 * page device dictionary.
                 */
                PSSetPageDevice setPageDevice = (PSSetPageDevice)extension;
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
            } else if (extension instanceof PSCommentBefore) {
                if (inPage()) {
                    PSRenderingUtil.writeEnclosedExtensionAttachment(
                            gen, (PSCommentBefore)extension);
                } else {
                    if (comments[COMMENT_DOCUMENT_HEADER] == null) {
                        comments[COMMENT_DOCUMENT_HEADER] = new java.util.ArrayList();
                    }
                    comments[COMMENT_DOCUMENT_HEADER].add(extension);
                }
            } else if (extension instanceof PSCommentAfter) {
                int targetCollection = (inPage() ? COMMENT_PAGE_TRAILER : COMMENT_DOCUMENT_TRAILER);
                if (comments[targetCollection] == null) {
                    comments[targetCollection] = new java.util.ArrayList();
                }
                comments[targetCollection].add(extension);
            } else if (extension instanceof PSPageTrailerCodeBefore) {
                if (comments[PAGE_TRAILER_CODE_BEFORE] == null) {
                    comments[PAGE_TRAILER_CODE_BEFORE] = new ArrayList();
                }
                comments[PAGE_TRAILER_CODE_BEFORE].add(extension);
            }
        } catch (IOException ioe) {
            throw new IFException("I/O error in handleExtensionObject()", ioe);
        }
    }

    /**
     * Returns the PSResource for the given font key.
     * @param key the font key ("F*")
     * @return the matching PSResource
     */
    protected PSResource getPSResourceForFontKey(String key) {
        return this.fontResources.getPSResourceForFontKey(key);
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

    private static final class PageDefinition {
        private Dimension2D dimensions;
        private boolean rotate;

        private PageDefinition(Dimension2D dimensions, boolean rotate) {
            this.dimensions = dimensions;
            this.rotate = rotate;
        }
    }

}
