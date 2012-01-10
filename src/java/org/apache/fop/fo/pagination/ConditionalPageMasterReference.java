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

// XML
import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.layoutmgr.BlockLevelEventProducer;

/**
 * Class modelling the <a href="http://www.w3.org/TR/xsl/#fo_conditional-page-master-reference">
 * <code>fo:conditional-page-master-reference</code></a> object.
 *
 * This is a reference to a page master with a set of conditions.
 * The conditions must be satisfied for the referenced master to
 * be used.
 * This element is must be the child of a repeatable-page-master-alternatives
 * element.
 */
public class ConditionalPageMasterReference extends FObj {
    // The value of properties relevant for fo:conditional-page-master-reference.
    private String masterReference;
    // The simple page master referenced
    private SimplePageMaster master;
    private int pagePosition;
    private int oddOrEven;
    private int blankOrNotBlank;
    // End of property values

    /**
     * Create a ConditionalPageMasterReference instance that is a
     * child of the given {@link FONode}.
     *
     * @param parent {@link FONode} that is the parent of this object
     */
    public ConditionalPageMasterReference(FONode parent) {
        super(parent);
    }

    /** {@inheritDoc} */
    public void bind(PropertyList pList) throws FOPException {
        masterReference = pList.get(PR_MASTER_REFERENCE).getString();
        pagePosition = pList.get(PR_PAGE_POSITION).getEnum();
        oddOrEven = pList.get(PR_ODD_OR_EVEN).getEnum();
        blankOrNotBlank = pList.get(PR_BLANK_OR_NOT_BLANK).getEnum();

        if (masterReference == null || masterReference.equals("")) {
            missingPropertyError("master-reference");
        }
    }

    /** {@inheritDoc} */
    protected void startOfNode() throws FOPException {
        getConcreteParent().addConditionalPageMasterReference(this);
    }

    private RepeatablePageMasterAlternatives getConcreteParent() {
        return (RepeatablePageMasterAlternatives) parent;
    }

    /**
     * {@inheritDoc}
     * <br>XSL Content Model: empty
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName)
           throws ValidationException {
       invalidChildError(loc, nsURI, localName);
    }

    /**
     * Check if the conditions for this reference are met.
     * checks the page number and emptyness to determine if this
     * matches.
     * @param isOddPage True if page number odd
     * @param isFirstPage True if page is first page
     * @param isLastPage True if page is last page
     * @param isBlankPage True if page is blank
     * @return True if the conditions for this reference are met
     */
    protected boolean isValid(boolean isOddPage,
                              boolean isFirstPage,
                              boolean isLastPage,
                              boolean isBlankPage) {

        return (
            // page-position
            (pagePosition == EN_ANY
                || (pagePosition == EN_FIRST && isFirstPage)
                || (pagePosition == EN_LAST && isLastPage)
                || (pagePosition == EN_ONLY && (isFirstPage && isLastPage))
                || (pagePosition == EN_REST && !(isFirstPage || isLastPage))
                )
            // odd-or-even
            && (oddOrEven == EN_ANY
                || (oddOrEven == EN_ODD && isOddPage)
                || (oddOrEven == EN_EVEN && !isOddPage)
                )
            // blank-or-not-blank
            && (blankOrNotBlank == EN_ANY
                || (blankOrNotBlank == EN_BLANK && isBlankPage)
                || (blankOrNotBlank == EN_NOT_BLANK && !isBlankPage)
                ));


    }

    /**
     * Get the value for the <code>master-reference</code> property.
     * @return the "master-reference" property
     */
    public SimplePageMaster getMaster() {
        return master;
    }

    /**
     * Get the value for the <code>page-position</code> property.
     * @return the page-position property value
     */
    public int getPagePosition() {
        return this.pagePosition;
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return "conditional-page-master-reference";
    }

    /**
     * {@inheritDoc}
     * @return {@link org.apache.fop.fo.Constants#FO_CONDITIONAL_PAGE_MASTER_REFERENCE}
     */
    public int getNameId() {
        return FO_CONDITIONAL_PAGE_MASTER_REFERENCE;
    }

    /**
     * called by the parent RepeatablePageMasterAlternatives to resolve object references
     * from  simple page master reference names
     * @param layoutMasterSet the layout-master-set
     * @throws ValidationException when a named reference cannot be resolved
     * */
    public void resolveReferences(LayoutMasterSet layoutMasterSet) throws ValidationException {
        master = layoutMasterSet.getSimplePageMaster(masterReference);
        if (master == null) {
            BlockLevelEventProducer.Provider.get(
                getUserAgent().getEventBroadcaster())
                .noMatchingPageMaster(this, parent.getName(), masterReference, getLocator());
        }
    }
}
