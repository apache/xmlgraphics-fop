/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.pagination;

import org.apache.fop.fo.*;
import org.apache.fop.apps.FOPException;

import org.xml.sax.Attributes;

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

    public RepeatablePageMasterReference(FONode parent) {
        super(parent);
    }

    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);

        String mr = getProperty("maximum-repeats").getString();
        if (mr.equals("no-limit")) {
            setMaximumRepeats(INFINITE);
        } else {
            try {
                setMaximumRepeats(Integer.parseInt(mr));
            } catch (NumberFormatException nfe) {
                throw new FOPException("Invalid number for "
                                       + "'maximum-repeats' property");
            }
        }
    }

    public String getNextPageMaster(int currentPageNumber,
                                    boolean thisIsFirstPage,
                                    boolean isEmptyPage) {
        String pm = getMasterName();

        if (getMaximumRepeats() != INFINITE) {
            if (numberConsumed < getMaximumRepeats()) {
                numberConsumed++;
            } else {
                pm = null;
            }
        }
        return pm;
    }

    private void setMaximumRepeats(int maximumRepeats) {
        if (maximumRepeats == INFINITE) {
            this.maximumRepeats = maximumRepeats;
        } else {
            this.maximumRepeats = (maximumRepeats < 0) ? 0 : maximumRepeats;
        }
    }

    private int getMaximumRepeats() {
        return this.maximumRepeats;
    }

    public void reset() {
        this.numberConsumed = 0;
    }

}
