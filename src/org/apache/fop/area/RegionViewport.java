/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.io.IOException;

public class RegionViewport extends Area implements Serializable, Cloneable {
    // this rectangle is relative to the page
    RegionReference region;
    Rectangle2D viewArea;
    boolean clip = false;


    public RegionViewport(Rectangle2D viewArea) {
        this.viewArea = viewArea;
    }

    public void setRegion(RegionReference reg) {
        region = reg;
	region.setParent(this);
    }

    public RegionReference getRegion() {
        return region;
    }

    public void setClip(boolean c) {
        clip = c;
    }

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
        out.writeObject(region);
    }

    private void readObject(java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException {
        viewArea = new Rectangle2D.Float(in.readFloat(), in.readFloat(),
					 in.readFloat(), in.readFloat());
        clip = in.readBoolean();
        setRegion( (RegionReference) in.readObject());
    }

    public Object clone() {
        RegionViewport rv = new RegionViewport((Rectangle2D)viewArea.clone());
        rv.region = (RegionReference)region.clone();
        rv.region.setParent(rv);
        return rv;
    }
}
