/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

import java.io.Serializable;

public class Page implements Serializable {
    // contains before, start, body, end and after regions
    RegionViewport regionBefore = null;
    RegionViewport regionStart = null;
    RegionViewport regionBody = null;
    RegionViewport regionEnd = null;
    RegionViewport regionAfter = null;

    public void setRegion(int areaclass, RegionViewport port) {
        if (areaclass == Region.BEFORE) {
            regionBefore = port;
        } else if (areaclass == Region.START) {
            regionStart = port;
        } else if (areaclass == Region.BODY) {
            regionBody = port;
        } else if (areaclass == Region.END) {
            regionEnd = port;
        } else if (areaclass == Region.AFTER) {
            regionAfter = port;
        }
    }

    public RegionViewport getRegion(int areaclass) {
        if (areaclass == Region.BEFORE) {
            return regionBefore;
        } else if (areaclass == Region.START) {
            return regionStart;
        } else if (areaclass == Region.BODY) {
            return regionBody;
        } else if (areaclass == Region.END) {
            return regionEnd;
        } else if (areaclass == Region.AFTER) {
            return regionAfter;
        }
        return null;
    }

}
