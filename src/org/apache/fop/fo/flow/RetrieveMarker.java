/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
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
        public FObj make(FObj parent, PropertyList propertyList,
                         String systemId, int line, int column)
            throws FOPException {
            return new RetrieveMarker(parent, propertyList,
                                      systemId, line, column);
        }

    }

    public static FObj.Maker maker() {
        return new RetrieveMarker.Maker();
    }

    public RetrieveMarker(FObj parent, PropertyList propertyList,
                          String systemId, int line, int column) {
        super(parent, propertyList, systemId, line, column);

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
            } else if (retrieveBoundary != RetrieveBoundary.PAGE) {
                throw new FOPException("Illegal 'retrieve-boundary' value", systemId, line, column);
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
            throw new FOPException("Illegal 'retrieve-position' value", systemId, line, column);
        }
        return null;
    }

}
