/*
 * $Id: ConditionalPageMasterReference.java,v 1.12 2003/03/06 13:42:42 jeremias Exp $
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

// XML
import org.xml.sax.Attributes;

// FOP
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FOTreeVisitor;
import org.apache.fop.fo.properties.BlankOrNotBlank;
import org.apache.fop.fo.properties.OddOrEven;
import org.apache.fop.fo.properties.PagePosition;
import org.apache.fop.apps.FOPException;

/**
 * A conditional-page-master-reference formatting object.
 * This is a reference to a page master with a set of conditions.
 * The conditions must be satisfied for the referenced master to
 * be used.
 * This element is must be the child of a repeatable-page-master-alternatives
 * element.
 */
public class ConditionalPageMasterReference extends FObj {

    private RepeatablePageMasterAlternatives repeatablePageMasterAlternatives;

    private String masterName;

    private int pagePosition;
    private int oddOrEven;
    private int blankOrNotBlank;

    /**
     * @see org.apache.fop.fo.FONode#FONode(FONode)
     */
    public ConditionalPageMasterReference(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FONode#handleAttrs(Attributes)
     */
    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);
        if (getProperty("master-reference") != null) {
            setMasterName(getProperty("master-reference").getString());
        }

        validateParent(parent);

        this.pagePosition = this.propertyList.get("page-position").getEnum();
        this.oddOrEven = this.propertyList.get("odd-or-even").getEnum();
        this.blankOrNotBlank = this.propertyList.get("blank-or-not-blank").getEnum();
    }

    /**
     * Sets the master name.
     * @param masterName name for the master
     */
    protected void setMasterName(String masterName) {
        this.masterName = masterName;
    }

    /**
     * Returns the "master-name" attribute of this page master reference
     * @return the master name
     */
    public String getMasterName() {
        return masterName;
    }

    /**
     * Check if the conditions for this reference are met.
     * checks the page number and emptyness to determine if this
     * matches.
     * @param isOddPage True if page number odd
     * @param isFirstPage True if page is first page
     * @param isBlankPage True if page is blank
     * @return True if the conditions for this reference are met
     */
    protected boolean isValid(boolean isOddPage,
                              boolean isFirstPage,
                              boolean isBlankPage) {
        // page-position
        if (isFirstPage) {
            if (pagePosition == PagePosition.REST) {
                return false;
            } else if (pagePosition == PagePosition.LAST) {
                // how the hell do you know at this point?
                getLogger().debug("LAST PagePosition NYI");
                return false;
            }
        } else {
            if (pagePosition == PagePosition.FIRST) {
                return false;
            } else if (pagePosition == PagePosition.LAST) {
                // how the hell do you know at this point?
                getLogger().debug("LAST PagePosition NYI");
                // potentially valid, don't return
            }
        }

        // odd-or-even
        if (isOddPage) {
            if (oddOrEven == OddOrEven.EVEN) {
              return false;
            }
        } else {
            if (oddOrEven == OddOrEven.ODD) {
              return false;
            }
        }

        // blank-or-not-blank
        if (isBlankPage) {
            if (blankOrNotBlank == BlankOrNotBlank.NOT_BLANK) {
                return false;
            }
        } else {
            if (blankOrNotBlank == BlankOrNotBlank.BLANK) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check that the parent is the right type of formatting object
     * repeatable-page-master-alternatives.
     * @param parent parent node
     * @throws FOPException If the parent is invalid
     */
    protected void validateParent(FONode parent) throws FOPException {
        if (parent.getName().equals("fo:repeatable-page-master-alternatives")) {
            this.repeatablePageMasterAlternatives =
                (RepeatablePageMasterAlternatives)parent;

            if (getMasterName() == null) {
                getLogger().warn("single-page-master-reference"
                                       + "does not have a master-name and so is being ignored");
            } else {
                this.repeatablePageMasterAlternatives.addConditionalPageMasterReference(this);
            }
        } else {
            throw new FOPException("fo:conditional-page-master-reference must be child "
                                   + "of fo:repeatable-page-master-alternatives, not "
                                   + parent.getName());
        }
    }

    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.serveConditionalPageMasterReference(this);
    }

}
