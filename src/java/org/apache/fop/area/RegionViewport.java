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
 
package org.apache.fop.area;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.HashMap;

/**
 * Region Viewport reference area.
 * This area is the viewport for a region and contains a region area.
 */
public class RegionViewport extends Area implements Cloneable {
    // this rectangle is relative to the page
    private RegionReference region;
    private Rectangle2D viewArea;
    private boolean clip = false;

    /**
     * Create a new region viewport.
     *
     * @param viewArea the view area of this viewport
     */
    public RegionViewport(Rectangle2D viewArea) {
        this.viewArea = viewArea;
        addTrait(Trait.IS_VIEWPORT_AREA, Boolean.TRUE);
    }

    /**
     * Set the region for this region viewport.
     *
     * @param reg the child region inside this viewport
     */
    public void setRegion(RegionReference reg) {
        region = reg;
    }

    /**
     * Get the region for this region viewport.
     *
     * @return the child region inside this viewport
     */
    public RegionReference getRegion() {
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

    private void writeObject(java.io.ObjectOutputStream out)
    throws IOException {
        out.writeFloat((float) viewArea.getX());
        out.writeFloat((float) viewArea.getY());
        out.writeFloat((float) viewArea.getWidth());
        out.writeFloat((float) viewArea.getHeight());
        out.writeBoolean(clip);
        out.writeObject(props);
        out.writeObject(region);
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        viewArea = new Rectangle2D.Float(in.readFloat(), in.readFloat(),
                                         in.readFloat(), in.readFloat());
        clip = in.readBoolean();
        props = (HashMap)in.readObject();
        setRegion((RegionReference) in.readObject());
    }

    /**
     * Clone this region viewport.
     * Used when creating a copy from the page master.
     *
     * @return a new copy of this region viewport
     */
    public Object clone() {
        RegionViewport rv = new RegionViewport((Rectangle2D)viewArea.clone());
        rv.region = (RegionReference)region.clone();
        if (props != null) {
            rv.props = (HashMap)props.clone();
        }
        return rv;
    }
}

