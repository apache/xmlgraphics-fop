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
package org.apache.fop.layout;

// FOP
import org.apache.fop.render.Renderer;

public class AreaContainer extends Area {

    private int xPosition;    // should be able to take value 'left' and 'right' too
    private int yPosition;    // should be able to take value 'top' and 'bottom' too
    private int position;
    private boolean isRegionArea = false;

    // use this for identifying the general usage of the area,
    // like 'main-reference-area' or 'region-before'
    private String areaName;

    public AreaContainer(FontState fontState, int xPosition, int yPosition,
                         int allocationWidth, int maxHeight, int position) {
        super(fontState, allocationWidth, maxHeight);
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.position = position;
        // setIsReferenceArea(true); // Should always be true!
    }

    public AreaContainer(FontState fontState, int xPosition, int yPosition,
                         int allocationWidth, int maxHeight, int position,
                         boolean isRegionArea) {
        this(fontState, xPosition, yPosition, allocationWidth, maxHeight,
           position);
        this.isRegionArea = isRegionArea;
    }

    public void render(Renderer renderer) {
        if (isRegionArea)
            renderer.renderRegionAreaContainer(this);
        else
            renderer.renderAreaContainer(this);
    }

    public int getPosition() {
        return position;
    }

    public int getXPosition() {
        // return xPosition + getPaddingLeft() + getBorderLeftWidth();
        return xPosition;
    }

    public void setXPosition(int value) {
        xPosition = value;
    }

    public int getYPosition() {
        // return yPosition + getPaddingTop() + getBorderTopWidth();
        return yPosition;
    }

    public int getCurrentYPosition() {
        return yPosition;
    }

    public void setYPosition(int value) {
        yPosition = value;
    }

    public void shiftYPosition(int value) {
        yPosition += value;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

}
