/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
package org.apache.fop.fo.pagination;

import org.apache.fop.fo.*;
import org.apache.fop.apps.FOPException;
import org.apache.fop.messaging.MessageHandler;

public class SinglePageMasterReference extends PageMasterReference
    implements SubSequenceSpecifier {

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
        this.state = FIRST;

    }

    public String getNextPageMaster(int currentPageNumber,
                                    boolean thisIsFirstPage,
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


    protected String getElementName() {
        return "fo:single-page-master-reference";
    }


}
