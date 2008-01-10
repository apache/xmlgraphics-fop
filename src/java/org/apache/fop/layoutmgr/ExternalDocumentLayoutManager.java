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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.area.AreaTreeHandler;
import org.apache.fop.area.Block;
import org.apache.fop.area.BodyRegion;
import org.apache.fop.area.CTM;
import org.apache.fop.area.LineArea;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.RegionViewport;
import org.apache.fop.area.inline.Image;
import org.apache.fop.area.inline.Viewport;
import org.apache.fop.datatypes.FODimension;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.extensions.ExternalDocument;
import org.apache.fop.image.FopImage;
import org.apache.fop.image.ImageFactory;
import org.apache.fop.layoutmgr.inline.ImageLayout;

/**
 * LayoutManager for an external-document extension element.  This class is instantiated by
 * area.AreaTreeHandler for each fo:external-document found in the
 * input document.
 */
public class ExternalDocumentLayoutManager extends AbstractPageSequenceLayoutManager {

    private static Log log = LogFactory.getLog(ExternalDocumentLayoutManager.class);

    private FopImage image;
    private ImageLayout imageLayout; 
    
    /**
     * Constructor
     *
     * @param ath the area tree handler object
     * @param pseq fo:page-sequence to process
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

        String uri = getExternalDocument().getSrc();
        FOUserAgent userAgent = pageSeq.getUserAgent();
        ImageFactory fact = userAgent.getFactory().getImageFactory();
        this.image = fact.getImage(uri, userAgent);
        if (this.image == null) {
            log.error("Image not available: " + uri);
            return;
        } else {
            // load dimensions
            if (!this.image.load(FopImage.DIMENSIONS)) {
                log.error("Cannot read image dimensions: " + uri);
                return;
            }
        }
        Dimension intrinsicSize = new Dimension(
                image.getIntrinsicWidth(),
                image.getIntrinsicHeight());
        this.imageLayout = new ImageLayout(getExternalDocument(), this, intrinsicSize);
        
        areaTreeHandler.getAreaTreeModel().startPageSequence(null);
        if (log.isDebugEnabled()) {
            log.debug("Starting layout");
        }

        curPage = makeNewPage(false, false);

        fillPage(); //TODO Implement multi-page documents (using new image package)
        
        finishPage();
    }

    private void fillPage() {

        Dimension imageSize = this.imageLayout.getViewportSize();
        
        Block blockArea = new Block();
        blockArea.setIPD(imageSize.width);
        LineArea lineArea = new LineArea();
        
        Image imageArea = new Image(getExternalDocument().getSrc());
        TraitSetter.setProducerID(imageArea, fobj.getId());
        transferForeignAttributes(imageArea);

        Viewport vp = new Viewport(imageArea);
        TraitSetter.setProducerID(vp, fobj.getId());
        vp.setIPD(imageSize.width);
        vp.setBPD(imageSize.height);
        vp.setContentPosition(imageLayout.getPlacement());
        vp.setOffset(0);
        
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
        CTM pageCTM = CTM.getCTMandRelDims(pageSeq.getReferenceOrientation(),
            Constants.EN_LR_TB, referenceRect, reldims);
        
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
