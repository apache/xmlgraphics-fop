/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.Status;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.fo.properties.RetrieveBoundary;
import org.apache.fop.fo.properties.RetrievePosition;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.AreaTree;
import org.apache.fop.layout.Page;
import org.apache.fop.apps.FOPException;

// Java
import java.util.ArrayList;

public class RetrieveMarker extends FObj {

    private String retrieveClassName;
    private int retrievePosition;
    private int retrieveBoundary;
    private Marker bestMarker;

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent,
                         PropertyList propertyList) throws FOPException {
            return new RetrieveMarker(parent, propertyList);
        }

    }

    public static FObj.Maker maker() {
        return new RetrieveMarker.Maker();
    }

    public RetrieveMarker(FObj parent, PropertyList propertyList) {
        super(parent, propertyList);

        this.retrieveClassName =
            this.properties.get("retrieve-class-name").getString();
        this.retrievePosition =
            this.properties.get("retrieve-position").getEnum();
        this.retrieveBoundary =
            this.properties.get("retrieve-boundary").getEnum();
    }

    public String getName() {
        return "fo:retrieve-marker";
    }

    public int layout(Area area) throws FOPException {
        if (marker == START) {
            marker = 0;
            // locate qualifying areas by 'marker-class-name' and
            // 'retrieve-boundary'. Initially we will always check
            // the containing page
            Page containingPage = area.getPage();
            bestMarker = searchPage(containingPage);

            if (bestMarker != null) {
                bestMarker.resetMarkerContent();
                return bestMarker.layoutMarker(area);
            }
            // If marker not yet found, and 'retrieve-boundary' permits,
            // search backward.
            AreaTree areaTree = containingPage.getAreaTree();
            if (retrieveBoundary == RetrieveBoundary.PAGE_SEQUENCE) {
                PageSequence pageSequence = areaTree.getCurrentPageSequence();
                if (pageSequence == containingPage.getPageSequence() ) {
                    return layoutBestMarker(areaTree.getCurrentPageSequenceMarkers(),area);
                }
            } else if (retrieveBoundary == RetrieveBoundary.DOCUMENT) {
                return layoutBestMarker(areaTree.getDocumentMarkers(),area);
            } else {
                throw new FOPException("Illegal 'retrieve-boundary' value");
            }
        } else if (bestMarker != null) {
            return bestMarker.layoutMarker(area);
        }
        return Status.OK;
    }

    private int layoutBestMarker(ArrayList markers, Area area)
        throws FOPException {
        if (markers!=null) {
            for (int i = markers.size() - 1; i >= 0; i--) {
                Marker currentMarker = (Marker)markers.get(i);
                if (currentMarker.getMarkerClassName().equals(retrieveClassName)) {
                    bestMarker = currentMarker;
                    bestMarker.resetMarkerContent();
                    return bestMarker.layoutMarker(area);
                }
            }
        }
        return Status.OK;
    }
    
    private Marker searchPage(Page page) throws FOPException {
        ArrayList pageMarkers = page.getMarkers();
        if (pageMarkers.isEmpty()) {
            return null;
        }

        // search forward if 'first-starting-within-page' or
        // 'first-including-carryover'
        if (retrievePosition == RetrievePosition.FIC) {
            for (int i = 0; i < pageMarkers.size(); i++) {
                Marker currentMarker = (Marker)pageMarkers.get(i);
                if (currentMarker.getMarkerClassName().equals(retrieveClassName)) {
                    return currentMarker;
                }
            }
        } else if (retrievePosition == RetrievePosition.FSWP) {
            for (int i = 0; i < pageMarkers.size(); i++) {
                Marker currentMarker = (Marker)pageMarkers.get(i);
                if (currentMarker.getMarkerClassName().equals(retrieveClassName)) {
                    if (currentMarker.getRegistryArea().isFirst()) {
                        return currentMarker;
                    }
                }
            }
        } else if (retrievePosition == RetrievePosition.LSWP) {
            for (int i = pageMarkers.size() - 1; i >= 0; i--) {
                Marker currentMarker = (Marker)pageMarkers.get(i);
                if (currentMarker.getMarkerClassName().equals(retrieveClassName)) {
                    if (currentMarker.getRegistryArea().isFirst()) {
                        return currentMarker;
                    }
                }
            }

        } else if (retrievePosition == RetrievePosition.LEWP) {
            for (int i = pageMarkers.size() - 1; i >= 0; i--) {
                Marker currentMarker = (Marker)pageMarkers.get(i);
                if (currentMarker.getMarkerClassName().equals(retrieveClassName)) {
                    if (currentMarker.getRegistryArea().isLast()) {
                        return currentMarker;
                    }
                }
            }

        } else {
            throw new FOPException("Illegal 'retrieve-position' value");
        }
        return null;
    }

}
