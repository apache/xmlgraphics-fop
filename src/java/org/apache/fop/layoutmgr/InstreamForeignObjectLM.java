/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.fop.layoutmgr;

// Java
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

// FOP
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.XMLObj;
import org.apache.fop.fo.flow.InstreamForeignObject;
import org.apache.fop.area.inline.ForeignObject;
import org.apache.fop.area.inline.Viewport;

/**
 * LayoutManager for the fo:basic-link formatting object
 */
public class InstreamForeignObjectLM extends LeafNodeLayoutManager {
    private InstreamForeignObject fobj;
    
    /**
     * Constructor
     * @param node the formatting object that creates this area
     */
    public InstreamForeignObjectLM(InstreamForeignObject node) {
        super(node);
        fobj = node;
        Viewport areaCurrent = getInlineArea();
        setCurrentArea(areaCurrent);
        setAlignment(node.getVerticalAlign());
        setLead(areaCurrent.getBPD());
    }

    /**
     * Get the inline area created by this element.
     *
     * @return the viewport inline area
     */
    private Viewport getInlineArea() {
        XMLObj child = (XMLObj) fobj.childNodes.get(0);

        // viewport size is determined by block-progression-dimension
        // and inline-progression-dimension

        // if replaced then use height then ignore block-progression-dimension
        //int h = this.propertyList.get("height").getLength().mvalue();

        // use specified line-height then ignore dimension in height direction
        boolean hasLH = false;//propertyList.get("line-height").getSpecifiedValue() != null;

        Length len;

        int bpd = -1;
        int ipd = -1;
        boolean bpdauto = false;
        if (hasLH) {
            bpd = fobj.getLineHeight().getValue();
        } else {
            // this property does not apply when the line-height applies
            // isn't the block-progression-dimension always in the same
            // direction as the line height?
            len = fobj.getBlockProgressionDimension().getOptimum().getLength();
            if (!len.isAuto()) {
                bpd = len.getValue();
            } else {
                len = fobj.getHeight();
                if (!len.isAuto()) {
                    bpd = len.getValue();
                }
            }
        }

        len = fobj.getInlineProgressionDimension().getOptimum().getLength();
        if (!len.isAuto()) {
            ipd = len.getValue();
        } else {
            len = fobj.getWidth();
            if (!len.isAuto()) {
                ipd = len.getValue();
            }
        }

        // if auto then use the intrinsic size of the content scaled
        // to the content-height and content-width
        int cwidth = -1;
        int cheight = -1;
        len = fobj.getContentWidth();
        if (!len.isAuto()) {
            /*if(len.scaleToFit()) {
                if(ipd != -1) {
                    cwidth = ipd;
                }
            } else {*/
            cwidth = len.getValue();
        }
        len = fobj.getContentHeight();
        if (!len.isAuto()) {
            /*if(len.scaleToFit()) {
                if(bpd != -1) {
                    cwidth = bpd;
                }
            } else {*/
            cheight = len.getValue();
        }

        Point2D csize = new Point2D.Float(cwidth == -1 ? -1 : cwidth / 1000f,
                                          cheight == -1 ? -1 : cheight / 1000f);
        Point2D size = child.getDimension(csize);
        if (size == null) {
            // error
            return null;
        }
        if (cwidth == -1) {
            cwidth = (int)size.getX() * 1000;
        }
        if (cheight == -1) {
            cheight = (int)size.getY() * 1000;
        }
        int scaling = fobj.getScaling();
        if (scaling == Scaling.UNIFORM) {
            // adjust the larger
            double rat1 = cwidth / (size.getX() * 1000f);
            double rat2 = cheight / (size.getY() * 1000f);
            if (rat1 < rat2) {
                // reduce cheight
                cheight = (int)(rat1 * size.getY() * 1000);
            } else {
                cwidth = (int)(rat2 * size.getX() * 1000);
            }
        }

        if (ipd == -1) {
            ipd = cwidth;
        }
        if (bpd == -1) {
            bpd = cheight;
        }

        boolean clip = false;
        if (cwidth > ipd || cheight > bpd) {
            int overflow = fobj.getOverflow();
            if (overflow == Overflow.HIDDEN) {
                clip = true;
            } else if (overflow == Overflow.ERROR_IF_OVERFLOW) {
                fobj.getLogger().error("Instream foreign object overflows the viewport: clipping");
                clip = true;
            }
        }

        int xoffset = fobj.computeXOffset(ipd, cwidth);
        int yoffset = fobj.computeYOffset(bpd, cheight);

        Rectangle2D placement = new Rectangle2D.Float(xoffset, yoffset, cwidth, cheight);

        org.w3c.dom.Document doc = child.getDOMDocument();
        String ns = child.getDocumentNamespace();

        fobj.childNodes = null;
        ForeignObject foreign = new ForeignObject(doc, ns);

        Viewport areaCurrent = new Viewport(foreign);
        areaCurrent.setIPD(ipd);
        areaCurrent.setBPD(bpd);
        areaCurrent.setContentPosition(placement);
        areaCurrent.setClip(clip);
        areaCurrent.setOffset(0);

        return areaCurrent;
    }
    
    protected void addId() {
        addID(fobj.getId());
    }
}

