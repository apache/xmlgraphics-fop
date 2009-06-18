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
import java.util.List;

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.layoutmgr.BlockLevelEventProducer;

/**
 * Class modelling the <a href="http://www.w3.org/TR/xsl/#fo_page-sequence-master">
 * <code>fo:page-sequence-master</code></a> object.
 *
 * This class handles a list of subsequence specifiers
 * which are simple or complex references to page-masters.
 */
public class PageSequenceMaster extends FObj {
    // The value of properties relevant for fo:page-sequence-master.
    private String masterName;
    // End of property values

    private LayoutMasterSet layoutMasterSet;
    private List subSequenceSpecifiers;
    private SubSequenceSpecifier currentSubSequence;
    private int currentSubSequenceNumber = -1;

    // The terminology may be confusing. A 'page-sequence-master' consists
    // of a sequence of what the XSL spec refers to as
    // 'sub-sequence-specifiers'. These are, in fact, simple or complex
    // references to page-masters. So the methods use the former
    // terminology ('sub-sequence-specifiers', or SSS),
    // but the actual FO's are MasterReferences.

    /**
     * Create a PageSequenceMaster instance that is a child of the
     * given {@link FONode}.
     *
     * @param parent {@link FONode} that is the parent of this object
     */
    public PageSequenceMaster(FONode parent) {
        super(parent);
    }

    /** {@inheritDoc} */
    public void bind(PropertyList pList) throws FOPException {
        masterName = pList.get(PR_MASTER_NAME).getString();

        if (masterName == null || masterName.equals("")) {
            missingPropertyError("master-name");
        }
    }

    /** {@inheritDoc} */
    protected void startOfNode() throws FOPException {
        subSequenceSpecifiers = new java.util.ArrayList();
        layoutMasterSet = parent.getRoot().getLayoutMasterSet();
        layoutMasterSet.addPageSequenceMaster(masterName, this);
    }

    /** {@inheritDoc} */
    protected void endOfNode() throws FOPException {
        if (firstChild == null) {
            missingChildElementError("(single-page-master-reference|"
                    + "repeatable-page-master-reference|repeatable-page-master-alternatives)+");
        }
    }

    /**
     * {@inheritDoc}
     * <br>XSL/FOP: (single-page-master-reference|repeatable-page-master-reference|
     *     repeatable-page-master-alternatives)+
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName)
        throws ValidationException {
        if (FO_URI.equals(nsURI)) {
            if (!"single-page-master-reference".equals(localName)
                && !"repeatable-page-master-reference".equals(localName)
                && !"repeatable-page-master-alternatives".equals(localName)) {
                    invalidChildError(loc, nsURI, localName);
            }
        }
    }

    /**
     * Adds a new suqsequence specifier to the page sequence master.
     * @param pageMasterReference the subsequence to add
     */
    protected void addSubsequenceSpecifier(SubSequenceSpecifier pageMasterReference) {
        subSequenceSpecifiers.add(pageMasterReference);
    }

    /**
     * Returns the next subsequence specifier
     * @return a subsequence specifier
     */
    private SubSequenceSpecifier getNextSubSequence() {
        currentSubSequenceNumber++;
        if (currentSubSequenceNumber >= 0
                && currentSubSequenceNumber < subSequenceSpecifiers.size()) {
            return (SubSequenceSpecifier)subSequenceSpecifiers
              .get(currentSubSequenceNumber);
        }
        return null;
    }

    /**
     * Resets the subsequence specifiers subsystem.
     */
    public void reset() {
        currentSubSequenceNumber = -1;
        currentSubSequence = null;
        if (subSequenceSpecifiers != null) {
            for (int i = 0; i < subSequenceSpecifiers.size(); i++) {
                ((SubSequenceSpecifier)subSequenceSpecifiers.get(i)).reset();
            }
        }
    }

    /**
     * Used to set the "cursor position" for the page masters to the previous item.
     * @return true if there is a previous item, false if the current one was the first one.
     */
    public boolean goToPreviousSimplePageMaster() {
        if (currentSubSequence != null) {
            boolean success = currentSubSequence.goToPrevious();
            if (!success) {
                if (currentSubSequenceNumber > 0) {
                    currentSubSequenceNumber--;
                    currentSubSequence = (SubSequenceSpecifier)subSequenceSpecifiers
                        .get(currentSubSequenceNumber);
                } else {
                    currentSubSequence = null;
                }
            }
        }
        return (currentSubSequence != null);
    }

    /** @return true if the page-sequence-master has a page-master with page-position="last" */
    public boolean hasPagePositionLast() {
        return (currentSubSequence != null
                && currentSubSequence.hasPagePositionLast());
    }

    /** @return true if the page-sequence-master has a page-master with page-position="only" */
    public boolean hasPagePositionOnly() {
        return (currentSubSequence != null
                && currentSubSequence.hasPagePositionOnly());
    }

    /**
     * Returns the next simple-page-master.
     * @param isOddPage True if the next page number is odd
     * @param isFirstPage True if the next page is the first
     * @param isLastPage True if the next page is the last
     * @param isBlankPage True if the next page is blank
     * @return the requested page master
     * @throws PageProductionException if there's a problem determining the next page master
     */
    public SimplePageMaster getNextSimplePageMaster(boolean isOddPage,
                                                    boolean isFirstPage,
                                                    boolean isLastPage,
                                                    boolean isBlankPage)
                                                      throws PageProductionException {
        if (currentSubSequence == null) {
            currentSubSequence = getNextSubSequence();
            if (currentSubSequence == null) {
                BlockLevelEventProducer eventProducer = BlockLevelEventProducer.Provider.get(
                        getUserAgent().getEventBroadcaster());
                eventProducer.missingSubsequencesInPageSequenceMaster(this,
                        masterName, getLocator());
            }
        }
        String pageMasterName = currentSubSequence
            .getNextPageMasterName(isOddPage, isFirstPage, isLastPage, isBlankPage);
        boolean canRecover = true;
        while (pageMasterName == null) {
            SubSequenceSpecifier nextSubSequence = getNextSubSequence();
            if (nextSubSequence == null) {
                BlockLevelEventProducer eventProducer = BlockLevelEventProducer.Provider.get(
                        getUserAgent().getEventBroadcaster());
                eventProducer.pageSequenceMasterExhausted(this,
                        masterName, canRecover, getLocator());
                currentSubSequence.reset();
                canRecover = false;
            } else {
                currentSubSequence = nextSubSequence;
            }
            pageMasterName = currentSubSequence
                .getNextPageMasterName(isOddPage, isFirstPage, isLastPage, isBlankPage);
        }
        SimplePageMaster pageMaster = this.layoutMasterSet
            .getSimplePageMaster(pageMasterName);
        if (pageMaster == null) {
            BlockLevelEventProducer eventProducer = BlockLevelEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.noMatchingPageMaster(this,
                    masterName, pageMasterName, getLocator());
        }
        return pageMaster;
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return "page-sequence-master";
    }

    /**
     * {@inheritDoc}
     * @return {@link org.apache.fop.fo.Constants#FO_PAGE_SEQUENCE_MASTER}
     */
    public int getNameId() {
        return FO_PAGE_SEQUENCE_MASTER;
    }
}

