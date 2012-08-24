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

package org.apache.fop.render.afp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.fop.afp.AFPDitheredRectanglePainter;
import org.apache.fop.afp.AFPPaintingState;
import org.apache.fop.afp.AFPRectanglePainter;
import org.apache.fop.afp.AFPResourceLevelDefaults;
import org.apache.fop.afp.AFPResourceManager;
import org.apache.fop.afp.AFPUnitConverter;
import org.apache.fop.afp.AbstractAFPPainter;
import org.apache.fop.afp.DataStream;
import org.apache.fop.afp.fonts.AFPFontCollection;
import org.apache.fop.afp.fonts.AFPPageFonts;
import org.apache.fop.afp.modca.ResourceObject;
import org.apache.fop.afp.util.AFPResourceAccessor;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.fonts.FontCollection;
import org.apache.fop.fonts.FontEventAdapter;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontManager;
import org.apache.fop.render.afp.AFPRendererConfig.AFPRendererConfigParser;
import org.apache.fop.render.afp.extensions.AFPElementMapping;
import org.apache.fop.render.afp.extensions.AFPIncludeFormMap;
import org.apache.fop.render.afp.extensions.AFPInvokeMediumMap;
import org.apache.fop.render.afp.extensions.AFPPageOverlay;
import org.apache.fop.render.afp.extensions.AFPPageSegmentElement;
import org.apache.fop.render.afp.extensions.AFPPageSetup;
import org.apache.fop.render.afp.extensions.ExtensionPlacement;
import org.apache.fop.render.intermediate.AbstractBinaryWritingIFDocumentHandler;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFDocumentHandlerConfigurator;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.IFPainter;

/**
 * {@link org.apache.fop.render.intermediate.IFDocumentHandler} implementation that produces AFP
 * (MO:DCA).
 */
