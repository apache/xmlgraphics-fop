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

import org.apache.fop.area.inline.InlineArea;

import java.util.ArrayList;
import java.util.List;

/**
 * The line area.
 * This is a line area that contains inline areas.
 */
public class LineArea extends Area {
    private int stacking = LR;
    // contains inline areas
    // has start indent and length, dominant baseline, height
    private int startIndent;
    private int length;

    private int lineHeight;
    // this is the offset for the dominant baseline
    private int baseLine;

    // this class can contain the dominant char styling info
    // this means that many renderers can optimise a bit

    private List inlineAreas = new ArrayList();

    /**
     * Set the height of this line area.
     *
     * @param height the height of the line area
     */
    public void setHeight(int height) {
        lineHeight = height;
    }

    /**
     * Get the height of this line area.
     *
     * @return the height of the line area
     */
    public int getHeight() {
        return lineHeight;
    }

    /**
     * Add a child area to this line area.
     *
     * @param childArea the inline child area to add
     */
    public void addChild(Area childArea) {
        if (childArea instanceof InlineArea) {
            addInlineArea((InlineArea)childArea);
        }
    }

    /**
     * Add an inline child area to this line area.
     *
     * @param area the inline child area to add
     */
    public void addInlineArea(InlineArea area) {
        inlineAreas.add(area);
    }

    /**
     * Get the inline child areas of this line area.
     *
     * @return the list of inline areas
     */
    public List getInlineAreas() {
        return inlineAreas;
    }

    /**
     * Set the start indent of this line area.
     * The start indent is used for offsetting the start of
     * the inline areas for alignment or other indents.
     *
     * @param si the start indent value
     */
    public void setStartIndent(int si) {
        startIndent = si;
    }

    /**
     * Get the start indent of this line area.
     * The start indent is used for offsetting the start of
     * the inline areas for alignment or other indents.
     *
     * @return the start indent value
     */
    public int getStartIndent() {
        return startIndent;
    }
}

