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

    private int maximumRepeats;
    private int numberConsumed = 0;

    public RepeatablePageMasterReference(FObj parent, PropertyList propertyList)
            throws FOPException {
        super(parent, propertyList);
        if (getProperty("master-reference") != null) {
            this.masterName = getProperty("master-reference").getString();
            if (parent.getName().equals("fo:page-sequence-master")) {
                PageSequenceMaster pageSequenceMaster = (PageSequenceMaster)parent;
                pageSequenceMaster.addSubsequenceSpecifier(this);
            } else {
                throw new FOPException("A fo:repeatable-page-master-reference must be child of fo:page-sequence-master, not "
                                       + parent.getName());
            }
        } else {
          log.warn("A fo:repeatable-page-master-reference does not have a master-reference and so is being ignored");
        }
        String mr = getProperty("maximum-repeats").getString();
        if (mr.equals("no-limit")) {
            this.maximumRepeats = INFINITE;
        } else {
            try {
                this.maximumRepeats = Integer.parseInt(mr);
                if (this.maximumRepeats < 0) {
                    log.debug("negative maximum-repeats: "+this.maximumRepeats);
                    this.maximumRepeats = 0;
                }
            } catch (NumberFormatException nfe) {
                throw new FOPException("Invalid number '" + mr
                                       + "'for 'maximum-repeats' property");
            }
        }
    }

    public String getName() {
        return "fo:repeatable-page-master-reference";
    }

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

    public void reset() {
        this.numberConsumed = 0;
    }

}
