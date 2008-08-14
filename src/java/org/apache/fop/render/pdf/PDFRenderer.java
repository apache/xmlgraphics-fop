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

package org.apache.fop.render.pdf;

// Java
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.color.ICC_Profile;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;

import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.util.ImageUtil;
import org.apache.xmlgraphics.xmp.Metadata;
import org.apache.xmlgraphics.xmp.schemas.XMPBasicAdapter;
import org.apache.xmlgraphics.xmp.schemas.XMPBasicSchema;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.area.BookmarkData;
import org.apache.fop.area.CTM;
import org.apache.fop.area.DestinationData;
import org.apache.fop.area.LineArea;
import org.apache.fop.area.OffDocumentExtensionAttachment;
import org.apache.fop.area.OffDocumentItem;
import org.apache.fop.area.PageSequence;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.AbstractTextArea;
import org.apache.fop.area.inline.Image;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.InlineParent;
import org.apache.fop.area.inline.Leader;
import org.apache.fop.area.inline.SpaceArea;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.area.inline.WordArea;
import org.apache.fop.datatypes.URISpecification;
import org.apache.fop.events.ResourceEventProducer;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.extensions.ExtensionAttachment;
import org.apache.fop.fo.extensions.xmp.XMPMetadata;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.LazyFont;
import org.apache.fop.fonts.SingleByteFont;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.pdf.PDFAMode;
import org.apache.fop.pdf.PDFAction;
import org.apache.fop.pdf.PDFAnnotList;
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.pdf.PDFConformanceException;
import org.apache.fop.pdf.PDFDictionary;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFEncryptionManager;
import org.apache.fop.pdf.PDFEncryptionParams;
import org.apache.fop.pdf.PDFFactory;
import org.apache.fop.pdf.PDFFilterList;
import org.apache.fop.pdf.PDFGoTo;
import org.apache.fop.pdf.PDFICCBasedColorSpace;
import org.apache.fop.pdf.PDFICCStream;
import org.apache.fop.pdf.PDFInfo;
import org.apache.fop.pdf.PDFLink;
import org.apache.fop.pdf.PDFMetadata;
import org.apache.fop.pdf.PDFNumber;
import org.apache.fop.pdf.PDFNumsArray;
import org.apache.fop.pdf.PDFOutline;
import org.apache.fop.pdf.PDFOutputIntent;
import org.apache.fop.pdf.PDFPage;
import org.apache.fop.pdf.PDFPageLabels;
import org.apache.fop.pdf.PDFResourceContext;
import org.apache.fop.pdf.PDFResources;
import org.apache.fop.pdf.PDFState;
import org.apache.fop.pdf.PDFStream;
import org.apache.fop.pdf.PDFTextUtil;
import org.apache.fop.pdf.PDFXMode;
import org.apache.fop.pdf.PDFXObject;
import org.apache.fop.render.AbstractPathOrientedRenderer;
import org.apache.fop.render.Graphics2DAdapter;
import org.apache.fop.render.RendererContext;
import org.apache.fop.util.CharUtilities;
import org.apache.fop.util.ColorProfileUtil;
import org.apache.fop.util.ColorUtil;

/**
 * Renderer that renders areas to PDF.
 */
public class PDFRenderer extends AbstractPathOrientedRenderer {

    /**
     * The mime type for pdf
     */
    public static final String MIME_TYPE = MimeConstants.MIME_PDF;

    /** Normal PDF resolution (72dpi) */
    public static final int NORMAL_PDF_RESOLUTION = 72;

    /** PDF encryption parameter: all parameters as object, datatype: PDFEncryptionParams */
    public static final String ENCRYPTION_PARAMS = "encryption-params";
    /** PDF encryption parameter: user password, datatype: String */
    public static final String USER_PASSWORD = "user-password";
    /** PDF encryption parameter: owner password, datatype: String */
    public static final String OWNER_PASSWORD = "owner-password";
    /** PDF encryption parameter: Forbids printing, datatype: Boolean or "true"/"false" */
    public static final String NO_PRINT = "noprint";
    /** PDF encryption parameter: Forbids copying content, datatype: Boolean or "true"/"false" */
    public static final String NO_COPY_CONTENT = "nocopy";
    /** PDF encryption parameter: Forbids editing content, datatype: Boolean or "true"/"false" */
    public static final String NO_EDIT_CONTENT = "noedit";
    /** PDF encryption parameter: Forbids annotations, datatype: Boolean or "true"/"false" */
    public static final String NO_ANNOTATIONS = "noannotations";
    /** Rendering Options key for the PDF/A mode. */
    public static final String PDF_A_MODE = "pdf-a-mode";
    /** Rendering Options key for the PDF/X mode. */
    public static final String PDF_X_MODE = "pdf-x-mode";
    /** Rendering Options key for the ICC profile for the output intent. */
    public static final String KEY_OUTPUT_PROFILE = "output-profile";
    /**
     * Rendering Options key for disabling the sRGB color space (only possible if no PDF/A or
     * PDF/X profile is active).
     */
    public static final String KEY_DISABLE_SRGB_COLORSPACE = "disable-srgb-colorspace";

    /** Controls whether comments are written to the PDF stream. */
    protected static final boolean WRITE_COMMENTS = true;

    /**
     * the PDF Document being created
     */
    protected PDFDocument pdfDoc;

    /** the PDF/A mode (Default: disabled) */
    protected PDFAMode pdfAMode = PDFAMode.DISABLED;

    /** the PDF/X mode (Default: disabled) */
    protected PDFXMode pdfXMode = PDFXMode.DISABLED;

    /**
     * Map of pages using the PageViewport as the key
     * this is used for prepared pages that cannot be immediately
     * rendered
     */
    protected Map pages = null;

    /**
     * Maps unique PageViewport key to PDF page reference
     */
    protected Map pageReferences = new java.util.HashMap();

    /**
     *  Maps unique PageViewport key back to PageViewport itself
     */
    protected Map pvReferences = new java.util.HashMap();

    /**
     * Maps XSL-FO element IDs to their on-page XY-positions
     * Must be used in conjunction with the page reference to fully specify the PDFGoTo details
     */
    protected Map idPositions = new java.util.HashMap();

    /**
     * Maps XSL-FO element IDs to PDFGoTo objects targeting the corresponding areas
     * These objects may not all be fully filled in yet
     */
    protected Map idGoTos = new java.util.HashMap();

    /**
     * The PDFGoTos in idGoTos that are not complete yet
     */
    protected List unfinishedGoTos = new java.util.ArrayList();
    // can't use a Set because PDFGoTo.equals returns true if the target is the same,
    // even if the object number differs

    /**
     * The output stream to write the document to
     */
    protected OutputStream ostream;

    /**
     * the /Resources object of the PDF document being created
     */
    protected PDFResources pdfResources;

    /**
     * the current stream to add PDF commands to
     */
    protected PDFStream currentStream;

    /**
     * the current annotation list to add annotations to
     */
    protected PDFResourceContext currentContext = null;

    /**
     * the current page to add annotations to
     */
    protected PDFPage currentPage;

    /**
     * the current page's PDF reference string (to avoid numerous function calls)
     */
    protected String currentPageRef;

    /** the (optional) encryption parameters */
    protected PDFEncryptionParams encryptionParams;

    /** the ICC stream used as output profile by this document for PDF/A and PDF/X functionality. */
    protected PDFICCStream outputProfile;
    /** the default sRGB color space. */
    protected PDFICCBasedColorSpace sRGBColorSpace;
    /** controls whether the sRGB color space should be installed */
    protected boolean disableSRGBColorSpace = false;

    /** Optional URI to an output profile to be used. */
    protected String outputProfileURI;

    /** drawing state */
    protected PDFState currentState = null;

    /** Text generation utility holding the current font status */
    protected PDFTextUtil textutil;
    /** page height */
    protected int pageHeight;

    /** Registry of PDF filters */
    protected Map filterMap;

    /** Image handler registry */
    private PDFImageHandlerRegistry imageHandlerRegistry = new PDFImageHandlerRegistry();

