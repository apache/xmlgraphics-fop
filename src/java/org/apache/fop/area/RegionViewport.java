/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id: RegionViewport.java,v 1.9 2003/03/05 15:19:31 jeremias Exp $
 */ 
package org.apache.fop.area;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import org.apache.fop.datastructs.Node;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.flow.FoPageSequence;

/**
 * Region Viewport reference area.
 * This area is the viewport for a region and contains a region area.
 */
public class RegionViewport
extends AbstractViewport
implements Viewport, Cloneable {
    /**
     * Creates a new region viewport with the given rectangular area
     * @param area the rectangular area
     * @param pageSeq the generating <code>page-sequence</code>
     * @param generatedBy the generating node; in this case, the page sequence
     * @param parent the <code>main-reference-area</code>
     * @param sync
     */
    public RegionViewport(
            Rectangle2D area,
            FoPageSequence pageSeq,
            FONode generatedBy,
            Node parent,
            Object sync) {
        super(area, pageSeq, generatedBy, parent, sync);
    }

    /**
     * Creates a new region viewport with a null rectangular area
     * @param pageSeq the generating <code>page-sequence</code>
     * @param generatedBy the generating node; in this case, the page sequence
     * @param parent the <code>main-reference-area</code>
     * @param sync
     */
    public RegionViewport(
            FoPageSequence pageSeq,
            FONode generatedBy,
            Node parent,
            Object sync) {
        super(pageSeq, generatedBy, parent, sync);
    }

    /**
     * Set the region-reference-area for this region viewport.
     *
     * @param regRef the child region inside this viewport
     */
    public void setRegion(RegionRefArea regRef) {
        setReferenceArea(regRef);
    }

    /**
     * Get the region for this region viewport.
     *
     * @return the child region inside this viewport
     */
    public RegionRefArea getRegion() {
        return (RegionRefArea)(getReferenceArea());
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
        out.writeFloat((float) area.getX());
        out.writeFloat((float) area.getY());
        out.writeFloat((float) area.getWidth());
        out.writeFloat((float) area.getHeight());
        out.writeBoolean(clip);
        //out.writeObject(props);
        out.writeObject(refArea);
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        area = new Rectangle2D.Float(in.readFloat(), in.readFloat(),
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
        synchronized (sync) {
            RegionViewport rv;
            try {
                rv = (RegionViewport)(super.clone());
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
            rv.area = (Rectangle2D)(area.clone());
            rv.refArea = (PageRefArea)(refArea.clone());
            return rv;
        }
    }

}

