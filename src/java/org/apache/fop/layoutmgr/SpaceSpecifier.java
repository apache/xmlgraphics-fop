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
        return !spaceVals.isEmpty();
    }

    /**
     * Add a new space to the sequence. If this sequence starts a reference
     * area, and the added space is conditional, and there are no
     * non-conditional values in the sequence yet, then ignore it. Otherwise
     * add it to the sequence.
     *
     * @param space the space to add.
     */
    public void addSpace(SpaceVal space) {
        if (!startsReferenceArea || !space.isConditional() || hasSpaces()) {
            if (space.isForcing()) {
                if (!hasForcing) {
                    // Remove all other values (must all be non-forcing)
                    spaceVals.clear();
                    hasForcing = true;
                }
                spaceVals.add(space);
            } else if (!hasForcing) {
                // Don't bother adding all 0 space-specifier if not forcing
                if (space.getSpace().isNonZero()) {
                    spaceVals.add(space);
                }
            }
        }
    }


    /**
     * Resolve the current sequence of space-specifiers, accounting for forcing values.
     *
     * @param endsReferenceArea whether the sequence should be resolved at the trailing edge of
     *                          reference area.
     * @return the resolved value as a min/opt/max triple.
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
        MinOptMax resolvedSpace = MinOptMax.ZERO;
        int maxPrecedence = -1;
        for (int index = 0; index < lastIndex; index++) {
            SpaceVal spaceVal = (SpaceVal) spaceVals.get(index);
            MinOptMax space = spaceVal.getSpace();
            if (hasForcing) {
                resolvedSpace = resolvedSpace.plus(space);
            } else {
                int precedence = spaceVal.getPrecedence();
                if (precedence > maxPrecedence) {
                    maxPrecedence = precedence;
                    resolvedSpace = space;
                } else if (precedence == maxPrecedence) {
                    if (space.getOpt() > resolvedSpace.getOpt()) {
                        resolvedSpace = space;
                    } else if (space.getOpt() == resolvedSpace.getOpt()) {
                        if (resolvedSpace.getMin() < space.getMin()) {
                            resolvedSpace = MinOptMax.getInstance(space.getMin(),
                                    resolvedSpace.getOpt(), resolvedSpace.getMax());
                        }
                        if (resolvedSpace.getMax() > space.getMax()) {
                            resolvedSpace = MinOptMax.getInstance(resolvedSpace.getMin(),
                                    resolvedSpace.getOpt(), space.getMax());
                        }
                    }
                }
            }

        }
        return resolvedSpace;
    }

    public String toString() {
        return "Space Specifier (resolved at begin/end of ref. area:):\n"
                + resolve(false) + "\n" + resolve(true);
    }
}
