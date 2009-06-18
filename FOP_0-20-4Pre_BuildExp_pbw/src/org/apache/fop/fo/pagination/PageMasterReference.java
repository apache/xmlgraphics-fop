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
 * Base PageMasterReference class. Provides implementation for handling the
 * master-reference attribute and containment within a PageSequenceMaster
 */
public abstract class PageMasterReference extends FObj
    implements SubSequenceSpecifier {

    private String _masterName;
    private PageSequenceMaster _pageSequenceMaster;

    public PageMasterReference(FONode parent) {
        super(parent);
    }

    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);
        if (getProperty("master-reference") != null) {
            setMasterName(getProperty("master-reference").getString());
        }
        validateParent(parent);

    }

    protected void setMasterName(String masterName) {
        _masterName = masterName;
    }

    /**
     * Returns the "master-reference" attribute of this page master reference
     */
    public String getMasterName() {
        return _masterName;
    }

    protected void setPageSequenceMaster(PageSequenceMaster pageSequenceMaster) {
        _pageSequenceMaster = pageSequenceMaster;
    }

    protected PageSequenceMaster getPageSequenceMaster() {
        return _pageSequenceMaster;
    }

    public abstract String getNextPageMaster(int currentPageNumber,
                                             boolean thisIsFirstPage,
                                             boolean isEmptyPage);

    /**
     * Checks that the parent is the right element. The default implementation
     * checks for fo:page-sequence-master
     */
    protected void validateParent(FONode parent) throws FOPException {
        if (parent.getName().equals("fo:page-sequence-master")) {
            _pageSequenceMaster = (PageSequenceMaster)parent;

            if (getMasterName() == null) {
                log.warn("" + getName()
                                       + " does not have a master-reference and so is being ignored");
            } else {
                _pageSequenceMaster.addSubsequenceSpecifier(this);
            }
        } else {
            throw new FOPException(getName() + " must be"
                                   + "child of fo:page-sequence-master, not "
                                   + parent.getName());
        }
    }

    public abstract void reset();

}
