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

public class RegionViewport implements Serializable {
    // this rectangle is relative to the page
    Rectangle2D regionArea;
    boolean clip = false;

    Region region;

    public RegionViewport(Rectangle2D area) {
        regionArea = area;
    }

    public void setRegion(Region reg) {
        region = reg;
    }

    public Rectangle2D getViewArea() {
        return regionArea;
    }

    public Region getRegion() {
        return region;
    }

    private void writeObject(java.io.ObjectOutputStream out)
    throws IOException {
        out.writeFloat((float) regionArea.getX());
        out.writeFloat((float) regionArea.getY());
        out.writeFloat((float) regionArea.getWidth());
        out.writeFloat((float) regionArea.getHeight());
        out.writeBoolean(clip);
        out.writeObject(region);
    }

    private void readObject(java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException {
        regionArea = new Rectangle2D.Float(in.readFloat(), in.readFloat(),
                                           in.readFloat(), in.readFloat());
        clip = in.readBoolean();
        region = (Region) in.readObject();
    }

}
