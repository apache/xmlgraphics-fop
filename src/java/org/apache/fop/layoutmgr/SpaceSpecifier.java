/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

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
