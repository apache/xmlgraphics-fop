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
    private SubSequenceSpecifier currentSubSequence;
    int currentSubSequenceNumber;
    String masterName;

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

        if (parent.getName().equals("fo:layout-master-set")) {
            this.layoutMasterSet = (LayoutMasterSet)parent;
            this.masterName = this.properties.get("master-name").getString();
            if (this.masterName == null) {
                log.warn("page-sequence-master does not have "
                         + "a page-master-name and so is being ignored");
            } else {
                this.layoutMasterSet.addPageSequenceMaster(masterName, this);
            }
        } else {
            throw new FOPException("fo:page-sequence-master must be child "
                                   + "of fo:layout-master-set, not "
                                   + parent.getName());
        }
        subSequenceSpecifiers = new Vector();
        currentSubSequenceNumber = -1;
        currentSubSequence = null;
    }

    protected void addSubsequenceSpecifier(SubSequenceSpecifier pageMasterReference) {
        subSequenceSpecifiers.addElement(pageMasterReference);
    }

    protected SubSequenceSpecifier getNextSubSequence() {
        currentSubSequenceNumber++;
        if (currentSubSequenceNumber >= 0
            && currentSubSequenceNumber < subSequenceSpecifiers.size()) {
            return (SubSequenceSpecifier)subSequenceSpecifiers
              .elementAt(currentSubSequenceNumber);
        }
        return null;
    }

    public void reset() {
        currentSubSequenceNumber = -1;
        currentSubSequence = null;
        for (Enumeration e = subSequenceSpecifiers.elements();
                e.hasMoreElements(); ) {
            ((SubSequenceSpecifier)e.nextElement()).reset();
        }
    }

    public SimplePageMaster getNextSimplePageMaster(boolean oddPage,
                                                    boolean blankPage)
      throws FOPException {
        boolean firstPage = false;
        if (currentSubSequence==null) {
            currentSubSequence = getNextSubSequence();
            if (currentSubSequence==null) {
                throw new FOPException("no subsequences in page-sequence-master '"
                                       + masterName + "'");
            }
            firstPage = true;
        }
        String pageMasterName = currentSubSequence
          .getNextPageMasterName(oddPage, firstPage, blankPage);
        boolean canRecover = true;
        while (pageMasterName==null) {
            SubSequenceSpecifier nextSubSequence = getNextSubSequence();
            firstPage = true;
            if (nextSubSequence==null) {
                if (!canRecover) {
                    throw new FOPException("subsequences exhausted in page-sequence-master '"
                                           +masterName
                                           +"', cannot recover");
                }
                log.warn("subsequences exhausted in page-sequence-master '"
                         +masterName
                         +"', use previous subsequence");
                currentSubSequence.reset();
                canRecover = false;
            } else {
                currentSubSequence = nextSubSequence;
            }
            pageMasterName = currentSubSequence
              .getNextPageMasterName(oddPage, firstPage, blankPage);
        }
        SimplePageMaster pageMaster=this.layoutMasterSet
          .getSimplePageMaster(pageMasterName);
        if (pageMaster==null) {
            throw new FOPException("No simple-page-master matching '"
                                   + pageMasterName + "' in page-sequence-master '"
                                   + masterName +"'");
        }
        return pageMaster;
    }
}
