/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.pagination;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.PageMaster;
import org.apache.fop.apps.FOPException;

// Java
import java.util.*;

import org.xml.sax.Attributes;

public class PageSequenceMaster extends FObj {

    LayoutMasterSet layoutMasterSet;
    Vector subSequenceSpecifiers;


    // The terminology may be confusing. A 'page-sequence-master' consists
    // of a sequence of what the XSL spec refers to as
    // 'sub-sequence-specifiers'. These are, in fact, simple or complex
    // references to page-masters. So the methods use the former
    // terminology ('sub-sequence-specifiers', or SSS),
    // but the actual FO's are MasterReferences.
    public PageSequenceMaster(FObj parent) {
        super(parent);
        this.name = "fo:page-sequence-master";
    }

    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);

        subSequenceSpecifiers = new Vector();

        if (parent.getName().equals("fo:layout-master-set")) {
            this.layoutMasterSet = (LayoutMasterSet)parent;
            String pm = this.properties.get("master-name").getString();
            if (pm == null) {
                log.warn("page-sequence-master does not have "
                                       + "a page-master-name and so is being ignored");
            } else {
                this.layoutMasterSet.addPageSequenceMaster(pm, this);
            }
        } else {
            throw new FOPException("fo:page-sequence-master must be child "
                                   + "of fo:layout-master-set, not "
                                   + parent.getName());
        }
    }

    protected void addSubsequenceSpecifier(SubSequenceSpecifier pageMasterReference) {
        subSequenceSpecifiers.addElement(pageMasterReference);
    }

    protected SubSequenceSpecifier getSubSequenceSpecifier(int sequenceNumber) {
        if (sequenceNumber >= 0
                && sequenceNumber < getSubSequenceSpecifierCount()) {
            return (SubSequenceSpecifier)subSequenceSpecifiers.elementAt(sequenceNumber);
        }
        return null;


    }

    protected int getSubSequenceSpecifierCount() {
        return subSequenceSpecifiers.size();
    }

    public void reset() {
        for (Enumeration e = subSequenceSpecifiers.elements();
                e.hasMoreElements(); ) {
            ((SubSequenceSpecifier)e.nextElement()).reset();
        }

    }


}
