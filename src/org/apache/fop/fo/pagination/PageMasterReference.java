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
import org.apache.fop.messaging.MessageHandler;

/**
 * Base PageMasterReference class. Provides implementation for handling the
 * master-name attribute and containment within a PageSequenceMaster
 */
public abstract class PageMasterReference extends FObj
    implements SubSequenceSpecifier {

    private String _masterName;
    private PageSequenceMaster _pageSequenceMaster;

    public PageMasterReference(FObj parent, PropertyList propertyList)
            throws FOPException {
        super(parent, propertyList);
        this.name = getElementName();
        if (getProperty("master-name") != null) {
            setMasterName(getProperty("master-name").getString());
        }
        validateParent(parent);

    }

    protected void setMasterName(String masterName) {
        _masterName = masterName;
    }

    /**
     * Returns the "master-name" attribute of this page master reference
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
     * Gets the formating object name for this object. Subclasses must provide this.
     *
     * @return the element name of this reference. e.g. fo:repeatable-page-master-reference
     */
    protected abstract String getElementName();

    /**
     * Checks that the parent is the right element. The default implementation
     * checks for fo:page-sequence-master
     */
    protected void validateParent(FObj parent) throws FOPException {
        if (parent.getName().equals("fo:page-sequence-master")) {
            _pageSequenceMaster = (PageSequenceMaster)parent;

            if (getMasterName() == null) {
                MessageHandler.errorln("WARNING: " + getElementName()
                                       + " does not have a master-name and so is being ignored");
            } else {
                _pageSequenceMaster.addSubsequenceSpecifier(this);
            }
        } else {
            throw new FOPException(getElementName() + " must be"
                                   + "child of fo:page-sequence-master, not "
                                   + parent.getName());
        }
    }

    public abstract void reset();




}
