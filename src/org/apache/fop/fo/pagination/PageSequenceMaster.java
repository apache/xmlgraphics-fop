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
import org.apache.fop.messaging.MessageHandler;

// Java
import java.util.*;

public class PageSequenceMaster extends FObj {

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent,
                         PropertyList propertyList) throws FOPException {
            return new PageSequenceMaster(parent, propertyList);
        }

    }

    public static FObj.Maker maker() {
        return new PageSequenceMaster.Maker();
    }

    LayoutMasterSet layoutMasterSet;
    Vector subSequenceSpecifiers;


    // The terminology may be confusing. A 'page-sequence-master' consists
    // of a sequence of what the XSL spec refers to as
    // 'sub-sequence-specifiers'. These are, in fact, simple or complex
    // references to page-masters. So the methods use the former
    // terminology ('sub-sequence-specifiers', or SSS),
    // but the actual FO's are MasterReferences.
    protected PageSequenceMaster(FObj parent, PropertyList propertyList)
            throws FOPException {
        super(parent, propertyList);
        this.name = "fo:page-sequence-master";

        subSequenceSpecifiers = new Vector();

        if (parent.getName().equals("fo:layout-master-set")) {
            this.layoutMasterSet = (LayoutMasterSet)parent;
            String pm = this.properties.get("master-name").getString();
            if (pm == null) {
                MessageHandler.errorln("WARNING: page-sequence-master does not have "
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
