/*
 * $Id: RegionViewport.java,v 1.9 2003/03/05 15:19:31 jeremias Exp $
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

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import org.apache.fop.datastructs.Node;

/**
 * Region Viewport reference area.
 * This area is the viewport for a region and contains a region area.
 */
public class RegionViewport extends Area implements Viewport, Cloneable {
    // this rectangle is relative to the page
    private RegionRefArea region;
    private Rectangle2D viewArea;
    private boolean clip = false;

    /**
     * Create a new region viewport.
     *
     * @param viewArea the view area of this viewport
     */
    public RegionViewport(Node parent, Object sync, Rectangle2D viewArea) {
        super(parent, sync);
        this.viewArea = viewArea;
    }

    /**
     * Set the region for this region viewport.
     *
     * @param reg the child region inside this viewport
     */
    public void setRegion(RegionRefArea reg) {
        region = reg;
    }

    /**
     * Get the region for this region viewport.
     *
     * @return the child region inside this viewport
     */
    public RegionRefArea getRegion() {
        return region;
    }

    /**
     * Set the clipping for this region viewport.
     *
     * @param c the clipping value
     */
    public void setClip(boolean c) {
        clip = c;
    }

    /**
     * Get the view area of this viewport.
     *
     * @return the viewport rectangle area
     */
    public Rectangle2D getViewArea() {
        return viewArea;
    }

    /**
     * Return the sum of region border- and padding-before
     *
     * @return width in millipoints
     */
//    public int getBorderAndPaddingWidthBefore() {
//        int margin = 0;
//        BorderProps bps = (BorderProps) getTrait(Trait.BORDER_BEFORE);
//        if (bps != null) {
//            margin = bps.width;
//        }
//        
//        Integer padWidth = (Integer) getTrait(Trait.PADDING_BEFORE);
//        if (padWidth != null) {
//            margin += padWidth.intValue();
//        }
//
//        return margin;
//    }
    
    /**
     * Return the sum of region border- and padding-after
     *
     * @return width in millipoints
     */
//    public int getBorderAndPaddingWidthAfter() {
//        int margin = 0;
//        
//        BorderProps bps = (BorderProps) getTrait(Trait.BORDER_AFTER);
//        if (bps != null) {
//            margin = bps.width;
//        }
//        
//        Integer padWidth = (Integer) getTrait(Trait.PADDING_AFTER);
//        if (padWidth != null) {
//            margin += padWidth.intValue();
//        }
//
//        return margin;
//    }

    /**
     * Return the sum of region border- and padding-start
     *
     * @return width in millipoints
     */
//    public int getBorderAndPaddingWidthStart() {
//        int margin = 0;
//        BorderProps bps = (BorderProps) getTrait(Trait.BORDER_START);
//        if (bps != null) {
//            margin = bps.width;
//        }
//        
//        Integer padWidth = (Integer) getTrait(Trait.PADDING_START);
//        if (padWidth != null) {
//            margin += padWidth.intValue();
//        }
//
//        return margin;
//    }

    /**
     * Return the sum of region border- and padding-end
     *
     * @return width in millipoints
     */
//    public int getBorderAndPaddingWidthEnd() {
//        int margin = 0;
//        BorderProps bps = (BorderProps) getTrait(Trait.BORDER_END);
//        if (bps != null) {
//            margin = bps.width;
//        }
//        
//        Integer padWidth = (Integer) getTrait(Trait.PADDING_END);
//        if (padWidth != null) {
//            margin += padWidth.intValue();
//        }
//
//        return margin;
//    }

    /**
     * @param out
     * @throws IOException
     */
    private void writeObject(java.io.ObjectOutputStream out)
    throws IOException {
        out.writeFloat((float) viewArea.getX());
        out.writeFloat((float) viewArea.getY());
        out.writeFloat((float) viewArea.getWidth());
        out.writeFloat((float) viewArea.getHeight());
        out.writeBoolean(clip);
        //out.writeObject(props);
        out.writeObject(region);
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        viewArea = new Rectangle2D.Float(in.readFloat(), in.readFloat(),
                                         in.readFloat(), in.readFloat());
        clip = in.readBoolean();
        //props = (HashMap)in.readObject();
        setRegion((RegionRefArea) in.readObject());
    }

    /**
     * Clone this region viewport.
     * Used when creating a copy from the page master.
     *
     * @return a new copy of this region viewport
     */
    public Object clone() {
        RegionViewport rv =
            new RegionViewport(parent, sync, (Rectangle2D)viewArea.clone());
        rv.region = (RegionRefArea)region.clone();
//        if (props != null) {
//            rv.props = (HashMap)props.clone();
//        }
        return rv;
    }
}

