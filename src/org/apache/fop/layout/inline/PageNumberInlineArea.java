/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layout.inline;

import org.apache.fop.render.Renderer;
import org.apache.fop.datatypes.IDNode;
import org.apache.fop.layout.*;

public class PageNumberInlineArea extends WordArea {


    public PageNumberInlineArea(FontState fontState, float red, float green,
                                float blue, String refid, int width) {
        super(fontState, red, green, blue, "?", width);
        this.pageNumberId = refid;
    }

}
