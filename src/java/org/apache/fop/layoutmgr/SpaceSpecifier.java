/*
 * $Id: SpaceSpecifier.java,v 1.11 2003/03/07 07:58:51 jeremias Exp $
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
package org.apache.fop.layoutmgr;

import org.apache.fop.traits.SpaceVal;
import java.util.ArrayList;
import java.util.List;
import org.apache.fop.traits.MinOptMax;

/**
 * Accumulate a sequence of space-specifiers (XSL space type) on
 * areas with a stacking constraint. Provide a way to resolve these into
 * a single MinOptMax value.
 */
public class SpaceSpecifier implements Cloneable {


    private boolean bStartsReferenceArea;
    private boolean bHasForcing = false;
    private List vecSpaceVals = new java.util.ArrayList();


    /**
     * Creates a new SpaceSpecifier.
     * @param bStarts true if it starts a new reference area
     */
    public SpaceSpecifier(boolean bStarts) {
        bStartsReferenceArea = bStarts;
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        try {
            SpaceSpecifier ss = (SpaceSpecifier) super.clone();
            ss.bStartsReferenceArea = this.bStartsReferenceArea;
            ss.bHasForcing = this.bHasForcing;            
            // Clone the vector, but share the objects in it!
            ss.vecSpaceVals = new ArrayList();
            ss.vecSpaceVals.addAll(this.vecSpaceVals);
            return ss;
        } catch (CloneNotSupportedException cnse) {
            return null;
        }

    }

    /**
     * Clear all space specifiers
     */
    public void clear() {
        bHasForcing = false;
        vecSpaceVals.clear();
    }

    /**
     * Indicates whether any space-specifiers have been added.
     * @return true if any space-specifiers have been added.
     */
    public boolean hasSpaces() {
        return (vecSpaceVals.size() > 0);
    }

    /**
     * Add a new space to the sequence. If this sequence starts a reference
     * area, and the added space is conditional, and there are no
     * non-conditional values in the sequence yet, then ignore it. Otherwise
     * add it to the sequence.
     */
    public void addSpace(SpaceVal moreSpace) {
        if (!bStartsReferenceArea
                || !moreSpace.isConditional()
                || !vecSpaceVals.isEmpty()) {
            if (moreSpace.isForcing()) {
                if (bHasForcing == false) {
                    // Remove all other values (must all be non-forcing)
                    vecSpaceVals.clear();
                    bHasForcing = true;
                }
                vecSpaceVals.add(moreSpace);
            } else if (bHasForcing == false) {
                // Don't bother adding all 0 space-specifier if not forcing
                if (moreSpace.getSpace().min != 0
                        || moreSpace.getSpace().opt != 0
                        || moreSpace.getSpace().max != 0) {
                    vecSpaceVals.add(moreSpace);
                }
            }
        }
    }


    /**
     * Resolve the current sequence of space-specifiers, accounting for
     * forcing values.
     * @param bEndsReferenceArea True if the sequence should be resolved
     * at the trailing edge of reference area.
     * @return The resolved value as a min/opt/max triple.
     */
    public MinOptMax resolve(boolean bEndsReferenceArea) {
        int lastIndex = vecSpaceVals.size();
        if (bEndsReferenceArea) {
            // Start from the end and count conditional specifiers
            // Stop at first non-conditional
            for (; lastIndex > 0; --lastIndex) {
                SpaceVal sval = (SpaceVal) vecSpaceVals.get(
                                  lastIndex - 1);
                if (!sval.isConditional()) {
                    break;
                }
            }
        }
        MinOptMax resSpace = new MinOptMax(0);
        int iMaxPrec = -1;
        for (int index = 0; index < lastIndex; index++) {
            SpaceVal sval = (SpaceVal) vecSpaceVals.get(index);
            if (bHasForcing) {
                resSpace.add(sval.getSpace());
            } else if (sval.getPrecedence() > iMaxPrec) {
                iMaxPrec = sval.getPrecedence();
                resSpace = sval.getSpace();
            } else if (sval.getPrecedence() == iMaxPrec) {
                if (sval.getSpace().opt > resSpace.opt) {
                    resSpace = sval.getSpace();
                } else if (sval.getSpace().opt == resSpace.opt) {
                    if (resSpace.min < sval.getSpace().min) {
                        resSpace.min = sval.getSpace().min;
                    }
                    if (resSpace.max > sval.getSpace().max) {
                        resSpace.max = sval.getSpace().max;
                    }
                }
            }

        }
        return resSpace;
    }
    
    public String toString() {
        return "Space Specifier (resolved at begin/end of ref. area:):\n" +
            resolve(false).toString() + "\n" +
            resolve(true).toString();
    }
}
