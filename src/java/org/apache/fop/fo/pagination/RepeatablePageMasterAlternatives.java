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

// Java
import java.util.ArrayList;

// XML
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

// FOP
import org.apache.fop.fo.FOElementMapping;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FOTreeVisitor;
import org.apache.fop.apps.FOPException;

/**
 * A repeatable-page-master-alternatives formatting object.
 * This contains a list of conditional-page-master-reference
 * and the page master is found from the reference that
 * matches the page number and emptyness.
 */
public class RepeatablePageMasterAlternatives extends FObj
    implements SubSequenceSpecifier {

    private static final int INFINITE = -1;

    /**
     * Max times this page master can be repeated.
     * INFINITE is used for the unbounded case
     */
    private int maximumRepeats;
    private int numberConsumed = 0;

    private ArrayList conditionalPageMasterRefs;

    /**
     * @see org.apache.fop.fo.FONode#FONode(FONode)
     */
    public RepeatablePageMasterAlternatives(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
        XSL/FOP: (conditional-page-master-reference+)
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) {
        if (!(nsURI == FOElementMapping.URI &&
            localName.equals("conditional-page-master-reference"))) {
                invalidChildError(loc, nsURI, localName);
        }
    }

    /**
     * @see org.apache.fop.fo.FONode#end
     */
    protected void endOfNode() {
        if (childNodes == null) {
           missingChildElementError("(conditional-page-master-reference+)");
        }
    }

    /**
     * @see org.apache.fop.fo.FObj#addProperties
     */
    protected void addProperties(Attributes attlist) throws FOPException {
        super.addProperties(attlist);
        conditionalPageMasterRefs = new ArrayList();

        if (parent.getName().equals("fo:page-sequence-master")) {
            PageSequenceMaster pageSequenceMaster = (PageSequenceMaster)parent;
            pageSequenceMaster.addSubsequenceSpecifier(this);
        } else {
            throw new FOPException("fo:repeatable-page-master-alternatives "
                                   + "must be child of fo:page-sequence-master, not "
                                   + parent.getName());
        }

        String mr = getProperty(PR_MAXIMUM_REPEATS).getString();
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
                throw new FOPException("Invalid number for "
                                       + "'maximum-repeats' property");
            }
        }
    }

    /**
     * Get the next matching page master from the conditional
     * page master references.
     * @see org.apache.fop.fo.pagination.SubSequenceSpecifier
     */
    public String getNextPageMasterName(boolean isOddPage,
                                        boolean isFirstPage,
                                        boolean isBlankPage) {
        if (maximumRepeats != INFINITE) {
            if (numberConsumed < maximumRepeats) {
                numberConsumed++;
            } else {
                return null;
            }
        }

        for (int i = 0; i < conditionalPageMasterRefs.size(); i++) {
            ConditionalPageMasterReference cpmr =
                (ConditionalPageMasterReference)conditionalPageMasterRefs.get(i);
            if (cpmr.isValid(isOddPage, isFirstPage, isBlankPage)) {
                return cpmr.getMasterName();
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
    }

    /**
     * @see org.apache.fop.fo.pagination.SubSequenceSpecifier#reset()
     */
    public void reset() {
        this.numberConsumed = 0;
    }

    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.serveRepeatablePageMasterAlternatives(this);
    }

    public String getName() {
        return "fo:repeatable-page-master-alternatives";
    }
}