    /**
     * create the PDF renderer
     */
    public PDFRenderer() {
    }

    private boolean booleanValueOf(Object obj) {
        if (obj instanceof Boolean) {
            return ((Boolean)obj).booleanValue();
        } else if (obj instanceof String) {
            return Boolean.valueOf((String)obj).booleanValue();
        } else {
            throw new IllegalArgumentException("Boolean or \"true\" or \"false\" expected.");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setUserAgent(FOUserAgent agent) {
        super.setUserAgent(agent);
        PDFEncryptionParams params
                = (PDFEncryptionParams)agent.getRendererOptions().get(ENCRYPTION_PARAMS);
        if (params != null) {
            this.encryptionParams = params; //overwrite if available
        }
        String pwd;
        pwd = (String)agent.getRendererOptions().get(USER_PASSWORD);
        if (pwd != null) {
            if (encryptionParams == null) {
                this.encryptionParams = new PDFEncryptionParams();
            }
            this.encryptionParams.setUserPassword(pwd);
        }
        pwd = (String)agent.getRendererOptions().get(OWNER_PASSWORD);
        if (pwd != null) {
            if (encryptionParams == null) {
                this.encryptionParams = new PDFEncryptionParams();
            }
            this.encryptionParams.setOwnerPassword(pwd);
        }
        Object setting;
        setting = agent.getRendererOptions().get(NO_PRINT);
        if (setting != null) {
            if (encryptionParams == null) {
                this.encryptionParams = new PDFEncryptionParams();
            }
            this.encryptionParams.setAllowPrint(!booleanValueOf(setting));
        }
        setting = agent.getRendererOptions().get(NO_COPY_CONTENT);
        if (setting != null) {
            if (encryptionParams == null) {
                this.encryptionParams = new PDFEncryptionParams();
            }
            this.encryptionParams.setAllowCopyContent(!booleanValueOf(setting));
        }
        setting = agent.getRendererOptions().get(NO_EDIT_CONTENT);
        if (setting != null) {
            if (encryptionParams == null) {
                this.encryptionParams = new PDFEncryptionParams();
            }
            this.encryptionParams.setAllowEditContent(!booleanValueOf(setting));
        }
        setting = agent.getRendererOptions().get(NO_ANNOTATIONS);
        if (setting != null) {
            if (encryptionParams == null) {
                this.encryptionParams = new PDFEncryptionParams();
            }
            this.encryptionParams.setAllowEditAnnotations(!booleanValueOf(setting));
        }
        String s = (String)agent.getRendererOptions().get(PDF_A_MODE);
        if (s != null) {
            this.pdfAMode = PDFAMode.valueOf(s);
        }
        s = (String)agent.getRendererOptions().get(PDF_X_MODE);
        if (s != null) {
            this.pdfXMode = PDFXMode.valueOf(s);
        }
        s = (String)agent.getRendererOptions().get(KEY_OUTPUT_PROFILE);
        if (s != null) {
            this.outputProfileURI = s;
        }
        setting = agent.getRendererOptions().get(KEY_DISABLE_SRGB_COLORSPACE);
        if (setting != null) {
            this.disableSRGBColorSpace = booleanValueOf(setting);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void startRenderer(OutputStream stream) throws IOException {
        if (userAgent == null) {
            throw new IllegalStateException("UserAgent must be set before starting the renderer");
        }
        ostream = stream;
        this.pdfDoc = new PDFDocument(
                userAgent.getProducer() != null ? userAgent.getProducer() : "");
        this.pdfDoc.getProfile().setPDFAMode(this.pdfAMode);
        this.pdfDoc.getProfile().setPDFXMode(this.pdfXMode);
        this.pdfDoc.getInfo().setCreator(userAgent.getCreator());
        this.pdfDoc.getInfo().setCreationDate(userAgent.getCreationDate());
        this.pdfDoc.getInfo().setAuthor(userAgent.getAuthor());
        this.pdfDoc.getInfo().setTitle(userAgent.getTitle());
        this.pdfDoc.getInfo().setKeywords(userAgent.getKeywords());
        this.pdfDoc.setFilterMap(filterMap);
        this.pdfDoc.outputHeader(ostream);

        //Setup encryption if necessary
        PDFEncryptionManager.setupPDFEncryption(encryptionParams, this.pdfDoc);

        addsRGBColorSpace();
        if (this.outputProfileURI != null) {
            addDefaultOutputProfile();
        }
        if (pdfXMode != PDFXMode.DISABLED) {
            log.debug(pdfXMode + " is active.");
            log.warn("Note: " + pdfXMode
                    + " support is work-in-progress and not fully implemented, yet!");
            addPDFXOutputIntent();
        }
        if (pdfAMode.isPDFA1LevelB()) {
            log.debug("PDF/A is active. Conformance Level: " + pdfAMode);
            addPDFA1OutputIntent();
        }

    }

    private void addsRGBColorSpace() throws IOException {
        if (disableSRGBColorSpace) {
            if (this.pdfAMode != PDFAMode.DISABLED
                    || this.pdfXMode != PDFXMode.DISABLED
                    || this.outputProfileURI != null) {
                throw new IllegalStateException("It is not possible to disable the sRGB color"
                        + " space if PDF/A or PDF/X functionality is enabled or an"
                        + " output profile is set!");
            }
        } else {
            if (this.sRGBColorSpace != null) {
                return;
            }
            //Map sRGB as default RGB profile for DeviceRGB
            this.sRGBColorSpace = PDFICCBasedColorSpace.setupsRGBAsDefaultRGBColorSpace(pdfDoc);
        }
    }

    private void addDefaultOutputProfile() throws IOException {
        if (this.outputProfile != null) {
            return;
        }
        ICC_Profile profile;
        InputStream in = null;
        if (this.outputProfileURI != null) {
            this.outputProfile = pdfDoc.getFactory().makePDFICCStream();
            Source src = userAgent.resolveURI(this.outputProfileURI);
            if (src == null) {
                throw new IOException("Output profile not found: " + this.outputProfileURI);
            }
            if (src instanceof StreamSource) {
                in = ((StreamSource)src).getInputStream();
            } else {
                in = new URL(src.getSystemId()).openStream();
            }
            try {
                profile = ICC_Profile.getInstance(in);
            } finally {
                IOUtils.closeQuietly(in);
            }
            this.outputProfile.setColorSpace(profile, null);
        } else {
            //Fall back to sRGB profile
            outputProfile = sRGBColorSpace.getICCStream();
        }
    }

    /**
     * Adds an OutputIntent to the PDF as mandated by PDF/A-1 when uncalibrated color spaces
     * are used (which is true if we use DeviceRGB to represent sRGB colors).
     * @throws IOException in case of an I/O problem
     */
    private void addPDFA1OutputIntent() throws IOException {
        addDefaultOutputProfile();

        String desc = ColorProfileUtil.getICCProfileDescription(this.outputProfile.getICCProfile());
        PDFOutputIntent outputIntent = pdfDoc.getFactory().makeOutputIntent();
        outputIntent.setSubtype(PDFOutputIntent.GTS_PDFA1);
        outputIntent.setDestOutputProfile(this.outputProfile);
        outputIntent.setOutputConditionIdentifier(desc);
        outputIntent.setInfo(outputIntent.getOutputConditionIdentifier());
        pdfDoc.getRoot().addOutputIntent(outputIntent);
    }

    /**
     * Adds an OutputIntent to the PDF as mandated by PDF/X when uncalibrated color spaces
     * are used (which is true if we use DeviceRGB to represent sRGB colors).
     * @throws IOException in case of an I/O problem
     */
    private void addPDFXOutputIntent() throws IOException {
        addDefaultOutputProfile();

        String desc = ColorProfileUtil.getICCProfileDescription(this.outputProfile.getICCProfile());
        int deviceClass = this.outputProfile.getICCProfile().getProfileClass();
        if (deviceClass != ICC_Profile.CLASS_OUTPUT) {
            throw new PDFConformanceException(pdfDoc.getProfile().getPDFXMode() + " requires that"
                    + " the DestOutputProfile be an Output Device Profile. "
                    + desc + " does not match that requirement.");
        }
        PDFOutputIntent outputIntent = pdfDoc.getFactory().makeOutputIntent();
        outputIntent.setSubtype(PDFOutputIntent.GTS_PDFX);
        outputIntent.setDestOutputProfile(this.outputProfile);
        outputIntent.setOutputConditionIdentifier(desc);
        outputIntent.setInfo(outputIntent.getOutputConditionIdentifier());
        pdfDoc.getRoot().addOutputIntent(outputIntent);
    }

    /**
     * Checks if there are any unfinished PDFGoTos left in the list and resolves them
     * to a default position on the page. Logs a warning, as this should not happen.
     */
    protected void finishOpenGoTos() {
        int count = unfinishedGoTos.size();
        if (count > 0) {
            // TODO : page height may not be the same for all targeted pages
            Point2D.Float defaultPos = new Point2D.Float(0f, pageHeight / 1000f);  // top-o-page
            while (!unfinishedGoTos.isEmpty()) {
                PDFGoTo gt = (PDFGoTo) unfinishedGoTos.get(0);
                finishIDGoTo(gt, defaultPos);
            }
            PDFEventProducer eventProducer = PDFEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.nonFullyResolvedLinkTargets(this, count);
            // dysfunctional if pageref is null
        }
    }

    /**
     * {@inheritDoc}
     */
    public void stopRenderer() throws IOException {
        finishOpenGoTos();

        pdfDoc.getResources().addFonts(pdfDoc, fontInfo);
        pdfDoc.outputTrailer(ostream);

        this.pdfDoc = null;
        ostream = null;

        pages = null;

        pageReferences.clear();
        pvReferences.clear();
        pdfResources = null;
        currentStream = null;
        currentContext = null;
        currentPage = null;
        currentState = null;
        this.textutil = null;

        idPositions.clear();
        idGoTos.clear();
    }

    /**
     * {@inheritDoc}
     */
    public boolean supportsOutOfOrder() {
        //return false;
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void processOffDocumentItem(OffDocumentItem odi) {
        if (odi instanceof DestinationData) {
            // render Destinations
            renderDestination((DestinationData) odi);
        } else if (odi instanceof BookmarkData) {
            // render Bookmark-Tree
            renderBookmarkTree((BookmarkData) odi);
        } else if (odi instanceof OffDocumentExtensionAttachment) {
            ExtensionAttachment attachment = ((OffDocumentExtensionAttachment)odi).getAttachment();
            if (XMPMetadata.CATEGORY.equals(attachment.getCategory())) {
                renderXMPMetadata((XMPMetadata)attachment);
            }
        }
    }

    private void renderDestination(DestinationData dd) {
        String targetID = dd.getIDRef();
        if (targetID == null || targetID.length() == 0) {
            throw new IllegalArgumentException("DestinationData must contain a ID reference");
        }
        PageViewport pv = dd.getPageViewport();
        if (pv != null) {
            PDFGoTo gt = getPDFGoToForID(targetID, pv.getKey());
            pdfDoc.getFactory().makeDestination(
                    dd.getIDRef(), gt.makeReference());
        } else {
            //Warning already issued by AreaTreeHandler (debug level is sufficient)
            log.debug("Unresolved destination item received: " + dd.getIDRef());
        }
    }

    /**
     * Renders a Bookmark-Tree object
     * @param bookmarks the BookmarkData object containing all the Bookmark-Items
     */
    protected void renderBookmarkTree(BookmarkData bookmarks) {
        for (int i = 0; i < bookmarks.getCount(); i++) {
            BookmarkData ext = bookmarks.getSubData(i);
            renderBookmarkItem(ext, null);
        }
    }

    private void renderBookmarkItem(BookmarkData bookmarkItem,
                                    PDFOutline parentBookmarkItem) {
        PDFOutline pdfOutline = null;

        String targetID = bookmarkItem.getIDRef();
        if (targetID == null || targetID.length() == 0) {
            throw new IllegalArgumentException("DestinationData must contain a ID reference");
        }
        PageViewport pv = bookmarkItem.getPageViewport();
        if (pv != null) {
            String pvKey = pv.getKey();
            PDFGoTo gt = getPDFGoToForID(targetID, pvKey);
            // create outline object:
            PDFOutline parent = parentBookmarkItem != null
                ? parentBookmarkItem
                : pdfDoc.getOutlineRoot();
            pdfOutline = pdfDoc.getFactory().makeOutline(parent,
                    bookmarkItem.getBookmarkTitle(), gt, bookmarkItem.showChildItems());
        } else {
            //Warning already issued by AreaTreeHandler (debug level is sufficient)
            log.debug("Bookmark with IDRef \"" + targetID + "\" has a null PageViewport.");
        }

        for (int i = 0; i < bookmarkItem.getCount(); i++) {
            renderBookmarkItem(bookmarkItem.getSubData(i), pdfOutline);
        }
    }

    private void renderXMPMetadata(XMPMetadata metadata) {
        Metadata docXMP = metadata.getMetadata();
        Metadata fopXMP = PDFMetadata.createXMPFromPDFDocument(pdfDoc);
        //Merge FOP's own metadata into the one from the XSL-FO document
        fopXMP.mergeInto(docXMP);
        XMPBasicAdapter xmpBasic = XMPBasicSchema.getAdapter(docXMP);
        //Metadata was changed so update metadata date
        xmpBasic.setMetadataDate(new java.util.Date());
        PDFMetadata.updateInfoFromMetadata(docXMP, pdfDoc.getInfo());

        PDFMetadata pdfMetadata = pdfDoc.getFactory().makeMetadata(
                docXMP, metadata.isReadOnly());
        pdfDoc.getRoot().setMetadata(pdfMetadata);
    }

    /** {@inheritDoc} */
    public Graphics2DAdapter getGraphics2DAdapter() {
        return new PDFGraphics2DAdapter(this);
    }

    /**
     * writes out a comment.
     * @param text text for the comment
     */
    protected void comment(String text) {
        if (WRITE_COMMENTS) {
            currentStream.add("% " + text + "\n");
        }
    }

    /** {@inheritDoc} */
    protected void saveGraphicsState() {
        endTextObject();
        currentState.push();
        currentStream.add("q\n");
    }

    private void restoreGraphicsState(boolean popState) {
        endTextObject();
        currentStream.add("Q\n");
        if (popState) {
            currentState.pop();
        }
    }

    /** {@inheritDoc} */
    protected void restoreGraphicsState() {
        restoreGraphicsState(true);
    }

    /** Indicates the beginning of a text object. */
    protected void beginTextObject() {
        if (!textutil.isInTextObject()) {
            textutil.beginTextObject();
        }
    }

    /** Indicates the end of a text object. */
    protected void endTextObject() {
        if (textutil.isInTextObject()) {
            textutil.endTextObject();
        }
    }

    /**
     * Start the next page sequence.
     * For the PDF renderer there is no concept of page sequences
     * but it uses the first available page sequence title to set
     * as the title of the PDF document, and the language of the
     * document.
     * @param pageSequence the page sequence
     */
    public void startPageSequence(PageSequence pageSequence) {
        super.startPageSequence(pageSequence);
        LineArea seqTitle = pageSequence.getTitle();
        if (seqTitle != null) {
            String str = convertTitleToString(seqTitle);
            PDFInfo info = this.pdfDoc.getInfo();
            if (info.getTitle() == null) {
                info.setTitle(str);
            }
        }
        if (pageSequence.getLanguage() != null) {
            String lang = pageSequence.getLanguage();
            String country = pageSequence.getCountry();
            String langCode = lang + (country != null ? "-" + country : "");
            if (pdfDoc.getRoot().getLanguage() == null) {
                //Only set if not set already (first non-null is used)
                //Note: No checking is performed whether the values are valid!
                pdfDoc.getRoot().setLanguage(langCode);
            }
        }
        if (pdfDoc.getRoot().getMetadata() == null) {
            //If at this time no XMP metadata for the overall document has been set, create it
            //from the PDFInfo object.
            Metadata xmp = PDFMetadata.createXMPFromPDFDocument(pdfDoc);
            PDFMetadata pdfMetadata = pdfDoc.getFactory().makeMetadata(
                    xmp, true);
            pdfDoc.getRoot().setMetadata(pdfMetadata);
        }
    }

    /**
     * The pdf page is prepared by making the page.
     * The page is made in the pdf document without any contents
     * and then stored to add the contents later.
     * The page objects is stored using the area tree PageViewport
     * as a key.
     *
     * @param page the page to prepare
     */
    public void preparePage(PageViewport page) {
        setupPage(page);
        if (pages == null) {
            pages = new java.util.HashMap();
        }
        pages.put(page, currentPage);
    }

    private void setupPage(PageViewport page) {
        this.pdfResources = this.pdfDoc.getResources();

        Rectangle2D bounds = page.getViewArea();
        double w = bounds.getWidth();
        double h = bounds.getHeight();
        currentPage = this.pdfDoc.getFactory().makePage(
            this.pdfResources,
            (int) Math.round(w / 1000), (int) Math.round(h / 1000),
            page.getPageIndex());
        pageReferences.put(page.getKey(), currentPage.referencePDF());
        pvReferences.put(page.getKey(), page);

        //Produce page labels
        PDFPageLabels pageLabels = this.pdfDoc.getRoot().getPageLabels();
        if (pageLabels == null) {
            //Set up PageLabels
            pageLabels = this.pdfDoc.getFactory().makePageLabels();
            this.pdfDoc.getRoot().setPageLabels(pageLabels);
        }
        PDFNumsArray nums = pageLabels.getNums();
        PDFDictionary dict = new PDFDictionary(nums);
        dict.put("P", page.getPageNumberString());
        //TODO If the sequence of generated page numbers were inspected, this could be
        //expressed in a more space-efficient way
        nums.put(page.getPageIndex(), dict);
    }

    /**
     * This method creates a pdf stream for the current page
     * uses it as the contents of a new page. The page is written
     * immediately to the output stream.
     * {@inheritDoc}
     */
    public void renderPage(PageViewport page)
                throws IOException, FOPException {
        if (pages != null
                && (currentPage = (PDFPage) pages.get(page)) != null) {
            //Retrieve previously prepared page (out-of-line rendering)
            pages.remove(page);
        } else {
            setupPage(page);
        }
        currentPageRef = currentPage.referencePDF();

        Rectangle2D bounds = page.getViewArea();
        double h = bounds.getHeight();
        pageHeight = (int) h;

        currentStream = this.pdfDoc.getFactory()
            .makeStream(PDFFilterList.CONTENT_FILTER, false);
        this.textutil = new PDFTextUtil() {
            protected void write(String code) {
                currentStream.add(code);
            }
        };

        currentState = new PDFState();
        // Transform the PDF's default coordinate system (0,0 at lower left) to the PDFRenderer's
        AffineTransform basicPageTransform = new AffineTransform(1, 0, 0, -1, 0,
                pageHeight / 1000f);
        currentState.concatenate(basicPageTransform);
        currentStream.add(CTMHelper.toPDFString(basicPageTransform, false) + " cm\n");

        super.renderPage(page);

        this.pdfDoc.registerObject(currentStream);
        currentPage.setContents(currentStream);
        PDFAnnotList annots = currentPage.getAnnotations();
        if (annots != null) {
            this.pdfDoc.addObject(annots);
        }
        this.pdfDoc.addObject(currentPage);
        this.pdfDoc.output(ostream);
        this.textutil = null;
    }

    /** {@inheritDoc} */
    protected void startVParea(CTM ctm, Rectangle2D clippingRect) {
        saveGraphicsState();
        // Set the given CTM in the graphics state
        currentState.concatenate(
                new AffineTransform(CTMHelper.toPDFArray(ctm)));

        if (clippingRect != null) {
            clipRect((float)clippingRect.getX() / 1000f,
                    (float)clippingRect.getY() / 1000f,
                    (float)clippingRect.getWidth() / 1000f,
                    (float)clippingRect.getHeight() / 1000f);
        }
        // multiply with current CTM
        currentStream.add(CTMHelper.toPDFString(ctm) + " cm\n");
    }

    /** {@inheritDoc} */
    protected void endVParea() {
        restoreGraphicsState();
    }

    /** {@inheritDoc} */
    protected void concatenateTransformationMatrix(AffineTransform at) {
        if (!at.isIdentity()) {
            currentState.concatenate(at);
            currentStream.add(CTMHelper.toPDFString(at, false) + " cm\n");
        }
    }

    /**
     * Formats a float value (normally coordinates) as Strings.
     * @param value the value
     * @return the formatted value
     */
    protected static String format(float value) {
        return PDFNumber.doubleOut(value);
    }

    /** {@inheritDoc} */
    protected void drawBorderLine(float x1, float y1, float x2, float y2,
            boolean horz, boolean startOrBefore, int style, Color col) {
        float w = x2 - x1;
        float h = y2 - y1;
        if ((w < 0) || (h < 0)) {
            log.error("Negative extent received (w=" + w + ", h=" + h
                    + "). Border won't be painted.");
            return;
        }
        switch (style) {
            case Constants.EN_DASHED:
                setColor(col, false, null);
                if (horz) {
                    float unit = Math.abs(2 * h);
                    int rep = (int)(w / unit);
                    if (rep % 2 == 0) {
                        rep++;
                    }
                    unit = w / rep;
                    currentStream.add("[" + format(unit) + "] 0 d ");
                    currentStream.add(format(h) + " w\n");
                    float ym = y1 + (h / 2);
                    currentStream.add(format(x1) + " " + format(ym) + " m "
                            + format(x2) + " " + format(ym) + " l S\n");
                } else {
                    float unit = Math.abs(2 * w);
                    int rep = (int)(h / unit);
                    if (rep % 2 == 0) {
                        rep++;
                    }
                    unit = h / rep;
                    currentStream.add("[" + format(unit) + "] 0 d ");
                    currentStream.add(format(w) + " w\n");
                    float xm = x1 + (w / 2);
                    currentStream.add(format(xm) + " " + format(y1) + " m "
                            + format(xm) + " " + format(y2) + " l S\n");
                }
                break;
            case Constants.EN_DOTTED:
                setColor(col, false, null);
                currentStream.add("1 J ");
                if (horz) {
                    float unit = Math.abs(2 * h);
                    int rep = (int)(w / unit);
                    if (rep % 2 == 0) {
                        rep++;
                    }
                    unit = w / rep;
                    currentStream.add("[0 " + format(unit) + "] 0 d ");
                    currentStream.add(format(h) + " w\n");
                    float ym = y1 + (h / 2);
                    currentStream.add(format(x1) + " " + format(ym) + " m "
                            + format(x2) + " " + format(ym) + " l S\n");
                } else {
                    float unit = Math.abs(2 * w);
                    int rep = (int)(h / unit);
                    if (rep % 2 == 0) {
                        rep++;
                    }
                    unit = h / rep;
                    currentStream.add("[0 " + format(unit) + " ] 0 d ");
                    currentStream.add(format(w) + " w\n");
                    float xm = x1 + (w / 2);
                    currentStream.add(format(xm) + " " + format(y1) + " m "
                            + format(xm) + " " + format(y2) + " l S\n");
                }
                break;
            case Constants.EN_DOUBLE:
                setColor(col, false, null);
                currentStream.add("[] 0 d ");
                if (horz) {
                    float h3 = h / 3;
                    currentStream.add(format(h3) + " w\n");
                    float ym1 = y1 + (h3 / 2);
                    float ym2 = ym1 + h3 + h3;
                    currentStream.add(format(x1) + " " + format(ym1) + " m "
                            + format(x2) + " " + format(ym1) + " l S\n");
                    currentStream.add(format(x1) + " " + format(ym2) + " m "
                            + format(x2) + " " + format(ym2) + " l S\n");
                } else {
                    float w3 = w / 3;
                    currentStream.add(format(w3) + " w\n");
                    float xm1 = x1 + (w3 / 2);
                    float xm2 = xm1 + w3 + w3;
                    currentStream.add(format(xm1) + " " + format(y1) + " m "
                            + format(xm1) + " " + format(y2) + " l S\n");
                    currentStream.add(format(xm2) + " " + format(y1) + " m "
                            + format(xm2) + " " + format(y2) + " l S\n");
                }
                break;
            case Constants.EN_GROOVE:
            case Constants.EN_RIDGE:
            {
                float colFactor = (style == EN_GROOVE ? 0.4f : -0.4f);
                currentStream.add("[] 0 d ");
                if (horz) {
                    Color uppercol = ColorUtil.lightenColor(col, -colFactor);
                    Color lowercol = ColorUtil.lightenColor(col, colFactor);
                    float h3 = h / 3;
                    currentStream.add(format(h3) + " w\n");
                    float ym1 = y1 + (h3 / 2);
                    setColor(uppercol, false, null);
                    currentStream.add(format(x1) + " " + format(ym1) + " m "
                            + format(x2) + " " + format(ym1) + " l S\n");
                    setColor(col, false, null);
                    currentStream.add(format(x1) + " " + format(ym1 + h3) + " m "
                                        + format(x2) + " " + format(ym1 + h3) + " l S\n");
                    setColor(lowercol, false, null);
                    currentStream.add(format(x1) + " " + format(ym1 + h3 + h3) + " m "
                                        + format(x2) + " " + format(ym1 + h3 + h3) + " l S\n");
                } else {
                    Color leftcol = ColorUtil.lightenColor(col, -colFactor);
                    Color rightcol = ColorUtil.lightenColor(col, colFactor);
                    float w3 = w / 3;
                    currentStream.add(format(w3) + " w\n");
                    float xm1 = x1 + (w3 / 2);
                    setColor(leftcol, false, null);
                    currentStream.add(format(xm1) + " " + format(y1) + " m "
                            + format(xm1) + " " + format(y2) + " l S\n");
                    setColor(col, false, null);
                    currentStream.add(format(xm1 + w3) + " " + format(y1) + " m "
                                        + format(xm1 + w3) + " " + format(y2) + " l S\n");
                    setColor(rightcol, false, null);
                    currentStream.add(format(xm1 + w3 + w3) + " " + format(y1) + " m "
                                        + format(xm1 + w3 + w3) + " " + format(y2) + " l S\n");
                }
                break;
            }
            case Constants.EN_INSET:
            case Constants.EN_OUTSET:
            {
                float colFactor = (style == EN_OUTSET ? 0.4f : -0.4f);
                currentStream.add("[] 0 d ");
                Color c = col;
                if (horz) {
                    c = ColorUtil.lightenColor(c, (startOrBefore ? 1 : -1) * colFactor);
                    currentStream.add(format(h) + " w\n");
                    float ym1 = y1 + (h / 2);
                    setColor(c, false, null);
                    currentStream.add(format(x1) + " " + format(ym1) + " m "
                            + format(x2) + " " + format(ym1) + " l S\n");
                } else {
                    c = ColorUtil.lightenColor(c, (startOrBefore ? 1 : -1) * colFactor);
                    currentStream.add(format(w) + " w\n");
                    float xm1 = x1 + (w / 2);
                    setColor(c, false, null);
                    currentStream.add(format(xm1) + " " + format(y1) + " m "
                            + format(xm1) + " " + format(y2) + " l S\n");
                }
                break;
            }
            case Constants.EN_HIDDEN:
                break;
            default:
                setColor(col, false, null);
                currentStream.add("[] 0 d ");
                if (horz) {
                    currentStream.add(format(h) + " w\n");
                    float ym = y1 + (h / 2);
                    currentStream.add(format(x1) + " " + format(ym) + " m "
                            + format(x2) + " " + format(ym) + " l S\n");
                } else {
                    currentStream.add(format(w) + " w\n");
                    float xm = x1 + (w / 2);
                    currentStream.add(format(xm) + " " + format(y1) + " m "
                            + format(xm) + " " + format(y2) + " l S\n");
                }
        }
    }

    /**
     * Sets the current line width in points.
     * @param width line width in points
     */
    private void updateLineWidth(float width) {
        if (currentState.setLineWidth(width)) {
            //Only write if value has changed WRT the current line width
            currentStream.add(format(width) + " w\n");
        }
    }

    /** {@inheritDoc} */
    protected void clipRect(float x, float y, float width, float height) {
        currentStream.add(format(x) + " " + format(y) + " "
                + format(width) + " " + format(height) + " re ");
        clip();
    }

    /**
     * Clip an area.
     */
    protected void clip() {
        currentStream.add("W\n");
        currentStream.add("n\n");
    }

    /**
     * Moves the current point to (x, y), omitting any connecting line segment.
     * @param x x coordinate
     * @param y y coordinate
     */
    protected void moveTo(float x, float y) {
        currentStream.add(format(x) + " " + format(y) + " m ");
    }

    /**
     * Appends a straight line segment from the current point to (x, y). The
     * new current point is (x, y).
     * @param x x coordinate
     * @param y y coordinate
     */
    protected void lineTo(float x, float y) {
        currentStream.add(format(x) + " " + format(y) + " l ");
    }

    /**
     * Closes the current subpath by appending a straight line segment from
     * the current point to the starting point of the subpath.
     */
    protected void closePath() {
        currentStream.add("h ");
    }

    /**
     * {@inheritDoc}
     */
    protected void fillRect(float x, float y, float w, float h) {
        if (w != 0 && h != 0) {
            currentStream.add(format(x) + " " + format(y) + " "
                    + format(w) + " " + format(h) + " re f\n");
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
        currentStream.add(format(startx) + " " + format(starty) + " m ");
        currentStream.add(format(endx) + " " + format(endy) + " l S\n");
    }

    /**
     * Breaks out of the state stack to handle fixed block-containers.
     * @return the saved state stack to recreate later
     */
    protected List breakOutOfStateStack() {
        List breakOutList = new java.util.ArrayList();
        PDFState.Data data;
        while (true) {
            data = currentState.getData();
            if (currentState.pop() == null) {
                break;
            }
            if (breakOutList.size() == 0) {
                comment("------ break out!");
            }
            breakOutList.add(0, data); //Insert because of stack-popping
            restoreGraphicsState(false);
        }
        return breakOutList;
    }

    /**
     * Restores the state stack after a break out.
     * @param breakOutList the state stack to restore.
     */
    protected void restoreStateStackAfterBreakOut(List breakOutList) {
        comment("------ restoring context after break-out...");
        PDFState.Data data;
        Iterator i = breakOutList.iterator();
        while (i.hasNext()) {
            data = (PDFState.Data)i.next();
            saveGraphicsState();
            AffineTransform at = data.getTransform();
            concatenateTransformationMatrix(at);
            //TODO Break-out: Also restore items such as line width and color
            //Left out for now because all this painting stuff is very
            //inconsistent. Some values go over PDFState, some don't.
        }
        comment("------ done.");
    }

    /**
     * Returns area's id if it is the first area in the document with that id
     * (i.e. if the area qualifies as a link target).
     * Otherwise, or if the area has no id, null is returned.
     *
     * <i>NOTE</i>: area must be on currentPageViewport, otherwise result may be wrong!
     *
     * @param area the area for which to return the id
     * @return the area's id (null if the area has no id or
     *              other preceding areas have the same id)
     */
    protected String getTargetableID(Area area) {
        String id = (String) area.getTrait(Trait.PROD_ID);
        if (id == null || id.length() == 0
            || !currentPageViewport.isFirstWithID(id)
            || idPositions.containsKey(id)) {
            return null;
        } else {
            return id;
        }
    }

    /**
     * Set XY position in the PDFGoTo and add it to the PDF trailer.
     *
     * @param gt the PDFGoTo object
     * @param position the X,Y position to set
     */
    protected void finishIDGoTo(PDFGoTo gt, Point2D.Float position) {
        gt.setPosition(position);
        pdfDoc.addTrailerObject(gt);
        unfinishedGoTos.remove(gt);
    }

    /**
     * Set page reference and XY position in the PDFGoTo and add it to the PDF trailer.
     *
     * @param gt the PDFGoTo object
     * @param pdfPageRef the PDF reference string of the target page object
     * @param position the X,Y position to set
     */
    protected void finishIDGoTo(PDFGoTo gt, String pdfPageRef, Point2D.Float position) {
        gt.setPageReference(pdfPageRef);
        finishIDGoTo(gt, position);
    }

    /**
     * Get a PDFGoTo pointing to the given id. Create one if necessary.
     * It is possible that the PDFGoTo is not fully resolved yet. In that case
     * it must be completed (and added to the PDF trailer) later.
     *
     * @param targetID the target id of the PDFGoTo
     * @param pvKey the unique key of the target PageViewport
     *
     * @return the PDFGoTo that was found or created
     */
    protected PDFGoTo getPDFGoToForID(String targetID, String pvKey) {
        // Already a PDFGoTo present for this target? If not, create.
        PDFGoTo gt = (PDFGoTo) idGoTos.get(targetID);
        if (gt == null) {
            String pdfPageRef = (String) pageReferences.get(pvKey);
            Point2D.Float position = (Point2D.Float) idPositions.get(targetID);
            // can the GoTo already be fully filled in?
            if (pdfPageRef != null && position != null) {
                // getPDFGoTo shares PDFGoTo objects as much as possible.
                // It also takes care of assignObjectNumber and addTrailerObject.
                gt = pdfDoc.getFactory().getPDFGoTo(pdfPageRef, position);
            } else {
                // Not complete yet, can't use getPDFGoTo:
                gt = new PDFGoTo(pdfPageRef);
                pdfDoc.assignObjectNumber(gt);
                // pdfDoc.addTrailerObject() will be called later, from finishIDGoTo()
                unfinishedGoTos.add(gt);
            }
            idGoTos.put(targetID, gt);
        }
        return gt;
    }

    /**
     * Saves id's absolute position on page for later retrieval by PDFGoTos
     *
     * @param id the id of the area whose position must be saved
     * @param pdfPageRef the PDF page reference string
     * @param relativeIPP the *relative* IP position in millipoints
     * @param relativeBPP the *relative* BP position in millipoints
     * @param tf the transformation to apply once the relative positions have been
     *           converted to points
     */
    protected void saveAbsolutePosition(String id, String pdfPageRef,
                                        int relativeIPP, int relativeBPP, AffineTransform tf) {
        Point2D.Float position = new Point2D.Float(relativeIPP / 1000f, relativeBPP / 1000f);
        tf.transform(position, position);
        idPositions.put(id, position);
        // is there already a PDFGoTo waiting to be completed?
        PDFGoTo gt = (PDFGoTo) idGoTos.get(id);
        if (gt != null) {
            finishIDGoTo(gt, pdfPageRef, position);
        }
/*
        // The code below auto-creates a named destination for every id in the document.
        // This should probably be controlled by a user-configurable setting, as it may
        // make the PDF file grow noticeably.
        // *** NOT YET WELL-TESTED ! ***
        if (true) {
            PDFFactory factory = pdfDoc.getFactory();
            if (gt == null) {
                gt = factory.getPDFGoTo(pdfPageRef, position);
                idGoTos.put(id, gt);  // so others can pick it up too
            }
            factory.makeDestination(id, gt.referencePDF(), currentPageViewport);
            // Note: using currentPageViewport is only correct if the id is indeed on
            // the current PageViewport. But even if incorrect, it won't interfere with
            // what gets created in the PDF.
            // For speedup, we should also create a lookup map id -> PDFDestination
        }
*/
    }

    /**
     * Saves id's absolute position on page for later retrieval by PDFGoTos,
     * using the currently valid transformation and the currently valid PDF page reference
     *
     * @param id the id of the area whose position must be saved
     * @param relativeIPP the *relative* IP position in millipoints
     * @param relativeBPP the *relative* BP position in millipoints
     */
    protected void saveAbsolutePosition(String id, int relativeIPP, int relativeBPP) {
        saveAbsolutePosition(id, currentPageRef,
                             relativeIPP, relativeBPP, currentState.getTransform());
    }

    /**
     * If the given block area is a possible link target, its id + absolute position will
     * be saved. The saved position is only correct if this function is called at the very
     * start of renderBlock!
     *
     * @param block the block area in question
     */
    protected void saveBlockPosIfTargetable(Block block) {
        String id = getTargetableID(block);
        if (id != null) {
            // FIXME: Like elsewhere in the renderer code, absolute and relative
            //        directions are happily mixed here. This makes sure that the
            //        links point to the right location, but it is not correct.
            int ipp = block.getXOffset();
            int bpp = block.getYOffset() + block.getSpaceBefore();
            int positioning = block.getPositioning();
            if (!(positioning == Block.FIXED || positioning == Block.ABSOLUTE)) {
                ipp += currentIPPosition;
                bpp += currentBPPosition;
            }
            AffineTransform tf = positioning == Block.FIXED
                ? currentState.getBaseTransform()
                : currentState.getTransform();
            saveAbsolutePosition(id, currentPageRef, ipp, bpp, tf);
        }
    }

    /**
     * If the given inline area is a possible link target, its id + absolute position will
     * be saved. The saved position is only correct if this function is called at the very
     * start of renderInlineArea!
     *
     * @param inlineArea the inline area in question
     */
    protected void saveInlinePosIfTargetable(InlineArea inlineArea) {
        String id = getTargetableID(inlineArea);
        if (id != null) {
            int extraMarginBefore = 5000; // millipoints
            int ipp = currentIPPosition;
            int bpp = currentBPPosition + inlineArea.getOffset() - extraMarginBefore;
            saveAbsolutePosition(id, ipp, bpp);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void renderBlock(Block block) {
        saveBlockPosIfTargetable(block);
        super.renderBlock(block);
    }

    /** {@inheritDoc} */
    protected void renderLineArea(LineArea line) {
        super.renderLineArea(line);
    }

    /**
     * {@inheritDoc}
     */
    protected void renderInlineArea(InlineArea inlineArea) {
        saveInlinePosIfTargetable(inlineArea);
        super.renderInlineArea(inlineArea);
    }

    /**
     * Render inline parent area.
     * For pdf this handles the inline parent area traits such as
     * links, border, background.
     * @param ip the inline parent area
     */
    public void renderInlineParent(InlineParent ip) {

        boolean annotsAllowed = pdfDoc.getProfile().isAnnotationAllowed();

        // stuff we only need if a link must be created:
        Rectangle2D ipRect = null;
        PDFFactory factory = null;
        PDFAction action   = null;
        if (annotsAllowed) {
            // make sure the rect is determined *before* calling super!
            int ipp = currentIPPosition;
            int bpp = currentBPPosition + ip.getOffset();
            ipRect = new Rectangle2D.Float(ipp / 1000f, bpp / 1000f,
                                           ip.getIPD() / 1000f, ip.getBPD() / 1000f);
            AffineTransform transform = currentState.getTransform();
            ipRect = transform.createTransformedShape(ipRect).getBounds2D();

            factory = pdfDoc.getFactory();
        }

        // render contents
        super.renderInlineParent(ip);

        boolean linkTraitFound = false;

        // try INTERNAL_LINK first
        Trait.InternalLink intLink = (Trait.InternalLink) ip.getTrait(Trait.INTERNAL_LINK);
        if (intLink != null) {
            linkTraitFound = true;
            String pvKey = intLink.getPVKey();
            String idRef = intLink.getIDRef();
            boolean pvKeyOK = pvKey != null && pvKey.length() > 0;
            boolean idRefOK = idRef != null && idRef.length() > 0;
            if (pvKeyOK && idRefOK) {
                if (annotsAllowed) {
                    action = getPDFGoToForID(idRef, pvKey);
                }
            } else {
                //Warnings already issued by AreaTreeHandler
            }
        }

        // no INTERNAL_LINK, look for EXTERNAL_LINK
        if (!linkTraitFound) {
            Trait.ExternalLink extLink = (Trait.ExternalLink) ip.getTrait(Trait.EXTERNAL_LINK);
            if (extLink != null) {
                String extDest = extLink.getDestination();
                if (extDest != null && extDest.length() > 0) {
                    linkTraitFound = true;
                    if (annotsAllowed) {
                        action = factory.getExternalAction(extDest, extLink.newWindow());
                    }
                }
            }
        }

        // warn if link trait found but not allowed, else create link
        if (linkTraitFound) {
            if (!annotsAllowed) {
                log.warn("Skipping annotation for a link due to PDF profile: "
                         + pdfDoc.getProfile());
            } else if (action != null) {
                PDFLink pdfLink = factory.makeLink(ipRect, action);
                currentPage.addAnnotation(pdfLink);
            }
        }
    }

    private Typeface getTypeface(String fontName) {
        Typeface tf = (Typeface) fontInfo.getFonts().get(fontName);
        if (tf instanceof LazyFont) {
            tf = ((LazyFont)tf).getRealFont();
        }
        return tf;
    }

    /** {@inheritDoc} */
    public void renderText(TextArea text) {
        renderInlineAreaBackAndBorders(text);
        Color ct = (Color) text.getTrait(Trait.COLOR);
        updateColor(ct, true);

        beginTextObject();

        String fontName = getInternalFontNameForArea(text);
        int size = ((Integer) text.getTrait(Trait.FONT_SIZE)).intValue();

        // This assumes that *all* CIDFonts use a /ToUnicode mapping
        Typeface tf = getTypeface(fontName);

        textutil.updateTf(fontName, size / 1000f, tf.isMultiByte());


        // word.getOffset() = only height of text itself
        // currentBlockIPPosition: 0 for beginning of line; nonzero
        //  where previous line area failed to take up entire allocated space
        int rx = currentIPPosition + text.getBorderAndPaddingWidthStart();
        int bl = currentBPPosition + text.getOffset() + text.getBaselineOffset();

        textutil.writeTextMatrix(new AffineTransform(1, 0, 0, -1, rx / 1000f, bl / 1000f));

        super.renderText(text);

        textutil.writeTJ();

        renderTextDecoration(tf, size, text, bl, rx);
    }

    /** {@inheritDoc} */
    public void renderWord(WordArea word) {
        Font font = getFontFromArea(word.getParentArea());
        String s = word.getWord();

        escapeText(s, word.getLetterAdjustArray(),
                font, (AbstractTextArea)word.getParentArea());

        super.renderWord(word);
    }

    /** {@inheritDoc} */
    public void renderSpace(SpaceArea space) {
        Font font = getFontFromArea(space.getParentArea());
        String s = space.getSpace();

        AbstractTextArea textArea = (AbstractTextArea)space.getParentArea();
        escapeText(s, null, font, textArea);

        if (space.isAdjustable()) {
            int tws = -((TextArea) space.getParentArea()).getTextWordSpaceAdjust()
                         - 2 * textArea.getTextLetterSpaceAdjust();

            if (tws != 0) {
                float adjust = tws / (font.getFontSize() / 1000f);
                textutil.adjustGlyphTJ(adjust);
            }
        }

        super.renderSpace(space);
    }

    /**
     * Escapes text according to PDF rules.
     * @param s Text to escape
     * @param letterAdjust an array of widths for letter adjustment (may be null)
     * @param font to font in use
     * @param parentArea the parent text area to retrieve certain traits from
     */
    protected void escapeText(String s,
                           int[] letterAdjust,
                           Font font, AbstractTextArea parentArea) {
        escapeText(s, 0, s.length(), letterAdjust, font, parentArea);
    }

    /**
     * Escapes text according to PDF rules.
     * @param s Text to escape
     * @param start the start position in the text
     * @param end the end position in the text
     * @param letterAdjust an array of widths for letter adjustment (may be null)
     * @param font to font in use
     * @param parentArea the parent text area to retrieve certain traits from
     */
    protected void escapeText(String s, int start, int end,
                           int[] letterAdjust,
                           Font font, AbstractTextArea parentArea) {
        String fontName = font.getFontName();
        float fontSize = font.getFontSize() / 1000f;
        Typeface tf = getTypeface(fontName);
        SingleByteFont singleByteFont = null;
        if (tf instanceof SingleByteFont) {
            singleByteFont = (SingleByteFont)tf;
        }

        int l = s.length();

        for (int i = start; i < end; i++) {
            char orgChar = s.charAt(i);
            char ch;
            float glyphAdjust = 0;
            if (font.hasChar(orgChar)) {
                ch = font.mapChar(orgChar);
                if (singleByteFont != null && singleByteFont.hasAdditionalEncodings()) {
                    int encoding = ch / 256;
                    if (encoding == 0) {
                        textutil.updateTf(fontName, fontSize, tf.isMultiByte());
                    } else {
                        textutil.updateTf(fontName + "_" + Integer.toString(encoding),
                                fontSize, tf.isMultiByte());
                        ch = (char)(ch % 256);
                    }
                }
                int tls = (i < l - 1 ? parentArea.getTextLetterSpaceAdjust() : 0);
                glyphAdjust -= tls;
            } else {
                if (CharUtilities.isFixedWidthSpace(orgChar)) {
                    //Fixed width space are rendered as spaces so copy/paste works in a reader
                    ch = font.mapChar(CharUtilities.SPACE);
                    glyphAdjust = font.getCharWidth(ch) - font.getCharWidth(orgChar);
                } else {
                    ch = font.mapChar(orgChar);
                }
            }
            if (letterAdjust != null && i < l - 1) {
                glyphAdjust -= letterAdjust[i + 1];
            }

            textutil.writeTJMappedChar(ch);

            float adjust = glyphAdjust / fontSize;

            if (adjust != 0) {
                textutil.adjustGlyphTJ(adjust);
            }

        }
    }

    /**
     * Establishes a new foreground or fill color. In contrast to updateColor
     * this method does not check the PDFState for optimization possibilities.
     * @param col the color to apply
     * @param fill true to set the fill color, false for the foreground color
     * @param pdf StringBuffer to write the PDF code to, if null, the code is
     *     written to the current stream.
     */
    protected void setColor(Color col, boolean fill, StringBuffer pdf) {
        PDFColor color = new PDFColor(this.pdfDoc, col);

        if (pdf != null) {
            pdf.append(color.getColorSpaceOut(fill));
        } else {
            currentStream.add(color.getColorSpaceOut(fill));
        }
    }

    /**
     * Establishes a new foreground or fill color.
     * @param col the color to apply (null skips this operation)
     * @param fill true to set the fill color, false for the foreground color
     * @param pdf StringBuffer to write the PDF code to, if null, the code is
     *     written to the current stream.
     */
    private void updateColor(Color col, boolean fill, StringBuffer pdf) {
        if (col == null) {
            return;
        }
        boolean update = false;
        if (fill) {
            update = currentState.setBackColor(col);
        } else {
            update = currentState.setColor(col);
        }

        if (update) {
            setColor(col, fill, pdf);
        }
    }

    /** {@inheritDoc} */
    protected void updateColor(Color col, boolean fill) {
        updateColor(col, fill, null);
    }

    /** {@inheritDoc} */
    public void renderImage(Image image, Rectangle2D pos) {
        endTextObject();
        String url = image.getURL();
        putImage(url, pos, image.getForeignAttributes());
    }

    /** {@inheritDoc} */
    protected void drawImage(String url, Rectangle2D pos, Map foreignAttributes) {
        endTextObject();
        putImage(url, pos, foreignAttributes);
    }

    /**
     * Adds a PDF XObject (a bitmap or form) to the PDF that will later be referenced.
     * @param uri URL of the bitmap
     * @param pos Position of the bitmap
     * @deprecated Use {@link #putImage(String, Rectangle2D, Map)} instead.
     */
    protected void putImage(String uri, Rectangle2D pos) {
        putImage(uri, pos, null);
    }

    /**
     * Adds a PDF XObject (a bitmap or form) to the PDF that will later be referenced.
     * @param uri URL of the bitmap
     * @param pos Position of the bitmap
     * @param foreignAttributes foreign attributes associated with the image
     */
    protected void putImage(String uri, Rectangle2D pos, Map foreignAttributes) {
        Rectangle posInt = new Rectangle(
                (int)pos.getX(),
                (int)pos.getY(),
                (int)pos.getWidth(),
                (int)pos.getHeight());

        uri = URISpecification.getURL(uri);
        PDFXObject xobject = pdfDoc.getXObject(uri);
        if (xobject != null) {
            float w = (float) pos.getWidth() / 1000f;
            float h = (float) pos.getHeight() / 1000f;
            placeImage((float)pos.getX() / 1000f,
                       (float)pos.getY() / 1000f, w, h, xobject);
            return;
        }
        Point origin = new Point(currentIPPosition, currentBPPosition);
        int x = origin.x + posInt.x;
        int y = origin.y + posInt.y;

        ImageManager manager = getUserAgent().getFactory().getImageManager();
        ImageInfo info = null;
        try {
            ImageSessionContext sessionContext = getUserAgent().getImageSessionContext();
            info = manager.getImageInfo(uri, sessionContext);

            Map hints = ImageUtil.getDefaultHints(sessionContext);
            org.apache.xmlgraphics.image.loader.Image img = manager.getImage(
                        info, imageHandlerRegistry.getSupportedFlavors(), hints, sessionContext);

            //First check for a dynamically registered handler
            PDFImageHandler handler = imageHandlerRegistry.getHandler(img.getClass());
            if (handler != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Using PDFImageHandler: " + handler.getClass().getName());
                }
                try {
                    RendererContext context = createRendererContext(
                            x, y, posInt.width, posInt.height, foreignAttributes);
                    handler.generateImage(context, img, origin, posInt);
                } catch (IOException ioe) {
                    ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(
                            getUserAgent().getEventBroadcaster());
                    eventProducer.imageWritingError(this, ioe);
                    return;
                }
            } else {
                throw new UnsupportedOperationException(
                        "No PDFImageHandler available for image: "
                            + info + " (" + img.getClass().getName() + ")");
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

        // output new data
        try {
            this.pdfDoc.output(ostream);
        } catch (IOException ioe) {
            // ioexception will be caught later
        }
    }

    /**
     * Places a previously registered image at a certain place on the page.
     * @param x X coordinate
     * @param y Y coordinate
     * @param w width for image
     * @param h height for image
     * @param xobj the image XObject
     */
    public void placeImage(float x, float y, float w, float h, PDFXObject xobj) {
        saveGraphicsState();
        currentStream.add(format(w) + " 0 0 "
                          + format(-h) + " "
                          + format(currentIPPosition / 1000f + x) + " "
                          + format(currentBPPosition / 1000f + h + y)
                          + " cm\n" + xobj.getName() + " Do\n");
        restoreGraphicsState();
    }

    /** {@inheritDoc} */
    protected RendererContext createRendererContext(int x, int y, int width, int height,
            Map foreignAttributes) {
        RendererContext context = super.createRendererContext(
                x, y, width, height, foreignAttributes);
        context.setProperty(PDFRendererContextConstants.PDF_DOCUMENT, pdfDoc);
        context.setProperty(PDFRendererContextConstants.OUTPUT_STREAM, ostream);
        context.setProperty(PDFRendererContextConstants.PDF_STATE, currentState);
        context.setProperty(PDFRendererContextConstants.PDF_PAGE, currentPage);
        context.setProperty(PDFRendererContextConstants.PDF_CONTEXT,
                    currentContext == null ? currentPage : currentContext);
        context.setProperty(PDFRendererContextConstants.PDF_CONTEXT, currentContext);
        context.setProperty(PDFRendererContextConstants.PDF_STREAM, currentStream);
        context.setProperty(PDFRendererContextConstants.PDF_FONT_INFO, fontInfo);
        context.setProperty(PDFRendererContextConstants.PDF_FONT_NAME, "");
        context.setProperty(PDFRendererContextConstants.PDF_FONT_SIZE, new Integer(0));
        return context;
    }

    /**
     * Render leader area.
     * This renders a leader area which is an area with a rule.
     * @param area the leader area to render
     */
    public void renderLeader(Leader area) {
        renderInlineAreaBackAndBorders(area);

        currentState.push();
        saveGraphicsState();
        int style = area.getRuleStyle();
        float startx = (currentIPPosition + area.getBorderAndPaddingWidthStart()) / 1000f;
        float starty = (currentBPPosition + area.getOffset()) / 1000f;
        float endx = (currentIPPosition + area.getBorderAndPaddingWidthStart()
                        + area.getIPD()) / 1000f;
        float ruleThickness = area.getRuleThickness() / 1000f;
        Color col = (Color)area.getTrait(Trait.COLOR);

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
                currentStream.add("1 0 0 1 " + format(ruleThickness / 2) + " 0 cm\n");
                drawBorderLine(startx, starty, endx, starty + ruleThickness,
                        true, true, style, col);
                break;
            case EN_GROOVE:
            case EN_RIDGE:
                float half = area.getRuleThickness() / 2000f;

                setColor(ColorUtil.lightenColor(col, 0.6f), true, null);
                currentStream.add(format(startx) + " " + format(starty) + " m\n");
                currentStream.add(format(endx) + " " + format(starty) + " l\n");
                currentStream.add(format(endx) + " " + format(starty + 2 * half) + " l\n");
                currentStream.add(format(startx) + " " + format(starty + 2 * half) + " l\n");
                currentStream.add("h\n");
                currentStream.add("f\n");
                setColor(col, true, null);
                if (style == EN_GROOVE) {
                    currentStream.add(format(startx) + " " + format(starty) + " m\n");
                    currentStream.add(format(endx) + " " + format(starty) + " l\n");
                    currentStream.add(format(endx) + " " + format(starty + half) + " l\n");
                    currentStream.add(format(startx + half) + " " + format(starty + half) + " l\n");
                    currentStream.add(format(startx) + " " + format(starty + 2 * half) + " l\n");
                } else {
                    currentStream.add(format(endx) + " " + format(starty) + " m\n");
                    currentStream.add(format(endx) + " " + format(starty + 2 * half) + " l\n");
                    currentStream.add(format(startx) + " " + format(starty + 2 * half) + " l\n");
                    currentStream.add(format(startx) + " " + format(starty + half) + " l\n");
                    currentStream.add(format(endx - half) + " " + format(starty + half) + " l\n");
                }
                currentStream.add("h\n");
                currentStream.add("f\n");
                break;
            default:
                throw new UnsupportedOperationException("rule style not supported");
        }

        restoreGraphicsState();
        currentState.pop();
        beginTextObject();
        super.renderLeader(area);
    }

    /** {@inheritDoc} */
    public String getMimeType() {
        return MIME_TYPE;
    }

    /**
     * Sets the PDF/A mode for the PDF renderer.
     * @param mode the PDF/A mode
     */
    public void setAMode(PDFAMode mode) {
        this.pdfAMode = mode;
    }

    /**
     * Sets the PDF/X mode for the PDF renderer.
     * @param mode the PDF/X mode
     */
    public void setXMode(PDFXMode mode) {
        this.pdfXMode = mode;
    }

    /**
     * Sets the output color profile for the PDF renderer.
     * @param outputProfileURI the URI to the output color profile
     */
    public void setOutputProfileURI(String outputProfileURI) {
        this.outputProfileURI = outputProfileURI;
    }

    /**
     * Sets the filter map to be used by the PDF renderer.
     * @param filterMap the filter map
     */
    public void setFilterMap(Map filterMap) {
        this.filterMap = filterMap;
    }

    /**
     * Sets the encryption parameters used by the PDF renderer.
     * @param encryptionParams the encryption parameters
     */
    public void setEncryptionParams(PDFEncryptionParams encryptionParams) {
        this.encryptionParams = encryptionParams;
    }
}

