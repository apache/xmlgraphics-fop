/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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


    private boolean startsReferenceArea;
    private boolean hasForcing = false;
    private List spaceVals = new ArrayList();


    /**
     * Creates a new SpaceSpecifier.
     * @param startsReferenceArea true if it starts a new reference area
     */
    public SpaceSpecifier(boolean startsReferenceArea) {
        this.startsReferenceArea = startsReferenceArea;
    }

    /**
     * {@inheritDoc}
     */
    public Object clone() {
        try {
            SpaceSpecifier ss = (SpaceSpecifier) super.clone();
            ss.startsReferenceArea = startsReferenceArea;
            ss.hasForcing = hasForcing;
            // Clone the vector, but share the objects in it!
            ss.spaceVals = new ArrayList();
            ss.spaceVals.addAll(spaceVals);
            return ss;
        } catch (CloneNotSupportedException cnse) {
            return null;
        }

    }

    /**
     * Clear all space specifiers
     */
    public void clear() {
        hasForcing = false;
        spaceVals.clear();
    }

    /**
     * Indicates whether any space-specifiers have been added.
     * @return true if any space-specifiers have been added.
     */
    public boolean hasSpaces() {
        return (spaceVals.size() > 0);
    }

    /**
     * Add a new space to the sequence. If this sequence starts a reference
     * area, and the added space is conditional, and there are no
     * non-conditional values in the sequence yet, then ignore it. Otherwise
     * add it to the sequence.
     */
    public void addSpace(SpaceVal moreSpace) {
        if (!startsReferenceArea
                || !moreSpace.isConditional()
                || !spaceVals.isEmpty()) {
            if (moreSpace.isForcing()) {
                if (!hasForcing) {
                    // Remove all other values (must all be non-forcing)
                    spaceVals.clear();
                    hasForcing = true;
                }
                spaceVals.add(moreSpace);
            } else if (!hasForcing) {
                // Don't bother adding all 0 space-specifier if not forcing
                if (moreSpace.getSpace().min != 0
                        || moreSpace.getSpace().opt != 0
                        || moreSpace.getSpace().max != 0) {
                    spaceVals.add(moreSpace);
                }
            }
        }
    }


    /**
     * Resolve the current sequence of space-specifiers, accounting for
     * forcing values.
     * @param endsReferenceArea True if the sequence should be resolved
     * at the trailing edge of reference area.
     * @return The resolved value as a min/opt/max triple.
     */
    public MinOptMax resolve(boolean endsReferenceArea) {
        int lastIndex = spaceVals.size();
        if (endsReferenceArea) {
            // Start from the end and count conditional specifiers
            // Stop at first non-conditional
            for (; lastIndex > 0; --lastIndex) {
                SpaceVal spaceVal = (SpaceVal) spaceVals.get(lastIndex - 1);
                if (!spaceVal.isConditional()) {
                    break;
                }
            }
        }
        MinOptMax resolvedSpace = new MinOptMax(0);
        int maxPrecedence = -1;
        for (int index = 0; index < lastIndex; index++) {
            SpaceVal spaceVal = (SpaceVal) spaceVals.get(index);
            if (hasForcing) {
                resolvedSpace.add(spaceVal.getSpace());
            } else if (spaceVal.getPrecedence() > maxPrecedence) {
                maxPrecedence = spaceVal.getPrecedence();
                resolvedSpace = spaceVal.getSpace();
            } else if (spaceVal.getPrecedence() == maxPrecedence) {
                if (spaceVal.getSpace().opt > resolvedSpace.opt) {
                    resolvedSpace = spaceVal.getSpace();
                } else if (spaceVal.getSpace().opt == resolvedSpace.opt) {
                    if (resolvedSpace.min < spaceVal.getSpace().min) {
                        resolvedSpace.min = spaceVal.getSpace().min;
                    }
                    if (resolvedSpace.max > spaceVal.getSpace().max) {
                        resolvedSpace.max = spaceVal.getSpace().max;
                    }
                }
            }

        }
        return resolvedSpace;
    }

    public String toString() {
        return "Space Specifier (resolved at begin/end of ref. area:):\n" +
            resolve(false).toString() + "\n" +
            resolve(true).toString();
    }
}
