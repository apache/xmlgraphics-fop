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

import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.apps.FOPException;

// Java
import java.util.ArrayList;

/**
 * Class modeling the fo:repeatable-page-master-alternatives object.
 *
 * @see <a href="@XSLFO-STD@#fo_repeatable-page-master-alternatives"
 *      target="_xslfostd">@XSLFO-STDID@
 *      &para;6.4.10</a>
 */
public class RepeatablePageMasterAlternatives extends FObj
    implements SubSequenceSpecifier {

    private static final int INFINITE = -1;


    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent, PropertyList propertyList,
                        String systemId, int line, int column)
            throws FOPException {
            return new RepeatablePageMasterAlternatives(parent, propertyList,
                                                        systemId, line, column);
        }

    }

    public static FObj.Maker maker() {
        return new RepeatablePageMasterAlternatives.Maker();
    }

    private PageSequenceMaster pageSequenceMaster;

    /**
     * Max times this page master can be repeated.
     * INFINITE is used for the unbounded case
     */
    private int maximumRepeats;
    private int numberConsumed = 0;

    private ArrayList conditionalPageMasterRefs;

    public RepeatablePageMasterAlternatives(FObj parent, PropertyList propertyList,
                                            String systemId, int line, int column)
        throws FOPException {
        super(parent, propertyList, systemId, line, column);

        if (parent.getName().equals("fo:page-sequence-master")) {
            this.pageSequenceMaster = (PageSequenceMaster)parent;
            this.pageSequenceMaster.addSubsequenceSpecifier(this);
        } else {
            throw new FOPException("A fo:repeatable-page-master-alternatives"
                                   + "must be child of fo:page-sequence-master, not "
                                   + parent.getName(), systemId, line, column);
        }

        String mr = getProperty("maximum-repeats").getString();
        if (mr.equals("no-limit")) {
            this.maximumRepeats=INFINITE;
        } else {
            try {
                this.maximumRepeats = Integer.parseInt(mr);
                if (this.maximumRepeats < 0) {
                    log.debug("negative maximum-repeats: "+this.maximumRepeats);
                    this.maximumRepeats = 0;
                }
            } catch (NumberFormatException nfe) {
                throw new FOPException("Invalid number for "
                                       + "'maximum-repeats' property", systemId, line, column);
            }
        }
        conditionalPageMasterRefs = new ArrayList();
    }

    public String getName() {
        return "fo:repeatable-page-master-alternatives";
    }

    public void addConditionalPageMasterReference(ConditionalPageMasterReference cpmr) {
        this.conditionalPageMasterRefs.add(cpmr);
    }

    public String getNextPageMasterName(boolean isOddPage,
                                        boolean isFirstPage,
                                        boolean isEmptyPage) {
        if (maximumRepeats != -1) {
            if (numberConsumed < maximumRepeats) {
                numberConsumed++;
            } else {
                return null;
            }
        }

        for (int i = 0; i < conditionalPageMasterRefs.size(); i++) {
            ConditionalPageMasterReference cpmr =
                (ConditionalPageMasterReference)conditionalPageMasterRefs.get(i);
            if (cpmr.isValid(isOddPage, isFirstPage, isEmptyPage)) {
                return cpmr.getMasterName();
            }
        }
        return null;
    }

    public void reset() {
        this.numberConsumed = 0;
    }

}
