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
            PageSequenceMaster pageSequenceMaster = (PageSequenceMaster)parent;
            pageSequenceMaster.addSubsequenceSpecifier(this);
        } else {
            throw new FOPException("fo:repeatable-page-master-alternatives "
                                   + "must be child of fo:page-sequence-master, not "
                                   + parent.getName());
        }

        String mr = getProperty("maximum-repeats").getString();
        if (mr.equals("no-limit")) {
            this.maximumRepeats=INFINITE;
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

        for (int i = 0; i < conditionalPageMasterRefs.size(); i++) {
            ConditionalPageMasterReference cpmr =
                (ConditionalPageMasterReference)conditionalPageMasterRefs.get(i);
            if (cpmr.isValid(isOddPage, isFirstPage, isEmptyPage)) {
                return cpmr.getMasterName();
            }
        }
        return null;
    }


    public void addConditionalPageMasterReference(ConditionalPageMasterReference cpmr) {
        this.conditionalPageMasterRefs.add(cpmr);
    }

    public void reset() {
        this.numberConsumed = 0;
    }

}
