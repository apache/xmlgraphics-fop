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

package org.apache.fop.fo.pagination;

import java.util.Locale;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.complexscripts.bidi.DelimitedTextRange;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.CommonHyphenation;
import org.apache.fop.traits.Direction;
import org.apache.fop.traits.WritingMode;
import org.apache.fop.traits.WritingModeTraits;
import org.apache.fop.traits.WritingModeTraitsGetter;

/**
 * Class modelling the <a href="http://www.w3.org/TR/xsl/#fo_page-sequence">
 * <code>fo:page-sequence</code></a> object.
 */
public class PageSequence extends AbstractPageSequence implements WritingModeTraitsGetter {

    private String masterReference;
    private Numeric referenceOrientation;
    private WritingModeTraits writingModeTraits;

    private Locale locale;

    // There doesn't seem to be anything in the spec requiring flows
    // to be in the order given, only that they map to the regions
    // defined in the page sequence, so all we need is this one hashmap
    // the set of flows includes StaticContent flows also

    /** Map of flows to their flow name (flow-name, Flow) */
    private Map<String, FONode> flowMap;

    /**
     * The currentSimplePageMaster is either the page master for the
     * whole page sequence if master-reference refers to a simple-page-master,
     * or the simple page master produced by the page sequence master otherwise.
     * The pageSequenceMaster is null if master-reference refers to a
     * simple-page-master.
     */
    private SimplePageMaster simplePageMaster;
    private PageSequenceMaster pageSequenceMaster;

    /**
     * The fo:title object for this page-sequence.
     */
    private Title titleFO;

    /**
     * The fo:flow object for this page-sequence.
     */
    private Flow mainFlow = null;

    /**
     * Create a PageSequence instance that is a child of the
     * given {@link FONode}.
     *
     * @param parent the parent {@link FONode}
     */
    public PageSequence(FONode parent) {
        super(parent);
    }

