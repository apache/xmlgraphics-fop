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
import java.util.Vector;

public class RepeatablePageMasterAlternatives extends FObj
    implements SubSequenceSpecifier {

    private static final int INFINITE = -1;


    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent,
                         PropertyList propertyList) throws FOPException {
            return new RepeatablePageMasterAlternatives(parent, propertyList);
        }

    }

    public static FObj.Maker maker() {
        return new RepeatablePageMasterAlternatives.Maker();
    }

    private PageSequenceMaster pageSequenceMaster;

    /**
     * Max times this page master can be repeated.
     * INFINITE is used for the unbounded case
     */
    private int maximumRepeats;
    private int numberConsumed = 0;

    private Vector conditionalPageMasterRefs;

    public RepeatablePageMasterAlternatives(FObj parent, PropertyList propertyList)
            throws FOPException {
        super(parent, propertyList);
        this.name = "fo:repeatable-page-master-alternatives";

        if (parent.getName().equals("fo:page-sequence-master")) {
            this.pageSequenceMaster = (PageSequenceMaster)parent;
            this.pageSequenceMaster.addSubsequenceSpecifier(this);
        } else {
            throw new FOPException("A fo:repeatable-page-master-alternatives"
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
                    log.debug("negative maximum-repeats: "+this.maximumRepeats);
                    this.maximumRepeats = 0;
                }
            } catch (NumberFormatException nfe) {
                throw new FOPException("Invalid number for "
                                       + "'maximum-repeats' property");
            }
        }
        conditionalPageMasterRefs = new Vector();
    }

    public void addConditionalPageMasterReference(ConditionalPageMasterReference cpmr) {
        this.conditionalPageMasterRefs.addElement(cpmr);
    }

    public String getNextPageMasterName(boolean isOddPage,
                                        boolean isFirstPage,
                                        boolean isEmptyPage) {
        if (maximumRepeats != -1) {
            if (numberConsumed < maximumRepeats) {
                numberConsumed++;
            } else {
                return null;
            }
        }

        for (int i = 0; i < conditionalPageMasterRefs.size(); i++) {
            ConditionalPageMasterReference cpmr =
                (ConditionalPageMasterReference)conditionalPageMasterRefs
              .elementAt(i);
            if (cpmr.isValid(isOddPage, isFirstPage, isEmptyPage)) {
                return cpmr.getMasterName();
            }
        }
        return null;
    }

    public void reset() {
        this.numberConsumed = 0;
    }

}
