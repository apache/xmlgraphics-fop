/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.fo.flow;

// XML
import org.xml.sax.Attributes;

// FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObjMixed;
import org.apache.fop.layoutmgr.AddLMVisitor;

/**
 * Marker formatting object.
 * This is the marker formatting object that handles merkers.
 * This attempts to add itself to the parent formatting object.
 */
public class Marker extends FObjMixed {

    private String markerClassName;

    /**
     * Create a marker fo.
     *
     * @param parent the parent fo node
     */
    public Marker(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#addProperties
     */
    protected void addProperties(Attributes attlist) throws FOPException {
        super.addProperties(attlist);
        this.markerClassName =
            this.propertyList.get(PR_MARKER_CLASS_NAME).getString();
    }

    /**
     * Get the marker class name for this marker.
     *
     * @return the marker class name
     */
    public String getMarkerClassName() {
        return markerClassName;
    }

    public void acceptVisitor(AddLMVisitor aLMV) {
        aLMV.serveMarker(this);
    }

    public String getName() {
        return "fo:marker";
    }
}
