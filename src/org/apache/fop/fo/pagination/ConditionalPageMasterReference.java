/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.pagination;

import org.apache.fop.fo.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.apps.FOPException;

public class ConditionalPageMasterReference extends FObj {

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent,
                         PropertyList propertyList) throws FOPException {
            return new ConditionalPageMasterReference(parent, propertyList);
        }

    }

    public static FObj.Maker maker() {
        return new ConditionalPageMasterReference.Maker();
    }

    private RepeatablePageMasterAlternatives repeatablePageMasterAlternatives;

    private String masterName;

    private int pagePosition;
    private int oddOrEven;
    private int blankOrNotBlank;

    public ConditionalPageMasterReference(FObj parent, PropertyList propertyList)
            throws FOPException {
        super(parent, propertyList);

        if (getProperty("master-reference") != null) {
            this.masterName = getProperty("master-reference").getString();
        }
        if (parent.getName().equals("fo:repeatable-page-master-alternatives")) {
            this.repeatablePageMasterAlternatives =
                (RepeatablePageMasterAlternatives)parent;
            if (masterName == null) {
                log.warn("A fo:conditional-page-master-reference does not have a master-reference and so is being ignored");
            } else {
                this.repeatablePageMasterAlternatives.addConditionalPageMasterReference(this);
            }
        } else {
            throw new FOPException("fo:conditional-page-master-reference must be child "
                                   + "of fo:repeatable-page-master-alternatives, not "
                                   + parent.getName());
        }
        this.pagePosition = this.properties.get("page-position").getEnum();
        this.oddOrEven = this.properties.get("odd-or-even").getEnum();
        this.blankOrNotBlank = this.properties.get("blank-or-not-blank").getEnum();
    }

    public String getName() {
        return "fo:conditional-page-master-reference";
    }

    protected boolean isValid(boolean isOddPage, boolean isFirstPage,
                              boolean isEmptyPage) {
        // page-position
        if( isFirstPage ) {
            if (pagePosition==PagePosition.REST) {
                return false;
            } else if (pagePosition==PagePosition.LAST) {
                // how the hell do you know at this point?
                log.debug("LAST PagePosition NYI");
                return false;
            }
        } else {
            if (pagePosition==PagePosition.FIRST) {
                return false;
            } else if (pagePosition==PagePosition.LAST) {
                // how the hell do you know at this point?
                log.debug("LAST PagePosition NYI");
                // potentially valid, don't return
            }
        }

        // odd-or-even
        if (isOddPage) {
            if (oddOrEven==OddOrEven.EVEN) {
              return false;
            }
        } else {
            if (oddOrEven==OddOrEven.ODD) {
              return false;
            }
        }

        // blank-or-not-blank
        if (isEmptyPage) {
            if (blankOrNotBlank==BlankOrNotBlank.NOT_BLANK) {
                return false;
            }
        } else {
            if (blankOrNotBlank==BlankOrNotBlank.BLANK) {
                return false;
            }
        }

        return true;

    }

    protected int getPagePosition() {
        return this.pagePosition;
    }

    protected int getOddOrEven() {
        return this.oddOrEven;
    }

    protected int getBlankOrNotBlank() {
        return this.blankOrNotBlank;
    }

    public String getMasterName() {
        return masterName;
    }
}
