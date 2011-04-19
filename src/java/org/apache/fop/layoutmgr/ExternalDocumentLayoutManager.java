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

package org.apache.fop.layoutmgr;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.util.ImageUtil;

import org.apache.fop.ResourceEventProducer;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.area.AreaTreeHandler;
import org.apache.fop.area.Block;
import org.apache.fop.area.BodyRegion;
import org.apache.fop.area.CTM;
import org.apache.fop.area.LineArea;
import org.apache.fop.area.PageSequence;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.RegionViewport;
import org.apache.fop.area.inline.Image;
import org.apache.fop.area.inline.InlineViewport;
import org.apache.fop.datatypes.FODimension;
import org.apache.fop.datatypes.URISpecification;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.extensions.ExternalDocument;
import org.apache.fop.layoutmgr.inline.ImageLayout;
import org.apache.fop.traits.WritingMode;

/**
 * LayoutManager for an external-document extension element.  This class is instantiated by
 * area.AreaTreeHandler for each fo:external-document found in the
 * input document.
 */
public class ExternalDocumentLayoutManager extends AbstractPageSequenceLayoutManager {

    private static Log log = LogFactory.getLog(ExternalDocumentLayoutManager.class);

    private ImageLayout imageLayout;

    /**
     * Constructor
     *
     * @param ath the area tree handler object
     * @param document fox:external-document to process
     */
    public ExternalDocumentLayoutManager(AreaTreeHandler ath, ExternalDocument document) {
        super(ath, document);
    }

    /**
     * @return the ExternalDocument being managed by this layout manager
     */
    protected ExternalDocument getExternalDocument() {
        return (ExternalDocument)pageSeq;
    }

    /** {@inheritDoc} */
    public PageSequenceLayoutManager getPSLM() {
        throw new IllegalStateException("getPSLM() is illegal for " + getClass().getName());
    }

    /** {@inheritDoc} */
    public void activateLayout() {
        initialize();

        FOUserAgent userAgent = pageSeq.getUserAgent();
        ImageManager imageManager = userAgent.getFactory().getImageManager();

        String uri = URISpecification.getURL(getExternalDocument().getSrc());
        Integer firstPageIndex = ImageUtil.getPageIndexFromURI(uri);
        boolean hasPageIndex = (firstPageIndex != null);

        try {
            ImageInfo info = imageManager.getImageInfo(uri, userAgent.getImageSessionContext());

            Object moreImages = info.getCustomObjects().get(ImageInfo.HAS_MORE_IMAGES);
            boolean hasMoreImages = moreImages != null && !Boolean.FALSE.equals(moreImages);

            Dimension intrinsicSize = info.getSize().getDimensionMpt();
            ImageLayout layout = new ImageLayout(getExternalDocument(), this, intrinsicSize);

            PageSequence pageSequence = new PageSequence(null);
            transferExtensions(pageSequence);
            areaTreeHandler.getAreaTreeModel().startPageSequence(pageSequence);
            if (log.isDebugEnabled()) {
                log.debug("Starting layout");
            }

            makePageForImage(info, layout);

            if (!hasPageIndex && hasMoreImages) {
                if (log.isTraceEnabled()) {
                    log.trace("Starting multi-page processing...");
                }
                URI originalURI;
                try {
                    originalURI = new URI(URISpecification.escapeURI(uri));
                    int pageIndex = 1;
                    while (hasMoreImages) {
                        URI tempURI = new URI(originalURI.getScheme(),
                                originalURI.getSchemeSpecificPart(),
                                "page=" + Integer.toString(pageIndex + 1));
                        if (log.isTraceEnabled()) {
                            log.trace("Subimage: " + tempURI.toASCIIString());
                        }
                        ImageInfo subinfo = imageManager.getImageInfo(
                                tempURI.toASCIIString(), userAgent.getImageSessionContext());

                        moreImages = subinfo.getCustomObjects().get(ImageInfo.HAS_MORE_IMAGES);
                        hasMoreImages = moreImages != null && !Boolean.FALSE.equals(moreImages);

                        intrinsicSize = subinfo.getSize().getDimensionMpt();
                        layout = new ImageLayout(
                                getExternalDocument(), this, intrinsicSize);

                        makePageForImage(subinfo, layout);

                        pageIndex++;
                    }
                } catch (URISyntaxException e) {
                    getResourceEventProducer().uriError(this, uri, e,
                            getExternalDocument().getLocator());
                }
            }
        } catch (FileNotFoundException fnfe) {
            getResourceEventProducer().imageNotFound(this, uri, fnfe,
                    getExternalDocument().getLocator());
        } catch (IOException ioe) {
            getResourceEventProducer().imageIOError(this, uri, ioe,
                    getExternalDocument().getLocator());
        } catch (ImageException ie) {
            getResourceEventProducer().imageError(this, uri, ie,
                    getExternalDocument().getLocator());
        }
    }

