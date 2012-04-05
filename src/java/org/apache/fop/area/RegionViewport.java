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

package org.apache.fop.area;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.HashMap;

import org.apache.fop.traits.WritingModeTraitsGetter;

/**
 * Region Viewport area.
 * This object represents the region-viewport-area.  It has a
 * region-reference-area as its child.  These areas are described
 * in the fo:region-body description in the XSL Recommendation.
 */
public class RegionViewport extends Area implements Cloneable, Viewport {

    private static final long serialVersionUID = 505781815165102572L;

    // this rectangle is relative to the page
    private RegionReference regionReference;
    private Rectangle2D viewArea;
    private boolean clip = false;

    /**
     * Create a new region-viewport-area
     *
     * @param viewArea the view area of this viewport
     */
    public RegionViewport(Rectangle2D viewArea) {
        this.viewArea = viewArea;
        addTrait(Trait.IS_VIEWPORT_AREA, Boolean.TRUE);
    }

    /**
     * Set the region-reference-area for this region viewport.
     *
     * @param reg the child region-reference-area inside this viewport
     */
    public void setRegionReference(RegionReference reg) {
        regionReference = reg;
    }

    /**
     * Get the region-reference-area for this region viewport.
     *
     * @return the child region-reference-area inside this viewport
     */
    public RegionReference getRegionReference() {
        return regionReference;
    }

    /**
     * Set the clipping for this region viewport.
     *
     * @param c the clipping value
     */
    public void setClip(boolean c) {
        clip = c;
    }

    /** {@inheritDoc} */
    public boolean hasClip() {
        return this.clip;
    }

    /** {@inheritDoc} */
    public Rectangle getClipRectangle() {
        if (clip) {
            return new Rectangle(getIPD(), getBPD());
        } else {
            return null;
        }
    }

    /**
     * Get the view area of this viewport.
     *
     * @return the viewport rectangle area
     */
    public Rectangle2D getViewArea() {
        return viewArea;
    }

    private void writeObject(java.io.ObjectOutputStream out)
    throws IOException {
        out.writeFloat((float) viewArea.getX());
        out.writeFloat((float) viewArea.getY());
        out.writeFloat((float) viewArea.getWidth());
        out.writeFloat((float) viewArea.getHeight());
        out.writeBoolean(clip);
        out.writeObject(traits);
        out.writeObject(regionReference);
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        viewArea = new Rectangle2D.Float(in.readFloat(), in.readFloat(),
                                         in.readFloat(), in.readFloat());
        clip = in.readBoolean();
        traits = (HashMap)in.readObject();
        setRegionReference((RegionReference) in.readObject());
    }

    /**
     * Clone this region viewport.
     * Used when creating a copy from the page master.
     *
     * @return a new copy of this region viewport
     */
    public Object clone() {
        RegionViewport rv = new RegionViewport((Rectangle2D)viewArea.clone());
        rv.regionReference = (RegionReference)regionReference.clone();
        if (traits != null) {
            rv.traits = new HashMap(traits);
        }
        if (foreignAttributes != null) {
            rv.foreignAttributes = new HashMap(foreignAttributes);
        }
        return rv;
    }

    /**
     * Sets the writing mode traits for the region reference of
     * this region viewport
     * @param wmtg a WM traits getter
     */
    public void setWritingModeTraits(WritingModeTraitsGetter wmtg) {
        if (regionReference != null) {
            regionReference.setWritingModeTraits(wmtg);
        }
    }

}

