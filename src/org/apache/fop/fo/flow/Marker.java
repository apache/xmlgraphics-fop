/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.*;
import org.apache.fop.datatypes.*;
import org.apache.fop.apps.FOPException;

public class Marker extends FObjMixed {

    private String markerClassName;
    private Area registryArea;
    private boolean isFirst;
    private boolean isLast;

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent,
                         PropertyList propertyList) throws FOPException {
            return new Marker(parent, propertyList);
        }

    }

    public static FObj.Maker maker() {
        return new Marker.Maker();
    }

    public Marker(FObj parent, PropertyList propertyList)
      throws FOPException {
        super(parent, propertyList);

        // do check to see that 'this' is under fo:flow

        this.markerClassName =
            this.properties.get("marker-class-name").getString();

        // check to ensure that no other marker with same parent
        // has this 'marker-class-name' is in addMarker() method
        parent.addMarker(this.markerClassName);
    }

    public String getName() {
        return "fo:marker";
    }

    public int layout(Area area) throws FOPException {
        // no layout action desired
        this.registryArea = area;
        area.getPage().registerMarker(this);
        return Status.OK;
    }

    public int layoutMarker(Area area) throws FOPException {
        if (this.marker == START)
            this.marker = 0;

        int numChildren = this.children.size();
        for (int i = this.marker; i < numChildren; i++) {
            FONode fo = (FONode)children.get(i);

            int status;
            if (Status.isIncomplete((status = fo.layout(area)))) {
                this.marker = i;
                return status;
            }
        }

        return Status.OK;
    }

    public String getMarkerClassName() {
        return markerClassName;
    }

    public Area getRegistryArea() {
        return registryArea;
    }

    public boolean mayPrecedeMarker() {
        return true;
    }  

    // The page the marker was registered is put into the renderer
    // queue. The marker is transferred to it's own marker list,
    // release the area for GC. We also know now whether the area is
    // first/last.
    public void releaseRegistryArea() {
        isFirst = registryArea.isFirst();
        isLast = registryArea.isLast();
        registryArea = null;
    }
    
    // This has actually nothing to do with resseting this marker,
    // but the 'marker' from FONode, marking layout status.
    // Called in case layout is to be rolled back. Unregister this
    // marker from the page, it isn't laid aout anyway.
    public void resetMarker() {
        if (registryArea != null ) {
            Page page=registryArea.getPage();
            if (page != null) {
                page.unregisterMarker(this);
            }
        }
    }

    // More hackery: reset layout status marker. Called before the
    // content is laid out from RetrieveMarker.
    public void resetMarkerContent() {
        super.resetMarker();
    }
}
