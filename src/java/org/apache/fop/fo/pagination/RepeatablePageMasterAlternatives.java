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
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.Property;

/**
 * Class modelling the <a href="http://www.w3.org/TR/xsl/#fo_repeatable-page-master-alternatives">
 * <code>fo:repeatable-page-master-alternatives</code></a> object.
 * This contains a list of conditional-page-master-reference
 * and the page master is found from the reference that
 * matches the page number and emptyness.
 */
public class RepeatablePageMasterAlternatives extends FObj
    implements SubSequenceSpecifier {
    // The value of properties relevant for fo:repeatable-page-master-alternatives.
    private Property maximumRepeats;
    // End of property values

    private static final int INFINITE = -1;

    private int numberConsumed;

    private List<ConditionalPageMasterReference> conditionalPageMasterRefs;
    private boolean hasPagePositionLast;
    private boolean hasPagePositionOnly;

    /**
     * Base constructor
     *
     * @param parent {@link FONode} that is the parent of this object
     */
    public RepeatablePageMasterAlternatives(FONode parent) {
        super(parent);
    }

    /** {@inheritDoc} */
    public void bind(PropertyList pList) throws FOPException {
        maximumRepeats = pList.get(PR_MAXIMUM_REPEATS);
    }

    /** {@inheritDoc} */
    public void startOfNode() throws FOPException {
        conditionalPageMasterRefs = new java.util.ArrayList<ConditionalPageMasterReference>();

        assert parent.getName().equals("fo:page-sequence-master"); //Validation by the parent
        PageSequenceMaster pageSequenceMaster = (PageSequenceMaster)parent;
        pageSequenceMaster.addSubsequenceSpecifier(this);
    }

    /** {@inheritDoc} */
    public void endOfNode() throws FOPException {
        if (firstChild == null) {
           missingChildElementError("(conditional-page-master-reference+)");
        }
    }

    /**
     * {@inheritDoc}
     * <br>XSL/FOP: (conditional-page-master-reference+)
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName)
                throws ValidationException {
        if (FO_URI.equals(nsURI)) {
            if (!localName.equals("conditional-page-master-reference")) {
                invalidChildError(loc, nsURI, localName);
            }
        }
    }

    /**
     * Get the value of the <code>maximum-repeats</code> property?
     * @return the "maximum-repeats" property
     */
    public int getMaximumRepeats() {
        if (maximumRepeats.getEnum() == EN_NO_LIMIT) {
            return INFINITE;
        } else {
            int mr = maximumRepeats.getNumeric().getValue();
            if (mr < 0) {
                log.debug("negative maximum-repeats: "
                        + this.maximumRepeats);
                mr = 0;
            }
            return mr;
        }
    }

    /** {@inheritDoc} */
    public SimplePageMaster getNextPageMaster(boolean isOddPage,
                                        boolean isFirstPage,
                                        boolean isLastPage,
                                        boolean isBlankPage) {

        if (!isInfinite() && numberConsumed >= getMaximumRepeats()) {
            return null;
        }

        numberConsumed++;

        for (ConditionalPageMasterReference cpmr : conditionalPageMasterRefs) {
            if (cpmr.isValid(isOddPage, isFirstPage, isLastPage, isBlankPage)) {
                return cpmr.getMaster();
            }
        }


        return null;
    }


    /**
     * Adds a new conditional page master reference.
     * @param cpmr the new conditional reference
     */
    public void addConditionalPageMasterReference(ConditionalPageMasterReference cpmr) {
        this.conditionalPageMasterRefs.add(cpmr);
        if (cpmr.getPagePosition() == EN_LAST) {
            this.hasPagePositionLast = true;
        }
        if (cpmr.getPagePosition() == EN_ONLY) {
            this.hasPagePositionOnly = true;
        }
    }

    /** {@inheritDoc} */
    public void reset() {
        this.numberConsumed = 0;
    }

    /** {@inheritDoc} */
    public boolean goToPrevious() {
        if (numberConsumed == 0) {
            return false;
        } else {
            numberConsumed--;
            return true;
        }
    }

    /** {@inheritDoc} */
    public boolean hasPagePositionLast() {
        return this.hasPagePositionLast;
    }

    /** {@inheritDoc} */
    public boolean hasPagePositionOnly() {
        return this.hasPagePositionOnly;
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return "repeatable-page-master-alternatives";
    }

    /**
     * {@inheritDoc}
     * @return {@link org.apache.fop.fo.Constants#FO_REPEATABLE_PAGE_MASTER_ALTERNATIVES}
     */
    public int getNameId() {
        return FO_REPEATABLE_PAGE_MASTER_ALTERNATIVES;
    }



    /** {@inheritDoc} */
    public void resolveReferences(LayoutMasterSet layoutMasterSet) throws ValidationException {
        for (ConditionalPageMasterReference conditionalPageMasterReference
                : conditionalPageMasterRefs) {
            conditionalPageMasterReference.resolveReferences(layoutMasterSet);
        }

    }

    /** {@inheritDoc} */
    public boolean canProcess(String flowName) {

        boolean willTerminate = true;


        //Look for rest spm that cannot terminate
        ArrayList<ConditionalPageMasterReference> rest
                = new ArrayList<ConditionalPageMasterReference>();
        for (ConditionalPageMasterReference cpmr
                : conditionalPageMasterRefs) {
            if (cpmr.isValid(true, false, false, false)
                    || cpmr.isValid(false, false, false, false)) {
                rest.add(cpmr);
            }
        }
        if (!rest.isEmpty()) {
            willTerminate = false;
            for (ConditionalPageMasterReference cpmr : rest) {
                willTerminate |= cpmr.getMaster().getRegion(FO_REGION_BODY).getRegionName()
                        .equals(flowName);
            }
        }


        return willTerminate;
    }

    /** {@inheritDoc} */
    public boolean isInfinite() {
        return getMaximumRepeats() == INFINITE;
    }

    /** {@inheritDoc} */
    public boolean isReusable() {
        return false;
    }

}
