/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
import org.xml.sax.SAXParseException;

// FOP
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;

/**
 * A single-page-master-reference formatting object.
 * This is a reference for a single page. It returns the
 * master name only once until reset.
 */
public class SinglePageMasterReference extends FObj 
    implements SubSequenceSpecifier {

    // The value of properties relevant for fo:single-page-master-reference.
    private String masterReference;
    // End of property values
    
    private static final int FIRST = 0;
    private static final int DONE = 1;

    private int state;

    /**
     * @see org.apache.fop.fo.FONode#FONode(FONode)
     */
    public SinglePageMasterReference(FONode parent) {
        super(parent);
        this.state = FIRST;
    }

    /**
     * @see org.apache.fop.fo.FObj#bind(PropertyList)
     */
    public void bind(PropertyList pList) {
        masterReference = pList.get(PR_MASTER_REFERENCE).getString();
    }

    /**
     * @see org.apache.fop.fo.FONode#startOfNode
     */
    protected void startOfNode() throws SAXParseException {
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
        throws SAXParseException {
       invalidChildError(loc, nsURI, localName);
    }

    /**
     * @see org.apache.fop.fo.pagination.SubSequenceSpecifier
     */
    public String getNextPageMasterName(boolean isOddPage,
                                        boolean isFirstPage,
                                        boolean isEmptyPage) {
        if (this.state == FIRST) {
            this.state = DONE;
            return getPropString(PR_MASTER_REFERENCE);
        } else {
            return null;
        }
    }

    /**
     * @see org.apache.fop.fo.pagination.SubSequenceSpecifier#reset()
     */
    public void reset() {
        this.state = FIRST;
    }

    public String getName() {
        return "fo:single-page-master-reference";
    }

    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_SINGLE_PAGE_MASTER_REFERENCE;
    }
}

