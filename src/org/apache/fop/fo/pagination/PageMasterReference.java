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

    private String masterName;

    public PageMasterReference(FONode parent) {
        super(parent);
    }

    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);
        if (getProperty("master-reference") != null) {
            this.masterName = getProperty("master-reference").getString();
        }
        validateParent(parent);
    }


    /**
     * Returns the "master-reference" attribute of this page master reference
     */
    public String getMasterName() {
        return masterName;
    }

    /**
     * Checks that the parent is the right element. The default implementation
     * checks for fo:page-sequence-master
     */
    protected void validateParent(FONode parent) throws FOPException {
        if (parent.getName().equals("fo:page-sequence-master")) {
            PageSequenceMaster pageSequenceMaster = (PageSequenceMaster)parent;

            if (getMasterName() == null) {
                getLogger().warn("" + getName()
                                       + " does not have a master-reference and so is being ignored");
            } else {
                pageSequenceMaster.addSubsequenceSpecifier(this);
            }
        } else {
            throw new FOPException(getName() + " must be"
                                   + "child of fo:page-sequence-master, not "
                                   + parent.getName());
        }
    }

}
