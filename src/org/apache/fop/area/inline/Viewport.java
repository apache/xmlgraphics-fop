/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area.inline;

import org.apache.fop.area.Area;

import java.awt.geom.Rectangle2D;

public class Viewport extends InlineArea {
    // contents could be foreign object or image
    Area content;
    // an inline-level viewport area for graphic and instream foreign object
    boolean clip = false;
    // position relative to this area
    Rectangle2D contentPosition;

}
