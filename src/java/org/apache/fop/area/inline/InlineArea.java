/*
 * $Id: InlineArea.java,v 1.15 2003/03/05 16:45:43 jeremias Exp $
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
package org.apache.fop.area.inline;

import org.apache.fop.area.Area;
import org.apache.fop.area.Trait;
import org.apache.fop.traits.BorderProps;

/**
 * Inline Area
 * This area is for all inline areas that can be placed
 * in a line area.
 * Extensions of this class should render themselves with the
 * requested renderer.
 */
public class InlineArea extends Area {
    // int width;
    private int height;
    /**
     * The content ipd of this inline area
     */
    protected int contentIPD = 0;

    /**
     * offset position from top of parent area
     */
    protected int verticalPosition = 0;

    /**
     * Handle a visitor (usually a renderer) for this inline area.
     * Inline areas that extend this class are expected
     * to pass themselves back to the visitor so that the visitor can process
     * them, usually by rendering them.
     *
     * @param visitor the InlineAreaVisitor that will process this
     */
    public void acceptVisitor(InlineAreaVisitor visitor) {
    }

    /**
     * Set the width of this inline area.
     * Currently sets the ipd.
     *
     * @param w the width
     */
    public void setWidth(int w) {
        contentIPD = w;
    }

    /**
     * Get the width of this inline area.
     * Currently gets the ipd.
     *
     * @return the width
     */
    public int getWidth() {
        return contentIPD;
    }

    /**
     * Set the inline progression dimension of this inline area.
     *
     * @param ipd the inline progression dimension
     */
    public void setIPD(int ipd) {
        this.contentIPD = ipd;
    }

    /**
     * Get the inline progression dimension
     *
     * @return the inline progression dimension of this area
     */
    public int getIPD() {
        return this.contentIPD;
    }

    /**
     * Increase the inline progression dimensions of this area.
     * This is used for inline parent areas that contain mulitple child areas.
     *
     * @param ipd the inline progression to increase by
     */
    public void increaseIPD(int ipd) {
        this.contentIPD += ipd;
    }

    /**
     * Set the height of this inline area.
     *
     * @param h the height value to set
     */
    public void setHeight(int h) {
        height = h;
    }

    /**
     * Get the height of this inline area.
     *
     * @return the height of the inline area
     */
    public int getHeight() {
        return height;
    }

    /**
     * Get the allocation inline progression dimension of this area.
     * This adds the content, borders and the padding to find the
     * total allocated IPD.
     *
     * @return the total IPD allocation for this area
     */
    public int getAllocIPD() {
        // If start or end border or padding is non-zero, add to content IPD
        int iBP = contentIPD;
        Object t;
        if ((t = getTrait(Trait.PADDING_START)) != null) {
            iBP += ((Integer) t).intValue();
        }
        if ((t = getTrait(Trait.PADDING_END)) != null) {
            iBP += ((Integer) t).intValue();
        }
        if ((t = getTrait(Trait.BORDER_START)) != null) {
            iBP += ((BorderProps) t).width;
        }
        if ((t = getTrait(Trait.BORDER_END)) != null) {
            iBP += ((BorderProps) t).width;
        }
        return iBP;
    }

    /**
     * Set the offset of this inline area.
     * This is used to set the offset of the inline area
     * which is normally relative to the top of the line
     * or the baseline.
     *
     * @param v the offset
     */
    public void setOffset(int v) {
        verticalPosition = v;
    }

    /**
     * Get the offset of this inline area.
     * This returns the offset of the inline area
     * which is normally relative to the top of the line
     * or the baseline.
     *
     * @return the offset
     */
    public int getOffset() {
        return verticalPosition;
    }
}

