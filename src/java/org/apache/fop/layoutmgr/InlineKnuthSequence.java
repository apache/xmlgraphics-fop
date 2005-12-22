/*
 * Copyright 2005 The Apache Software Foundation.
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

import java.util.List;


/**
 * 
 */
public class InlineKnuthSequence extends KnuthSequence  {

    private boolean isClosed = false;

    /**
     * Creates a new and empty list. 
     */
    public InlineKnuthSequence() {
        super();
    }
    
    /**
     * Creates a new list from an existing list.
     * @param list The list from which to create the new list.
     */
    public InlineKnuthSequence(List list) {
        super(list);
    }

    /**
     * Is this an inline or a block sequence?
     * @return false
     */
    public boolean isInlineSequence() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.apache.fop.layoutmgr.KnuthSequence#canAppendSequence(org.apache.fop.layoutmgr.KnuthSequence)
     */
    public boolean canAppendSequence(KnuthSequence sequence) {
        return sequence.isInlineSequence() && !isClosed;
    }

    /* (non-Javadoc)
     * @see org.apache.fop.layoutmgr.KnuthSequence#appendSequence(org.apache.fop.layoutmgr.KnuthSequence, org.apache.fop.layoutmgr.LayoutManager)
     */
    public boolean appendSequence(KnuthSequence sequence, LayoutManager lm) {
        if (!canAppendSequence(sequence)) {
            return false;
        }
        addAll(sequence);
        return true;
    }

    /* (non-Javadoc)
     * @see org.apache.fop.layoutmgr.KnuthSequence#appendSequenceOrClose(org.apache.fop.layoutmgr.KnuthSequence, org.apache.fop.layoutmgr.LayoutManager)
     */
    public boolean appendSequenceOrClose(KnuthSequence sequence, LayoutManager lm) {
        if (!appendSequence(sequence, lm)) {
            endSequence();
            return false;
        } else {
            return true;
        }
    }

    /* (non-Javadoc)
     * @see org.apache.fop.layoutmgr.KnuthSequence#endSequence()
     */
    public KnuthSequence endSequence() {
        if (!isClosed) {
            add(new KnuthPenalty(0, -KnuthElement.INFINITE, false, null, false));
            isClosed = true;
        }
        return this;
    }

}
