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

import java.util.List;
import java.util.Stack;

/**
 * Represents a list of block level Knuth elements.
 */
public class BlockKnuthSequence extends KnuthSequence {
    
    private boolean isClosed = false;
    
    private Stack subSequences;
    
    /**
     * Creates a new and empty list.
     */
    public BlockKnuthSequence() {
        super();
    }
    
    /**
     * Creates a new list from an existing list.
     * @param list The list from which to create the new list.
     */
    public BlockKnuthSequence(List list) {
        super(list);
    }

    /** {@inheritDoc} */
    public boolean isInlineSequence() {
        return false;
    }

    /** {@inheritDoc} */
    public boolean canAppendSequence(KnuthSequence sequence) {
        return !sequence.isInlineSequence() && !isClosed;
    }

    /** {@inheritDoc} */
    public boolean appendSequence(KnuthSequence sequence) {
        // log.debug("Cannot append a sequence without a BreakElement");
        return false;
    }
    
    /** {@inheritDoc} */
    public boolean appendSequence(KnuthSequence sequence, boolean keepTogether,
                                  BreakElement breakElement) {
        if (!canAppendSequence(sequence)) {
            return false;
        }
        if (keepTogether) {
            breakElement.setPenaltyValue(KnuthElement.INFINITE);
            add(breakElement);
        } else if (!((ListElement) getLast()).isGlue()) {
            breakElement.setPenaltyValue(0);
            add(breakElement);
        }
        addAll(sequence);
        return true;
    }

    /** {@inheritDoc} */
    public KnuthSequence endSequence() {
        isClosed = true;
        return this;
    }

    public class SubSequence {
        private KnuthBox firstBox = null;
        private int widowRowLimit = 0;
        private SubSequence(KnuthBox firstBox) {
            this.firstBox = firstBox;
        }
        private SubSequence(KnuthBox firstBox, int widowRowLimit) {
            this.firstBox = firstBox;
            this.widowRowLimit = widowRowLimit;
        }
        
        /**
         * @return the firstBox
         */
        public KnuthBox getFirstBox() {
            return firstBox;
        }
        
        /**
         * @return the widowRowLimit
         */
        public int getWidowRowLimit() {
            return widowRowLimit;
        }

    }
    
    public void addSubSequence(KnuthBox firstBox) {
        if (subSequences == null) {
            subSequences = new Stack();
        }
        subSequences.push(new SubSequence(firstBox));
    }

    public void addSubSequence(KnuthBox firstBox, int orphanRowLimit) {
        if (subSequences == null) {
            subSequences = new Stack();
        }
        subSequences.push(new SubSequence(firstBox, orphanRowLimit));
    }
    
    public boolean hasSubSequence() {
        return !(subSequences == null || subSequences.isEmpty());
    }
    
    public SubSequence getSubSequence() {
        return (SubSequence) subSequences.peek();
    }

    public SubSequence removeSubSequence() {
        return (SubSequence) subSequences.pop();
    }
    
}
