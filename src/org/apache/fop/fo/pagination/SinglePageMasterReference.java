/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
package org.apache.fop.fo.pagination;

import org.apache.fop.fo.*;
import org.apache.fop.apps.FOPException;

public class SinglePageMasterReference extends PageMasterReference {

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent,
                         PropertyList propertyList) throws FOPException {
            return new SinglePageMasterReference(parent, propertyList);
        }

    }

    public static FObj.Maker maker() {
        return new SinglePageMasterReference.Maker();
    }

    private static final int FIRST = 0;
    private static final int DONE = 1;

    private int state;

    public SinglePageMasterReference(FObj parent, PropertyList propertyList)
            throws FOPException {
        super(parent, propertyList);
        this.name = "fo:single-page-master-reference";
        if (getProperty("master-reference") != null) {
            this.masterName = getProperty("master-reference").getString();
            if (parent.getName().equals("fo:page-sequence-master")) {
                PageSequenceMaster pageSequenceMaster = (PageSequenceMaster)parent;
                pageSequenceMaster.addSubsequenceSpecifier(this);
            } else {
                throw new FOPException("A fo:single-page-master-reference must be child of fo:page-sequence-master, not "
                                       + parent.getName());
            }
        } else {
          log.warn("A fo:single-page-master-reference does not have a master-reference and so is being ignored");
        }
        this.state = FIRST;
    }

    public String getNextPageMasterName(boolean isOddPage,
                                        boolean isFirstPage,
                                        boolean isEmptyPage) {
        if (this.state == FIRST) {
            this.state = DONE;
            return getMasterName();
        } else {
            return null;
        }

    }

    public void reset() {
        this.state = FIRST;
    }

}
