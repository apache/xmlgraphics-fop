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
package org.apache.fop.area;

import java.util.ArrayList;

// block areas hold either more block areas or line
// areas can also be used as a block spacer
// a block area may have children positioned by stacking
// or by relative to the parent for floats, tables and lists
// cacheable object
// has id information

/**
 * This is the block area class.
 * It holds child block areas such as other blocks or lines.
 */
public class Block extends BlockParent {
    /**
     * Normally stacked with other blocks.
     */
    public static final int STACK = 0;

    /**
     * Placed relative to the flow position.
     * This effects the flow placement of stacking normally.
     */
    public static final int RELATIVE = 1;

    /**
     * Relative to the block parent but not effecting the stacking
     * Used for block-container, tables and lists.
     */
    public static final int ABSOLUTE = 2;

    private int stacking = TB;
    private int positioning = STACK;

    // a block with may contain the dominant styling info in
    // terms of most lines or blocks with info

    /**
     * Add the block to this block area.
     *
     * @param block the block area to add
     */
    public void addBlock(Block block) {
        if (children == null) {
            children = new ArrayList();
        }
        height += block.getHeight();
        children.add(block);
    }

    /**
     * Add the line area to this block area.
     *
     * @param line the line area to add
     */
    public void addLineArea(LineArea line) {
        if (children == null) {
            children = new ArrayList();
        }
        height += line.getHeight();
        children.add(line);
    }

    /**
     * Set the positioning of this area.
     *
     * @param pos the positioning to use when rendering this area
     */
    public void setPositioning(int pos) {
        positioning = pos;
    }

    /**
     * Get the positioning of this area.
     *
     * @return the positioning to use when rendering this area
     */
    public int getPositioning() {
        return positioning;
    }

}

