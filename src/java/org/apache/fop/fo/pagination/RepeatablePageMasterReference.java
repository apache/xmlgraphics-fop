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
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

// FOP
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;

/**
 * A repeatable-page-master-reference formatting object.
 * This handles a reference with a specified number of repeating
 * instances of the referenced page master (may have no limit).
 */
public class RepeatablePageMasterReference extends FObj
    implements SubSequenceSpecifier {

    private static final int INFINITE = -1;

    private PageSequenceMaster pageSequenceMaster;
    private int maximumRepeats;
    private int numberConsumed = 0;

    /**
     * @see org.apache.fop.fo.FONode#FONode(FONode)
     */
    public RepeatablePageMasterReference(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#addProperties
     */
    protected void addProperties(Attributes attlist) throws SAXParseException {
        super.addProperties(attlist);

        PageSequenceMaster pageSequenceMaster = (PageSequenceMaster) parent;

        if (getPropString(PR_MASTER_REFERENCE) == null) {
            missingPropertyError("master-reference");
        } else {
            pageSequenceMaster.addSubsequenceSpecifier(this);
        }

        String mr = getPropString(PR_MAXIMUM_REPEATS);

        if (mr.equals("no-limit")) {
            this.maximumRepeats = INFINITE;
        } else {
            try {
                this.maximumRepeats = Integer.parseInt(mr);
                if (this.maximumRepeats < 0) {
                    getLogger().debug("negative maximum-repeats: "
                                      + this.maximumRepeats);
                    this.maximumRepeats = 0;
                }
            } catch (NumberFormatException nfe) {
                throw new SAXParseException("Invalid number for "
                                       + "'maximum-repeats' property", locator);
            }
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
        if (maximumRepeats != INFINITE) {
            if (numberConsumed < maximumRepeats) {
                numberConsumed++;
            } else {
                return null;
            }
        }
        return getPropString(PR_MASTER_REFERENCE);
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
