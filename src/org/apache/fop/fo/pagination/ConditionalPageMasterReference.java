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

        this.name = getElementName();
        if (getProperty("master-name") != null) {
            setMasterName(getProperty("master-name").getString());
        }

        validateParent(parent);

        setPagePosition(this.properties.get("page-position").getEnum());
        setOddOrEven(this.properties.get("odd-or-even").getEnum());
        setBlankOrNotBlank(this.properties.get("blank-or-not-blank").getEnum());


    }

    protected void setMasterName(String masterName) {
        this.masterName = masterName;
    }

    /**
     * Returns the "master-name" attribute of this page master reference
     */
    public String getMasterName() {
        return masterName;
    }


    protected boolean isValid(int currentPageNumber, boolean thisIsFirstPage,
                              boolean isEmptyPage) {
        // page-position
        boolean okOnPagePosition = true;    // default is 'any'
        switch (getPagePosition()) {
        case PagePosition.FIRST:
            if (!thisIsFirstPage)
                okOnPagePosition = false;
            break;
        case PagePosition.LAST:
            // how the hell do you know at this point?
            log.debug("LAST PagePosition NYI");
            okOnPagePosition = true;
            break;
        case PagePosition.REST:
            if (thisIsFirstPage)
                okOnPagePosition = false;
            break;
        case PagePosition.ANY:
            okOnPagePosition = true;
        }

        // odd or even
        boolean okOnOddOrEven = true;    // default is 'any'
        int ooe = getOddOrEven();
        boolean isOddPage = ((currentPageNumber % 2) == 1) ? true : false;
        if ((OddOrEven.ODD == ooe) &&!isOddPage) {
            okOnOddOrEven = false;
        }
        if ((OddOrEven.EVEN == ooe) && isOddPage) {
            okOnOddOrEven = false;
        }

        // experimental check for blank-or-not-blank

        boolean okOnBlankOrNotBlank = true;    // default is 'any'

        int bnb = getBlankOrNotBlank();

        if ((BlankOrNotBlank.BLANK == bnb) &&!isEmptyPage) {
            okOnBlankOrNotBlank = false;
        } else if ((BlankOrNotBlank.NOT_BLANK == bnb) && isEmptyPage) {
            okOnBlankOrNotBlank = false;
        }

        return (okOnOddOrEven && okOnPagePosition && okOnBlankOrNotBlank);

    }

    protected void setPagePosition(int pagePosition) {
        this.pagePosition = pagePosition;
    }

    protected int getPagePosition() {
        return this.pagePosition;
    }

    protected void setOddOrEven(int oddOrEven) {
        this.oddOrEven = oddOrEven;
    }

    protected int getOddOrEven() {
        return this.oddOrEven;
    }

    protected void setBlankOrNotBlank(int blankOrNotBlank) {
        this.blankOrNotBlank = blankOrNotBlank;
    }

    protected int getBlankOrNotBlank() {
        return this.blankOrNotBlank;
    }

    protected String getElementName() {
        return "fo:conditional-page-master-reference";
    }


    protected void validateParent(FObj parent) throws FOPException {
        if (parent.getName().equals("fo:repeatable-page-master-alternatives")) {
            this.repeatablePageMasterAlternatives =
                (RepeatablePageMasterAlternatives)parent;

            if (getMasterName() == null) {
                log.warn("single-page-master-reference"
                                       + "does not have a master-name and so is being ignored");
            } else {
                this.repeatablePageMasterAlternatives.addConditionalPageMasterReference(this);
            }
        } else {
            throw new FOPException("fo:conditional-page-master-reference must be child "
                                   + "of fo:repeatable-page-master-alternatives, not "
                                   + parent.getName());
        }
    }


}
