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

// Java
import java.util.Vector;

import org.xml.sax.Attributes;

public class RetrieveMarker extends FObjMixed {

    private String retrieveClassName;
    private int retrievePosition;
    private int retrieveBoundary;

    public RetrieveMarker(FONode parent) {
        super(parent);
    }

    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);
        this.retrieveClassName =
            this.properties.get("retrieve-class-name").getString();
        this.retrievePosition =
            this.properties.get("retrieve-position").getEnum();
        this.retrieveBoundary =
            this.properties.get("retrieve-boundary").getEnum();
    }

    public Status layout(Area area) throws FOPException {
        // locate qualifying areas by 'marker-class-name' and
        // 'retrieve-boundary'. Initially we will always check
        // the containing page
        Page containingPage = area.getPage();
        Marker bestMarker = searchPage(containingPage, true);

        // if marker not yet found, and 'retrieve-boundary' permits,
        // search forward by Page
        if ((null == bestMarker)
                && (retrieveBoundary != RetrieveBoundary.PAGE)) {
            // System.out.println("Null bestMarker and searching...");
            Page currentPage = containingPage;
            boolean isFirstCall = true;
            while (bestMarker == null) {
                Page previousPage = locatePreviousPage(currentPage,
                                                       retrieveBoundary,
                                                       isFirstCall);
                isFirstCall = false;
                // System.out.println("Previous page = '" + previousPage + "'");
                bestMarker = searchPage(previousPage, false);
                currentPage = previousPage;
            }
        }

        Status status = new Status(Status.OK);
        if (null != bestMarker) {
            // System.out.println("Laying out marker '" + bestMarker + "' in area '" + area + "'");
            // the 'markers' referred to in this method are internal; they have
            // nothing to do with fo:marker
            bestMarker.resetMarker();
            status = bestMarker.layoutMarker(area);
        }

        return status;
    }

    private Marker searchPage(Page page,
                              boolean isContainingPage) throws FOPException {
        Vector pageMarkers = page.getMarkers();
        if (pageMarkers.isEmpty()) {
            // System.out.println("No markers on page");
            return null;
        }

        // if no longer the containing page (fo:retrieve-marker, or the page
        // being processed), grab the last qualifying marker on this one
        if (!isContainingPage) {
            for (int c = pageMarkers.size(); c > 0; c--) {
                Marker currentMarker = (Marker)pageMarkers.elementAt(c - 1);
                if (currentMarker.getMarkerClassName().equals(retrieveClassName)) {
                    return currentMarker;
                }
            }
        }

        // search forward if 'first-starting-within-page' or
        // 'first-including-carryover'
        if (retrievePosition == RetrievePosition.FIC) {
            for (int c = 0; c < pageMarkers.size(); c++) {
                Marker currentMarker = (Marker)pageMarkers.elementAt(c);
                if (currentMarker.getMarkerClassName().equals(retrieveClassName)) {
                    return currentMarker;
                }
            }

        } else if (retrievePosition == RetrievePosition.FSWP) {
            for (int c = 0; c < pageMarkers.size(); c++) {
                Marker currentMarker = (Marker)pageMarkers.elementAt(c);
                if (currentMarker.getMarkerClassName().equals(retrieveClassName)) {
                    if (currentMarker.getRegistryArea().isFirst()) {
                        return currentMarker;
                    }
                }
            }

        } else if (retrievePosition == RetrievePosition.LSWP) {
            for (int c = pageMarkers.size(); c > 0; c--) {
                Marker currentMarker = (Marker)pageMarkers.elementAt(c - 1);
                if (currentMarker.getMarkerClassName().equals(retrieveClassName)) {
                    if (currentMarker.getRegistryArea().isFirst()) {
                        return currentMarker;
                    }
                }
            }

        } else if (retrievePosition == RetrievePosition.LEWP) {
            for (int c = pageMarkers.size(); c > 0; c--) {
                Marker currentMarker = (Marker)pageMarkers.elementAt(c - 1);
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

    private Page locatePreviousPage(Page page, int retrieveBoundary,
                                    boolean isFirstCall) {
        boolean pageWithinSequence = true;
        if (retrieveBoundary == RetrieveBoundary.DOCUMENT)
            pageWithinSequence = false;
        return page.getAreaTree().getPreviousPage(page, pageWithinSequence,
        isFirstCall);
    }

}
