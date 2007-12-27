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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.GraphicsProperties;

public class ImageLayout implements Constants {
    
    /** logging instance */
    protected static Log log = LogFactory.getLog(ImageLayout.class);
    
    //Input
    private GraphicsProperties props;
    private PercentBaseContext percentBaseContext;
    private Dimension intrinsicSize;

    //Output
    private Rectangle placement;
    private Dimension viewportSize = new Dimension(-1, -1);
    private boolean clip;
    
    public ImageLayout(GraphicsProperties props, PercentBaseContext percentBaseContext,
            Dimension intrinsicSize) {
        this.props = props;
        this.percentBaseContext = percentBaseContext;
        this.intrinsicSize = intrinsicSize;
        
        doLayout();
    }

    protected void doLayout() {
        Length len;

        int bpd = -1;
        int ipd = -1;
        
        len = props.getBlockProgressionDimension().getOptimum(percentBaseContext).getLength();
        if (len.getEnum() != EN_AUTO) {
            bpd = len.getValue(percentBaseContext);
        } else {
            len = props.getHeight();
            if (len.getEnum() != EN_AUTO) {
                bpd = len.getValue(percentBaseContext);
            }
        }

        len = props.getInlineProgressionDimension().getOptimum(percentBaseContext).getLength();
        if (len.getEnum() != EN_AUTO) {
            ipd = len.getValue(percentBaseContext);
        } else {
            len = props.getWidth();
            if (len.getEnum() != EN_AUTO) {
                ipd = len.getValue(percentBaseContext);
            }
        }

        // if auto then use the intrinsic size of the content scaled
        // to the content-height and content-width
        int cwidth = -1;
        int cheight = -1;
        len = props.getContentWidth();
        if (len.getEnum() != EN_AUTO) {
            switch (len.getEnum()) {
            case EN_SCALE_TO_FIT:
                if (ipd != -1) {
                    cwidth = ipd;
                }
                break;
            case EN_SCALE_DOWN_TO_FIT:
                if (ipd != -1 && intrinsicSize.width > ipd) {
                    cwidth = ipd;
                }
                break;
            case EN_SCALE_UP_TO_FIT:
                if (ipd != -1 && intrinsicSize.width < ipd) {
                    cwidth = ipd;
                }
                break;
            default:
                cwidth = len.getValue(percentBaseContext);
            }
        }
        len = props.getContentHeight();
        if (len.getEnum() != EN_AUTO) {
            switch (len.getEnum()) {
            case EN_SCALE_TO_FIT:
                if (bpd != -1) {
                    cheight = bpd;
                }
                break;
            case EN_SCALE_DOWN_TO_FIT:
                if (bpd != -1 && intrinsicSize.height > bpd) {
                    cheight = bpd;
                }
                break;
            case EN_SCALE_UP_TO_FIT:
                if (bpd != -1 && intrinsicSize.height < bpd) {
                    cheight = bpd;
                }
                break;
            default:
                cheight = len.getValue(percentBaseContext);
            }
        }

        int scaling = props.getScaling();
        if ((scaling == EN_UNIFORM) || (cwidth == -1) || cheight == -1) {
            if (cwidth == -1 && cheight == -1) {
                cwidth = intrinsicSize.width;
                cheight = intrinsicSize.height;
            } else if (cwidth == -1) {
                if (intrinsicSize.height == 0) {
                    cwidth = 0;
                } else {
                    cwidth = (int)(intrinsicSize.width * (double)cheight 
                            / intrinsicSize.height);
                }
            } else if (cheight == -1) {
                if (intrinsicSize.width == 0) {
                    cheight = 0;
                } else {
                    cheight = (int)(intrinsicSize.height * (double)cwidth 
                            / intrinsicSize.width);
                }
            } else {
                // adjust the larger
                if (intrinsicSize.width == 0 || intrinsicSize.height == 0) {
                    cwidth = 0;
                    cheight = 0;
                } else {
                    double rat1 = (double) cwidth / intrinsicSize.width;
                    double rat2 = (double) cheight / intrinsicSize.height;
                    if (rat1 < rat2) {
                        // reduce cheight
                        cheight = (int)(rat1 * intrinsicSize.height);
                    } else if (rat1 > rat2) {
                        cwidth = (int)(rat2 * intrinsicSize.width);
                    }
                }
            }
        }

        if (ipd == -1) {
            ipd = cwidth;
        }
        if (bpd == -1) {
            bpd = cheight;
        }

        this.clip = false;
        if (cwidth > ipd || cheight > bpd) {
            int overflow = props.getOverflow();
            if (overflow == EN_HIDDEN) {
                this.clip = true;
            } else if (overflow == EN_ERROR_IF_OVERFLOW) {
                //TODO Don't use logging to report error!
                log.error("Object overflows the viewport: clipping");
                this.clip = true;
            }
        }

        int xoffset = computeXOffset(ipd, cwidth);
        int yoffset = computeYOffset(bpd, cheight);

        //Build calculation results
        this.viewportSize.setSize(ipd, bpd);
        this.placement = new Rectangle(xoffset, yoffset, cwidth, cheight);
    }
    
    /**
     * Given the ipd and the content width calculates the
     * required x offset based on the text-align property
     * @param ipd the inline-progression-dimension of the object
     * @param cwidth the calculated content width of the object
     * @return the X offset
     */
    public int computeXOffset (int ipd, int cwidth) {
        int xoffset = 0;
        switch (props.getTextAlign()) {
            case EN_CENTER:
                xoffset = (ipd - cwidth) / 2;
                break;
            case EN_END:
                xoffset = ipd - cwidth;
                break;
            case EN_START:
                break;
            case EN_JUSTIFY:
            default:
                break;
        }
        return xoffset;
    }

    /**
     * Given the bpd and the content height calculates the
     * required y offset based on the display-align property
     * @param bpd the block-progression-dimension of the object
     * @param cheight the calculated content height of the object
     * @return the Y offset
     */
    public int computeYOffset(int bpd, int cheight) {
        int yoffset = 0;
        switch (props.getDisplayAlign()) {
            case EN_BEFORE:
                break;
            case EN_AFTER:
                yoffset = bpd - cheight;
                break;
            case EN_CENTER:
                yoffset = (bpd - cheight) / 2;
                break;
            case EN_AUTO:
            default:
                break;
        }
        return yoffset;
    }

    public Rectangle getPlacement() {
        return this.placement;
    }
    
    public Dimension getViewportSize() {
        return this.viewportSize;
    }
    
    public Dimension getIntrinsicSize() {
        return this.intrinsicSize;
    }
    
    public boolean isClipped() {
        return this.clip;
    }
    
}