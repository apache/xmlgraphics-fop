/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

import java.awt.geom.Rectangle2D;

public class RegionViewport {
    // this rectangle is relative to the page
    Rectangle2D regionArea;
    boolean clip;

    Region region;

    public void setRegion(Region reg) {
        region = reg;
    }

    public Region getRegion() {
        return region;
    }

}
