/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layout;

import org.apache.fop.fo.properties.*;
import org.apache.fop.datatypes.ColorType;

public class RegionArea {

    protected int xPosition;
    protected int yPosition;
    protected int width;
    protected int height;

    protected ColorType backgroundColor;

    public RegionArea(int xPosition, int yPosition, int width, int height) {
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.width = width;
        this.height = height;
    }

    public AreaContainer makeAreaContainer() {
        return new AreaContainer(null, xPosition, yPosition, width, height,
                                 Position.ABSOLUTE);
    }

    public ColorType getBackgroundColor() {
        return this.backgroundColor;
    }

    public void setBackgroundColor(ColorType bgColor) {
        this.backgroundColor = bgColor;
    }

    public int getHeight() {
        return height;
    }

}
