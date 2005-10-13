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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.traits.MinOptMax;

/**
 * This class represents an unresolved list element element with a (conditional) length. This
 * is the base class for spaces, borders and paddings.
 */
public abstract class UnresolvedListElementWithLength extends UnresolvedListElement {

    /** Logger instance */
    protected static Log log = LogFactory.getLog(UnresolvedListElementWithLength.class);
    
    private MinOptMax length;
    private boolean conditional;
    private RelSide side;
    private boolean isFirst;
    private boolean isLast;
    
    /**
     * Main constructor
     * @param position the Position instance needed by the addAreas stage of the LMs.
     * @param length the length of the element
     * @param side the side to which this element applies
     * @param conditional true if it's a conditional element (conditionality=discard)
     * @param isFirst true if this is a space-before of the first area generated.
     * @param isLast true if this is a space-after of the last area generated.
     */
    public UnresolvedListElementWithLength(Position position, MinOptMax length, 
            RelSide side,
            boolean conditional, boolean isFirst, boolean isLast) {
        super(position);
        this.length = length;
        this.side = side;
        this.conditional = conditional;
        this.isFirst = isFirst;
        this.isLast = isLast;
    }
    
    /** @see org.apache.fop.layoutmgr.UnresolvedListElement#isConditional() */
    public boolean isConditional() {
        return this.conditional;
    }
    
    /** @return the space as resolved MinOptMax instance */
    public MinOptMax getLength() {
        return this.length;
    }
    
    /** @return the side this element was generated for */
    public RelSide getSide() {
        return this.side;
    }
    
    /** @return true if this is a space-before of the first area generated. */
    public boolean isFirst() {
        return this.isFirst;
    }
    
    /** @return true if this is a space-after of the last area generated. */
    public boolean isLast() {
        return this.isLast;
    }
    
    /**
     * Called to notify the affected layout manager about the effective length after resolution.
     * This method is called once before each call to the layout manager's addAreas() method.
     * @param effectiveLength the effective length after resolution (may be null which equals to
     *                        zero effective length)
     */
    public abstract void notifyLayoutManager(MinOptMax effectiveLength);
    
    /** @see java.lang.Object#toString() */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getSide().getName()).append(", ");
        sb.append(this.length.toString());
        if (isConditional()) {
            sb.append("[discard]");
        } else {
            sb.append("[RETAIN]");
        }
        if (isFirst()) {
            sb.append("[first]");
        }
        if (isLast()) {
            sb.append("[last]");
        }
        return sb.toString();
    }
    
}
