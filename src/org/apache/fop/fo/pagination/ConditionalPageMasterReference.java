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
import org.apache.fop.fo.properties.PagePosition;
import org.apache.fop.fo.properties.OddOrEven;
import org.apache.fop.fo.properties.BlankOrNotBlank;
import org.apache.fop.apps.FOPException;

/**
 * Class modeling the fo:conditional-page-master-reference object.
 *
 * @see <a href="@XSLFO-STD@#fo_conditional-page-master-reference"
       target="_xslfostd">@XSLFO-STDID@
 *     &para;6.4.11</a>
 */
public class ConditionalPageMasterReference extends FObj {

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent, PropertyList propertyList,
                        String systemId, int line, int column)
            throws FOPException {
            return new ConditionalPageMasterReference(parent, propertyList,
                                                      systemId, line, column);
        }

    }

    public static FObj.Maker maker() {
        return new ConditionalPageMasterReference.Maker();
    }

    private RepeatablePageMasterAlternatives repeatablePageMasterAlternatives;

    private String masterName;

    private int pagePosition;
    private int oddOrEven;
    private int blankOrNotBlank;

    public ConditionalPageMasterReference(FObj parent, PropertyList propertyList,
                                          String systemId, int line, int column)
        throws FOPException {
        super(parent, propertyList, systemId, line, column);

        if (getProperty("master-reference") != null) {
            this.masterName = getProperty("master-reference").getString();
        }
        if (parent.getName().equals("fo:repeatable-page-master-alternatives")) {
            this.repeatablePageMasterAlternatives =
                (RepeatablePageMasterAlternatives)parent;
            if (masterName == null) {
                log.warn("A fo:conditional-page-master-reference does not have a master-reference and so is being ignored");
            } else {
                this.repeatablePageMasterAlternatives.addConditionalPageMasterReference(this);
            }
        } else {
            throw new FOPException("fo:conditional-page-master-reference must be child "
                                   + "of fo:repeatable-page-master-alternatives, not "
                                   + parent.getName(), systemId, line, column);
        }
        this.pagePosition = this.properties.get("page-position").getEnum();
        this.oddOrEven = this.properties.get("odd-or-even").getEnum();
        this.blankOrNotBlank = this.properties.get("blank-or-not-blank").getEnum();
    }

    public String getName() {
        return "fo:conditional-page-master-reference";
    }

    protected boolean isValid(boolean isOddPage, boolean isFirstPage,
                              boolean isEmptyPage) {
        // page-position
        if( isFirstPage ) {
            if (pagePosition==PagePosition.REST) {
                return false;
            } else if (pagePosition==PagePosition.LAST) {
                // how the hell do you know at this point?
                log.warn("conditional-page-master-reference: page-position='last' is not yet implemented (NYI)");
                return false;
            }
        } else {
            if (pagePosition==PagePosition.FIRST) {
                return false;
            } else if (pagePosition==PagePosition.LAST) {
                // how the hell do you know at this point?
                log.warn("conditional-page-master-reference: page-position='last' is not yet implemented (NYI)");
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

    protected int getPagePosition() {
        return this.pagePosition;
    }

    protected int getOddOrEven() {
        return this.oddOrEven;
    }

    protected int getBlankOrNotBlank() {
        return this.blankOrNotBlank;
    }

    public String getMasterName() {
        return masterName;
    }
}
