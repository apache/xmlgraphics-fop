/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area.inline;

import org.apache.fop.area.Area;

// cacheable object
// image object, mime type, url
public class Image extends Area {
    String url;

    public Image(String u) {
        url = u;
    }

    public String getURL() {
        return url;
    }

}
