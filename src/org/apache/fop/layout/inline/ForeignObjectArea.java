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
package org.apache.fop.layout.inline;

// FOP
import org.apache.fop.render.Renderer;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.FontState;

public class ForeignObjectArea extends InlineArea {

    protected int xOffset = 0;
    /* text-align of contents */
    protected int align;
    /* vertical align of contents */
    protected int valign;
    /* scaling method */
    protected int scaling;
    protected Area foreignObject;
    /* height according to the instream-foreign-object */
    protected int cheight;
    /* width according to the instream-foreign-object */
    protected int cwidth;
    /* width of the content */
    protected int awidth;
    /* height of the content */
    protected int aheight;
    /* width */
    protected int width;
    boolean wauto;
    boolean hauto;
    boolean cwauto;
    boolean chauto;
    int overflow;

    public ForeignObjectArea(FontState fontState, int width) {
        super(fontState, width, 0, 0, 0);
    }

    public void render(Renderer renderer) {
        if (foreignObject != null)
            renderer.renderForeignObjectArea(this);
    }

    /**
     * This is NOT the content width of the instream-foreign-object.
     * This is the content width for a Box.
     */
    public int getContentWidth() {
        return getEffectiveWidth();
    }

    /**
     * This is NOT the content height of the instream-foreign-object.
     * This is the content height for a Box.
     */
    public int getHeight() {
        return getEffectiveHeight();
    }

    public int getContentHeight() {
        return getEffectiveHeight();
    }

    public int getXOffset() {
        return this.xOffset;
    }

    public void setStartIndent(int startIndent) {
        xOffset = startIndent;
    }

    public void setObject(Area fobject) {
        foreignObject = fobject;
    }

    public Area getObject() {
        return foreignObject;
    }

    public void setSizeAuto(boolean wa, boolean ha) {
        wauto = wa;
        hauto = ha;
    }

    public void setContentSizeAuto(boolean wa, boolean ha) {
        cwauto = wa;
        chauto = ha;
    }

    public boolean isContentWidthAuto() {
        return cwauto;
    }

    public boolean isContentHeightAuto() {
        return chauto;
    }

    public void setAlign(int align) {
        this.align = align;
    }

    public int getAlign() {
        return this.align;
    }

    public void setVerticalAlign(int align) {
        this.valign = align;
    }

    public int getVerticalAlign() {
        return this.valign;
    }

    public void setOverflow(int o) {
        this.overflow = o;
    }

    public int getOverflow() {
        return this.overflow;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setContentHeight(int cheight) {
        this.cheight = cheight;
    }

    public void setContentWidth(int cwidth) {
        this.cwidth = cwidth;
    }

    public void setScaling(int scaling) {
        this.scaling = scaling;
    }

    public int scalingMethod() {
        return this.scaling;
    }

    public void setIntrinsicWidth(int w) {
        awidth = w;
    }

    public void setIntrinsicHeight(int h) {
        aheight = h;
    }

    public int getIntrinsicHeight() {
        return aheight;
    }

    public int getIntrinsicWidth() {
        return awidth;
    }

    public int getEffectiveHeight() {
        if (this.hauto) {
            if (this.chauto) {
                return aheight;
            } else {
                // need to handle percentages, this would be a scaling factor on the
                // instrinsic height (content determined height)
                // if(this.properties.get("content-height").getLength().isPercentage()) {
                // switch(scaling) {
                // case Scaling.UNIFORM:
                // break;
                // case Scaling.NON_UNIFORM:
                // break;
                // }
                // } else {
                return this.cheight;
            }
        } else {
            return this.height;
        }
    }

    public int getEffectiveWidth() {
        if (this.wauto) {
            if (this.cwauto) {
                return awidth;
            } else {
                // need to handle percentages, this would be a scaling factor on the
                // instrinsic height (content determined height)
                // if(this.properties.get("content-width").getLength().isPercentage()) {
                // switch(scaling) {
                // case Scaling.UNIFORM:
                // break;
                // case Scaling.NON_UNIFORM:
                // break;
                // }
                // } else {
                return this.cwidth;
            }
        } else {
            return this.width;
        }
    }

}
