/*
 * Copyright 1999-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

/**
 * A conditional-page-master-reference formatting object.
 * This is a reference to a page master with a set of conditions.
 * The conditions must be satisfied for the referenced master to
 * be used.
 * This element is must be the child of a repeatable-page-master-alternatives
 * element.
 */
public class ConditionalPageMasterReference extends FObj {
    // The value of properties relevant for fo:conditional-page-master-reference.
    private String masterReference;
    private int pagePosition;
    private int oddOrEven;
    private int blankOrNotBlank;
    // End of property values
    
    private RepeatablePageMasterAlternatives repeatablePageMasterAlternatives;

    /**
     * @see org.apache.fop.fo.FONode#FONode(FONode)
     */
    public ConditionalPageMasterReference(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#bind(PropertyList)
     */
    public void bind(PropertyList pList) throws FOPException {
        masterReference = pList.get(PR_MASTER_REFERENCE).getString();
        pagePosition = pList.get(PR_PAGE_POSITION).getEnum();
        oddOrEven = pList.get(PR_ODD_OR_EVEN).getEnum();
        blankOrNotBlank = pList.get(PR_BLANK_OR_NOT_BLANK).getEnum();

        if (masterReference == null || masterReference.equals("")) {
            missingPropertyError("master-reference");
        }        
    }

    /**
     * @see org.apache.fop.fo.FONode#startOfNode
     */
    protected void startOfNode() throws FOPException {
        this.repeatablePageMasterAlternatives =
            (RepeatablePageMasterAlternatives) parent;
        this.repeatablePageMasterAlternatives.addConditionalPageMasterReference(this);
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
     * XSL Content Model: empty
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
     * @param isBlankPage True if page is blank
     * @return True if the conditions for this reference are met
     */
    protected boolean isValid(boolean isOddPage,
                              boolean isFirstPage,
                              boolean isBlankPage) {
        // page-position
        if (isFirstPage) {
            if (pagePosition == EN_REST) {
                return false;
            } else if (pagePosition == EN_LAST) {
                // ?? how can one know at this point?
                getLogger().debug("LAST PagePosition NYI");
                return false;
            }
        } else {
            if (pagePosition == EN_FIRST) {
                return false;
            } else if (pagePosition == EN_LAST) {
                // ?? how can one know at this point?
                getLogger().debug("LAST PagePosition NYI");
                // potentially valid, don't return
            }
        }

        // odd-or-even
        if (isOddPage) {
            if (oddOrEven == EN_EVEN) {
              return false;
            }
        } else {
            if (oddOrEven == EN_ODD) {
              return false;
            }
        }

        // blank-or-not-blank
        if (isBlankPage) {
            if (blankOrNotBlank == EN_NOT_BLANK) {
                return false;
            }
        } else {
            if (blankOrNotBlank == EN_BLANK) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the "master-reference" property.
     */
    public String getMasterReference() {
        return masterReference;
    }
    
    /**
     * @see org.apache.fop.fo.FObj#getName()
     */
    public String getName() {
        return "fo:conditional-page-master-reference";
    }
    
    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_CONDITIONAL_PAGE_MASTER_REFERENCE;
    }
}