public class AFPDocumentHandler extends AbstractBinaryWritingIFDocumentHandler
            implements AFPCustomizable {

    //** logging instance */
    //private static Log log = LogFactory.getLog(AFPDocumentHandler.class);

    /** the resource manager */
    private AFPResourceManager resourceManager;

    /** the painting state */
    private final AFPPaintingState paintingState;

    /** unit converter */
    private final AFPUnitConverter unitConv;

    /** the AFP datastream */
    private DataStream dataStream;

    /** the map of page segments */
    private Map<String, PageSegmentDescriptor> pageSegmentMap
        = new java.util.HashMap<String, PageSegmentDescriptor>();

    private static enum Location {
        ELSEWHERE, IN_DOCUMENT_HEADER, FOLLOWING_PAGE_SEQUENCE, IN_PAGE_HEADER
    }

    private Location location = Location.ELSEWHERE;

    /** temporary holds extensions that have to be deferred until the end of the page-sequence */
    private List<AFPPageSetup> deferredPageSequenceExtensions
        = new java.util.LinkedList<AFPPageSetup>();

    /** the shading mode for filled rectangles */
    private AFPShadingMode shadingMode = AFPShadingMode.COLOR;

    /**
     * Default constructor.
     */
    public AFPDocumentHandler(IFContext context) {
        super(context);
        this.resourceManager = new AFPResourceManager(context.getUserAgent().getResourceResolver());
        this.paintingState = new AFPPaintingState();
        this.unitConv = paintingState.getUnitConverter();
    }

    /** {@inheritDoc} */
    public boolean supportsPagesOutOfOrder() {
        return false;
    }

    /** {@inheritDoc} */
    public String getMimeType() {
        return MimeConstants.MIME_AFP;
    }

    /** {@inheritDoc} */
    public IFDocumentHandlerConfigurator getConfigurator() {
        return new AFPRendererConfigurator(getUserAgent(), new AFPRendererConfigParser());
    }

    /** {@inheritDoc} */
    @Override
    public void setDefaultFontInfo(FontInfo fontInfo) {
        FontManager fontManager = getUserAgent().getFontManager();
        FontCollection[] fontCollections = new FontCollection[] {
            new AFPFontCollection(getUserAgent().getEventBroadcaster(), null)
        };

        FontInfo fi = (fontInfo != null ? fontInfo : new FontInfo());
        fi.setEventListener(new FontEventAdapter(getUserAgent().getEventBroadcaster()));
        fontManager.setup(fi, fontCollections);
        setFontInfo(fi);
    }

    AFPPaintingState getPaintingState() {
        return this.paintingState;
    }

    DataStream getDataStream() {
        return this.dataStream;
    }

    AFPResourceManager getResourceManager() {
        return this.resourceManager;
    }

    AbstractAFPPainter createRectanglePainter() {
        if (AFPShadingMode.DITHERED.equals(this.shadingMode)) {
            return new AFPDitheredRectanglePainter(
                    getPaintingState(), getDataStream(), getResourceManager());
        } else {
            return new AFPRectanglePainter(
                    getPaintingState(), getDataStream());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void startDocument() throws IFException {
        super.startDocument();
        try {
            paintingState.setColor(Color.WHITE);

            this.dataStream = resourceManager.createDataStream(paintingState, outputStream);

            this.dataStream.startDocument();
        } catch (IOException e) {
            throw new IFException("I/O error in startDocument()", e);
        }
    }


    /** {@inheritDoc} */
    @Override
    public void startDocumentHeader() throws IFException {
        super.startDocumentHeader();
        this.location = Location.IN_DOCUMENT_HEADER;
    }

    /** {@inheritDoc} */
    @Override
    public void endDocumentHeader() throws IFException {
        super.endDocumentHeader();
        this.location = Location.ELSEWHERE;
    }

    /** {@inheritDoc} */
    @Override
    public void endDocument() throws IFException {
        try {
            this.dataStream.endDocument();
            this.dataStream = null;
            this.resourceManager.writeToStream();
            this.resourceManager = null;
        } catch (IOException ioe) {
            throw new IFException("I/O error in endDocument()", ioe);
        }
        super.endDocument();
    }

    /** {@inheritDoc} */
    public void startPageSequence(String id) throws IFException {
        try {
            dataStream.startPageGroup();
        } catch (IOException ioe) {
            throw new IFException("I/O error in startPageSequence()", ioe);
        }
        this.location = Location.FOLLOWING_PAGE_SEQUENCE;
    }

    /** {@inheritDoc} */
    public void endPageSequence() throws IFException {
        try {
            //Process deferred page-sequence-level extensions
            Iterator<AFPPageSetup> iter = this.deferredPageSequenceExtensions.iterator();
            while (iter.hasNext()) {
                AFPPageSetup aps = iter.next();
                iter.remove();
                if (AFPElementMapping.NO_OPERATION.equals(aps.getElementName())) {
                    handleNOP(aps);
                } else {
                    throw new UnsupportedOperationException("Don't know how to handle " + aps);
                }
            }

            //End page sequence
            dataStream.endPageGroup();
        } catch (IOException ioe) {
            throw new IFException("I/O error in endPageSequence()", ioe);
        }
        this.location = Location.ELSEWHERE;
    }

    /**
     * Returns the base AFP transform
     *
     * @return the base AFP transform
     */
    private AffineTransform getBaseTransform() {
        AffineTransform baseTransform = new AffineTransform();
        double scale = unitConv.mpt2units(1);
        baseTransform.scale(scale, scale);
        return baseTransform;
    }

    /** {@inheritDoc} */
    public void startPage(int index, String name, String pageMasterName, Dimension size)
                throws IFException {
        this.location = Location.ELSEWHERE;
        paintingState.clear();

        AffineTransform baseTransform = getBaseTransform();
        paintingState.concatenate(baseTransform);

        int pageWidth = Math.round(unitConv.mpt2units(size.width));
        paintingState.setPageWidth(pageWidth);

        int pageHeight = Math.round(unitConv.mpt2units(size.height));
        paintingState.setPageHeight(pageHeight);

        int pageRotation = paintingState.getPageRotation();
        int resolution = paintingState.getResolution();

        dataStream.startPage(pageWidth, pageHeight, pageRotation,
                resolution, resolution);
    }

    /** {@inheritDoc} */
    @Override
    public void startPageHeader() throws IFException {
        super.startPageHeader();
        this.location = Location.IN_PAGE_HEADER;
    }

    /** {@inheritDoc} */
    @Override
    public void endPageHeader() throws IFException {
        this.location = Location.ELSEWHERE;
        super.endPageHeader();
    }

    /** {@inheritDoc} */
    public IFPainter startPageContent() throws IFException {
        return new AFPPainter(this);
    }

    /** {@inheritDoc} */
    public void endPageContent() throws IFException {
    }

    /** {@inheritDoc} */
    public void endPage() throws IFException {
        try {
            AFPPageFonts pageFonts = paintingState.getPageFonts();
            if (pageFonts != null && !pageFonts.isEmpty()) {
                dataStream.addFontsToCurrentPage(pageFonts);
            }

            dataStream.endPage();
        } catch (IOException ioe) {
            throw new IFException("I/O error in endPage()", ioe);
        }
    }

    /** {@inheritDoc} */
    public void handleExtensionObject(Object extension) throws IFException {
        if (extension instanceof AFPPageSetup) {
            AFPPageSetup aps = (AFPPageSetup)extension;
            String element = aps.getElementName();
            if (AFPElementMapping.TAG_LOGICAL_ELEMENT.equals(element)) {
                switch (this.location) {
                case FOLLOWING_PAGE_SEQUENCE:
                case IN_PAGE_HEADER:
                    String name = aps.getName();
                    String value = aps.getValue();
                    int encoding = aps.getEncoding();
                    dataStream.createTagLogicalElement(name, value, encoding);
                    break;
                default:
                    throw new IFException(
                        "TLE extension must be in the page header or between page-sequence"
                            + " and the first page: " + aps, null);
                }
            } else if (AFPElementMapping.NO_OPERATION.equals(element)) {
                switch (this.location) {
                case FOLLOWING_PAGE_SEQUENCE:
                    if (aps.getPlacement() == ExtensionPlacement.BEFORE_END) {
                        this.deferredPageSequenceExtensions.add(aps);
                        break;
                    }
                case IN_DOCUMENT_HEADER:
                case IN_PAGE_HEADER:
                    handleNOP(aps);
                    break;
                default:
                    throw new IFException(
                            "NOP extension must be in the document header, the page header"
                                + " or between page-sequence"
                                + " and the first page: " + aps, null);
                }
            } else {
                if (this.location != Location.IN_PAGE_HEADER) {
                    throw new IFException(
                        "AFP page setup extension encountered outside the page header: " + aps,
                        null);
                }
                if (AFPElementMapping.INCLUDE_PAGE_SEGMENT.equals(element)) {
                    AFPPageSegmentElement.AFPPageSegmentSetup apse
                        = (AFPPageSegmentElement.AFPPageSegmentSetup)aps;
                    String name = apse.getName();
                    String source = apse.getValue();
                    String uri = apse.getResourceSrc();
                    pageSegmentMap.put(source, new PageSegmentDescriptor(name, uri));
                }
            }
        } else if (extension instanceof AFPPageOverlay) {
            AFPPageOverlay ipo = (AFPPageOverlay)extension;
            if (this.location != Location.IN_PAGE_HEADER) {
                    throw new IFException(
                        "AFP page overlay extension encountered outside the page header: " + ipo,
                        null);
            }
            String overlay = ipo.getName();
            if (overlay != null) {
                dataStream.createIncludePageOverlay(overlay, ipo.getX(), ipo.getY());
            }
        } else if (extension instanceof AFPInvokeMediumMap) {
            if (this.location != Location.FOLLOWING_PAGE_SEQUENCE
                    && this.location != Location.IN_PAGE_HEADER) {

                throw new IFException(
                    "AFP IMM extension must be between page-sequence"
                    + " and the first page or child of page-header: "
                    + extension, null);
            }
            AFPInvokeMediumMap imm = (AFPInvokeMediumMap)extension;
            String mediumMap = imm.getName();
            if (mediumMap != null) {
                dataStream.createInvokeMediumMap(mediumMap);
            }
        } else if (extension instanceof AFPIncludeFormMap) {
            AFPIncludeFormMap formMap = (AFPIncludeFormMap)extension;
            AFPResourceAccessor accessor = new AFPResourceAccessor(
                    getUserAgent().getResourceResolver());
            try {
                getResourceManager().createIncludedResource(formMap.getName(),
                        formMap.getSrc(), accessor,
                        ResourceObject.TYPE_FORMDEF);
            } catch (IOException ioe) {
                throw new IFException(
                        "I/O error while embedding form map resource: " + formMap.getName(), ioe);
            }
        }
    }

    private void handleNOP(AFPPageSetup nop) {
        String content = nop.getContent();
        if (content != null) {
            dataStream.createNoOperation(content);
        }
    }

    // ---=== AFPCustomizable ===---

    /** {@inheritDoc} */
    public void setBitsPerPixel(int bitsPerPixel) {
        paintingState.setBitsPerPixel(bitsPerPixel);
    }

    /** {@inheritDoc} */
    public void setColorImages(boolean colorImages) {
        paintingState.setColorImages(colorImages);
    }

    /** {@inheritDoc} */
    public void setNativeImagesSupported(boolean nativeImages) {
        paintingState.setNativeImagesSupported(nativeImages);
    }

    /** {@inheritDoc} */
    public void setCMYKImagesSupported(boolean value) {
        paintingState.setCMYKImagesSupported(value);
    }

    /** {@inheritDoc} */
    public void setDitheringQuality(float quality) {
        this.paintingState.setDitheringQuality(quality);
    }

    /** {@inheritDoc} */
    public void setBitmapEncodingQuality(float quality) {
        this.paintingState.setBitmapEncodingQuality(quality);
    }

    /** {@inheritDoc} */
    public void setShadingMode(AFPShadingMode shadingMode) {
        this.shadingMode = shadingMode;
    }

    /** {@inheritDoc} */
    public void setResolution(int resolution) {
        paintingState.setResolution(resolution);
    }

    /** {@inheritDoc} */
    public void setLineWidthCorrection(float correction) {
        paintingState.setLineWidthCorrection(correction);
    }

    /** {@inheritDoc} */
    public int getResolution() {
        return paintingState.getResolution();
    }

    /** {@inheritDoc} */
    public void setGOCAEnabled(boolean enabled) {
        this.paintingState.setGOCAEnabled(enabled);
    }

    /** {@inheritDoc} */
    public boolean isGOCAEnabled() {
        return this.paintingState.isGOCAEnabled();
    }

    /** {@inheritDoc} */
    public void setStrokeGOCAText(boolean stroke) {
        this.paintingState.setStrokeGOCAText(stroke);
    }

    /** {@inheritDoc} */
    public boolean isStrokeGOCAText() {
        return this.paintingState.isStrokeGOCAText();
    }

    /** {@inheritDoc} */
    public void setWrapPSeg(boolean pSeg) {
        paintingState.setWrapPSeg(pSeg);
    }

    /** {@inheritDoc} */
    public void setFS45(boolean fs45) {
        paintingState.setFS45(fs45);
    }

    /** {@inheritDoc} */
    public boolean getWrapPSeg() {
        return  paintingState.getWrapPSeg();
    }

    /** {@inheritDoc} */
    public boolean getFS45() {
        return  paintingState.getFS45();
    }

    public void setDefaultResourceGroupUri(URI uri) {
        resourceManager.setDefaultResourceGroupUri(uri);
    }

    /** {@inheritDoc} */
    public void setResourceLevelDefaults(AFPResourceLevelDefaults defaults) {
        resourceManager.setResourceLevelDefaults(defaults);
    }

    /**
     * Returns the page segment descriptor for a given URI if it actually represents a page segment.
     * Otherwise, it just returns null.
     * @param uri the URI that identifies the page segment
     * @return the page segment descriptor or null if there's no page segment for the given URI
     */
    PageSegmentDescriptor getPageSegmentNameFor(String uri) {
        return pageSegmentMap.get(uri);
    }

    /** {@inheritDoc} */
    public void canEmbedJpeg(boolean canEmbed) {
        paintingState.setCanEmbedJpeg(canEmbed);
    }

}