    /** {@inheritDoc} */
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);
        String country = pList.get(PR_COUNTRY).getString();
        String language = pList.get(PR_LANGUAGE).getString();
        locale = CommonHyphenation.toLocale(language, country);
        masterReference = pList.get(PR_MASTER_REFERENCE).getString();
        referenceOrientation = pList.get(PR_REFERENCE_ORIENTATION).getNumeric();
        writingModeTraits = new WritingModeTraits
            ( WritingMode.valueOf(pList.get(PR_WRITING_MODE).getEnum()) );
        if (masterReference == null || masterReference.equals("")) {
            missingPropertyError("master-reference");
        }
    }

    /** {@inheritDoc} */
    protected void startOfNode() throws FOPException {
        super.startOfNode();
        flowMap = new java.util.HashMap<String, FONode>();

        this.simplePageMaster
            = getRoot().getLayoutMasterSet().getSimplePageMaster(masterReference);
        if (simplePageMaster == null) {
            this.pageSequenceMaster
                = getRoot().getLayoutMasterSet().getPageSequenceMaster(masterReference);
            if (pageSequenceMaster == null) {
                getFOValidationEventProducer().masterNotFound(this, getName(),
                        masterReference, getLocator());
            }
        }
        getFOEventHandler().startPageSequence(this);
    }

    /** {@inheritDoc} */
    protected void endOfNode() throws FOPException {
        if (mainFlow == null) {
           missingChildElementError("(title?,static-content*,flow)");
        }

        getFOEventHandler().endPageSequence(this);
    }

    /**
     * {@inheritDoc}
        XSL Content Model: (title?,static-content*,flow)
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName)
                throws ValidationException {
        if (FO_URI.equals(nsURI)) {
            if ("title".equals(localName)) {
                if (titleFO != null) {
                    tooManyNodesError(loc, "fo:title");
                } else if (!flowMap.isEmpty()) {
                    nodesOutOfOrderError(loc, "fo:title", "fo:static-content");
                } else if (mainFlow != null) {
                    nodesOutOfOrderError(loc, "fo:title", "fo:flow");
                }
            } else if ("static-content".equals(localName)) {
                if (mainFlow != null) {
                    nodesOutOfOrderError(loc, "fo:static-content", "fo:flow");
                }
            } else if ("flow".equals(localName)) {
                if (mainFlow != null) {
                    tooManyNodesError(loc, "fo:flow");
                }
            } else {
                invalidChildError(loc, nsURI, localName);
            }
        }
    }

    /**
     * {@inheritDoc}
     * TODO see if addChildNode() should also be called for fo's other than
     *  fo:flow.
     */
    public void addChildNode(FONode child) throws FOPException {
        int childId = child.getNameId();

        switch (childId) {
        case FO_TITLE:
            this.titleFO = (Title)child;
            break;
        case FO_FLOW:
            this.mainFlow = (Flow)child;
            addFlow(mainFlow);
            break;
        case FO_STATIC_CONTENT:
            addFlow((StaticContent)child);
            flowMap.put(((Flow)child).getFlowName(), (Flow)child);
            break;
        default:
            super.addChildNode(child);
        }
    }

    /**
     * Add a flow or static content, mapped by its flow-name.
     * The flow-name is used to associate the flow with a region on a page,
     * based on the region-names given to the regions in the page-master
     * used to generate that page.
     * @param flow  the {@link Flow} instance to be added
     * @throws org.apache.fop.fo.ValidationException if the fo:flow maps
     * to an invalid page-region
     */
     private void addFlow(Flow flow) throws ValidationException {
        String flowName = flow.getFlowName();

        if (hasFlowName(flowName)) {
            getFOValidationEventProducer().duplicateFlowNameInPageSequence(this, flow.getName(),
                    flowName, flow.getLocator());
        }

        if (!getRoot().getLayoutMasterSet().regionNameExists(flowName)
            && !flowName.equals("xsl-before-float-separator")
            && !flowName.equals("xsl-footnote-separator")) {
            getFOValidationEventProducer().flowNameNotMapped(this, flow.getName(),
                    flowName, flow.getLocator());
        }
    }

    /**
     * Get the static content FO node from the flow map.
     * This gets the static content flow for the given flow name.
     *
     * @param name the flow name to find
     * @return the static content FO node
     */
    public StaticContent getStaticContent(String name) {
        return (StaticContent) flowMap.get(name);
    }

    /**
     * Accessor method for the fo:title associated with this fo:page-sequence
     * @return titleFO for this object
     */
    public Title getTitleFO() {
        return titleFO;
    }

    /**
     * Public accessor for getting the MainFlow to which this PageSequence is
     * attached.
     * @return the MainFlow object to which this PageSequence is attached.
     */
    public Flow getMainFlow() {
        return mainFlow;
    }

    /**
     * Determine if this PageSequence already has a flow with the given flow-name
     * Used for validation of incoming fo:flow or fo:static-content objects
     * @param flowName The flow-name to search for
     * @return true if flow-name already defined within this page sequence,
     *    false otherwise
     */
    public boolean hasFlowName(String flowName) {
        return flowMap.containsKey(flowName);
    }

    /** @return the flow map for this page-sequence */
    public Map<String, FONode> getFlowMap() {
        return this.flowMap;
    }

    /**
     * Public accessor for determining the next page master to use within this page sequence.
     * @param page the page number of the page to be created
     * @param isFirstPage indicator whether this page is the first page of the
     *      page sequence
     * @param isLastPage indicator whether this page is the last page of the
     *      page sequence
     * @param isBlank indicator whether the page will be blank
     * @return the SimplePageMaster to use for this page
     * @throws PageProductionException if there's a problem determining the page master
     */
    public SimplePageMaster getNextSimplePageMaster
        (int page, boolean isFirstPage, boolean isLastPage, boolean isBlank)
        throws PageProductionException {

        if (pageSequenceMaster == null) {
            return simplePageMaster;
        }
        boolean isOddPage = ((page % 2) == 1);
        if (log.isDebugEnabled()) {
            log.debug("getNextSimplePageMaster(page=" + page
                    + " isOdd=" + isOddPage
                    + " isFirst=" + isFirstPage
                    + " isLast=" + isLastPage
                    + " isBlank=" + isBlank + ")");
        }
        return pageSequenceMaster.getNextSimplePageMaster(isOddPage,
            isFirstPage, isLastPage, isBlank, getMainFlow().getFlowName());
    }

    /**
     * Used to set the "cursor position" for the page masters to the previous item.
     * @return true if there is a previous item, false if the current one was the first one.
     */
    public boolean goToPreviousSimplePageMaster() {
        return pageSequenceMaster == null || pageSequenceMaster.goToPreviousSimplePageMaster();
    }

    /** @return true if the page-sequence has a page-master with page-position="last" */
    public boolean hasPagePositionLast() {
        return pageSequenceMaster != null && pageSequenceMaster.hasPagePositionLast();
    }

    /** @return true if the page-sequence has a page-master with page-position="only" */
    public boolean hasPagePositionOnly() {
        return pageSequenceMaster != null && pageSequenceMaster.hasPagePositionOnly();
    }

    /**
     * Get the value of the <code>master-reference</code> trait.
     * @return the "master-reference" trait
     */
    public String getMasterReference() {
        return masterReference;
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return "page-sequence";
    }

    /**
     * {@inheritDoc}
     * @return {@link org.apache.fop.fo.Constants#FO_PAGE_SEQUENCE}
     */
    public int getNameId() {
        return FO_PAGE_SEQUENCE;
    }

    public Locale getLocale() {
        return locale;
    }

    /**
     * Get the value of the <code>reference-orientation</code> trait.
     * @return the reference orientation trait value
     */
    public int getReferenceOrientation() {
        if ( referenceOrientation != null ) {
            return referenceOrientation.getValue();
        } else {
            return 0;
        }
    }

    /**
     * {@inheritDoc}
     */
    public Direction getInlineProgressionDirection() {
        if ( writingModeTraits != null ) {
            return writingModeTraits.getInlineProgressionDirection();
        } else {
            return Direction.LR;
        }
    }

    /**
     * {@inheritDoc}
     */
    public Direction getBlockProgressionDirection() {
        if ( writingModeTraits != null ) {
            return writingModeTraits.getBlockProgressionDirection();
        } else {
            return Direction.TB;
        }
    }

    /**
     * {@inheritDoc}
     */
    public Direction getColumnProgressionDirection() {
        if ( writingModeTraits != null ) {
            return writingModeTraits.getColumnProgressionDirection();
        } else {
            return Direction.LR;
        }
    }

    /**
     * {@inheritDoc}
     */
    public Direction getRowProgressionDirection() {
        if ( writingModeTraits != null ) {
            return writingModeTraits.getRowProgressionDirection();
        } else {
            return Direction.TB;
        }
    }

    /**
     * {@inheritDoc}
     */
    public Direction getShiftDirection() {
        if ( writingModeTraits != null ) {
            return writingModeTraits.getShiftDirection();
        } else {
            return Direction.TB;
        }
    }

    /**
     * {@inheritDoc}
     */
    public WritingMode getWritingMode() {
        if ( writingModeTraits != null ) {
            return writingModeTraits.getWritingMode();
        } else {
            return WritingMode.LR_TB;
        }
    }


    @Override
    protected Stack collectDelimitedTextRanges ( Stack ranges, DelimitedTextRange currentRange ) {
        // collect ranges from static content flows
        Map<String, FONode> flows = getFlowMap();
        if ( flows != null ) {
            for ( FONode fn : flows.values() ) {
                if ( fn instanceof StaticContent ) {
                    ranges = ( (StaticContent) fn ).collectDelimitedTextRanges ( ranges );
                }
            }
        }
        // collect ranges in main flow
        Flow main = getMainFlow();
        if ( main != null ) {
            ranges = main.collectDelimitedTextRanges ( ranges );
        }
        return ranges;
    }

    /**
     * Releases a page-sequence's children after the page-sequence has been fully processed.
     */
    public void releasePageSequence() {
        this.mainFlow = null;
        this.flowMap.clear();
    }

}
