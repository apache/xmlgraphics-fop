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
import org.apache.fop.fo.FObjMixed;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.Status;
import org.apache.fop.fo.FONode;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.Page;
import org.apache.fop.apps.FOPException;

public class Marker extends FObjMixed {

    private String markerClassName;
    private Area registryArea;
    private boolean isFirst;
    private boolean isLast;

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent, PropertyList propertyList,
                         String systemId, int line, int column)
            throws FOPException {
            return new Marker(parent, propertyList, systemId, line, column);
        }

    }

    public static FObj.Maker maker() {
        return new Marker.Maker();
    }

    public Marker(FObj parent, PropertyList propertyList,
                  String systemId, int line, int column)
        throws FOPException {
        super(parent, propertyList, systemId, line, column);

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
