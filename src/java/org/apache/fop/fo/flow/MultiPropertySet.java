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
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

// FOP
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOTreeVisitor;
import org.apache.fop.fo.ToBeImplementedElement;

/**
 * Class modelling the fo:multi-property-set object. See Sec. 6.9.7 of the
 * XSL-FO Standard.
 */
public class MultiPropertySet extends ToBeImplementedElement {

    /**
     * @param parent FONode that is the parent of this object
     */
    public MultiPropertySet(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
     * XSL Content Model: empty
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
        throws SAXParseException {
            invalidChildError(loc, nsURI, localName);
    }

    private void setup() {
        setupID();
        // this.propertyList.get("active-state");
    }

    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.serveMultiPropertySet(this);
    }

    public String getName() {
        return "fo:multi-property-set";
    }
}
