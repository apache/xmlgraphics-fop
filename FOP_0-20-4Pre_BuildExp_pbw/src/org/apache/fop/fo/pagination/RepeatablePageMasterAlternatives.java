/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.pagination;

import org.apache.fop.fo.*;
import org.apache.fop.apps.FOPException;

// Java
import java.util.ArrayList;

import org.xml.sax.Attributes;

/**
 * A repeatable-page-master-alternatives formatting object.
 * This contains a list of conditional-page-master-reference
 * and the page master is found from the reference that
 * matches the page number and emptyness.
 */
public class RepeatablePageMasterAlternatives extends FObj
    implements SubSequenceSpecifier {

    private static final int INFINITE = -1;

    private PageSequenceMaster pageSequenceMaster;

    /**
     * Max times this page master can be repeated.
     * INFINITE is used for the unbounded case
     */
    private int maximumRepeats;
    private int numberConsumed = 0;

    private ArrayList conditionalPageMasterRefs;

    public RepeatablePageMasterAlternatives(FONode parent) {
        super(parent);
    }

    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);

        conditionalPageMasterRefs = new ArrayList();

        if (parent.getName().equals("fo:page-sequence-master")) {
            this.pageSequenceMaster = (PageSequenceMaster)parent;
            this.pageSequenceMaster.addSubsequenceSpecifier(this);
        } else {
            throw new FOPException("fo:repeatable-page-master-alternatives"
                                   + "must be child of fo:page-sequence-master, not "
                                   + parent.getName());
        }

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

    /**
     * Get the next matching page master from the conditional
     * page master references.
     */
    public String getNextPageMaster(int currentPageNumber,
                                    boolean thisIsFirstPage,
                                    boolean isEmptyPage) {
        String pm = null;

        if (getMaximumRepeats() != INFINITE) {
            if (numberConsumed < getMaximumRepeats()) {
                numberConsumed++;
            } else {
                return null;
            }
        }

        for (int i = 0; i < conditionalPageMasterRefs.size(); i++) {
            ConditionalPageMasterReference cpmr =
                (ConditionalPageMasterReference)conditionalPageMasterRefs.get(i);

            // 0-indexed page number
            if (cpmr.isValid(currentPageNumber + 1, thisIsFirstPage,
                             isEmptyPage)) {
                pm = cpmr.getMasterName();
                break;
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

    public void addConditionalPageMasterReference(ConditionalPageMasterReference cpmr) {
        this.conditionalPageMasterRefs.add(cpmr);
    }

    public void reset() {
        this.numberConsumed = 0;
    }

    protected PageSequenceMaster getPageSequenceMaster() {
        return pageSequenceMaster;
    }

}
