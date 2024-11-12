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

package org.apache.fop.fo.flow;

import java.awt.Color;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObjMixed;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAccessibilityHolder;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonFont;
import org.apache.fop.fo.properties.CommonMarginInline;
import org.apache.fop.fo.properties.KeepProperty;
import org.apache.fop.fo.properties.SpaceProperty;

/**
 * Class modelling the commonalities of several inline-level
 * formatting objects.
 */
public abstract class InlineLevel extends FObjMixed implements CommonAccessibilityHolder {

    // The value of FO traits (refined properties) that apply to inline level FOs.
    private CommonAccessibility commonAccessibility;
    private CommonBorderPaddingBackground commonBorderPaddingBackground;
    private CommonMarginInline commonMarginInline;
    private CommonFont commonFont;
    private Color color;
    private KeepProperty keepWithNext;
    private KeepProperty keepWithPrevious;
    private SpaceProperty lineHeight;
    // End of trait values

    /**
     * Base constructor
     *
     * @param parent {@link FONode} that is the parent of this object
     */
    protected InlineLevel(FONode parent) {
        super(parent);
    }

    /** {@inheritDoc} */
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);
        commonAccessibility = CommonAccessibility.getInstance(pList);
        commonBorderPaddingBackground = pList.getBorderPaddingBackgroundProps();
        commonMarginInline = pList.getMarginInlineProps();
        commonFont = pList.getFontProps();
        color = pList.get(PR_COLOR).getColor(getUserAgent());
        keepWithNext = pList.get(PR_KEEP_WITH_NEXT).getKeep();
        keepWithPrevious = pList.get(PR_KEEP_WITH_PREVIOUS).getKeep();
        lineHeight = pList.get(PR_LINE_HEIGHT).getSpace();
    }

    /** {@inheritDoc} */
    public CommonAccessibility getCommonAccessibility() {
        return commonAccessibility;
    }

    /** @return the {@link CommonMarginInline} */
    public CommonMarginInline getCommonMarginInline() {
        return commonMarginInline;
    }

    /** @return the {@link CommonBorderPaddingBackground} */
    public CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
        return commonBorderPaddingBackground;
    }

    /** @return the {@link CommonFont} */
    public CommonFont getCommonFont() {
        return commonFont;
    }

    /**
     * Set the Common Font Property
     *
     * @param font
     */
    public void setCommonFont(CommonFont font) {
        commonFont = font;
    }

    /** @return the "color" trait */
    public Color getColor() {
        return color;
    }

    /** @return the "line-height" trait */
    public SpaceProperty getLineHeight() {
        return lineHeight;
    }

    /** @return the "keep-with-next" trait */
    public KeepProperty getKeepWithNext() {
        return keepWithNext;
    }

    /** @return the "keep-with-previous" trait */
    public KeepProperty getKeepWithPrevious() {
        return keepWithPrevious;
    }

    @Override
    public boolean isDelimitedTextRangeBoundary(int boundary) {
        return false;
    }

}
