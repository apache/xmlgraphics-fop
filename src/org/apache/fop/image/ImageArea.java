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
package org.apache.fop.image;

import org.apache.fop.layout.FontState;
import org.apache.fop.layout.inline.InlineArea;

import org.apache.fop.render.Renderer;

public class ImageArea extends InlineArea {

    protected int xOffset = 0;
    protected int align;
    protected int valign;
    protected FopImage image;


    public ImageArea(FontState fontState, FopImage img, int AllocationWidth,
                     int width, int height, int startIndent, int endIndent,
                     int align) {
        super(fontState, width, 0, 0, 0);
        this.currentHeight = height;
        this.contentRectangleWidth = width;
        this.height = height;
        this.image = img;
        this.align = align;

        /*
         * switch (align) {
         * case TextAlign.START:
         * xOffset = startIndent;
         * break;
         * case TextAlign.END:
         * if (endIndent == 0)
         * endIndent = AllocationWidth;
         * xOffset = (endIndent - width);
         * break;
         * case TextAlign.JUSTIFY:
         * xOffset = startIndent;
         * break;
         * case TextAlign.CENTER:
         * if (endIndent == 0)
         * endIndent = AllocationWidth;
         * xOffset = startIndent + ((endIndent - startIndent) - width)/2;
         * break;
         * }
         */
    }

    public int getXOffset() {
        return this.xOffset;
    }

    public FopImage getImage() {
        return this.image;
    }

    public void render(Renderer renderer) {
        renderer.renderImageArea(this);
    }

    public int getImageHeight() {
        return currentHeight;
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

    public void setStartIndent(int startIndent) {
        xOffset = startIndent;
    }



}


