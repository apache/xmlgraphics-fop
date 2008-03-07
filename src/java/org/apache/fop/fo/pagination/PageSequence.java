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

// Java
import java.util.Map;

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;

/**
 * Abstract base implementation for page sequences.
 */
public class PageSequence extends AbstractPageSequence {
    
    // The value of properties relevant for fo:page-sequence.
    private String country;
    private String language;
    private String masterReference;
    //private int writingMode; //XSL 1.1
    // End of property values

    // There doesn't seem to be anything in the spec requiring flows
    // to be in the order given, only that they map to the regions
    // defined in the page sequence, so all we need is this one hashmap
    // the set of flows includes StaticContent flows also

    /** Map of flows to their flow name (flow-name, Flow) */
    private Map flowMap;

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
     * Create a page sequence FO node.
     *
     * @param parent the parent FO node
     */
    public PageSequence(FONode parent) {
        super(parent);
    }

    /**
     * {@inheritDoc}
     */
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);
        country = pList.get(PR_COUNTRY).getString();
        language = pList.get(PR_LANGUAGE).getString();
        masterReference = pList.get(PR_MASTER_REFERENCE).getString();
        //writingMode = pList.getWritingMode();
        
        if (masterReference == null || masterReference.equals("")) {
            missingPropertyError("master-reference");
        }        
    }

    /**
     * {@inheritDoc}
     */
    protected void startOfNode() throws FOPException {
        super.startOfNode();
        flowMap = new java.util.HashMap();

        this.simplePageMaster = getRoot().getLayoutMasterSet().getSimplePageMaster(masterReference);
        if (this.simplePageMaster == null) {
            this.pageSequenceMaster
                    = getRoot().getLayoutMasterSet().getPageSequenceMaster(masterReference);
            if (this.pageSequenceMaster == null) {
                throw new ValidationException("master-reference '" + masterReference
                   + "' for fo:page-sequence matches no"
                   + " simple-page-master or page-sequence-master", locator);
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
            if (localName.equals("title")) {
                if (titleFO != null) {
                    tooManyNodesError(loc, "fo:title");
                } else if (flowMap.size() > 0) {
                    nodesOutOfOrderError(loc, "fo:title", "fo:static-content");
                } else if (mainFlow != null) {
                    nodesOutOfOrderError(loc, "fo:title", "fo:flow");
                }
            } else if (localName.equals("static-content")) {
                if (mainFlow != null) {
                    nodesOutOfOrderError(loc, "fo:static-content", "fo:flow");
                }                
            } else if (localName.equals("flow")) {
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
     * @todo see if addChildNode() should also be called for fo's other than
     *  fo:flow.
     */
    public void addChildNode(FONode child) throws FOPException {
        int childId = child.getNameId();

        if (childId == FO_TITLE) {
            this.titleFO = (Title) child;
        } else if (childId == FO_FLOW) {
            this.mainFlow = (Flow) child;
            addFlow(mainFlow);
        } else if (childId == FO_STATIC_CONTENT) {
            addFlow((StaticContent) child);
            String flowName = ((StaticContent) child).getFlowName();
            flowMap.put(flowName, child);
        }
    }

    /**
     * Add a flow or static content, mapped by its flow-name.
     * The flow-name is used to associate the flow with a region on a page,
     * based on the region-names given to the regions in the page-master
     * used to generate that page.
     */
     private void addFlow(Flow flow) throws ValidationException {
        String flowName = flow.getFlowName();

        if (hasFlowName(flowName)) {
            throw new ValidationException("duplicate flow-name \""
                + flowName
                + "\" found within fo:page-sequence", flow.getLocator());
        }

        if (!getRoot().getLayoutMasterSet().regionNameExists(flowName) 
            && !flowName.equals("xsl-before-float-separator") 
            && !flowName.equals("xsl-footnote-separator")) {
                throw new ValidationException("flow-name \""
                    + flowName
                    + "\" could not be mapped to a region-name in the"
                    + " layout-master-set", flow.getLocator());
        }
    }

//     /**
//      * Returns true when there is more flow elements left to lay out.
//      */
//     private boolean flowsAreIncomplete() {
//         boolean isIncomplete = false;

//         for (Iterator e = flowMap.values().iterator(); e.hasNext(); ) {
//             Flow flow = (Flow)e.next();
//             if (flow instanceof StaticContent) {
//                 continue;
//             }

//             Status status = flow.getStatus();
//             isIncomplete |= status.isIncomplete();
//         }
//         return isIncomplete;
//     }

//     /**
//      * Returns the flow that maps to the given region class for the current
//      * page master.
//      */
//     private Flow getCurrentFlow(String regionClass) {
//         Region region = getCurrentSimplePageMaster().getRegion(regionClass);
//         if (region != null) {
//             Flow flow = (Flow)flowMap.get(region.getRegionName());
//             return flow;

//         } else {

//             getLogger().error("flow is null. regionClass = '" + regionClass
//                                + "' currentSPM = "
//                                + getCurrentSimplePageMaster());

//             return null;
//         }

//     }

//      private boolean isFlowForMasterNameDone(String masterName) {
//          // parameter is master-name of PMR; we need to locate PM
//          // referenced by this, and determine whether flow(s) are OK
//          if (isForcing)
//              return false;
//          if (masterName != null) {

//              SimplePageMaster spm =
//                  root.getLayoutMasterSet().getSimplePageMaster(masterName);
//              Region region = spm.getRegion(FO_REGION_BODY);


//              Flow flow = (Flow)flowMap.get(region.getRegionName());
//              /*if ((null == flow) || flow.getStatus().isIncomplete())
//                  return false;
//              else
//                  return true;*/
//          }
//          return false;
//      }

//     private void forcePage(AreaTree areaTree, int firstAvailPageNumber) {
//         boolean makePage = false;
//         if (this.forcePageCount == ForcePageCount.AUTO) {
//             PageSequence nextSequence =
//                 this.root.getSucceedingPageSequence(this);
//             if (nextSequence != null) {
//                 if (nextSequence.getIpnValue().equals("auto")) {
//                     // do nothing special
//                 }
//                 else if (nextSequence.getIpnValue().equals("auto-odd")) {
//                     if (firstAvailPageNumber % 2 == 0) {
//                         makePage = true;
//                     }
//                 } else if (nextSequence.getIpnValue().equals("auto-even")) {
//                     if (firstAvailPageNumber % 2 != 0) {
//                         makePage = true;
//                     }
//                 } else {
//                     int nextSequenceStartPageNumber =
//                         nextSequence.getCurrentPageNumber();
//                     if ((nextSequenceStartPageNumber % 2 == 0)
//                             && (firstAvailPageNumber % 2 == 0)) {
//                         makePage = true;
//                     } else if ((nextSequenceStartPageNumber % 2 != 0)
//                                && (firstAvailPageNumber % 2 != 0)) {
//                         makePage = true;
//                     }
//                 }
//             }
//         } else if ((this.forcePageCount == ForcePageCount.EVEN)
//                    && (this.pageCount % 2 != 0)) {
//             makePage = true;
//         } else if ((this.forcePageCount == ForcePageCount.ODD)
//                    && (this.pageCount % 2 == 0)) {
//             makePage = true;
//         } else if ((this.forcePageCount == ForcePageCount.END_ON_EVEN)
//                    && (firstAvailPageNumber % 2 == 0)) {
//             makePage = true;
//         } else if ((this.forcePageCount == ForcePageCount.END_ON_ODD)
//                    && (firstAvailPageNumber % 2 != 0)) {
//             makePage = true;
//         } else if (this.forcePageCount == ForcePageCount.NO_FORCE) {
//             // do nothing
//         }

//         if (makePage) {
//             try {
//                 this.isForcing = true;
//                 this.currentPageNumber++;
//                 firstAvailPageNumber = this.currentPageNumber;
//                 currentPage = makePage(areaTree, firstAvailPageNumber, false,
//                                        true);
//                 String formattedPageNumber =
//                     pageNumberGenerator.makeFormattedPageNumber(this.currentPageNumber);
//                 currentPage.setFormattedNumber(formattedPageNumber);
//                 currentPage.setPageSequence(this);
//                 formatStaticContent(areaTree);
//                 log.debug("[forced-" + firstAvailPageNumber + "]");
//                 areaTree.addPage(currentPage);
//                 this.root.setRunningPageNumberCounter(this.currentPageNumber);
//                 this.isForcing = false;
//             } catch (FOPException fopex) {
//                 log.debug("'force-page-count' failure");
//             }
//         }
//     }

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
     * Accessor method for titleFO
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
    public Map getFlowMap() {
        return this.flowMap;
    }
    
    /**
     * Public accessor for determining the next page master to use within this page sequence.
     * @param page the page number of the page to be created
     * @param isFirstPage indicator whether this page is the first page of the
     *      page sequence
     * @param isLastPage indicator whether this page is the last page of the
     *      page sequence
     * @param isOnlyPage indicator whether this page is the only page of the
     *      page sequence
     * @param isBlank indicator whether the page will be blank
     * @return the SimplePageMaster to use for this page
     * @throws FOPException if there's a problem determining the page master
     */
    public SimplePageMaster getNextSimplePageMaster(int page, 
            boolean isFirstPage,  
            boolean isLastPage,  
            boolean isOnlyPage,
            boolean isBlank) throws FOPException {

        if (pageSequenceMaster == null) {
            return simplePageMaster;
        }
        boolean isOddPage = ((page % 2) == 1);
        if (log.isDebugEnabled()) {
            log.debug("getNextSimplePageMaster(page=" + page
                    + " isOdd=" + isOddPage 
                    + " isFirst=" + isFirstPage 
                    + " isLast=" + isLastPage
                    + " isOnly=" + isOnlyPage
                    + " isBlank=" + isBlank + ")");
        }
        return pageSequenceMaster.getNextSimplePageMaster(isOddPage, 
            isFirstPage, isLastPage, isOnlyPage, isBlank);
    }

    /**
     * Used to set the "cursor position" for the page masters to the previous item.
     * @return true if there is a previous item, false if the current one was the first one.
     */
    public boolean goToPreviousSimplePageMaster() {
        if (pageSequenceMaster == null) {
            return true;
        } else {
            return pageSequenceMaster.goToPreviousSimplePageMaster();
        }
    }

    /** @return true if the page-sequence has a page-master with page-position="last" */
    public boolean hasPagePositionLast() {
        if (pageSequenceMaster == null) {
            return false;
        } else {
            return pageSequenceMaster.hasPagePositionLast();
        }
    }
    
    /** @return true if the page-sequence has a page-master with page-position="only" */
    public boolean hasPagePositionOnly() {
        if (pageSequenceMaster == null) {
            return false;
        } else {
            return pageSequenceMaster.hasPagePositionOnly();
        }
    }
    
    /** @return the "master-reference" property. */
    public String getMasterReference() {
        return masterReference;
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return "page-sequence";
    }

    /** {@inheritDoc} */
    public int getNameId() {
        return FO_PAGE_SEQUENCE;
    }
    
    /** @return the country property value */
    public String getCountry() {
        return this.country;
    }
    
    /** @return the language property value */
    public String getLanguage() {
        return this.language;
    }

    /**
     * Releases a page-sequence's children after the page-sequence has been fully processed.
     */
    public void releasePageSequence() {
        this.mainFlow = null;
        this.flowMap.clear();
    }
    
}
