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

import org.xml.sax.Attributes;

/**
 * A conditional-page-master-reference formatting object.
 * This is a reference to a page master with a set of conditions.
 * The conditions must be satisfied for the referenced master to
 * be used.
 * This element is must be the child of a repeatable-page-master-alternatives
 * element.
 */
public class ConditionalPageMasterReference extends FObj {

    private RepeatablePageMasterAlternatives repeatablePageMasterAlternatives;

    private String masterName;

    private int pagePosition;
    private int oddOrEven;
    private int blankOrNotBlank;

    public ConditionalPageMasterReference(FONode parent) {
        super(parent);
    }

    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);
        if (getProperty("master-reference") != null) {
            setMasterName(getProperty("master-reference").getString());
        }

        validateParent(parent);

        this.pagePosition = this.properties.get("page-position").getEnum();
        this.oddOrEven = this.properties.get("odd-or-even").getEnum();
        this.blankOrNotBlank = this.properties.get("blank-or-not-blank").getEnum();
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

    /**
     * Check if the conditions for this reference are met.
     * checks the page number and emptyness to determine if this
     * matches.
     */
    protected boolean isValid(boolean isOddPage, boolean isFirstPage,
                              boolean isEmptyPage) {
        // page-position
        if( isFirstPage ) {
            if (pagePosition==PagePosition.REST) {
                return false;
            } else if (pagePosition==PagePosition.LAST) {
                // how the hell do you know at this point?
                getLogger().debug("LAST PagePosition NYI");
                return false;
            }
        } else {
            if (pagePosition==PagePosition.FIRST) {
                return false;
            } else if (pagePosition==PagePosition.LAST) {
                // how the hell do you know at this point?
                getLogger().debug("LAST PagePosition NYI");
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

    /**
     * Check that the parent is the right type of formatting object
     * repeatable-page-master-alternatives.
     */
    protected void validateParent(FONode parent) throws FOPException {
        if (parent.getName().equals("fo:repeatable-page-master-alternatives")) {
            this.repeatablePageMasterAlternatives =
                (RepeatablePageMasterAlternatives)parent;

            if (getMasterName() == null) {
                getLogger().warn("single-page-master-reference"
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
