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
import org.apache.fop.fo.FOTreeVisitor;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAural;
import org.apache.fop.fo.properties.CommonBorderAndPadding;
import org.apache.fop.fo.properties.CommonBackground;
import org.apache.fop.fo.properties.CommonMarginInline;
import org.apache.fop.fo.properties.CommonRelativePosition;

/**
 * The basic link.
 * This sets the basic link trait on the inline parent areas
 * that are created by the fo element.
 */
public class BasicLink extends Inline {

    private String link = null;
    private boolean external = false;

    /**
     * @param parent FONode that is the parent of this object
     */
    public BasicLink(FONode parent) {
        super(parent);
    }

    public void setup() {
        String destination;
        int linkType;

        // Common Accessibility Properties
        CommonAccessibility mAccProps = propMgr.getAccessibilityProps();

        // Common Aural Properties
        CommonAural mAurProps = propMgr.getAuralProps();

        // Common Border, Padding, and Background Properties
        CommonBorderAndPadding bap = propMgr.getBorderAndPadding();
        CommonBackground bProps = propMgr.getBackgroundProps();

        // Common Margin Properties-Inline
        CommonMarginInline mProps = propMgr.getMarginInlineProps();

        // Common Relative Position Properties
        CommonRelativePosition mRelProps = propMgr.getRelativePositionProps();

        // this.propertyList.get("alignment-adjust");
        // this.propertyList.get("alignment-baseline");
        // this.propertyList.get("baseline-shift");
        // this.propertyList.get("destination-place-offset");
        // this.propertyList.get("dominant-baseline");
        String ext =  propertyList.get(PR_EXTERNAL_DESTINATION).getString();
        setupID();
        // this.propertyList.get("indicate-destination");
        String internal = propertyList.get(PR_INTERNAL_DESTINATION).getString();
        if (ext.length() > 0) {
            link = ext;
            external = true;
        } else if (internal.length() > 0) {
            link = internal;
        } else {
            getLogger().error("basic-link requires an internal or external destination");
        }
        // this.propertyList.get("keep-together");
        // this.propertyList.get("keep-with-next");
        // this.propertyList.get("keep-with-previous");
        // this.propertyList.get("line-height");
        // this.propertyList.get("line-height-shift-adjustment");
        // this.propertyList.get("show-destination");
        // this.propertyList.get("target-processing-context");
        // this.propertyList.get("target-presentation-context");
        // this.propertyList.get("target-stylesheet");

    }

    /**
     * @return true (BasicLink can contain Markers)
     */
    protected boolean containsMarkers() {
        return true;
    }

    /**
     * @return the String value of the link
     */
    public String getLink() {
        return link;
    }

    /**
     * @return true if the link is external, false otherwise
     */
    public boolean getExternal() {
        return external;
    }

    /**
     * This is a hook for an FOTreeVisitor subclass to be able to access
     * this object.
     * @param fotv the FOTreeVisitor subclass that can access this object.
     * @see org.apache.fop.fo.FOTreeVisitor
     */
    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.serveBasicLink(this);
    }

    /**
     * @see org.apache.fop.fo.FObj#handleAttrs
     */
    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);

        getFOTreeControl().getFOInputHandler().startLink(this);
    }
    
    /**
     * @see org.apache.fop.fo.FONode#end
     */
    public void end() {
        super.end();
        
        getFOTreeControl().getFOInputHandler().endLink();
    }
}
