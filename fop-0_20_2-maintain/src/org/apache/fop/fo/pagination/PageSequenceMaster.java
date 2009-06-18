/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
package org.apache.fop.fo.pagination;

// FOP
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.apps.FOPException;

// Java
import java.util.ArrayList;

/**
 * Class modeling the fo:page-sequence-master object.
 *
 * @see <a href="@XSLFO-STD@#fo_page-sequence-master"
       target="_xslfostd">@XSLFO-STDID@
 *     &para;6.4.7</a>
 */
public class PageSequenceMaster extends FObj {

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent, PropertyList propertyList,
                        String systemId, int line, int column)
            throws FOPException {
            return new PageSequenceMaster(parent, propertyList,
                                          systemId, line, column);
        }

    }

    public static FObj.Maker maker() {
        return new PageSequenceMaster.Maker();
    }

    private LayoutMasterSet layoutMasterSet;
    private ArrayList subSequenceSpecifiers;
    private SubSequenceSpecifier currentSubSequence;
    private int currentSubSequenceNumber;
    private String masterName;

    // The terminology may be confusing. A 'page-sequence-master' consists
    // of a sequence of what the XSL spec refers to as
    // 'sub-sequence-specifiers'. These are, in fact, simple or complex
    // references to page-masters. So the methods use the former
    // terminology ('sub-sequence-specifiers', or SSS),
    // but the actual FO's are MasterReferences.
    protected PageSequenceMaster(FObj parent, PropertyList propertyList,
                                 String systemId, int line, int column)
            throws FOPException {
        super(parent, propertyList, systemId, line, column);

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
                                   + parent.getName(), systemId, line, column);
        }
        subSequenceSpecifiers = new ArrayList();
        currentSubSequenceNumber = -1;
        currentSubSequence = null;
    }

    protected void addSubsequenceSpecifier(SubSequenceSpecifier pageMasterReference) {
        subSequenceSpecifiers.add(pageMasterReference);
    }

    protected SubSequenceSpecifier getNextSubSequence() {
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
                                                    boolean blankPage)
      throws FOPException {
        boolean firstPage = false;
        if (currentSubSequence==null) {
            currentSubSequence = getNextSubSequence();
            if (currentSubSequence==null) {
                throw new FOPException("no subsequences in page-sequence-master '"
                                       + masterName + "'", systemId, line, column);
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
                                           + masterName
                                           + "', cannot recover", systemId, line, column);
                }
                log.warn("subsequences exhausted in page-sequence-master '"
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
                                   + masterName +"'", systemId, line, column);
        }
        return pageMaster;
    }

    public String getName() {
        return "fo:page-sequence-master";
    }

}
