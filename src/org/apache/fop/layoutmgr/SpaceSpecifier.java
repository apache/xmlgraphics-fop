/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr;

import org.apache.fop.area.MinOptMax;
import org.apache.fop.traits.SpaceVal;
import java.util.ArrayList;
import java.util.List;

/**
 * Accumulate a sequence of space-specifiers (XSL space type) on
 * areas with a stacking constraint. Provide a way to resolve these into
 * a single MinOptMax value.
 */
public class SpaceSpecifier implements Cloneable {


    private boolean bStartsRefArea;
    private boolean bHasForcing = false;
    private List vecSpaceVals = new ArrayList();


    public SpaceSpecifier(boolean bStarts) {
        bStartsRefArea = bStarts;
    }

    public Object clone() {
        try {
            SpaceSpecifier ss = (SpaceSpecifier) super.clone();
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


    /** Return true if any space-specifiers have been added. */
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
        if (!bStartsRefArea || !moreSpace.bConditional ||
                !vecSpaceVals.isEmpty()) {
            if (moreSpace.bForcing) {
                if (bHasForcing == false) {
                    // Remove all other values (must all be non-forcing)
                    vecSpaceVals.clear();
                    bHasForcing = true;
                }
                vecSpaceVals.add(moreSpace);
            } else if (bHasForcing == false) {
                // Don't bother adding all 0 space-specifier if not forcing
                if (moreSpace.space.min != 0 || moreSpace.space.opt != 0 ||
                        moreSpace.space.max != 0) {
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
                if (!sval.bConditional) {
                    break;
                }
            }
        }
        MinOptMax resSpace = new MinOptMax(0);
        int iMaxPrec = -1;
        for (int index = 0; index < lastIndex; index++) {
            SpaceVal sval = (SpaceVal) vecSpaceVals.get(index);
            if (bHasForcing) {
                resSpace.add(sval.space);
            } else if (sval.iPrecedence > iMaxPrec) {
                iMaxPrec = sval.iPrecedence;
                resSpace = sval.space;
            } else if (sval.iPrecedence == iMaxPrec) {
                if (sval.space.opt > resSpace.opt) {
                    resSpace = sval.space;
                } else if (sval.space.opt == resSpace.opt) {
                    if (resSpace.min < sval.space.min) {
                        resSpace.min = sval.space.min;
                    }
                    if (resSpace.max > sval.space.max) {
                        resSpace.max = sval.space.max;
                    }
                }
            }

        }
        return resSpace;
    }
}
