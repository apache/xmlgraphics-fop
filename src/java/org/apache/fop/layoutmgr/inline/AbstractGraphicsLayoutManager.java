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

package org.apache.fop.layoutmgr.inline;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.List;

import org.apache.fop.area.Area;
import org.apache.fop.area.inline.Viewport;
import org.apache.fop.datatypes.LengthBase;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.flow.AbstractGraphics;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.TraitSetter;


/**
 * LayoutManager handling the common tasks for the fo:instream-foreign-object 
 * and fo:external-graphics formatting objects
 */
public abstract class AbstractGraphicsLayoutManager extends LeafNodeLayoutManager {
    
    /** The graphics object this LM deals with */
    protected AbstractGraphics fobj;
    
    /**
     * Constructor
     * @param node the formatting object that creates this area
     */
    public AbstractGraphicsLayoutManager(AbstractGraphics node) {
        super(node);
        fobj = node;
    }

    /**
     * Get the inline area created by this element.
     *
     * @return the viewport inline area
     */
    private Viewport getInlineArea() {
        Dimension intrinsicSize = new Dimension(
                fobj.getIntrinsicWidth(),
                fobj.getIntrinsicHeight());

        //TODO Investigate if the line-height property has to be taken into the calculation
        //somehow. There was some code here that hints in this direction but it was disabled.

        ImageLayout imageLayout = new ImageLayout(fobj, this, intrinsicSize);
        Rectangle placement = imageLayout.getPlacement();

        CommonBorderPaddingBackground borderProps = fobj.getCommonBorderPaddingBackground();
        
        //Determine extra BPD from borders and padding
        int beforeBPD = borderProps.getPadding(CommonBorderPaddingBackground.BEFORE, false, this);
        beforeBPD += borderProps.getBorderWidth(CommonBorderPaddingBackground.BEFORE, false);
        
        placement.y += beforeBPD;
        
        //Determine extra IPD from borders and padding
        int startIPD = borderProps.getPadding(CommonBorderPaddingBackground.START, false, this);
        startIPD += borderProps.getBorderWidth(CommonBorderPaddingBackground.START, false);
        
        placement.x += startIPD;

        Area viewportArea = getChildArea();
        TraitSetter.setProducerID(viewportArea, fobj.getId());
        transferForeignAttributes(viewportArea);

        Viewport vp = new Viewport(viewportArea);
        TraitSetter.setProducerID(vp, fobj.getId());
        vp.setIPD(imageLayout.getViewportSize().width);
        vp.setBPD(imageLayout.getViewportSize().height);
        vp.setContentPosition(placement);
        vp.setClip(imageLayout.isClipped());
        vp.setOffset(0);

        // Common Border, Padding, and Background Properties
        TraitSetter.addBorders(vp, fobj.getCommonBorderPaddingBackground()
                                , false, false, false, false, this);
        TraitSetter.addPadding(vp, fobj.getCommonBorderPaddingBackground()
                                , false, false, false, false, this);
        TraitSetter.addBackground(vp, fobj.getCommonBorderPaddingBackground(), this);

        return vp;
    }
    
    /** {@inheritDoc} */
    public List getNextKnuthElements(LayoutContext context,
                                           int alignment) {
        Viewport areaCurrent = getInlineArea();
        setCurrentArea(areaCurrent);
        return super.getNextKnuthElements(context, alignment);
    }
    
    /** {@inheritDoc} */
    protected AlignmentContext makeAlignmentContext(LayoutContext context) {
        return new AlignmentContext(
                get(context).getAllocBPD()
                , fobj.getAlignmentAdjust()
                , fobj.getAlignmentBaseline()
                , fobj.getBaselineShift()
                , fobj.getDominantBaseline()
                , context.getAlignmentContext()
            );
    }

    /**
     * Returns the image of foreign object area to be put into
     * the viewport.
     * @return the appropriate area
     */
    abstract Area getChildArea();
    
    // --------- Property Resolution related functions --------- //
    
    /**
     * {@inheritDoc} 
     */
    public int getBaseLength(int lengthBase, FObj fobj) {
        switch (lengthBase) {
        case LengthBase.IMAGE_INTRINSIC_WIDTH:
            return getIntrinsicWidth();
        case LengthBase.IMAGE_INTRINSIC_HEIGHT:
            return getIntrinsicHeight();
        case LengthBase.ALIGNMENT_ADJUST:
            return get(null).getBPD();
        default: // Delegate to super class
            return super.getBaseLength(lengthBase, fobj);
        }
    }

    /**
     * Returns the intrinsic width of the e-g.
     * @return the width of the element
     */
    protected int getIntrinsicWidth() {
        return fobj.getIntrinsicWidth();
    }

    /**
     * Returns the intrinsic height of the e-g.
     * @return the height of the element
     */
    protected int getIntrinsicHeight() {
        return fobj.getIntrinsicHeight();
    }

}

