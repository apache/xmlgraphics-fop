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

// FOP
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObjMixed;
import org.apache.fop.layoutmgr.AddLMVisitor;
import org.apache.fop.fo.properties.CommonAural;
import org.apache.fop.fo.properties.CommonRelativePosition;

/**
 * fo:bidi-override element.
 */
public class BidiOverride extends FObjMixed {

    /**
     * @param parent FONode that is the parent of this object
     */
    public BidiOverride(FONode parent) {
        super(parent);
    }

    private void setup() {

        // Common Aural Properties
        CommonAural mAurProps = propMgr.getAuralProps();

        // Common Font Properties
        //this.fontState = propMgr.getFontState(area.getFontInfo());

        // Common Margin Properties-Inline
        CommonRelativePosition mProps = propMgr.getRelativePositionProps();

        // this.propertyList.get("color");
        // this.propertyList.get("direction");
        setupID();
        // this.propertyList.get("letter-spacing");
        // this.propertyList.get("line-height");
        // this.propertyList.get("line-height-shift-adjustment");
        // this.propertyList.get("score-spaces");
        // this.propertyList.get("text-shadow");
        // this.propertyList.get("text-transform");
        // this.propertyList.get("unicode-bidi");
        // this.propertyList.get("word-spacing");

    }

    /**
     * @return true (BidiOverride can contain Markers)
     */
    protected boolean containsMarkers() {
        return true;
    }

    /**
     * This is a hook for the AddLMVisitor class to be able to access
     * this object.
     * @param aLMV the AddLMVisitor object that can access this object.
     */
    public void acceptVisitor(AddLMVisitor aLMV) {
        aLMV.serveBidiOverride(this);
    }

    public String getName() {
        return "fo:bidi-override";
    }
}
