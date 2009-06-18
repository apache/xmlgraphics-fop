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
import org.apache.fop.fo.properties.Property;

/**
 * A repeatable-page-master-reference formatting object.
 * This handles a reference with a specified number of repeating
 * instances of the referenced page master (may have no limit).
 */
public class RepeatablePageMasterReference extends FObj
    implements SubSequenceSpecifier {

    // The value of properties relevant for fo:repeatable-page-master-reference.
    private String masterReference;
    private Property maximumRepeats;
    // End of property values
    
    private static final int INFINITE = -1;

    private PageSequenceMaster pageSequenceMaster;
    private int numberConsumed = 0;

    /**
     * @see org.apache.fop.fo.FONode#FONode(FONode)
     */
    public RepeatablePageMasterReference(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#bind(PropertyList)
     */
    public void bind(PropertyList pList) throws FOPException {
        masterReference = pList.get(PR_MASTER_REFERENCE).getString();
        maximumRepeats = pList.get(PR_MAXIMUM_REPEATS);
        
        if (masterReference == null || masterReference.equals("")) {
            missingPropertyError("master-reference");
        }        
    }

    /**
     * @see org.apache.fop.fo.FONode#startOfNode
     */
    protected void startOfNode() throws FOPException {
        PageSequenceMaster pageSequenceMaster = (PageSequenceMaster) parent;

        if (masterReference == null) {
            missingPropertyError("master-reference");
        } else {
            pageSequenceMaster.addSubsequenceSpecifier(this);
        }
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
     * @see org.apache.fop.fo.pagination.SubSequenceSpecifier
     */
    public String getNextPageMasterName(boolean isOddPage,
                                        boolean isFirstPage,
                                        boolean isEmptyPage) {
        if (getMaximumRepeats() != INFINITE) {
            if (numberConsumed < getMaximumRepeats()) {
                numberConsumed++;
            } else {
                return null;
            }
        }
        return masterReference;
    }

    /**
     * Return the "maximum-repeats" property.
     */
    public int getMaximumRepeats() {
        if (maximumRepeats.getEnum() == EN_NO_LIMIT) {
            return INFINITE;
        } else {
            int mr = maximumRepeats.getNumeric().getValue();
            if (mr < 0) {
                getLogger().debug("negative maximum-repeats: "
                        + this.maximumRepeats);
                mr = 0;
            }
            return mr;
        }
    }

    /**
     * @see org.apache.fop.fo.pagination.SubSequenceSpecifier#reset()
     */
    public void reset() {
        this.numberConsumed = 0;
    }

    /**
     * @see org.apache.fop.fo.FObj#getName()
     */
    public String getName() {
        return "fo:repeatable-page-master-reference";
    }

    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_REPEATABLE_PAGE_MASTER_REFERENCE;
    }
}
