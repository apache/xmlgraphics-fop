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
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FOTreeVisitor;
import org.apache.fop.fo.properties.CommonAccessibility;

/**
 * Class modelling the fo:list-item-label object. See Sec. 6.8.5 of the XSL-FO
 * Standard.
 */
public class ListItemLabel extends FObj {

    /**
     * @param parent FONode that is the parent of this object
     */
    public ListItemLabel(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#addProperties
     */
    protected void addProperties(Attributes attlist) throws FOPException {
        super.addProperties(attlist);
        getFOTreeControl().getFOInputHandler().startListLabel();
    }

    private void setup() {

        // Common Accessibility Properties
        CommonAccessibility mAccProps = propMgr.getAccessibilityProps();

        setupID();
        // this.propertyList.get("keep-together");

        /*
         * For calculating the lineage - The fo:list-item-label formatting object
         * does not generate any areas. The fo:list-item-label formatting object
         * returns the sequence of areas created by concatenating the sequences
         * of areas returned by each of the children of the fo:list-item-label.
         */

    }

    /**
     * @return true (ListItemLabel may contain Markers)
     */
    protected boolean containsMarkers() {
        return true;
    }

    /**
     * This is a hook for an FOTreeVisitor subclass to be able to access
     * this object.
     * @param fotv the FOTreeVisitor subclass that can access this object.
     * @see org.apache.fop.fo.FOTreeVisitor
     */
    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.serveListItemLabel(this);
    }

    protected void end() {
        super.end();
        
        getFOTreeControl().getFOInputHandler().endListLabel();
    }
}