    private ResourceEventProducer getResourceEventProducer() {
        return ResourceEventProducer.Provider.get(
                getExternalDocument().getUserAgent().getEventBroadcaster());
    }

    private void makePageForImage(ImageInfo info, ImageLayout layout) {
        this.imageLayout = layout;
        curPage = makeNewPage(false, false);
        fillPage(info.getOriginalURI());
        finishPage();
    }

    private void fillPage(String uri) {

        Dimension imageSize = this.imageLayout.getViewportSize();

        Block blockArea = new Block();
        blockArea.setIPD(imageSize.width);
        LineArea lineArea = new LineArea();

        Image imageArea = new Image(uri);
        TraitSetter.setProducerID(imageArea, fobj.getId());
        transferForeignAttributes(imageArea);

        InlineViewport vp = new InlineViewport(imageArea);
        TraitSetter.setProducerID(vp, fobj.getId());
        vp.setIPD(imageSize.width);
        vp.setBPD(imageSize.height);
        vp.setContentPosition(imageLayout.getPlacement());
        vp.setBlockProgressionOffset(0);

        //Link them all together...
        lineArea.addInlineArea(vp);
        lineArea.updateExtentsFromChildren();
        blockArea.addLineArea(lineArea);
        curPage.getPageViewport().getCurrentFlow().addBlock(blockArea);
        curPage.getPageViewport().getCurrentSpan().notifyFlowsFinished();
    }

    /** {@inheritDoc} */
    public void finishPageSequence() {
        if (pageSeq.hasId()) {
            idTracker.signalIDProcessed(pageSeq.getId());
        }

        pageSeq.getRoot().notifyPageSequenceFinished(currentPageNum,
                (currentPageNum - startPageNum) + 1);
        areaTreeHandler.notifyPageSequenceFinished(pageSeq,
                (currentPageNum - startPageNum) + 1);

        if (log.isDebugEnabled()) {
            log.debug("Ending layout");
        }
    }

    /** {@inheritDoc} */
    protected Page createPage(int pageNumber, boolean isBlank) {
        String pageNumberString = pageSeq.makeFormattedPageNumber(pageNumber);

        Dimension imageSize = this.imageLayout.getViewportSize();

        // Set up the CTM on the page reference area based on writing-mode
        // and reference-orientation
        Rectangle referenceRect;
        if (pageSeq.getReferenceOrientation() % 180 == 0) {
            referenceRect = new Rectangle(0, 0, imageSize.width, imageSize.height);
        } else {
            referenceRect = new Rectangle(0, 0, imageSize.height, imageSize.width);
        }
        FODimension reldims = new FODimension(0, 0);
        // [TBD] BIDI ALERT
        CTM pageCTM = CTM.getCTMandRelDims(pageSeq.getReferenceOrientation(),
                                           WritingMode.LR_TB, referenceRect, reldims);

        Page page = new Page(referenceRect, pageNumber, pageNumberString, isBlank);

        PageViewport pv = page.getPageViewport();
        org.apache.fop.area.Page pageArea = new org.apache.fop.area.Page();
        pv.setPage(pageArea);

        RegionViewport rv = new RegionViewport(referenceRect);
        rv.setIPD(referenceRect.width);
        rv.setBPD(referenceRect.height);
        rv.setClip(true);

        BodyRegion body = new BodyRegion(Constants.FO_REGION_BODY,
                "fop-image-region", rv, 1, 0);
        body.setIPD(imageSize.width);
        body.setBPD(imageSize.height);
        body.setCTM(pageCTM);
        rv.setRegionReference(body);
        pageArea.setRegionViewport(
                Constants.FO_REGION_BODY, rv);
        //Set unique key obtained from the AreaTreeHandler
        pv.setKey(areaTreeHandler.generatePageViewportKey());

        //Also creates first normal flow region
        pv.createSpan(false);

        return page;
    }

}
