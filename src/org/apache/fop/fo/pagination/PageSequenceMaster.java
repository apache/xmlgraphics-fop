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
import java.util.ArrayList;
import java.util.Iterator;

import org.xml.sax.Attributes;

/**
 * The page-sequence-master formatting object.
 * This class handles a list of subsequence specifiers
 * which are simple or complex references to page-masters.
 */
public class PageSequenceMaster extends FObj {
    LayoutMasterSet layoutMasterSet;
    ArrayList subSequenceSpecifiers;
    private SubSequenceSpecifier currentSubSequence;
    private int currentSubSequenceNumber;
    private String masterName;

    // The terminology may be confusing. A 'page-sequence-master' consists
    // of a sequence of what the XSL spec refers to as
    // 'sub-sequence-specifiers'. These are, in fact, simple or complex
    // references to page-masters. So the methods use the former
    // terminology ('sub-sequence-specifiers', or SSS),
    // but the actual FO's are MasterReferences.
    public PageSequenceMaster(FONode parent) {
        super(parent);
    }

    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);

        subSequenceSpecifiers = new ArrayList();

        if (parent.getName().equals("fo:layout-master-set")) {
            this.layoutMasterSet = (LayoutMasterSet)parent;
            String pm = this.properties.get("master-name").getString();
            if (pm == null) {
                getLogger().warn("page-sequence-master does not have "
                                       + "a master-name and so is being ignored");
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
        subSequenceSpecifiers.add(pageMasterReference);
    }

    private SubSequenceSpecifier getNextSubSequence() {
        currentSubSequenceNumber++;
        if (currentSubSequenceNumber >= 0
            && currentSubSequenceNumber < subSequenceSpecifiers.size()) {
            return (SubSequenceSpecifier)subSequenceSpecifiers
              .get(currentSubSequenceNumber);
        }
        return null;
    }

    public void reset() {
        currentSubSequenceNumber = -1;
        currentSubSequence = null;
        for (int i = 0; i< subSequenceSpecifiers.size(); i++ ) {
            ((SubSequenceSpecifier)subSequenceSpecifiers.get(i)).reset();
        }
    }

    public SimplePageMaster getNextSimplePageMaster(boolean oddPage,
                                                    boolean firstPage,
                                                    boolean blankPage)
      throws FOPException {
        if (currentSubSequence==null) {
            currentSubSequence = getNextSubSequence();
            if (currentSubSequence==null) {
                throw new FOPException("no subsequences in page-sequence-master '"
                                       + masterName + "'");
            }
        }
        String pageMasterName = currentSubSequence
          .getNextPageMasterName(oddPage, firstPage, blankPage);
        boolean canRecover = true;
        while (pageMasterName==null) {
            SubSequenceSpecifier nextSubSequence = getNextSubSequence();
            if (nextSubSequence==null) {
                if (!canRecover) {
                    throw new FOPException("subsequences exhausted in page-sequence-master '"
                                           + masterName
                                           + "', cannot recover");
                }
                getLogger().warn("subsequences exhausted in page-sequence-master '"
                                 + masterName
                                 + "', use previous subsequence");
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

