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
        if (areaclass == RegionReference.BEFORE) {
            regionBefore = port;
        } else if (areaclass == RegionReference.START) {
            regionStart = port;
        } else if (areaclass == RegionReference.BODY) {
            regionBody = port;
        } else if (areaclass == RegionReference.END) {
            regionEnd = port;
        } else if (areaclass == RegionReference.AFTER) {
            regionAfter = port;
        }
    }

    public RegionViewport getRegion(int areaclass) {
        if (areaclass == RegionReference.BEFORE) {
            return regionBefore;
        } else if (areaclass == RegionReference.START) {
            return regionStart;
        } else if (areaclass == RegionReference.BODY) {
            return regionBody;
        } else if (areaclass == RegionReference.END) {
            return regionEnd;
        } else if (areaclass == RegionReference.AFTER) {
            return regionAfter;
        }
        return null;
    }

}
