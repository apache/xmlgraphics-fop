/*
 * $Id: BlockParent.java,v 1.7 2003/03/05 15:19:31 jeremias Exp $
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
import java.util.List;

/**
 * A BlockParent holds block-level areas.
 */
public class BlockParent extends Area {

    // this position is used for absolute position
    // or as an indent
    // this has the size in the block progression dimension

    /**
     * The x offset position of this block parent.
     * Used for relative and absolute positioning.
     */
    protected int xOffset = 0;

    /**
     * The y offset position of this block parent.
     * Used for relative and absolute positioning.
     */
    protected int yOffset = 0;

    /**
     * The width of this block parent.
     */
    protected int width = 0;

    /**
     * The height of this block parent.
     */
    protected int height = 0;

    /**
     * The children of this block parent area.
     */
    protected List children = null;

    // orientation if reference area
    private int orientation = ORIENT_0;

    /**
     * Add the block area to this block parent.
     *
     * @param block the child block area to add
     */
    public void addBlock(Block block) {
        if (children == null) {
            children = new ArrayList();
        }
        children.add(block);
    }

    /**
     * Get the list of child areas for this block area.
     *
     * @return the list of child areas
     */
    public List getChildAreas() {
        return children;
    }

    /**
     * Set the X offset of this block parent area.
     *
     * @param off the x offset of the block parent area
     */
    public void setXOffset(int off) {
        xOffset = off;
    }

    /**
     * Set the Y offset of this block parent area.
     *
     * @param off the y offset of the block parent area
     */
    public void setYOffset(int off) {
        yOffset = off;
    }

    /**
     * Set the width of this block parent area.
     *
     * @param w the width of the area
     */
    public void setWidth(int w) {
        width = w;
    }

    /**
     * Set the height of this block parent area.
     *
     * @param h the height of the block parent area
     */
    public void setHeight(int h) {
        height = h;
    }

    /**
     * Get the X offset of this block parent area.
     *
     * @return the x offset of the block parent area
     */
    public int getXOffset() {
        return xOffset;
    }

    /**
     * Get the Y offset of this block parent area.
     *
     * @return the y offset of the block parent area
     */
    public int getYOffset() {
        return yOffset;
    }

    /**
     * Get the width of this block parent area.
     *
     * @return the width of the area
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get the height of this block parent area.
     *
     * @return the height of the block parent area
     */
    public int getHeight() {
        return height;
    }

}
