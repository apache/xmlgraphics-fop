/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.pagination;

import org.apache.fop.fo.*;
import org.apache.fop.apps.FOPException;

public class RepeatablePageMasterReference extends PageMasterReference
    implements SubSequenceSpecifier {

    private static final int INFINITE = -1;

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent,
                         PropertyList propertyList) throws FOPException {
            return new RepeatablePageMasterReference(parent, propertyList);
        }

    }

    public static FObj.Maker maker() {
        return new RepeatablePageMasterReference.Maker();
    }


    private PageSequenceMaster pageSequenceMaster;

    private int maximumRepeats;
    private int numberConsumed = 0;

    public RepeatablePageMasterReference(FObj parent, PropertyList propertyList)
            throws FOPException {
        super(parent, propertyList);

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

    protected String getElementName() {
        return "fo:repeatable-page-master-reference";
    }

    public void reset() {
        this.numberConsumed = 0;
    }

}
