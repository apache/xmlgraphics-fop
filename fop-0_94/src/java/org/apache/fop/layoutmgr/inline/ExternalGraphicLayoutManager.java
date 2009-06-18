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

import java.awt.geom.Rectangle2D;
import org.apache.fop.area.Area;
import org.apache.fop.area.inline.Image;
import org.apache.fop.fo.flow.ExternalGraphic;


/**
 * LayoutManager for the fo:external-graphic formatting object
 */
public class ExternalGraphicLayoutManager extends AbstractGraphicsLayoutManager {
    
    private ExternalGraphic fobj;

    /**
     * Constructor
     *
     * @param node the fo:external-graphic formatting object that creates the area
     */
    public ExternalGraphicLayoutManager(ExternalGraphic node) {
        super(node);
        fobj = node;
    }

    /**
     * Setup this image.
     * This gets the sizes for the image and the dimensions and clipping.
     * @todo see if can simplify property handling logic
     */
    /*
    private void setup() {
        // assume lr-tb for now and just use the .optimum value of the range
        Length ipd = fobj.getInlineProgressionDimension().getOptimum(this).getLength();
        if (ipd.getEnum() != EN_AUTO) {
            viewWidth = ipd.getValue(this);
        } else {
            ipd = fobj.getWidth();
            if (ipd.getEnum() != EN_AUTO) {
                viewWidth = ipd.getValue(this);
            }
        }
        Length bpd = fobj.getBlockProgressionDimension().getOptimum(this).getLength();
        if (bpd.getEnum() != EN_AUTO) {
            viewHeight = bpd.getValue(this);
        } else {
            bpd = fobj.getHeight();
            if (bpd.getEnum() != EN_AUTO) {
                viewHeight = bpd.getValue(this);
            }
        }

        int cwidth = -1;
        int cheight = -1;
        Length ch = fobj.getContentHeight();
        if (ch.getEnum() != EN_AUTO) {
            if (ch.getEnum() == EN_SCALE_TO_FIT) {
                if (viewHeight != -1) {
                    cheight = viewHeight;
                }
            } else {
                cheight = ch.getValue(this);
            }
        }
        Length cw = fobj.getContentWidth();
        if (cw.getEnum() != EN_AUTO) {
            if (cw.getEnum() == EN_SCALE_TO_FIT) {
                if (viewWidth != -1) {
                    cwidth = viewWidth;
                }
            } else {
                cwidth = cw.getValue(this);
            }
        }

        int scaling = fobj.getScaling();
        if ((scaling == EN_UNIFORM) || (cwidth == -1) || cheight == -1) {
            if (cwidth == -1 && cheight == -1) {
                cwidth = fobj.getIntrinsicWidth();
                cheight = fobj.getIntrinsicHeight();
            } else if (cwidth == -1) {
                cwidth = (int)(fobj.getIntrinsicWidth() * (double)cheight 
                    / fobj.getIntrinsicHeight());
            } else if (cheight == -1) {
                cheight = (int)(fobj.getIntrinsicHeight() * (double)cwidth 
                    / fobj.getIntrinsicWidth());
            } else {
                // adjust the larger
                double rat1 = (double)cwidth / fobj.getIntrinsicWidth();
                double rat2 = (double)cheight / fobj.getIntrinsicHeight();
                if (rat1 < rat2) {
                    // reduce cheight
                    cheight = (int)(rat1 * fobj.getIntrinsicHeight());
                } else if (rat1 > rat2) {
                    cwidth = (int)(rat2 * fobj.getIntrinsicWidth());
                }
            }
        }

        if (viewWidth == -1) {
            viewWidth = cwidth;
        }
        if (viewHeight == -1) {
            viewHeight = cheight;
        }

        if (cwidth > viewWidth || cheight > viewHeight) {
            int overflow = fobj.getOverflow();
            if (overflow == EN_HIDDEN) {
                clip = true;
            } else if (overflow == EN_ERROR_IF_OVERFLOW) {
                fobj.getLogger().error("Image: " + fobj.getURL()
                                  + " overflows the viewport, clipping to viewport");
                clip = true;
            }
        }

        int xoffset = 0;
        int yoffset = 0;
        switch(fobj.getDisplayAlign()) {
            case EN_BEFORE:
            break;
            case EN_AFTER:
                yoffset = viewHeight - cheight;
            break;
            case EN_CENTER:
                yoffset = (viewHeight - cheight) / 2;
            break;
            case EN_AUTO:
            default:
            break;
        }

        switch(fobj.getTextAlign()) {
            case EN_CENTER:
                xoffset = (viewWidth - cwidth) / 2;
            break;
            case EN_END:
                xoffset = viewWidth - cwidth;
            break;
            case EN_START:
            break;
            case EN_JUSTIFY:
            default:
            break;
        }
        
        
        CommonBorderPaddingBackground borderProps = fobj.getCommonBorderPaddingBackground();
        
        //Determine extra BPD from borders etc.
        int beforeBPD = borderProps.getPadding(CommonBorderPaddingBackground.BEFORE, false, this);
        beforeBPD += borderProps.getBorderWidth(CommonBorderPaddingBackground.BEFORE,
                                             false);
        int afterBPD = borderProps.getPadding(CommonBorderPaddingBackground.AFTER, false, this);
        afterBPD += borderProps.getBorderWidth(CommonBorderPaddingBackground.AFTER, false);
        
        yoffset += beforeBPD;
        viewBPD = viewHeight;
        viewHeight += beforeBPD;
        viewHeight += afterBPD;
        
        //Determine extra IPD from borders etc.
        int startIPD = borderProps.getPadding(CommonBorderPaddingBackground.START,
                false, this);
        startIPD += borderProps.getBorderWidth(CommonBorderPaddingBackground.START,
                 false);
        int endIPD = borderProps.getPadding(CommonBorderPaddingBackground.END, false, this);
        endIPD += borderProps.getBorderWidth(CommonBorderPaddingBackground.END, false);
        
        xoffset += startIPD;
        viewIPD = viewWidth;
        viewWidth += startIPD;
        viewWidth += endIPD;
        
        placement = new Rectangle2D.Float(xoffset, yoffset, cwidth, cheight);
    }
    */
    
    /**
     * Get the inline area created by this element.
     *
     * @return the inline area
     */
    protected Area getChildArea() {
        return new Image(fobj.getSrc());
    }
    
}

