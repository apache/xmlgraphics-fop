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

// FOP
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOTreeVisitor;
import org.apache.fop.apps.FOPException;

/**
 * A repeatable-page-master-reference formatting object.
 * This handles a reference with a specified number of repeating
 * instances of the referenced page master (may have no limit).
 */
public class RepeatablePageMasterReference extends PageMasterReference
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
    protected void addProperties(Attributes attlist) throws FOPException {
        super.addProperties(attlist);
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
        return getMasterName();
    }

    /**
     * @see org.apache.fop.fo.pagination.SubSequenceSpecifier#reset()
     */
    public void reset() {
        this.numberConsumed = 0;
    }

    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.serveRepeatablePageMasterReference(this);
    }

}
