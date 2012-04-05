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

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;

/**
 * Abstract base class for the <a href="http://www.w3.org/TR/xsl/#fo_page-sequence">
 * <code>fo:page-sequence</code></a> formatting object and the
 * <a href="http://xmlgraphics.apache.org/fop/0.95/extensions.html#external-document">
 * <code>fox:external-document</code></a> extension object.
 */
public abstract class AbstractPageSequence extends FObj {

    // The value of properties relevant for fo:page-sequence.
    /** initial page number */
    protected Numeric initialPageNumber;
    /** forced page count */
    protected int forcePageCount;
    private String format;
    private int letterValue;
    private char groupingSeparator;
    private int groupingSize;
    private Numeric referenceOrientation; //XSL 1.1
    private String language;
    private String country;
    private String numberConversionFeatures;
    // End of property values

    private PageNumberGenerator pageNumberGenerator;

    /** starting page number */
    protected int startingPageNumber = 0;

    /**
     * Create an AbstractPageSequence that is a child
     * of the given parent {@link FONode}.
     *
     * @param parent the parent {@link FONode}
     */
    public AbstractPageSequence(FONode parent) {
        super(parent);
    }

    /** {@inheritDoc} */
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);
        initialPageNumber = pList.get(PR_INITIAL_PAGE_NUMBER).getNumeric();
        forcePageCount = pList.get(PR_FORCE_PAGE_COUNT).getEnum();
        format = pList.get(PR_FORMAT).getString();
        letterValue = pList.get(PR_LETTER_VALUE).getEnum();
        groupingSeparator = pList.get(PR_GROUPING_SEPARATOR).getCharacter();
        groupingSize = pList.get(PR_GROUPING_SIZE).getNumber().intValue();
        referenceOrientation = pList.get(PR_REFERENCE_ORIENTATION).getNumeric();
        language = pList.get(PR_LANGUAGE).getString();
        country = pList.get(PR_COUNTRY).getString();
        numberConversionFeatures = pList.get(PR_X_NUMBER_CONVERSION_FEATURES).getString();
    }

    /** {@inheritDoc} */
    protected void startOfNode() throws FOPException {
        this.pageNumberGenerator = new PageNumberGenerator(
                format, groupingSeparator, groupingSize, letterValue,
                numberConversionFeatures, language, country);

    }

    /**
     * Initialize the current page number for the start of the page sequence.
     */
    public void initPageNumber() {
        int pageNumberType = 0;

        if (initialPageNumber.getEnum() != 0) {
            // auto | auto-odd | auto-even.
            startingPageNumber = getRoot().getEndingPageNumberOfPreviousSequence() + 1;
            pageNumberType = initialPageNumber.getEnum();
            if (pageNumberType == EN_AUTO_ODD) {
                if (startingPageNumber % 2 == 0) {
                    startingPageNumber++;
                }
            } else if (pageNumberType == EN_AUTO_EVEN) {
                if (startingPageNumber % 2 == 1) {
                    startingPageNumber++;
                }
            }
        } else { // <integer> for explicit page number
            int pageStart = initialPageNumber.getValue();
            startingPageNumber = (pageStart > 0) ? pageStart : 1; // spec rule
        }
    }

    /**
     * Get the starting page number for this page sequence.
     *
     * @return the starting page number
     */
    public int getStartingPageNumber() {
        return startingPageNumber;
    }

    /**
     * Retrieves the string representation of a page number applicable
     * for this page sequence
     * @param pageNumber the page number
     * @return string representation of the page number
     */
    public String makeFormattedPageNumber(int pageNumber) {
        return pageNumberGenerator.makeFormattedPageNumber(pageNumber);
    }

    /**
     * Public accessor for the ancestor Root.
     * @return the ancestor Root
     */
    public Root getRoot() {
        return (Root)this.getParent();
    }

    /**
     * Get the value of the <code>force-page-count</code> property.
     * @return the force-page-count value
     */
    public int getForcePageCount() {
        return forcePageCount;
    }

    /**
     * Get the value of the <code>initial-page-number</code> property.
     * @return the initial-page-number property value
     */
    public Numeric getInitialPageNumber() {
        return initialPageNumber;
    }

    /**
     * Get the value of the <code>reference-orientation</code> property.
     * @return the "reference-orientation" property
     * @since XSL 1.1
     */
    public int getReferenceOrientation() {
        return referenceOrientation.getValue();
    }

}
