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

package org.apache.fop.fo.pagination;

// XML
import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.flow.InlineLevel;

/**
 * Class modelling the fo:title object.
 */
public class Title extends InlineLevel {
    // The value of properties relevant for fo:title.
    // See superclass InlineLevel
    // End of property values

    /**
     * @param parent FONode that is the parent of this object
     */
    public Title(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
        XSL/FOP: (#PCDATA|%inline;)*
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
        throws ValidationException {
        if (!isInlineItem(nsURI, localName)) {
            invalidChildError(loc, nsURI, localName);
        }
    }

    /**
     * @see org.apache.fop.fo.FObj#getName()
     */
    public String getName() {
        return "fo:title";
    }

    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_TITLE;
    }
}

